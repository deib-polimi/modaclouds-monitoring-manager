package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;
import it.polimi.modaclouds.qos_models.monitoring_ontology.ExternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;

import java.util.List;

public class DeserialisedUpdateModel {
	
	private List<VM> vms;
	
	private List<Component> components;
	
	private List<ExternalComponent> externalComponents;

	public List<VM> getVms() {
		return vms;
	}

	public void setVms(List<VM> vms) {
		this.vms = vms;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public List<ExternalComponent> getExternalComponents() {
		return externalComponents;
	}

	public void setExternalComponents(List<ExternalComponent> externalComponents) {
		this.externalComponents = externalComponents;
	}

}
