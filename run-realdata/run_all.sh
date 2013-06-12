#!/bin/bash

# Number of nodes to run
N=$1

# Replication Degree

W=$2

for i in 2
do
    cp ../hyperdex_aux/workloads/Workload$W$i Config
    cp ../hyperdex_aux/spaces/config-$W$i/*.py .
    sleep 3

    for j in {1..10}
    do
        ./repeat.sh $N $j
        sleep 5
        scp -r space$j cloudtm:nas/Workloads/Workload$W$i/
        sleep 1
        rm -rf space$j
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
