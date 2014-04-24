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
import it.polimi.csparqool._select;
import it.polimi.csparqool.graph;
import it.polimi.csparqool.union;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.Parameter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import polimi.deib.csparql_rest_api.exception.ObserverErrorException;
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

public class CSPARQLEngineManager {

	private Logger logger = LoggerFactory.getLogger(CSPARQLEngineManager.class
			.getName());
	private DatasetAccessor da = DatasetAccessorFactory.createHTTP(MO
			.getKnowledgeBaseDataURL());
	private URL ddaURL;
	private URL sdaURL;
	private RSP_services_csparql_API csparqlAPI;

	private Map<String, String> registeredQueries;
	private List<String> registeredStreams;
	private Map<String, List<String>> ruleQueriesMap;

	// private RuleValidator validator;

	public CSPARQLEngineManager() throws ConfigurationException {
		try {
			loadConfig();
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
		registeredStreams = new ArrayList<String>();
		registeredQueries = new HashMap<String, String>();
		ruleQueriesMap = new HashMap<String, List<String>>();
		// validator = new RuleValidator();
		csparqlAPI = new RSP_services_csparql_API(ddaURL.toString());
	}

	private void loadConfig() throws MalformedURLException,
			ConfigurationException {
		Config config = Config.getInstance();
		String ddaAddress = config.getDDAServerAddress();
		int ddaPort = config.getDDAServerPort();
		ddaAddress = cleanAddress(ddaAddress);
		ddaURL = new URL("http://" + ddaAddress + ":" + ddaPort);
		String sdaAddress = config.getSDAServerAddress();
		int sdaPort = config.getSDAServerPort();
		sdaAddress = cleanAddress(sdaAddress);
		sdaURL = new URL("http://" + sdaAddress + ":" + sdaPort);
	}

	private String cleanAddress(String address) {
		if (address.indexOf("://") != -1)
			address = address.substring(address.indexOf("://") + 3);
		if (address.endsWith("/"))
			address = address.substring(0, address.length() - 1);
		return address;
	}

	public void installRule(MonitoringRule rule, String aggregateFunction,
			String groupingCategory, boolean sdaRequired, String sdaReturnedMetric)
			throws RuleInstallationException {
		try {
			List<String> queriesURIs = new ArrayList<String>();
			// validator.prevalidateRule(rule, installedRules.values());

			if (sdaRequired) {
				String outQueryName = CSquery.escapeName(rule.getId()) + "OUT";
				while (registeredQueries.containsKey(outQueryName)) {
					outQueryName = CSquery.generateRandomName() + "OUT";
				}

				// OUT QUERY
				CSquery outQuery = CSquery.createDefaultQuery(outQueryName);
				_select outTopSelect = new _select();
				addPrefixes(outQuery);
				outQuery.select(QueryVars.TARGET);
				outQuery.select(QueryVars.OUTPUT);
				outQuery.select(QueryVars.TIMESTAMP);
				outTopSelect.add(QueryVars.TARGET);
				outTopSelect.add(QueryVars.OUTPUT);
				outTopSelect.add(QueryVars.TIMESTAMP, new String[] {
						QueryVars.DATUM, MO.shortForm(MO.aboutResource),
						QueryVars.TARGET }, Aggregation.TIMESTAMP);
				String outSourceStreamURI = getSourceStreamURI(rule.getMetricName());
				outQuery.fromStream(outSourceStreamURI,
						rule.getTimeWindow() + "s", rule.getTimeStep() + "s");
				outQuery.from(MO.getKnowledgeBaseDataURL() + "?graph=default");
				_graph outBody = createBody(rule, QueryVars.OUTPUT);
				outQuery.where(outTopSelect.where(outBody));

				// IN QUERY
				String inQueryName = CSquery.escapeName(rule.getId()) + "IN";
				while (registeredQueries.containsKey(inQueryName)) {
					inQueryName = CSquery.generateRandomName() + "IN";
				}
				CSquery inQuery = CSquery.createDefaultQuery(inQueryName);
				_select inTopSelect = new _select();
				addPrefixes(inQuery);
				addActions(rule, inQuery);
				String inSourceStreamURI = getSourceStreamURI(sdaReturnedMetric);
				inQuery.fromStream(inSourceStreamURI, rule.getTimeWindow() + "s",
						rule.getTimeStep() + "s");
				inQuery.from(MO.getKnowledgeBaseDataURL() + "?graph=default");
				_graph inBody = createBody(rule, QueryVars.OUTPUT);
				inQuery.where(inTopSelect.where(inBody));
				if (rule.getCondition() != null) {
					inTopSelect.having(parseCondition(rule.getCondition()));
				}

				
				if (!registeredStreams.contains(outSourceStreamURI)) {
					logger.info("Registering stream: " + outSourceStreamURI);
					String response = csparqlAPI
							.registerStream(outSourceStreamURI);
					logger.info("Server response: " + response);
					registeredStreams.add(outSourceStreamURI);
				}
				
				if (!registeredStreams.contains(inSourceStreamURI)) {
					logger.info("Registering stream: " + inSourceStreamURI);
					String response = csparqlAPI
							.registerStream(inSourceStreamURI);
					logger.info("Server response: " + response);
					registeredStreams.add(inSourceStreamURI);
				}

				String CSPARQLOutQuery = outQuery.getCSPARQL();
				String outQueryURI = csparqlAPI.registerQuery(outQueryName,
						CSPARQLOutQuery);
				logger.info("Server response, query ID: " + outQueryURI);
				registeredQueries.put(outQueryName, CSPARQLOutQuery);
				queriesURIs.add(outQueryURI);
				
				String CSPARQLInQuery = inQuery.getCSPARQL();
				String inQueryURI = csparqlAPI.registerQuery(inQueryName,
						CSPARQLInQuery);
				logger.info("Server response, query ID: " + inQueryURI);
				registeredQueries.put(inQueryName, CSPARQLInQuery);
				queriesURIs.add(inQueryURI);
				
				//attaching sda as observer
				csparqlAPI.addObserver(outQueryURI, sdaURL.toString());				

			} else {
				String queryName = CSquery.escapeName(rule.getId());
				while (registeredQueries.containsKey(queryName)) {
					queryName = CSquery.generateRandomName();
				}

				CSquery query = CSquery.createDefaultQuery(queryName);
				_select topSelect = new _select();
				addPrefixes(query);
				addActions(rule, query);

				String sourceStreamURI = getSourceStreamURI(rule.getMetricName());
				query.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
						rule.getTimeStep() + "s");
				query.from(MO.getKnowledgeBaseDataURL() + "?graph=default");
				_graph body;

				
				if (aggregateFunction != null) {
					String computation;
					switch (aggregateFunction) {
					case DDAAggregateFunction.Average:
						computation = Aggregation.AVERAGE;
						break;
					default:
						throw new InternalErrorException("Aggregate function "
								+ aggregateFunction
								+ " is not a valid CSPARQL function");
					}
					topSelect.add(QueryVars.OUTPUT,
							new String[] { QueryVars.INPUT }, computation);
					if (groupingCategory != null) {
						topSelect.groupby("?" + groupingCategory);
					}
					body = createBody(rule, QueryVars.INPUT);
				} else {
					body = createBody(rule, QueryVars.OUTPUT);
				}
				
				query.where(topSelect.where(body));

				if (rule.getCondition() != null) {
					topSelect.having(parseCondition(rule.getCondition()));
				}

				if (!registeredStreams.contains(sourceStreamURI)) {
					logger.info("Registering stream: " + sourceStreamURI);
					String response = csparqlAPI
							.registerStream(sourceStreamURI);
					logger.info("Server response: " + response);
					registeredStreams.add(sourceStreamURI);
				}

				String CSPARQLquery = query.getCSPARQL();
				String queryURI = csparqlAPI.registerQuery(queryName,
						CSPARQLquery);
				logger.info("Server response, query ID: " + queryURI);
				registeredQueries.put(queryName, CSPARQLquery);
				queriesURIs.add(queryURI);
			}
			ruleQueriesMap.put(rule.getId(), queriesURIs);
		} catch (QueryErrorException | MalformedQueryException
				| InternalErrorException e) {
			logger.error("Internal error", e);
			throw new RuleInstallationException("Internal error", e);
		} catch (ServerErrorException | StreamErrorException e) {
			logger.error("Connection to the DDA server failed", e);
			throw new RuleInstallationException(
					"Connection to the DDA server failed", e);
		} catch (ObserverErrorException e){
			logger.error("Connection to the SDA server failed", e);
			throw new RuleInstallationException(
					"Connection to the SDA server failed", e);
		}
	}

	private String getSourceStreamURI(String metric) {
		return MO.streamsURI + metric;
	}

	private void addActions(MonitoringRule rule, CSquery query) {
		for (Action a : rule.getActions().getActions()) {
			switch (a.getName()) {
			case "OutputMetric":
				query.construct(graph
						.add(CSquery.BLANK_NODE, MO.metric,
								"mo:" + getParValue(a.getParameters(), "name"))
						.add(MO.aboutResource, QueryVars.TARGET)
						.add(MO.value, QueryVars.OUTPUT));
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

	private Object getParValue(List<Parameter> parameters, String key) {
		for (Parameter p : parameters) {
			if (p.getName().equals(key))
				return p.getValue();
		}
		return null;
	}

	private void addPrefixes(CSquery query) {
		query.setNsPrefix("xsd", XSD.getURI())
				.setNsPrefix("rdf", RDF.getURI())
				.setNsPrefix("rdfs", RDFS.getURI())
				.setNsPrefix("mo", MO.URI)
				.setNsPrefix(CSquery.getFunctionsPrefix(),
						CSquery.getFunctionsURI());
	}

	private String parseCondition(String condition) {
		return condition.replace("METRIC", QueryVars.OUTPUT);
	}

	private _graph createBody(MonitoringRule rule, String inputVar) {
		_graph body = new _graph();
		List<MonitoredTarget> targets = rule.getMonitoredTargets()
				.getMonitoredTargets();
		if (targets.size() != 1)
			throw new NotImplementedException(
					"Multiple or zero monitored target is not implemented yet");
		String targetClassName = targets.get(0).getKlass();
		String targetClassURI = MO.URI + targetClassName;
		body.add(QueryVars.DATUM, MO.metric, "mo:" + rule.getMetricName())
				.add(MO.aboutResource, QueryVars.TARGET)
				.add(MO.value, inputVar);
		// .add(QueryVars.TARGET, RDF.type, targetClassURI);

		String groupingClass = rule.getMetricAggregation()
				.getGroupingCategoryName();

		if (isSubclassOf(targetClassURI, MO.Method.toString())) {
			if (groupingClass.equals(Vocabulary.Method)) {
				body.add(QueryVars.TARGET, RDF.type, "?" + groupingClass)
						.addTransitive("?" + groupingClass, RDFS.subClassOf,
								MO.Method);
			} else if (groupingClass.equals(Vocabulary.Component)) {
				body.add(union.add(
						graph.add("?" + groupingClass, MO.providedMethod,
								QueryVars.TARGET)).add(
						graph.add("?" + groupingClass, MO.providedMethod,
								QueryVars.COMPONENT).addTransitive(
								QueryVars.COMPONENT, MO.requiredComponent,
								QueryVars.TARGET)));
			} else if (groupingClass.equals(Vocabulary.CloudProvider)) {
				body.add(QueryVars.EXTERNAL_COMPONENT, MO.cloudProvider,
						"?" + groupingClass)
						.addTransitive(QueryVars.COMPONENT,
								MO.requiredComponent,
								QueryVars.EXTERNAL_COMPONENT)
						.add(MO.providedMethod, QueryVars.TARGET);
			} else {
				throw new NotImplementedException("Grouping class "
						+ groupingClass + " has not been implemented yet");
			}
		} else {
			throw new NotImplementedException(
					"Cannot install rules with target " + targetClassName
							+ " yet");
		}
		return body;
	}

	public String getQuery(String queryId) {
		return registeredQueries.get(queryId);
	}

	private boolean isSubclassOf(String resourceURI, String superClassURI) {
		String queryString = "ASK " + "FROM <" + MO.getKnowledgeBaseDataURL()
				+ "?graph=default>" + "WHERE { <" + resourceURI + "> <"
				+ RDFS.subClassOf + "> <" + superClassURI + "> . }";

		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.create(query);

		return qexec.execAsk();
	}

}
