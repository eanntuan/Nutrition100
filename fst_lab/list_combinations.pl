#!/usr/bin/env perl

# parse arguments
if (@ARGV != 0) {
    die("Usage: list_combinations.pl < <STDIN> > <STDOUT>\n");
}

@lines = <STDIN>;

foreach $l (@lines) {
    chomp($l);
    push(@labels, $l);
}

foreach $l (@labels) {
    foreach $k (@labels) {
	if ($l ne $k) {
	    print "$l $k\n";
	}
    }
}
