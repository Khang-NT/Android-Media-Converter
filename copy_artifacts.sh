#!/usr/bin/env bash

set -e

currentDir=$(pwd)
artifact_folder="$currentDir/artifacts"
apk_folder="$artifact_folder/apks"
mapping_folder="$artifact_folder/mappings"
report_folder="$artifact_folder/reports"
mkdir ${artifact_folder}
mkdir ${apk_folder}
mkdir ${report_folder}

for path in $(find app/build/outputs/apk -name '*.apk');
do
    cp ${path} ${apk_folder}
done

pushd app/build/outputs/mapping
for path in $(find . -name 'mapping.txt');
do
    mkdir -p $(dirname "$mapping_folder/$path")
    cp ${path} "$mapping_folder/$path"
done
popd

cp -r app/build/reports/* ${report_folder}
