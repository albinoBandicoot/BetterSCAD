#!/bin/sh

rm `find ./ -name "*.class"`

if [ $# -ge 1 ]
then
	if [ $1 = "-c" ]
	then
		return 0
	fi
fi

javac common/*.java
javac frontend/*.java

if [ $# -ge 1 ]
then
	./run.sh $1
fi
