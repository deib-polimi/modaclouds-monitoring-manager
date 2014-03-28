package it.polimi.modaclouds.monitoring.test;

import it.polimi.modaclouds.monitoring.monitoring_manager.MPServer;
import it.polimi.modaclouds.monitoring.monitoring_manager.MonitoringPlatform;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.VM;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OfbizDeploymentTest {

	MonitoringPlatform mp;
	
	@Before
	public void init() {
		mp = new MPServer();
		mp.start();
	}
	
	@Test
	public void test() {
		
		
		String amazonFEVMurl = "http://x.x.x.x";
		String amazonBEVMurl = "http://x.x.x.x";
		String flexiFEVMurl = "http://x.x.x.x";
		String flexiBEVMurl = "http://x.x.x.x";
		String amazonFEurl = "http://x.x.x.x:p";
		String amazonMySqlurl = "http://x.x.x.x:p";
		String flexiFEurl = "http://x.x.x.x:p";
		String flexiMySqlurl = "http://x.x.x.x:p";
		String SDAVMurl = "http://x.x.x.x";
		String SDAurl = "http://x.x.x.x:p";

		// *** AMAZON *** //

		VM amazonFrontendVM = new VM();
		amazonFrontendVM.setType("FrontendVM");
		amazonFrontendVM.setUrl(amazonFEVMurl);
		amazonFrontendVM.setCloudProvider("amazon");

		VM amazonBackendVM = new VM();
		amazonBackendVM.setType("BackendVM");
		amazonBackendVM.setUrl(amazonBEVMurl);
		amazonBackendVM.setCloudProvider("amazon");

		InternalComponent amazonJVM = new InternalComponent();
		amazonJVM.setType("JVM");
		amazonJVM.addRequiredComponent(amazonFrontendVM);

		InternalComponent amazonMySQL = new InternalComponent();
		amazonMySQL.setType("MySQL");
		amazonMySQL.setUrl(amazonMySqlurl);
		amazonJVM.addRequiredComponent(amazonBackendVM);

		InternalComponent amazonFrontend = new InternalComponent();
		amazonFrontend.setType("Frontend");
		amazonFrontend.setUrl(amazonFEurl);
		amazonFrontend.addRequiredComponent(amazonJVM);
		amazonFrontend.addRequiredComponent(amazonMySQL);
		
		amazonFrontend.addProvidedMethod(new Method("/addtocartbulk"));
		amazonFrontend.addProvidedMethod(new Method("/checkLogin"));
		amazonFrontend.addProvidedMethod(new Method("/checkoutoptions"));
		amazonFrontend.addProvidedMethod(new Method("/login"));
		amazonFrontend.addProvidedMethod(new Method("/logout"));
		amazonFrontend.addProvidedMethod(new Method("/main"));
		amazonFrontend.addProvidedMethod(new Method("/orderhistory"));
		amazonFrontend.addProvidedMethod(new Method("/quickadd"));
		
		amazonMySQL.addProvidedMethod(new Method("/create"));
		amazonMySQL.addProvidedMethod(new Method("/read"));
		amazonMySQL.addProvidedMethod(new Method("/update"));
		amazonMySQL.addProvidedMethod(new Method("/delete"));
		
		InternalComponent flexiFrontend = (InternalComponent) amazonFrontend.copyPaste2Provider("flexi");
		

	}
	
	@After
	public void tearDown() {
		mp.stop();
	}

}
