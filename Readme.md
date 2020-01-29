### Usage SUBDUE to mine library usage pattern ###

This set of scripts is to use SUBDUE to discover library usage patterns.
SUBDUE is used to mine subgraphs from clusters of groums which produced by intigroum.
Thus we need to execute intigroum to produce those clusters.

To use intigroum to produce clusters, use the following commands:
1. extract groums from project
`./extract_groums.sh PROJECT_DIR LIBRARIES`
By default, the output groums are stored in `intigroums/biggroum-extractor/out` directory
2. mine library usage patterns
`./mine_groums.sh MIN_FREQ MIN_SIZE GROUM_DIRECTORY`
After running this command, intigroum generates a set of clusters. By default, they are stored in `intigroums/FixrGraphIso/build/src/fixrgraphiso/clusters/all_clusters`

The groums are in the form of BIN. We need to transform them into JSON. To do that, using the following command: 
`./convertGroumsToJSON.sh ALL_CLUSTERS_DIRECTORY JSON_DIRECTORY NUMBER_CLUSTERS`
- ALL_CLUSTERS_DIRECTORY: a directory contains the output of mine_groums.sh
	by default, it is `intigroums/FixrGraphIso/build/src/fixrgraphiso/clusters/all_clusters`
- JSON_DIRECTORY: a directory contains json files
- NUMBER_CLUSTERS: number of clusters
**Note that, this script uses `IntigroumsConverter` to covert bin to XML. Thus, we need to modify the path indicated in this script correctly.**

Once JSON files are created, we can execute SUBDUE to find subgraphs from each cluster by the following command:
`./runSubdue.sh JSON_DIRECTORY GRAPHS_DIRECTORY`
- JSON_DIRECTORY : a directory contains json files, each json file corresponds to a cluster
- GRAPHS_DIRECTORY: a directory contains output of SUBDUE. It includes .dot files and .png files which contain the output subgraphs.