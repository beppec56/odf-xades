#!/bin/bash
# this file can be used in GNU/Linux to remove the extension in OOo
# to be used during testing
#
# IMPORTANT: OOo should be closed before running the script
#
# cd /opt/openoffice.org3/program
./unopkg remove --shared -v com.yacme.ext.oxsit
