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

import it.polimi.modaclouds.monitoring.monitoring_manager.Config;
import it.polimi.modaclouds.monitoring.monitoring_manager.ConfigurationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;

import org.apache.jena.atlas.web.HttpException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMServer extends Application {
	private static Component component;
	private static MonitoringManager manager = null;
	private static final String apiVersion = "v1";

	private static Logger logger = LoggerFactory.getLogger(MMServer.class);

	public static void main(String[] args) {

		
		try {
			Config.init(args);
		} catch (ConfigurationException e) {
			System.err.println("Environment variables not set correctly: " + e.getMessage());
			System.err.println("Run \"monitoring-manager -help\" for help");
			return;
		}
		if (Config.getInstance().isHelp()) {
			System.out.println(Config.usage);
			return;
		}
		
		logger.info("Current configuration:\n{}", Config.getInstance().toString());

		try {
			manager = new MonitoringManager(Config.getInstance());

			System.setProperty("org.restlet.engine.loggerFacadeClass",
					"org.restlet.ext.slf4j.Slf4jLoggerFacade");
			component = new Component();
			component.getServers().add(Protocol.HTTP, Config.getInstance().getMmPort());
			component.getClients().add(Protocol.FILE);

			MMServer mmServer = new MMServer();
			component.getDefaultHost().attach("", mmServer);

			component.start();
		} catch (HttpException e) {
			logger.error("Could not connect to Knowledge Base: {}", e.getMessage());
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

		return router;
	}
}
