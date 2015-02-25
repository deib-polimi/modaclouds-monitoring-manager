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

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.monitoring.dcfactory.DCVocabulary;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.monitoring_manager.configuration.ManagerConfig;
import it.polimi.modaclouds.monitoring.monitoring_manager.server.MMServer;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class MonitoringPlatformIT {

	private static Logger logger = LoggerFactory
			.getLogger(MonitoringPlatformIT.class);
	private static MonitoringManager mm;
	private static ManagerConfig config;
	private Component component;
	private FusekiKBAPI knowledgeBaseAPI;

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
		component = new Component();
		component.getServers().add(Protocol.HTTP,
				ManagerConfig.getInstance().getMmPort());
		component.getClients().add(Protocol.FILE);
		component.getDefaultHost().attach("", new MMServer(mm, component));
		component.start();
		knowledgeBaseAPI = new FusekiKBAPI(config.getKbUrl());
	}

	@After
	public void tearDown() throws Exception {
		component.stop();
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
				knowledgeBaseAPI.getAll(DCConfig.class,
						DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).size(), 1);
		mm.uninstallRule(rules.getMonitoringRules().get(0).getId());
		assertTrue(mm.getMonitoringRules().getMonitoringRules().isEmpty());
		assertTrue(knowledgeBaseAPI.getAll(DCConfig.class,
				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).isEmpty());
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
		String observerId = mm.addObserver("AverageResponseTime",
				"http://127.0.0.1/null");
		List<Observer> observers = mm.getObservers("AverageResponseTime");
		assertEquals(observers.get(0).getId(), observerId);
		assertEquals(observers.size(), 1);
		mm.removeObserver("AverageResponseTime", observerId);
		assertTrue(mm.getObservers("AverageResponseTime").isEmpty());
	}

	@Test
	public void rulesShouldBeInstalledAndUninstalledCorrectlyThroughREST()
			throws Exception {
		given().port(config.getMmPort())
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		assertEquals(
				knowledgeBaseAPI.getAll(DCConfig.class,
						DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).size(), 1);
		String jsonMetrics = given().port(config.getMmPort())
				.get("/v1/metrics").asString();
		assertEquals(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.size(), 1);
		assertThat(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.get(0), equalToIgnoringCase("AverageResponseTime"));
		given().port(config.getMmPort())
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.delete("/v1/monitoring-rules/AvgResponseTimeRule").then()
				.assertThat().statusCode(204);
		InputStream emptyMonitoringRulesIS = given().port(config.getMmPort())
				.get("/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().isEmpty());
		given().port(config.getMmPort()).get("/v1/metrics").then().assertThat()
				.body(equalToIgnoringWhiteSpace("{\"metrics\":[]}"));
		assertTrue(knowledgeBaseAPI.getAll(DCConfig.class,
				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).isEmpty());
	}

	@Test
	public void observersShouldBeAddedAndDeletedThroughREST() throws Exception {
		given().port(config.getMmPort())
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		String callbackurl = "http://127.0.0.1/null";
		String observerId = given().port(config.getMmPort()).body(callbackurl)
				.post("/v1/metrics/AverageResponseTime/observers").andReturn()
				.body().asString();
		String jsonObservers = given().port(config.getMmPort())
				.get("/v1/metrics/AverageResponseTime/observers").asString();
		List<Observer> observers = getListFromJsonField(jsonObservers,
				Observer.class, "observers");
		assertEquals(observers.get(0).getId(), observerId);
		assertEquals(observers.get(0).getCallbackUrl(), callbackurl);
		assertEquals(observers.size(), 1);
		given().port(config.getMmPort())
				.delete("/v1/metrics/AverageResponseTime/observers/"
						+ observerId).then().assertThat().statusCode(204);
		given().port(config.getMmPort())
				.get("/v1/metrics/AverageResponseTime/observers").then()
				.assertThat()
				.body(equalToIgnoringWhiteSpace("{\"observers\":[]}"));
	}

	@Test
	public void rulesWithSameIdShouldNotBeInstalled() throws Exception {
		given().port(config.getMmPort())
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		given().port(config.getMmPort())
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(400);
		InputStream emptyMonitoringRulesIS = given().port(config.getMmPort())
				.get("/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().size() == 1);
		String jsonMetrics = given().port(config.getMmPort())
				.get("/v1/metrics").asString();
		assertEquals(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.size(), 1);
		assertThat(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.get(0), equalToIgnoringCase("AverageResponseTime"));
		assertTrue(knowledgeBaseAPI.getAll(DCConfig.class,
				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).size() == 1);
	}

	private <T> List<T> getListFromJsonField(String json, Class<T> clazz,
			String field) {
		JsonArray array = new JsonParser().parse(json).getAsJsonObject()
				.get(field).getAsJsonArray();
		List<T> returnedList = new ArrayList<T>();
		for (JsonElement observer : array) {
			returnedList.add(new Gson().fromJson(observer, clazz));
		}
		return returnedList;
	}

	private InputStream getResourceAsStream(String filenName) {
		return getClass().getClassLoader().getResourceAsStream(filenName);
	}

	

}
