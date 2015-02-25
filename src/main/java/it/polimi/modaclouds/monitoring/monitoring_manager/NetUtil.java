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
