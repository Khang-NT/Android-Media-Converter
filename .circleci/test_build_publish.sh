#!/usr/bin/env bash

set -e

## Always test and build debug (ARM7)
commandBuilder="clean testArm7DebugUnitTest lintArm7Debug assembleArm7Debug -PdisablePreDex"

if [ -z "$CIRCLE_PR_REPONAME" ]; then
    ## It is not fork repo -> + test and build release arm7
    commandBuilder="$commandBuilder testArm7Release lintArm7Release assembleArm7Release"
fi

if [[ "$CIRCLE_TAG" =~ ^release-.* ]]; then
    ## Release -> build all
    commandBuilder="clean buildRelease publishApks -PdisablePreDex"
else
    echo "===========> Skip PlayStore publish"
fi

./gradlew ${commandBuilder}

./copy_artifacts.sh

if [ ! -z "$CIRCLE_TAG" ]; then
    echo "===========> Create github release"
    go get github.com/tcnksm/ghr
    ghr \
        -t "$GITHUB_TOKEN" \
        -u 'Khang-NT' \
        -r 'Android-Media-Converter' \
        -c "$CIRCLE_SHA1" \
        -delete \
        -draft \
        "$CIRCLE_TAG" artifacts/apks
fi


