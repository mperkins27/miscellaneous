#!/bin/bash

sequ=100
a=5

while (( $a <= $sequ )); do
        sbatch B17.sh $a
        let a+=5
done