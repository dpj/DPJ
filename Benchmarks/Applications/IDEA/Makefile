IMPLEMENTATION=$(DPJ_ROOT)/Implementation
RUNTIME=$(IMPLEMENTATION)/Runtime
SCRIPTS=$(DPJ_ROOT)/Benchmarks/Scripts
RUNTIME_SRC=$(RUNTIME)/dpj/*.java
CP = $(RUNTIME)/classes:jgfutil-classes:idea-classes:test-classes

NT ?= 1
CUTOFF ?= 1
MODE ?= TEST

# SIZE must be 1,2, or 3.  It's not the problem size, but an index
# into the space of available sizes.  See
# original/crypt/JGFCryptBench.java.

SIZE ?= 1

all: test-classes original-code

jgfutil-classes : jgfutil/*.java
	rm -Rf $@
	mkdir $@
	javac -d $@ $+

idea-java : jgfutil-classes dpj/*.java
	rm -Rf $@
	mkdir $@
	dpjc -cp $(CP) -d $@ dpj/*.java $(RUNTIME_SRC)

idea-classes : idea-java
	rm -Rf $@
	mkdir $@
	javac -cp $(CP) -d $@ idea-java/*.java

test-java : idea-classes test-dpj/*.java
	rm -Rf $@
	mkdir $@
	dpjc -cp $(CP) -d $@ test-dpj/*.java $(RUNTIME_SRC)

test-classes : test-java
	rm -Rf $@
	mkdir $@
	javac -cp $(CP) -d $@ test-java/*.java

original-code:
	javac original/crypt/*.java
	cd original; javac -cp ../:. *.java

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
	java -cp $(CP) JGFCryptBenchSizeA $(num)

runB-dpj:
	echo "Running DPJ"
	java -cp $(CP) JGFCryptBenchSizeB $(num)

runC-dpj:
	echo "Running DPJ"
	java -cp $(CP) JGFCryptBenchSizeC --dpj-foreach-cutoff 1 --dpj-num-threads $(NT) $(num)

harness:
	echo "Running Harness"
	java -cp $(CP) JGFCryptBenchSize --dpj-foreach-cutoff $(CUTOFF) --dpj-num-threads $(NT) $(MODE) $(SIZE)

harness1:
	java -cp $(CP) JGFCryptBenchSize --dpj-num-threads $(NT) $(MODE) $(SIZE)

harness-strace:
	strace -T java -cp $(CP) JGFCryptBenchSize --dpj-num-threads $(NT) $(MODE) $(SIZE)

idealharness:
	java -cp $(CP) JGFCryptBenchSize $(MODE) $(SIZE)

countAll ::
	dpjc -count -cp $(RUNTIME)/classes:. -d java dpj/*.java $(RUNTIME)/dpj/*.java

count ::
	perl $(DPJ_ROOT)/Benchmarks/Scripts/count.pl $(RUNTIME) .

clean:
	rm -Rf jgfutil-classes idea-classes idea-java test-classes test-java
	rm -f original/*.class
	rm -f original/crypt/*.class
