#!/bin/sh
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

if [ -d /opt/mm ];
	then rm -r /opt/mm
fi
mkdir -p /opt/mm
cd /opt/mm

echo "Downloading monitoring-manager-1.5..."
wget --quiet -O monitoring-manager.tar.gz https://github.com/deib-polimi/modaclouds-monitoring-manager/releases/download/v1.5/monitoring-manager-1.5-distribution.tar.gz
tar -xvzf monitoring-manager.tar.gz -C .
cd monitoring-manager*
chmod +x monitoring-manager