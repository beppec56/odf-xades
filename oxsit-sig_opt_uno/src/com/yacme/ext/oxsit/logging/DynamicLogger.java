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


import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 * class to wrap the UNO logger. Characterizes the class owner
 * Can be used only if the UNO context is known
 * All the logging rigamarole is carried out in an UNO singleton object.
 * The drawback is that the
 * 
 * Main logging switch turned on/off on a owner basis (eg the parent class).
 * 
 * 
 * @author beppe
 *
 */
public class DynamicLogger extends DynamicLoggerBase {
	/**
	 * Class for logger.
	 * The link point between the user class (e.g. the one that uses a logger) and
	 * the UNO singleton which exposes it.
	 * 
	 * Default starts disabled.
	 * Must be enabled when needed!
	 * @param _theOwner parent object
	 * @param _ctx the UNO context
	 */
	public DynamicLogger(Object _theOwner, XComponentContext _ctx) {
		super(_theOwner,_ctx);
	}

	public void warning(String _theMethod, String _message, Throwable ex) {
		if(m_bLogEnabled && m_bWarningEnabled)
			log_exception(GlobConstant.m_nLOG_LEVEL_WARNING, _theMethod, _message, ex,false);
	}

	public void severe(String _theMethod, String _message) {
		m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_SEVERE,  m_sOwnerClassHashHex+" "+m_sOwnerClass, _theMethod, _message);
	}

	/**
	 * this method cannot be disabled.
	 * Severe log messages are always sent to UNO logger.
	 * 
	 * 
	 * @param _theMethod
	 * @param _message
	 * @param ex
	 */
	public void severe(Throwable ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, "", "", ex,false);
	}

	public void severe(String _theMethod, String _message, java.lang.Throwable ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, _message, ex,false);
	}

	public void severe(String _theMethod, java.lang.Throwable ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, "", ex,false);
	}
}
