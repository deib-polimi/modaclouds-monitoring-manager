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
package it.polimi.modaclouds.monitoring.monitoring_manager.configuration;

import it.polimi.modaclouds.monitoring.monitoring_manager.ConfigurationException;
import it.polimi.modaclouds.monitoring.monitoring_manager.Env;
import it.polimi.modaclouds.qos_models.schema.Metrics;
import it.polimi.modaclouds.qos_models.util.XMLHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.apache.commons.validator.routines.UrlValidator;
import org.xml.sax.SAXException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class ManagerConfig {
	
	
	private static final String actionsExecutorPath = "/ActionsExecutor";

	@Parameter(names = "-help", help = true, description = "Shows this message")
	private boolean help;

	@Parameter(names = "-ddaip", description = "DDA endpoint IP address")
	private String ddaIP;

	@Parameter(names = "-ddaport", description = "DDA endpoint port")
	private int ddaPort;

	@Parameter(names = "-kbip", description = "KB endpoint IP address")
	private String kbIP;

	@Parameter(names = "-kbport", description = "KB endpoint port")
	private int kbPort;

	@Parameter(names = "-kbpath", description = "KB URL path")
	private String kbPath;

	@Parameter(names = "-mmport", description = "Monitoring Manager endpoint port")
	private int mmPort;

	@Parameter(names = "-validmetrics", description = "The xml file containing the list of valid metrics. "
			+ "Will overwrite default ones", validateWith = FileExistsValidator.class)
	private String monitoringMetricsFileName;
	
	@Parameter(names = "-mmprivateport", description = "Monitoring Manager private endpoint Port")
	private int mmPrivatePort;

	@Parameter(names = "-mmprivateip", description = "Monitoring Manager private endpoint IP address")
	private String mmPrivateIP;
	

	private Metrics monitoringMetrics;
	private String ddaUrl;
	private String kbUrl;
	private String actionsExecutorUrl;

	public String getActionsExecutorUrl() {
		return actionsExecutorUrl;
	}

	private static ManagerConfig _instance = null;
	public static String usage = null;

	public static void init(String[] CLIargs) throws ConfigurationException {
		_instance = new ManagerConfig();
		if (CLIargs != null) {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				JCommander jc = new JCommander(_instance, CLIargs);
				jc.setProgramName("monitoring-manager");
				jc.usage(stringBuilder);
			} catch (ParameterException e) {
				throw new ConfigurationException(e.getMessage());
			}
			usage = stringBuilder.toString();
		}
		_instance.initMonitoringMetrics();
	}

	public static void init() throws ConfigurationException {
		_instance = new ManagerConfig();
		_instance.initMonitoringMetrics();
	}

	public static ManagerConfig getInstance() {
		return _instance;
	}

	private ManagerConfig() throws ConfigurationException {
		UrlValidator validator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

		try {
			ddaPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT, "8175"));
			kbPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT, "3030"));
			mmPort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_MONITORING_MANAGER_PORT, "8170"));
			mmPrivatePort = Integer.parseInt(getEnvVar(
					Env.MODACLOUDS_MONITORING_MANAGER_PRIVATE_PORT, "8070"));
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
					"The chosen port is not a valid number");
		}

		monitoringMetricsFileName = getEnvVar(
				Env.MODACLOUDS_MONITORING_MONITORING_METRICS_FILE, null);

		ddaIP = getEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP,
				"127.0.0.1");
		kbIP = getEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP, "127.0.0.1");
		mmPrivateIP = getEnvVar(
				Env.MODACLOUDS_MONITORING_MANAGER_PRIVATE_IP, "127.0.0.1");
		kbPath = getEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH,
				"/modaclouds/kb");

		ddaUrl = "http://" + ddaIP + ":" + ddaPort;
		kbUrl = "http://" + kbIP + ":" + kbPort + kbPath;
		actionsExecutorUrl = "http://" + mmPrivateIP + ":" + mmPrivatePort + actionsExecutorPath;

		if (!validator.isValid(ddaUrl))
			throw new ConfigurationException(ddaUrl + " is not a valid URL");
		if (!validator.isValid(kbUrl))
			throw new ConfigurationException(kbUrl + " is not a valid URL");

	}
	
	@Override
	public String toString() {
		return "\tDDA URL: "
				+ ddaUrl
				+ "\n"
				+ "\tKB URL: "
				+ kbUrl
				+ "\n"
				+ "\tMonitoring Manager Port: "
				+ mmPort
				+ "\n"
				+ "\tMonitoring Manager Private Port: "
				+ mmPrivatePort
				+ "\n"
				+ "\tMonitoring Manager IP Address: "
				+ mmPrivateIP
				+ (monitoringMetricsFileName == null ? ""
						: "\n\tMonitoring metrics file: "
								+ monitoringMetricsFileName);
	}

	

	public String getMonitoringMetricsFileName() {
		return monitoringMetricsFileName;
	}

	public boolean isHelp() {
		return help;
	}

	public String getDdaIP() {
		return ddaIP;
	}

	public void setDdaIP(String ddaIP) {
		this.ddaIP = ddaIP;
	}

	public int getDdaPort() {
		return ddaPort;
	}

	public void setDdaPort(int ddaPort) {
		this.ddaPort = ddaPort;
	}

	public String getKbIP() {
		return kbIP;
	}

	public void setKbIP(String kbIP) {
		this.kbIP = kbIP;
	}

	public int getKbPort() {
		return kbPort;
	}

	public void setKbPort(int kbPort) {
		this.kbPort = kbPort;
	}

	public String getKbPath() {
		return kbPath;
	}

	public void setKbPath(String kbPath) {
		this.kbPath = kbPath;
	}

	public String getDdaUrl() {
		return ddaUrl;
	}

	public void setDdaUrl(String ddaUrl) {
		this.ddaUrl = ddaUrl;
	}

	public String getKbUrl() {
		return kbUrl;
	}

	public void setKbUrl(String kbUrl) {
		this.kbUrl = kbUrl;
	}

	public int getMmPort() {
		return mmPort;
	}

	public void setMmPort(int mmPort) {
		this.mmPort = mmPort;
	}
	
	public String getMmPrivateIP() {
		return mmPrivateIP;
	}

	public int getMmPrivatePort() {
		return mmPrivatePort;
	}

	private String getEnvVar(String varName, String defaultValue) {
		String var = System.getProperty(varName);
		if (var == null)
			var = System.getenv(varName);
		if (var == null)
			var = defaultValue;
		return var;
	}

	

	private void initMonitoringMetrics() throws ConfigurationException {
		if (monitoringMetricsFileName != null) {
			InputStream is = null;
			try {
				is = new FileInputStream(monitoringMetricsFileName);
				monitoringMetrics = unmarshallMonitoringMetrics(is);
			} catch (FileNotFoundException e) {
				try {
					is = new URL(monitoringMetricsFileName).openStream();
					monitoringMetrics = unmarshallMonitoringMetrics(is);
				} catch (IOException e1) {
					throw new ConfigurationException("File "
							+ monitoringMetricsFileName + " does not exist!",
							e1);
				}
			} finally {
				close(is);
			}
		}
	}

	private Metrics unmarshallMonitoringMetrics(InputStream is)
			throws ConfigurationException {
		Metrics metrics;
		try {
			metrics = XMLHelper.deserialize(is, Metrics.class);
		} catch (JAXBException e) {
			throw new ConfigurationException("Could not open file "
					+ monitoringMetricsFileName, e);
		}
		return metrics;
	}

	private void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Metrics getMonitoringMetrics() {
		return monitoringMetrics;
	}

	public String getActionExecutorPath() {
		return actionsExecutorPath;
	}

	

}
