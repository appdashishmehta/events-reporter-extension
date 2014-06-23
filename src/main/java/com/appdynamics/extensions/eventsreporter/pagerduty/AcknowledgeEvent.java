package com.appdynamics.extensions.eventsreporter.pagerduty;

class AcknowledgeEvent extends PagerDutyEvent {
	AcknowledgeEvent(String serviceKey, Incident incident) {
		super(serviceKey, "acknowledge");
		setDescription(incident.getDescription());
		setIncidentKey(incident.getIncidentKey());
		setDetails(incident.getDetails());
	}

	@Override
	public String toString(){		
		return super.toString();
	}
}


