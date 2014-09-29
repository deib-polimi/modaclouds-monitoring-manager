/**
 * Copyright 2014 deib-polimi
 * Contact: deib-polimi <marco.miglierina@polimi.it>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import it.polimi.modaclouds.qos_models.monitoring_ontology.CloudProvider;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Location;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.PaaSService;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Model {

	private Set<CloudProvider> cloudProviders;
	private Set<Location> locations;
	private Set<VM> vMs;
	private Set<PaaSService> paaSServices;
	private Set<InternalComponent> internalComponents;
	private Set<Method> methods;
	
	public void add(CloudProvider cloudProvider) {
		if (cloudProviders == null) cloudProviders = new HashSet<CloudProvider>();
		cloudProviders.add(cloudProvider);
	}
	public void add(Location location) {
		if (locations == null) locations = new HashSet<Location>();
		locations.add(location);
	}
	public void add(VM vM) {
		if (vMs == null) vMs = new HashSet<VM>();
		vMs.add(vM);
	}
	public void add(PaaSService paaSService) {
		if (paaSServices == null) paaSServices = new HashSet<PaaSService>();
		paaSServices.add(paaSService);
	}
	public void add(InternalComponent internalComponent) {
		if (internalComponents == null) internalComponents = new HashSet<InternalComponent>();
		internalComponents.add(internalComponent);
	}
	public void add(Method method) {
		if (methods == null) methods = new HashSet<Method>();
		methods.add(method);
	}
	
	public Set<CloudProvider> getCloudProviders() {
		return cloudProviders;
	}
	public void setCloudProviders(Set<CloudProvider> cloudProviders) {
		this.cloudProviders = cloudProviders;
	}
	public Set<Location> getLocations() {
		return locations;
	}
	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}
	public Set<VM> getvMs() {
		return vMs;
	}
	public void setvMs(Set<VM> vMs) {
		this.vMs = vMs;
	}
	public Set<PaaSService> getPaaSServices() {
		return paaSServices;
	}
	public void setPaaSServices(Set<PaaSService> paaSServices) {
		this.paaSServices = paaSServices;
	}
	public Set<InternalComponent> getInternalComponents() {
		return internalComponents;
	}
	public void setInternalComponents(Set<InternalComponent> internalComponents) {
		this.internalComponents = internalComponents;
	}
	public Set<Method> getMethods() {
		return methods;
	}
	public void setMethods(Set<Method> methods) {
		this.methods = methods;
	}
	
	public Set<Resource> getResources() {
		Set<Resource> resources = new HashSet<Resource>();
		resources.addAll(nullable(cloudProviders));
		resources.addAll(nullable(locations));
		resources.addAll(nullable(vMs));
		resources.addAll(nullable(paaSServices));
		resources.addAll(nullable(internalComponents));
		resources.addAll(nullable(methods));
		return resources;
	}
	
	private <T> Collection<T> nullable(
			Set<T> collection) {
		return collection == null? new HashSet<T>() : collection;
	}
	
	

}
