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
import it.polimi.csparqool.body;
import it.polimi.csparqool.graph;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
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

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class CSPARQLEngineManager {

	private Logger logger = LoggerFactory.getLogger(CSPARQLEngineManager.class
			.getName());
	// private DatasetAccessor da = DatasetAccessorFactory.createHTTP(MO
	// .getKnowledgeBaseDataURL());
	private URL ddaURL;
	private URL sdaURL;
	private RSP_services_csparql_API csparqlAPI;

	private Map<String, String> registeredQueriesById;
	private Map<String, String> registeredStreamsByRuleId;
	private Map<String, Set<String>> registeredQueriesByRuleId;
	private ConcurrentHashMap<String, String> queryURIByMetric;
	private ConcurrentHashMap<String, String> metricByObserverId;

	// private RuleValidator validator;

	public CSPARQLEngineManager() throws ConfigurationException {
		try {
			loadConfig();
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}

		registeredStreamsByRuleId = new ConcurrentHashMap<String, String>();
		registeredQueriesById = new ConcurrentHashMap<String, String>();
		registeredQueriesByRuleId = new ConcurrentHashMap<String, Set<String>>();
		queryURIByMetric = new ConcurrentHashMap<String, String>();
		metricByObserverId = new ConcurrentHashMap<String, String>();
		// validator = new RuleValidator();
		csparqlAPI = new RSP_services_csparql_API(ddaURL.toString());
	}

	private void loadConfig() throws MalformedURLException,
			ConfigurationException {
		Config config = Config.getInstance();
		ddaURL = createURL(config.getDDAServerAddress(),
				config.getDDAServerPort());
		sdaURL = createURL(config.getSDAServerAddress(),
				config.getSDAServerPort());
	}

	private URL createURL(String address, int port)
			throws MalformedURLException {
		return new URL("http://" + cleanAddress(address) + ":" + port);
	}

	private String cleanAddress(String address) {
		if (address.indexOf("://") != -1)
			address = address.substring(address.indexOf("://") + 3);
		if (address.endsWith("/"))
			address = address.substring(0, address.length() - 1);
		return address;
	}

	public void uninstallRule(MonitoringRule rule)
			throws FailedToUninstallRuleException {
		Set<String> relatedInstalledQueries = registeredQueriesByRuleId
				.get(rule.getId());
		try {
			deleteObservableMetrics(rule);
			removeObservers(rule);
			for (String queryId : relatedInstalledQueries) {
				csparqlAPI.unregisterQuery(queryId);
				registeredQueriesById.remove(queryId);
			}
			registeredQueriesByRuleId.remove(rule.getId());

			String sourceStream = registeredStreamsByRuleId
					.remove(rule.getId());
			if (streamIsNotUsed(sourceStream)) {
				try {
					csparqlAPI.unregisterStream(sourceStream);
				} catch (Exception e) {
					registeredStreamsByRuleId.put(rule.getId(), sourceStream);
					throw e;
				}
			}

		} catch (Exception e) {
			throw new FailedToUninstallRuleException(e);
		}
	}

	private boolean streamIsNotUsed(String stream) {
		return !registeredStreamsByRuleId.values().contains(stream);
	}

	public void installRule(MonitoringRule rule, boolean sdaRequired,
			String sdaReturnedMetric) throws RuleInstallationException {
		try {
			String metricName = sdaRequired ? sdaReturnedMetric
					: getMetricName(rule);

			String queryName = getNewQueryName(rule, null);
			String sourceStreamURI = getSourceStreamURI(metricName);
			CSquery query = createActionQuery(rule, queryName, sourceStreamURI,
					sdaRequired);
			String csparqlQuery = query.getCSPARQL();
			logger.info("Query generated:\n" + csparqlQuery);

			if (sdaRequired) {
				String tunnelQueryName = getNewQueryName(rule, "Tunnel");
				String tunnelSourceStreamURI = getSourceStreamURI(getMetricName(rule));
				CSquery tunnelQuery = createTunnelQuery(rule, tunnelQueryName,
						tunnelSourceStreamURI);
				String csparqlTunnelQuery = tunnelQuery.getCSPARQL();
				logger.info("Tunnel query generated:\n" + csparqlTunnelQuery);

				registerStream(tunnelSourceStreamURI, rule);
				String tunnelQueryURI = registerQuery(tunnelQueryName,
						csparqlTunnelQuery, rule);
				attachObserver(tunnelQueryURI, sdaURL);
			}

			registerStream(sourceStreamURI, rule);
			String queryURI = registerQuery(queryName, csparqlQuery, rule);
			addObservableMetrics(rule, queryURI);

		} catch (QueryErrorException | MalformedQueryException e) {
			logger.error("Internal error", e);
			throw new RuleInstallationException("Internal error", e);
		} catch (ServerErrorException e) {
			logger.error("Connection to the DDA server failed", e);
			throw new RuleInstallationException(
					"Connection to the DDA server failed", e);
		} catch (ObserverErrorException e) {
			logger.error("Connection to the SDA server failed", e);
			throw new RuleInstallationException(
					"Connection to the SDA server failed", e);
		}
	}

	private String getMetricName(MonitoringRule rule) {
		while (rule.getCollectedMetric().isInherited()) {
			rule = rule.getParentMonitoringRule();
		}
		return rule.getCollectedMetric().getMetricName();
	}

	private void attachObserver(String queryURI, URL url)
			throws ServerErrorException, ObserverErrorException {
		csparqlAPI.addObserver(queryURI, url.toString());
	}

	private String registerQuery(String queryName, String csparqlQuery,
			MonitoringRule rule) throws ServerErrorException,
			QueryErrorException {
		String queryURI = csparqlAPI.registerQuery(queryName, csparqlQuery);
		logger.info("Server response, query ID: " + queryURI);

		// fix:
		queryURI = ddaURL.toString() + "/queries/" + queryName;
		logger.info("actual query ID (temp fix):" + queryURI);

		registeredQueriesById.put(queryURI, csparqlQuery);
		Set<String> queriesURIs = registeredQueriesByRuleId.get(rule.getId());
		if (queriesURIs == null) {
			queriesURIs = Collections
					.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
			;
			registeredQueriesByRuleId.put(rule.getId(), queriesURIs);
		}
		queriesURIs.add(queryURI);
		return queryURI;
	}

	private void registerStream(String streamURI, MonitoringRule rule)
			throws RuleInstallationException {
		if (!registeredStreamsByRuleId.containsValue(streamURI)) {
			logger.info("Registering stream: " + streamURI);
			String response;
			boolean registered = false;
			try {
				response = csparqlAPI.registerStream(streamURI);
				logger.info("Server response: " + response);
				registered = true;
			} catch (Exception e) {
				if (e.getMessage().contains("already exists")) {
					registered = true;
					logger.info("Stream already exists");
				}
			}
			if (!registered)
				throw new RuleInstallationException(
						"Could not register stream " + streamURI);
			registeredStreamsByRuleId.put(rule.getId(), streamURI);
		}
	}

	private CSquery createActionQuery(MonitoringRule rule, String queryName,
			String sourceStreamURI, boolean sdaRequired)
			throws MalformedQueryException, RuleInstallationException {
		CSquery query = createQueryTemplate(queryName);
		String[] requiredVars = addActions(rule, query, sdaRequired);
		_body queryBody = new _body();
		addSelect(queryBody, requiredVars, rule, sdaRequired);

		if (sdaRequired) {
			queryBody.where(createGraphPattern(rule));
		} else {
			_body innerQueryBody = new _body();
			addSelect(innerQueryBody, getInnerQueryRequiredVars(requiredVars),
					rule, sdaRequired);
			queryBody.where(innerQueryBody.where(createGraphPattern(rule)))
					.groupby(Util.getGroupingClassVariable(rule));
		}

		query.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
				rule.getTimeStep() + "s")
				.from(MO.getKnowledgeBaseDataURL() + "?graph=default")
				.where(queryBody.having(parseCondition(rule.getCondition(),
						Util.getOutputValueVariable(rule, sdaRequired))));
		return query;
	}

	// private String getParameterValue(String parameterName,
	// List<Parameter> parameters) {
	// for (Parameter p : parameters) {
	// if (p.getName().equals(parameterName))
	// return p.getValue();
	// }
	// return null;
	// }

	private String[] getInnerQueryRequiredVars(String[] outerQueryRequiredVars) {
		String[] innerQueryRequiredVars = new String[outerQueryRequiredVars.length];
		for (int i = 0; i < outerQueryRequiredVars.length; i++) {
			switch (outerQueryRequiredVars[i]) {
			case QueryVars.OUTPUT:
				innerQueryRequiredVars[i] = QueryVars.INPUT;
				break;
			case QueryVars.TIMESTAMP:
				innerQueryRequiredVars[i] = QueryVars.INPUT_TIMESTAMP;
				break;

			default:
				innerQueryRequiredVars[i] = outerQueryRequiredVars[i];
				break;
			}
		}
		return innerQueryRequiredVars;
	}

	private void addSelect(_body queryBody, String[] variables,
			MonitoringRule rule, boolean sdaRequired)
			throws MalformedQueryException {
		String aggregateFunction = Util.getAggregateFunction(rule);
		for (String var : variables) {
			switch (var) {
			case QueryVars.TIMESTAMP:
				if (sdaRequired) {
					queryBody.selectFunction(QueryVars.TIMESTAMP,
							Function.TIMESTAMP, QueryVars.DATUM,
							MO.shortForm(MO.aboutResource), QueryVars.TARGET);
				} else {
					queryBody.selectFunction(QueryVars.TIMESTAMP, Function.MAX,
							QueryVars.INPUT_TIMESTAMP);
				}
				break;
			case QueryVars.INPUT_TIMESTAMP:
				queryBody.selectFunction(QueryVars.INPUT_TIMESTAMP,
						Function.TIMESTAMP, QueryVars.DATUM,
						MO.shortForm(MO.aboutResource), QueryVars.TARGET);
				break;
			case QueryVars.OUTPUT:
				if (Util.isGroupedMetric(rule) && !sdaRequired) {
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

	private CSquery createTunnelQuery(MonitoringRule rule, String queryName,
			String sourceStreamURI) throws RuleInstallationException {
		try {
			CSquery tunnelQuery = createQueryTemplate(queryName);
			tunnelQuery
					.select(QueryVars.TARGET, QueryVars.INPUT,
							QueryVars.TIMESTAMP)
					.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
							rule.getTimeStep() + "s")
					.from(MO.getKnowledgeBaseDataURL() + "?graph=default")
					.where(body
							.select(QueryVars.TARGET, QueryVars.INPUT)
							.selectFunction(QueryVars.TIMESTAMP,
									Function.TIMESTAMP, QueryVars.DATUM,
									MO.shortForm(MO.aboutResource),
									QueryVars.TARGET)
							.where(createGraphPattern(rule)));
			return tunnelQuery;
		} catch (MalformedQueryException e) {
			throw new RuleInstallationException(e);
		}
	}

	private CSquery createQueryTemplate(String queryName)
			throws MalformedQueryException {
		CSquery query = CSquery.createDefaultQuery(queryName);
		addPrefixes(query);
		return query;
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

	private String getSourceStreamURI(String metric) {
		return MO.streamsURI + metric;
	}

	private String[] addActions(MonitoringRule rule, CSquery query,
			boolean sdaRequired) throws RuleInstallationException {
		String[] requiredVars = null;
		if (rule.getActions() == null)
			return requiredVars;

		for (Action action : rule.getActions().getActions()) {
			switch (action.getName()) {
			case Vocabulary.OutputMetric:
				String outputTargetVariable = Util.getOutputTarget(rule);
				String outputValueVariable = Util.getOutputValueVariable(rule,
						sdaRequired);
				requiredVars = new String[] { outputTargetVariable,
						outputValueVariable, QueryVars.TIMESTAMP };
				query.construct(graph
						.add(CSquery.BLANK_NODE,
								MO.metric,
								"mo:"
										+ Util.getParameterValue(
												Vocabulary.name, action))
						.add(MO.aboutResource, outputTargetVariable)
						.add(MO.value, outputValueVariable)
						.add(MO.timestamp, QueryVars.TIMESTAMP));
				break;
			case Vocabulary.EnableMonitoringRule:
				throw new NotImplementedException("Action " + action.getName()
						+ " has not been implemented yet.");
				// break;
			case Vocabulary.DisableMonitoringRule:
				throw new NotImplementedException("Action " + action.getName()
						+ " has not been implemented yet.");
				// break;
			case Vocabulary.SetSamplingProbability:
				throw new NotImplementedException("Action " + action.getName()
						+ " has not been implemented yet.");
				// break;
			case Vocabulary.SetSamplingTime:
				throw new NotImplementedException("Action " + action.getName()
						+ " has not been implemented yet.");
				// break;

			default:
				throw new NotImplementedException("Action " + action.getName()
						+ " has not been implemented yet.");
			}
		}

		return requiredVars;
	}

	private void addPrefixes(CSquery query) {
		query.setNsPrefix("xsd", XSD.getURI())
				.setNsPrefix("rdf", RDF.getURI())
				.setNsPrefix("rdfs", RDFS.getURI())
				.setNsPrefix("mo", MO.URI)
				.setNsPrefix(CSquery.getFunctionsPrefix(),
						CSquery.getFunctionsURI());
	}

	private String parseCondition(String condition, String outputValueVariable) {
		return condition != null ? condition.replace("METRIC",
				outputValueVariable) : null;
	}

	private _graph createGraphPattern(MonitoringRule rule)
			throws RuleInstallationException {
		_graph graph = new _graph();
		List<MonitoredTarget> targets = Util.getMonitoredTargets(rule);
		String groupingClass = Util.getGroupingClass(rule);

		graph.add(QueryVars.DATUM, MO.metric,
				MO.prefix + ":" + rule.getCollectedMetric().getMetricName())
				.add(MO.aboutResource, QueryVars.TARGET)
				.add(MO.value, QueryVars.INPUT)
				.add(QueryVars.TARGET, MO.id,
						getTargetIDLiteral(targets.get(0)));

		switch (targets.get(0).getClazz()) {
		case Vocabulary.VM:
			graph.add(QueryVars.TARGET, RDF.type, MO.VM);
			if (groupingClass != null) {
				switch (groupingClass) {
				case Vocabulary.VM:
					break;
				case Vocabulary.CloudProvider:
					graph.add(QueryVars.TARGET, MO.cloudProvider,
							Util.getGroupingClassVariable(rule));
					break;
				default:
					throw new NotImplementedException("Grouping class "
							+ groupingClass + " for target "
							+ targets.get(0).getClazz()
							+ " has not been implemented yet");
				}
				break;
			}
		default:
			throw new NotImplementedException(
					"Cannot install rules with target class "
							+ targets.get(0).getClazz() + " yet");
		}
		return graph;
	}

	private String getTargetIDLiteral(MonitoredTarget monitoredTarget) {
		return "\"" + monitoredTarget.getId() + "\"";
	}

	public String getQuery(String queryId) {
		return registeredQueriesById.get(queryId);
	}

	// private boolean isSubclassOf(String resourceURI, String superClassURI) {
	// String queryString = "ASK " + "FROM <" + MO.getKnowledgeBaseDataURL()
	// + "?graph=default>" + "WHERE { <" + resourceURI + "> <"
	// + RDFS.subClassOf + "> <" + superClassURI + "> . }";
	//
	// Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
	// QueryExecution qexec = QueryExecutionFactory.create(query);
	//
	// return qexec.execAsk();
	// }

	private void deleteObservableMetrics(MonitoringRule rule) {
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(Vocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(Vocabulary.name, action);
				queryURIByMetric.remove(metric);
			}
		}
	}

	private void addObservableMetrics(MonitoringRule rule, String queryURI) {
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(Vocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(Vocabulary.name, action);
				queryURIByMetric.put(metric, queryURI);
			}
		}
	}

	public String addObserver(String metricname, String callbackUrl)
			throws MetricDoesNotExistException, ServerErrorException,
			ObserverErrorException {
		if (!queryURIByMetric.keySet().contains(metricname))
			throw new MetricDoesNotExistException();
		String queryURI = queryURIByMetric.get(metricname);
		csparqlAPI.addObserver(queryURI, callbackUrl);
		String observerId = String.valueOf(queryURI.hashCode());
		metricByObserverId.put(observerId, metricname);
		return observerId;
	}

	public Set<String> getObservableMetrics() {
		return queryURIByMetric.keySet();
	}

	private void removeObservers(MonitoringRule rule)
			throws ServerErrorException, ObserverErrorException {
		Set<String> observersToRemove = new HashSet<String>();
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(Vocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(Vocabulary.name, action);
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

}
