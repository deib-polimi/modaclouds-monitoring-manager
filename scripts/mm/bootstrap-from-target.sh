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

cd /vagrant

ls -1 target/monitoring-manager-*-distribution.tar.gz > /dev/null 2>&1

if [ "$?" != "0" ]; then 
	echo "Cannot find monitoring-manager-*-distribution.tar.gz in target folder"
	exit 1
fi

latest=`ls -1 target/monitoring-manager-*-distribution.tar.gz | tail -1`

if [ -d /opt/mm ];
	then rm -r /opt/mm
fi
mkdir -p /opt/mm

tar -xvzf $latest -C /opt/mm
cd /opt/mm/monitoring-manager*
chmod +x monitoring-manager