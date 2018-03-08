#!/usr/bin/env bash

set -e

currentDir=$(pwd)
artifact_folder="$currentDir/artifacts"; mkdir -p ${artifact_folder}
apk_folder="$artifact_folder/apks"; mkdir -p ${apk_folder}
mapping_folder="$artifact_folder/mappings"; mkdir -p ${mapping_folder}
report_folder="$artifact_folder/reports"; mkdir -p ${report_folder}

for path in $(find app/build/outputs/apk -name '*.apk');
do
  cp ${path} ${apk_folder}
done

if [ -d app/build/outputs/mapping ]; then
  pushd app/build/outputs/mapping
    for path in $(find . -name 'mapping.txt');
    do
      mkdir -p $(dirname "$mapping_folder/$path")
      cp ${path} "$mapping_folder/$path"
    done
  popd
fi

cp -r app/build/reports/* ${report_folder}
