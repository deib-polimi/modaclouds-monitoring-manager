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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringPlatformIT {

	// @BeforeClass
	// public static void setupMM() throws Exception{
	// Config config = Config.getInstance();
	// MonitoringManager mm = new MonitoringManager(config);
	// }

	private Logger logger = LoggerFactory.getLogger(MonitoringPlatformIT.class);

	@Test
	public void kbIsUp() throws InterruptedException {
		if (!serviceIsUp(3030, "/", 3, "KB"))
			fail();
		int statusCode = given().port(3030).get("/").statusCode();
		assertEquals(statusCode, 200);
	}

	@Test
	public void ddaIsUp() throws InterruptedException {
		if (!serviceIsUp(8175, "/queries", 3, "DDA"))
			fail();
		int statusCode = given().port(8175).get("/queries").statusCode();
		assertEquals(statusCode, 200);
	}

	private boolean serviceIsUp(int port, String path) {
		boolean isUp = true;
		try {
			given().port(port).get(path);
		} catch (Exception e) {
			isUp = false;
		}
		return isUp;
	}

	private boolean serviceIsUp(int port, String path, int retry,
			String serviceName) {
		while (!serviceIsUp(port, path)) {
			if (retry <= 0) {
				logger.error("{} unreachable", serviceName);
				return false;
			}
			logger.info("{}: {} unreachable, retrying in 5 seconds...",
					--retry, serviceName);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				fail();
			}
		}
		return true;
	}

}
