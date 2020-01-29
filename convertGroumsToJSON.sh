#!/bin/bash
#convert groums into json for SUBDUE


if [ "$#" -lt 3 ]; then
    echo "Usage: createJSON.sh CLUSTERS_DIRECTORY JSON_DIRECTORY NUMBER_CLUSTERS"
    exit 0
fi

#directory containing all_clusters = output of biggroum
inputDir=$1
#directory containing json files = input of subdue
outputDir=$2
#number of clusters
cluster=$3

echo convert groums to xmls ...
for i in $(seq 1 $cluster)
do
	echo covert cluster: $i
	#delete old xml files
	rm -f $inputDir/cluster_$i/*.xml
	#convert .bin to .xml
	# Be sure to adapt this variable to your run!
	java -jar ../intigroums/IntigroumsConverter/out/artifacts/main_jar/main.jar -binpath $inputDir/cluster_$i
	#delete xmls which are not groums
	rm -f $xmlsDir/iso*
	rm -f $xmlsDir/pop*
	rm -f $xmlsDir/anom*
done

echo convert xmls to json files
for i in $(seq 1 $cluster)
do
	echo "covert cluster: " $i
	java -jar subdueConverter/out/artifacts/subdueConverter_jar/subdueConverter.jar $inputDir/cluster_$i $outputDir/cluster$i.json
done
