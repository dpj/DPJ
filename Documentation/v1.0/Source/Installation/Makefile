TARGET=DPJ-Install-v1.0

include ../Makefile.common

${TARGET}.pdf : ${TEXFILES} hevea.sty
	-rm -f *.aux
	pdflatex -draftmode $(TARGET).tex
	pdflatex $(TARGET).tex

