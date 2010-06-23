SRCS= ${wildcard dpj/*.java}
OLDEN= olden/original
include ../../Makefile.common

run :
	java -cp classes BarnesHut ${NBODY}

test:
	${DPJ-PAR} BarnesHut --dpj-num-threads 1 100 1 true > output
	diff --brief -s output ref.output
	
run-inst :
	${DPJ-INST} BarnesHut ${NBODY}

run-seq :
	${DPJ-SEQ} BarnesHut ${NBODY}

run-par:
	${DPJ-PAR} BarnesHut --dpj-num-threads ${TN} ${NBODY}

run-par1:
	java -cp ${RUNTIME}/classes:parallel/classes:${RUNTIME}/classes/EDU/oswego/cs/dl/util/concurrent BarnesHut --dpj-num-threads ${TN} ${NBODY}
