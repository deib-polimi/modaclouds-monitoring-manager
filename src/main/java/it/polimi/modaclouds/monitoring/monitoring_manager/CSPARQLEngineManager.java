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
import it.polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import it.polimi.deib.csparql_rest_api.exception.ObserverErrorException;
import it.polimi.deib.csparql_rest_api.exception.QueryErrorException;
import it.polimi.deib.csparql_rest_api.exception.ServerErrorException;
import it.polimi.modaclouds.monitoring.dcfactory.wrappers.DDAOntology;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.monitoring_manager.configuration.ManagerConfig;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MOVocabulary;
import it.polimi.modaclouds.qos_models.monitoring_rules.actions.OutputMetric;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class CSPARQLEngineManager {

	private Logger logger = LoggerFactory.getLogger(CSPARQLEngineManager.class
			.getName());
	private URL ddaURL;
	private RSP_services_csparql_API csparqlAPI;

	private Map<String, String> queriesById;
	private Map<String, String> streamsByRuleId;
	private Map<String, String> queryIdByRuleId;
	private Map<String, String> queryIdByMetric;
	private Map<String, String> metricByObserverUri;
	private String kbURL;
	private Map<String, MonitoringRule> rulesById;

	public CSPARQLEngineManager(ManagerConfig config, FusekiKBAPI kb)
			throws Exception {
		this.kbURL = config.getKbUrl();
		ddaURL = createURL(config.getDdaIP(), config.getDdaPort());
		streamsByRuleId = new ConcurrentHashMap<String, String>();
		queriesById = new ConcurrentHashMap<String, String>();
		queryIdByRuleId = new ConcurrentHashMap<String, String>();
		queryIdByMetric = new ConcurrentHashMap<String, String>();
		metricByObserverUri = new ConcurrentHashMap<String, String>();
		csparqlAPI = new RSP_services_csparql_API(ddaURL.toString());
		rulesById = new ConcurrentHashMap<String, MonitoringRule>();

		logger.info("Clearing the DDA");
		clearCsparqlEngine();
	}

	private void clearCsparqlEngine() throws Exception {
		try {
			JsonParser parser = new JsonParser();
			JsonArray jsonQueriesInfoArray = parser.parse(
					csparqlAPI.getQueriesInfo()).getAsJsonArray();
			for (JsonElement jsonElement : jsonQueriesInfoArray) {
				JsonObject queryInfoJson = jsonElement.getAsJsonObject();
				String queryId = queryInfoJson.get("id").getAsString();
				csparqlAPI.unregisterQuery(ddaURL + "/queries/" + queryId);
			}
			unregisterAllStreams();
		} catch (ServerErrorException | QueryErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void installRule(MonitoringRule rule)
			throws RuleInstallationException {
		try {
			String queryId = getNewQueryId(rule, null);
			CSquery query = createQueryFromRule(rule, queryId, kbURL);
			String csparqlQuery = query.getCSPARQL();
			registerInputStream(rule);
			registerQuery(queryId, csparqlQuery, rule);
			addObservableMetrics(rule, queryId);
			rulesById.put(rule.getId(), rule);
		} catch (QueryErrorException | MalformedQueryException e) {
			throw new RuleInstallationException("Internal error", e);
		} catch (ServerErrorException e) {
			throw new RuleInstallationException(
					"Connection to the DDA server failed", e);
		}
	}

	private void unregisterAllStreams() throws Exception {
		JsonParser parser = new JsonParser();
		JsonArray jsonStreamsInfoArray = parser.parse(
				csparqlAPI.getStreamsInfo()).getAsJsonArray();
		for (JsonElement jsonElement : jsonStreamsInfoArray) {
			String streamId = jsonElement.getAsJsonObject().get("streamIRI")
					.getAsString();
			csparqlAPI.unregisterStream(streamId);
		}
	}

	private static String[] addActions(MonitoringRule rule, CSquery query)
			throws RuleInstallationException {
		String[] requiredVars = null;
		if (rule.getActions() == null)
			return requiredVars;

		for (Action action : rule.getActions().getActions()) {
			switch (action.getName()) {
			case OutputMetric.ID:
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
												OutputMetric.metric, action)
										+ "\"")
						.add(DDAOntology.resourceId,
								(Util.getParameterValue(
										OutputMetric.resourceId, action)
										.equals("ID") ? outputResourceIdVariable
										: "\""
												+ Util.getParameterValue(
														OutputMetric.resourceId,
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

	private void addObservableMetrics(MonitoringRule rule, String queryId) {
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(OutputMetric.ID)) {
				String metric = Util.getParameterValue(OutputMetric.metric,
						action);
				queryIdByMetric.put(metric.toLowerCase(), queryId);
			}
		}
	}

	public String addObserver(String metricname, String callbackUrl)
			throws MetricDoesNotExistException, ServerErrorException,
			ObserverErrorException, InternalErrorException {
		String queryUri = getQueryUriFromMetric(metricname);
		String observerUri = csparqlAPI.addObserver(queryUri, callbackUrl);
		metricByObserverUri.put(observerUri, metricname);
		String observerId = observerUri
				.substring(observerUri.lastIndexOf("/") + 1);
		return observerId;
	}

	public List<Observer> getObservers(String metricname)
			throws ServerErrorException, ObserverErrorException,
			MetricDoesNotExistException {
		String queryUri = getQueryUriFromMetric(metricname);
		JsonArray jsonObservers = new JsonParser().parse(
				csparqlAPI.getObserversInformations(queryUri)).getAsJsonArray();
		List<Observer> observers = new ArrayList<Observer>();
		for (JsonElement jsonElement : jsonObservers) {
			observers.add(new Observer(jsonElement.getAsJsonObject().get("id")
					.getAsString(), jsonElement.getAsJsonObject()
					.get("observer").getAsJsonObject().get("clientAddress")
					.getAsString()));
		}
		return observers;
	}

	private String getQueryUriFromMetric(String metricname)
			throws MetricDoesNotExistException {
		String queryId = queryIdByMetric.get(metricname.toLowerCase());
		if (queryId == null)
			throw new MetricDoesNotExistException(metricname);
		String realQueryUri = ddaURL.toString() + "/queries/" + queryId;
		return realQueryUri;
	}

	public void removeObserver(String metricName, String observerId)
			throws ServerErrorException, ObserverErrorException,
			MetricDoesNotExistException {
		String queryUri = getQueryUriFromMetric(metricName);
		String observerUri = queryUri + "/observers/" + observerId;
		csparqlAPI.deleteObserver(observerUri);
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
			String queryId, String kbURL) throws MalformedQueryException,
			RuleInstallationException {
		CSquery query = CSquery.createDefaultQuery(queryId);
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

		query.fromStream(getSourceStreamName(rule), rule.getTimeWindow() + "s",
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

	private URL createURL(String address, int port)
			throws MalformedURLException {
		return new URL("http://" + cleanAddress(address) + ":" + port);
	}

	private void deleteObservableMetrics(MonitoringRule rule) {
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(OutputMetric.ID)) {
				String metric = Util.getParameterValue(OutputMetric.metric,
						action);
				queryIdByMetric.remove(metric.toLowerCase());
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

	private String getNewQueryId(MonitoringRule rule, String suffix) {
		if (suffix == null)
			suffix = "";
		String queryName = CSquery.escapeName(rule.getId()) + suffix;
		while (queriesById.containsKey(queryName)) {
			queryName = CSquery.generateRandomName() + suffix;
		}
		return queryName;
	}

	public Set<String> getObservableMetrics() {
		return queryIdByMetric.keySet();
	}

	public String getQuery(String queryId) {
		return queriesById.get(queryId);
	}

	private static String getSourceStreamName(MonitoringRule rule) {
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

	private void registerQuery(String queryId, String csparqlQuery,
			MonitoringRule rule) throws ServerErrorException,
			QueryErrorException {
		logger.info("Registering query: \n{}", csparqlQuery);
		String queryURI = null;
		try {
			queryURI = csparqlAPI.registerQuery(queryId, csparqlQuery);
		} catch (QueryErrorException e) {
			try {
				csparqlAPI.unregisterQuery(ddaURL + "/queries/" + queryId);
				queryURI = csparqlAPI.registerQuery(queryId, csparqlQuery);
				logger.info("A query with the same id was already installed, it was overwritten by the new one");
			} catch (Exception e2) {
				throw e;
			}
		}
		logger.debug("Server response, query URI {}", queryURI);
		logger.info("Query {} installed", queryId);
		queriesById.put(queryId, csparqlQuery);
		queryIdByRuleId.put(rule.getId(), queryId);

	}

	private String registerInputStream(MonitoringRule rule)
			throws RuleInstallationException {
		String streamName = getSourceStreamName(rule);
		logger.info("Registering stream {}", streamName);
		if (!streamsByRuleId.containsValue(streamName)) {
			String response;
			boolean registered = false;
			try {
				response = csparqlAPI.registerStream(streamName);
				logger.info("Server response: {}", response);
				registered = true;
			} catch (Exception e) {
				if (isStreamInstalled(streamName)) {
					registered = true;
					logger.info("Stream {} already registered", streamName);
				}
			}
			if (!registered) {
				throw new RuleInstallationException(
						"Could not register stream " + streamName);
			}
			streamsByRuleId.put(rule.getId(), streamName);
		} else {
			logger.info("Stream {} already registered", streamName);
		}
		return streamName;
	}

	private boolean isStreamInstalled(String streamName) {
		String streamInfo = null;
		try {
			streamInfo = csparqlAPI.getStreamInfo(streamName);
		} catch (Exception e) {
			return false;
		}
		return streamInfo.contains("\"status\":\"RUNNING\"");
	}

	private void removeObservers(MonitoringRule rule)
			throws ServerErrorException, ObserverErrorException {
		Set<String> observersToRemove = new HashSet<String>();
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(OutputMetric.ID)) {
				String metric = Util.getParameterValue(OutputMetric.metric,
						action);
				for (String observerId : metricByObserverUri.keySet()) {
					if (metricByObserverUri.get(observerId).equals(metric)) {
						observersToRemove.add(observerId);
					}
				}
			}
		}
		for (String observer : observersToRemove) {
			csparqlAPI.deleteObserver(observer);
			metricByObserverUri.remove(observer);
		}
	}

	private boolean streamIsNotUsed(String stream) {
		return !streamsByRuleId.values().contains(stream);
	}

	public void uninstallRule(String ruleId) {
		MonitoringRule rule = rulesById.get(ruleId);
		if (rule == null) {
			logger.error(
					"Error while trying to uninstall rule {}: not installed",
					ruleId);
			return;
		}
		try {
			String queryId = queryIdByRuleId.get(ruleId);
			deleteObservableMetrics(rule);
			removeObservers(rule);
			csparqlAPI.unregisterQuery(ddaURL + "/queries/" + queryId);
			queriesById.remove(queryId);
			queryIdByRuleId.remove(ruleId);

			String sourceStream = streamsByRuleId.remove(ruleId);
			if (streamIsNotUsed(sourceStream)) {
				try {
					csparqlAPI.unregisterStream(sourceStream);
				} catch (Exception e) {
					streamsByRuleId.put(ruleId, sourceStream);
					throw e;
				}
			}
			rulesById.remove(ruleId);
		} catch (Exception e) {
			logger.error("Error while uninstalling rule {}", ruleId, e);
		}
	}

}
