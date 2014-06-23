package com.appdynamics.extensions.eventsreporter.model;

public abstract class Action {
	
	private Integer id;
	private String type;
	private boolean enabled = false;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(this.id).append("|");
		sb.append("type=").append(this.type).append("|");
		sb.append("enabled=").append(this.enabled).append("|");
		
		return sb.toString();
	}

}
