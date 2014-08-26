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
package it.polimi.modaclouds.monitoring.deployment_examples;

import it.polimi.modaclouds.monitoring.monitoring_manager.server.Model;
import it.polimi.modaclouds.qos_models.monitoring_ontology.CloudProvider;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;

import java.net.URISyntaxException;

import com.google.gson.Gson;

public class ICSE2015Deployment {

	public static void main(String[] args) {

		Model model = new Model();
		try {

			CloudProvider amazonCloud = new CloudProvider();
			model.add(amazonCloud);
			amazonCloud.setId("amazon");
			amazonCloud.setType("IaaS");

			VM amazonFrontendVM = new VM();
			model.add(amazonFrontendVM);
			amazonFrontendVM.setId("frontend1");
			amazonFrontendVM.setType("Frontend");
			amazonFrontendVM.setCloudProvider(amazonCloud.getId());

			InternalComponent amazonMic = new InternalComponent();
			model.add(amazonMic);
			amazonMic.setId("mic1");
			amazonMic.setType("Mic");
			amazonMic.addRequiredComponent(amazonFrontendVM.getId());

			model.add(addMethod(amazonMic, "register"));
			model.add(addMethod(amazonMic, "saveAnswers"));
			model.add(addMethod(amazonMic, "answerQuestions"));
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Gson gson = new Gson();
		String json = gson.toJson(model);
		System.out.println(json);
	}

	private static Method addMethod(InternalComponent iComponent, String methodType)
			throws URISyntaxException {
		Method method = new Method(iComponent.getId(), methodType);
		iComponent.addProvidedMethod(method.getId());
		return method;
	}

}
