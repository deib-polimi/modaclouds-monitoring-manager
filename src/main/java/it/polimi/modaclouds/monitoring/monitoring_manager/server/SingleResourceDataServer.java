package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SingleResourceDataServer extends ServerResource {
	
	private Logger logger = LoggerFactory.getLogger(SingleMetricDataServer.class
			.getName());
	
	
	
	@Delete
	public void deleteInstance(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String id = (String) this.getRequest().getAttributes().get("id");
			
			manager.deleteInstance(id);

			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
//		} catch (ComponentDoesNotExistException e) {
//			logger.error("The component does not exist", e);
//			this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,
//					"The component does not exist");
//			this.getResponse().setEntity("The component does not exist",
//					MediaType.TEXT_PLAIN);
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