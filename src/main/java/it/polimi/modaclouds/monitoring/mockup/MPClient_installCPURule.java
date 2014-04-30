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

import it.polimi.csparqool.Function;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.qos_models.monitoring_ontology.DeterministicDataAnalyzer;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.SDAFactory;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.Actions;
import it.polimi.modaclouds.qos_models.schema.CollectedMetric;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoredTargets;
import it.polimi.modaclouds.qos_models.schema.MonitoringMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.Parameter;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPClient_installCPURule {

	private static Logger logger = LoggerFactory.getLogger(MPClient_installCPURule.class
			.getName());

	// private static final String amazonFEVMURL = "http://x.x.x.x";
	// private static final String amazonBEVMURL = "http://x.x.x.x";
	// private static final String amazonFEURL = "http://x.x.x.x:p";
	// private static final String amazonMySqlURL = "http://x.x.x.x:p";

	private static final String flexiFEVMURL = "http://109.231.122.88";
	private static final String flexiBEVMURL = "http://109.231.122.220";
	private static final String flexiFEURL = "http://109.231.122.88:8080";
	private static final String flexiMySqlURL = "http://109.231.122.220:3306";

	private static final String sdaFactoryVMURL = "http://109.231.122.88";
	private static final String sdaFactoryURL = "http://109.231.122.88:8176";

	private static final String ddaVMURL = "http://109.231.122.194";
	private static final String ddaURL = "http://109.231.122.194:8175";

	public static void main(String[] args) {
		try {
			MonitoringManager mp = new MonitoringManager();

			// *** MODELS @ RUNTIME *** //

			InternalComponent flexiFrontend = makeFlexiFrontendDeployment();
			mp.newInstance(flexiFrontend);

			SDAFactory sdaFactory = makeSDAFactoryDeployment();
			mp.newInstance(sdaFactory);

			DeterministicDataAnalyzer dda = makeDDADeployment();
			mp.newInstance(dda);

			// *** IDE or GUI *** //

			MonitoringRule cpuRule = makeCPURule();
			mp.installRule(cpuRule);

		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private static MonitoringRule makeCPURule() {
		MonitoringRule mr = new MonitoringRule();

		mr.setId("cpuRule");
		mr.setLabel("CPU rule");
		
		CollectedMetric metric = new CollectedMetric();
		mr.setCollectedMetric(metric);
		
		metric.setMetricName(Vocabulary.CpuUtilization);
		metric.setInherited(false);
	
		metric.getParameters().add(newParameter(Vocabulary.samplingProbability,"1"));
		metric.getParameters().add(newParameter(Vocabulary.samplingTime,"10"));
	
		MonitoringMetricAggregation metricAggregation = new MonitoringMetricAggregation();
		mr.setMetricAggregation(metricAggregation);
		
		metricAggregation.setAggregateFunction(Function.AVERAGE);
		metricAggregation.setGroupingClass(Vocabulary.CloudProvider);
		
		mr.setTimeStep("60");
		mr.setTimeWindow("60");
		
		MonitoredTargets targets = new MonitoredTargets();
		MonitoredTarget target = new MonitoredTarget();
		target.setId("FrontendVM");
		target.setClazz(Vocabulary.VM);
		targets.getMonitoredTargets().add(target);
		mr.setMonitoredTargets(targets);

		mr.setCondition("METRIC >= 0.6");

		Actions actions = new Actions();
		Action action = new Action();
		action.setName(Vocabulary.OutputMetric);
		action.getParameters().add(newParameter(Vocabulary.name,"CpuUtilizationViolation"));
		actions.getActions().add(action);
		mr.setActions(actions);
		return mr;
	}

	private static Parameter newParameter(String parameterName,
			String parameterValue) {
		Parameter parameter = new Parameter();
		parameter.setName(parameterName);
		parameter.setValue(parameterValue);
		return parameter;
	}

	private static InternalComponent makeFlexiFrontendDeployment() {
		VM flexiFrontendVM = new VM();
		flexiFrontendVM.setId("FrontendVM");
		flexiFrontendVM.setUrl(flexiFEVMURL);
		flexiFrontendVM.setCloudProvider("flexi");

		VM flexiBackendVM = new VM();
		flexiBackendVM.setId("BackendVM");
		flexiBackendVM.setUrl(flexiBEVMURL);
		flexiBackendVM.setCloudProvider("flexi");

		InternalComponent flexiJVM = new InternalComponent();
		flexiJVM.setId("JVM");
		flexiJVM.addRequiredComponent(flexiFrontendVM);

		InternalComponent flexiMySQL = new InternalComponent();
		flexiMySQL.setId("MySQL");
		flexiMySQL.setUrl(flexiMySqlURL);
		flexiJVM.addRequiredComponent(flexiBackendVM);

		InternalComponent flexiFrontend = new InternalComponent();
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

	private static SDAFactory makeSDAFactoryDeployment() {

		VM sdaVM = new VM();
		sdaVM.setUrl(sdaFactoryVMURL);
		SDAFactory sdaFactory = new SDAFactory();
		sdaFactory.setUrl(sdaFactoryURL);
		sdaFactory.addRequiredComponent(sdaVM);

		return sdaFactory;
	}

	private static DeterministicDataAnalyzer makeDDADeployment() {
		VM ddaVM = new VM();
		ddaVM.setUrl(ddaVMURL);
		DeterministicDataAnalyzer dda = new DeterministicDataAnalyzer();
		dda.setUrl(ddaURL);
		dda.addRequiredComponent(ddaVM);

		return dda;
	}
}
