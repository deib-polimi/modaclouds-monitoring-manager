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

import org.apache.commons.validator.routines.UrlValidator;


public class Config {
	
	private static Config _instance = null;

	public static Config getInstance() throws ConfigurationException {
		if (_instance == null)
			_instance = new Config();
		return _instance;
	}

	private UrlValidator validator;
	private String ddaIP;
	private String ddaPort;
	private String kbIP;
	private String kbPort;
	private String kbPath;
	private String ddaUrl;
	private String kbUrl;
	private String mmPort;
	private String matlabSdaIP;
	private String matlabSdaPort;
	private String javaSdaIP;
	private String javaSdaPort;
	
	private Config() throws ConfigurationException{
		validator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
		ddaIP = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP);
		ddaPort = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT);
		kbIP = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP);
		kbPort = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT);
		kbPath = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH);
		mmPort = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_MANAGER_PORT);
		matlabSdaIP = getMandatoryEnvVar(Env.MODACLOUDS_MATLAB_SDA_IP);
		matlabSdaPort = getMandatoryEnvVar(Env.MODACLOUDS_MATLAB_SDA_PORT);
		javaSdaIP = getMandatoryEnvVar(Env.MODACLOUDS_WEKA_SDA_IP);
		javaSdaPort = getMandatoryEnvVar(Env.MODACLOUDS_WEKA_SDA_PORT);
		
		ddaUrl = "http://" + ddaIP + ":" + ddaPort;
		kbUrl = "http://" + kbIP + ":" + kbPort + kbPath;
		
		if (!validator.isValid(ddaUrl))
			throw new ConfigurationException(ddaUrl + " is not a valid URL");
		if (!validator.isValid(kbUrl))
			throw new ConfigurationException(kbUrl + " is not a valid URL");
		
	}

	public String getDdaPort() {
		return ddaPort;
	}

	public String getDdaIP() {
		return ddaIP;
	}

	public String getMmPort() {
		return mmPort;
	}

	public String getMatlabSdaIP() {
		return matlabSdaIP;
	}

	public String getMatlabSdaPort() {
		return matlabSdaPort;
	}

	public String getJavaSdaIP() {
		return javaSdaIP;
	}

	public String getJavaSdaPort() {
		return javaSdaPort;
	}
	public String getKbUrl() {
		return kbUrl;
	}
	
	
	private String getMandatoryEnvVar(String varName)
			throws ConfigurationException {
		String var = System.getProperty(varName);
		if (var == null) {
			var = System.getenv(varName);
		}
		if (var == null) {
			throw new ConfigurationException(varName
					+ " variable was not defined");
		}
		return var;
	}

}
