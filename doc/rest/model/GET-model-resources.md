[Documentation table of contents](../../TOC.md) / [API Reference](../../api.md) / GET-model-resources

# Model

	GET /model/resources

## Description
Returns all resources in the current model.

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** A json object with a list of resources.

***

## Errors

None

***

## Example
**Request**

	GET v1/model/resources

**Response**

	Status: 200 OK

``` json
{
  "cloudProviders": [
    {
      "id": "Amazon"
    }
  ],
  "vMs": [
    {
      "numberOfCPUs": 2,
      "cloudProvider": "Amazon",
      "type": "AdministrationServer",
      "id": "AdministrationServer1"
    },
    {
      "numberOfCPUs": 2,
      "cloudProvider": "Amazon",
      "type": "MainAgent",
      "id": "MainAgent2"
    },
    {
      "numberOfCPUs": 2,
      "cloudProvider": "Amazon",
      "type": "MainAgent",
      "id": "MainAgent1"
    }
  ],
  "internalComponents": [
    {
      "requiredComponents": [
        "MainAgent2"
      ],
      "providedMethods": [],
      "type": "AgentApp",
      "id": "agentApp2"
    },
    {
      "requiredComponents": [
        "AdministrationServer1"
      ],
      "providedMethods": [],
      "type": "ServerApp",
      "id": "ServerApp1"
    },
    {
      "requiredComponents": [
        "MainAgent1"
      ],
      "providedMethods": [],
      "type": "AgentApp",
      "id": "agentApp1"
    }
  ]
}
```