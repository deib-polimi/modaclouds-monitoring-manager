[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / POST-metrics-id

# Monitoring Rules

	POST /metrics/:id

## Description
Attach an observer to the metric.

***

## URL Parameters

None

***

## Data Parameters

An xml object with the callback url of the observer.

***

## Response

**Status:** **201 Created**

**Body:** An xml object with information about the registered observer.

***

## Errors

* **400 Bad Request** - The body of the request is not in the correct format.
* **404 Resource not found** - The metric does not exist.

***

## Example
**Request**

	POST v1/metrics/ResponseTime
	
``` xml
<callback_url>http://url.to.observer.1:8176/ResponseTime</callback_url>
```

**Response**

	Status: 201 Created

``` xml
<observer id="observer-1">http://url.to.observer.1:8176/ResponseTime</observer>
```