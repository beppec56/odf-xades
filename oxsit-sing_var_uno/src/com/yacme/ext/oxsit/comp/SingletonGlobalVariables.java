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

package com.yacme.ext.oxsit.comp;

import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;

import java.util.HashMap;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 * This class is a singleton UNO object.
 * It contains the global volatile variables of the applications
 * The permanent variables are stored in the registry.
 * 
 * This objects has properties, they are set by the callings UNO objects.
 * 
 * 
 * @author beppe
 *
 */
public class SingletonGlobalVariables extends ComponentBase 
			implements XServiceInfo, 
			XOX_SingletonDataAccess {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= SingletonGlobalVariables.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sSINGLETON_SERVICE };
	
	protected DynamicLogger			m_aLogger;
	
	protected	XComponentContext		m_xCtx;
	protected	XMultiComponentFactory	m_MFC;

	private class DocumentDescriptor {
		public	String	DocumentId;
		public	Object m_aDocumentSignaturesService;
	};

	private HashMap<String, DocumentDescriptor>	m_aDocumentList = new HashMap<String, DocumentDescriptor>(10);
	
	private HashMap<String,XComponent>		m_aComponentList	= new HashMap<String,XComponent>(10);
	
/*	public String	m_sDocumentId;   //the Id of this document
	// these are the listener on changes of all the signatures related variables
	public HashMap<XChangesListener,XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);*/

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public SingletonGlobalVariables(XComponentContext _ctx) {		
		m_aLogger = new DynamicLogger(this,_ctx);
		if(m_aLogger == null)
			System.out.println("no DynamicLogger !");

		m_aLogger.enableLogging();
		m_aLogger.ctor();
		
		m_xCtx = _ctx;
		m_MFC = m_xCtx.getServiceManager();
	}

	@Override
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
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
	@Override
	public void addEventListener(XEventListener arg0) {
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
//cleanup all the things we have		
		m_aComponentList.clear();
		m_aDocumentList.clear();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_SingletonDataAccess#getDocumentSignatures(java.lang.String)
	 */
	@Override
	public XOX_DocumentSignaturesState getDocumentSignatures(String _aDocumentId) {
		// TODO Auto-generated method stub
		synchronized (m_aDocumentList) {				
			//see if the document already exists
			if(m_aDocumentList.containsKey(_aDocumentId)) {
				//if exists, returns the document signatures element
				m_aLogger.log("initDocumentAndListener","RETURNING doc id: "+_aDocumentId);
				XOX_DocumentSignaturesState aDoc = null;
				Object aObj = m_aDocumentList.get(_aDocumentId).m_aDocumentSignaturesService;
				if(aObj != null) {
					aDoc = (XOX_DocumentSignaturesState)UnoRuntime.queryInterface(XOX_DocumentSignaturesState.class, aObj);
					if(aDoc == null) 
						m_aLogger.severe("initDocumentAndListener", "XOX_DocumentSignaturesState is null");					
				}
				else
					m_aLogger.severe("initDocumentAndListener", "aObj is null");					
				return aDoc;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_SingletonDataAccess#initDocumentAndListener(java.lang.String, com.sun.star.util.XChangesListener)
	 * 
	 * FIXME set exception
	 */
	@Override
	public XOX_DocumentSignaturesState initDocumentAndListener(String _aDocumentId, XChangesListener _aListener) {
		synchronized (m_aDocumentList) {				
			//see if the document already exists
			if(!m_aDocumentList.containsKey(_aDocumentId)) {
				DocumentDescriptor docuDescrip = new DocumentDescriptor();
				docuDescrip.DocumentId = _aDocumentId;			
				Object aObj;
				try {
					//doesn't exists: instantiate a DocumentSignatures service and
					//add it the list of available documents.
					aObj = m_MFC.createInstanceWithContext(GlobConstant.m_sDOCUMENT_SIGNATURES_SERVICE, m_xCtx);
					docuDescrip.m_aDocumentSignaturesService = aObj;
					m_aDocumentList.put(docuDescrip.DocumentId, docuDescrip);
//need to add the listener to the doc, if needed
					XChangesNotifier aNotif = (XChangesNotifier)UnoRuntime.queryInterface(XChangesNotifier.class, aObj);
					if(aNotif != null)
						aNotif.addChangesListener(_aListener);
					else
						m_aLogger.severe("initDocumentAndListener", "XChangesNotifier missing.");
						
					XOX_DocumentSignaturesState aDoc = (XOX_DocumentSignaturesState)UnoRuntime.queryInterface(XOX_DocumentSignaturesState.class, aObj);
					if(aDoc == null) 
						m_aLogger.severe("initDocumentAndListener", "XOX_DocumentSignaturesState missing.");
					else
						aDoc.setDocumentId(_aDocumentId);
					m_aLogger.exiting("initDocumentAndListener", _aDocumentId);
					return aDoc;
				}
				catch (Exception e) {
					m_aLogger.severe("initDocumentAndListener", "error instantionting a new DocumentSignatures service", e);
					return null;
				}
			}
			else {
				//if exists, returns the document signatures element
				m_aLogger.log("initDocumentAndListener","RETURNING doc id: "+_aDocumentId);
				XOX_DocumentSignaturesState aDoc = null;
				Object aObj = m_aDocumentList.get(_aDocumentId).m_aDocumentSignaturesService;
				if(aObj != null) {
					//need to add the listener to the doc, if needed
					XChangesNotifier aNotif = (XChangesNotifier)UnoRuntime.queryInterface(XChangesNotifier.class, aObj);
					if(aNotif != null)
						aNotif.addChangesListener(_aListener);
					else
						m_aLogger.severe("initDocumentAndListener", "XChangesNotifier missing.");
					aDoc = (XOX_DocumentSignaturesState)UnoRuntime.queryInterface(XOX_DocumentSignaturesState.class, aObj);
					if(aDoc == null) 
						m_aLogger.severe("initDocumentAndListener", "XOX_DocumentSignaturesState is null");
				}
				else
					m_aLogger.severe("initDocumentAndListener", "aObj is null");					
				return aDoc;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_SingletonDataAccess#removeDocumentSignatures(java.lang.String)
	 */
	@Override
	public void removeDocumentSignatures(String _aDocumentId) {
		// TODO Auto-generated method stub
		//remove the DocumentSignatures element whose id is the one on parameter
		synchronized (m_aDocumentList) {
			m_aLogger.log("removeDocumentSignatures","doc id: "+_aDocumentId);		
			if(m_aDocumentList.containsKey(_aDocumentId)) {
				Object aObj = m_aDocumentList.get(_aDocumentId).m_aDocumentSignaturesService;
				m_aDocumentList.remove(_aDocumentId);
				XComponent aComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, aObj);
				if(aComp != null)
					aComp.dispose(); //clean up the UNO object
				else
					m_aLogger.severe("removeDocumentSignatures", "XComponent missing.");
			}		
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_SingletonDataAccess#addUNOComponent(java.lang.String, com.sun.star.lang.XComponent)
	 */
	@Override
	public void addUNOComponent(String _UNOComponentName, XComponent _UNOComponent)
			throws ElementExistException, IllegalArgumentException {
		// TODO Auto-generated method stub
		if(!m_aComponentList.containsKey(_UNOComponentName))
			m_aComponentList.put(_UNOComponentName,_UNOComponent);
		else
			throw (new ElementExistException("\""+_UNOComponentName+"\" already exists in "+this.getClass().getName()));		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_SingletonDataAccess#getUNOComponent(java.lang.String)
	 */
	@Override
	public XComponent getUNOComponent(String _UNOComponentName)
			throws NoSuchElementException, IllegalArgumentException {
		if(m_aComponentList.containsKey(_UNOComponentName))
			return m_aComponentList.get(_UNOComponentName);
		else
			throw (new NoSuchElementException("\""+_UNOComponentName+"\" doesn't exist in "+this.getClass().getName()));
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_SingletonDataAccess#removeUNOComponent(java.lang.String)
	 * 
	 * rerome a UNO component from the list
	 */
	@Override
	public void removeUNOComponent(String _UNOComponentName) throws NoSuchElementException,
			IllegalArgumentException {
		if(m_aComponentList.containsKey(_UNOComponentName)) {
			m_aComponentList.remove(_UNOComponentName);
		}
		else
			throw (new NoSuchElementException("\""+_UNOComponentName+"\" doesn't exist in "+this.getClass().getName()));
	}
}
