RUNTIME = ${DPJ_ROOT}/Implementation/Runtime

numthread ?= 1

all: jgfutil-files dpj-files java-files original-code test-files

jgfutil-files:
	javac -d classes jgfutil/*.java

dpj-files:
	dpjc -cp ${RUNTIME}/classes:. -d java dpj/*.java ${DPJ_ROOT}/Implementation/Runtime/dpj/*.java

java-files:
	javac -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. -d classes java/*.java

original-code:
	javac original/crypt/*.java

run: runA runB runC

runA: runA-jg runA-dpj

runB: runB-jg runB-dpj

runC: runC-jg runC-dpj

runA-jg:
	echo "Running Java Grande"
	cd original; java -cp ../:. JGFCryptBenchSizeA $(num)

runB-jg:
	echo "Running Java Grande"
	cd original; java -cp ../:. JGFCryptBenchSizeB $(num)

runC-jg:
	echo "Running Java Grande"
	cd original; java -cp ../:. JGFCryptBenchSizeC $(num)	

runA-dpj:
	echo "Running DPJ"
	cd classes; java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. JGFCryptBenchSizeA $(num)

runB-dpj:
	echo "Running DPJ"
	cd classes; java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. JGFCryptBenchSizeB $(num)

runC-dpj:
	echo "Running DPJ"
	cd classes; java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:${RUNTIME}/classes/EDU/oswego/cs/dl/util/concurrent:. JGFCryptBenchSizeC --dpj-foreach-cutoff 1 --dpj-num-threads $(numthread) $(num)

harness:
	echo "Running Harness"
	cd classes; java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. JGFCryptBenchSize --dpj-foreach-cutoff $(numforeach) --dpj-num-threads $(numthread) $(mode) $(num)

harness1:
	cd classes; java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. JGFCryptBenchSize --dpj-num-threads $(numthread) $(mode) $(num)

harness-strace:
	cd classes; strace -T java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. JGFCryptBenchSize --dpj-num-threads $(numthread) $(mode) $(num)

idealharness:
	cd classes; java -cp ${DPJ_ROOT}/Implementation/Runtime/classes:. JGFCryptBenchSize $(mode) $(num)

test-files:
	dpjc -cp ${RUNTIME}/classes:classes -d test-java test-dpj/*.java ${DPJ_ROOT}/Implementation/Runtime/dpj/*.java
	javac -cp ${DPJ_ROOT}/Implementation/Runtime/classes:classes -d classes test-java/*.java 
	cd original; javac -cp ../:. *.java

IMPLEMENTATION=${DPJ_ROOT}/Implementation
RUNTIME=${IMPLEMENTATION}/Runtime
SCRIPTS=${DPJ_ROOT}/Benchmarks/Scripts

countAll ::
	dpjc -count -cp ${RUNTIME}/classes:. -d java dpj/*.java ${DPJ_ROOT}/Implementation/Runtime/dpj/*.java

count ::
	perl ${DPJ_ROOT}/Benchmarks/Scripts/count.pl ${RUNTIME} .

clean:
	rm -rf classes/*
	rm -f original/*.class
	rm -f original/crypt/*.class
