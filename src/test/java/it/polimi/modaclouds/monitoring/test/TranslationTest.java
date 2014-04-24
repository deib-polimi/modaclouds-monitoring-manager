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

import static org.junit.Assert.fail;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleInstallationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.CSPARQLEngineManager;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.larkc.csparql.core.new_parser.ParseException;
import eu.larkc.csparql.core.new_parser.utility_files.CSparqlTranslator;
import eu.larkc.csparql.core.new_parser.utility_files.Translator;
import eu.larkc.csparql.core.streams.formats.TranslationException;

public class TranslationTest {

//	private MonitoringRules monitoringRules;
//	private CSPARQLEngineManager ruleManager;
//	
//
//	@Before
//	public void init() {
//		try {
//			monitoringRules = XMLHelper.deserialize(
//					getClass().getResource("/mic_monitoring_rules_example.xml"),
//					MonitoringRules.class);
//			ruleManager = new CSPARQLEngineManager();
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail();
//		}
//	}
//	
//	
//
//	@Test
//	public void test() {
//		try {
//			for (MonitoringRule rule : monitoringRules.getMonitoringRules()) {
//				List<String> queriesIds = ruleManager.installRule(rule);
//				for (String queryId : queriesIds) {
//					validateQuery(ruleManager.getQuery(queryId));
//				}
//			}
//		} catch (RuleInstallationException e) {
//			e.printStackTrace();
//			fail();
//		}
//	}
//
//	public static void validateQuery(String query) {
//		Translator t = new CSparqlTranslator();
//		System.out.println(WordUtils.wrap(query, 100));
//		try {
//			t.translate(query);
//		} catch (TranslationException | ParseException e) {
//			e.printStackTrace();
//			fail();
//		} catch (Exception e) {
//			System.err
//					.println("Parsing was successful, the following exception was raised after parsing: "
//							+ e.getClass().getName());
////			e.printStackTrace();
//		}
//	}

}
