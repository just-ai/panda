#!/bin/bash

script_folder=$(dirname `readlink -f "$0"`)
name=$(basename $script_folder)

/usr/local/bin/docker-compose -f docker-compose.yml -p $name up $1

