package com.appdynamics.extensions.eventsreporter.pagerduty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.appdynamics.extensions.eventsreporter.model.Command;
import com.appdynamics.extensions.eventsreporter.utils.EventsReporterConstants;

public class PagerDutyHelper {

	private static Logger logger = Logger.getLogger("com.singularity.extensions.eventsreporter.pagerduty.PagerDutyHelper");

	final String serviceURL;
	private final String serviceKey;

	/**
	 * Create the api with the specified base URL.
	 * 
	 * @param serviceKey
	 * @param baseURL
	 */
	public PagerDutyHelper(String serviceKey, String baseURL) {
		this.serviceKey = serviceKey;
		this.serviceURL = baseURL;
	}

	/**
	 * Create an API facade based on the URL
	 * https://events.pagerduty.com/generic/2010-04-15/create_event.json
	 * @param serviceKey the API key obtained from the PagerDuty service definition.
	 */
	public PagerDutyHelper(String serviceKey) {
		this.serviceKey = serviceKey;
		serviceURL = "https://events.pagerduty.com/generic/2010-04-15/create_event.json";
	}

	/**
	 * Trigger the incident. If the incident doesn't have an incidentKey then one
	 * is obtained from the service and assigned to the incident instance.
	 * 
	 * @param incident
	 * @return a Response object which may have warnings.
	 * @throws MessageException if there is any problem with the message at all.
	 */
	public Response trigger(Incident incident) throws PagerDutyException {
		return sendEvent(incident, new TriggerEvent(serviceKey, incident));
	}

	/**
	 * Acknowledge the incident.  Use the same incident passed to the trigger call.
	 * 
	 * @param incident
	 * @return the Response object
	 * @throws MessageException if there is any problem with the message
	 */
	public Response acknowledge(Incident incident) throws PagerDutyException {
		return sendEvent(incident, new AcknowledgeEvent(serviceKey, incident));
	}

	/**
	 * Set the issue to resolved.  Use the same incident passed to the trigger call.
	 * 
	 * @param incident
	 * @return the Response object
	 * @throws MessageException if there is any problem with the message
	 */
	public Response resolve(Incident incident) throws PagerDutyException {
		return sendEvent(incident, new ResolveEvent(serviceKey, incident));
	}

	/** Send a message to the service to verify the serviceKey and serviceURL 
	 * @throws MessageException if there is a problem with the service.  This will
	 * be a 400 response if the service key is no good. 
	 * */
	public void verify() throws PagerDutyException {
		Incident bogusIncident = new Incident("PagerDuty Verification Incident", "PagerDuty Verification Key");
		acknowledge(bogusIncident);
	}

	public static Response callPagerDuty(Command cmd, PagerDutyAction pAction, String status, String incidentKey) throws PagerDutyException{
		Response response = null;

		PagerDutyHelper helper = new PagerDutyHelper(pAction.getServiceKey(), pAction.getServiceUrl());

		Incident incident = new Incident(cmd.getPropertyValue(EventsReporterConstants.DESCRIPTION));
		incident.addDetail(EventsReporterConstants.DATE_TIME, new Date(System.currentTimeMillis()).toString());
		incident.addDetail(EventsReporterConstants.INCIDENT_STATUS, status);
		
		for(String key : cmd.getCmdProperties().keySet()){
			if(!key.equals(EventsReporterConstants.DESCRIPTION))
				incident.addDetail(key, cmd.getCmdProperties().get(key));
		}
		
		String uuid = "";
		if(incidentKey != null){
			uuid = incidentKey;
		}else{
			uuid = UUID.randomUUID().toString();
		}

		incident.setIncidentKey(uuid);
		response = helper.trigger(incident);

		return response;
	}

	/*
	 * Send the event.  Unfortunately since we're using the J2SE http client
	 * we can't dig out the message details when there is a 400 response.  
	 */
	private Response sendEvent(Incident incident, PagerDutyEvent event) throws PagerDutyException {
		logger.info("Calling PagerDuty REST API for Incident : " + incident);

		URL url = null;
		try {
			url = new URL(new URL(serviceURL), "create_event.json");
			HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();
			openConnection.setRequestMethod("POST");
			openConnection.setDoOutput(true);
			OutputStream outputStream = openConnection.getOutputStream();
			PrintStream out = new PrintStream(outputStream);
			out.print(event.encode());
			out.close();
			InputStreamReader in = new InputStreamReader(openConnection.getInputStream());
			Response response = new Response(in);
			if (response.getIncidentKey() != null){
				incident.setIncidentKey(response.getIncidentKey());
			}
			logger.info("PagerDuty call response : " + response);
			return response;			
		} catch (FileNotFoundException e) {
			logger.error("Request returned NOT FOUND", e);
			throw new PagerDutyException("Request returned NOT FOUND");
		} catch (MalformedURLException e) {
			logger.error("Error building message or bad Pager Duty API URL", e);
			throw new PagerDutyException("Error building message or bad Pager Duty API URL", e);
		} catch (ProtocolException e) {
			logger.error("Error building message", e);
			throw new PagerDutyException("Error building message", e);
		} catch (IOException e) {
			logger.error("Error communicating with PagerDuty server", e);
			throw new PagerDutyException("Error communicating with PagerDuty server: " + e, e);
		} catch (ParseException e) {
			logger.error("Error parsing response from server: ", e);
			throw new PagerDutyException("Error parsing response from server: " + e, e);
		}
	}

	public static void main(String[] args) {
		try{
			PagerDutyHelper helper = new PagerDutyHelper("e93facc04764012d7bfb002500d5d1a6", "https://events.pagerduty.com/generic/2010-04-15/create_event.json");

			Incident incident = new Incident(EventsReporterConstants.DESCRIPTION);
			incident.addDetail(EventsReporterConstants.DATE_TIME, new Date(System.currentTimeMillis()).toString());
			String uuid = UUID.randomUUID().toString();
			incident.setIncidentKey(uuid);

			helper.trigger(incident);			
		}catch(Exception exp){
			logger.error("PagerDuty REST API call failed, reason: " + exp.getMessage());
		}
	}
}
