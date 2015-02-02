FROM ubuntu

MAINTAINER mmiglier

RUN DEBIAN_FRONTEND=noninteractive \
    apt-get update && \
    apt-get install -y tar openjdk-7-jre

RUN mkdir /opt/fuseki

ADD http://archive.apache.org/dist/jena/binaries/jena-fuseki-1.1.1-distribution.tar.gz /opt/fuseki/jena-fuseki-1.1.1-distribution.tar.gz
RUN tar -xvzf /opt/fuseki/jena-fuseki-1.1.1-distribution.tar.gz -C /opt/fuseki
RUN chmod +x /opt/fuseki/jena-fuseki-1.1.1/fuseki-server
ADD startup.sh /opt/fuseki/jena-fuseki-1.1.1/startup.sh
RUN chmod +x /opt/fuseki/jena-fuseki-1.1.1/startup.sh
RUN mkdir /data
VOLUME /data
EXPOSE 3030
CMD ["/opt/fuseki/jena-fuseki-1.1.1/startup.sh"]