#! /usr/bin/env sh

jarName=`find . -type f -printf "%f\n" | grep "^KotlinFuzzer-.*-all.jar$"`
command="java -jar $jarName"
for param in $@
do
command="$command $param"
done
exec $command