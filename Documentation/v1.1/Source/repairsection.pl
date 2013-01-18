#!/usr/bin/perl

# Script to work around an apparent bug in Hevea: The Latex section
# symbol gets translated to an ASCII code that is unprintable in HTML;
# replace it with '&sect;', which is the HTML code for the section
# symbol.
#
# Rob Bocchino
# June 2010

while (<>) {
    my $line = $_;
    $line =~ s/\xa7/&sect;/g;
    print $line;
}
