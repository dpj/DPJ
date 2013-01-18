#!/usr/bin/perl

my $DIR = "../Programs/dpj";

foreach (`ls $DIR`) {
    if (/(.*)\.java$/) {
	chomp;
	my $filename = $1;
	print `perl mklist.pl $DIR/$filename\.java > ./$filename\.tex`;
    }
}


