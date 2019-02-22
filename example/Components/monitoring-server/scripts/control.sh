#!/bin/bash

cd /opt/monitoring-server/
case $1 in
    deploy)
        mkdir /opt/monitoring-server/prometheus_data 2>/dev/null
        mkdir /opt/monitoring-server/grafana_data 2>/dev/null
        chmod 777 -R /opt/monitoring-server/prometheus_data
        chmod 777 -R /opt/monitoring-server/grafana_data

        ./restart.sh -d
    ;;
    start)
        ./start.sh -d
    ;;
    restart)
        ./restart.sh -d
    ;;
    reload)
        curl -X POST http://localhost:7010/-/reload
    ;;
    stop)
        ./kill.sh
    ;;

    *)
        echo Command $1 is not supported
esac
