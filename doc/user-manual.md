[Documentation table of contents](TOC.md) / User Manual

#User Manual

## Ready to go Monitoring Platform with Vagrant

If you installed [Vagrant](http://www.vagrantup.com) on your machine, you can simply have a ready to go monitoring platform by extracting the latest release package (for versions >= 1.6) and run `vagrant up`.

The platform will be up and running with KB, DDA and Monitoring Manager configured with the [default configuration](#default-configuration).

For testing purpose two observers where included in the package. Run `START_TEST_OBSERVERS='true' vagrant up` for having vagrant start also such observers:
	* `localhost:8000/simpleobserver/data` will print the received monitoring data as it comes (json/rdf format)
	* `localhost:8000/csvobserver/data` will print received monitoring data in csv format (resourceId,metric,value,timestamp).

A logs folder will be created automatically, where all components log files will be available.

Use `vagrant reload` instead of `vagrant up` if you want to restart the machine. Use `vagrant halt` to stop the machine. Use `vagrant destroy` to destroy the virtual machine. Check out [Vagrant official documentation](https://docs.vagrantup.com/v2/) for further details.

## Monitoring Manager configuration

### What to configure

* DDA URL: the Deterministic Data Analyzer endpoint
* KB URL: the Knowledge Base endpoint
* Monitoring Manager Port: the port the Monitoring Manager should listen to
* Monitoring Manager private Port: the port the Monitoring Manager should listen to for internal communication among platform components
* Monitoring Manager private IP: the private Monitoring Manager IP address for internal communication among platform components, must be accessible by the DDA
* Monitoring metrics file: the xml file list of metrics used for validating monitoring rules. The list should contain all metrics data collectors can provide. The file should be validated by the [metrics_schema](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/metamodels/commons/metrics_schema.xsd). The [default list](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/src/main/resources/monitoring_metrics.xml) can be overridden by a custom one either using a local file or a public URL.

### How to configure

The monitoring manager can be configured by means of different options (latters replaces the formers):
* Default Configuration
* Environment Variables
* System Properties
* CLI Arguments

#### Default Configuration

* DDA URL: `http://127.0.0.1:8175`
* KB URL: `http://127.0.0.1:3030/modaclouds/kb`
* Monitoring Manager Port: `8170`
* Monitoring Manager private Port: `8070`
* Monitoring Manager private IP address: `127.0.0.1`
* Monitoring metrics file: [default list of monitoring metrics](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/src/main/resources/monitoring_metrics.xml)

#### Environment Variables

```
MODACLOUDS_MONITORING_DDA_ENDPOINT_IP
MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT
MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH
MODACLOUDS_MONITORING_MANAGER_PORT
MODACLOUDS_MONITORING_MANAGER_PRIVATE_PORT
MODACLOUDS_MONITORING_MANAGER_PRIVATE_IP
MODACLOUDS_MONITORING_MONITORING_METRICS_FILE
```

where:
* DDA URL: `http://${MODACLOUDS_MONITORING_DDA_ENDPOINT_IP}:${MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT}`
* KB URL: `http://${MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP}:${MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT}${MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH}`
* Monitoring Manager Port: `${MODACLOUDS_MONITORING_MANAGER_PORT}`
* Monitoring Manager private Port: `${MODACLOUDS_MONITORING_MANAGER_PRIVATE_PORT}`
* Monitoring Manager private IP address: `${MODACLOUDS_MONITORING_MANAGER_PRIVATE_IP}`
* Monitoring metrics file: `${MODACLOUDS_MONITORING_MONITORING_METRICS_FILE}`

#### System Properties

Same names used for Environment Variables.

#### CLI Arguments

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
   -mmprivateport
       Monitoring Manager private endpoint Port
       Default: 8170
   -mmprivate ip
       Monitoring Manager private endpoint IP address
       Default: 8170
    -validmetrics
       The xml file containing the list of valid metrics. Will overwrite default ones
```
