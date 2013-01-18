#!/usr/bin/perl

# Expand all template files

my @targets = ( 
    ["char", "Char"], 
    ["boolean", "Boolean"],
    ["byte", "Byte"], 
    ["double", "Double"],
    ["float", "Float"],
    ["int", "Int"], 
    ["String", "String"]
);

foreach (`ls dpj`) {
    chomp;
    if (/(.*)\.tpt$/) {
	my $prefix = "dpj/".$1;
	foreach(@targets) {
	    my $typeName = $$_[0];
	    my $className = $$_[1];
	    my $inFileName = $prefix.".tpt";
	    my $outFileName = $prefix.$className.".java";
	    print `perl expand.pl $typeName $className < $inFileName > $outFileName\n`;
	}
    }
}
