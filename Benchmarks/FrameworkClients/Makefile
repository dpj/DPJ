include ../Makefile.common

RUNTIME_JAVA += $(RUNTIME)/dpj/Framework/*.java

CUTOFF ?= 1
NTHREADS ?= 1

run-par :
	${DPJ-PAR} ${PROG} --dpj-foreach-cutoff ${CUTOFF} --dpj-num-threads ${NTHREADS}

