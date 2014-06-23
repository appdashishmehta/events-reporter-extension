package com.appdynamics.extensions.eventsreporter.parser;

import java.io.File;
import java.net.InetAddress;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.appdynamics.extensions.eventsreporter.email.EmailAction;
import com.appdynamics.extensions.eventsreporter.http.HttpAction;
import com.appdynamics.extensions.eventsreporter.model.Command;
import com.appdynamics.extensions.eventsreporter.model.Events;
import com.appdynamics.extensions.eventsreporter.pagerduty.PagerDutyAction;
import com.appdynamics.extensions.eventsreporter.utils.EventsReporterConstants;

public class DomXmlParser extends Parser{

	private static Logger logger = Logger.getLogger("com.singularity.extensions.parser.DomXmlParser");

	private String xmlFilePath = "";

	public void setXmlFilePath(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
	}

	@Override
	public Events parse() {

		Events events = new Events();
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			logger.info("Parsing xml document : " + this.xmlFilePath);
			
			Document document =  builder.parse(new File(this.xmlFilePath));
			document.getDocumentElement().normalize();

			NodeList args = document.getElementsByTagName(EventsReporterConstants.ARGS);
			String hostname = null;
			String alertSystem = null;
			String maxCommands = null;
			
			if(args == null || args.getLength() == 0){
				logger.info("No Args defined in the xml file, setting default values");
			}else{
				Node argNode = args.item(0);
				if (argNode.getNodeType() == Node.ELEMENT_NODE) { 
					Element eElement = (Element) argNode;
					alertSystem = eElement.getElementsByTagName(EventsReporterConstants.ALERT_SYSTEM).item(0).getTextContent();
					maxCommands = eElement.getElementsByTagName(EventsReporterConstants.MAX_COMMANDS).item(0).getTextContent();
				    hostname = eElement.getElementsByTagName(EventsReporterConstants.HOSTNAME).item(0).getTextContent();
				}
			}
			
			if(hostname == null || hostname.length() == 0)
				hostname = InetAddress.getLocalHost().getHostName();
			events.setHostname(hostname);
			if(alertSystem != null)
				events.setAlertSystem(alertSystem);
			if(maxCommands != null){
				int maxCmnds = Integer.valueOf(maxCommands);
				events.setMaxCommands(maxCmnds);
			}
			
			NodeList actionList = document.getElementsByTagName(EventsReporterConstants.ACTION);
			if(actionList == null || actionList.getLength() == 0){
				logger.info("No Actions defined in the xml file: " + this.xmlFilePath);
			}else{
				for (int actionNode = 0; actionNode < actionList.getLength(); actionNode++) {
					Node nNode = actionList.item(actionNode);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) { 
						Element eElement = (Element) nNode; 

						String actionId = eElement.getAttribute(EventsReporterConstants.ID);						
						String type = eElement.getElementsByTagName(EventsReporterConstants.TYPE).item(0).getTextContent();

						if(type.equals(EventsReporterConstants.EMAIL)){
							EmailAction emailAction = new EmailAction();
							emailAction.setId(Integer.valueOf(actionId));
							emailAction.setType(type);							
							emailAction.setToAddress(eElement.getElementsByTagName(EventsReporterConstants.EMAIL_TO).item(0).getTextContent());
							
							NodeList propNodes = eElement.getElementsByTagName(EventsReporterConstants.PROPERTY);
							for(int i = 0; i < propNodes.getLength(); i++){
								Element propEle = (Element) propNodes.item(i); 
								String key = propEle.getAttribute(EventsReporterConstants.PROP_NAME);
								String value = propEle.getAttribute(EventsReporterConstants.PROP_VALUE);
								emailAction.putMailServerProp(key, value);
							}	

							events.putAction(emailAction.getId(), emailAction);
						}else if(type.equals(EventsReporterConstants.PAGERDUTY)){							
							PagerDutyAction pagerDutyAction = new PagerDutyAction();
							pagerDutyAction.setId(Integer.valueOf(actionId));
							pagerDutyAction.setType(type);		
							pagerDutyAction.setServiceKey(eElement.getElementsByTagName(EventsReporterConstants.SERVICE_KEY).item(0).getTextContent());
							pagerDutyAction.setServiceUrl(eElement.getElementsByTagName(EventsReporterConstants.SERVICE_URL).item(0).getTextContent()); 

							events.putAction(pagerDutyAction.getId(), pagerDutyAction);
						}else if(type.equals(EventsReporterConstants.EVENTS_TO_CONTROLLER)){
							HttpAction httpAction = new HttpAction();
							httpAction.setId(Integer.valueOf(actionId));
							httpAction.setType(type);
							httpAction.setHttpUrl(eElement.getElementsByTagName(EventsReporterConstants.HTTP_URL).item(0).getTextContent());
						    
							events.putAction(httpAction.getId(), httpAction);
						}
					}
				}
			}

			NodeList commandList = document.getElementsByTagName(EventsReporterConstants.COMMAND);
			if(commandList == null || commandList.getLength() == 0){
				logger.info("No Commands defined in the xml file: " + this.xmlFilePath);
			}else{

				Integer numOfEnabledCommands = 0;
				for (int cmdNode = 0; cmdNode < commandList.getLength(); cmdNode++) { 
					Node nNode = commandList.item(cmdNode);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) { 
						Element eElement = (Element) nNode; 

						Command cmd = new Command();
						String cmdId = eElement.getAttribute(EventsReporterConstants.ID);
						String run = eElement.getElementsByTagName(EventsReporterConstants.RUN).item(0).getTextContent();
						String actionId = eElement.getElementsByTagName(EventsReporterConstants.ACTION_ID).item(0).getTextContent();
						String frequency = eElement.getElementsByTagName(EventsReporterConstants.FREQUENCY_MINS).item(0).getTextContent();
                        String enabled = eElement.getElementsByTagName(EventsReporterConstants.ENABLED).item(0).getTextContent();
						
						cmd.setId(Integer.valueOf(cmdId));
						cmd.setRun(run);
						cmd.setFrequencyMins(Integer.valueOf(frequency));
						cmd.setActionId(Integer.valueOf(actionId));
						if(enabled != null && enabled.equalsIgnoreCase("true")){
							cmd.setEnabled(true);
							numOfEnabledCommands++;
						}

						NodeList propNodes = eElement.getElementsByTagName(EventsReporterConstants.PROPERTY);

						for(int i = 0; i < propNodes.getLength(); i++){
							Element propEle = (Element) propNodes.item(i); 
							String key = propEle.getAttribute(EventsReporterConstants.PROP_NAME);
							String value = propEle.getAttribute(EventsReporterConstants.PROP_VALUE);
							cmd.putProperty(key, value);
						}					
						events.putCommand(cmd.getId(), cmd);
					}
				}
				events.setNumOfEnabledCommands(numOfEnabledCommands);
			}
			logger.debug("Xml document parsed and resulted into Events Object: " + events);
			return events;
		}catch(Exception ex){
			logger.error("Exception thrwon while parsing xml : " + ex);
		}

		return null;
	}

	public static void main(String argv[]) {
		DomXmlParser p = new DomXmlParser();
		p.setXmlFilePath("D:\\appdynamics\\events.xml");
		p.parse();
	}

}
