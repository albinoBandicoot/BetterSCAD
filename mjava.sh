#!/bin/sh

# a little utility for making java files that puts in the package name based on the 
# current directory and fills in 'public class <name> { }'

while [ $# -ge 1 ] 
do
echo 'package' `basename $PWD` ';
public class ' $1 '{
}' > $1.java
shift
done
