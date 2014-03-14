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
package it.polimi.modaclouds.monitoring.test;

import eu.larkc.csparql.core.new_parser.utility_files.CSparqlTranslator;
import eu.larkc.csparql.core.new_parser.utility_files.Translator;
import eu.larkc.csparql.core.streams.formats.TranslationException;
import it.polimi.csparqool.CSquery;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleManager;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class TranslationTest {

    private MonitoringRules monitoringRules;
    private Translator t;
    private RuleManager ruleManager;

    @Before
    public void loadXMLString() {
        t = new CSparqlTranslator();
        try {
            monitoringRules = XMLHelper.deserialize(getClass().getResource("/monitoring_rules_example.xml"), MonitoringRules.class);
            ruleManager = new RuleManager();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void test() {
        for (MonitoringRule rule : monitoringRules.getMonitoringRules()) {
            List<String> queriesIds = ruleManager.installRule(rule);
            for (String queryId : queriesIds) {
                validateQuery(ruleManager.getQuery(queryId));
            }
        }
    }

    private void validateQuery(CSquery query) {
        try {
            t.translate(query.toString());
        } catch (TranslationException e) {
            e.printStackTrace();
            fail();
        } catch (Exception e) {
            System.err
                    .println("Parsing was successful, the following exception was raised after parsing: "
                            + e.getClass().getName());
        }
    }

}
