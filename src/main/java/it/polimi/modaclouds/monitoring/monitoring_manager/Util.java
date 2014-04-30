package it.polimi.modaclouds.monitoring.monitoring_manager;

import it.polimi.csparqool.FunctionArgs;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Parameter;
import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.CollectedMetric;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

public class Util {

	public static Parameter getParameter(String parameterName,
			CollectedMetric collectedMetric) {
		for (it.polimi.modaclouds.qos_models.schema.Parameter par : collectedMetric
				.getParameters()) {
			if (par.getName().equals(parameterName)) {
				return new Parameter(par.getName(), par.getValue());
			}
		}
		return null;
	}

	public static String getOutputValueVariable(MonitoringRule rule,
			boolean sdaRequired) {
		if (sdaRequired || !isGroupedMetric(rule))
			return QueryVars.INPUT;
		return QueryVars.OUTPUT;
	}

	public static String getOutputTarget(MonitoringRule rule)
			throws RuleInstallationException {
		String outputTarget;
		if (isGroupedMetric(rule)) {
			outputTarget = getGroupingClassVariable(rule);
		} else {
			outputTarget = QueryVars.TARGET;
		}
		return outputTarget;
	}

	public static String getAggregateFunction(MonitoringRule rule) {
		if (!isGroupedMetric(rule))
			return null;
		String aggregateFunction = rule.getMetricAggregation()
				.getAggregateFunction();
		return aggregateFunction;
	}

	public static String getGroupingClassVariable(MonitoringRule rule)
			throws RuleInstallationException {
		if (!isGroupedMetric(rule))
			return null;
		String groupingClass = getGroupingClass(rule);
		String targetClass = getTargetClass(rule);
		if (groupingClass.equals(targetClass))
			return QueryVars.TARGET;
		return "?" + groupingClass;
	}

	public static String getTargetClass(MonitoringRule rule)
			throws RuleInstallationException {
		List<MonitoredTarget> targets = getMonitoredTargets(rule);
		String targetClass = null;
		for (MonitoredTarget t : targets) {
			if (targetClass != null)
				if (!targetClass.equals(t.getClazz()))
					throw new RuleInstallationException(
							"Monitored targets must belong to the same class");
				else
					targetClass = t.getClazz();
		}
		return targetClass;
	}

	public static String getGroupingClass(MonitoringRule rule) {
		if (isGroupedMetric(rule))
			return rule.getMetricAggregation().getGroupingClass();
		else
			return null;
	}

	public static boolean isGroupedMetric(MonitoringRule rule) {
		return rule.getMetricAggregation() != null;
	}

	public static List<MonitoredTarget> getMonitoredTargets(MonitoringRule rule) {
		List<MonitoredTarget> targets = rule.getMonitoredTargets()
				.getMonitoredTargets();
		if (targets.size() != 1)
			throw new NotImplementedException(
					"Multiple or zero monitored target is not implemented yet");
		return targets;
	}

	public static Parameter getParameter(String parameterName,
			MonitoringMetricAggregation metricAggregation) {
		for (it.polimi.modaclouds.qos_models.schema.Parameter par : metricAggregation
				.getParameters()) {
			if (par.getName().equals(parameterName)) {
				return new Parameter(par.getName(), par.getValue());
			}
		}
		return null;
	}

	public static String getParameterValue(String parameterName,
			MonitoringMetricAggregation metricAggregation) {
		for (it.polimi.modaclouds.qos_models.schema.Parameter par : metricAggregation
				.getParameters()) {
			if (par.getName().equals(parameterName))
				return par.getValue();
		}
		return null;
	}

	public static String getParameterValue(String parameterName, Action action) {
		for (it.polimi.modaclouds.qos_models.schema.Parameter par : action
				.getParameters()) {
			if (par.getName().equals(parameterName))
				return par.getValue();
		}
		return null;
	}

	public static String[] getAggregateFunctionArgs(MonitoringRule rule) {
		String aggregateFunction = Util.getAggregateFunction(rule);
		String[] args = new String[FunctionArgs
				.getNumberOfArgs(aggregateFunction)];
		args[FunctionArgs.getArgIdx(aggregateFunction,
				FunctionArgs.INPUT_VARIABLE)] = QueryVars.INPUT;
		if (rule.getMetricAggregation().getParameters() != null) {
			List<it.polimi.modaclouds.qos_models.schema.Parameter> rulePars = rule
					.getMetricAggregation().getParameters();
			for (it.polimi.modaclouds.qos_models.schema.Parameter p : rulePars) {
				int index = FunctionArgs.getArgIdx(aggregateFunction,
						p.getName());
				args[index] = p.getValue().toString();
			}
		}
		return args;
	}

}
