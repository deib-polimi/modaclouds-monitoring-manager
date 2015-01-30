FROM ubuntu

MAINTAINER mmiglier

RUN DEBIAN_FRONTEND=noninteractive \
    apt-get update && \
    apt-get install -y tar openjdk-7-jre

RUN mkdir /opt/dda

ADD https://github.com/deib-polimi/rsp-services-csparql/releases/download/0.4.6.2-modaclouds/rsp-services-csparql-0.4.6.2-modaclouds-distribution.tar.gz /opt/dda/rsp-services-csparql-0.4.6.2-modaclouds-distribution.tar.gz
RUN tar -xvzf /opt/dda/rsp-services-csparql-0.4.6.2-modaclouds-distribution.tar.gz -C /opt/dda
RUN chmod +x /opt/dda/rsp-services-csparql-0.4.6.2-modaclouds/rsp-services-csparql
ADD startup.sh /opt/dda/rsp-services-csparql-0.4.6.2-modaclouds/startup.sh
RUN chmod +x /opt/dda/rsp-services-csparql-0.4.6.2-modaclouds/startup.sh
EXPOSE 8175
CMD ["/opt/dda/rsp-services-csparql-0.4.6.2-modaclouds/startup.sh"]