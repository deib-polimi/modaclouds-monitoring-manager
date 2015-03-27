[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / GET-monitoring-rules-id

# Model

	GET /monitoring-rules/:id

## Description
Get the specified monitoring rule

***

## URL Parameters

None.

***

## Response

**Status:** **200 OK**

**Body:** An XML representation of the monitoring rule

***

## Errors

* **404 Resource not found** - The monitoring rule does not exist

***

## Example
**Request**

	GET v1/monitoring-rules/mr_1
	
**Response**

	Status: 200 OK

``` xml
<monitoringRule id="mr_1" label="CPU Utilization Rule"
	startEnabled="true" timeStep="60" timeWindow="60">
	<monitoredTargets>
		<monitoredTarget type="tr_1" class="VM"/>
	</monitoredTargets>
	<collectedMetric inherited="false" metricName="CpuUtilization">
		<parameter name="samplingTime">10</parameter>
	</collectedMetric>
	<metricAggregation groupingClass="Region"
		aggregateFunction="Average">
	</metricAggregation>
	<condition>METRIC &gt;= 0.6</condition>
	<actions>
		<action name="OutputMetric">
			<parameter name="resourceId">ID</parameter>
			<parameter name="metric">CpuUtilizationViolation</parameter>
			<parameter name="value">METRIC</parameter>
		</action>
	</actions>
</monitoringRule>
```
