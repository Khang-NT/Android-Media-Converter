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

./gradlew ${commandBuilder} -PdisablePreDex

./copy_artifacts.sh

if [ ! -z "$CIRCLE_TAG" ]; then
    echo "===========> Create Github release"
    curl -L -O 'https://github.com/tcnksm/ghr/releases/download/v0.5.4/ghr_v0.5.4_linux_amd64.zip'
    unzip ghr_v0.5.4_linux_amd64.zip -d .
    sudo chmod +x ghr
    ./ghr \
        -t "$GITHUB_TOKEN" \
        -u 'Khang-NT' \
        -r 'Android-Media-Converter' \
        -c "$CIRCLE_SHA1" \
        -delete \
        "$CIRCLE_TAG" artifacts/apks
fi


