package com.appdynamics.extensions.eventsreporter.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.springframework.web.util.UriUtils;

import com.appdynamics.extensions.eventsreporter.model.Command;
import com.appdynamics.extensions.eventsreporter.utils.EventsReporterConstants;

public class HttpHandler {

	private static Logger logger = Logger.getLogger("com.singularity.extensions.eventsreporter.http.HttpHandler");

	public boolean sendEvent(Command cmd, HttpAction hAction) {

		String url = hAction.getHttpUrl();
		String summarySuffix = cmd.getPropertyValue(EventsReporterConstants.SUMMARY_PREFIX);
		String eventType = cmd.getPropertyValue(EventsReporterConstants.EVENT_TYPE);

		try{

			String urlQuery = UriUtils.encodeQuery("type=" + eventType + "&summary='"  + cmd.getRun() + "' " +  summarySuffix , "UTF-8");
			url = url + urlQuery;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");		
			
			int responseCode = con.getResponseCode();
			logger.debug("Sent 'GET' request to URL : " + url + ", received response code = " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		}catch (Exception ex) {
			logger.error("Error occured while sending events to controller (via machine agent) : " + ex);
			return false;
		}

		return true;
	}


}
