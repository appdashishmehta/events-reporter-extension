package com.appdynamics.extensions.eventsreporter.http;

import com.appdynamics.extensions.eventsreporter.model.Action;

public class HttpAction extends Action{
	
	private String httpUrl;

	public String getHttpUrl() {
		return httpUrl;
	}

	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("HttpAction[").append(super.toString()).append("|");
		sb.append("httpUrl=").append(this.httpUrl).append("]");
		
		return sb.toString();
	}

}
