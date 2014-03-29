[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / GET-monitoring-rules

# Monitoring Rules

	GET /monitoring-rules

## Description
Returns the list of installed monitoring rules.

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** An XML object with a list of monitoring rules, conforming to the [monitoring_rules_schema.xsd][].

***

## Errors

None

***

## Example
**Request**

	GET v1/monitoring-rules

**Response**

	Status: 200 OK

``` xml
<monitoringRules
	xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema
	https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/v1.0/metamodels/monitoringrules/monitoring_rules_schema.xsd">
	<monitoringRule id="mr_1" label="CPU Utilization Rule"
		metricName="CpuUtilization" relatedQosConstraintId="qs_1" timeStep="60"
		timeWindow="60" startEnabled="true" samplingProbability="1"
		samplingTime="5">
		<monitoredTargets>
			<monitoredTarget id="tr_1" />
		</monitoredTargets>
		<metricAggregation groupingCategoryName="Region"
			aggregateFunction="Average" />
		<condition>METRIC &gt;= 0.6</condition>
		<actions>
			<action name="NotifyViolation" />
		</actions>
	</monitoringRule>
	<monitoringRule id="mr_2" label="Percentile Monitoring Rule"
		metricName="ResponseTime" relatedQosConstraintId="qs_2" timeStep="60"
		startEnabled="true" timeWindow="60" samplingProbability="1"
		samplingTime="5">
		<monitoredTargets>
			<monitoredTarget id="tr_2" />
		</monitoredTargets>
		<metricAggregation groupingCategoryName="CloudProvider"
			aggregateFunction="Percentile">
			<parameter>95</parameter>
		</metricAggregation>
		<condition>METRIC &gt;= 1</condition>
		<actions>
			<action name="NotifyViolation" />
		</actions>
	</monitoringRule>
</monitoringRules>
```

[monitoring_rules_schema.xsd]: https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/v1.0/metamodels/monitoringrules/monitoring_rules_schema.xsd