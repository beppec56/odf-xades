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

package it.plio.ext.oxsit.singleton;

import it.plio.ext.oxsit.logging.LocalLogFormatter;
import it.plio.ext.oxsit.ooo.GlobConstant;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XProperty;
import com.sun.star.beans.XPropertyAccess;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.logging.XLogHandler;
import com.sun.star.logging.XLogger;
import com.sun.star.uno.Type;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

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
public class SingletonGlobalVariables extends WeakBase 
			implements XServiceInfo, 
			XComponent,
			XInterface,
			XProperty,
			XPropertyAccess,
			XPropertySetInfo,
			XChangesNotifier,
			XNameContainer,
			XLogger {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= SingletonGlobalVariables.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sSINGLETON_SERVICE };

/// This instead is the global logger, instantiated to have a Java singleton available	
	private static ConsoleHandler		myHandl;
	private static LocalLogFormatter 	myformatter;
	private static FileHandler			myFileHandl;
	private static LocalLogFormatter 	myFileformatter;
	
	//logger configuration
	public static boolean	m_sEnableInfoLevel = true;
	public static boolean	m_sEnableWarningLevel = true;
	public static boolean	m_sEnableConsoleOutput = false;
	public static boolean	m_sEnableFileOutput = true;
	public static String	m_sLogFilePath = "";
	public static int		m_sFileRotationCount = 1;
	public static int		m_sMaxFileSize = 50000;
	public	boolean			m_nCanLogMyself;
	
//only used as a synchronising object
	private static Boolean 				m_bLogConfigChanged = new Boolean(false);

// the 'real' global logger
	private static	Logger				m_log;
	private static	boolean				m_bEnableLogging = true;
	
	public static	String m_sProperties[] = {"SelfObject","DataInstance"}; 
	private class DocumentDescriptor {
		public String	sURLHash; // the hash computed from the URL name class
		public String	sURL;   //the corresponding URL
		public int		nXAdESSignatureState; // state of the signature(s) in this frame
		// these are the lister on changes on this element only
		// called only if the signature changes state.
		public HashMap<XChangesListener,XChangesListener> listeners;
	};

	private HashMap<String, DocumentDescriptor>	theDocumentList = new HashMap<String, DocumentDescriptor>(10);
	// these are the lister on changes of all the variables
	public HashMap<XChangesListener,XChangesListener> listeners;

    private LoggerParametersAccess m_LoggerConfigAccess;

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public SingletonGlobalVariables(XComponentContext _ctx) {
		
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

	public String getImplementationName() {
		// TODO Auto-generated method stub
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
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		if(m_nCanLogMyself)
			m_log.info("addEventListener");
	}
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		//clean all the element created and exit
		Collection<DocumentDescriptor> cDocuDescriptors = theDocumentList.values();
		
		if(!cDocuDescriptors.isEmpty()) {
			Iterator<DocumentDescriptor> aIter = cDocuDescriptors.iterator();
			while (aIter.hasNext()) {
				DocumentDescriptor aDocDesc = aIter.next();
				aDocDesc.listeners.clear();
			}			
		}
		theDocumentList.clear();	

		if(m_nCanLogMyself)
			m_log.info("");
		myFileHandl.close();
	}

	
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		if(m_nCanLogMyself)
			m_log.info("removeEventListener");				
	}

	public void indentify() {
//		logger.info(getHashHex());
		if(m_nCanLogMyself)
			m_log.info("indentify");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.beans.XProperty#getAsProperty()
	 */
	public Property getAsProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.beans.XPropertyAccess#getPropertyValues()
	 */
	public PropertyValue[] getPropertyValues() {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * every time a property is set,  all the registered listeners are notified.
	 * Note that if a pproperty does not exists, it is added 
	 * Example:
	 * 
	 *  property.Name = String("Fuhc8222a626")
	 *  property.Value = array[] of properties [].Name "Operation"       the operation to carry out
	 *  										.Value int = 1           remove it from list
	 *                                                       2           add if not present
	 *                                                       3			 set value, if not present throws
	 *                                                       4			 set a ChangeListener
	 *                                                       5			 remove a change listener
	 *  										.Name "URL"
	 *  										.Value 'the full URL'
	 *  										.Name "XAdESSignatureState"
	 *  										.Value int, the signature status
	 *  										.Name "ChangesListener"
	 *  										.Value Obj should ne an object of the kind change listener, e.g.
	 *  													should have a XChangesLister interface implemented
	 *  													
	 *  
	 * 
	 * (non-Javadoc)
	 *  @see com.sun.star.beans.XPropertyAccess#setPropertyValues(com.sun.star.beans.PropertyValue[])
	 */
	public void setPropertyValues(PropertyValue[] aPropertyValue)
			throws UnknownPropertyException, PropertyVetoException,
			IllegalArgumentException, WrappedTargetException {
		// TODO Auto-generated method stub
		
//		if(m_bEnableLogging)
//			m_log.info("setPropertyValues: "+aPropertyValue[0].Name);
	}

	/* returns all the properties, in this case all the registered
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.beans.XPropertySetInfo#getProperties()
	 */
	public Property[] getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * return a single property or throws
	 * 
	 * in case of a Document add-on data (e.g. the signature status)
	 * the property is the document URL hash computed as a key. E.g. the URL
	 * becomes someting as: Fuhc8222a626
	 * This is the name name of the frame
	 * the value is a sequence of property containing the corresponding data to be
	 * set.
	 * Example:
	 * 
	 *  property.Name = String("Fuhc8222a626")
	 *  property.Value = array[] of properties [].Name "Operation"            the operation to carry out
	 *  										.Value int = 1           retrieve the value
	 *                                                       2
	 *                                                       3
	 *                                                       4
	 *  										.Name "URL"
	 *  										.Value 'the full URL'
	 *  										.Name "XAdESSignatureState"
	 *  										.Value int, the signature status
	 *  
	 *  
	 *  The properties in the sequence can be in any order.
	 * 
	 * 
	 * @see com.sun.star.beans.XPropertySetInfo#getPropertyByName(java.lang.String)
	 */
	public Property getPropertyByName(String arg0)
			throws UnknownPropertyException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.beans.XPropertySetInfo#hasPropertyByName(java.lang.String)
	 */
	public boolean hasPropertyByName(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	// XChangesNotifier
	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#addChangesListener(com.sun.star.util.XChangesListener)
	 */
	public void addChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star.util.XChangesListener)
	 */
	public void removeChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub

		// last of XChangesNotifier
	}

	/////////////////// XNameContainer

	/* (non-Javadoc)
	 * @see com.sun.star.container.XNameContainer#insertByName(java.lang.String, java.lang.Object)
	 */
	public void insertByName(String _aURLHash, Object _oObj)
			throws IllegalArgumentException, ElementExistException,
			WrappedTargetException {
		// TODO Auto-generated method stub
		if(!theDocumentList.containsKey(_aURLHash)) {
			DocumentDescriptor docuDescrip = new DocumentDescriptor();
			docuDescrip.sURLHash = _aURLHash;

			PropertyValue aVal = (PropertyValue)_oObj;
			//we recevive a series of propertyvalues
			PropertyValue[] aValues = (PropertyValue[])aVal.Value;
			for(int i= 0; i< aValues.length; i++)
				if(aValues[i].Name.compareTo(SigletonGlobalVarConstants.m_sURL_VALUE) == 0) {
					docuDescrip.sURL = (String)aValues[i].Value;
					break;
				}
		
			for(int i= 0; i< aValues.length; i++) {
				if(aValues[i].Name.compareTo(SigletonGlobalVarConstants.m_sXADES_SIGNATURE_STATE) == 0) {
					docuDescrip.nXAdESSignatureState = ((Integer)aValues[i].Value).intValue();
					break;
				}
			}
			docuDescrip.listeners = new HashMap<XChangesListener, XChangesListener>(10);
			theDocumentList.put(docuDescrip.sURLHash, docuDescrip);
		}
		else
			throw new ElementExistException();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XNameContainer#removeByName(java.lang.String)
	 */
	public void removeByName(String _aURLHash) throws NoSuchElementException,
			WrappedTargetException {
		// TODO Auto-generated method stub
		if(m_nCanLogMyself)
			m_log.info("removeByName");
		if(theDocumentList.containsKey(_aURLHash)) {
			DocumentDescriptor docuDescrip = theDocumentList.remove(_aURLHash);
			docuDescrip.listeners.clear();
		}
		else
			throw new NoSuchElementException();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XNameReplace#replaceByName(java.lang.String, java.lang.Object)
	 */
	public void replaceByName(String arg0, Object arg1)
			throws IllegalArgumentException, NoSuchElementException,
			WrappedTargetException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XNameAccess#getByName(java.lang.String)
	 */
	public Object getByName(String arg0) throws NoSuchElementException,
			WrappedTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XNameAccess#getElementNames()
	 */
	public String[] getElementNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XNameAccess#hasByName(java.lang.String)
	 */
	public boolean hasByName(String _sElementName) { // this is the name of the frame
												// is the key inside the full hash list
		return theDocumentList.containsKey(_sElementName);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XElementAccess#getElementType()
	 */
	public Type getElementType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.container.XElementAccess#hasElements()
	 */
	public boolean hasElements() {
		// TODO Auto-generated method stub
// check if we have some elements onlist, returns		
		return false;
		
		//////////////// last of XNameContainer
	}

///logger functions
	
	/**
	 * read logging configuration from registry and set internal variables
	 */
	public void getLoggingConfiguration() {
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
	
	public void configureLogger() {
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
