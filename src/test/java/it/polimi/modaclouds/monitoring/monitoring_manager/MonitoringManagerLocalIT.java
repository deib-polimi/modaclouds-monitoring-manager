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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.monitoring.dcfactory.DCVocabulary;
import it.polimi.modaclouds.monitoring.kb.api.DeserializationException;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.kb.api.SerializationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.configuration.ManagerConfig;
import it.polimi.modaclouds.monitoring.monitoring_manager.server.Model;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.io.InputStream;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MonitoringManagerLocalIT {

	private static MonitoringManager mm;
	private static ManagerConfig config;

	@Before
	public void setUp() throws Exception {
		ManagerConfig.init();
		config = ManagerConfig.getInstance();
		NetUtil.waitForResponseCode(
				"http://" + config.getKbIP() + ":" + config.getKbPort(), 200,
				5, 5000);
		NetUtil.waitForResponseCode(config.getDdaUrl() + "/queries", 200, 5,
				5000);
		mm = new MonitoringManager(config);
	}

	@After
	public void tearDown() throws Exception {
		System.clearProperty(Env.MODACLOUDS_MONITORING_MANAGER_PORT);
		System.clearProperty(Env.MODACLOUDS_MONITORING_MANAGER_PRIVATE_PORT);
		mm.knowledgeBase.clearAll();
		mm.csparqlEngineManager.clearAll();
	}

	@Test
	public void csparqlShouldBeClear() throws Exception {
		given().port(config.getDdaPort()).get("/queries").then()
				.body("$", emptyIterable());
		given().port(config.getDdaPort()).get("/streams").then()
				.body("$", emptyIterable());
	}

	@Test
	public void kbDCGraphShouldBeEmpty() throws Exception {
		given().port(config.getKbPort())
				.param("graph",
						FusekiKBAPI
								.getGraphURI(DCVocabulary.DATA_COLLECTORS_GRAPH_NAME))
				.get(config.getKbPath() + "/data").then().statusCode(404);
	}

	@Test
	public void rulesShouldBeInstalledAndUninstalledCorrectly()
			throws Exception {
		MonitoringRules rules = XMLHelper.deserialize(
				getResourceAsStream("AvgResponseTimeRule.xml"),
				MonitoringRules.class);
		mm.installRules(rules);
		assertEquals(
				mm.knowledgeBase.getAll(DCConfig.class,
						DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).size(), 1);
		mm.uninstallRule(rules.getMonitoringRules().get(0).getId());
		assertTrue(mm.getMonitoringRules().getMonitoringRules().isEmpty());
		assertTrue(mm.knowledgeBase.getAll(DCConfig.class,
				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).isEmpty());
		given().port(config.getDdaPort()).get("/queries").then()
				.body("$", emptyIterable());
		given().port(config.getDdaPort()).get("/streams").then()
				.body("$", emptyIterable());
	}

	@Test
	public void observersShouldBeAddedAndDeleted() throws Exception {
		MonitoringRules rules = XMLHelper.deserialize(
				getResourceAsStream("AvgResponseTimeRule.xml"),
				MonitoringRules.class);
		assertEquals(rules.getMonitoringRules().size(), 1);
		mm.installRules(rules);
		assertEquals(mm.getMetrics().size(), 1);
		assertThat(mm.getMetrics().iterator().next(),
				equalToIgnoringCase("AverageResponseTime"));
		Observer observer = mm.addObserver("AverageResponseTime",
				"http://127.0.0.1/null");
		Set<Observer> observers = mm.getObservers("AverageResponseTime");
		assertEquals(observers.iterator().next().getId(), observer.getId());
		assertEquals(observers.size(), 1);
		mm.removeObserver("AverageResponseTime", observer.getId());
		assertTrue(mm.getObservers("AverageResponseTime").isEmpty());
	}

	private InputStream getResourceAsStream(String filenName) {
		return getClass().getClassLoader().getResourceAsStream(filenName);
	}

	@Test
	public void ruleWithAggregationInValueShouldBeInstalledCorrectly()
			throws Exception {
		MonitoringRules rules = XMLHelper.deserialize(
				getResourceAsStream("EffectiveResponseTimeRule.xml"),
				MonitoringRules.class);
		mm.installRules(rules);
		assertFalse(mm.getMonitoringRules().getMonitoringRules().isEmpty());
	}

	@Test
	public void uploadedModelShouldBeRetrieved() throws SerializationException,
			DeserializationException {
		Method method = new Method();
		method.setId("register1");
		method.setType("register");
		Model model = new Model();
		model.add(method);
		mm.uploadModel(model);
		Model retrievedModel = mm.getCurrentModel();
		assertNotNull(retrievedModel);
		assertTrue(retrievedModel.getMethods() != null);
		assertTrue(retrievedModel.getMethods().size() == 1);
		Method retrievedMethod = retrievedModel.getMethods().iterator().next();
		assertEquals(method.getId(), retrievedMethod.getId());
		assertEquals(method.getType(), retrievedMethod.getType());
	}

	@Test(expected = RuleInstallationException.class)
	public void wrongRuleInstallationShouldGiveValuableInformation()
			throws Exception {
		MonitoringRules rules = XMLHelper
				.deserialize(getResourceAsStream("WrongAtosRule.xml"),
						MonitoringRules.class);
		mm.installRules(rules);
	}

//	@Test
//	public void restCallRuleShouldSelfDestroy() throws Exception {
//		mm.installRules(XMLHelper.deserialize(
//				getResourceAsStream("RestCallRule4SelfDestroy.xml"),
//				MonitoringRules.class));
//		AbstractAction action = mm.getActionImplByRuleId("RestCallRule");
//		action.execute("register1", "2000", "918724871264871264");
//		assertTrue(mm.getMonitoringRules().getMonitoringRules().isEmpty());
//		assertTrue(mm.knowledgeBase.getAll(DCConfig.class,
//				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).isEmpty());
//		given().port(config.getDdaPort()).get("/queries").then()
//				.body("$", emptyIterable());
//		given().port(config.getDdaPort()).get("/streams").then()
//				.body("$", emptyIterable());
//	}
}
