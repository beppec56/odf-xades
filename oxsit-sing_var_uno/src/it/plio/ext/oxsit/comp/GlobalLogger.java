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

package it.plio.ext.oxsit.comp;

import it.plio.ext.oxsit.logging.LocalLogFormatter;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.singleton.LoggerParametersAccess;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.logging.XLogHandler;
import com.sun.star.logging.XLogger;
import com.sun.star.uno.XComponentContext;

/**
 * This class is a singleton UNO object.
 * It contains the global volatile variables of the applications
 * The permanent variables are stored in the registry.
 * 
 * This class implements the global logger for the extension, since it needs to
 * be a singleton object.
 * NOTE: it can't use the XDynamicLogger, but instead will use the 'real' Java logger.
 * 
 * This objects has properties, they are set by the callings UNO objects.
 * 
 * 
 * @author beppe
 *
 */
public class GlobalLogger extends ComponentBase 
			implements XServiceInfo,
			XLogger	{

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= GlobalLogger.class.getName();

	// the Object name, used to instantiate it inside the OOo APIs
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sSINGLETON_LOGGER_SERVICE };

/// This instead is the global logger, instantiated to have a Java singleton available	
	protected static ConsoleHandler		myHandl;
	protected static LocalLogFormatter 	myformatter;
	protected static FileHandler			myFileHandl;
	protected static LocalLogFormatter 	myFileformatter;

	//logger configuration
	protected static boolean	m_sEnableInfoLevel = true;
	protected static boolean	m_sEnableWarningLevel = true;
	protected static boolean	m_sEnableConsoleOutput = false;
	protected static boolean	m_sEnableFileOutput = true;
	protected static String		m_sLogFilePath = "";
	protected static int		m_sFileRotationCount = 1;
	protected static int		m_sMaxFileSize = 200000;
	protected	boolean			m_nCanLogMyself;

//only used as a synchronising object
	private static Boolean 				m_bLogConfigChanged = new Boolean(false);

// the 'real' global logger
	private static	Logger				m_log;
	private static	boolean				m_bEnableLogging = true;
	
    private LoggerParametersAccess m_LoggerConfigAccess;

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public GlobalLogger(XComponentContext _ctx) {
		
///rigamarole for logging....
//read the logger configuration locally
//contained in the <extension installation path>/logger subdirectory
		//get configuration access, using standard registry functions

		m_LoggerConfigAccess = new LoggerParametersAccess(_ctx);
		
		m_log = Logger.getLogger("it.plio.ext.oxsit");		
		m_log.setUseParentHandlers(false);//disables the console output of the root logger

		getLoggingConfiguration();
		configureLogger();

		if(m_nCanLogMyself)
			m_log.info("ctor");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	@Override
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	public String[] getSupportedServiceNames() {
		// TODO Auto-generated method stub
		if(m_nCanLogMyself)
			m_log.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

// protected logger functions
	
	/**
	 * read logging configuration from registry and set internal variables
	 */
	protected void getLoggingConfiguration() {
		m_sEnableInfoLevel = m_LoggerConfigAccess.getBoolean(GlobConstant.m_sENABLE_INFO_LEVEL);
		m_sEnableWarningLevel = m_LoggerConfigAccess.getBoolean(GlobConstant.m_sENABLE_WARNING_LEVEL);
		m_sEnableConsoleOutput = m_LoggerConfigAccess.getBoolean(GlobConstant.m_sENABLE_CONSOLE_OUTPUT);
		m_sEnableFileOutput = m_LoggerConfigAccess.getBoolean(GlobConstant.m_sENABLE_FILE_OUTPUT);
		m_sLogFilePath = m_LoggerConfigAccess.getText(GlobConstant.m_sLOG_FILE_PATH);
//FIXME: the file path need to be converted according to the platform
// e.g.: get the path separator, then scan the file path and change from whatever value to '/'
		m_sFileRotationCount = m_LoggerConfigAccess.getNumber(GlobConstant.m_sFILE_ROTATION_COUNT);
		m_sMaxFileSize = m_LoggerConfigAccess.getNumber(GlobConstant.m_sMAX_FILE_SIZE);
	}
	
	protected void configureLogger() {
		if(m_sEnableConsoleOutput) {
			myHandl = new ConsoleHandler();
			myformatter = new LocalLogFormatter();
			myHandl.setFormatter(myformatter);		
			m_log.addHandler(myHandl);
			System.out.println("console logging enabled");
		}
/*		else
			System.out.println("console logging NOT enabled");*/

		if(m_sEnableFileOutput) {
			String sFileName = GlobConstant.m_sEXTENSION_IDENTIFIER+".log";
			try {
// TODO document the default position of log file: $HOME/<extension id>.log, maximum
				if(m_sLogFilePath.length() > 0)
					sFileName = m_sLogFilePath+"/"+sFileName;
				else
					sFileName = "%h/"+sFileName;
				myFileHandl = new FileHandler( sFileName,m_sMaxFileSize,m_sFileRotationCount);
				myFileformatter = new LocalLogFormatter();
				myFileHandl.setFormatter(myFileformatter);
				m_log.addHandler(myFileHandl);
//FIXME DEBUG				System.out.println("files logging enabled, path "+" "+sFileName+" size: "+m_sMaxFileSize+" count: "+m_sFileRotationCount);
			} catch (SecurityException e) {
				//FIXME it seems the formatter does act accordingly
/*				if(m_sEnableConsoleOutput)
					m_log.log(Level.SEVERE, "exception: ", e);
				else*/
					e.printStackTrace();
				System.out.println("file logging NOT enabled ");
			} catch (IOException e) {
				//FIXME it seems the formatter does act accordingly
/*				if(m_sEnableConsoleOutput)
					m_log.log(Level.SEVERE, "exception: ", e);
				else*/
					e.printStackTrace();
				System.out.println("file logging NOT enabled ");
			}
		}
/*		else
			System.out.println("file logging NOT enabled ");*/
			
		if(!m_sEnableConsoleOutput && !m_sEnableFileOutput)
			m_bEnableLogging = false;

		m_nCanLogMyself =  m_bEnableLogging && m_sEnableInfoLevel;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#addLogHandler(com.sun.star.logging.XLogHandler)
	 */
	public void addLogHandler(XLogHandler arg0) {		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#getLevel()
	 */
	public int getLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#getName()
	 */
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#isLoggable(int)
	 */
	public boolean isLoggable(int arg0) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#log(int, java.lang.String)
	 */
	public void log(int arg0, String arg1) {
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#logp(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void logp(int _nLevel, String arg1, String arg2, String arg3) {
		if(m_bEnableLogging)		
			synchronized (m_bLogConfigChanged) {			
				switch (_nLevel) {
				default:
					m_log.logp(Level.FINE, arg1, arg2, arg3);
					break;
				case GlobConstant.m_nLOG_LEVEL_INFO:
					if(m_sEnableInfoLevel)
						m_log.logp(Level.INFO, arg1, arg2, arg3);						
					break;
				case GlobConstant.m_nLOG_LEVEL_SEVERE:
					m_log.logp(Level.SEVERE, arg1, arg2, arg3);						
					break;			
				case GlobConstant.m_nLOG_LEVEL_WARNING:
					if(m_sEnableWarningLevel)
						m_log.logp(Level.WARNING, arg1, arg2, arg3);						
					break;
				}
			}
	}

	/* (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#removeLogHandler(com.sun.star.logging.XLogHandler)
	 */
	public void removeLogHandler(XLogHandler arg0) {
	}

	/* 
	 * this method works a little differently then what intended
	 * on the original OOo API specification.
	 * It's called from configuration when the logging
	 * leveles in the configuration change: the new level
	 * will be taken into account immediately.
	 * as well as the file name:
	 * close the current handler,
	 * 
	 *  TODO
	 *  May be we can use a changelistener object on the
	 *  configuration parameters.
	 *  
	 *  The event is fired when the parameters change.
	 *  
	 * (non-Javadoc)
	 * @see com.sun.star.logging.XLogger#setLevel(int)
	 */
	public void setLevel(int arg0) {
		synchronized (m_bLogConfigChanged) {
			m_log.info("setLevel (change config) called");
			// protected area to change base elements of configuration			
			getLoggingConfiguration();
// restart logger, what is possible to restart, that is...		
//			configureLogger();
		}	
////// last of com.sun.star.logging.XLogger
	}
}
