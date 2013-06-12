#!/bin/bash

for i in `ls`
do
    #echo "Update"
    for j in $i/node-*/result.*
    do
        # Errors
        cat $j | grep "hyperclient" | wc -l
    done | awk '{sum+=$1} END {print sum}'
    #echo "DONE"
done


