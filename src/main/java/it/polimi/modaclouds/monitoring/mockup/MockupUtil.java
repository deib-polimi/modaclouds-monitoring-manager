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
package it.polimi.modaclouds.monitoring.mockup;

import it.polimi.modaclouds.qos_models.monitoring_ontology.DeterministicDataAnalyzer;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;
import it.polimi.modaclouds.qos_models.schema.Parameter;

public class MockupUtil {
	
	private static final String amazonFEVMURL = "http://54.73.155.143";
	private static final String amazonBEVMURL = "http://54.246.34.122";
	private static final String amazonFEURL = "http://54.73.155.143:8080";
	private static final String amazonMySqlURL = "http://54.246.34.122:3306";
	
	private static final String flexiFEVMURL = "http://109.231.122.88";
	private static final String flexiBEVMURL = "http://109.231.122.220";
	private static final String flexiFEURL = "http://109.231.122.88:8080";
	private static final String flexiMySqlURL = "http://109.231.122.220:3306";
	
//	private static final String ddaVMURL = "http://109.231.122.194";
//	private static final String ddaURL = "http://109.231.122.194:8175";
	private static final String ddaVMURL = "http://54.74.25.183";
	private static final String ddaURL = "http://54.74.25.183:8175";
//	private static final String ddaVMURL = "http://localhost";
//	private static final String ddaURL = "http://localhost:8175";
	
	public static Parameter newParameter(String parameterName,
			String parameterValue) {
		Parameter parameter = new Parameter();
		parameter.setName(parameterName);
		parameter.setValue(parameterValue);
		return parameter;
	}

	public static InternalComponent makeFlexiFrontendDeployment() {
		VM flexiFrontendVM = new VM();
		flexiFrontendVM.setUri(MO.URI+"FrontendVM-1");
		flexiFrontendVM.setId("FrontendVM");
		flexiFrontendVM.setUrl(flexiFEVMURL);
		flexiFrontendVM.setCloudProvider("Flexi");

		VM flexiBackendVM = new VM();
		flexiBackendVM.setUri(MO.URI+"BackendVM-1");
		flexiBackendVM.setId("BackendVM");
		flexiBackendVM.setUrl(flexiBEVMURL);
		flexiBackendVM.setCloudProvider("Flexi");

		InternalComponent flexiJVM = new InternalComponent();
		flexiJVM.setUri(MO.URI + "JVM-1");
		flexiJVM.setId("JVM");
		flexiJVM.addRequiredComponent(flexiFrontendVM);

		InternalComponent flexiMySQL = new InternalComponent();
		flexiMySQL.setUri(MO.URI + "MySQL-1");
		flexiMySQL.setId("MySQL");
		flexiMySQL.setUrl(flexiMySqlURL);
		flexiJVM.addRequiredComponent(flexiBackendVM);

		InternalComponent flexiFrontend = new InternalComponent();
		flexiFrontend.setUri(MO.URI + "Frontend-1");
		flexiFrontend.setId("Frontend");
		flexiFrontend.setUrl(flexiFEURL);
		flexiFrontend.addRequiredComponent(flexiJVM);
		flexiFrontend.addRequiredComponent(flexiMySQL);

		flexiFrontend.addProvidedMethod(new Method("/addtocartbulk"));
		flexiFrontend.addProvidedMethod(new Method("/checkLogin"));
		flexiFrontend.addProvidedMethod(new Method("/checkoutoptions"));
		flexiFrontend.addProvidedMethod(new Method("/login"));
		flexiFrontend.addProvidedMethod(new Method("/logout"));
		flexiFrontend.addProvidedMethod(new Method("/main"));
		flexiFrontend.addProvidedMethod(new Method("/orderhistory"));
		flexiFrontend.addProvidedMethod(new Method("/quickadd"));

		flexiMySQL.addProvidedMethod(new Method("/create"));
		flexiMySQL.addProvidedMethod(new Method("/read"));
		flexiMySQL.addProvidedMethod(new Method("/update"));
		flexiMySQL.addProvidedMethod(new Method("/delete"));

		return flexiFrontend;
	}
	
	public static InternalComponent makeAmazonFrontendDeployment() {
		VM amazonFrontendVM = new VM();
		amazonFrontendVM.setUri(MO.URI+"FrontendVM-1");
		amazonFrontendVM.setId("FrontendVM");
		amazonFrontendVM.setUrl(amazonFEVMURL);
		amazonFrontendVM.setCloudProvider("Amazon");

		VM amazonBackendVM = new VM();
		amazonBackendVM.setUri(MO.URI+"BackendVM-1");
		amazonBackendVM.setId("BackendVM");
		amazonBackendVM.setUrl(amazonBEVMURL);
		amazonBackendVM.setCloudProvider("Amazon");

		InternalComponent amazonJVM = new InternalComponent();
		amazonJVM.setUri(MO.URI + "JVM-1");
		amazonJVM.setId("JVM");
		amazonJVM.addRequiredComponent(amazonFrontendVM);

		InternalComponent amazonMySQL = new InternalComponent();
		amazonMySQL.setUri(MO.URI + "MySQL-1");
		amazonMySQL.setId("MySQL");
		amazonMySQL.setUrl(amazonMySqlURL);
		amazonJVM.addRequiredComponent(amazonBackendVM);

		InternalComponent amazonFrontend = new InternalComponent();
		amazonFrontend.setUri(MO.URI + "Frontend-1");
		amazonFrontend.setId("Frontend");
		amazonFrontend.setUrl(amazonFEURL);
		amazonFrontend.addRequiredComponent(amazonJVM);
		amazonFrontend.addRequiredComponent(amazonMySQL);

		amazonFrontend.addProvidedMethod(new Method("/addtocartbulk"));
		amazonFrontend.addProvidedMethod(new Method("/checkLogin"));
		amazonFrontend.addProvidedMethod(new Method("/checkoutoptions"));
		amazonFrontend.addProvidedMethod(new Method("/login"));
		amazonFrontend.addProvidedMethod(new Method("/logout"));
		amazonFrontend.addProvidedMethod(new Method("/main"));
		amazonFrontend.addProvidedMethod(new Method("/orderhistory"));
		amazonFrontend.addProvidedMethod(new Method("/quickadd"));

		amazonMySQL.addProvidedMethod(new Method("/create"));
		amazonMySQL.addProvidedMethod(new Method("/read"));
		amazonMySQL.addProvidedMethod(new Method("/update"));
		amazonMySQL.addProvidedMethod(new Method("/delete"));

		return amazonFrontend;
	}

	public static DeterministicDataAnalyzer makeDDADeployment() {
		VM ddaVM = new VM();
		ddaVM.setUrl(ddaVMURL);
		DeterministicDataAnalyzer dda = new DeterministicDataAnalyzer();
		dda.setUrl(ddaURL);
		dda.addRequiredComponent(ddaVM);

		return dda;
	}
}
