#! /bin/bash

find . -maxdepth 1 -mindepth 1 -type d | while read -r hw ; do
    if [[ "$hw" == ./hw* ]] ; then
        echo "$hw"
        cd "$hw" && gradle check || exit 1
    fi
done

