#!/bin/bash

cd /opt/monitoring-node-exporter/
case $1 in
    start)
        ./start.sh -d
    ;;
    deploy|restart|reload)
        ./restart.sh -d
    ;;
    stop)
        ./kill.sh
    ;;

    *)
        echo Command $1 is not supported
esac
