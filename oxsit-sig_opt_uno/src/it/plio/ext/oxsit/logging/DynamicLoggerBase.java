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
import it.plio.ext.oxsit.ooo.ui.ControlDims;
import it.plio.ext.oxsit.ooo.ui.DialogDisplayLog;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * class to wrap the UNO logger. Characterizes the class owner
 * Can be used only if the UNO context is known
 * All the logging rigamarole is carried out in an UNO singleton object.
 * The drawback is that the
 * 
 * Main logging switch turned on/off on a owner basis (eg the parent class).
 * 
 * 
 * @author beppec56
 *
 */
abstract class DynamicLoggerBase implements IDynamicLogger {
	protected String m_sOwnerClass;
	protected String m_sOwnerClassHashHex;
	protected XOX_Logger m_xLogger;
	protected boolean	m_bLogEnabled = false;
	protected boolean 	m_bInfoEnabled = true;
	protected boolean 	m_bWarningEnabled = true;
	
	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_xMCF;

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
	public DynamicLoggerBase(Object _theOwner, XComponentContext _ctx) {
		//compute the parent class ID hex hash
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_xLogger = (XOX_Logger)UnoRuntime.queryInterface(XOX_Logger.class, 
				_ctx.getValueByName(GlobConstant.m_sSINGLETON_LOGGER_SERVICE_INSTANCE));
		if(m_xLogger == null)
			System.out.println("no main logger!");
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

	public abstract void warning(String _theMethod, String _message, java.lang.Exception ex);

	public abstract void severe(String _theMethod, String _message);

	public abstract void severe(java.lang.Exception ex);

	public abstract void severe(String _theMethod, String _message, java.lang.Exception ex);

	public abstract void severe(String _theMethod, java.lang.Exception ex);

	public static String getStackFromException(java.lang.Exception ex) {
		String term = System.getProperty("line.separator");
		String stack = term+ex.toString();

		StackTraceElement[] ste = ex.getStackTrace();
		if(ste != null)
			for(int i = 0; i < ste.length; i++)
				stack = stack+term+"\t"+ste[i].toString();
		return stack;
	}

	public void log_exception(int n_TheLevel, String _theMethod, String _message, java.lang.Exception ex, boolean usedialog) {
		m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_SEVERE, m_sOwnerClassHashHex+" "+m_sOwnerClass,
					_theMethod +" "+_message,
					DynamicLoggerBase.getStackFromException(ex));

		if(usedialog && n_TheLevel == GlobConstant.m_nLOG_LEVEL_SEVERE) {
			try {
			//Use the dialog
				String theMex = m_sOwnerClassHashHex+" "+m_sOwnerClass+" "+_theMethod +
									" "+_message+
									DynamicLoggerBase.getStackFromException(ex);			
				DialogDisplayLog dlg = new DialogDisplayLog(null,m_xCC,m_xMCF,theMex);
				dlg.initialize( 0, 0);
				dlg.executeDialog();
			} catch (java.lang.Exception e) {
				e.printStackTrace();
				warning("error showing dialog");
			}
		}
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

	/**
	 * this method is modal: it disables the logging
	 * for ALL the entire extension.
	 */
	public	void stopLogging() {
		m_xLogger.stopLogging();
	}
}
