#!/bin/bash
# simple script to clean-up the license
# statement
# usage: strip-license.sh
#
#ls -f build.* > $HOME/tmp.lst
#ls -f *.java > $HOME/tmp.lst
ls -f *.xml > $HOME/tmp.lst
{
	while read afile
	do
		echo $afile
#		      sed '1,23 s/the terms of European Union Public License (EUPL) as published/the terms of European Union Public License (EUPL) version 1.1/' < "$afile"  | sed '1,23 s/by the European Community, either version 1.1 of the License,/as published by the European Community./' |sed '/or any later version.$/d' > tmp.tmp
		      sed '1,23 s/the terms of European Union Public License (EUPL) as published/the terms of European Union Public License (EUPL) version 1.1/' < "$afile"  | sed '1,23 s/by the European Community version 1.1./as published by the European Community./' > tmp.tmp
		mv tmp.tmp "$afile"
	done 
} < $HOME/tmp.lst
