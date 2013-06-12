#!/bin/bash

for i in `ls`
do
    #echo "Update"
    #echo "---- $i"
    for j in $i/node-*/result.err
    do
        # Update
        #tail -n -1 $j | grep -P -o "\S*$"
        cat $j | grep "Update" | head -n -1 | grep -v "NaN" | grep -P -o "\S*$"
    done | awk '{sum+=$1} END {print sum/8.0}'
    #echo "DONE"
done


