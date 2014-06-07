#!/bin/sh

egrep -H -n -i --color=auto "TODO|FIXME|for now" `find ./ -name "*.java"`
