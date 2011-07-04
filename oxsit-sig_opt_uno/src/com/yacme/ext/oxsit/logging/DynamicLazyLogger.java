/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) version 1.1
 *  as published by the European Community.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the EUPL.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  EUPL for more details.
 *
 *  You should have received a copy of the EUPL along with this
 *  program.  If not, see:
 *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
 *
 ************************************************************************/

package com.yacme.ext.oxsit.logging;

/**
 * A logger helper class to be used when you want to disable the logging in a class
 * that receive the object from the parent.
 * 
 * @author beppec56
 *
 */
public class DynamicLazyLogger implements IDynamicLogger {

	/**
	 * 
	 */
	public DynamicLazyLogger() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#ctor()
	 */
	@Override
	public void ctor() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#ctor(java.lang.String)
	 */
	@Override
	public void ctor(String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#disableInfo()
	 */
	@Override
	public void disableInfo() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#disableLogging()
	 */
	@Override
	public void disableLogging() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#disableWarning()
	 */
	@Override
	public void disableWarning() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#enableInfo()
	 */
	@Override
	public void enableInfo() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#enableLogging()
	 */
	@Override
	public void enableLogging() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#enableWarning()
	 */
	@Override
	public void enableWarning() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#entering(java.lang.String)
	 */
	@Override
	public void entering(String method) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#entering(java.lang.String, java.lang.String)
	 */
	@Override
	public void entering(String method, String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#exiting(java.lang.String, java.lang.String)
	 */
	@Override
	public void exiting(String method, String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#info(java.lang.String)
	 */
	@Override
	public void info(String method) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#info(java.lang.String, java.lang.String)
	 */
	@Override
	public void info(String method, String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#log(java.lang.String)
	 */
	@Override
	public void log(String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#log(java.lang.String, java.lang.String)
	 */
	@Override
	public void log(String method, String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String)
	 */
	@Override
	public void severe(String method, String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#severe(java.lang.Exception)
	 */
	@Override
	public void severe(Throwable ex) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String method, String _message, Throwable ex) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String method, Throwable ex) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#stopLogging()
	 */
	@Override
	public void stopLogging() {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String)
	 */
	@Override
	public void warning(String method) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String)
	 */
	@Override
	public void warning(String method, String _message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void warning(String method, String _message, Throwable ex) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#log_exception(int, java.lang.String, java.lang.String, java.lang.Throwable, boolean)
	 */
	@Override
	public void log_exception(int theLevel, String method, String _message,
			Throwable ex, boolean useDialog) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#log(java.lang.Throwable, boolean)
	 */
	@Override
	public void log(Throwable e, boolean dialog) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#debug(java.lang.String)
	 */
	@Override
	public void debug(String message) {
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.logging.IDynamicLogger#debug(java.lang.String, java.lang.String)
	 */
	@Override
	public void debug(String theMethod, String message) {
		// TODO Auto-generated method stub
		
	}
}
