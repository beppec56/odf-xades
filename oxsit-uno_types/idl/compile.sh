#!/bin/bash
# for Linux only, to be used when generation of interface class is needed
# not normally needed when the Interface is defined
# for Windows some fixes may be necesary
# it needs OOO sdk in place and initialized
#
HOME_OO_SDK="/home/beppe/openoffice.org_sdk"
SOURCE_SHELL=$HOME_OO_SDK/dsklnx

#HOME_OO_SDK="/home/beppe/openoffice.org2.2_sdk"
#SOURCE_SHELL=$HOME_OO_SDK/

CLASS_FILE=XOXDocumentSignatures

echo "source env for OOo SDK"
. $SOURCE_SHELL/setsdkenv_unix.sh

echo "compile idl file"
idlc -I $OO_SDK_HOME/idl $CLASS_FILE".idl"

echo "remove old registry file"
rm ../pre-built-type-classes/rdb/oxsit-uno_types.uno.rdb

echo "merge the registry file"
regmerge -v ../pre-built-type-classes/rdb/oxsit-uno_types.uno.rdb /UCR $CLASS_FILE".urd"

echo "prepare the java classes"
javamaker -BUCR -O../pre-built-type-classes -T"it.plio.ext.oxsit.ooo.cert."$CLASS_FILE -nD $OO_SDK_URE_HOME/share/misc/types.rdb ../pre-built-type-classes/rdb/oxsit-uno_types.uno.rdb
