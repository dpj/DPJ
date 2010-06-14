#!/usr/bin/perl

# Expand a template file

use strict;

if (@ARGV == 0) {
    die ("Please specify a type to substitute for \$");
}

my $type = $ARGV[0];
my $className = $type;
if (@ARGV > 1) {
    $className = $ARGV[1];
}

print "// WARNING:  THIS FILE IS AUTO-GENERATED\n\n";

while(<STDIN>) {
    chomp;
    my $line = $_;
    $line =~ s/\$\$/$className/g;
    $line =~ s/\$/$type/g;
    print "$line\n";
}
