#!/bin/bash

case $1 in
    deploy|start|reload)

        yum install -y firewalld
        systemctl start firewalld
        systemctl enable firewalld

        # render config for trusted zone
cat > /etc/firewalld/zones/trusted.xml << EOF
<?xml version="1.0" encoding="utf-8"?>
<zone target="ACCEPT">
  <short>Trusted</short>
  <description>All network connections are accepted.</description>
<#list env.hosts as host>
  <source address="${host.props.ip!host.ip}"/> <!-- ${host.name} / ${host.fqdn}-->
</#list>
</zone>
EOF
        # reload
firewall-cmd --reload
    ;;
    undeploy|stop)
cat > /etc/firewalld/zones/trusted.xml << EOF
<?xml version="1.0" encoding="utf-8"?>
<zone target="ACCEPT">
  <short>Trusted</short>
  <description>All network connections are accepted.</description>
</zone>
EOF
firewall-cmd --reload
    ;;
    *)
        echo Command $1 is not supported
esac
