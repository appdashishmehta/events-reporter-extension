package com.appdynamics.extensions.eventsreporter.email;

import java.util.HashMap;
import java.util.Map;

import com.appdynamics.extensions.eventsreporter.model.Action;

public class EmailAction extends Action{

	private String toAddress;
	private Map<String, String> mailServerProps = new HashMap<String, String>();


	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getMailServerPropValue(String property){
		return this.mailServerProps.get(property);
	}

	public void putMailServerProp(String key, String value){
		this.mailServerProps.put(key, value);
	}

	public Map<String, String> getMailServerProperties(){
		return this.mailServerProps;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("EmailAction[").append(super.toString()).append("|");
		sb.append("toAddress=").append(this.toAddress).append("|");
		sb.append("mailServerProps=").append(this.getMaskedMailServerPropsString()).append("]");

		return sb.toString();
	}

	private String getMaskedMailServerPropsString(){
		StringBuilder sb = new StringBuilder();
		for(String key : this.mailServerProps.keySet()){	
			if(key.equals("mail.smtp.password"))
				sb.append(key).append("=").append("****").append("|");
			else
				sb.append(key).append("=").append(this.mailServerProps.get(key)).append("|");
		}

		return sb.toString();
	}

}
