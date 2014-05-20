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
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.AggregateFunction;
import it.polimi.modaclouds.qos_models.schema.GroupingCategory;
import it.polimi.modaclouds.qos_models.schema.Metric;
import it.polimi.modaclouds.qos_models.schema.Metrics;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;

public class MonitoringManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private KBConnector knowledgeBase;
	private CSPARQLEngineManager csparqlEngineManager;
	private DCFactoriesManager dcFactoriesManager;
	private SDAFactoryManager sdaFactoryManager;
	private Map<String, MonitoringRule> installedRules;

	private Config config;

	public MonitoringManager() throws InternalErrorException {
		try {
			knowledgeBase = KBConnector.getInstance();
			config = Config.getInstance();
			installedRules = new ConcurrentHashMap<String, MonitoringRule>();
			csparqlEngineManager = new CSPARQLEngineManager();
			dcFactoriesManager = new DCFactoriesManager(knowledgeBase);
			sdaFactoryManager = new SDAFactoryManager(knowledgeBase);
		} catch (Exception e) {
			logger.error("Inernal Error", e);
			throw new InternalErrorException(e);
		}
	}

	public void newInstance(Component instance) {
		knowledgeBase.add(instance);
	}

	public void installRules(MonitoringRules rules)
			throws RuleInstallationException {
		validate(rules);
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			installRule(rule);
		}
	}

	private void validate(MonitoringRules rules)
			throws RuleInstallationException {
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			if (installedRules.containsKey(rule.getId()))
				throw new RuleInstallationException("A rule with id "
						+ rule.getId() + " is already installed");
		}
		// TODO
	}

	public void uninstallRule(String id) throws RuleDoesNotExistException,
			FailedToUninstallRuleException {
		MonitoringRule rule = installedRules.get(id);
		if (rule == null)
			throw new RuleDoesNotExistException();
		csparqlEngineManager.uninstallRule(rule);
		dcFactoriesManager.uninstallRule(rule);
		sdaFactoryManager.uninstallRule(rule);
		installedRules.remove(id);
		
	}

	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		if (installedRules.containsKey(rule.getId()))
			throw new RuleInstallationException("A rule with id "
					+ rule.getId() + " is already installed");
		boolean sdaRequired = false;
		String sdaReturnedMetric = null;
		String aggregateFunction = null;
		String groupingClass = null;
		if (rule.getMetricAggregation() != null) {
			MonitoringRule pRule = rule;
			while (pRule.getMetricAggregation() != null
					&& rule.getMetricAggregation().isInherited()) {
				pRule = rule.getParentMonitoringRule();
			}
			aggregateFunction = pRule.getMetricAggregation()
					.getAggregateFunction();
			groupingClass = pRule.getMetricAggregation().getGroupingClass();
		}
		if (aggregateFunction != null) {
			boolean validAggregateFunction = false;
			List<AggregateFunction> availableFunctions = config
					.getAvailableAggregateFunctions().getAggregateFunctions();
			for (AggregateFunction availableFunction : availableFunctions) {
				if (aggregateFunction.equals(availableFunction.getName())) {
					validAggregateFunction = true;
					sdaRequired = availableFunction.getComputedBy().value().equals(
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
		if (groupingClass != null) {
			boolean validGroupingCategoryFunction = false;
			List<GroupingCategory> availableGroupingCategories = config
					.getAvailableGroupingClasses().getGroupingCategories();
			for (GroupingCategory availableGroupingCategory : availableGroupingCategories) {
				if (groupingClass.equals(availableGroupingCategory.getName())) {
					validGroupingCategoryFunction = true;
					break;
				}
			}
			if (!validGroupingCategoryFunction) {
				logger.error("Grouping category " + groupingClass
						+ " is not valid");
				throw new RuleInstallationException("Grouping category "
						+ groupingClass + " is not valid");
			}
		}
		if (sdaRequired) {
			sdaReturnedMetric = generateRandomMetricName();
		}
		try {
			csparqlEngineManager.installRule(rule, sdaRequired,
					sdaReturnedMetric);
			dcFactoriesManager.installRule(rule);
			if (sdaRequired)
				sdaFactoryManager.installRule(rule, aggregateFunction,
						sdaReturnedMetric);
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

	public Metrics getMetrics() {
		Metrics metrics = new Metrics();
		for (String observableMetric : csparqlEngineManager.getObservableMetrics()) {
			Metric metric = new Metric();
			metric.setName(observableMetric);
			metrics.getMetrics().add(metric);
		}
		return metrics;
	}

	public MonitoringRules getMonitoringRules() {
		MonitoringRules rules = new MonitoringRules();
		rules.getMonitoringRules().addAll(installedRules.values());
		return rules;
	}

	

	public String addObserver(String metricname, String callbackUrl)
			throws MetricDoesNotExistException, ServerErrorException, ObserverErrorException {
		String observerId = csparqlEngineManager.addObserver(metricname, callbackUrl);
		return observerId;
	}
}
