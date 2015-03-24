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

import it.polimi.deib.csparql_rest_api.exception.ServerErrorException;
import it.polimi.modaclouds.monitoring.monitoring_manager.ConfigurationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.configuration.ManagerConfig;

import java.io.IOException;

import org.apache.jena.atlas.web.HttpException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMServer extends Application {
	private Component component;
	private MonitoringManager manager = null;
	private static final String apiVersion = "v1";

	private static Logger logger = LoggerFactory.getLogger(MMServer.class);

	public MMServer(MonitoringManager manager, Component component) {
		this.manager = manager;
		this.component = component;
	}

	public static void main(String[] args) {

		try {
			ManagerConfig.init(args);
		} catch (ConfigurationException e) {
			System.err.println("Configuration problem: " + e.getMessage());
			System.err.println("Run \"monitoring-manager -help\" for help");
			return;
		}
		if (ManagerConfig.getInstance().isHelp()) {
			System.out.println(ManagerConfig.usage);
			return;
		}

		logger.info("Current configuration:\n{}", ManagerConfig.getInstance()
				.toString());

		try {
			MonitoringManager manager = new MonitoringManager(
					ManagerConfig.getInstance());

			System.setProperty("org.restlet.engine.loggerFacadeClass",
					"org.restlet.ext.slf4j.Slf4jLoggerFacade");
			Component component = new Component();
			component.getServers().add(Protocol.HTTP,
					ManagerConfig.getInstance().getMmPort());
			component.getClients().add(Protocol.CLAP);
			component.getDefaultHost().attach("",
					new MMServer(manager, component));

			logger.info("Starting Monitoring Manager public server on port "
					+ ManagerConfig.getInstance().getMmPort());
			component.start();

			Component privateComponent = new Component();
			privateComponent.getServers().add(Protocol.HTTP,
					ManagerConfig.getInstance().getMmPrivatePort());
			PrivateServer privateServer = new PrivateServer(manager,
					privateComponent);
			privateComponent.getDefaultHost().attach("", privateServer);
			logger.info("Starting Monitoring Manager private server on port "
					+ ManagerConfig.getInstance().getMmPrivatePort());
			privateComponent.start();

		} catch (HttpException | IOException | ServerErrorException e) {
			logger.error("Connection problem: {}", e.getMessage());
		} catch (ConfigurationException e) {
			logger.error("Configuration problem: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Unknown error", e);
		}
	}

	public Restlet createInboundRoot() {

		String server_address = component.getServers().get(0).getAddress();
		if (server_address == null) {
			server_address = "http://localhost";
			server_address = server_address
					+ ":"
					+ String.valueOf(component.getServers().get(0)
							.getActualPort());
		}

		getContext().getAttributes().put("complete_server_address",
				server_address);
		getContext().getAttributes().put("manager", manager);

		Router router = new Router(getContext());
		router.setDefaultMatchingMode(Template.MODE_EQUALS);

		router.attach("/" + apiVersion + "/monitoring-rules",
				MultipleRulesDataServer.class);
		router.attach("/" + apiVersion + "/monitoring-rules/{id}",
				SingleRuleDataServer.class);
		router.attach("/" + apiVersion + "/metrics",
				MultipleMetricsDataServer.class);
		router.attach("/" + apiVersion + "/metrics/{metricname}",
				SingleMetricDataServer.class);
		router.attach("/" + apiVersion + "/metrics/{metricname}/observers",
				MultipleObserversDataServer.class);
		router.attach(
				"/" + apiVersion + "/metrics/{metricname}/observers/{id}",
				SingleObserverDataServer.class);

		router.attach("/" + apiVersion + "/model/resources",
				MultipleResourcesDataServer.class);
		router.attach("/" + apiVersion + "/model/resources/{id}",
				SingleResourceDataServer.class);

		Redirector redirector = new Redirector(getContext(),
				"/webapp/index.html", Redirector.MODE_CLIENT_PERMANENT);
		router.attach("/webapp",redirector);
		router.attach("/webapp/",redirector);
		
		final Directory dir = new Directory(getContext(), new LocalReference(
				"clap://class/webapp"));
		dir.setListingAllowed(false);
		dir.setDeeplyAccessible(true);
		dir.setIndexName("index");
		TemplateRoute route = router.attach("/webapp/", dir);
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		

		return router;
	}
}
