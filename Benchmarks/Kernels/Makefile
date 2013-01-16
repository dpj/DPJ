include ../Makefile.common

# Problem size
SIZE ?= 1024

# Foreach cutoff (min. data size)
DPJ_FC ?= 1

# Number of threads
DPJ_NT ?= 1

# "Pushbutton" targets
test-all :
	make test-all-seq
	make test-all-par

test-all-seq :
	${DPJ-SEQ-TEST} ListRanking TEST ${SIZE}
	${DPJ-SEQ-TEST} MergeSort4 TEST ${SIZE}
	${DPJ-SEQ-TEST} MergeSort8 TEST ${SIZE}
	${DPJ-SEQ-TEST} SumReduce TEST ${SIZE}
	${DPJ-SEQ-TEST} Quadtree TEST ${SIZE}

test-all-par :
	${DPJ-PAR-TEST} ListRanking TEST ${SIZE}
	${DPJ-PAR-TEST} MergeSort4 TEST ${SIZE}
	${DPJ-PAR-TEST} MergeSort8 TEST ${SIZE}
	${DPJ-PAR-TEST} SumReduce TEST ${SIZE}
	${DPJ-PAR-TEST} Quadtree TEST ${SIZE}

# Targets requiring command-line arguments
# E.g.:
#	make test-par PROG=ListRanking
#	make test-par PROG=ListRanking SIZE=4096
#	make time-par PROG=ListRanking DPJ_NT=2 DPJ_FC=512
test-par :
	${DPJ-PAR-TEST} ${PROG} TEST ${SIZE} ${ARG2} ${ARG3}

test-seq :
	${DPJ-SEQ-TEST} ${PROG} TEST ${SIZE} ${ARG2} ${ARG3}

ideal :
	${DPJ-INST} ${PROG} IDEAL ${SIZE} ${ARG2} ${ARG3}

time-seq :
	${DPJ-SEQ} ${PROG} TIME ${SIZE} ${ARG2} ${ARG3}

time-par :
	${DPJ-PAR} ${PROG} --dpj-foreach-cutoff ${DPJ_FC} --dpj-num-threads ${DPJ_NT} \
  TIME ${SIZE} ${ARG2} ${ARG3}

.PRECIOUS: java/%.java
