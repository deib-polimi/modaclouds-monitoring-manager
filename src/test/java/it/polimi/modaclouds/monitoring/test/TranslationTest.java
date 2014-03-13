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
import it.polimi.csparqool.CSquery;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

//import eu.larkc.csparql.core.parser.CSparqlTranslator;
//import eu.larkc.csparql.core.parser.Translator;
//import eu.larkc.csparql.core.streams.formats.TranslationException;

public class TranslationTest {

	private List<MonitoringRule> monitoringRules;
//	private Translator t;

	@Before
	public void loadXMLString() {
//		t = new CSparqlTranslator();
//		try {
//			String mrXmlText = XMLHelper.getXMLText(
//					getClass().getResource("/monitoring_rules_SpecWeb.xml"),
//					"UTF-8");
//			monitoringRules = XMLHelper.getXMLBlocks(mrXmlText,
//					"monitoringRules/monitoringRule");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail();
//		}
	}

	@Test
	public void test() {
//		for (String rule : monitoringRules) {
//			CSquery query = RuleManager.translate(rule);
//			validateQuery(query);
//		}
	}

	private void validateQuery(CSquery query) {
//		try {
//			t.translate(query.toString());
//		} catch (TranslationException e) {
//			e.printStackTrace();
//			fail();
//		} catch (Exception e) {
//			System.err
//					.println("Parsing was successful, the following exception was raised after parsing: "
//							+ e.getClass().getName());
//		}
	}

}
