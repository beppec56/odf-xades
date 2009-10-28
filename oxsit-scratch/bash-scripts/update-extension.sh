#!/bin/bash
# this file can be used in GNU/Linux to update the extension in OOo
#
# IMPORTANT: OOo should be closed before running the script
#
# cd /opt/openoffice.org3/program
./unopkg remove -v it.plio.ext.oxsit
sleep 2
./unopkg add -v $HOME/oxsit.oxt
#
