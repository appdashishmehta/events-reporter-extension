package com.appdynamics.extensions.eventsreporter.model;

import java.util.HashMap;
import java.util.Map;

public class Events {
	
	public Integer maxCommands = 10;
	public String alertSystem = "AppDynamics Machine Agent Alerting Extension";
	public String hostname;
	public Map<Integer, Action> actionsList;
	public Map<Integer, Command> commandsList;
	public Integer numOfEnabledCommands;
	
	public Events(){
		this.actionsList = new HashMap<Integer, Action>();
		this.commandsList = new HashMap<Integer, Command>();
	}
	
	public Integer getMaxCommands() {
		return maxCommands;
	}

	public void setMaxCommands(Integer maxCommands) {
		this.maxCommands = maxCommands;
	}

	public String getAlertSystem() {
		return alertSystem;
	}

	public void setAlertSystem(String alertSystem) {
		this.alertSystem = alertSystem;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Action getActionById(int actionId){
		Action action = this.actionsList.get(actionId);
		return action;
	}
	
	public Command getCommandById(int commandId){
		Command command = this.commandsList.get(commandId);
		return command;
	}

	public Integer getNumOfEnabledCommands() {
		return numOfEnabledCommands;
	}

	public void setNumOfEnabledCommands(Integer numOfEnabledCommands) {
		this.numOfEnabledCommands = numOfEnabledCommands;
	}

	public void putAction(int id, Action action){
		this.actionsList.put(id, action);
	}
	
	public void putCommand(int id, Command command){
		this.commandsList.put(id, command);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Events[").append("hostname=").append(this.hostname).append("|");
		sb.append("alertSystem=").append(this.alertSystem).append("|");
		sb.append("maxCommands=").append(this.maxCommands).append("|");
		sb.append("numOfEnabledCommands=").append(this.numOfEnabledCommands).append("|");
		sb.append("actions=").append(this.actionsList).append("|");
		sb.append("commands=").append(this.commandsList).append("]");
		
		return sb.toString();
	}

}
