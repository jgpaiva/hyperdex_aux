#!/bin/bash

# number of severs
N=$1

# subspace config
S=$2

./deploy.sh $N
sleep 5
./run2.sh $N $S
sleep 3

./deploy.sh $N
sleep 5

for k in {1..2}
do
    ./run2.sh $N $S
    sleep 5
done

mkdir -p space$S
mv 13* space$S/
sleep 7

