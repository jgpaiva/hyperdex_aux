#!/bin/bash

IPS=($(cat servers.txt))
CRD=${IPS[0]}
N=$1

echo "SEND-SEARCH-JAVA"
for ((i=1; i<=$N; i++))
do
    echo "SENDING-TO-${IPS[$i]}"
    scp Client.java Query.java QueryGenerator.java Config search.sh load4.py hotels_space_$2.py ${IPS[$i]}:~/Runs
done

echo "COMPILE-ALL"
for ((i=1; i<=$N; i++))
do
    echo "COMPILE-ON-${IPS[$i]}"
    ssh ${IPS[$i]} "bash -c \"cd ~/Runs; killall -9 java; javac -cp .:/home/morazow/HyperDex/hyperclient-1.0.dev.jar Client.java\""
done

echo "DELETE-SPACE"
ssh ${IPS[1]} "bash -c \"cd ~/Runs; python remove_space.py $CRD\""
sleep 6

echo "CREATE-SPACE-$2"
ssh ${IPS[1]} "bash -c \"cd ~/Runs; python hotels_space_$2.py $CRD\""
sleep 6

echo "LOAD-DATA"
ssh ${IPS[1]} "bash -c \"cd ~/Runs; python load4.py $CRD\""
sleep 6

#echo "GET-CONFIG"
#scp ${IPS[1]}:Runs/configuration.txt config-$2.txt
#sleep 2

echo "START SEARCHING"
for ((i=1; i<=$N; i++))
do
    echo "SEARCH-ON-NODE-${IPS[$i]}"
    ssh ${IPS[$i]} "bash -c \"cd ~/Runs; ./search.sh $CRD\""
done
sleep 7

./check_finish.sh $1

./collect.sh $1

