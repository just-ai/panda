#!/bin/bash

case $1 in
    deploy)
        # These commands was copied from https://docs.docker.com/install/linux/docker-ce/centos/
        yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

        sudo yum install -y yum-utils \
          device-mapper-persistent-data \
          lvm2

        yum-config-manager \
            --add-repo \
            https://download.docker.com/linux/centos/docker-ce.repo

        yum install -y docker-ce docker-ce-cli containerd.io


        systemctl start docker
        systemctl enable docker

        usermod ${localProps.ssh_user} -G docker


        # Install docker-compose
        rm -f /usr/local/bin/docker-compose

        curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
    ;;
    *)
        echo Command $1 is not supported
esac
