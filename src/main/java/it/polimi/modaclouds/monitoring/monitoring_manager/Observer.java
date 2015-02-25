package it.polimi.modaclouds.monitoring.monitoring_manager;

public class Observer {

	private String id;
	private String callbackUrl;

	public Observer(String id, String callbackUrl) {
		this.id = id;
		this.callbackUrl = callbackUrl;
	}

	public String getId() {
		return id;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

}
