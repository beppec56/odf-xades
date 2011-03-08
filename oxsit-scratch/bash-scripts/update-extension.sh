#!/bin/bash
# this file can be used in GNU/Linux to update the extension in OOo
#
# IMPORTANT: OOo should be closed before running the script
#
# cd /opt/openoffice.org3/program
./unopkg remove --shared -v com.yacme.ext.oxsit
echo "Waiting for unopkg to sync..."
sleep 2
echo "installing new extension..."
./unopkg add --shared -v $HOME/oxsit.oxt
#
