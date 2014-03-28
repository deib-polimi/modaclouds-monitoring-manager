package it.polimi.modaclouds.monitoring.monitoring_manager;

import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;

public interface MonitoringPlatform {

	void newInstance(Component instance);

	void start();

	void stop();

}
