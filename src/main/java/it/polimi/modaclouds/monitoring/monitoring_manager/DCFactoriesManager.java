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
import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCFactoriesManager {

	private Logger logger = LoggerFactory.getLogger(DCFactoriesManager.class);

	private FusekiKBAPI knowledgeBase;

	public DCFactoriesManager(FusekiKBAPI knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public void uninstallRule(MonitoringRule rule) throws FailedToUninstallRuleException {
		logger.info("Removing data collectors related to rule {} from KB",
				rule.getId());
		try {
			knowledgeBase.deleteEntitiesByPropertyValue(rule.getId(),
					DCFields.monitoringRuleId);
		} catch (SerializationException e) {
			throw new FailedToUninstallRuleException(e);
		}

	}

	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		logger.info("Adding data collectors related to rule {} to KB",
				rule.getId());
		Set<String> dataCollectorsIds = new HashSet<String>();
		Set<DCMetaData> dataCollectors = new HashSet<DCMetaData>();
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {

			DCMetaData dc = new DCMetaData();
			dc.setMonitoringRuleId(rule.getId());

			dataCollectorsIds.add(dc.getId());
			dataCollectors.add(dc);

			if (target.getId() != null) // TODO THIS WILL CHANGE TO TYPE
				dc.addMonitoredResourceType(target.getId());
			if (target.getClazz() != null)
				dc.addMonitoredResourceClass(target.getClazz());
			Util.addParameters(dc, rule.getCollectedMetric().getParameters());
			
			dc.setMonitoredMetric(rule.getCollectedMetric().getMetricName());

		}
		try {
			knowledgeBase.add(dataCollectors, DCFields.id);
		} catch (SerializationException | DeserializationException e) {
			throw new RuleInstallationException(e);
		}

	}

}
