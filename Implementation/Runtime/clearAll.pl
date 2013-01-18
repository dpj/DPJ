#!/usr/bin/perl

# Clear all files expanded from a template

my @targets = ( "Char", "Boolean", "Byte", "Double", 
		"Float", "Int", "String" );

sub myexec(@) {
    (my $cmd) = @_;
    print "$cmd\n";
    print `$cmd`;
}

foreach (`ls dpj`) {
    chomp;
    if (/(.*)\.tpt$/) {
	foreach(@targets) {
	    myexec("rm -f dpj/$1$_.java");
	}
    }
}
