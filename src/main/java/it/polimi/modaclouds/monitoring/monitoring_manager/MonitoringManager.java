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
import it.polimi.modaclouds.qos_models.util.Model;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Component;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Location;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MOVocabulary;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;
import it.polimi.modaclouds.qos_models.monitoring_rules.Problem;
import it.polimi.modaclouds.qos_models.monitoring_rules.Validator;
import it.polimi.modaclouds.qos_models.schema.Metric;
import it.polimi.modaclouds.qos_models.schema.Metrics;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.PaasService;
import it.polimi.modaclouds.qos_models.schema.Provider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.web.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;

import polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;

public class MonitoringManager {

	public static final String MODEL_GRAPH_NAME = "model";

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private CSPARQLEngineManager csparqlEngineManager;
	private DCFactoriesManager dcFactoriesManager;
	private SDAFactoryManager sdaFactoryManager;
	private Map<String, MonitoringRule> installedRules;
	private List<MonitoringRule> installingRules;

	private Validator validator;

	private Config config;

	private FusekiKBAPI knowledgeBase;

	public MonitoringManager(Config config) throws Exception {
		this.config = config;
		validator = new Validator();
		knowledgeBase = new FusekiKBAPI(config.getKbUrl());
		installedRules = new ConcurrentHashMap<String, MonitoringRule>();
		csparqlEngineManager = new CSPARQLEngineManager(this, config,
				knowledgeBase);
		dcFactoriesManager = new DCFactoriesManager(knowledgeBase);
		sdaFactoryManager = new SDAFactoryManager(knowledgeBase);

		boolean isKBActive = false;
		while(!isKBActive){
			isKBActive = uploadingOntology(MO.model, MODEL_GRAPH_NAME);
		}
		
	}

	private boolean uploadingOntology(OntModel model, String modelGraphName) {
		try{
			logger.info("Uploading ontology to KB");
			knowledgeBase.uploadOntology(MO.model, MODEL_GRAPH_NAME);
			return true;
			}catch(HttpException e){
				logger.info("Knowledge base not found, please start the knowledge base");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return false;
			}
	}

	// public void newInstance(Component instance) {
	// systemDomainKB.add(instance);
	// }

	public synchronized void installRules(MonitoringRules rules)
			throws RuleInstallationException {
		validate(rules);
		installingRules = rules.getMonitoringRules();
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			installRule(rule);
		}
		installingRules = null;
	}

	private void validate(MonitoringRules rules)
			throws RuleInstallationException {
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			if (installedRules.containsKey(rule.getId()))
				throw new RuleInstallationException("A rule with id "
						+ rule.getId() + " is already installed");
		}
		MonitoringRules allrules = new MonitoringRules();
		allrules.getMonitoringRules().addAll(installedRules.values());
		allrules.getMonitoringRules().addAll(rules.getMonitoringRules());
		Set<Problem> problems = validator.validateAllRules(allrules);
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

	public synchronized void uninstallRule(String id)
			throws RuleDoesNotExistException, FailedToUninstallRuleException {
		MonitoringRule rule = installedRules.get(id);
		if (rule == null)
			throw new RuleDoesNotExistException();
		csparqlEngineManager.uninstallRule(rule);
		dcFactoriesManager.uninstallRule(rule);
		sdaFactoryManager.uninstallRule(rule);
		installedRules.remove(id);

	}

	public synchronized void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		if (installedRules.containsKey(rule.getId()))
			throw new RuleInstallationException("A rule with id "
					+ rule.getId() + " is already installed");
		String requiredDataAnalyzer = null;
		String sdaReturnedMetric = null;
		String aggregateFunction = null;
		String groupingClass = null;
		if (rule.getMetricAggregation() != null) {
			MonitoringRule pRule = rule;
//			while (pRule.getMetricAggregation() != null
//					&& rule.getMetricAggregation().isInherited()) {
//				pRule = getParentRule(rule.getParentMonitoringRuleId());
//			}
			aggregateFunction = pRule.getMetricAggregation()
					.getAggregateFunction();
			groupingClass = pRule.getMetricAggregation().getGroupingClass();
		}
		if (aggregateFunction != null) {
			requiredDataAnalyzer = validator
					.getRequiredDataAnalyzer(aggregateFunction);
		}
		if (Util.softEquals(requiredDataAnalyzer, MMVocabulary.MATLAB_SDA)
				|| Util.softEquals(requiredDataAnalyzer, MMVocabulary.JAVA_SDA)) {
			sdaReturnedMetric = generateRandomMetricName();
		}
		try {
			csparqlEngineManager.installRule(rule, requiredDataAnalyzer,
					sdaReturnedMetric);
			dcFactoriesManager.installRule(rule);
			if (requiredDataAnalyzer.equals(MMVocabulary.MATLAB_SDA)
					|| requiredDataAnalyzer.equals(MMVocabulary.JAVA_SDA)) {
				sdaFactoryManager.installRule(rule, aggregateFunction,
						sdaReturnedMetric);
			}
			installedRules.put(rule.getId(), rule);
		} catch (Exception e) {
			// TODO rollback
			logger.error("Error while installing rule", e);
			throw new RuleInstallationException(e);
		}
	}

protected MonitoringRule getParentRule(String parentMonitoringRuleId) {
	MonitoringRule parent = installedRules.get(parentMonitoringRuleId);
	if (parent == null && installingRules != null) {
		for (MonitoringRule rule : installingRules) {
			if (rule.getId().equals(parentMonitoringRuleId))
				return rule;
		}
	}
	return null;
}

private String generateRandomMetricName() {
	return escape(UUID.randomUUID().toString());
}

private String escape(String string) {
	return string.replaceAll("[^a-zA-Z0-9]", "");
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

	public void deleteResource(String id) throws SerializationException, DeserializationException, ResourceDoesNotExistException {
		
		Object resource = knowledgeBase.getEntityById(id, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
		
		if (resource == null){
			throw new ResourceDoesNotExistException();
		}
		
		if (resource instanceof VM){
			Set<String> internalComponents = knowledgeBase.getIds(InternalComponent.class,
					MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);	
			
			for(String idInternal : internalComponents){
				InternalComponent internalComponent =(InternalComponent) knowledgeBase.getEntityById(idInternal, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
				Set<String> requiredComponents = internalComponent.getRequiredComponents();
				for(String reqComp : requiredComponents){
					if(reqComp.equals(id)){
						deleteMethodByInternalComponent(idInternal);
						deleteByPropertyValue(knowledgeBase, idInternal, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
						break;
					}
				}
			}		
		} 
		
		if (resource instanceof InternalComponent){				
		deleteMethodByInternalComponent(id);
}		
	deleteByPropertyValue(knowledgeBase, id, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
}

	private void deleteMethodByInternalComponent(String internalComp) throws DeserializationException, SerializationException{
		Set<String> methods = knowledgeBase.getIds(Method.class, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
		for(String idMethod : methods){
			Method methodToRemove = (Method) knowledgeBase.getEntityById(idMethod, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
			String providedBy = methodToRemove.getProvidedBy();
		if(providedBy.equals(internalComp)){
			deleteByPropertyValue(knowledgeBase, idMethod, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
		}
	}
	}
	
private void deleteByPropertyValue(FusekiKBAPI fusekiKnowledgeBase, String id, String vocabulary, String graphName) throws SerializationException{
	logger.info("deleting "+id);
	fusekiKnowledgeBase.deleteEntitiesByPropertyValue(id, vocabulary, graphName);
}

public void uploadModel(Model update) throws SerializationException,
		DeserializationException {	
	
	Set<String> ids = new HashSet();

	//retrieving all the resources stored in the model
	ids.addAll(knowledgeBase.getIds(Location.class,
			MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME));
	ids.addAll(knowledgeBase.getIds(VM.class,
			MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME));
	ids.addAll(knowledgeBase.getIds(PaasService.class,
			MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME));
	ids.addAll(knowledgeBase.getIds(InternalComponent.class,
			MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME));
	ids.addAll(knowledgeBase.getIds(Method.class,
			MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME));
	ids.addAll(knowledgeBase.getIds(Provider.class,
			MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME));
	
	if (!ids.isEmpty()) {
		knowledgeBase.deleteEntitiesByPropertyValues(ids,
				MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
	}
	
	addResources(update);
}

public void addResources(Model update) throws SerializationException,
		DeserializationException {
	logger.info("adding resources");
		knowledgeBase.add(update.getResources(),
				MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);
	}

	public Resource getResource(String id) throws ResourceDoesNotExistException, DeserializationException {
		
		Object resource = knowledgeBase.getEntityById(id, MOVocabulary.resourceIdParameterName, MODEL_GRAPH_NAME);

		if (resource == null)
			throw new ResourceDoesNotExistException();
		
		return (Resource) resource;
	}

}
