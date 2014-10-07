package it.polimi.modaclouds.monitoring.monitoring_manager.test;

import static org.junit.Assert.fail;

import java.util.Set;

import it.polimi.csparqool.CSquery;
import it.polimi.csparqool.MalformedQueryException;
import it.polimi.modaclouds.monitoring.monitoring_manager.CSPARQLEngineManager;
import it.polimi.modaclouds.monitoring.monitoring_manager.RuleInstallationException;
import it.polimi.modaclouds.qos_models.monitoring_rules.ConfigurationException;
import it.polimi.modaclouds.qos_models.monitoring_rules.Problem;
import it.polimi.modaclouds.qos_models.monitoring_rules.Validator;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.Actions;
import it.polimi.modaclouds.qos_models.schema.CollectedMetric;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoredTargets;
import it.polimi.modaclouds.qos_models.schema.MonitoringMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.Parameter;

import org.junit.Test;

public class Rule2CSPARQLTest {

	@Test
	public void test1() {
		MonitoringRule rule = new MonitoringRule();
		
		rule.setId("queryTest1");
		
		Actions actions = new Actions();
		Action action = new  Action();
		action.setName("OutputMetric");
		Parameter actionParameter1 = new Parameter();
		actionParameter1.setName("metric");
		actionParameter1.setValue("myMetric");
		action.getParameters().add(actionParameter1);
		Parameter actionParameter2 = new Parameter();
		actionParameter2.setName("resourceId");
		actionParameter2.setValue("ID");
		action.getParameters().add(actionParameter2);
		Parameter actionParameter3 = new Parameter();
		actionParameter3.setName("value");
		actionParameter3.setValue("METRIC");
		action.getParameters().add(actionParameter3);
		actions.getActions().add(action);
		rule.setActions(actions);
		
		rule.setTimeStep("60");
		rule.setTimeWindow("60");
		
		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName("ResponseTime");
		Parameter metricParameter = new Parameter();
		metricParameter.setName("samplingProbability");
		metricParameter.setValue("1.0");
		collectedMetric.getParameters().add(metricParameter);
		rule.setCollectedMetric(collectedMetric);
		
		MonitoredTarget target = new MonitoredTarget();
		target.setClazz("Method");
		target.setType("Login");
		MonitoredTargets targets = new MonitoredTargets();
		targets.getMonitoredTargets().add(target);
		rule.setMonitoredTargets(targets);
		
		try {
			Validator validator = new Validator();
			Set<Problem> problems = validator.validateRule(rule, null);
			if (problems.isEmpty()) {
				CSquery query = CSPARQLEngineManager.createQueryFromRule(rule, "Test1", "http://localhost:3030/modaclouds/kb");
				System.out.println(query.getCSPARQL());
			} else {
				for (Problem p: problems) {
					System.out.println(p.toString());
				}
				fail();
			}
			
		} catch (MalformedQueryException | RuleInstallationException | ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() {
		MonitoringRule rule = new MonitoringRule();
		
		rule.setId("queryTest2");
		
		Actions actions = new Actions();
		Action action = new  Action();
		action.setName("OutputMetric");
		Parameter actionParameter1 = new Parameter();
		actionParameter1.setName("metric");
		actionParameter1.setValue("myMetric");
		action.getParameters().add(actionParameter1);
		Parameter actionParameter2 = new Parameter();
		actionParameter2.setName("resourceId");
		actionParameter2.setValue("allLogins");
		action.getParameters().add(actionParameter2);
		Parameter actionParameter3 = new Parameter();
		actionParameter3.setName("value");
		actionParameter3.setValue("METRIC");
		action.getParameters().add(actionParameter3);
		actions.getActions().add(action);
		rule.setActions(actions);
		
		rule.setTimeStep("60");
		rule.setTimeWindow("60");
		
		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName("ResponseTime");
		Parameter metricParameter = new Parameter();
		metricParameter.setName("samplingProbability");
		metricParameter.setValue("1.0");
		collectedMetric.getParameters().add(metricParameter);
		rule.setCollectedMetric(collectedMetric);
		
		MonitoredTarget target = new MonitoredTarget();
		target.setClazz("Method");
		target.setType("Login");
		MonitoredTargets targets = new MonitoredTargets();
		targets.getMonitoredTargets().add(target);
		rule.setMonitoredTargets(targets);
		
		MonitoringMetricAggregation aggregation = new MonitoringMetricAggregation();
		aggregation.setAggregateFunction("Average");
		rule.setMetricAggregation(aggregation);
		
		try {
			Validator validator = new Validator();
			Set<Problem> problems = validator.validateRule(rule, null);
			if (problems.isEmpty()) {
				CSquery query = CSPARQLEngineManager.createQueryFromRule(rule, "Test2", "http://localhost:3030/modaclouds/kb");
				System.out.println(query.getCSPARQL());
			} else {
				for (Problem p: problems) {
					System.out.println(p.toString());
				}
				fail();
			}
			
		} catch (MalformedQueryException | RuleInstallationException | ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test3() {
		MonitoringRule rule = new MonitoringRule();
		
		rule.setId("queryTest3");
		
		Actions actions = new Actions();
		Action action = new  Action();
		action.setName("OutputMetric");
		Parameter actionParameter1 = new Parameter();
		actionParameter1.setName("metric");
		actionParameter1.setValue("myMetric");
		action.getParameters().add(actionParameter1);
		Parameter actionParameter2 = new Parameter();
		actionParameter2.setName("resourceId");
		actionParameter2.setValue("ID");
		action.getParameters().add(actionParameter2);
		Parameter actionParameter3 = new Parameter();
		actionParameter3.setName("value");
		actionParameter3.setValue("METRIC");
		action.getParameters().add(actionParameter3);
		actions.getActions().add(action);
		rule.setActions(actions);
		
		rule.setTimeStep("60");
		rule.setTimeWindow("60");
		
		CollectedMetric collectedMetric = new CollectedMetric();
		collectedMetric.setMetricName("ResponseTime");
		Parameter metricParameter = new Parameter();
		metricParameter.setName("samplingProbability");
		metricParameter.setValue("1.0");
		collectedMetric.getParameters().add(metricParameter);
		rule.setCollectedMetric(collectedMetric);
		
		MonitoredTarget target = new MonitoredTarget();
		target.setClazz("Method");
		target.setType("Login");
		MonitoredTargets targets = new MonitoredTargets();
		targets.getMonitoredTargets().add(target);
		rule.setMonitoredTargets(targets);
		
		MonitoringMetricAggregation aggregation = new MonitoringMetricAggregation();
		aggregation.setAggregateFunction("Average");
		aggregation.setGroupingClass("CloudProvider");
		rule.setMetricAggregation(aggregation);
		
		try {
			Validator validator = new Validator();
			Set<Problem> problems = validator.validateRule(rule, null);
			if (problems.isEmpty()) {
				CSquery query = CSPARQLEngineManager.createQueryFromRule(rule, "Test3", "http://localhost:3030/modaclouds/kb");
				System.out.println(query.getCSPARQL());
			} else {
				for (Problem p: problems) {
					System.out.println(p.toString());
				}
				fail();
			}
			
		} catch (MalformedQueryException | RuleInstallationException | ConfigurationException e) {
			e.printStackTrace();
			fail();
		}
	}

}
