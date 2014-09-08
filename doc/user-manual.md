[Documentation table of contents](TOC.md) / User Manual

#User Manual

## Installation

Requirements:
* Java 7 JDK
* Apache Maven 3+
* git

```
git clone https://github.com/deib-polimi/modaclouds-monitoring-manager.git
cd modaclouds-monitoring-manager
git checkout tags/v1.1.1
mvn package assembly:single
cd ..
```

Set the following environment variables:

```
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP=<KB_IP> (e.g., "127.0.0.1")
MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT=<KB_PORT> (e.g., "3030")
MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH=<KB_PATH> (e.g., "/modaclouds/kb")
MODACLOUDS_MONITORING_DDA_ENDPOINT_IP=<DDA_IP> (e.g., "127.0.0.1")
MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT=<DDA_PORT> (e.g., "8175")
MODACLOUDS_MONITORING_MANAGER_PORT=<MM_PORT> (e.g., "8170")
MODACLOUDS_MATLAB_SDA_IP=<MATLAB_SDA_IP> (e.g., "127.0.0.1")
MODACLOUDS_MATLAB_SDA_PORT=<MATLAB_SDA_PORT> (e.g., "8176")
MODACLOUDS_WEKA_SDA_IP=<WEKA_SDA_IP> (e.g., "127.0.0.1")
MODACLOUDS_WEKA_SDA_PORT=<WEKA_SDA_PORT> (e.g., "8177")
```



## Usage

Refer to the [wiki](https://github.com/deib-polimi/modaclouds-monitoring-manager/wiki) for usage instructions.
