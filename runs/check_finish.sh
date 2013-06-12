#!/bin/bash

IPS=($(cat servers.txt))
N=$1

for i in {1..60}
do
    ALL_DONE=1
    sleep 3
    for ((j=1; j<=$N; j++))
    do
        RES=`ssh ${IPS[$j]} ps aux | grep morazow | grep java | grep -v "grep" | wc -l`
        if [[ $RES -eq "0" ]]
        then
            echo "NODE-${IPS[$j]} Finished!"
        else
            echo "NODE-${IPS[$j]} Still Running!"
            ALL_DONE=0
        fi
    done
    if [[ $ALL_DONE -eq 1 ]]
    then
        echo "THEY ALL DONE!!!"
        break
    fi
done

echo "STARTING TO COLLECT"
