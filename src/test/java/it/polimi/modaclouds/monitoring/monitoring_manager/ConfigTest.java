package it.polimi.modaclouds.monitoring.monitoring_manager;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigTest {
	
	@Test
	public void defaultDDAIP() throws ConfigurationException {
		String expected = "127.0.0.1";
		Config.init();
		assertEquals(Config.getInstance().getDdaIP(),expected);
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
