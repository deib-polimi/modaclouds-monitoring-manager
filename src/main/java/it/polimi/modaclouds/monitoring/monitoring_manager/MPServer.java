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
import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;
import it.polimi.modaclouds.qos_models.monitoring_rules.RuleValidationException;
import it.polimi.modaclouds.qos_models.schema.AggregateFunction;
import it.polimi.modaclouds.qos_models.schema.GroupingCategories;
import it.polimi.modaclouds.qos_models.schema.GroupingCategory;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPServer implements MonitoringPlatform {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private KBConnector knowledgeBase;
	private CSPARQLEngineManager csparqlEngineManager;
	private DCFactoriesManager dcFactoriesManager;
	private SDAFactoryManager sdaFactoryManager;
	private Map<String,MonitoringRule> installedRules;

	private Config config;

	public MPServer() throws InternalErrorException {
		try {
			knowledgeBase = KBConnector.getInstance();
			config = Config.getInstance();
			installedRules = new HashMap<String,MonitoringRule>();
			csparqlEngineManager = new CSPARQLEngineManager();
			dcFactoriesManager = new DCFactoriesManager(knowledgeBase);
			sdaFactoryManager = new SDAFactoryManager(knowledgeBase);
		} catch (Exception e) {
			logger.error("Inernal Error", e);
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void newInstance(Component instance) {
		knowledgeBase.add(instance);
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		if (installedRules.containsKey(rule.getId()))
			throw new RuleInstallationException("A rule with id " + rule.getId() + " is already installed");
		boolean sdaRequired = false;
		String sdaReturnedMetric = null;
		String aggregateFunction = null;
		String groupingCategory = null;
		if (rule.getMetricAggregation() != null) {
			MonitoringRule pRule = rule;
			while (pRule.getMetricAggregation() != null
					&& rule.getMetricAggregation().isInherited()) {
				pRule = rule.getParentMonitoringRule();
			}
			aggregateFunction = pRule.getMetricAggregation()
					.getAggregateFunction();
			groupingCategory = pRule.getMetricAggregation().getGroupingCategoryName();
		}
		if (aggregateFunction != null) {
			boolean validAggregateFunction = false;
			List<AggregateFunction> availableFunctions = config
					.getAvailableAggregateFunctions().getAggregateFunctions();
			for (AggregateFunction availableFunction : availableFunctions) {
				if (aggregateFunction.equals(availableFunction.getName())) {
					validAggregateFunction = true;
					sdaRequired = availableFunction.getComputedBy().equals(
							Vocabulary.StatisticalDataAnalyzer);
					break;
				}
			}
			if (!validAggregateFunction) {
				logger.error("Aggregate function " + aggregateFunction
						+ " is not valid");
				throw new RuleInstallationException("Aggregate function "
						+ aggregateFunction + " is not valid");
			}
		}
		if (groupingCategory != null) {
			boolean validGroupingCategoryFunction = false;
			List<GroupingCategory> availableGroupingCategories = config
					.getAvailableGroupingCategories().getGroupingCategories();
			for (GroupingCategory availableGroupingCategory : availableGroupingCategories) {
				if (groupingCategory.equals(availableGroupingCategory.getName())) {
					validGroupingCategoryFunction = true;
					break;
				}
			}
			if (!validGroupingCategoryFunction) {
				logger.error("Grouping category " + groupingCategory
						+ " is not valid");
				throw new RuleInstallationException("Grouping category " + groupingCategory
						+ " is not valid");
			}
		}
		if (sdaRequired) {
			sdaReturnedMetric = generateRandomMetricName();
		}
		try {
			csparqlEngineManager.installRule(rule, aggregateFunction, groupingCategory, sdaRequired, sdaReturnedMetric);
			dcFactoriesManager.installRule(rule);
			if (sdaRequired)
				sdaFactoryManager.installRule(rule, aggregateFunction, sdaReturnedMetric);
			installedRules.put(rule.getId(), rule);
		} catch (Exception e) {
			// TODO rollback
			logger.error("Error while installing rule", e);
			throw new RuleInstallationException(e);
		}
	}

	private String generateRandomMetricName() {
		return escape(UUID.randomUUID().toString());
	}

	private String escape(String string) {
		return string.replaceAll("[^a-zA-Z0-9]", "");
	}
}
