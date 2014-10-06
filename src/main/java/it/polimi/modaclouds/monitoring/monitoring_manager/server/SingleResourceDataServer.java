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
import it.polimi.modaclouds.monitoring.monitoring_manager.ResourceDoesNotExistException;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;
import it.polimi.modaclouds.qos_models.schema.Metrics;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SingleResourceDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(SingleMetricDataServer.class
			.getName());
	
	@Delete
	public void deleteResource() {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String id = (String) this.getRequest().getAttributes().get("id");			
			manager.deleteResource(id);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		} catch (ResourceDoesNotExistException e) {
			logger.error("The component does not exist", e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"The component does not exist");
			this.getResponse().setEntity("The component does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while deleting the component", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while deleting the component: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}
	
	@Get
	public void getResource(){
		try{
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String id = (String) this.getRequest().getAttributes().get("id");
			Resource resource = manager.getResource(id);
			this.getResponse().setStatus(Status.SUCCESS_OK);
			this.getResponse().setEntity(resource.toString(), MediaType.TEXT_PLAIN);
		}catch (ResourceDoesNotExistException e) {
			logger.error("The component does not exist", e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"The component does not exist");
			this.getResponse().setEntity("The component does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while deleting the component", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while deleting the component: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
		
	}

}
