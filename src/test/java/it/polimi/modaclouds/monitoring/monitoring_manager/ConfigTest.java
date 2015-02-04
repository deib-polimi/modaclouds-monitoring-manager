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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ConfigTest {
	
	@Before
	public void clearProperties(){
		System.clearProperty(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP);
		System.clearProperty(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT);
	}
	

	@Test
	public void ddaIPFromSystemPropertyShouldOverrideDefault() throws ConfigurationException {
		String expected = "100.100.100.100";
		System.setProperty(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP,expected);
		Config.init();
		assertEquals(Config.getInstance().getDdaIP(),expected);
	}
	
	@Test
	public void ddaIPFromArgsShouldOverrideSystemProperty() throws ConfigurationException {
		String expected = "100.100.100.100";
		System.setProperty(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP,"99.99.99.99");
		Config.init(new String[]{"-ddaip",expected});
		assertEquals(Config.getInstance().getDdaIP(),expected);
	}
	
	@Test(expected = ConfigurationException.class)
	public void ddaPortShouldBeANumber() throws ConfigurationException {
		System.setProperty(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT,"notaport");
		Config.init();
	}
	
	@Test(expected = ConfigurationException.class)
	public void ddaPortShouldBeAValidPort() throws ConfigurationException {
		System.setProperty(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT,"9999999");
		Config.init();
	}

}
