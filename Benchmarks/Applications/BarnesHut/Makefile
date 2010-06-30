SRCS= ${wildcard dpj/*.java}
OLDEN= olden/original
include ../../Makefile.common

NBODY ?= 100000

run :
	java -cp classes BarnesHut ${NBODY}

test:
	${DPJ-PAR} BarnesHut --dpj-num-threads 10 100000 true > output
	diff --brief -s output ref.output

run-inst :
	${DPJ-INST} BarnesHut ${NBODY} ${DEBUG}

run-seq :
	${DPJ-SEQ} BarnesHut ${NBODY} ${DEBUG}

run-par:
	${DPJ-PAR} BarnesHut --dpj-num-threads ${TN} ${NBODY} ${DEBUG}

run-par1:
	java -cp ${RUNTIME}/classes:parallel/classes:${RUNTIME}/classes/EDU/oswego/cs/dl/util/concurrent BarnesHut --dpj-num-threads ${TN} ${NBODY}
