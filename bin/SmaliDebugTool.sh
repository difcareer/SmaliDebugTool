#!/bin/bash

cur=`pwd`

# 支持相对路径转绝对路径
# mac下readlink不能正常执行，使用greadlink，参考：
# https://stackoverflow.com/questions/1055671/how-can-i-get-the-behavior-of-gnus-readlink-f-on-a-mac
unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=Linux;;
    Darwin*)    machine=Mac;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac

if [ "$machine" = "Mac" ]
then
abs1=`greadlink -f $1`
abs2=`greadlink -f $2`
echo "Cool Beans"
elif [ "$machine" = "Linux" ]
then
abs1=`readlink -f $1`
abs2=`readlink -f $2`
else
abs1=$1
abs2=$2
fi
echo $abs1
echo $abs2

cd `dirname $0`


jdk8
java -jar SmaliDebugTool.jar $abs1 $abs2

cd ${cur}