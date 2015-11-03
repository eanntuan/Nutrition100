#!/bin/csh -f

if ($#argv != 1) then
  echo usage: "$0 <fst filename>"
  exit
endif

fst_to_dot $1 /tmp/test.dot
dotty /tmp/test.dot
