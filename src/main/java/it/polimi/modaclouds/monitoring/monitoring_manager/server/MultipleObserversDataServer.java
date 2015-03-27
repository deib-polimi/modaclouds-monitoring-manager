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

import it.polimi.modaclouds.monitoring.monitoring_manager.MetricDoesNotExistException;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.Observer;

import java.net.MalformedURLException;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MultipleObserversDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(MultipleObserversDataServer.class
			.getName());
	
	@Post
	public void addObserver(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String metricname = (String) this.getRequest().getAttributes().get("metricname");
			String callbackUrl = rep.getText();
			String observerId = manager.addObserver(metricname, callbackUrl);
			this.getResponse().setStatus(Status.SUCCESS_CREATED);
			this.getResponse().setEntity(observerId, MediaType.TEXT_PLAIN);
		} catch (MetricDoesNotExistException e) {
			logger.error(e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					e.getMessage());
			this.getResponse().setEntity(e.getMessage(),
					MediaType.TEXT_PLAIN);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					e.getMessage());
			this.getResponse().setEntity(e.getMessage(),
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while adding observer", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while adding observer: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}
	
	@Get
	public void getObservers() {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String metricname = (String) this.getRequest().getAttributes().get("metricname");
			
			Set<Observer> observers = manager.getObservers(metricname);
			JsonObject json = new JsonObject();
			json.add("observers", new Gson().toJsonTree(observers));
			this.getResponse().setStatus(Status.SUCCESS_CREATED);
			this.getResponse().setEntity(json.toString(), MediaType.APPLICATION_JSON);
		} catch (MetricDoesNotExistException e) {
			logger.error("The metric does not exist", e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"The metric does not exist");
			this.getResponse().setEntity("The metric does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while getting observers", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while getting observers: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

}
