#!/bin/bash

<#noparse>
script_folder=$(dirname `readlink -f "$0"`)
name=$(basename $script_folder)

echo $name

docker exec -it ${name}_$1_1 /bin/bash
</#noparse>