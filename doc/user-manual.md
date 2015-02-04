[Documentation table of contents](TOC.md) / User Manual

#User Manual

The monitoring manager can be configured by means of different options (latters replaces the formers):
* Default Configuration
* Environment Variables
* System Properties
* CLI Arguments

## Default Configuration

* DDA URL: `http://127.0.0.1:8175`
* KB URL: `http://127.0.0.1:3030/modaclouds/kb`
* Monitoring Manager Port: `8170`

## Environment Variables

```
MODACLOUDS_MONITORING_DDA_ENDPOINT_IP
MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT
MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH
MODACLOUDS_MONITORING_MANAGER_PORT
```

where:
* DDA URL: `http://${MODACLOUDS_MONITORING_DDA_ENDPOINT_IP}:${MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT}`
* KB URL: `http://${MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP}:${MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT}${MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH}`
* Monitoring Manager Port: `${MODACLOUDS_MONITORING_MANAGER_PORT}`

## System Properties

Same names used for [Environment Variables](#EnvironmentVariables).

## CLI Arguments

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
```