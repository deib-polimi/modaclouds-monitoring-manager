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
import it.polimi.csparqool.FunctionArgs;
import it.polimi.csparqool.MalformedQueryException;
import it.polimi.csparqool._body;
import it.polimi.csparqool._graph;
import it.polimi.csparqool._union;
import it.polimi.csparqool.body;
import it.polimi.csparqool.graph;
import it.polimi.modaclouds.monitoring.dcfactory.ddaconnectors.RCSOntology;
import it.polimi.modaclouds.monitoring.dcfactory.kbconnectors.FusekiConnector;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MOVocabulary;
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

import com.google.common.base.Strings;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class CSPARQLEngineManager {

	private Logger logger = LoggerFactory.getLogger(CSPARQLEngineManager.class
			.getName());
	// private DatasetAccessor da = DatasetAccessorFactory.createHTTP(MO
	// .getKnowledgeBaseDataURL());
	private URL ddaURL;
	private URL matlabSdaURL;
	private URL javaSdaURL;
	private RSP_services_csparql_API csparqlAPI;

	private Map<String, String> registeredQueriesById;
	private Map<String, String> registeredStreamsByRuleId;
	private Map<String, Set<String>> registeredQueriesByRuleId;
	private ConcurrentHashMap<String, String> queryURIByMetric;
	// private ConcurrentHashMap<String, String> observableQueryURIByMetric;
	private ConcurrentHashMap<String, String> metricByObserverId;
	private MonitoringManager monitoringManager;
	private Config config;
	private FusekiKBAPI kb;

	// private RuleValidator validator;

	public CSPARQLEngineManager(MonitoringManager monitoringManager,
			Config config, FusekiKBAPI kb) throws MalformedURLException {
		this.config = config;
		this.kb = kb;
		configure();

		this.monitoringManager = monitoringManager;
		registeredStreamsByRuleId = new ConcurrentHashMap<String, String>();
		registeredQueriesById = new ConcurrentHashMap<String, String>();
		registeredQueriesByRuleId = new ConcurrentHashMap<String, Set<String>>();
		queryURIByMetric = new ConcurrentHashMap<String, String>();
		// observableQueryURIByMetric = new ConcurrentHashMap<String, String>();
		metricByObserverId = new ConcurrentHashMap<String, String>();
		// validator = new RuleValidator();
		csparqlAPI = new RSP_services_csparql_API(ddaURL.toString());
	}

	private String[] addActions(MonitoringRule rule, CSquery query,
			boolean sdaRequired) throws RuleInstallationException {
		String[] requiredVars = null;
		if (rule.getActions() == null)
			return requiredVars;

		for (Action action : rule.getActions().getActions()) {
			switch (action.getName()) {
			case MMVocabulary.OutputMetric:
				String outputReousourceIdVariableVariable = Util
						.getOutputResourceIdVariable(rule);
				String outputValueVariable = Util.getOutputValueVariable(rule);
				requiredVars = new String[] {
						outputReousourceIdVariableVariable,
						outputValueVariable, QueryVars.TIMESTAMP };
				query.construct(graph
						.add(CSquery.BLANK_NODE,
								RCSOntology.metric,
								"\""
										+ Util.getParameterValue(
												MOVocabulary.name, action) + "\"")
						.add(RCSOntology.resourceId,
								outputReousourceIdVariableVariable)
						.add(RCSOntology.value, outputValueVariable)
						.add(RCSOntology.timestamp, QueryVars.TIMESTAMP));
				break;
			// case MMVocabulary.EnableMonitoringRule:
			// throw new NotImplementedException("Action " + action.getName()
			// + " has not been implemented yet.");
			// // break;
			// case MMVocabulary.DisableMonitoringRule:
			// throw new NotImplementedException("Action " + action.getName()
			// + " has not been implemented yet.");
			// // break;
			// case MMVocabulary.SetSamplingProbability:
			// throw new NotImplementedException("Action " + action.getName()
			// + " has not been implemented yet.");
			// // break;
			// case MMVocabulary.SetSamplingTime:
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
				String metric = Util.getParameterValue(MOVocabulary.name, action);
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

	private void addPrefixes(CSquery query) {
		query.setNsPrefix("xsd", XSD.getURI())
				.setNsPrefix("rdf", RDF.getURI())
				.setNsPrefix("rdfs", RDFS.getURI())
				.setNsPrefix(MO.prefix, MO.URI)
				.setNsPrefix(RCSOntology.prefix, RCSOntology.URI)
				.setNsPrefix(CSquery.getFunctionsPrefix(),
						CSquery.getFunctionsURI());
	}

	private void addSelect(_body queryBody, String[] variables,
			MonitoringRule rule, boolean sdaRequired)
			throws MalformedQueryException, RuleInstallationException {
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
						RCSOntology.shortForm(RCSOntology.resourceId),
						QueryVars.RESOURCE_ID);
				break;
			case QueryVars.OUTPUT:
				if (Util.isGroupedMetric(rule) && !sdaRequired) {
					String[] parameters = Util.getAggregateFunctionArgs(rule);
					queryBody.selectFunction(QueryVars.OUTPUT,
							aggregateFunction, parameters);
				} else if (Util.isGroupedMetric(rule) && sdaRequired) {
					String[] parameters = new String[1];
					parameters[FunctionArgs.getArgIdx(Function.AVERAGE,
							FunctionArgs.INPUT_VARIABLE)] = QueryVars.INPUT;
					queryBody.selectFunction(QueryVars.OUTPUT,
							Function.AVERAGE, parameters);
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

	private void attachObserver(String queryURI, URL url)
			throws ServerErrorException, ObserverErrorException {
//		csparqlAPI.addObserver(queryURI, url.toString() + "/v1/results");
		csparqlAPI.addObserver(queryURI, url.toString());
	}

	private String cleanAddress(String address) {
		if (address.indexOf("://") != -1)
			address = address.substring(address.indexOf("://") + 3);
		if (address.endsWith("/"))
			address = address.substring(0, address.length() - 1);
		return address;
	}

	private CSquery createActionQuery(MonitoringRule rule, String queryName,
			String sourceStreamURI, boolean sdaRequired)
			throws MalformedQueryException, RuleInstallationException {
		CSquery query = createQueryTemplate(queryName);
		String[] requiredVars = addActions(rule, query, sdaRequired);
		_body queryBody = new _body();
		addSelect(queryBody, requiredVars, rule, sdaRequired);

		_body innerQueryBody = new _body();
		addSelect(innerQueryBody, getInnerQueryRequiredVars(requiredVars),
				rule, sdaRequired);
		queryBody.where(innerQueryBody.where(createGraphPattern(rule)));

		if (Util.isGroupedMetric(rule)) {
			queryBody.groupby(Util.getGroupingClassIdVariable(rule));
		}
		if (rule.getCondition() != null) {
			queryBody.having(parseCondition(rule.getCondition().getValue(),
					Util.getOutputValueVariable(rule)));
		}

		query.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
				rule.getTimeStep() + "s")
				.from(kb.getGraphURL(MonitoringManager.MODEL_GRAPH_NAME))
				.where(queryBody);
		return query;
	}

	private _graph createGraphPattern(MonitoringRule rule)
			throws RuleInstallationException {
		_graph graphPattern = new _graph();
		List<MonitoredTarget> targets = Util.getMonitoredTargets(rule);
		String groupingClass = Util.getGroupingClass(rule);

		graphPattern
				.add(QueryVars.DATUM, RCSOntology.resourceId,
						QueryVars.RESOURCE_ID)
				.add(RCSOntology.value, QueryVars.INPUT)
				.add(QueryVars.RESOURCE, MO.id, QueryVars.RESOURCE_ID);

		_union unionOfTargets = new _union();
		graphPattern.add(unionOfTargets);
		for (MonitoredTarget target : targets) {
			if (!Strings.isNullOrEmpty(target.getId()))
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
		case MOVocabulary.Method:
			graphPattern.add(QueryVars.RESOURCE, RDF.type, MO.Method);
			if (groupingClass != null) {
				switch (groupingClass) {
				case MOVocabulary.Method:
					break;
				case MOVocabulary.CloudProvider:
					graphPattern
							//.add(QueryVars.INTERNAL_COMPONENT,
									//MO.providedMethods, QueryVars.RESOURCE)
					.add(MO.requiredInternalComponent,
							QueryVars.INTERNAL_COMPONENT, QueryVars.RESOURCE)
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
		default:
			throw new NotImplementedException(
					"Cannot install rules with target class "
							+ targets.get(0).getClazz() + " yet");
		}
		return graphPattern;
	}

	private CSquery createQueryTemplate(String queryName)
			throws MalformedQueryException {
		CSquery query = CSquery.createDefaultQuery(queryName);
		addPrefixes(query);
		return query;
	}

	private CSquery createTunnelQuery(MonitoringRule rule, String queryName,
			String sourceStreamURI) throws RuleInstallationException {
		try {
			CSquery tunnelQuery = createQueryTemplate(queryName);
			List<MonitoredTarget> targets = Util.getMonitoredTargets(rule);
			tunnelQuery
					.select(QueryVars.RESOURCE_ID, QueryVars.METRIC,
							QueryVars.INPUT, QueryVars.TIMESTAMP)
					.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
							rule.getTimeStep() + "s")
					.from(kb.getGraphURL(MonitoringManager.MODEL_GRAPH_NAME))
					.where(body
							.select(QueryVars.RESOURCE_ID, QueryVars.INPUT,
									QueryVars.METRIC)
							.selectFunction(
									QueryVars.TIMESTAMP,
									Function.TIMESTAMP,
									QueryVars.DATUM,
									RCSOntology
											.shortForm(RCSOntology.resourceId),
									QueryVars.RESOURCE_ID)
							.where(graph
									.add(QueryVars.DATUM, RCSOntology.metric,
											QueryVars.METRIC)
									.add(RCSOntology.resourceId,
											QueryVars.RESOURCE_ID)
									.add(RCSOntology.value, QueryVars.INPUT)
									.add(QueryVars.RESOURCE, MO.id,
											QueryVars.RESOURCE_ID)
									.add(MO.type,
											getTargetIDLiteral(targets.get(0)))
									.filter(QueryVars.METRIC
											+ " = "
											+ "\""
											+ rule.getCollectedMetric()
													.getMetricName() + "\"")));
			return tunnelQuery;
		} catch (MalformedQueryException e) {
			throw new RuleInstallationException(e);
		}
	}

	private URL createURL(String address, String port)
			throws MalformedURLException {
		return new URL("http://" + cleanAddress(address) + ":" + port);
	}

	// private String getParameterValue(String parameterName,
	// List<Parameter> parameters) {
	// for (Parameter p : parameters) {
	// if (p.getName().equals(parameterName))
	// return p.getValue();
	// }
	// return null;
	// }

	private void deleteObservableMetrics(MonitoringRule rule) {
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(MMVocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(MOVocabulary.name, action);
				queryURIByMetric.remove(metric.toLowerCase());
			}
		}
	}

	private String extractNewStreamNameFromStreamQuery(String queryURI) {
		// return ddaURL.toString() + "/streams/"
		// + queryURI.substring(queryURI.lastIndexOf("/") + 1,
		// queryURI.length());
		return "http://www.modaclouds.eu/streams/"
				+ queryURI.substring(queryURI.lastIndexOf("/") + 1,
						queryURI.length());
	}

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

	private String getMetricName(MonitoringRule rule) {
		while (rule.getCollectedMetric().isInherited()) {
			rule = monitoringManager.getParentRule(rule
					.getParentMonitoringRuleId());
		}
		return rule.getCollectedMetric().getMetricName();
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

	private String getSourceStreamURI(String metric) {
		return "http://www.modaclouds.eu/streams/" + metric.toLowerCase();
	}

	private String getTargetIDLiteral(MonitoredTarget monitoredTarget) {
		return "\"" + monitoredTarget.getId() + "\"";
	}

	public void installRule(MonitoringRule rule, String requiredDataAnalyzer,
			String sdaReturnedMetric) throws RuleInstallationException {
		try {
			String metricName = isSDARequired(requiredDataAnalyzer) ? sdaReturnedMetric
					: getMetricName(rule);

			String queryName = getNewQueryName(rule, null);
			String sourceStreamURI = getSourceStreamURI(metricName);
			CSquery query = createActionQuery(rule, queryName, sourceStreamURI,
					isSDARequired(requiredDataAnalyzer));
			String csparqlQuery = query.getCSPARQL();

			if (isSDARequired(requiredDataAnalyzer)) {
				String tunnelQueryName = getNewQueryName(rule, "Tunnel");
				String tunnelSourceStreamURI = getSourceStreamURI(getMetricName(rule));
				CSquery tunnelQuery = createTunnelQuery(rule, tunnelQueryName,
						tunnelSourceStreamURI);
				String csparqlTunnelQuery = tunnelQuery.getCSPARQL();

				registerStream(tunnelSourceStreamURI, rule);
				String tunnelQueryURI = registerQuery(tunnelQueryName,
						csparqlTunnelQuery, rule);
				switch (requiredDataAnalyzer) {
				case MMVocabulary.MATLAB_SDA: //TODO what path??
					attachObserver(tunnelQueryURI, matlabSdaURL);
					break;

				case MMVocabulary.JAVA_SDA: //TODO what path??
					attachObserver(tunnelQueryURI, javaSdaURL);
					break;
				}
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

	private boolean isSDARequired(String requiredDataAnalyzer) {
		return Util.softEquals(requiredDataAnalyzer, MMVocabulary.MATLAB_SDA)
				|| Util.softEquals(requiredDataAnalyzer, MMVocabulary.JAVA_SDA);
	}

	private void configure() throws MalformedURLException {
		ddaURL = createURL(config.getDdaIP(), config.getDdaPort());
		matlabSdaURL = createURL(config.getMatlabSdaIP(),
				config.getMatlabSdaPort());
		javaSdaURL = createURL(config.getJavaSdaIP(), config.getJavaSdaPort());
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

	private String parseCondition(String condition, String outputValueVariable) {
		return condition != null ? condition.replace("METRIC",
				outputValueVariable) : null;
	}

	// private String registerObservableQuery(String queryURI, String
	// metricname)
	// throws InternalErrorException {
	// try {
	// String queryName = CSquery.generateRandomName();
	// CSquery observableQuery = createQueryTemplate(queryName);
	// observableQuery
	// .select(QueryVars.RESOURCE_ID, QueryVars.METRIC,
	// QueryVars.VALUE, QueryVars.TIMESTAMP)
	// .fromStream(extractNewStreamNameFromStreamQuery(queryURI),
	// "10s", "10s")
	// // .from(MO.getKnowledgeBaseDataURL() + "?graph=default")
	// .where(body
	// .select(QueryVars.RESOURCE_ID, QueryVars.METRIC,
	// QueryVars.VALUE)
	// .selectFunction(
	// QueryVars.TIMESTAMP,
	// Function.TIMESTAMP,
	// QueryVars.DATUM,
	// RCSOntology
	// .shortForm(RCSOntology.resourceId),
	// QueryVars.RESOURCE_ID)
	// .where(graph
	// .add(QueryVars.DATUM, RCSOntology.metric,
	// QueryVars.METRIC)
	// .add(RCSOntology.resourceId,
	// QueryVars.RESOURCE_ID)
	// .add(RCSOntology.value, QueryVars.VALUE)));
	// String queryString = observableQuery.getCSPARQL();
	// logger.info("Registering observable query: " + queryString);
	// String observableQueryURI = csparqlAPI.registerQuery(queryName,
	// queryString);
	// logger.info("Server response, query ID: " + observableQueryURI);
	//
	// observableQueryURI = ddaURL.toString() + "/queries/" + queryName;
	// // observableQueryURI = "http://www.modaclouds.eu/queries/" +
	// // queryName;
	// logger.info("actual query ID (temp fix):" + observableQueryURI);
	// return observableQueryURI;
	// } catch (Exception e) {
	// throw new InternalErrorException(e);
	// }
	// }

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
		Set<String> queriesURIs = registeredQueriesByRuleId.get(rule.getId());
		if (queriesURIs == null) {
			queriesURIs = Collections
					.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
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

	private void removeObservers(MonitoringRule rule)
			throws ServerErrorException, ObserverErrorException {
		Set<String> observersToRemove = new HashSet<String>();
		for (Action action : rule.getActions().getActions()) {
			if (action.getName().equals(MMVocabulary.OutputMetric)) {
				String metric = Util.getParameterValue(MOVocabulary.name, action);
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

}
