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

import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.server.Model;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MOVocabulary;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;
import it.polimi.modaclouds.qos_models.monitoring_rules.Problem;
import it.polimi.modaclouds.qos_models.monitoring_rules.Validator;
import it.polimi.modaclouds.qos_models.schema.Metric;
import it.polimi.modaclouds.qos_models.schema.Metrics;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;

public class MonitoringManager {

	public static final String MODEL_GRAPH_NAME = "model";

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private CSPARQLEngineManager csparqlEngineManager;
	private DCFactoriesManager dcFactoriesManager;
	private Map<String, MonitoringRule> installedRules;

	private Validator validator;

	private FusekiKBAPI knowledgeBase;

	public MonitoringManager(Config config) throws Exception {
		validator = new Validator();
		knowledgeBase = new FusekiKBAPI(config.getKbUrl());
		installedRules = new ConcurrentHashMap<String, MonitoringRule>();
		csparqlEngineManager = new CSPARQLEngineManager(config, knowledgeBase);
		dcFactoriesManager = new DCFactoriesManager(knowledgeBase);

		logger.info("Uploading ontology to KB");
		knowledgeBase.uploadOntology(MO.model, MODEL_GRAPH_NAME);
	}

	public synchronized void installRules(MonitoringRules rules)
			throws RuleInstallationException {
		validate(rules);
		String installedRules = "";
		try {
			for (MonitoringRule rule : rules.getMonitoringRules()) {
				installRule(rule);
				installedRules += " " + rule.getId();
			}
		} catch (RuleInstallationException e) {
			throw new RuleInstallationException(
					"Error while installing rules, only the following rules were successfully installed:"
							+ installedRules, e);
		}
	}

	private void validate(MonitoringRules rules)
			throws RuleInstallationException {
		Set<Problem> problems = new HashSet<Problem>();
		List<MonitoringRule> otherRules = new ArrayList<MonitoringRule>(installedRules.values());
		otherRules.addAll(rules.getMonitoringRules());
		MonitoringRule previousRule = null;
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			if (previousRule!=null)
				otherRules.add(previousRule);
			otherRules.remove(rule);
			problems.addAll(validator.validateRule(rule, otherRules));
			previousRule = rule;
		}
		if (!problems.isEmpty()) {
			String message = "Rules could not be installed because of the following problems:\n";
			for (Problem p : problems) {
				message += "Rule "
						+ p.getId()
						+ ", error: "
						+ p.getError()
						+ " at position "
						+ p.getTagName()
						+ (p.getDescription() != null ? ", details: "
								+ p.getDescription() : "") + "\n";
			}
			throw new RuleInstallationException(message);
		}
	}

	public synchronized void uninstallRule(String id) {
		MonitoringRule rule = installedRules.get(id);
		if (rule != null) {
			dcFactoriesManager.uninstallRule(id);
			csparqlEngineManager.uninstallRule(id);
			installedRules.remove(id);
		} else {
			logger.warn("Specified rule does not exist, nothing was uninstalled");
		}
	}

	private void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		try {
			csparqlEngineManager.installRule(rule); // it's better to configure
													// csparql engine first so
													// to prepare the stream
			dcFactoriesManager.installRule(rule);
			installedRules.put(rule.getId(), rule);
			logger.info("Rule {} installed successfully", rule.getId());
		} catch (Exception e) {
			logger.error("Error while installing rule {}, rolling back...",
					rule.getId(), e);
			dcFactoriesManager.uninstallRule(rule.getId());
			csparqlEngineManager.uninstallRule(rule.getId());
			throw new RuleInstallationException(e);
		}
	}

	public Metrics getMetrics() {
		Metrics metrics = new Metrics();
		for (String observableMetric : csparqlEngineManager
				.getObservableMetrics()) {
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
			throws MetricDoesNotExistException, ServerErrorException,
			ObserverErrorException, InternalErrorException {
		String observerId = csparqlEngineManager.addObserver(metricname,
				callbackUrl);
		return observerId;
	}

	public void deleteInstance(String id) throws SerializationException {
		knowledgeBase.deleteEntitiesByPropertyValue(id,
				MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);

	}

	public void uploadModel(Model update) throws SerializationException,
			DeserializationException {

		Set<String> ids = knowledgeBase.getIds(Resource.class,
				MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);

		if (!ids.isEmpty()) {
			knowledgeBase.deleteEntitiesByPropertyValues(ids,
					MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
		}

		updateModel(update);
	}

	public void updateModel(Model update) throws SerializationException,
			DeserializationException {
		knowledgeBase.add(update.getResources(),
				MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
	}

}
