[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / GET-metrics-id-observers

# Metrics

	GET /metrics/:id/observers

## Description
Returns the list of observers attached to the metric.

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** An XML object with a list of information about attached observers.

***

## Errors

* **404 Resource not found** - The metric does not exist.

***

## Example
**Request**

	GET v1/metrics/ResponseTime/observers

**Response**

	**Status:** **200 OK**

``` xml
<observers>
	<observer id="observer-1">http://url.to.observer.1:8176/response-time</observer>
	<observer id="observer-2">http://url.to.observer.2:8222/data</observer>
</observers>
```