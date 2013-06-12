#!/bin/bash

for i in 1; do rm -rf space$i/*.sh; done
for i in 1; do cp $1 space$i/; cd space$i/; echo "/////////////////// SPACE-$i"; ./$1; cd ..; done
