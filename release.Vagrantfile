VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.define "monitoring-platform" do |config|
    
    config.vm.box = "ubuntu/trusty64"
    config.vm.hostname = "mp-vm"
    config.vm.network "forwarded_port", guest: 8170, host: 8170
    config.vm.network "forwarded_port", guest: 8070, host: 8070
    config.vm.network "forwarded_port", guest: 3030, host: 3030
    config.vm.network "forwarded_port", guest: 8175, host: 8175

    config.vm.provision "shell", path: "scripts/bootstrap.sh"
    config.vm.provision "shell", path: "scripts/dda/bootstrap.sh"
    config.vm.provision "shell", path: "scripts/kb/bootstrap.sh"
    config.vm.provision "shell", path: "scripts/mm/bootstrap.sh"

    config.vm.provision "shell", run: "always", path: "scripts/kb/startup.sh"
    config.vm.provision "shell", run: "always", path: "scripts/dda/startup.sh"
    config.vm.provision "shell", run: "always", path: "scripts/mm/startup.sh"

    if ENV['START_TEST_OBSERVERS'] == 'true'
        config.vm.provision "shell", run: "always", path: "scripts/testobservers/bootstrap.sh"
        config.vm.provision "shell", run: "always", path: "scripts/testobservers/startup.sh"
    end        

    # http://fgrehm.viewdocs.io/vagrant-cachier
    if Vagrant.has_plugin?("vagrant-cachier")
      config.cache.scope = :box
    end

  end
end
