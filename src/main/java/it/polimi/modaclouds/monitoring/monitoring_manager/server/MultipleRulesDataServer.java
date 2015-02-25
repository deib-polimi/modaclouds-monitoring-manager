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

import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleInstallationException;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleRulesDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(MultipleRulesDataServer.class.getName());
	
	@Get
	public void getMonitoringRules() {
		try {
			MonitoringManager manager = (MonitoringManager) getContext().getAttributes().get("manager");
			MonitoringRules rules = manager.getMonitoringRules();
			this.getResponse().setStatus(Status.SUCCESS_OK);
			this.getResponse().setEntity(new JaxbRepresentation<MonitoringRules>(rules));
		} catch(Exception e){
			logger.error("Error while getting monitoring rules", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			this.getResponse().setEntity("Error while getting monitoring rules: " + e.toString(), MediaType.TEXT_PLAIN);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
	
	@Post
	public void installMonitoringRules(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext().getAttributes().get("manager");
			JaxbRepresentation<MonitoringRules> jaxbMonitoringRule = new JaxbRepresentation<MonitoringRules>(rep,MonitoringRules.class);
			MonitoringRules rules = jaxbMonitoringRule.getObject();
			manager.installRules(rules);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		} catch (RuleInstallationException e) {
			logger.error("Error while installing monitoring rules", e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
			this.getResponse().setEntity("Error while installing monitoring rules: " + e.getMessage(), MediaType.TEXT_PLAIN);
		} catch(Exception e){
			logger.error("Error while installing monitoring rules", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			this.getResponse().setEntity("Error while getting monitoring rules: " + e.toString(), MediaType.TEXT_PLAIN);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

}
