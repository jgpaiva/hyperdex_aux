#!/bin/bash

for i in `ls`
do
    #echo "Update"
    for j in $i/node-*/result.txt
    do
        # Throughput
        tail -n -1 $j | grep -P -o "(?<=, )\S*"
        
        # Search
        #tail -n -2 $j  | head -n 1 | grep -P -o "\S*$"

        # Update
        #tail -n -1 $j | grep -P -o "\S*$"
        
        # Errors
        #cat $j | grep "hyperclient" | wc -l
    done | awk '{sum+=$1} END {print sum}'
    #echo "DONE"
done


