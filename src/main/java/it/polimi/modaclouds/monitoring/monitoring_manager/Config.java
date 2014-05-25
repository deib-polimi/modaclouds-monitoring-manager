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

import it.polimi.modaclouds.qos_models.schema.AggregateFunctions;
import it.polimi.modaclouds.qos_models.schema.GroupingCategories;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	private static Config _instance = null;
	private static final Logger logger = LoggerFactory.getLogger(Config.class);

	private PropertiesConfiguration config;
	private AggregateFunctions availableAggregateFunctions;
	private GroupingCategories availableGroupingClasses;

	private static final String monitoringManagerPropertiesFileName = "monitoring_manager.properties";
	private static final String monitoringAggregateFunctionsFileName = "monitoring_aggregate_functions.xml";
	private static final String monitoringGroupingClassesFileName = "monitoring_grouping_categories.xml";

	private Config() throws ConfigurationException {
		ClassLoader cl = this.getClass().getClassLoader();
		try {
			config = new PropertiesConfiguration(monitoringManagerPropertiesFileName);
			availableAggregateFunctions = XMLHelper
					.deserialize(
							cl.getResourceAsStream(monitoringAggregateFunctionsFileName),
							AggregateFunctions.class);
			availableGroupingClasses = XMLHelper
					.deserialize(
							cl.getResourceAsStream(monitoringGroupingClassesFileName),
							GroupingCategories.class);
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}

		// URL monitoringManagerPropertiesURL =
		// getURL(monitoringManagerPropertiesURLName);
//		URL monitoringAggregateFunctionsURL = getURL(monitoringAggregateFunctionsURLName);
//		URL monitoringGroupingCategoriesURL = getURL(monitoringGroupingCategoriesURLName);
		// if (monitoringManagerPropertiesURL != null) {
		// try {
		// config = new PropertiesConfiguration(monitoringManagerPropertiesURL);
		// } catch (Exception e) {
		// }
		// }
		// if (config == null) {
		// logger.warn("Could not load " + monitoringManagerPropertiesURLName +
		// ".");
		// }
//		if (monitoringAggregateFunctionsURLName != null) {
//			try {
//				availableAggregateFunctions = XMLHelper.deserialize(
//						monitoringAggregateFunctionsURL,
//						AggregateFunctions.class);
//			} catch (Exception e) {
//			}
//		}
//		if (availableAggregateFunctions == null) {
//			throw new ConfigurationException("Could not load "
//					+ monitoringAggregateFunctionsURLName + ".");
//		}
//		if (monitoringGroupingCategoriesURLName != null) {
//			try {
//				availableGroupingClass = XMLHelper.deserialize(
//						monitoringGroupingCategoriesURL,
//						GroupingCategories.class);
//			} catch (Exception e) {
//			}
//		}
//		if (availableGroupingClasses == null) {
//			throw new ConfigurationException("Could not load "
//					+ monitoringGroupingClassesURLName + ".");
//		}
	}

	public static Config getInstance() throws ConfigurationException {
		if (_instance == null)
			_instance = new Config();
		return _instance;
	}

	public int getDDAServerPort() {
		return config.getInt("dda_server.port");
	}

	public String getDDAServerAddress() {
		return config.getString("dda_server.address");
	}

	public AggregateFunctions getAvailableAggregateFunctions() {
		return availableAggregateFunctions;
	}

//	public static URL getURL(String URLName) {
//		boolean exists = false;
//		URL url = null;
//		try {
//			url = Config.class.getResource(URLName);
//			exists = new File(url.toURI()).exists();
//		} catch (Exception e) {
//			logger.error("Error checking if file exists from URLName", e);
//			exists = false;
//		}
//		if (exists)
//			return url;
//		else {
//			try {
//				String alternativeURLName = new File(Config.class
//						.getProtectionDomain().getCodeSource().getLocation()
//						.getPath()).getParent()
//						+ URLName;
//
//				url = new File(alternativeURLName).toURI().toURL();
//				exists = new File(url.toURI()).exists();
//			} catch (Exception e) {
//				logger.error(
//						"Error checking if file exists from alternativeURLName",
//						e);
//				exists = false;
//			}
//		}
//		return null;
//	}

	public GroupingCategories getAvailableGroupingClasses() {
		return availableGroupingClasses;
	}

	public int getMMServerPort() {
		return config.getInt("mm_server.port");
	}

	public String getMatlabSDAServerAddress() {
		return config.getString("matlab_sda_server.address");
	}

	public int getMatlabSDAServerPort() {
		return config.getInt("matlab_sda_server.port");
	}

	public String getJavaSDAServerAddress() {
		return config.getString("java_sda_server.address");
	}

	public int getJavaSDAServerPort() {
		return config.getInt("java_sda_server.port");
	}

}
