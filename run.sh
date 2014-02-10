#!/bin/sh

if [ -e tests/$1.scad ] 
then
	java frontend/Test tests/$1.scad
else
	echo "No such file (tests/$1.scad)"
fi
