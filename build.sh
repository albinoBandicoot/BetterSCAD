#!/bin/sh

rm `find ./ -name "*.class"`
javac common/*.java
javac frontend/*.java

if [ $# -ge 1 ]
then
	./run.sh $1
fi
