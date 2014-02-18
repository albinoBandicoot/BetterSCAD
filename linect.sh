#!/bin/bash

# A little script for printing out a nice linecount.
for file in *
do
	if [ -d $file ]
	then
		ls $file/*.java > /dev/null 2> /dev/null
		if [ $? -eq 0 ]
		then
			echo -n $file:
			wc -l $file/*.java | tail -n 1
		fi
	fi
done
echo -n 'TOTAL:'
wc -l `find ./ -name *.java` | tail -n 1
