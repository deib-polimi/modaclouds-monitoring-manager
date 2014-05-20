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

import it.polimi.modaclouds.monitoring.kb.api.KBConnector;
import it.polimi.modaclouds.qos_models.monitoring_ontology.DataCollector;
import it.polimi.modaclouds.qos_models.monitoring_ontology.KBEntity;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DCFactoriesManager {

	private KBConnector knowledgeBase;
	private Map<KBEntity, Map<String, DataCollector>> dataCollectorsByMetricByEntity;
	private Map<String, Set<DataCollector>> dataCollectorsByRuleId;
	private Map<DataCollector, String> ruleIdByDataCollector;

	public DCFactoriesManager(KBConnector knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		dataCollectorsByMetricByEntity = new ConcurrentHashMap<KBEntity, Map<String, DataCollector>>();
		dataCollectorsByRuleId = new ConcurrentHashMap<String, Set<DataCollector>>();
		ruleIdByDataCollector = new ConcurrentHashMap<DataCollector, String>();
	}

	public void uninstallRule(MonitoringRule rule) {
		Set<DataCollector> dataCollectors = dataCollectorsByRuleId.remove(rule
				.getId());
		if (dataCollectors != null) {
			knowledgeBase.deleteAll(dataCollectors);
			for (DataCollector dc : dataCollectors) {
				ruleIdByDataCollector.remove(dc);
				for (KBEntity entity : dataCollectorsByMetricByEntity.keySet()) {
					dataCollectorsByMetricByEntity.get(entity).remove(
							rule.getCollectedMetric().getMetricName());
				}
			}
		}
	}

	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		Set<DataCollector> updatedDCs = Collections
				.newSetFromMap(new ConcurrentHashMap<DataCollector, Boolean>());
		String requiredMetric = rule.getCollectedMetric().getMetricName();
		for (MonitoredTarget target : rule.getMonitoredTargets()
				.getMonitoredTargets()) {
			Set<KBEntity> targetEntities = knowledgeBase.getByPropertyValue(
					Vocabulary.id, target.getId());
			for (KBEntity targetEntity : targetEntities) {
				Map<String, DataCollector> targetEntityDCbyMetric = dataCollectorsByMetricByEntity
						.get(targetEntity);
				if (targetEntityDCbyMetric == null) {
					targetEntityDCbyMetric = new ConcurrentHashMap<String, DataCollector>();
					dataCollectorsByMetricByEntity.put(targetEntity,
							targetEntityDCbyMetric);
				}
				if (targetEntityDCbyMetric.containsKey(requiredMetric)) {
					throw new RuleInstallationException(
							"Metric "
									+ requiredMetric
									+ " is already monitored on entity "
									+ targetEntity.getUri()
									+ " based on rule "
									+ ruleIdByDataCollector
											.get(dataCollectorsByMetricByEntity
													.get(targetEntity).get(
															requiredMetric)));
				}
				DataCollector dc = new DataCollector();
				dc.setCollectedMetric(requiredMetric);
				targetEntityDCbyMetric.put(requiredMetric, dc);
				ruleIdByDataCollector.put(dc, rule.getId());
				
				Util.addParameters(dc, rule.getCollectedMetric().getParameters());
				
				dc.setEnabled(true);
				updatedDCs.add(dc);
			}
		}
		dataCollectorsByRuleId.put(rule.getId(), updatedDCs);
		knowledgeBase.addAll(updatedDCs);
	}

}
