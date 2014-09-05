[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / POST-metrics-id-observers

# Model

	POST /model/resources

## Description
Add resources to the knowledge base. If a previous model existed, the existing model is updated. 
A new resource is created for each resource in the uploaded model with the id specified. If a resource with an existing
id already exists, the resource is replaced with the new one.

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
	
``` json
{
  "internalComponents": [
    {
      "id": "mic3", 
      "providedMethods": [
        "mic3-register", 
        "mic3-answerQuestions", 
        "mic3-saveAnswers"
      ], 
      "requiredComponents": [
        "frontend3"
      ], 
      "type": "Mic"
    }
  ], 
  "methods": [
    {
      "id": "mic3-answerQuestions", 
      "type": "answerQuestions"
    }, 
    {
      "id": "mic3-saveAnswers", 
      "type": "saveAnswers"
    }, 
    {
      "id": "mic3-register", 
      "type": "register"
    }
  ], 
  "vMs": [
    {
      "cloudProvider": "amazon", 
      "id": "frontend3", 
      "numberOfCPUs": 2, 
      "type": "Frontend"
    }
  ]
}

```

**Response**

	Status: 204 No Content
