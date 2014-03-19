package it.polimi.modaclouds.monitoring.monitoring_manager;

public class RuleInstallationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2238203600032056812L;

	public RuleInstallationException(String message, Exception e) {
		super(message,e);
	}

}
