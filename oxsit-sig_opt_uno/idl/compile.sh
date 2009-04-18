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
SERVICE_FILE=DocumentSignatures

echo "source env for OOo SDK"
. $SOURCE_SHELL/setsdkenv_unix.sh


echo "compile idl file"
idlc -I $OO_SDK_HOME/idl $CLASS_FILE".idl"
#idlc -I $OO_SDK_HOME/idl $SERVICE_FILE".idl"

echo "remove old registry file"
rm ../rdb/oxsit_types.uno.rdb

echo "merge the registry file"
#regmerge -v ../rdb/oxsit_types.uno.rdb / $CLASS_FILE".urd"
regmerge -v ../rdb/oxsit_types.uno.rdb /UCR $CLASS_FILE".urd"
#regmerge -v ../rdb/oxsit_types.uno.rdb /UCR $SERVICE_FILE".urd"

echo "prepare the java classes"
javamaker -BUCR -O../class-lib -T"it.plio.ext.oxsit.ooo.cert."$CLASS_FILE -nD $OO_SDK_URE_HOME/share/misc/types.rdb ../rdb/oxsit_types.uno.rdb
#javamaker -BUCR -O../class-lib -T"it.plio.ext.oxsit.ooo.cert."$SERVICE_FILE -nD $OO_SDK_URE_HOME/share/misc/types.rdb ../rdb/oxsit_types.uno.rdb

#prepare the cpp classes
#cppumaker -BUCR -Tit.plio.ext.oxsit.ooo.cert.$CLASS_FILE $OO_SDK_URE_HOME/share/misc/types.rdb ../rdb/oxsit_types.uno.rdb
