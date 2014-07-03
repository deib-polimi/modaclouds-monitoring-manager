package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import it.polimi.modaclouds.monitoring.monitoring_manager.ComponentDoesNotExistException;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleDoesNotExistException;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class UpdateDeploymentDataServer extends ServerResource  {
	
	private Logger logger = LoggerFactory.getLogger(SingleMetricDataServer.class
			.getName());
	
	@Post
	public void addInstance(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			
			Component instance = null;
			String message = rep.getText();
			
			//TODO extract component from the body of the post request
			//this should not be a component, but a representation of the deployment
			
			
			//TODO conversion from json to Component object
			Gson g = new Gson();
			
			//TODO KB deployment update
			manager.newInstance(instance);
		
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
	
	@Delete
	public void deleteInstance(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String id = (String) this.getRequest().getAttributes().get("id");
			
			manager.deleteInstance(id);

			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		} catch (ComponentDoesNotExistException e) {
			logger.error("The monitoring rule does not exist", e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
					"The component does not exist");
			this.getResponse().setEntity("The monitoring rule does not exist",
					MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			logger.error("Error while deleting the rule", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while installing the rule: " + e.getMessage(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}	

}