
SRCS= ${wildcard dpj/*.java}
SRC = Cluster.java KMeans.java Normal.java Point.java RandomType.java

include ../../Makefile.common

run-inst :
	${DPJ-INST} KMeans ${INPUT} ${MIN} ${MAX} ${TN}

run-seq :
	${DPJ-SEQ} KMeans ${INPUT} ${MIN} ${MAX} ${TN}

run-par:
	${DPJ-PAR} KMeans --dpj_threadnum ${TN} ${INPUT} ${MIN} ${MAX} ${TN}

rg:
	${DPJ-PAR} KMeans --dpj_threadnum 24 color100 40 40 24 debug > output
	diff --brief -s output ref_output
