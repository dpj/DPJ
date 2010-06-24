SRCS= ${wildcard dpj/*.java}
CUTOFF ?= 100
SIZE ?= 0
TN ?=1

include ../../Makefile.common

run-inst :
	${DPJ-INST} JGFMonteCarloBenchHarness IDEAL ${SIZE}

run-seq :
	${DPJ-SEQ} JGFMonteCarloBenchHarness TIME ${SIZE}

run-par:
	${DPJ-PAR} JGFMonteCarloBenchHarness --dpj-foreach-cutoff ${CUTOFF} --dpj-num-threads ${TN} TIME ${SIZE}

