#!/bin/bash
# this file can be used in GNU/Linux to update the extension in OOo
#
# IMPORTANT: OOo should be closed before running the script
#
# cd /opt/openoffice.org3/program
./unopkg remove --shared -v it.plio.ext.oxsit
sleep 2
./unopkg add --shared -v $HOME/oxsit.oxt
#
