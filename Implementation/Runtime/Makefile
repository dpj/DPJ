#UNCHECKED= -Xlint:unchecked

ifndef INSTRUMENT		## set this in your env to change behavior
INSTRUMENT=#-instrument		## or take out the comment on this line
endif

all : 
	cp -Rf forkjoin jsr166y extra166y java
	# Compile FJTask
	javac -d classes java/forkjoin/*.java
	make runtime-only
	make dpjdoc-html

runtime-only:
	# Generate Java 'template' classes
	perl expandAll.pl
	# Compile the DPJ runtime
	dpjc -seq ${INSTRUMENT} -d java -cp classes ${UNCHECKED} dpj/*.java
	javac -cp classes -d classes java/DPJRuntime/*.java

# Count DPJ Annotations
countAll :
	dpjc -seq -count -d java -cp classes dpj/*.java

classes/DPJRuntime/%.class: java/DPJRuntime/%.java
	javac -d classes/DPJRuntime $<

java/DPJRuntime/%.java: dpj/%.java
	dpjc -seq ${INSTRUMENT} -d java ${UNCHECKED} $<


# Build javadoc for the OLD version of FJTask that the compiler still uses
# This includes the OLD version of ParallelArray, which we do not use
javadoc-forkjoin :
	javadoc forkjoin/*java -d $@

DPJDOC=${DPJ_ROOT}/Implementation/Compiler/build/bootstrap/bin/javadoc
dpjdoc-html : dpjdoc dpj/*java
	# Generate html documentation for runtime classes
	${DPJDOC} dpj/*java -classpath classes -d dpjdoc

dpjdoc :
	mkdir $@

clean :
	rm -Rf classes/*
	rm -Rf java/*
	rm -Rf dpjdoc
	perl clearAll.pl