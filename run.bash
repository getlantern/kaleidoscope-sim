#!/usr/bin/env bash
function die() {
  echo $*
  exit 1
}

mvn package || die "Could not package kscope?"

fullPath=`dirname $0`
jar=`find $fullPath/target/*with-dependencies.jar`
cp=`echo $jar | sed 's,./,'$fullPath'/,'`
javaArgs="-jar "$cp" $*"

echo "Running using Java on path at `which java` with args $javaArgs"
java $javaArgs || die "Java process exited abnormally"
