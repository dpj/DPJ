PACKAGE=DPJBenchmarks
OLDEN= olden/original
include ../../Makefile.common
BH=DPJBenchmarks.BarnesHut

NBODY ?= 100000

test:
	$(DPJ-PAR) $(BH) --dpj-num-threads 10 100000 true > output
	diff --brief -s output ref.output

run-inst :
	$(DPJ-INST) $(BH) $(NBODY) $(DEBUG)

run-seq :
	$(DPJ-SEQ) $(BH) $(NBODY) $(DEBUG)

run-par:
	$(DPJ-PAR) $(BH) --dpj-num-threads $(TN) $(NBODY) $(DEBUG)

