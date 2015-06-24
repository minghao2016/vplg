#!/bin/sh
## vis_graph_sim.sh -- Visualize Graph Similarity -- written by Tim Schaefer, 2015
##
## This script uses bk_protsim to compute common substructures in 2 graphs, then visualizes such a common substructure in both graphs using plcc.
## 
## USAGE: ./vis_graph_sim.sh <first_graph.gml> <second_graph.gml> [<result_mapping_number>]
## <first_graph.gml> : a graph file in GML format
## <second_graph.gml> : a graph file in GML format
## <result_mapping_number> : optional, default=0. the result mapping to use. several mappings may be computed by bk_protsim (as files name 'results_<mapping>_first.txt' and 'results_<mapping>_second.txt'), this defines which one to use.
##
## This scipt is part of the bk_protsim software package.
##



APPTAG="[VGS] "

echo "$APPTAG === vis_graph_sim.sh -- Visualize Graph Similarity ==="
echo "$APPTAG written by Tim Schaefer, 2015. See the script header for USAGE info and settings."

FIRST_GRAPH="7tim_A_albelig_PG.gml"
if [ ! -z "$1" ]; then
  FIRST_GRAPH="$1"
fi

SECOND_GRAPH="7tim_A_albe_PG.gml"
if [ ! -z "$2" ]; then
  SECOND_GRAPH="$2"
fi

RESULT_NUMBER="0"
if [ ! -z "$3" ]; then
  RESULT_NUMBER="$3"
fi

RESULTS_FIRST="results_${RESULT_NUMBER}_first.txt"
RESULTS_SECOND="results_${RESULT_NUMBER}_second.txt"

echo "$APPTAG Using graph first from file '$FIRST_GRAPH'."
echo "$APPTAG Using graph second from file '$SECOND_GRAPH'."
echo "$APPTAG Will try to use BK mapping result '$RESULT_NUMBER'."


BK="./bk_protsim"
PLCC="plcc.jar"



if [ ! -f "$BK" ]; then
  echo "$APPTAG ERROR: Missing bk_protsim binary, please copy it to '$BK'."
  exit 1
fi

if [ ! -f "$PLCC" ]; then
  echo "$APPTAG ERROR: Missing plcc JAR file, please copy it to '$PLCC'."
  exit 1
fi

echo "$APPTAG Deleting old results files..."
for FILE in results_*; do
  rm $FILE
done

echo "$APPTAG Running bk_protsim on the two input graphs to detect common substructures..."

$BK "$FIRST_GRAPH" "$SECOND_GRAPH" -l -f

NUM_RES=$(ls results_* | grep first | wc -w)
echo "$APPTAG Found a total of $NUM_RES possible mappings generated by bk_protsim (starting at 0). Using result index '$RESULT_NUMBER'."

if [ ! -f "$RESULTS_FIRST" ]; then
  echo "$APPTAG ERROR: No results file '$RESULTS_FIRST' produced for graph first by bk_protsim."
  exit 1
fi

if [ ! -f "$RESULTS_SECOND" ]; then
  echo "$APPTAG ERROR: No results file '$RESULTS_SECOND' produced for graph second by bk_protsim."
  exit 1
fi

echo "$APPTAG Visualizing common substructure in both graphs."
echo "$APPTAG   Running PLCC for first graph '$FIRST_GRAPH'..."
java -jar $PLCC NONE --draw-gml-graph "$FIRST_GRAPH" "$RESULTS_FIRST"

echo "$APPTAG   Running PLCC for second graph '$SECOND_GRAPH'..."
java -jar $PLCC NONE --draw-gml-graph "$SECOND_GRAPH" "$RESULTS_SECOND"

echo "$APPTAG PLCC runs finished.Check output above for errors."
echo "$APPTAG If none, see files '$FIRST_GRAPH.png' and '$SECOND_GRAPH.png' for result images. Exiting."
