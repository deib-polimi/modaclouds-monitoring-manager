[Documentation table of contents](TOC.md) / User Manual

# Usage

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


## Configuration

## Code samples