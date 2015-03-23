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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.polimi.modaclouds.monitoring.monitoring_manager.server.ActionsExecutorServer.MonitoringDatum;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ActionsExecutorTest {

	@Test
	public void shouldSerializeMonitoringDatum() throws IOException {
		InputStream jsonStream = getResourceAsStream("MonitoringDatumRestCallAction.json");
		String json = IOUtils.toString(jsonStream);
		List<MonitoringDatum> data = ActionsExecutorServer.jsonToMonitoringDatum(json);
		assertNotNull(data);
		assertTrue(data.size()==1);
	}

	private InputStream getResourceAsStream(String filenName) {
		return getClass().getClassLoader().getResourceAsStream(filenName);
	}
}
