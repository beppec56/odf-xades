#!/bin/bash
#this grab the entire repository revision, for user display
svn info https://joinup.ec.europa.eu/svn/ooo-xadessig-it | grep "^Revision[^\b]*:" | awk '{print $2}'
