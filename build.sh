#!/bin/sh

rm `find ./ -name "*.class"` 2>/dev/null

if [ $# -ge 1 ]
then
	if [ $1 = "-c" ]
	then
		exit 0
	fi
fi

javac common/*.java frontend/*.java render/*.java gui/*.java

if [ $# -ge 1 ]
then
	./run.sh $1
fi
