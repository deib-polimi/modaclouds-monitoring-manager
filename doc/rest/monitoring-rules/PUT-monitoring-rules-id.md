[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / PUT-monitoring-rules-id

# Monitoring Rules

	PUT /monitoring-rules/:id

## Description
Creates or updates a monitoring rule. The id of the monitoring rule is updated so to match the id specified in the URL.

***

## URL Parameters

None

***

## Data Parameters

An XML object with a monitoring rule conforming to the [monitoring_rules_schema.xsd][].

***

## Response

**Status:** **204 No Content**

***

## Errors

All known errors cause the resource to return HTTP error code header together with a description of the error.

* **400 Bad Request** - The monitoring rule is not valid

***

## Example
**Request**

	PUT v1/monitoring-rules/mr_1


``` xml
<monitoringRule id="mr_1" label="CPU Utilization Rule"
		metricName="CpuUtilization" relatedQosConstraintId="qs_1" timeStep="60"
		timeWindow="60" startEnabled="true" samplingProbability="1"
		samplingTime="5">
		<monitoredTargets>
			<monitoredTarget id="tr_1" clazz="VM"/>
		</monitoredTargets>
		<metricAggregation groupingCategoryName="Region"
			aggregateFunction="Average" />
		<condition>METRIC &gt;= 0.6</condition>
		<actions>
			<action name="OutputMetric">
				<parameter name="name">CpuUtilizationViolation</parameter>
			</action>
		</actions>
</monitoringRule>
```

[monitoring_rules_schema.xsd]: https://github.com/deib-polimi/modaclouds-qos-models/blob/master/metamodels/monitoringrules/monitoring_rules_schema.xsd