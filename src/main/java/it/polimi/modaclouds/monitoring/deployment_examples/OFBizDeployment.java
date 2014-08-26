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
package it.polimi.modaclouds.monitoring.deployment_examples;

import it.polimi.modaclouds.qos_models.monitoring_ontology.CloudProvider;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class OFBizDeployment {

	public static void main(String[] args) {

		Set<Object> entities = new HashSet<Object>();
		try {

			CloudProvider amazonCloud = new CloudProvider();
			entities.add(amazonCloud);
			amazonCloud.setId("Amazon");

			VM amazonFrontendVM = new VM();
			entities.add(amazonFrontendVM);
			amazonFrontendVM.setId("FrontendVM1");
			amazonFrontendVM.setType("FrontendVM");
			amazonFrontendVM.setCloudProvider(amazonCloud.getId());

			VM amazonBackendVM = new VM();
			entities.add(amazonBackendVM);
			amazonBackendVM.setId("BackendVM1");
			amazonBackendVM.setType("BackendVM");
			amazonBackendVM.setCloudProvider(amazonCloud.getId());

			InternalComponent amazonJVM = new InternalComponent();
			entities.add(amazonJVM);
			amazonJVM.setId("JVM1");
			amazonJVM.setType("JVM");
			amazonJVM.addRequiredComponent(amazonFrontendVM.getId());

			InternalComponent amazonMySQL = new InternalComponent();
			entities.add(amazonMySQL);
			amazonMySQL.setId("MySQL1");
			amazonMySQL.setType("MySQL");
			amazonJVM.addRequiredComponent(amazonBackendVM.getId());

			InternalComponent amazonFrontend = new InternalComponent();
			entities.add(amazonFrontend);
			amazonFrontend.setId("Frontend1");
			amazonFrontend.setType("Frontend");
			amazonFrontend.addRequiredComponent(amazonJVM.getId());
			amazonFrontend.addRequiredComponent(amazonMySQL.getId());

			entities.add(addMethod(amazonFrontend, "addtocartbulk"));
			entities.add(addMethod(amazonFrontend, "checkLogin"));
			entities.add(addMethod(amazonFrontend, "checkoutoptions"));
			entities.add(addMethod(amazonFrontend, "addtocartbulk"));
			entities.add(addMethod(amazonFrontend, "login"));
			entities.add(addMethod(amazonFrontend, "logout"));
			entities.add(addMethod(amazonFrontend, "main"));
			entities.add(addMethod(amazonFrontend, "orderhistory"));
			entities.add(addMethod(amazonFrontend, "quickadd"));

			entities.add(addMethod(amazonMySQL, "create"));
			entities.add(addMethod(amazonMySQL, "read"));
			entities.add(addMethod(amazonMySQL, "update"));
			entities.add(addMethod(amazonMySQL, "delete"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println(entities);
	}

	private static Method addMethod(InternalComponent iComponent, String methodType)
			throws URISyntaxException {
		Method method = new Method(iComponent.getId(), methodType);
		iComponent.addProvidedMethod(method.getId());
		return method;
	}

}
