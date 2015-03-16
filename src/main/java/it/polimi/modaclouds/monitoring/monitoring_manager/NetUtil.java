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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetUtil {

	private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

	public static boolean isResponseCode(String url, int expectedCode)
			throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		return connection.getResponseCode() == expectedCode;
	}

	public static void waitForResponseCode(String url, int expectedCode,
			int retryTimes, int retryPeriodInMilliseconds) throws IOException {
		while (!isResponseCode(url, expectedCode)) {
			retryTimes--;
			if (retryTimes <= 0) {
				throw new IOException();
			}
			try {
				logger.info("Service is down, retrying in {} seconds...", retryPeriodInMilliseconds/1000);
				Thread.sleep(retryPeriodInMilliseconds);
			} catch (InterruptedException e) {
				throw new IOException();
			}
		}
	}

}
