[Documentation table of contents](TOC.md) / Developer Manual

#Developer Manual

## Requirements
In order to automate testing in a common development environment, the installation of [Vagrant](http://www.vagrantup.com) is suggested.

## Usage of Vagrant during development and integration testing

In the root folder of the project you can run one of the following:
* `vagrant up` to start a virtual machine with an instance of DDA and one of KB running and an instance of the MM compiled right from the current code
* `COMPILE_MM='false' vagrant up` if you want to skip compilation (the package assembled in target folder will be used)
* `RUN_LATEST_MM_RELEASE='true' vagrant up` if you want to run the latest published released version of the monitoring manager instead of the current one
* `EXCLUDE_MM='true' vagrant up` for starting only an instance of DDA and of KB. Use this configuration when you want to run the monitoring manager from your machine for debugging purposes. WARNING: hosts cannot be exposed to guest machines, therefore the DDA will not be able to connect to the MM for triggering actions 
* `mvn verify` for launching integration tests (it will use `COMPILE_MM='false' vagrant up`)

The [default configuration](user-manual.md#default-configuration) will be used.

Use `vagrant reload` instead of `vagrant up` if you want to restart the machine. Use `vagrant halt` to stop the machine. Use `vagrant destroy` to destroy the virtual machine. Check out [Vagrant official documentation](https://docs.vagrantup.com/v2/) for further details.