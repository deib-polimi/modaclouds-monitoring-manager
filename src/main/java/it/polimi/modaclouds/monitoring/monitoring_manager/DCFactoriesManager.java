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
import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.HashMap;
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
		logger.info(
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
					"Error while deleting data collector related to rule {} from KB",
					ruleId, e);
		}

	}

	public synchronized void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		logger.info("Adding data collectors related to rule {} to KB",
				rule.getId());
		DCConfig dc = makeDCConfiguration(rule);
		try {
			knowledgeBase.add(dc, DCFields.id,
					DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
			dcConfigByRuleId.put(rule.getId(), dc);
		} catch (SerializationException | DeserializationException e) {
			throw new RuleInstallationException(e);
		}

	}

	private DCConfig makeDCConfiguration(MonitoringRule rule) {
		String metricName = rule.getCollectedMetric().getMetricName()
				.toLowerCase();
		DCConfig dc = new DCConfig();
		Util.addParameters(dc, rule.getCollectedMetric().getParameters());
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

}
