#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PWD=$(pwd)

pack=$1
url=$2

[ ! -e run ] && mkdir run
cd run

[ ! -e "$pack" ] && mkdir "$pack"
cd "$pack"

rm src/**/*.lock.json
rm src/**/*.entry.hjson

echo
echo "importing $pack"
echo

rm -rf $pack

cd $DIR
$DIR/gradlew :voodoo:run --args "import curse $url $DIR/run/$pack"
if [ ! $? -eq 0 ]; then
    echo "Error importing $pack from yaml"
    exit 1
fi