[Documentation table of contents](TOC.md) / User Manual

#User Manual

## Requirements

Look at the [wiki](https://github.com/deib-polimi/modaclouds-monitoring-manager/wiki) for the setup of required services.

## Installation

You can download the jar from https://github.com/deib-polimi/modaclouds-monitoring-manager/releases.
If you need to embed the monitoring manager inside your code the project is also distributed to our Maven Repo.

Releases repository:
```xml
<repositories>
	...
	<repository>
        <id>deib-polimi-releases</id>
        <url>https://github.com/deib-polimi/deib-polimi-mvn-repo/raw/master/releases</url>
	</repository>
	...
</repositories>
```

Snapshots repository:
```xml
<repositories>
	...
	<repository>
        <id>deib-polimi-snapshots</id>
        <url>https://github.com/deib-polimi/deib-polimi-mvn-repo/raw/master/snapshots</url>
	</repository>
	...
</repositories>
```

Dependency:
```xml
<dependencies>
	<dependency>
		<groupId>it.polimi.modaclouds.monitoring</groupId>
		<artifactId>monitoring-manager</artifactId>
		<version>VERSION</version>
	</dependency>
</dependencies>
```

Put in the same folder of the jar the kb.properties file containing information about
the knowledge base instance like in the example:

```
kb_server.port=3030
kb_server.address=localhost
```

and the monitoring_manager.properties file containing information about dda, sda and
the monitoring manager server port, like in the example:

```
mm_server.port=8170
dda_server.port=8175
matlab_sda_server.port=8176
java_sda_server.port=8177
dda_server.address=localhost
matlab_sda_server.address=localhost
java_sda_server.address=localhost
```

launch the server: java -jar monitoring-manager-[version].jar