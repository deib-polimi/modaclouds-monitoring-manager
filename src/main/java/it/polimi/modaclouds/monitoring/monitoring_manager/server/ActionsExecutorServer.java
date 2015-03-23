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

import it.polimi.modaclouds.monitoring.dcfactory.wrappers.DDAOntology;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.qos_models.monitoring_rules.AbstractAction;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class ActionsExecutorServer extends ServerResource {

	private static Logger logger = LoggerFactory
			.getLogger(ActionsExecutorServer.class);

	@Post
	public void performAction(Representation rep) {
		try {
			String json = rep.getText();
			logger.debug("Received json object: {}", json);
			MonitoringManager manager = (MonitoringManager) getContext()
					.getAttributes().get("manager");
			List<MonitoringDatum> monitoringData = jsonToMonitoringDatum(json);
			if (!monitoringData.isEmpty()) {
				MonitoringDatum datum = monitoringData.get(0);
				String metric = datum.metric;
				String ruleId = metric.substring(metric.indexOf("_") + 1);
				AbstractAction actionImpl = manager
						.getActionImplByRuleId(ruleId);
				if (actionImpl != null) {
					logger.info("Action {} requested by rule {}",
							actionImpl.getName(), ruleId);
					actionImpl.execute(datum.resourceId, datum.value,
							datum.timestamp);
					this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
				} else {
					logger.error(
							"An action request was requested by (supposingly) rule {}. "
									+ "However such rule is not installed in the platform.",
							ruleId);
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
							"Rule " + ruleId + " doesn't exist");
				}
			} else {
				logger.warn("Empty monitoring data json object received");
				this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
			}
		} catch (Exception e) {
			logger.error("Error while trying to execute action", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,
					e.getMessage());
			this.getResponse().setEntity(
					"Error while trying to execute action: " + e.toString(),
					MediaType.TEXT_PLAIN);
		} finally {
			this.getResponse().commit();
			this.commit();
			this.release();
		}
	}

	static List<MonitoringDatum> jsonToMonitoringDatum(String json)
			throws IOException {
		JsonReader reader = new JsonReader(new StringReader(json));
		Type type = new TypeToken<Map<String, Map<String, List<Map<String, String>>>>>() {
		}.getType();
		Map<String, Map<String, List<Map<String, String>>>> jsonMonitoringData = new Gson()
				.fromJson(reader, type);
		List<MonitoringDatum> monitoringData = new ArrayList<MonitoringDatum>();
		if (jsonMonitoringData.isEmpty()) {
			return monitoringData;
		}
		for (Map<String, List<Map<String, String>>> jsonMonitoringDatum : jsonMonitoringData
				.values()) {
			MonitoringDatum datum = new MonitoringDatum();
			datum.setMetric(nullable(
					jsonMonitoringDatum.get(DDAOntology.metric.toString()))
					.get(0).get("value"));
			datum.setTimestamp(nullable(
					jsonMonitoringDatum.get(DDAOntology.timestamp.toString()))
					.get(0).get("value"));
			datum.setValue(nullable(
					jsonMonitoringDatum.get(DDAOntology.value.toString())).get(
					0).get("value"));
			datum.setResourceId(nullable(
					jsonMonitoringDatum.get(DDAOntology.resourceId.toString()))
					.get(0).get("value"));
			monitoringData.add(datum);
		}
		return monitoringData;
	}

	private static List<Map<String, String>> nullable(
			List<Map<String, String>> list) {
		if (list != null)
			return list;
		else {
			List<Map<String, String>> emptyValueList = new ArrayList<Map<String, String>>();
			Map<String, String> emptyValueMap = new HashMap<String, String>();
			emptyValueMap.put("value", "");
			emptyValueList.add(emptyValueMap);
			return emptyValueList;
		}
	}

	static class MonitoringDatum {

		String resourceId;
		String metric;
		String timestamp;
		String value;

		public String getResourceId() {
			return resourceId;
		}

		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}

		public String getMetric() {
			return metric;
		}

		public void setMetric(String metric) {
			this.metric = metric;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "MonitoringDatum [resourceId=" + resourceId + ", metric="
					+ metric + ", timestamp=" + timestamp + ", value=" + value
					+ "]";
		}

	}

}
