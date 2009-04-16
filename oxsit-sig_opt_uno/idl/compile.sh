#!/bin/bash
# for Linux, to be used when generation of classes is needed
#needs OOO sdk in place and initialized
HOME_OO_SDK="/home/beppe/openoffice.org_sdk"
SOURCE_SHELL=$HOME_OO_SDK/dsklnx

. $SOURCE_SHELL/setsdkenv_unix.sh
# compile file
idlc -I $OO_SDK_HOME/idl OXDocumentSignatures.idl

#remove old file
rm ../rdb/oxsit_types.rdb

#merge the registry file
regmerge -v ../rdb/oxsit_types.rdb /UCR OXDocumentSignatures.urd

#prepare the java classes
javamaker -BUCR -O../class-lib -Tit.plio.ext.oxsit.ooo.cert.OXDocumentSignatures -nD $OO_SDK_URE_HOME/share/misc/types.rdb ../rdb/oxsit_types.rdb

#prepare the java classes
#cppumaker -BUCR -Tit.plio.ext.oxsit.ooo.cert.OXDocumentSignatures $OO_SDK_URE_HOME/share/misc/types.rdb ../rdb/oxsit_types.rdb
 