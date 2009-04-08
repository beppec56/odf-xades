#!/bin/bash
# simple script to copy the web structure to a local apache2 web server, for testing purposes
# the path are relative with respect to the project, absolute wrt the
# web server (Used Debian lenny web)
# the command does some clean up so the directory
# from the local web server can be used as a src to copy to publishing web
# 
# usage: from inside the root directory in web subproject, run:
# ../scratch/bash-scripts/copy-web-locally.sh
WEBLOCAL="/var/www/test-osor"
cp -apuvf . $WEBLOCAL
#cd /var/www/test-osor
#remove the un-needed .svn files...
find $WEBLOCAL -name ".svn" -type d -exec rm -rf '{}' \;
rm $WEBLOCAL"/README"
