#!/bin/bash

cd `dirname $0`/..

if [ -f "bin/jar/familytree-0.0.1-SNAPSHOT.jar" ]; then
	java -jar bin/jar/familytree-0.0.1-SNAPSHOT.jar
else
	echo "Required file 'jar/familytree-0.0.1-SNAPSHOT.jar' doesn't exist"
	exit 1
fi