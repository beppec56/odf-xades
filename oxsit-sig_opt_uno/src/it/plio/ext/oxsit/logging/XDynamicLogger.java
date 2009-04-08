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

package it.plio.ext.oxsit.logging;

import it.plio.ext.oxsit.ooo.GlobConstant;

import com.sun.star.logging.XLogger;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * class to wrap the UNO logger. Carachterizes the class owner
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
public class XDynamicLogger {
	protected String m_sOwnerClass;
	protected String m_sOwnerClassHashHex;
	protected XLogger m_xLogger;
	protected boolean	m_bLogEnabled = false;
	protected boolean 	m_bInfoEnabled = true;
	protected boolean 	m_bWarningEnabled = true;

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
	public XDynamicLogger(Object _theOwner, XComponentContext _ctx) {
		//compute the parent class ID hex hash
		
		m_xLogger = (XLogger)UnoRuntime.queryInterface(XLogger.class, 
				_ctx.getValueByName(GlobConstant.m_sSINGLETON_SERVICE_INSTANCE));
		m_sOwnerClassHashHex = String.format( "%8H", _theOwner );
		m_sOwnerClass =  _theOwner.getClass().getName();		
	}

	/**
	 * outputs a log with the class hex hash+class full name + "<init>"
	 * @param _theMethod
	 */
public void ctor() {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, m_sOwnerClass, "<init>");
	}

	/**
	 * outputs a log with the class hex hash+class full name + "<init>" + a message
	 * @param _theMethod
	 */
	public void ctor(String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, m_sOwnerClass, "<init> "+_message);
	}

	/**
	 * outputs a log with the class hex hash + one message
	 * @param _theMethod
	 */
	public void log(String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, "", _message);
	}

	public void entering(String _theMethod) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, "entering "+_theMethod, "");
	}

	public void entering(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, "entering "+_theMethod, _message);
	}

	public void exiting(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, "exiting " + _theMethod, _message);
	}

	/**
	 * outputs a log with the class hex hash + two message
	 * @param _theMethod
	 */
	public void log(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, _theMethod, _message);
	}

	/**
	 * outputs a log with the class hex hash + one message
	 * @param _theMethod
	 */
	public void info(String _theMethod) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, _theMethod, "");
	}

	/**
	 * outputs a log with the class hex hash+class full name + two messages
	 * @param _theMethod
	 */
	public void info(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex+" "+ m_sOwnerClass, _theMethod, _message);
	}

	public void warning(String _theMethod) {
		if(m_bLogEnabled && m_bWarningEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_WARNING,  m_sOwnerClassHashHex, _theMethod, "");
	}

	public void warning(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bWarningEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_WARNING, m_sOwnerClassHashHex+" "+ m_sOwnerClass, _theMethod, _message);
	}

	public void warning(String _theMethod, String _message, Exception ex) {
		if(m_bLogEnabled && m_bWarningEnabled)
			log_exception(GlobConstant.m_nLOG_LEVEL_WARNING, _theMethod, _message, ex);
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
	public void severe(String _theMethod, String _message, Exception ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, _message, ex);
	}

	private void log_exception(int n_TheLevel, String _theMethod, String _message, Exception ex) {
		String stack = "\n"+ex.toString();

		StackTraceElement[] ste = ex.getStackTrace();
		if(ste != null)
			for(int i = 0; i < ste.length; i++)
				stack = stack+"\n\t"+ste[i].toString();

		m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_SEVERE, m_sOwnerClassHashHex+" "+m_sOwnerClass, _theMethod +" "+_message, ex.getLocalizedMessage()+stack);
	}

//enable/disable, set level
	/**
	 * disable logging completely, severe level as well
	 */
	public void disableLogging() {
		m_bLogEnabled = false;
	}
	
	/**
	 * enable all logging
	 */
	public void enableLogging() {
		m_bLogEnabled = true;
	}
	
	/**
	 * enable INFO level only
	 */
	public void enableInfo() {
		m_bInfoEnabled = true;
	}

	/**
	 * disable INFO level only
	 */
	public void disableInfo() {
		m_bInfoEnabled = false;		
	}

	/**
	 * enable WARNING level only
	 */
	public void enableWarning() {
		m_bWarningEnabled = true;
	}

	/**
	 * disable WARNING level only
	 */
	public void disableWarning() {
		m_bWarningEnabled = false;
		
	}
}
