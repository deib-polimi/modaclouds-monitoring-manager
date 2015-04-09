[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / POST-metrics-id-observers

# Monitoring Rules

	POST /metrics/:id/observers

## Description
Attach an observer to the metric.

***

## URL Parameters

None

***

## Data Parameters

the callback url of the observer.

***

## Response

**Status:** **201 Created**

**Body:** a json object containing information about the observer just registered.

***

## Errors

* **400 Bad Request** - the callback url is not valid.
* **404 Resource not found** - The metric does not exist.

***

## Example
**Request**

	POST v1/metrics/ResponseTime/observers
	
```
http://url.to.observer.1:9999/path
```

**Response**

	Status: 201 Created

``` json
{
	"id": "109384935893",
	"callbackUrl": "http://url.to.observer.1:9999/path"
}
```