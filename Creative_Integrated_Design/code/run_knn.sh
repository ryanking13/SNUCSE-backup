#!/bin/bash

d_method='DTW'
n_method='BORDA'
for i in {11..11}
do
	for j in {1..20}
	do
		python3 train_KNN.py $j $i $d_method $n_method
	done
done
