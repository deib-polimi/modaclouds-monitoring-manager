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
package it.polimi.modaclouds.monitoring.test;

import static org.junit.Assert.assertTrue;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MO;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Vocabulary;

import org.junit.Test;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

public class KbTest {

	private DatasetAccessor da = DatasetAccessorFactory.createHTTP(MO
			.getKnowledgeBaseDataURL());
//	private SPARQLServer fusekiServer;

//	@Before
//	public void init() {
//		File datasetDir = new File("target/generated-test-resources/dataset");
//		assertTrue(datasetDir.mkdirs());
//		ServerConfig config = FusekiConfig.configure(getClass().getResource(
//				"/moda_fuseki_configuration.ttl").getFile());
//		fusekiServer = new SPARQLServer(config);
//		fusekiServer.start();
//		Model coreOnt = RDFDataMgr.loadModel(getClass().getResource(
//				"/mic_ontology.ttl").getFile());
//		Model micOnt = RDFDataMgr.loadModel(getClass().getResource(
//				"/monitoring_core_ontology.ttl").getFile());
//		da.putModel(coreOnt);
//		da.add(micOnt);
//	}
//
//	@After
//	public void teardown() {
//		fusekiServer.stop();
//		try {
//			FileUtils.deleteDirectory(new File(
//					"target/generated-test-resources/dataset"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	@Test
	public void testSelectStar() {
		String queryString = "SELECT ?s ?p ?o " + "FROM <"
				+ MO.getKnowledgeBaseDataURL() + "?graph=default> "
				+ "WHERE { ?s ?p ?o . }";

		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

		QueryExecution qexec = QueryExecutionFactory.create(query);

		System.out.println(ResultSetFormatter.asText(qexec.execSelect()));
	}

	@Test
	public void testAsk() {
		String queryString = "ASK " + "FROM <" + MO.getKnowledgeBaseDataURL()
				+ "?graph=default> " + "WHERE { <" + MO.vm
				+ "> <" + RDFS.subClassOf + "> <"
				+ MO.externalComponent + "> . }";

		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

		QueryExecution qexec = QueryExecutionFactory.create(query);

		assertTrue(qexec.execAsk());
	}

	@Test
	public void testSubClassTransitivity() {
		String queryString = "ASK " + "FROM <" + MO.getKnowledgeBaseDataURL()
				+ "?graph=default> " + "WHERE { <" + MO.vm
				+ "> <" + RDFS.subClassOf + ">+ <"
				+ MO.component + "> . }";

		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

		QueryExecution qexec = QueryExecutionFactory.create(query);

		assertTrue(qexec.execAsk());
	}

	

	@Test
	public void testRequiresTransitivity() {
		Model m = ModelFactory.createDefaultModel();
		String vmInstanceURI = MO.URI + "ubuntu-vm";
		String containerInstanceURI = MO.URI + "tomcat";
		String appInstanceURI = MO.URI + "app";

		m.createResource(appInstanceURI).addProperty(
				MO.requires,
				m.createResource(containerInstanceURI).addProperty(
						MO.requires,
						m.createResource(vmInstanceURI)));

		da.add(m);

		String queryString = "ASK " + "FROM <" + MO.getKnowledgeBaseDataURL()
				+ "?graph=default> " + "WHERE { <" + appInstanceURI + "> <"
				+ MO.requires + ">+ <" + vmInstanceURI
				+ "> . }";
		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

		QueryExecution qexec = QueryExecutionFactory.create(query);

		assertTrue(qexec.execAsk());
	}
}
