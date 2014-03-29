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

	PUT v1/monitoring-rules/mr_3


``` xml
<monitoringRule id="mr_3" label="Gold Percentile Monitoring Rule"
	parentMonitoringRuleId="mr_2" samplingProbability="1" samplingTime="5">
	<monitoredTargets>
		<monitoredTarget id="tr_3" />
	</monitoredTargets>
	<metricAggregation inherited="true" />
	<condition>METRIC &gt;= 0.3</condition>
	<actions inherited="true" />
</monitoringRule>
```

[monitoring_rules_schema.xsd]: https://github.com/deib-polimi/modaclouds-qos-models/blob/master/metamodels/monitoringrules/monitoring_rules_schema.xsd