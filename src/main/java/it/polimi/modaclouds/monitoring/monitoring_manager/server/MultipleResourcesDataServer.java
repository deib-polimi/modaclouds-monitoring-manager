package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.gson.Gson;

public class MultipleResourcesDataServer extends ServerResource {

	private Logger logger = LoggerFactory
			.getLogger(MultipleResourcesDataServer.class);

	@Post
	public void updateResources(Representation rep) {
		try {
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String payload = rep.getText();

			Gson gson = new Gson();
			Model deserialised = gson.fromJson(payload, Model.class);

			// update model and responde to the post request
			manager.updateModel(deserialised);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

		} catch (Exception e) {
			logger.error("Error while adding components", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
			this.getResponse().setEntity(
					"Error while adding components:\n"
							+ Throwables.getStackTraceAsString(e),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	@Put
	public void uploadModel(Representation rep) {
		try {
			// invoking manager and payload of the post request
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			String payload = rep.getText();

			// deserialisation from json to ModelUpdates.class
			Gson gson = new Gson();
			Model deserialised = gson.fromJson(payload, Model.class);

			// upload the new model in the knowledge base and responde to the
			// request
			manager.uploadModel(deserialised);
			this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

		} catch (Exception e) {
			logger.error("Error while uploading the model", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while uploading the model: \n"
							+ Throwables.getStackTraceAsString(e),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

}