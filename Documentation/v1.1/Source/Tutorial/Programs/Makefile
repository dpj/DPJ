include ${DPJ_ROOT}/Benchmarks/Makefile.common

CUTOFF ?= 1
NUM_THREADS ?= 1

run-seq :
	${DPJ-SEQ} ${PROG}

run-par :
	${DPJ-PAR} ${PROG} --dpj_foreach_cutoff ${CUTOFF} --dpj_threadnum ${NUM_THREADS}
