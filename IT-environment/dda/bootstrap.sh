#
# Copyright 2014 deib-polimi
# Contact: deib-polimi <marco.miglierina@polimi.it>
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

apt-get update
apt-get install -y tar openjdk-7-jre

if [ -d /opt/dda ];
	then rm -r /opt/dda
fi

mkdir -p /opt/dda
cd /opt/dda

echo "Downloading dda..."
wget --quiet -O rsp-services-csparql-0.4.6.2-modaclouds-distribution.tar.gz https://github.com/deib-polimi/rsp-services-csparql/releases/download/0.4.6.2-modaclouds/rsp-services-csparql-0.4.6.2-modaclouds-distribution.tar.gz
tar -xvzf rsp-services-csparql-0.4.6.2-modaclouds-distribution.tar.gz -C .
chmod +x rsp-services-csparql-0.4.6.2-modaclouds/rsp-services-csparql