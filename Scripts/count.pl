#!/usr/bin/perl

#
# A hack to get around the fact that dpjc does not support separate
# compilation: Get annotation count from two different places, and
# subtract the one from the other.
#

use strict;

my $count = "countAll";
my $tareDir = shift @ARGV;
if (!$tareDir) { $tareDir = "."; }
my $measureDir = shift @ARGV;
if (!$measureDir) { $measureDir = "."; }
my %nonStatMsgs;
my @statNames = ();


print "Getting count from $tareDir\n";
my @tareStats = ();
my $counter = 0;
foreach (`make -C $tareDir $count`) { 
    chomp;
    if (/^(.*:\s*)(\d+)$/) {
	push @statNames, $1;
	push @tareStats, $2;
	++$counter;
    } elsif (/^\*\*\*/) {
	$nonStatMsgs{$counter} = $_;
    }
}

print "Getting count from $measureDir\n";
my @measureStats = ();
foreach (`make -C $measureDir $count`) { 
    chomp;
    if (/:\s*(\d+)$/) {
	push @measureStats, $1;
    }
}

foreach(0..@statNames-1) {
    if ($nonStatMsgs{$_}) {
	print "$nonStatMsgs{$_}\n";
    }
    print "$statNames[$_]".($measureStats[$_]-$tareStats[$_])."\n";
}
