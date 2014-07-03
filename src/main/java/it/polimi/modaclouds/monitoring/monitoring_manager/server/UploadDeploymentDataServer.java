package it.polimi.modaclouds.monitoring.monitoring_manager.server;

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


public class UploadDeploymentDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(SingleMetricDataServer.class
			.getName());
	
	@Post
	public void uploadModel(Representation rep){
		try {
			MonitoringManager manager = (MonitoringManager) getContext().getAttributes().get("manager");
			
			//TODO xml dei componenti
			JaxbRepresentation<MonitoringRules> jaxbMonitoringRule = new JaxbRepresentation<MonitoringRules>(rep,MonitoringRules.class);
			MonitoringRules rules = jaxbMonitoringRule.getObject();
			//Deployment deployment = jaxBDeployment.getObject(); sarebbe una lista di componenti
			
			
			//TODO
			manager.installRules(rules);
			//manager.uploadModel(deployment);
			
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		} catch (RuleInstallationException e) {
			logger.error("Error while uploading the deployment", e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,e.getMessage());
			this.getResponse().setEntity("Error while uploading the deployment: " + e.toString(), MediaType.TEXT_PLAIN);
		} catch(Exception e){
			logger.error("Error while installing the deployment", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
			this.getResponse().setEntity("Error while uploading the deployment: " + e.toString(), MediaType.TEXT_PLAIN);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

}