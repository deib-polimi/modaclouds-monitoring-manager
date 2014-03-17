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

import com.hp.hpl.jena.vocabulary.OWLResults;
import it.polimi.csparqool.Aggregation;
import it.polimi.csparqool.CSquery;
import it.polimi.csparqool.MalformedQueryException;
import it.polimi.csparqool.graph;
import it.polimi.csparqool.select;
import it.polimi.modaclouds.monitoring.monitoring_rules.ConfigurationException;
import it.polimi.modaclouds.monitoring.monitoring_rules.RuleValidator;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoringMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import java.util.UUID;

public class RuleManager {

    private Logger logger = LoggerFactory
            .getLogger(RuleManager.class.getName());

    private Map<String, CSquery> installedQueries;
    private Map<String, MonitoringRule> installedRules;
    private Map<String, List<String>> ruleQueriesMap;
    private RuleValidator validator;

    public RuleManager() throws ConfigurationException,
            JAXBException {
        installedQueries = new HashMap<String, CSquery>();
        installedRules = new HashMap<String, MonitoringRule>();
        ruleQueriesMap = new HashMap<String, List<String>>();
        validator = new RuleValidator();
    }

    public List<String> installRule(MonitoringRule rule) {
        List<String> queriesIds = new ArrayList<String>();
        try {
            CSquery query = CSquery.createDefaultQuery(rule.getId());
            query.setNsPrefix("xsd", XSD.getURI())
                    .setNsPrefix("rdf", RDF.getURI())
                    .setNsPrefix("rdfs", RDFS.getURI())
                    .setNsPrefix("mo", MO.getURI());

            for (Action a : rule.getActions().getActions()) {
                switch (a.getName()) {
                    case "notify_violation":
                        query.construct(graph
                                .add(CSquery.BLANK_NODE, MO.hasMetric,
                                        "mo:" + rule.getMetricName() + "_violation")
                                .add(MO.isAbout, QueryVars.TARGET)
                                .add(MO.hasValue, QueryVars.OUTPUT));
                        break;
                    case "enable_monitoring_rule":

                        break;
                    case "disable_monitoring_rule":

                        break;
                    case "set_sampling_probability":

                        break;
                    case "set_sampling_time":

                        break;

                    default:
                        break;
                }
            }

            String sourceStreamURI = MO.getStreamsURI() + rule.getMetricName();

            query.fromStream(sourceStreamURI, rule.getTimeWindow() + "s",
                    rule.getTimeStep() + "s");
            query.from(MO.getKnowledgeBaseURL());

            MonitoringMetricAggregation aggregation = rule
                    .getMetricAggregation();
            if (aggregation != null && !aggregation.isInherited()) {
                String computation;
                switch (aggregation.getAggregateFunction()) {
                    case "average":
                        computation = Aggregation.AVERAGE;
                        break;
                    default:
                        throw new MalformedQueryException("Aggregate function " + aggregation.getAggregateFunction() + " does not exist");
                }
                query.where(select.add(QueryVars.OUTPUT, QueryVars.INPUT, computation));
            }

            String queryId = UUID.randomUUID().toString();
            installedQueries.put(queryId, query);
            installedRules.put(rule.getId(), rule);
            queriesIds.add(queryId);

            ruleQueriesMap.put(rule.getId(), queriesIds);

        } catch (MalformedQueryException e) {
            logger.error("Cannot install rule, bug found", e);
            e.printStackTrace();
        }
        return queriesIds;
    }

    public List<String> installRules(MonitoringRules rules) {

        List<String> queriesIds = new ArrayList<String>();

        for (MonitoringRule rule : rules.getMonitoringRules()) {
            queriesIds.addAll(installRule(rule));
        }
        return queriesIds;
    }

    public String getQuery(String queryId) {
        return installedQueries.get(queryId).toString();
    }

}
