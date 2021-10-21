#!/bin/bash

for i in {11..11}
do
	for j in {1..20}
	do
		python3 train_LSTM.py $j $i
	done
done
