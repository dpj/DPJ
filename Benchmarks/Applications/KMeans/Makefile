ALL=sequential-classes parallel-classes

include ../../Makefile.common

INPUT ?= inputs/color100
MIN ?= 40
MAX ?= 40
TN ?= 2

run-seq :
	${DPJ-SEQ} KMeans ${INPUT} ${MIN} ${MAX} ${TN}

run-par:
	${DPJ-PAR} KMeans --dpj-num-threads ${TN} ${INPUT} ${MIN} ${MAX} ${TN}

