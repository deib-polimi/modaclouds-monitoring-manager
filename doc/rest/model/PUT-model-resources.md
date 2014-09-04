[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / POST-metrics-id-observers

# Model

	PUT /model/resources

## Description
Upload a new model from scratch on the knowledge base.

***

## URL Parameters

None

***

## Data Parameters

A JSON containing the new model that must be uploaded on the knowledge base

***

## Response

**Status:** **204 No Content**


***

## Errors

* **505 error while uploading the model** - The model was not valid

***

## Example
**Request**

	POST v1/model/resources
	
```
{"cloudProviders":null,"locations":null,"vMs":[{"numberOfCPUs":1,"location":"192.168.12.4","cloudProvider":"flexiant","type":"vm","id":"vm1"}],"paaSServices":null,"internalComponents":[{"requiredComponents":[],"providedMethods":[],"type":null,"id":"internalComp"},{"requiredComponents":[],"providedMethods":[],"type":null,"id":"internalComp1"},{"requiredComponents":[],"providedMethods":[],"type":null,"id":"internalComp2"}],"methods":null}

```

**Response**

	Status: 204 No Content
