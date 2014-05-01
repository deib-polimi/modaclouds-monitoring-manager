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
package it.polimi.modaclouds.monitoring.mockup;

import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringManager;
import it.polimi.modaclouds.qos_models.schema.Metric;
import it.polimi.modaclouds.qos_models.schema.Metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPClient_AddObserver {

	private static Logger logger = LoggerFactory.getLogger(MPClient_AddObserver.class
			.getName());


	public static void main(String[] args) {
		try {
			MonitoringManager mp = new MonitoringManager();
			Metrics metrics = mp.getMetrics();
			for (Metric metric: metrics.getMetrics()) {
				System.out.println(metric.getName());
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
}
