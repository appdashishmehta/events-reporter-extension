package com.appdynamics.extensions.eventsreporter.model;

import java.util.HashMap;
import java.util.Map;

public class Command {
	
	private Integer id;
	private String run;
	private Integer actionId;
	private Integer frequencyMins;
	private boolean enabled = false;
	private Map<String, String> properties = new HashMap<String, String>();
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getRun() {
		return run;
	}
	public void setRun(String run) {
		this.run = run;
	}
	public Integer getActionId() {
		return actionId;
	}
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	
	public Integer getFrequencyMins() {
		return frequencyMins;
	}
	public void setFrequencyMins(Integer frequencyMins) {
		this.frequencyMins = frequencyMins;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getPropertyValue(String property){
		return this.properties.get(property);
	}
	
	public void putProperty(String key, String value){
		this.properties.put(key, value);
	}
	
	public Map<String, String> getCmdProperties(){
		return this.properties;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Command[").append("id=").append(this.id).append("|");
		sb.append("enabled=").append(this.enabled).append("|");
		sb.append("run=").append(this.run).append("|");
		sb.append("frequencyMins=").append(this.frequencyMins).append("|");
		sb.append("actionId=").append(this.actionId).append("|");
		sb.append("properties=").append(this.properties).append("]");
		
		return sb.toString();
	}

}
