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
package it.polimi.modaclouds.monitoring.monitoring_manager.server;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.*;
import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.monitoring.dcfactory.DCVocabulary;
import it.polimi.modaclouds.monitoring.kb.api.FusekiKBAPI;
import it.polimi.modaclouds.monitoring.monitoring_manager.NetUtil;
import it.polimi.modaclouds.monitoring.monitoring_manager.Observer;
import it.polimi.modaclouds.monitoring.monitoring_manager.server.Model;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.restassured.RestAssured;

public class MonitoringManagerRemoteIT {

	private static final int DDA_PORT = 8175;
	private static final int KB_PORT = 3030;
	private static final int MM_PORT = 8170;
	private static final int MM_PRIVATE_PORT = 8070;
	private static final String knowledgeBaseURL = "http://localhost:3030/modaclouds/kb";
	private FusekiKBAPI knowledgeBaseAPI = new FusekiKBAPI(knowledgeBaseURL);

	private static Logger logger = LoggerFactory
			.getLogger(MonitoringManagerRemoteIT.class);

	@Before
	public void setUp() throws Exception {
		NetUtil.waitForResponseCode("http://localhost:" + KB_PORT, 200, 5, 5000);
		NetUtil.waitForResponseCode(
				"http://localhost:" + DDA_PORT + "/queries", 200, 5, 5000);
		resetPlatform();
		RestAssured.urlEncodingEnabled = false;
	}

	private void resetPlatform() throws JAXBException, SAXException {
		InputStream response = given()
				.get("http://localhost:8170/v1/monitoring-rules").andReturn()
				.asInputStream();
		MonitoringRules rules = XMLHelper.deserialize(response,
				MonitoringRules.class);
		for (MonitoringRule rule : rules.getMonitoringRules()) {
			given().delete(
					"http://localhost:8170/v1/monitoring-rules/" + rule.getId())
					.then().statusCode(204);
		}
		String modeljson = given()
				.get("http://localhost:8170/v1/model/resources").andReturn()
				.asString();
		Model model = new Gson().fromJson(modeljson, Model.class);

		for (Resource res : model.getResources()) {
			given().delete(
					"http://localhost:8170/v1/model/resources/" + res.getId())
					.then().statusCode(204);
		}
	}

	@After
	public void tearDown() throws Exception {
		resetPlatform();
	}

	@Test
	public void csparqlShouldBeClear() throws Exception {
		given().port(DDA_PORT).get("/queries").then()
				.body("$", emptyIterable());
		given().port(DDA_PORT).get("/streams").then()
				.body("$", emptyIterable());
	}

	@Test
	public void kbDCGraphShouldBeEmpty() throws Exception {
		given().port(KB_PORT)
				.param("graph",
						FusekiKBAPI
								.getGraphURI(DCVocabulary.DATA_COLLECTORS_GRAPH_NAME))
				.get("/modaclouds/kb/data").then().statusCode(404);
	}

	@Test
	public void rulesShouldBeInstalledAndUninstalledCorrectlyThroughREST()
			throws Exception {
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		assertEquals(
				knowledgeBaseAPI.getAll(DCConfig.class,
						DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).size(), 1);
		String jsonMetrics = given().port(MM_PORT).get("/v1/metrics")
				.asString();
		assertEquals(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.size(), 1);
		assertThat(getListFromJsonField(jsonMetrics, String.class, "metrics")
				.get(0), equalToIgnoringCase("AverageResponseTime"));
		given().port(MM_PORT)
				.delete("/v1/monitoring-rules/AvgResponseTimeRule").then()
				.assertThat().statusCode(204);
		InputStream emptyMonitoringRulesIS = given().port(MM_PORT)
				.get("/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().isEmpty());
		given().port(MM_PORT).get("/v1/metrics").then().assertThat()
				.body(equalToIgnoringWhiteSpace("{\"metrics\":[]}"));
		assertTrue(knowledgeBaseAPI.getAll(DCConfig.class,
				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).isEmpty());
		given().port(DDA_PORT).get("/queries").then()
				.body("$", emptyIterable());
		given().port(DDA_PORT).get("/streams").then()
				.body("$", emptyIterable());
	}

	@Test
	public void atosRulesShouldBeInstalledAndUninstalledCorrectlyThroughREST()
			throws Exception {
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("AtosMonitoringRules.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		given().port(MM_PORT)
				.delete("/v1/monitoring-rules/4d1ad207-6b4d-4aed-bee1-cb0fcb1629dc_79127a7e-c2d9-4463-93de-20e422a2d4be_2468ecf1-18e3-42ef-af4b-a94e3d78d823_b780c3f4-298c-4114-8d86-074b81374b4c_seff")
				.then().assertThat().statusCode(204);
		given().port(MM_PORT)
				.delete("/v1/monitoring-rules/4d1ad207-6b4d-4aed-bee1-cb0fcb1629dc_79127a7e-c2d9-4463-93de-20e422a2d4be_2468ecf1-18e3-42ef-af4b-a94e3d78d823_b780c3f4-298c-4114-8d86-074b81374b4c_seff")
				.then().assertThat().statusCode(404);
	}

	@Test
	public void observersShouldBeAddedAndDeletedThroughREST() throws Exception {
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		String callbackurl = "http://127.0.0.1/null";
		Observer registeredObserver = given().port(MM_PORT).body(callbackurl)
				.post("/v1/metrics/AverageResponseTime/observers").andReturn()
				.body().as(Observer.class);
		String jsonObservers = given().port(MM_PORT)
				.get("/v1/metrics/AverageResponseTime/observers").asString();
		List<Observer> observers = getListFromJsonField(jsonObservers,
				Observer.class, "observers");
		assertEquals(observers.get(0).getId(), registeredObserver.getId());
		assertEquals(observers.get(0).getCallbackUrl(), callbackurl);
		assertNull(observers.get(0).getQueryUri());
		assertEquals(observers.size(), 1);
		given().port(MM_PORT)
				.delete("/v1/metrics/AverageResponseTime/observers/"
						+ registeredObserver.getId()).then().assertThat()
				.statusCode(204);
		given().port(MM_PORT).get("/v1/metrics/AverageResponseTime/observers")
				.then().assertThat()
				.body(equalToIgnoringWhiteSpace("{\"observers\":[]}"));
	}

	@Test
	public void rulesWithSameIdShouldNotBeInstalled() throws Exception {
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(400);
		InputStream emptyMonitoringRulesIS = given().port(MM_PORT)
				.get("/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().size() == 1);
		String jsonMetrics = given().port(MM_PORT).get("/v1/metrics")
				.asString();
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

	@Test
	public void restCallRuleShouldSelfDestroy() throws Exception {
		Method method = new Method();
		method.setId("register1");
		method.setType("register");
		Model model = new Model();
		model.add(method);
		given().port(MM_PORT).body(new Gson().toJson(model))
				.post("/v1/model/resources").then().assertThat()
				.statusCode(204);
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("RestCallRule4SelfDestroy.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);

		// TODO temp fix: first datum is ignored by csparql engine
		given().body(
				IOUtils.toString(getResourceAsStream("MonitoringDatumRT.json")))
				.port(DDA_PORT)
				.post("/streams/"
						+ URLEncoder
								.encode("http://www.modaclouds.eu/streams/responsetime",
										"UTF-8")).then().assertThat()
				.statusCode(200);

		// TODO temp fix: some time not to have this datum ignored as well
		Thread.sleep(1000);
		given().body(
				IOUtils.toString(getResourceAsStream("MonitoringDatumRT.json")))
				.port(DDA_PORT)
				.post("/streams/"
						+ URLEncoder
								.encode("http://www.modaclouds.eu/streams/responsetime",
										"UTF-8")).then().assertThat()
				.statusCode(200);

		// The rule have a 5 seconds period
		Thread.sleep(10000);
		InputStream emptyMonitoringRulesIS = given().port(MM_PORT)
				.get("/v1/monitoring-rules").asInputStream();
		assertTrue(XMLHelper
				.deserialize(emptyMonitoringRulesIS, MonitoringRules.class)
				.getMonitoringRules().isEmpty());
		assertTrue(knowledgeBaseAPI.getAll(DCConfig.class,
				DCVocabulary.DATA_COLLECTORS_GRAPH_NAME).isEmpty());
		given().port(DDA_PORT).get("/queries").then()
				.body("$", emptyIterable());
		given().port(DDA_PORT).get("/streams").then()
				.body("$", emptyIterable());
	}

	@Test
	public void registeringWrongObserverUrlShouldFail() throws Exception {
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("AvgResponseTimeRule.xml")))
				.post("/v1/monitoring-rules").then().assertThat()
				.statusCode(204);
		String callbackurl = "localhost/null";
		given().port(MM_PORT).body(callbackurl)
				.post("/v1/metrics/AverageResponseTime/observers").then()
				.assertThat().statusCode(400);
	}

	@Test
	public void correctModelShouldBeUploaded() throws Exception {
		given().port(MM_PORT)
				.body(IOUtils
						.toString(getResourceAsStream("mic-deployment.json")))
				.post("/v1/model/resources").then().assertThat()
				.statusCode(204);
	}

}
