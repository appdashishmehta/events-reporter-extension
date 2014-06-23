package com.appdynamics.extensions.eventsreporter.pagerduty;

import com.appdynamics.extensions.eventsreporter.model.Action;

public class PagerDutyAction extends Action{
	
	private String serviceKey;
	private String serviceUrl;
	
	public String getServiceKey() {
		return serviceKey;
	}
	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}
	public String getServiceUrl() {
		return serviceUrl;
	}
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("PagerDutyAction[").append(super.toString()).append("|");
		sb.append("serviceKey=").append(this.serviceKey).append("|");
		sb.append("serviceUrl=").append(this.serviceUrl).append("]");
		
		return sb.toString();
	}

}
