#! /bin/bash

# while read loop suddenly breaks being used instead of for :(
# can anyone explain me why?
for hw in $(find . -maxdepth 1 -mindepth 1 -type d) ; do
    if [ -f "$hw/build.gradle" ] ; then
        echo "processing $hw"
        cd "$hw"                              || { echo "cd $hw failed" ; exit 1 ; }
        gradle wrapper --gradle-version 4.5.1 || { echo "gradle wrapper update failed" ; exit 1 ; }
        ./gradlew check -i                    || { echo "check failed" ; exit 1 ; }
        cd ..                                 || { echo "cd .. failed" ; exit 1 ; }
        echo "finishing $hw"
    fi
done
