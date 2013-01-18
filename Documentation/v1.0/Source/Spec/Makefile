TARGET=DPJ-Spec-v1.0

include ../Makefile.common

${TARGET}.pdf : ${TEXFILES} ${BIBFILES} hevea.sty
	-rm -f *.aux
	pdflatex -draftmode $(TARGET).tex
	bibtex $(TARGET)
	pdflatex -draftmode $(TARGET).tex
	pdflatex $(TARGET).tex

