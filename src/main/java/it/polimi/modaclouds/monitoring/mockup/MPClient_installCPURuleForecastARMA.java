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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPClient_installCPURuleForecastARMA {

	private static Logger logger = LoggerFactory
			.getLogger(MPClient_installCPURuleForecastARMA.class.getName());

	

//	private static final String sdaFactoriesVMURL = "http://109.231.122.88";
//	private static final String matlab_sdaFactoryURL = "http://109.231.122.88:8176";
//	private static final String java_sdaFactoryURL = "http://109.231.122.88:8177";	
	
	private static final String sdaFactoriesVMURL = "http://54.195.5.240";
	private static final String matlab_sdaFactoryURL = "http://54.195.5.240:8176";
	private static final String java_sdaFactoryURL = "http://54.195.5.240:8177";	
	
	private static final String flexiTextualObsURL = "http://109.231.122.221:8123/v1/results";
	private static final String amazonTextualObsURL = "http://54.195.5.240:8123/v1/results";
	private static final String graphiteObsURL = "http://54.73.192.6:9998/graphiteMetricsService/sendMetrics";
	
	private static final String observerURL = graphiteObsURL;

//	private static final String sdaFactoriesVMURL = "http://localhost";
//	private static final String matlab_sdaFactoryURL = "http://localhost:8176";
//	private static final String java_sdaFactoryURL = "http://localhost:8177";

//	
//	private static final String textualObserverURL = "http://localhost:8123/v1/results";

	public static void main(String[] args) {
		try {
			MonitoringManager mp = new MonitoringManager();

			// *** MODELS @ RUNTIME *** //

//			InternalComponent flexiFrontend = MockupUtil.makeFlexiFrontendDeployment();
//			mp.newInstance(flexiFrontend);
			
			InternalComponent amazonFrontend = MockupUtil.makeAmazonFrontendDeployment();
			mp.newInstance(amazonFrontend);

			VM sdaVM = new VM();
			sdaVM.setUrl(sdaFactoriesVMURL);
			SDAFactory matlabSdaFactory = new SDAFactory();
			SDAFactory javaSdaFactory = new SDAFactory();
			matlabSdaFactory.setUrl(matlab_sdaFactoryURL);
			javaSdaFactory.setUrl(java_sdaFactoryURL);
			matlabSdaFactory.addRequiredComponent(sdaVM);
			javaSdaFactory.addRequiredComponent(sdaVM);
			mp.newInstance(matlabSdaFactory);
			mp.newInstance(javaSdaFactory);

			DeterministicDataAnalyzer dda = MockupUtil.makeDDADeployment();
			mp.newInstance(dda);

			// *** IDE or GUI *** //

			MonitoringRule cpuRule = makeCPURule();
			mp.installRule(cpuRule);

			// *** ///
//			Metrics metrics = mp.getMetrics();
//			for (Metric metric : metrics.getMetrics()) {
//				System.out.println(metric.getName());
//			}

			mp.addObserver("CpuForecastViolation", observerURL);

		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private static MonitoringRule makeCPURule() {
		MonitoringRule mr = new MonitoringRule();

		mr.setId("cpuFCRule");
		mr.setLabel("CPU Forecast rule");

		CollectedMetric metric = new CollectedMetric();
		mr.setCollectedMetric(metric);

		metric.setMetricName(Vocabulary.CpuUtilization);
		metric.setInherited(false);

		metric.getParameters().add(
				MockupUtil.newParameter(Vocabulary.samplingProbability, "1"));
		metric.getParameters().add(MockupUtil.newParameter(Vocabulary.samplingTime, "3"));

		MonitoringMetricAggregation metricAggregation = new MonitoringMetricAggregation();
		mr.setMetricAggregation(metricAggregation);

		metricAggregation
				.setAggregateFunction(Function.FORECASTING_TIME_SERIES_ARMA);
		metricAggregation.setGroupingClass(Vocabulary.CloudProvider);
		metricAggregation.getParameters().add(
				MockupUtil.newParameter(Vocabulary.returnedMetric, "CpuForecast"));
		metricAggregation.getParameters().add(
				MockupUtil.newParameter(Vocabulary.autoregressive, "1"));
		metricAggregation.getParameters().add(
				MockupUtil.newParameter(Vocabulary.movingAverage, "1"));
		metricAggregation.getParameters().add(
				MockupUtil.newParameter(Vocabulary.forecastPeriod, "5"));

		mr.setTimeStep("10");
		mr.setTimeWindow("10");

		MonitoredTargets targets = new MonitoredTargets();
		MonitoredTarget target = new MonitoredTarget();
		target.setId("FrontendVM");
		target.setClazz(Vocabulary.VM);
		targets.getMonitoredTargets().add(target);
		mr.setMonitoredTargets(targets);

		mr.setCondition("METRIC >= 0.5");

		Actions actions = new Actions();
		Action action = new Action();
		action.setName(Vocabulary.OutputMetric);
		action.getParameters().add(
				MockupUtil.newParameter(Vocabulary.name, "CpuForecastViolation"));
		actions.getActions().add(action);
		mr.setActions(actions);
		return mr;
	}

	
}
