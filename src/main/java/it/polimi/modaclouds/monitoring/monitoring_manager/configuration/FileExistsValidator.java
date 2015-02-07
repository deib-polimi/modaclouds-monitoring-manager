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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

public class FileExistsValidator implements IParameterValidator {

	@Override
	public void validate(String name, String value) throws ParameterException {
		File file = getFile(value);
		if (!file.exists()) {
			URL url = getURL(value);
			if (url == null)
				throw new ParameterException("File " + value
						+ " does not exist.");
		}
	}

	private URL getURL(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	File getFile(String value) {
		return new FileConverter().convert(value);
	}

}