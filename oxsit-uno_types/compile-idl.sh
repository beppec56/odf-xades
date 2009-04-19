#!/bin/bash
# for Linux only, to be used when generation of interface class is needed
# not normally needed when the Interface is defined
# for Windows some fixes may be necesary
# it needs OOO sdk in place and initialized
#

# parameters:
# $1 is the full path to the SDK initialization path
# $2 is the UNO type name
# $3 is the full path to source idl files
# $4 is the full path to the rdb library
# $5 is the full path to class destination
# $6 is the full module path of the class

echo $1 $2 $3 $4

# change working dir to the $3 directory
cd "$3"

#source the configuration file
. "$1" > /dev/null

#compile the file
idlc -I $OO_SDK_HOME/idl "$2".idl

if [ $? -ne 0 ] ;
	then
	echo "Error compiling $2"
	exit
fi

#add the class to the rdb registry
regmerge -v "$4"/oxsit-uno_types.uno.rdb /UCR "$2.urd"

if [ $? -ne 0 ] ;
	then
	echo "Error merging (regmerge) $2"
	exit
fi

echo "prepare the java classes"
javamaker -BUCR -O$5 -T"$6."$2 -nD $OO_SDK_URE_HOME/share/misc/types.rdb "$4"/oxsit-uno_types.uno.rdb

if [ $? -ne 0 ] ;
	then
	echo "Error building (javamaker) $2"
	exit
else
	echo "javamaker succeded"
fi
