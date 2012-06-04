#!/bin/bash
#this grab the entire repository revision, for user display
svn info https://joinup.ec.europa.eu/svn/ooo-xadessig-it/tags/1.0.0rc2 | grep "^Revision[^\b]*:" | awk '{print $2}'
