[Documentation table of contents](TOC.md) / User Manual

#User Manual

## What to configure

* DDA URL: the Deterministic Data Analzyer endpoint
* KB URL: the Knowledge Base endpoint
* Monitoring Manager Port: the port the Monitoring Mager should listen to
* Monitoring metrics file: the xml file list of metrics used for validating monitoring rules. The list should contain all metrics data collectors can provide. The file should be validated by the [metrics_schema](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/metamodels/commons/metrics_schema.xsd).The [default list](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/src/main/resources/monitoring_metrics.xml) can be overridden by a custom one either using a local file or a public URL.
* Upload ontology to KB: uploading the ontology to the KB can be disabled. This can be useful if the ontology is uploaded in a different way or is already in the KB.

## How to configure

The monitoring manager can be configured by means of different options (latters replaces the formers):
* Default Configuration
* Environment Variables
* System Properties
* CLI Arguments

### Default Configuration

* DDA URL: `http://127.0.0.1:8175`
* KB URL: `http://127.0.0.1:3030/modaclouds/kb`
* Monitoring Manager Port: `8170`
* Monitoring metrics file: [default list of monitoring metrics](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/src/main/resources/monitoring_metrics.xml)
* Upload ontology to KB: `true`

### Environment Variables

```
MODACLOUDS_MONITORING_DDA_ENDPOINT_IP
MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT
MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH
MODACLOUDS_MONITORING_MANAGER_PORT
MODACLOUDS_MONITORING_MONITORING_METRICS_FILE
MODACLOUDS_MONITORING_UPLOAD_ONTOLOGY
```

where:
* DDA URL: `http://${MODACLOUDS_MONITORING_DDA_ENDPOINT_IP}:${MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT}`
* KB URL: `http://${MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP}:${MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT}${MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH}`
* Monitoring Manager Port: `${MODACLOUDS_MONITORING_MANAGER_PORT}`
* Monitoring metrics file: `${MODACLOUDS_MONITORING_MONITORING_METRICS_FILE}`
* Upload ontology to KB: `${MODACLOUDS_MONITORING_UPLOAD_ONTOLOGY}`

### System Properties

Same names used for Environment Variables.

### CLI Arguments

Usage available by running `./monitoring-manager -help`:

```
Usage: monitoring-manager [options]
  Options:
    -ddaip
       DDA endpoint IP address
       Default: 127.0.0.1
    -ddaport
       DDA endpoint port
       Default: 8175
    -help
       Shows this message
       Default: false
    -kbip
       KB endpoint IP address
       Default: 127.0.0.1
    -kbpath
       KB URL path
       Default: /modaclouds/kb
    -kbport
       KB endpoint port
       Default: 3030
    -mmport
       Monitoring Manager endpoint port
       Default: 8170
    -uploadontology
       Upload ontology to kb at startup
       Default: true
    -validmetrics
       The xml file containing the list of valid metrics. Will overwrite default ones
```
