The MODAClouds Monitoring Manager
=============================

In the context of MODAClouds European project (www.modaclouds.eu), Politecnico was
one of the partners involved in the development of the Monitoring Platform.

The monitoring manager is the main coordinator of the platform. 
It is responsible of installing monitoring rules, configuring monitoring components,
 attaching external observers to requested metrics and keeping the knowledge 
 base up to date through the interaction with Models@Runtime. The monitoring manager is also 
 is the main interface towards external components.

Please refer to deliverable [D6.3.2](http://www.modaclouds.eu/publications/public-deliverables/) 
to better understand the role of this component in the MODAClouds Monitoring Platform.

Refer to the [Monitoring Platform Wiki](https://github.com/deib-polimi/modaclouds-monitoring-manager/wiki) for installation and usage of the whole platform.

## Documentation

Take a look at the [documentation table of contents](doc/TOC.md).

## Change List

v1.3:
* implemented API: `GET /model/resources/:id`
* updated to [csparqool 1.2.1](https://github.com/deib-polimi/csparqool/releases/tag/v1.2.1)
* updated to [knowledge-base-api 2.2](https://github.com/deib-polimi/modaclouds-knowledge-base-api/releases/tag/v2.2)
* updatad to [data-collector-factory 0.3](https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3)
* updated to [qos-models 2.2](https://github.com/deib-polimi/modaclouds-qos-models/releases/tag/v2.2)
* queries are now created according to the new qos-models 2.2 version of monitoring rules:
  * metricAggregation is optional
  * outputMetric action now accepts 3 parameters (resourceId, metric, value) 
* sda are now just observers & data collectors from the monitoring manager perspective
* bug fixes

v1.2:
* internalComponents can now be a monitoredTarget
* system properties can be used now besides environemnt variables
* inheritance was removed

v1.1.2:

* Fixed a bug that didn't allow to upload a new model the first time using the PUT method
* updated to qos-models 2.1.4: check qos-models change list for the new of version of monitoring rules
* installation instructions updated (look at the documentation)
