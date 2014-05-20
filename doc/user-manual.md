[Documentation table of contents](TOC.md) / User Manual

#User Manual

## Requirements

The knowledge base should be started with the following command:

```
./fuseki-server --update --port=[fuseki port] --config=moda_fuseki_configuration.ttl
```

where moda_fuseki_configuration.ttl content can be found at this
[link](https://github.com/deib-polimi/modaclouds-knowledge-base-api/blob/master/doc/user-manual.md#configuration).

The MODAClouds monitoring ontology should be loaded on the knowledge base with the following command:

```
[fuseki installation directory]/s-put http://[fuseki url]:[fuseki port]/modaclouds/kb/data default monitoring_ontology.ttl
```

monitoring_ontology.ttl can be found at this [link](https://raw.githubusercontent.com/deib-polimi/modaclouds-qos-models/master/metamodels/monitoringontology/monitoring_ontology.ttl).

The dda server should be launched with the following command:

```
java jar rsp-services-csparql-[version].jar
```

and should have in the same folder the logback.xml file:

``` xml
<configuration>

	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>ERROR</level>
		</filter>
		<encoder>
			<pattern>
				%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
			</pattern>
		</encoder>
	</appender>
	<appender name="FILE_ERROR" class="ch.qos.logback.core.FileAppender">
		<file>log/csparql_server_error.log</file>
		<append>true</append>
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>ERROR</level>
		</filter>
		<encoder>
			<pattern>
				%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
			</pattern>
		</encoder>
	</appender>
	<appender name="FILE_DEBUG" class="ch.qos.logback.core.FileAppender">
		<file>log/csparql_server_debug.log</file>
		<append>true</append>
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>DEBUG</level>
		</filter>
		<encoder>
			<pattern>
				%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
			</pattern>
		</encoder>
	</appender>
	<appender name="CSPARQL_ENGINE" class="ch.qos.logback.core.FileAppender">
		<file>log/csparql_engine_perferomance.log</file>
		<append>true</append>
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>DEBUG</level>
		</filter>
		<encoder>
			<pattern>
				%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n
			</pattern>
		</encoder>
	</appender>


	<logger name="eu.larkc.csparql.sparql.jena.JenaEngine">
		<appender-ref ref="CSPARQL_ENGINE" />
	</logger>
	
	<logger name="org.apache.jena.riot.*" level="OFF" />
	
	<logger name="ch.qos.logback.*" level="OFF" />

	<root level="DEBUG">
		<appender-ref ref="STDOUT" />
	</root>
	<root level="ERROR">
		<appender-ref ref="FILE_ERROR" />
	</root>
	<root level="DEBUG">
		<appender-ref ref="FILE_DEBUG" />
	</root>

</configuration>
```

and the setup.properties file:

```
#Rest server properties
csparql_server.port=8175
csparql_server.version=0.4.3
csparql_server.host_name=http://[dda actual address]:8175
#Csparql Engine properties
csparql_engine.enable_timestamp_function=true
csparql_engine.send_empty_results=false
csparql_engine.activate_inference=false
#If inference is activated set the rules file path (or URL). The default reasoner is RDFS.
#csparql_engine.inference_rule_file=inference_rules_files/transitive.rules
```

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
dda_server.port=8175
dda_server.address=localhost
sda_server.port=8176
sda_server.address=localhost
mm_server.port=8170
```

launch the server: java -jar monitoring-manager-[version].jar