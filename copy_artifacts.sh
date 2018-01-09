#!/usr/bin/env bash

set -e
set -x

artifact_folder="artifacts"
mkdir ${artifact_folder}

for path in $(find app/build/outputs -name '*.apk' -o -name 'mapping.txt');
do
    mkdir -p "$artifact_folder/$path"
    cp ${path} "$artifact_folder/$path"
done