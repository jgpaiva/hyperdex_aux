#!/bin/bash

CRD=$1

java -cp .:/home/morazow/HyperDex/hyperclient-1.0.dev.jar -Djava.library.path=/home/morazow/hyperdex_install/lib Client $CRD > result.txt 2> result.err &
