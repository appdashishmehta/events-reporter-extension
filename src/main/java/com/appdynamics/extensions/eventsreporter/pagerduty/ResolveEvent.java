package com.appdynamics.extensions.eventsreporter.pagerduty;

class ResolveEvent extends PagerDutyEvent {

	public ResolveEvent(String serviceKey, Incident incident) {
		super(serviceKey, "resolve");
		setDescription(incident.getDescription());
		setIncidentKey(incident.getIncidentKey());
		setDetails(incident.getDetails());
	}

	@Override
	public String toString(){		
		return super.toString();
	}
}