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
import it.polimi.modaclouds.monitoring.dcfactory.kbconnectors.FusekiConnector;
import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCFactoriesManager {

	private Logger logger = LoggerFactory.getLogger(DCFactoriesManager.class);

	private FusekiKBAPI knowledgeBase;

	public DCFactoriesManager(FusekiKBAPI knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public void uninstallRule(MonitoringRule rule)
			throws FailedToUninstallRuleException {
		logger.info("Removing data collectors related to rule {} from KB",
				rule.getId());
		try {
			knowledgeBase.deleteEntitiesByPropertyValue(rule.getId(),
					DCFields.monitoringRuleId,
					DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
		} catch (SerializationException e) {
			throw new FailedToUninstallRuleException(e);
		}

	}

	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		logger.info("Adding data collectors related to rule {} to KB",
				rule.getId());
		DCMetaData dc = new DCMetaData();
		dc.setMonitoringRuleId(rule.getId());
		Util.addParameters(dc, rule.getCollectedMetric().getParameters());
		dc.setMonitoredMetric(rule.getCollectedMetric().getMetricName());
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			if (target.getType() != null)
				dc.addMonitoredResourceType(target.getType());
			if (target.getClazz() != null)
				dc.addMonitoredResourceClass(target.getClazz());
		}
		try {
			dc.setId("dc" + dc.hashCode()); // identical dc won't be persisted
			knowledgeBase.add(dc, DCFields.id, DCVocabulary.DATA_COLLECTORS_GRAPH_NAME);
		} catch (SerializationException | DeserializationException e) {
			throw new RuleInstallationException(e);
		}

	}

}
