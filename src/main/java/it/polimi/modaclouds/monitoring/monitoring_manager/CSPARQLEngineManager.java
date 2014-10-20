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

import it.polimi.csparqool.CSquery;
import it.polimi.csparqool.Function;
import it.polimi.csparqool.MalformedQueryException;
import it.polimi.csparqool._body;
import it.polimi.csparqool._graph;
import it.polimi.csparqool._union;
import it.polimi.csparqool.graph;
import it.polimi.modaclouds.monitoring.dcfactory.wrappers.DDAOntology;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MOVocabulary;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import polimi.deib.csparql_rest_api.exception.QueryErrorException;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;

import com.google.common.base.Strings;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class CSPARQLEngineManager {

	private Logger logger = LoggerFactory.getLogger(CSPARQLEngineManager.class
			.getName());
	private URL ddaURL;
	private RSP_services_csparql_API csparqlAPI;

	private Map<String, String> registeredQueriesById;
	private Map<String, String> registeredStreamsByRuleId;
	private Map<String, String> registeredQueriesByRuleId;
	private Map<String, String> queryURIByMetric;
	private Map<String, String> metricByObserverId;
	private String kbURL;
	private Map<String, MonitoringRule> installedRules;

	public CSPARQLEngineManager(Config config, FusekiKBAPI kb)
			throws MalformedURLException {
		this.kbURL = config.getKbUrl();
		ddaURL = createURL(config.getDdaIP(), config.getDdaPort());
		registeredStreamsByRuleId = new ConcurrentHashMap<String, String>();
		registeredQueriesById = new ConcurrentHashMap<String, String>();
		registeredQueriesByRuleId = new ConcurrentHashMap<String, String>();
		queryURIByMetric = new ConcurrentHashMap<String, String>();
		metricByObserverId = new ConcurrentHashMap<String, String>();
		csparqlAPI = new RSP_services_csparql_API(ddaURL.toString());
		installedRules = new ConcurrentHashMap<String, MonitoringRule>();
	}

	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		try {
			String queryName = getNewQueryName(rule, null);
			CSquery query = createQueryFromRule(rule, queryName, kbURL);
			String csparqlQuery = query.getCSPARQL();
			registerInputStream(rule);
			String queryURI = registerQuery(queryName, csparqlQuery, rule);
			addObservableMetrics(rule, queryURI);
			installedRules.put(rule.getId(),rule);
		} catch (QueryErrorException | MalformedQueryException e) {
			logger.error("Internal error", e);
			throw new RuleInstallationException("Internal error", e);
		} catch (ServerErrorException e) {
			logger.error("Connection to the DDA server failed", e);
			throw new RuleInstallationException(
					"Connection to the DDA server failed", e);
		}
	}

	private static String[] addActions(MonitoringRule rule, CSquery query)
			throws RuleInstallationException {
		String[] requiredVars = null;
		if (rule.getActions() == null)
			return requiredVars;

		for (Action action : rule.getActions().getActions()) {
			switch (action.getName()) {
			case MMVocabulary.OutputMetric:
				String outputResourceIdVariable = Util
						.getOutputResourceIdVariable(rule);
				String outputValueVariable = Util.getOutputValueVariable(rule);
				String outputTimestampVariable = Util
						.getOutputTimestampVariable(rule);
				if (outputResourceIdVariable != null) {
					requiredVars = new String[] { outputResourceIdVariable,
							outputValueVariable, outputTimestampVariable };
				} else {
					requiredVars = new String[] { outputValueVariable,
							outputTimestampVariable };
				}
				query.construct(graph
						.add(CSquery.BLANK_NODE,
								DDAOntology.metric,
								"\""
										+ Util.getParameterValue(
												MOVocabulary.metric, action)
										+ "\"")
						.add(DDAOntology.resourceId,
								(Util.getParameterValue(
										MOVocabulary.resourceId, action)
										.equals("ID") ? outputResourceIdVariable
										: "\""
												+ Util.getParameterValue(
														MOVocabulary.resourceId,
														action) + "\""))
						.add(DDAOntology.value, outputValueVariable)
						.add(DDAOntology.timestamp, outputTimestampVariable));
				break;
			// case MMVocabulary.EnableMonitoringRule:
			// throw new NotImplementedException("Action " + action.getName()
			// + " has not been implemented yet.");
			// // break;
			// case MMVocabulary.DisableMonitoringRule:
			// throw new NotImplementedException("Action " + action.getName()
			// + " has not been implemented yet.");
			// // break;

			default:
				throw new NotImplementedException("Action " + action.getName()
						+ " has not been implemented yet.");
			}
		}

		return requiredVars;
	}

	private void addObservableMetrics(MonitoringRule rule, String queryURI) {
		// TODO are we checking if metric already exist?
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(MMVocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(MOVocabulary.metric,
						action);
				queryURIByMetric.put(metric.toLowerCase(), queryURI);
			}
		}
	}

	public String addObserver(String metricname, String callbackUrl)
			throws MetricDoesNotExistException, ServerErrorException,
			ObserverErrorException, InternalErrorException {
		String queryURI = queryURIByMetric.get(metricname.toLowerCase());
		if (queryURI == null)
			throw new MetricDoesNotExistException();
		// TODO gonna be fixed?
		String queryName = queryURI.substring(queryURI.lastIndexOf('/') + 1);
		String realQueryUri = ddaURL.toString() + "/queries/" + queryName;
		csparqlAPI.addObserver(realQueryUri, callbackUrl);
		String observerId = String.valueOf(queryURI.hashCode());
		metricByObserverId.put(observerId, metricname);
		return observerId;
	}

	private static void addPrefixes(CSquery query) {
		query.setNsPrefix("xsd", XSD.getURI())
				.setNsPrefix("rdf", RDF.getURI())
				.setNsPrefix("rdfs", RDFS.getURI())
				.setNsPrefix(MO.prefix, MO.URI)
				.setNsPrefix(DDAOntology.prefix, DDAOntology.URI)
				.setNsPrefix(CSquery.getFunctionsPrefix(),
						CSquery.getFunctionsURI());
	}

	private static void addSelect(_body queryBody, String[] variables,
			MonitoringRule rule) throws MalformedQueryException,
			RuleInstallationException {
		String aggregateFunction = Util.getAggregateFunction(rule);
		for (String var : variables) {
			switch (var) {
			case QueryVars.TIMESTAMP:
				queryBody.selectFunction(QueryVars.TIMESTAMP, Function.MAX,
						QueryVars.INPUT_TIMESTAMP);
				break;
			case QueryVars.INPUT_TIMESTAMP:
				queryBody.selectFunction(QueryVars.INPUT_TIMESTAMP,
						Function.TIMESTAMP, QueryVars.DATUM,
						DDAOntology.shortForm(DDAOntology.resourceId),
						QueryVars.RESOURCE_ID);
				break;
			case QueryVars.OUTPUT:
				if (Util.isAggregatedMetric(rule)) {
					String[] parameters = Util.getAggregateFunctionArgs(rule);
					queryBody.selectFunction(QueryVars.OUTPUT,
							aggregateFunction, parameters);
				} else {
					queryBody.select(QueryVars.OUTPUT);
				}
				break;
			default:
				queryBody.select(var);
				break;
			}
		}
	}

	private String cleanAddress(String address) {
		if (address.indexOf("://") != -1)
			address = address.substring(address.indexOf("://") + 3);
		if (address.endsWith("/"))
			address = address.substring(0, address.length() - 1);
		return address;
	}

	public static CSquery createQueryFromRule(MonitoringRule rule,
			String queryName, String kbURL) throws MalformedQueryException,
			RuleInstallationException {
		CSquery query = CSquery.createDefaultQuery(queryName);
		addPrefixes(query);

		String[] outputRequiredVars = addActions(rule, query);

		_body mainQueryBody = new _body();
		addSelect(mainQueryBody, outputRequiredVars, rule);

		if (Util.isAggregatedMetric(rule)) {
			if (Util.isGroupedMetric(rule))
				mainQueryBody.groupby(Util.getGroupingClassIdVariable(rule));
			_body innerQueryBody = new _body();
			addSelect(innerQueryBody,
					getInnerQueryRequiredVars(outputRequiredVars), rule);
			mainQueryBody.where(innerQueryBody.where(createGraphPattern(rule)));
		} else {
			mainQueryBody.where(createGraphPattern(rule));
		}
		if (rule.getCondition() != null) {
			mainQueryBody.having(parseCondition(rule.getCondition().getValue(),
					Util.getOutputValueVariable(rule)));
		}

		query.fromStream(getSourceStreamURI(rule), rule.getTimeWindow() + "s",
				rule.getTimeStep() + "s")
				.from(FusekiKBAPI.getGraphURL(kbURL,
						MonitoringManager.MODEL_GRAPH_NAME))
				.where(mainQueryBody);
		return query;
	}

	private static _graph createGraphPattern(MonitoringRule rule)
			throws RuleInstallationException {
		_graph graphPattern = new _graph();
		List<MonitoredTarget> targets = Util.getMonitoredTargets(rule);
		String groupingClass = Util.getGroupingClass(rule);

		graphPattern
				.add(QueryVars.DATUM, DDAOntology.resourceId,
						QueryVars.RESOURCE_ID)
				.add(DDAOntology.value, QueryVars.INPUT)
				.add(QueryVars.RESOURCE, MO.id, QueryVars.RESOURCE_ID);

		_union unionOfTargets = new _union();
		graphPattern.add(unionOfTargets);
		for (MonitoredTarget target : targets) {
			if (!Strings.isNullOrEmpty(target.getType()))
				unionOfTargets.add(graph.add(QueryVars.RESOURCE, MO.type,
						getTargetIDLiteral(target)));
		}

		switch (targets.get(0).getClazz()) {
		case MOVocabulary.VM:
			graphPattern.add(QueryVars.RESOURCE, RDF.type, MO.VM);
			if (groupingClass != null) {
				switch (groupingClass) {
				case MOVocabulary.VM:
					break;
				case MOVocabulary.CloudProvider:
					graphPattern.add(QueryVars.RESOURCE, MO.cloudProvider,
							Util.getGroupingClassIdVariable(rule)).add(
							Util.getGroupingClassVariable(rule), MO.id,
							Util.getGroupingClassIdVariable(rule));
					break;
				default:
					throw new NotImplementedException("Grouping class "
							+ groupingClass + " for target "
							+ targets.get(0).getClazz()
							+ " has not been implemented yet");
				}
				break;
			}
			break;
		case MOVocabulary.InternalComponent:
			graphPattern
					.add(QueryVars.RESOURCE, RDF.type, MO.InternalComponent);
			if (groupingClass != null) {
				switch (groupingClass) {
				case MOVocabulary.InternalComponent:
					break;
				case MOVocabulary.CloudProvider:
					graphPattern
							.addTransitive(QueryVars.RESOURCE,
									MO.requiredComponents, QueryVars.COMPONENT)
							.add(QueryVars.COMPONENT, MO.cloudProvider,
									Util.getGroupingClassIdVariable(rule))
							.add(Util.getGroupingClassVariable(rule), MO.id,
									Util.getGroupingClassIdVariable(rule));
					break;
				default:
					throw new NotImplementedException("Grouping class "
							+ groupingClass + " for target "
							+ targets.get(0).getClazz()
							+ " has not been implemented yet");
				}
				break;
			}
			break;
		case MOVocabulary.Method:
			graphPattern.add(QueryVars.RESOURCE, RDF.type, MO.Method);
			if (groupingClass != null) {
				switch (groupingClass) {
				case MOVocabulary.Method:
					break;
				case MOVocabulary.CloudProvider:
					graphPattern
							.add(QueryVars.INTERNAL_COMPONENT,
									MO.providedMethods, QueryVars.RESOURCE)
							.addTransitive(QueryVars.INTERNAL_COMPONENT,
									MO.requiredComponents, QueryVars.COMPONENT)
							.add(QueryVars.COMPONENT, MO.cloudProvider,
									Util.getGroupingClassIdVariable(rule))
							.add(Util.getGroupingClassVariable(rule), MO.id,
									Util.getGroupingClassIdVariable(rule));
					break;
				default:
					throw new NotImplementedException("Grouping class "
							+ groupingClass + " for target "
							+ targets.get(0).getClazz()
							+ " has not been implemented yet");
				}
				break;
			}
			break;
		default:
			throw new NotImplementedException(
					"Cannot install rules with target class "
							+ targets.get(0).getClazz() + " yet");
		}
		return graphPattern;
	}

	private URL createURL(String address, String port)
			throws MalformedURLException {
		return new URL("http://" + cleanAddress(address) + ":" + port);
	}

	private void deleteObservableMetrics(MonitoringRule rule) {
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(MMVocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(MOVocabulary.metric,
						action);
				queryURIByMetric.remove(metric.toLowerCase());
			}
		}
	}

	private static String[] getInnerQueryRequiredVars(
			String[] mainQueryRequiredVars) {
		String[] innerQueryRequiredVars = new String[mainQueryRequiredVars.length];
		for (int i = 0; i < mainQueryRequiredVars.length; i++) {
			switch (mainQueryRequiredVars[i]) {
			case QueryVars.OUTPUT:
				innerQueryRequiredVars[i] = QueryVars.INPUT;
				break;
			case QueryVars.TIMESTAMP:
				innerQueryRequiredVars[i] = QueryVars.INPUT_TIMESTAMP;
				break;

			default:
				innerQueryRequiredVars[i] = mainQueryRequiredVars[i];
				break;
			}
		}
		return innerQueryRequiredVars;
	}

	private String getNewQueryName(MonitoringRule rule, String suffix) {
		if (suffix == null)
			suffix = "";
		String queryName = CSquery.escapeName(rule.getId()) + suffix;
		while (registeredQueriesById.containsKey(queryName)) {
			queryName = CSquery.generateRandomName() + suffix;
		}
		return queryName;
	}

	public Set<String> getObservableMetrics() {
		return queryURIByMetric.keySet();
	}

	public String getQuery(String queryId) {
		return registeredQueriesById.get(queryId);
	}

	private static String getSourceStreamURI(MonitoringRule rule) {
		return "http://www.modaclouds.eu/streams/"
				+ rule.getCollectedMetric().getMetricName().toLowerCase();
	}

	private static String getTargetIDLiteral(MonitoredTarget monitoredTarget) {
		return "\"" + monitoredTarget.getType() + "\"";
	}

	private static String parseCondition(String condition,
			String outputValueVariable) {
		return condition != null ? condition.replace("METRIC",
				outputValueVariable) : null;
	}

	private String registerQuery(String queryName, String csparqlQuery,
			MonitoringRule rule) throws ServerErrorException,
			QueryErrorException {
		logger.info("Registering query: \n{}", csparqlQuery);
		String queryURI = csparqlAPI.registerQuery(queryName, csparqlQuery);
		logger.info("Server response, query ID {}", queryURI);

		// fix:
		// queryURI = ddaURL.toString() + "/queries/" + queryName;
		queryURI = "http://www.modaclouds.eu/queries/" + queryName;
		logger.info("actual query ID (temp fix):" + queryURI);

		registeredQueriesById.put(queryURI, csparqlQuery);
		registeredQueriesByRuleId.put(rule.getId(), queryURI);
		return queryURI;
	}

	private void registerInputStream(MonitoringRule rule)
			throws RuleInstallationException {
		String streamURI = getSourceStreamURI(rule);
		logger.info("Registering stream {}", streamURI);
		if (!registeredStreamsByRuleId.containsValue(streamURI)) {
			String response;
			boolean registered = false;
			try {
				response = csparqlAPI.registerStream(streamURI);
				logger.info("Server response: {}", response);
				registered = true;
			} catch (Exception e) {
				if (e.getMessage().contains("already exists")) {
					registered = true;
					logger.info("Stream already exists");
				}
			}
			if (!registered) {
				throw new RuleInstallationException(
						"Could not register stream " + streamURI);
			}
			registeredStreamsByRuleId.put(rule.getId(), streamURI);
		} else {
			logger.info("Stream {} already registered", streamURI);
		}
	}

	private void removeObservers(MonitoringRule rule)
			throws ServerErrorException, ObserverErrorException {
		Set<String> observersToRemove = new HashSet<String>();
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(MMVocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(MOVocabulary.metric,
						action);
				for (String observerId : metricByObserverId.keySet()) {
					if (metricByObserverId.get(observerId).equals(metric)) {
						observersToRemove.add(observerId);
					}
				}
			}
		}
		for (String observer : observersToRemove) {
			csparqlAPI.deleteObserver(observer);
			metricByObserverId.remove(observer);
		}
	}

	private boolean streamIsNotUsed(String stream) {
		return !registeredStreamsByRuleId.values().contains(stream);
	}

	public void uninstallRule(String ruleId) {
		MonitoringRule rule = installedRules.get(ruleId);
		if (rule==null) {
			logger.error("Error while trying to uninstall rule {}: not installed", ruleId);
			return;
		}
		try {
			String relatedInstalledQuery = registeredQueriesByRuleId.get(ruleId);
			deleteObservableMetrics(rule);
			removeObservers(rule);
			csparqlAPI.unregisterQuery(relatedInstalledQuery);
			registeredQueriesById.remove(relatedInstalledQuery);
			registeredQueriesByRuleId.remove(ruleId);

			String sourceStream = registeredStreamsByRuleId
					.remove(ruleId);
			if (streamIsNotUsed(sourceStream)) {
				try {
					csparqlAPI.unregisterStream(sourceStream);
				} catch (Exception e) {
					registeredStreamsByRuleId.put(ruleId, sourceStream);
					throw e;
				}
			}
			installedRules.remove(ruleId);
		} catch (Exception e) {
			logger.error("Error while uninstalling rule {}", ruleId, e);
		}
	}

}
