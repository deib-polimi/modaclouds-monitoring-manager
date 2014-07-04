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
	
	@Post("json")
	public void uploadModel(Representation rep){
		try {
			MonitoringManager manager = (MonitoringManager) getContext().getAttributes().get("manager");
			
			//[{"started":true,"id":"0","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:b94b65f7-fd5c-416f-a43b-c72644134fbc","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/b94b65f7-fd5c-416f-a43b-c72644134fbc"},{"started":true,"id":"1","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:f50551cf-2807-4919-8e18-162d56675b46","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/f50551cf-2807-4919-8e18-162d56675b46"},{"started":true,"id":"2","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:b0ce36cf-c6a0-4c30-a72b-e5e1d5c8cde8","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/b0ce36cf-c6a0-4c30-a72b-e5e1d5c8cde8"},{"started":true,"id":"3","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:86fa89b6-b8d0-4777-a1f5-94aca8ce922c","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/86fa89b6-b8d0-4777-a1f5-94aca8ce922c"},{"started":true,"id":"4","logger":{"currentLogLevel":20,"name":"it.polimi.modaclouds.qos_models.monitoring_ontology.Component"},"shortURI":"mo:8f406c1b-1965-40d3-9d76-ca0309711110","uri":"http://www.modaclouds.eu/rdfs/1.0/monitoring/8f406c1b-1965-40d3-9d76-ca0309711110"}]
			
			//ricordarsi problema logger
			
			// {"vms":[{"numberOfCpus":0,"started":false,"url":"abcd","id":"0"}],"components":[{"started":true,"url":"abcd-compo","id":"0comp"},{"started":true,"url":"abcd-compo","id":"1comp"}],"externalComponents":[{"cloudProvider":"me","started":false,"url":"abcd-extcompo","id":"0extcomp"},{"cloudProvider":"me","started":false,"url":"abcd-extcompo","id":"1extcomp"},{"cloudProvider":"me","started":false,"url":"abcd-extcompo","id":"2extcomp"}]}
			
			/* test to serialize object
			ComponentForJson test = new ComponentForJson();
			
			List<VM> vms = new ArrayList<VM>();
			List<Component> components = new ArrayList<Component>();
			List<ExternalComponent> externalComponents = new ArrayList<ExternalComponent>();
			
			for(int i = 0; i<1;i++){
				VM vm = new VM();
				vm.setId(Integer.toString(i));
				vm.setNumberOfCpus(i);
				vm.setUrl("abcd");
				vms.add(vm);
			}
			
			for(int i = 0; i<2;i++){
				Component component = new Component();
				component.setId(Integer.toString(i)+"comp");
				component.setStarted(true);
				component.setUrl("abcd-compo");
				components.add(component);
			}
			
			for(int i = 0; i<3;i++){
				ExternalComponent externalComponent = new ExternalComponent();
				externalComponent.setId(Integer.toString(i)+"extcomp");
				externalComponent.setStarted(false);
				externalComponent.setUrl("abcd-extcompo");
				externalComponent.setCloudProvider("me");
				externalComponents.add(externalComponent);
			}
			
			test.setVms(vms);
			test.setComponents(components);
			test.setExternalComponents(externalComponents);
			*/

			String payload = rep.getText();
			
			Gson gson = new Gson();
			
			ModelUpdates deserialised = gson.fromJson(payload, ModelUpdates.class);
			
			/* test to check deserialized object
			List<VM> vms = new ArrayList<VM>();
			List<Component> components = new ArrayList<Component>();
			List<ExternalComponent> externalComponents = new ArrayList<ExternalComponent>();
			
			vms = upload.getVms();
			components = upload.getComponents();
			externalComponents = upload.getExternalComponents();
			

			for(int i = 0; i<vms.size();i++){
				System.out.println(vms.get(i).getId());
				System.out.println(vms.get(i).getUrl());
				System.out.println("");
			}
			
			for(int i = 0; i<components.size();i++){
				System.out.println(components.get(i).getId());
				System.out.println(components.get(i).getUrl());
				System.out.println(components.get(i).isStarted());
				System.out.println("");
			}
			for(int i = 0; i<externalComponents.size();i++){
				System.out.println(externalComponents.get(i).getId());
				System.out.println(externalComponents.get(i).getUrl());
				System.out.println(externalComponents.get(i).getCloudProvider());
				System.out.println("");
			}*/
			
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