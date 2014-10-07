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

import it.polimi.modaclouds.monitoring.dcfactory.DCFields;
import it.polimi.modaclouds.monitoring.dcfactory.DCMetaData;
import it.polimi.modaclouds.monitoring.dcfactory.DCVocabulary;
import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCFactoriesManager {

	private Logger logger = LoggerFactory.getLogger(DCFactoriesManager.class);

	private FusekiKBAPI knowledgeBase;
	private Map<String, List<String>> rulesIdByMetric;

	public DCFactoriesManager(FusekiKBAPI knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		rulesIdByMetric = new HashMap<String, List<String>>();
	}

	public synchronized void uninstallRule(MonitoringRule rule) {
		logger.info("Removing data collectors related to rule {} from KB",
				rule.getId());
		try {
			String metricName = rule.getCollectedMetric().getMetricName().toLowerCase();
			unregisterRuleFromMetric(rule, metricName);
			if (noRulesRegisteredToMetric(metricName)) {
				knowledgeBase.deleteEntitiesByPropertyValue(metricName,
						DCFields.monitoredMetric,
						DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
			}
		} catch (SerializationException e) {
			logger.error(
					"Error while deleting data collector related to rule {} from KB",
					rule.getId(), e);
		}

	}

	private boolean noRulesRegisteredToMetric(String metricName) {
		return rulesIdByMetric.get(metricName) == null || rulesIdByMetric.get(metricName).isEmpty();
	}

	private void unregisterRuleFromMetric(MonitoringRule rule, String metricName) {
		List<String> registeredRules = rulesIdByMetric.get(metricName);
		if (registeredRules != null) {
			registeredRules.remove(rule.getId());
		}
	}

	public synchronized void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		logger.info("Adding data collectors related to rule {} to KB",
				rule.getId());
		String metricName = rule.getCollectedMetric().getMetricName().toLowerCase();
		DCMetaData dc = new DCMetaData();
		Util.addParameters(dc, rule.getCollectedMetric().getParameters());
		dc.setMonitoredMetric(metricName);
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			if (target.getType() != null)
				dc.addMonitoredResourceType(target.getType());
			if (target.getClazz() != null)
				dc.addMonitoredResourceClass(target.getClazz());
		}
		try {
			knowledgeBase.add(dc, DCFields.monitoredMetric,
					DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
			registerRuleForMetric(rule, metricName);
		} catch (SerializationException | DeserializationException e) {
			throw new RuleInstallationException(e);
		}

	}

	private void registerRuleForMetric(MonitoringRule rule, String metricName) {
		List<String> registeredRules = rulesIdByMetric.get(metricName);
		if (registeredRules == null) {
			registeredRules = new ArrayList<String>();
			rulesIdByMetric.put(metricName, registeredRules);
		}
		registeredRules.add(rule.getId());
	}

}
