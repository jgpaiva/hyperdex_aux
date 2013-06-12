#!/bin/bash

rm -rf /tmp/hyperdex/hyperdex_* hyperdex-* replicant-* *.INFO
kill -9 `ps aux | grep '[h]yperdex' | awk '{print $2}'`
sleep 5

hyperdex coordinator -p 1982 -D /tmp/hyperdex/hyperdex_coorddb
