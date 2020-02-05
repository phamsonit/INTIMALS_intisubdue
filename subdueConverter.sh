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
nb_cluster=$3

#if nb_cluster == 1, convert entire groums of a project to xmls
if [ $nb_cluster == 1 ]; then
	#delete old xml files
	rm -f $inputDir/*.xml
	#convert .bin to .xml
	# Be sure to adapt this variable to your run!
	#java -jar ../intigroums/IntigroumsConverter/out/artifacts/main_jar/main.jar -binpath $inputDir
	java -jar IntigroumsConverter.jar -binpath $inputDir
	#delete xmls which are not groums
	rm -f $inputDir/iso*
	rm -f $inputDir/pop*
	rm -f $inputDir/anom*
	java -jar subdueConverter/out/artifacts/subdueConverter_jar/subdueConverter.jar $inputDir $outputDir/groums.json	
	exit 0
fi

#if nb_cluster > 1, then convert groum in clusters to xmls
echo convert groums to xmls ...
for i in $(seq 1 $nb_cluster)
do
	echo covert cluster: $i
	clusterDir=$inputDir/cluster_$i
	#delete old xml files in the current cluster
	rm -f $clusterDir/*.xml
	#convert .bin to .xml
	# Be sure to adapt this variable to your run!
	java -jar ../intigroums/IntigroumsConverter/out/artifacts/main_jar/main.jar -binpath $clusterDir
	#delete xmls which are not groums
	rm -f $clusterDir/iso*
	rm -f $clusterDir/pop*
	rm -f $clusterDir/anom*
done

echo convert xmls to json files
for i in $(seq 1 $nb_cluster)
do
	echo "covert cluster: " $i
	java -jar subdueConverter/out/artifacts/subdueConverter_jar/subdueConverter.jar $inputDir/cluster_$i $outputDir/cluster_$i.json
done
