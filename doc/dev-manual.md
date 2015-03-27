[Documentation table of contents](TOC.md) / Developer Manual

#Developer Manual

## Requirements
In order to automate testing in a common development environment, the installation of [Vagrant](http://www.vagrantup.com) is required.

## Usage of Vagrant during development and integration testing

In the root folder of the project run `vagrant up` to start a virtual machine with an instance of DDA and one of KB running and an instance of the MM compiled right from the current code.

Alternatively you can launch the `vagrant up` command preceded by one ore more of the following environment variables:
* `COMPILE_MM='false'` if you want to skip compilation (the package assembled in target folder will be used)
* `RUN_LATEST_MM_RELEASE='true'` if you want to run the latest published released version of the monitoring manager instead of the current one
* `EXCLUDE_MM='true'` for starting only an instance of DDA and of KB. Use this configuration when you want to run the monitoring manager from your machine for debugging purposes. WARNING: hosts cannot be exposed to guest machines, therefore the DDA will not be able to connect to the MM for triggering actions
* `START_TEST_OBSERVERS='true'` for launching two observers for testing purposes:
	* `localhost:8000/simpleobserver/data` will print the received monitoring data as it comes (json/rdf format)
	* `localhost:8000/csvobserver/data` will print received monitoring data in csv format (resourceId,metric,value,timestamp).

The [default configuration](user-manual.md#default-configuration) will be used.

A logs folder will be created automatically, where all components log files will be available.

Use `vagrant reload` instead of `vagrant up` if you want to restart the machine. Use `vagrant halt` to stop the machine. Use `vagrant destroy` to destroy the virtual machine. Check out [Vagrant official documentation](https://docs.vagrantup.com/v2/) for further details.

## Integration tests
Run `mvn verify` for launching integration tests (it will use `COMPILE_MM='false' vagrant up`)
