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
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleDoesNotExistException;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleRuleDataServer extends ServerResource {

	private Logger logger = LoggerFactory.getLogger(SingleRuleDataServer.class
			.getName());
	
	@Get
	public void getMonitoringRule(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String id = (String) this.getRequest().getAttributes().get("id");
			MonitoringRule rule = manager.getMonitoringRule(id);
			this.getResponse().setStatus(Status.SUCCESS_OK);
			this.getResponse().setEntity(
					new JaxbRepresentation<MonitoringRule>(rule));
		} catch (RuleDoesNotExistException e){
			logger.error("Rule does not exist");
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while retrieving the rule: the rule does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while retrieving monitoring rule", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while retrieving monitoring rule: " + e.toString(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Delete
	public void uninstallMonitoringRule(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String id = (String) this.getRequest().getAttributes().get("id");
			manager.uninstallRule(id);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		} catch (RuleDoesNotExistException e){
			logger.error("Rule does not exist");
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while uninstalling the rule: the rule does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while deleting the rule", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while uninstalling the rule: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

}
