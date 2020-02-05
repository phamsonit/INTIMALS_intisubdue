#!/bin/bash


if [ "$#" -lt 2 ]; then
    echo "Usage: runSubdue.sh JSON_DIRECTORY GRAPHS_DIRECTORY"
    exit 0
fi

#inputPath = directory contains json files
inputPath=$1
if [ -d $intputPath ]; then
	echo input OK
else
	echo input not found
	exit 0
fi
#create output directory
outputPath=$2
if [ -d $outputPath ]; then rm -r $outputPath; fi
mkdir $outputPath
#mine sub-graphs
for inputCluster in $inputPath/*.json
do
	#get name of the input cluster
	clusterName="${inputCluster%.*}"
	clusterName="${clusterName##*/}"
	echo "run SUBDUE on " $clusterName
	#create temporary directory to store the result of SUBDUE
	outputTemp=outputDotDir	
	if [ -d $outputTemp ]; then rm -r $outputTemp; fi
	mkdir $outputTemp
	#run SUBDUE
	python subdue-master/src/Subdue.py --iterations 100 --writepattern --prune --beam 20 --minsize 7 --numbest 5 --outputDotFile $inputCluster > $outputTemp/result.txt
	#copy result to cluster output
	outputClusterPath=$outputPath/$clusterName
	if [ -d $outputClusterPath ]; then rm -r $outputClusterPath; fi
	mv $outputTemp $outputClusterPath

	#create graph representation
	echo "create graph ..."
	file=$outputClusterPath/graph_1.dot
	#if exist a graph
	if [ -f $file ]; then
		for dotFileName in $outputClusterPath/*.dot
		do
			graphFileName="${dotFileName%.*}"
			dot -Tpng $dotFileName -o $graphFileName.png
		done
	fi
done