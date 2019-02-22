#!/bin/bash

SCRIPT=$(readlink -f "$0")
FOLDER=$(dirname "$SCRIPT")
pushd $FOLDER/.. > /dev/null
PANDA_HOME=`pwd`
popd > /dev/null

java -cp $PANDA_HOME/lib/*: -DPANDA_HOME=$PANDA_HOME -Dfile.encoding=UTF-8 com.justai.cm.CLI "$@"
