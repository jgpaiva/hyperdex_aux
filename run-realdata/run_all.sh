#!/bin/bash

# Number of nodes to run
N=$1

# Replication Degree

W=$2

for i in 1 2 3
do
    cp ../hyperdex_aux/configs/Workload$W$i Config
    sleep 3

    for j in 11 12 13 14
    do
        cp ../hyperdex_aux/configs/spaces-$W$i/hotels_space_$j.py .
        sleep 2
        ./repeat.sh $N $j
        sleep 5
        scp -r space$j cloudtm:nas/Workloads/Workload$W$i/
        sleep 1
        rm -rf space$j hotels_space_$j.py
    done

    #./deploy.sh $N
    #sleep 5
    #for j in {3..4}
    #do
    #    ./repeat.sh $N $j
    #    sleep 5
    #    scp -r space$j cloudtm:nas/Workloads/WorkloadB$i/
    #    sleep 1
    #    rm -rf space$j
    #done

    #./deploy.sh $N
    #sleep 5
    #for j in {5..7}
    #do
    #    ./repeat.sh $N $j
    #    sleep 5
    #    scp -r space$j cloudtm:nas/Workloads/WorkloadB$i/
    #    sleep 1
    #    rm -rf space$j
    #done

    #./deploy.sh $N
    #sleep 5
    #for j in {8..10}
    #do
    #    ./repeat.sh $N $j
    #    sleep 5
    #    scp -r space$j cloudtm:nas/Workloads/WorkloadB$i/
    #    sleep 1
    #    rm -rf space$j
    #done

    #mkdir -p workload-$i
    #mv space{1..10} workload-$i/
    #tar -czvf workload-A1.tar.gz workload-$i/
    #scp workload-A1.tar.gz cloudtm:nas/Workloads/
    #rm -rf *.tar.gz

done
