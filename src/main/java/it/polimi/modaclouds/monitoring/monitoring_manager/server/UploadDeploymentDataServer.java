package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import java.util.ArrayList;
import java.util.List;

import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleDoesNotExistException;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleInstallationException;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;
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


public class UploadDeploymentDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(SingleMetricDataServer.class
			.getName());
	
	@Post("json")
	public void uploadModel(Representation rep){
		try {
			MonitoringManager manager = (MonitoringManager) getContext().getAttributes().get("manager");
			
			//[{"started":true,"id":"0","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:b94b65f7-fd5c-416f-a43b-c72644134fbc","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/b94b65f7-fd5c-416f-a43b-c72644134fbc"},{"started":true,"id":"1","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:f50551cf-2807-4919-8e18-162d56675b46","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/f50551cf-2807-4919-8e18-162d56675b46"},{"started":true,"id":"2","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:b0ce36cf-c6a0-4c30-a72b-e5e1d5c8cde8","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/b0ce36cf-c6a0-4c30-a72b-e5e1d5c8cde8"},{"started":true,"id":"3","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:86fa89b6-b8d0-4777-a1f5-94aca8ce922c","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/86fa89b6-b8d0-4777-a1f5-94aca8ce922c"},{"started":true,"id":"4","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:8f406c1b-1965-40d3-9d76-ca0309711110","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/8f406c1b-1965-40d3-9d76-ca0309711110"}]
			
			String payload = rep.getText();
					
			//conversion from json to Component object
			Gson g = new Gson();
			Component[] components = g.fromJson(payload , Component[].class);  
	
			
			//TODO
			//manager.uploadModel(deployment);
			
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		
		} catch(Exception e){
			logger.error("Error while uploading the deployment", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			this.getResponse().setEntity("Error while uploading the deployment: " + e.toString(), MediaType.TEXT_PLAIN);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

}