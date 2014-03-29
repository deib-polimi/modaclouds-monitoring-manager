[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / GET-metrics

# Metrics

	GET /metrics

## Description
Returns the list of metrics available to be observed.

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** An XML object with a list of metrics.

***

## Errors

None

***

## Example
**Request**

	GET v1/metrics

**Response**

	**Status:** **200 OK**

``` xml
<metrics>
	<metric>CpuUtilization</metric>
	<metric>ResponseTime</metric>
	<metric>CpuUtilizationViolation</metric>
	<metric>CpuUtilizationForecast</metric>
	<metric>Availability</metric>
	<metric>ThreadsRunning</metric>
</metrics>
```