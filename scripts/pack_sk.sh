#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"
PWD=$(pwd)

cd $DIR

${DIR}/gradlew clean publishToMavenLocal
if [[ ! $? -eq 0 ]]; then
    echo "Error compiling voodoo"
    exit 1
fi

pack=$1

[[ ! -e samples ]] && mkdir samples
cd samples

./gradlew "$pack" --args "pack sk" -Si
if [[ ! $? -eq 0 ]]; then
    echo "Error packing sk of $pack"
    exit 1
fi

cd run
cd "$pack"

#echo validating skpackaging with original
#java -jar /home/nikky/dev/Launcher/launcher-builder/build/libs/launcher-builder-4.3-SNAPSHOT-all.jar \
#  --version 1.0 \
#  --input $DIR/run/$pack/workspace/$pack \
#  --output $DIR/run/$pack/workspace/_upload2 \
#  --manifest-dest $DIR/run/$pack/workspace/_upload2/$pack.json
