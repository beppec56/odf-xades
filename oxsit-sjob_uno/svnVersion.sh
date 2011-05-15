#!/bin/bash
svn info | grep "^Revision[^\b]*:" | awk '{print $2}'
