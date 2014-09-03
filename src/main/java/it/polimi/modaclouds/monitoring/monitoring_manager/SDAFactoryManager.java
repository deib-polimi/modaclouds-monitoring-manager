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

import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;
import it.polimi.modaclouds.qos_models.monitoring_ontology.StatisticalDataAnalyzer;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MOVocabulary;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;

public class SDAFactoryManager {

	private FusekiKBAPI knowledgeBase;
	private Map<String, StatisticalDataAnalyzer> sdaByRuleId;

	public SDAFactoryManager(FusekiKBAPI knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		sdaByRuleId = new ConcurrentHashMap<String, StatisticalDataAnalyzer>();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void installRule(MonitoringRule rule, String aggregateFunction,
			String sdaReturnedMetric) {
		StatisticalDataAnalyzer sda = sdaByRuleId.get(rule.getId());
		if (sda == null) {
			sda = new StatisticalDataAnalyzer();
			sdaByRuleId.put(rule.getId(), sda);
		}
		sda.setAggregateFunction(aggregateFunction);
		sda.addParameter(MOVocabulary.timeStep, rule.getTimeStep());
		sda.addParameter(MOVocabulary.timeWindow, rule
				.getTimeWindow());

		Util.addParameters(sda, rule.getMetricAggregation().getParameters());

		sda.addInputMetric(rule.getCollectedMetric().getMetricName());
		sda.setReturnedMetric(sdaReturnedMetric);

		Set<Resource> monitorableResources = Collections
				.newSetFromMap(new ConcurrentHashMap<Resource, Boolean>());
		
//		TODO:

//		for (MonitoredTarget target : rule.getMonitoredTargets()
//				.getMonitoredTargets()) {
//			monitorableResources.addAll((Set) knowledgeBase.getEntitiesByPropertyValue(
//					Vocabulary.id, target.getId()));
//		}
		

//		sda.setTargetResources(monitorableResources);
//		knowledgeBase.add(sda);
		throw new NotImplementedException();
	}

	public void uninstallRule(MonitoringRule rule) {
		// TODO:
//		StatisticalDataAnalyzer sda = sdaByRuleId.get(rule.getId());
//		if (sda != null)
//			knowledgeBase.deleteEntityById(sda.getId(), "id");
	}

}
