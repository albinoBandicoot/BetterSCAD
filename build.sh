#!/bin/sh

rm `find ./ -name "*.class"` 2>/dev/null

if [ $# -ge 1 ]
then
	if [ $1 = "-c" ]
	then
		rm `find ./ -name "*.java~"` 2>/dev/null
		exit 0
	fi
fi

javac common/*.java frontend/*.java render/*.java gui/*.java

if [ $# -ge 1 ]
then
	./run.sh $1
fi
