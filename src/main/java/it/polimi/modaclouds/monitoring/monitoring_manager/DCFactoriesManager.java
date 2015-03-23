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
package it.polimi.modaclouds.monitoring.monitoring_manager;

import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.monitoring.dcfactory.DCFields;
import it.polimi.modaclouds.monitoring.dcfactory.DCVocabulary;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.qos_models.schema.Metric;
import it.polimi.modaclouds.qos_models.schema.Metric.RequiredParameter;
import it.polimi.modaclouds.qos_models.schema.Metrics;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.util.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCFactoriesManager {

	private Logger logger = LoggerFactory.getLogger(DCFactoriesManager.class);

	private FusekiKBAPI knowledgeBase;
	private Map<String, DCConfig> dcConfigByRuleId;

	public DCFactoriesManager(FusekiKBAPI knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		dcConfigByRuleId = new HashMap<String, DCConfig>();
	}

	public synchronized void uninstallRule(String ruleId) {
		logger.debug(
				"Removing data collectors configurations related to rule {} from KB",
				ruleId);
		try {
			DCConfig dcConfig = dcConfigByRuleId.get(ruleId);
			if (dcConfig != null) {
				knowledgeBase.deleteEntitiesByPropertyValue(dcConfig.getId(),
						DCFields.id, DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
			} else {
				logger.warn("No dc configuration found for rule {}", ruleId);
			}
		} catch (SerializationException e) {
			logger.error(
					"Error while deleting data collector configuration related to rule {} from KB",
					ruleId, e);
		}

	}

	public synchronized void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		logger.debug("Adding data collectors related to rule {} to KB",
				rule.getId());
		try {
			DCConfig dc = makeDCConfiguration(rule, Config.getInstance()
					.getMonitoringMetrics());
			knowledgeBase.add(dc, DCFields.id,
					DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
			dcConfigByRuleId.put(rule.getId(), dc);
		} catch (Exception e) {
			throw new RuleInstallationException(e);
		}

	}

	private DCConfig makeDCConfiguration(MonitoringRule rule,
			Metrics availableMetrics) {
		String metricName = rule.getCollectedMetric().getMetricName()
				.toLowerCase();
		DCConfig dc = new DCConfig();
		Util.addParameters(dc, rule.getCollectedMetric().getParameters(),
				getMetricParameters(metricName, availableMetrics));
		dc.setMonitoredMetric(metricName);
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			if (target.getType() != null)
				dc.addMonitoredResourceType(target.getType());
			if (target.getClazz() != null)
				dc.addMonitoredResourceClass(target.getClazz());
		}
		return dc;
	}

	public List<RequiredParameter> getMetricParameters(String metricName,
			Metrics metrics) {
		for (Metric metric : metrics.getMetrics()) {
			if (metric.getName().equalsIgnoreCase(metricName))
				return metric.getRequiredParameters();
		}
		return null;
	}

}
