#!/usr/bin/env bash

set -e

## Always test and build debug (ARM7)
commandBuilder="clean testArm7DebugUnitTest lintArm7Debug assembleArm7Debug -PdisablePreDex"

if [ -z "$CIRCLE_PR_REPONAME" ]; then
    ## It is not fork repo -> + test and build release arm7
    commandBuilder="$commandBuilder testArm7ReleaseUnitTest lintArm7Release assembleArm7Release"
    echo "===========> Build releases"
fi

if [[ "$CIRCLE_TAG" =~ ^release-.* ]]; then
    ## Release -> build all
    commandBuilder="clean testReleaseUnitTest lintRelease assembleRelease publishApks -PdisablePreDex"
else
    echo "===========> Skip PlayStore publish"
fi

./gradlew ${commandBuilder}

./copy_artifacts.sh

if [ ! -z "$CIRCLE_TAG" ]; then
    echo "===========> Create Github release"
    curl -O https://storage.googleapis.com/golang/go1.6.linux-amd64.tar.gz
    sudo tar -xf go1.6.linux-amd64.tar.gz
    sudo chmod +x go/bin/go
    export GOROOT=$HOME/go
    export PATH=$PATH:$GOROOT/bin
    ./go/bin/go get github.com/tcnksm/ghr
    ghr \
        -t "$GITHUB_TOKEN" \
        -u 'Khang-NT' \
        -r 'Android-Media-Converter' \
        -c "$CIRCLE_SHA1" \
        -delete \
        -draft \
        "$CIRCLE_TAG" artifacts/apks
fi


