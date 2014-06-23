package com.appdynamics.extensions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.appdynamics.extensions.eventsreporter.email.EmailAction;
import com.appdynamics.extensions.eventsreporter.email.EmailHelper;
import com.appdynamics.extensions.eventsreporter.http.HttpAction;
import com.appdynamics.extensions.eventsreporter.http.HttpHandler;
import com.appdynamics.extensions.eventsreporter.model.Action;
import com.appdynamics.extensions.eventsreporter.model.Command;
import com.appdynamics.extensions.eventsreporter.model.EmailData;
import com.appdynamics.extensions.eventsreporter.model.Events;
import com.appdynamics.extensions.eventsreporter.pagerduty.PagerDutyAction;
import com.appdynamics.extensions.eventsreporter.pagerduty.PagerDutyHelper;
import com.appdynamics.extensions.eventsreporter.pagerduty.Response;
import com.appdynamics.extensions.eventsreporter.parser.DomXmlParser;
import com.appdynamics.extensions.eventsreporter.utils.EventsReporterConstants;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;


public class EventsReporter extends AManagedMonitor {

	private static Logger logger = Logger.getLogger("com.singularity.extensions.EventsReporter");

	private static Map<Integer, EmailData> cmdEmailData = new HashMap<Integer, EmailData>();
	private static Map<Integer, String> cmdIncidentKey = new HashMap<Integer, String>();
	private static final String osName = System.getProperty("os.name").toLowerCase();
	private static boolean isWindows = false;
	private static boolean isUnix = false;
	private boolean parseXml = true;
	private boolean cmdsScheduled = false;
	private Events events = null;
	private String xmlFilePath = "";
	private EmailHelper emailHelper = new EmailHelper();
	private HttpHandler httpHandler = new HttpHandler();

	private static ScheduledExecutorService scheduledExecutorService = null;


	public EventsReporter(){
		logger.info("OS name = " + osName);
		if(osName.indexOf("win") >= 0)
			isWindows = true;
		else if(osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0 )
			isUnix = true;
		else 
			logger.info("OS = " + osName + " not supported");
	}

	public TaskOutput execute(Map<String, String> argsMap, TaskExecutionContext executionContext) throws TaskExecutionException {

		TaskOutput out = null;
		if(!cmdsScheduled){
			if (!argsMap.containsKey(EventsReporterConstants.PROPERTIES_PATH)) {
				logger.error("monitor.xml needs to contain a task-argument 'properties-path' describingthe path to the xml-file with the properties. Quitting Events Reporter.");
				return null;
			}else{
				xmlFilePath = argsMap.get(EventsReporterConstants.PROPERTIES_PATH);
				this.execute();
			}	
			out = new TaskOutput("Events Reporter task Scheduled successfully.");
			cmdsScheduled = true;
		}else{
			out = new TaskOutput("Events Reporter task already Scheduled.");
		}

		return out;
	}

	public void execute(){
		if(parseXml && (isWindows || isUnix)){
			DomXmlParser parser = new DomXmlParser();
			parser.setXmlFilePath(xmlFilePath);
			events = parser.parse();
			this.verifyActions();
			parseXml = false;
		}else
			return;

		try{
			if(events != null && (events.getNumOfEnabledCommands() > 0)){
				scheduledExecutorService = Executors.newScheduledThreadPool(events.getNumOfEnabledCommands());	;
				for(Command cmd : events.commandsList.values()){
					if(cmd.isEnabled()){							
						CommandRunnable cmdCall = new CommandRunnable(cmd);
						scheduledExecutorService.scheduleAtFixedRate(cmdCall, 0, cmd.getFrequencyMins() * 60, TimeUnit.SECONDS);
					}else{
						logger.info("Command [id=" + cmd.getId() + "] is not enabled");
					}
				}
			}
		}catch(Exception ex){
			logger.error("Error Occured while processing events: " + ex);
			return;
		}
	}

	private boolean executeCommand(Command command) {
		Process p = null;
		String line;
		BufferedReader input = null;

		boolean found = false;
		try {
			if(isWindows){
				p = Runtime.getRuntime().exec(EventsReporterConstants.TASKLIST);
				input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					if (line.contains(command.getRun().trim())) {
						found = true;
						break;
					}
				}
			}else if(isUnix){
				String[] cmd = { "sh", "-c", command.getRun().trim()};
				p = Runtime.getRuntime().exec(cmd);
				input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while((line = input.readLine()) != null) {
					logger.debug("Process output = " + line);
					found = true;
				}
			}
			if(input != null)
				input.close();
		} catch (Exception exep) {
			logger.error("Process [" + command.getRun() + "]  not running or could not be executed.");
			logger.error(exep);
		}finally {
			if (p != null) {
				p.destroy();
			}
		}

		logger.info("Command = " + command.getRun() + " returning : " + found);
		return found;
	}

	private boolean executeAction(Command cmd){
		Integer actionId = cmd.getActionId();
		boolean actionSuccess = false;
		Action action = events.actionsList.get(actionId);
		if(action instanceof EmailAction){
			EmailAction eAction = (EmailAction)action;

			boolean isEnabled = eAction.isEnabled();
			if(isEnabled){
				EmailData emailData = cmdEmailData.get(cmd.getId());
				if(emailData == null){
					emailData = emailHelper.getEmailData(eAction, cmd);
					emailData.setHostname(events.getHostname());
					emailData.setAlertSystem(events.getAlertSystem());
					cmdEmailData.put(cmd.getId(), emailData);
				}

				try{
					actionSuccess = emailHelper.sendEmail(emailData);
					if(actionSuccess){
						logger.info("Action[id=" + actionId + "] executed, Email sent successfully.");
					}
				}catch(Exception exp){
					logger.error("Email action[id=" + actionId + "] failed for: " + emailData, exp);
					actionSuccess = false;
				}
			}else{
				logger.info("Action [Id=" + eAction.getId() + "] is not enabled, therefore, not invoking it.");
			}
		}else if(action instanceof PagerDutyAction){
			try{
				String incidentKey = null;
				if(cmdIncidentKey.get(cmd.getId()) != null){
					incidentKey = cmdIncidentKey.get(cmd.getId());
				}

				PagerDutyAction pAction = (PagerDutyAction)action;
				Response resp = PagerDutyHelper.callPagerDuty(cmd, pAction, EventsReporterConstants.INCIDENT_OPEN, incidentKey);

				if(resp.successful()){
					actionSuccess = true;
					logger.info("Action[id=" + actionId + "] executed, Incident raised to PagerDuty.");
					cmdIncidentKey.put(cmd.getId(), resp.getIncidentKey());
				}

			}catch(Exception exp){
				logger.error("PagerDuty API call failed with reason: " + exp.getMessage());
			}
		}else if(action instanceof HttpAction){
			logger.info("inside HTTP Action");
			HttpAction hAction = (HttpAction)action;
			actionSuccess = httpHandler.sendEvent(cmd, hAction);
		}

		return actionSuccess;
	}

	private void resolveIncident(Command cmd){
		Integer actionId = cmd.getActionId();
		Action action = events.actionsList.get(actionId);
		if(action instanceof EmailAction){
			EmailAction eAction = (EmailAction)action;

			boolean isEnabled = eAction.isEnabled();
			if(isEnabled){
				EmailHelper emailHelper = new EmailHelper();
				EmailData emailData = cmdEmailData.get(cmd.getId());
				if(emailData == null){
					emailData = emailHelper.getEmailData(eAction, cmd);
					emailData.setHostname(events.getHostname());
					emailData.setAlertSystem(events.getAlertSystem());
					cmdEmailData.put(cmd.getId(), emailData);
				}

				try{
					emailHelper.sendEmail(emailData);
				}catch(Exception exp){
					logger.error("Email action[id=" + actionId + "] failed for: " + emailData, exp);
				}
			}else{
				logger.info("Action [Id=" + eAction.getId() + "] is not enabled, therefore, not invoking it.");
			}
		}else if(action instanceof PagerDutyAction){
			try{
				if(cmdIncidentKey.get(cmd.getId()) != null){
					PagerDutyAction pAction = (PagerDutyAction)action;

					Response resp = PagerDutyHelper.callPagerDuty(cmd, pAction, EventsReporterConstants.INCIDENT_CLOSE, cmdIncidentKey.get(cmd.getId()));

					if(resp.successful()){
						cmdIncidentKey.remove(cmd.getId());
						logger.debug("PagerDuty Call for resolving Incident [" + cmdIncidentKey.get(cmd.getId())  + "]");
					}					
				}
			}catch(Exception exp){
				logger.error("PagerDuty API call failed with reason: " + exp.getMessage());
			}
		}
	}

	private void verifyActions(){
		logger.info("Triggering all actions to verify if they work or not");

		for(Action action : events.actionsList.values()){
			if(action instanceof EmailAction){
				EmailAction eAction = (EmailAction)action;
				EmailData emailData = emailHelper.getEmailData(eAction, null);
				emailData.setHostname(events.getHostname());
				emailData.setAlertSystem(events.getAlertSystem());

				try{
					boolean emailSuccess = emailHelper.verifyEmail(emailData);
					if(emailSuccess){
						eAction.setEnabled(true);
						logger.info("Email verification successful, enabling action[id=" + eAction.getId() + "]");
					}
				}catch(Exception exp){
					logger.error("Email action failed for API call failed for: " + emailData, exp);
				}
			}else if(action instanceof PagerDutyAction){
				PagerDutyAction pAction = (PagerDutyAction)action;
				PagerDutyHelper helper = new PagerDutyHelper(pAction.getServiceKey(), pAction.getServiceUrl());
				try{
					helper.verify();
					pAction.setEnabled(true);
					logger.info("PagerDuty API call verification successful, enabling action[id=" + pAction.getId() + "]");
				}catch(Exception exp){
					logger.error("PagerDuty API call failed with reason: " + exp.getMessage());
				}
			}else if(action instanceof HttpAction){
				HttpAction hAction = (HttpAction)action;
				hAction.setEnabled(true);
			}
		}
	}

	private class CommandRunnable implements Runnable{

		private Command cmd = null;

		public CommandRunnable(Command cmd) {
			super();
			this.cmd = cmd;
		}

		public void run() {
			logger.debug(cmd);
			logger.debug("Command id:" + cmd.getId() + " check executing at " + new DateTime());
			boolean cmdRun = executeCommand(cmd);
			if(!cmdRun){
				logger.info("Command [id=" + cmd.getId() + "] is not running, will invoke action[id=" + cmd.getActionId() + "]");
				boolean actionSuccess = executeAction(cmd);
				if(!actionSuccess)
					logger.info("Action [id=" + cmd.getActionId() + "] could not be executed.");
			}else{
				resolveIncident(cmd);
				logger.info("Not Invoking action[id=" + cmd.getActionId() + "], command[id=" + cmd.getId() + "] is running : " + cmd.getRun());
			}

		}

	}

	public static void main(String[] args){
		EventsReporter ep = new EventsReporter();
		ep.xmlFilePath = "C:\\AppDynamics\\MachineAgent3.7.7\\monitors\\EventsReporter\\properties.xml";
		ep.execute();	
	}

}
