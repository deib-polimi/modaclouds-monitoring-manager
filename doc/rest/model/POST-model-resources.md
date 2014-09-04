[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / POST-metrics-id-observers

# Model

	POST /model/resources

## Description
Add resources to the knowledge base.

***

## URL Parameters

None

***

## Data Parameters

A JSON containing all the resources that must be added to the knowledge base.

***

## Response

**Status:** **204 No Content**


***

## Errors

* **505 error while adding resources** - One or more resources were not valid

***

## Example
**Request**

	POST v1/model/resources
	
```
{"cloudProviders":null,"locations":null,"vMs":[{"numberOfCPUs":1,"location":"192.168.12.4","cloudProvider":"flexiant","type":"vm","id":"vm1"}],"paaSServices":null,"internalComponents":[{"requiredComponents":[],"providedMethods":[],"type":null,"id":"internalComp"},{"requiredComponents":[],"providedMethods":[],"type":null,"id":"internalComp1"},{"requiredComponents":[],"providedMethods":[],"type":null,"id":"internalComp2"}],"methods":null}

```

**Response**

	Status: 204 No Content
