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

import it.polimi.csparqool.Aggregation;
import it.polimi.csparqool.CSquery;
import it.polimi.csparqool.MalformedQueryException;
import it.polimi.csparqool._graph;
import it.polimi.csparqool.graph;
import it.polimi.csparqool.select;
import it.polimi.csparqool.union;
import it.polimi.modaclouds.monitoring.monitoring_rules.ConfigurationException;
import it.polimi.modaclouds.monitoring.monitoring_rules.RuleValidationException;
import it.polimi.modaclouds.monitoring.monitoring_rules.RuleValidator;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import polimi.deib.csparql_rest_api.exception.QueryErrorException;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;
import polimi.deib.csparql_rest_api.exception.StreamErrorException;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class RuleManager {

	private Logger logger = LoggerFactory
			.getLogger(RuleManager.class.getName());
	private DatasetAccessor da = DatasetAccessorFactory.createHTTP(MO
			.getKnowledgeBaseDataURL());
	private URL ddaURL;
	private RSP_services_csparql_API csparqlAPI;

	private Map<String, String> registeredQueries;
	private List<String> registeredStreams;
	private Map<String, MonitoringRule> installedRules;
	private Map<String, List<String>> ruleQueriesMap;
	private RuleValidator validator;

	public RuleManager() throws MalformedURLException, ConfigurationException,
			JAXBException {
		loadConfig();
		registeredStreams = new ArrayList<String>();
		registeredQueries = new HashMap<String, String>();
		installedRules = new HashMap<String, MonitoringRule>();
		ruleQueriesMap = new HashMap<String, List<String>>();
		validator = new RuleValidator();
		csparqlAPI = new RSP_services_csparql_API(ddaURL.toString());
	}

	private void loadConfig() throws MalformedURLException {
		Config config = Config.getInstance();
		String ddaAddress = config.getDDAServerAddress();
		int ddaPort = config.getDDAServerPort();
		ddaAddress = cleanAddress(ddaAddress);
		ddaURL = new URL("http://" + ddaAddress + ":" + ddaPort);
	}

	private String cleanAddress(String address) {
		if (address.indexOf("://") != -1)
			address = address.substring(address.indexOf("://") + 3);
		if (address.endsWith("/"))
			address = address.substring(0, address.length() - 1);
		return address;
	}

	public List<String> installRule(MonitoringRule rule)
			throws RuleInstallationException {
		List<String> queriesIds = new ArrayList<String>();
		try {
			validator.prevalidateRule(rule, installedRules.values());
			String queryId = CSquery.escapeName(rule.getId());
			while (registeredQueries.containsKey(queryId)) {
				queryId = CSquery.generateRandomName();
			}

			CSquery query = CSquery.createDefaultQuery(queryId);
			addPrefixes(query);
			addActions(rule, query);
			String sourceStreamURI = MO.streamsURI + rule.getMetricName();
			query.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
					rule.getTimeStep() + "s");
			query.from(MO.getKnowledgeBaseDataURL() + "?graph=default");
			_graph body = createBody(rule);
			MonitoringMetricAggregation aggregation = rule
					.getMetricAggregation();
			if (aggregation != null && !aggregation.isInherited()) {
				String computation;
				switch (aggregation.getAggregateFunction()) {
				case "Average":
					computation = Aggregation.AVERAGE;
					break;
				default:
					throw new RuleValidationException("Aggregate function "
							+ aggregation.getAggregateFunction()
							+ " does not exist");
				}
				query.where(select
						.add(QueryVars.OUTPUT, QueryVars.INPUT, computation)
						.where(body)
						.groupby(
								"?"
										+ rule.getMetricAggregation()
												.getGroupingCategoryName())
						.having(parse(rule.getCondition())));
			}

			if (!registeredStreams.contains(sourceStreamURI)) {
				logger.info("Registering stream: " + sourceStreamURI);
				String response = csparqlAPI.registerStream(sourceStreamURI);
				logger.info("Server response: " + response);
				registeredStreams.add(sourceStreamURI);
			}

			String CSPARQLquery = query.getCSPARQL();
			String queryURI = csparqlAPI.registerQuery(queryId, CSPARQLquery);
			logger.info("Server response, query ID: " + queryURI);
			registeredQueries.put(queryId, CSPARQLquery);
			installedRules.put(rule.getId(), rule);
			queriesIds.add(queryId);
			ruleQueriesMap.put(rule.getId(), queriesIds);

		} catch (QueryErrorException | MalformedQueryException
				| StreamErrorException e) {
			logger.error("Internal error", e);
			throw new RuleInstallationException("Internal error", e);
		} catch (ServerErrorException e) {
			logger.error("Connection to the DDA server failed", e);
			throw new RuleInstallationException(
					"Connection to the DDA server failed", e);
		} catch (RuleValidationException e) {
			logger.error("Rule is invalid", e);
			throw new RuleInstallationException("Rule is invalid", e);
		} finally {

		}
		return queriesIds;
	}

	private void addActions(MonitoringRule rule, CSquery query) {
		for (Action a : rule.getActions().getActions()) {
			switch (a.getName()) {
			case "NotifyViolation":
				query.construct(graph
						.add(CSquery.BLANK_NODE, MO.hasMetric,
								"mo:" + rule.getMetricName() + "Violation")
						.add(MO.isAbout, QueryVars.TARGET)
						.add(MO.hasValue, QueryVars.OUTPUT));
				break;
			case "EnableMonitoringRule":
				throw new NotImplementedException("Action " + a.getName()
						+ " has not been implemented yet.");
				// break;
			case "DisableMonitoringRule":
				throw new NotImplementedException("Action " + a.getName()
						+ " has not been implemented yet.");
				// break;
			case "SetSamplingProbability":
				throw new NotImplementedException("Action " + a.getName()
						+ " has not been implemented yet.");
				// break;
			case "SetSamplingTime":
				throw new NotImplementedException("Action " + a.getName()
						+ " has not been implemented yet.");
				// break;

			default:
				throw new NotImplementedException("Action " + a.getName()
						+ " has not been implemented yet.");
			}
		}
	}

	private void addPrefixes(CSquery query) {
		query.setNsPrefix("xsd", XSD.getURI()).setNsPrefix("rdf", RDF.getURI())
				.setNsPrefix("rdfs", RDFS.getURI())
				.setNsPrefix("mo", MO.URI);
	}

	private String parse(String condition) {
		return condition.replace("METRIC", QueryVars.OUTPUT);
	}

	private _graph createBody(MonitoringRule rule) {
		_graph body = new _graph();
		List<MonitoredTarget> targets = rule.getMonitoredTargets()
				.getMonitoredTargets();
		if (targets.size() != 1)
			throw new NotImplementedException(
					"Multiple or zero monitored target is not implemented yet");
		String targetClassName = targets.get(0).getId();
		String targetClassURI = MO.URI + targetClassName;
		body.add("?datum", MO.hasMetric, "mo:"+rule.getMetricName())
				.add(MO.isAbout, QueryVars.TARGET)
				.add(MO.hasValue, QueryVars.INPUT);
//				.add(QueryVars.TARGET, RDF.type, targetClassURI);

		String groupingClass = rule.getMetricAggregation()
				.getGroupingCategoryName();

		if (isSubclassOf(targetClassURI, MO.method.toString())) {
			if (groupingClass.equals(Vocabulary.Method)) {
				body.add(QueryVars.TARGET, RDF.type, "?" + groupingClass).addTransitive(
						"?" + groupingClass, RDFS.subClassOf,
						MO.method);
			} else if (groupingClass.equals(Vocabulary.Component)) {
				body.add(union.add(
						graph.add("?" + groupingClass,
								MO.provides, QueryVars.TARGET))
						.add(graph.add("?" + groupingClass,
								MO.provides,
								QueryVars.COMPONENT).addTransitive(
								QueryVars.COMPONENT,
								MO.requires, QueryVars.TARGET)));
			} else if (groupingClass.equals(Vocabulary.CloudProvider)) {
				body.add(QueryVars.EXTERNAL_COMPONENT, MO.hasProvider, "?" + groupingClass)
								.addTransitive(QueryVars.COMPONENT, MO.requires, QueryVars.EXTERNAL_COMPONENT)
								.add(MO.provides, QueryVars.TARGET);
			} else {
				throw new NotImplementedException("Grouping class " + groupingClass + " has not been implemented yet");
			}
		} else {
			throw new NotImplementedException("Cannot install rules with target " + targetClassName + " yet");
		}
		return body;
	}

	public List<String> installRules(MonitoringRules rules)
			throws RuleValidationException, RuleInstallationException {

		List<String> queriesIds = new ArrayList<String>();

		for (MonitoringRule rule : rules.getMonitoringRules()) {
			queriesIds.addAll(installRule(rule));
		}
		return queriesIds;
	}

	public String getQuery(String queryId) {
		return registeredQueries.get(queryId);
	}

	private boolean isSubclassOf(String resourceURI, String superClassURI) {
		String queryString = "ASK " + "FROM <"
				+ MO.getKnowledgeBaseDataURL() + "?graph=default>"
				+ "WHERE { <" + resourceURI + "> <" + RDFS.subClassOf + "> <"
				+ superClassURI + "> . }";

		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.create(query);

		return qexec.execAsk();
	}

}
