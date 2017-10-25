#!/bin/bash

cur = `pwd`
cd `dirname $0`

jdk8
java -jar SmaliDebugTool.jar $1 $2

cd ${cur}