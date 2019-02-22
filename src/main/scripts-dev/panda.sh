#!/bin/bash

SCRIPT=$(readlink -f "$0")
FOLDER=$(dirname "$SCRIPT")
pushd $FOLDER/.. > /dev/null
PANDA_HOME=`pwd`
popd > /dev/null

# classes path was added for more convenient development with IDEA
java -cp $PANDA_HOME/../classes/:$PANDA_HOME/lib/*: -DPANDA_HOME=$PANDA_HOME -Dfile.encoding=UTF-8 com.justai.cm.CLI "$@"

