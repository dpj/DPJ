TARGET=DPJ-Tutorial-v1.0
include ../Makefile.common

CLEAN = make -C Listings clean; make -C Programs clean

${TARGET}.pdf : ${TEXFILES} ${PROGRAMS} hevea.sty
	make -C Listings
	pdflatex $(TARGET).tex
	pdflatex $(TARGET).tex

