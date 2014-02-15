#!/bin/sh

egrep -H -n --color=auto "TODO|FIXME" `find ./ -name "*.java"`
