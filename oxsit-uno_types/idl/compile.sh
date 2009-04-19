#!/bin/bash
# for Linux only, to be used when generation of interface class is needed
# not normally needed when the Interface is defined
# for Windows some fixes may be necesary
# it needs OOO sdk in place and initialized
#
HOME_OO_SDK="/home/beppe/openoffice.org_sdk"
SOURCE_SHELL=$HOME_OO_SDK/dsklnx
RDB_DEST=../pre-built-type-rdb
CLASS_DEST=../pre-built-type-classes

#CLASS_FILE=XOXDocumentSignatures

echo "source env for OOo SDK"
. $SOURCE_SHELL/setsdkenv_unix.sh

echo "compile idl file"
#idlc -I $OO_SDK_HOME/idl $CLASS_FILE".idl"
idlc -I $OO_SDK_HOME/idl "*.idl"

echo "remove old registry file"
rm $RDB_DEST/oxsit-uno_types.uno.rdb

echo "merge the registry file"
regmerge -v $RDB_DEST/oxsit-uno_types.uno.rdb /UCR "*.urd"

echo "prepare the java classes"
javamaker -BUCR -O$CLASS_DEST -T"it.plio.ext.oxsit.ooo.cert."$CLASS_FILE -nD $OO_SDK_URE_HOME/share/misc/types.rdb ../pre-built-type-classes/rdb/oxsit-uno_types.uno.rdb
