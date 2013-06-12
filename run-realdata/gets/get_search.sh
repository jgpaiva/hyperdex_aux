#!/bin/bash

for i in `ls`
do
    #echo "Update"
    for j in $i/node-*/result.err
    do
        # Search
        #tail -n -2 $j  | head -n 1 | grep -P -o "\S*$"
        cat $j | grep "Gozleg" | head -n -1 | grep -P -o "\S*$"
    done | awk '{sum+=$1} END {print sum/8.0}'
    #echo "DONE"
done


