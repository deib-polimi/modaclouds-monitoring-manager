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

import it.polimi.modaclouds.monitoring.monitoring_manager.ConfigurationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.configuration.ManagerConfig;

import org.apache.jena.atlas.web.HttpException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateServer extends Application {
	private Component component;
	private MonitoringManager manager = null;

	private static Logger logger = LoggerFactory.getLogger(PrivateServer.class);

	public PrivateServer(MonitoringManager manager, Component component) {
		this.manager = manager;
		this.component = component;
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
		
		router.attach(ManagerConfig.getInstance().getActionExecutorPath(),
				ActionsExecutorServer.class);

		return router;
	}
}
