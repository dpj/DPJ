#!/usr/bin/perl

my $filename = shift @ARGV;

print "\\begin{numbereddpjlisting}\n";
print `cat $filename`;
print "\\end{numbereddpjlisting}\n";

