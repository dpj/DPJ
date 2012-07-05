Windows-Related DPJ Scripts
===========================

This directory contains some scripts to enable DPJ to work (more or less) 
under Windows, using Cygwin to get a Unix-like environment.  These scripts
act as wrappers for java and javac, translating path names in command lines 
between Cygwin and Windows styles.

To install DPJ on Windows, do the following:

1. Install the JDK.

2. Install Cygwin (from cygwin.com). In addition to the default installation, 
be sure to install the "perl", "make", and "unzip" packages.  Use a Cygwin 
shell for the remaining steps.

3. Set the JAVA_HOME environment variable to point to the JDK, e.g. 
export JAVA_HOME=/cygdrive/c/Progra~1/Java/jdk1.7.0_05
(To avoid problems, we suggest using a version of the path without spaces.)

4. Do the DPJ user installation as described in the Installation Manual, but 
don't run the tests yet.

5. Add the directory $DPJ_ROOT/Scripts/Windows to the beginning of your PATH:
export PATH=$DPJ_ROOT/Scripts/Windows:$PATH
(This directory must go before any other directory containing java or javac,
so that the wrapper scripts in this directory are used when java and javac
are invoked by the DPJ tools.)

6. Now your DPJ installation should be working.  You can test it by compiling
and running the kernels, as described in the Installation Manual.  Note that
you will have to set the JAVA_HOME, DPJ_ROOT, and PATH variables each time 
you run the shell, or else put them in your shell's start-up script.
