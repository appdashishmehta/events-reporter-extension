package com.appdynamics.extensions.eventsreporter.model;

import com.appdynamics.extensions.eventsreporter.email.EmailAction;

public class EmailData {

	public EmailAction emailAction;
	public String subject;
	public String body;
	public String command;
	public String hostname;
	public String alertSystem;
	
	public EmailAction getEmailAction() {
		return emailAction;
	}
	public void setEmailAction(EmailAction emailAction) {
		this.emailAction = emailAction;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getAlertSystem() {
		return alertSystem;
	}
	public void setAlertSystem(String alertSystem) {
		this.alertSystem = alertSystem;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("EmailData[").append("emailAction=").append(this.emailAction).append("|");
		sb.append("subject=").append(this.subject).append("|");
		sb.append("command=").append(this.command).append("|");
		sb.append("body=").append(this.body).append("|");
		sb.append("hostname=").append(this.hostname).append("|");
		sb.append("alertSystem=").append(this.alertSystem).append("]");
		
		return sb.toString();
	}
	
}
