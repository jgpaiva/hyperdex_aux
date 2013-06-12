#! /bin/bash

IPS=($(cat servers.txt))
CRD=${IPS[0]}
N=$1

echo "CLEAN"
rm -rf /tmp/hyperdex/hyperdex_* hyperdex-* replicant-* *.INFO
kill -9 `ps aux | grep '[h]yperdex' | awk '{print $2}'`

for (( i=1; i<=$N; i++))
do
    echo "CLEAN-IN-$i"
    ssh ${IPS[$i]} "./clean.sh"
done

echo "START-COORDINATOR"
./start_coordinator.sh
sleep 5

for (( i=1; i<=$N; i++))
do
    echo "START-DAEMON-${IPS[$i]}"
    ssh ${IPS[$i]} "bash -c \"cd daemon; ./start_daemon.sh $CRD\""
    sleep 5
    #ssh node$i "cd ~/tests; export PATH=/home/morazow/hyperdex_install/bin:$PATH; ./hyp_daemon.sh"
done
