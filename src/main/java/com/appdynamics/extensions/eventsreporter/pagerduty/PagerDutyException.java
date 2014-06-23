package com.appdynamics.extensions.eventsreporter.pagerduty;

public class PagerDutyException extends Exception {

	private static final long serialVersionUID = 1L;

	public PagerDutyException(String string, Throwable e) {
		super(string, e);
	}

	public PagerDutyException(String string) {
		super(string);
	}
}
