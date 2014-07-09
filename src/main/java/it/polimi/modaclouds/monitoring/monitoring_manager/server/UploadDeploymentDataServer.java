package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import java.util.ArrayList;
import java.util.List;

import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleDoesNotExistException;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleInstallationException;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;
import it.polimi.modaclouds.qos_models.monitoring_ontology.ExternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MonitorableResource;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class UploadDeploymentDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(SingleMetricDataServer.class
			.getName());
	
	@Post
	public void uploadModel(Representation rep){
		try {
			//invoking manager and payload of the post request
			MonitoringManager manager = (MonitoringManager) getContext().getAttributes().get("manager");	
			String payload = rep.getText();
			
			//deserialisation from json to ModelUpdates.class
			Gson gson = new Gson();
			ModelUpdates deserialised = gson.fromJson(payload, ModelUpdates.class);
			
			//upload the new model in the knowledge base and responde to the request
			manager.uploadModel(deserialised);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		
		} catch(Exception e){
			logger.error("Error while uploading the model", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			this.getResponse().setEntity("Error while uploading the model: " + e.toString(), MediaType.TEXT_PLAIN);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

}