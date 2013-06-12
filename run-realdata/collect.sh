#!/bin/bash

IPS=($(cat servers.txt))
PREFIX=`date +%s`
N=$1

for ((i=1; i<=$N; i++))
do
    echo "COPYING-FROM-${IPS[$i]}"
    mkdir -p $PREFIX/node-$i
    scp ${IPS[$i]}:~/Runs/result.* $PREFIX/node-$i/
done

