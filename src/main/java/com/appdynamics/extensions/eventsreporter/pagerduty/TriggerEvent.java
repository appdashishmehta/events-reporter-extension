package com.appdynamics.extensions.eventsreporter.pagerduty;

public class TriggerEvent extends PagerDutyEvent{

	public TriggerEvent(String serviceKey, Incident incident) {
		super(serviceKey, "trigger");
		setDescription(incident.getDescription());
		setDetails(incident.getDetails());
		setIncidentKey(incident.getIncidentKey());
	}

	@Override
	public String toString(){		
		return super.toString();
	}
}
