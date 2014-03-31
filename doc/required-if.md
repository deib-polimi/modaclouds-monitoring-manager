[Documentation table of contents](TOC.md) / Required Interfaces

# Required Interfaces

## Deterministic Data Analyzer

The Deterministic Data Analyzer must provide an implementation of RDF Stream Processor 
REST Services (rsp-services) for C-SPARQL engine. Version 1.0 of the monitoring platform requires
version 0.4 of rsp-services:

- **0.4**: https://github.com/streamreasoning/rsp-services/releases/tag/0.4
- **Latest Version**: https://github.com/streamreasoning/rsp-services

Refer also to the iswc poster [*"A Restful Interface for RDF Stream Processors"*](http://ceur-ws.org/Vol-1035/iswc2013_poster_8.pdf), 
by Balduini M. and Della Valle E., for further details on the specification.

## Knowledge Base

The Knowledge Base must provide a REST interface compliant with the SPARQL protocol over HTTP (http://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/).

## Models@runtime

The required interface towards Models@runtime will be defined during the integration with Models@runtime, due at M24.
It will consist of an interface for retrieving the current deployment configuration.

## Object Store

The required interface towards the Object Store will be defined during the integration with Object Store, due at M24. It will consist of an interface that allows all the components to retrieve the url of the knowledge base.