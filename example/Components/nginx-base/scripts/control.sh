#!/bin/bash

case $1 in
    deploy)
        yum install -y epel-release

        yum install -y nginx
        systemctl start nginx
        systemctl enable nginx

        firewall-cmd --permanent --zone=public --add-service=http
        firewall-cmd --permanent --zone=public --add-service=https
        firewall-cmd --reload
    ;;
    reload)
        sudo nginx -s reload
    ;;
    start|restart|stop)
        systemctl $1 nginx
    ;;
    *)
        echo Command $1 is not supported
esac
