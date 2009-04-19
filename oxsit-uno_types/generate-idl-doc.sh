#!/bin/bash
# for Linux only, to be used to generate documentation for new
# interfaces
# for Windows some fixes may be necesary
# it needs OOO sdk in place and initialized
#

# parameters:
# $1 is the full path to the SDK initialization path
# $2 is the full path to source idl files
# $3 is the full path to the target html directory

#source the configuration file
. "$1" > /dev/null

#compile the file
autodoc -html $3 -name "OXSIT" -lg idl -t $2 
