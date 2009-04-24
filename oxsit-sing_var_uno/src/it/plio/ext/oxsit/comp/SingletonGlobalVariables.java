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

import it.plio.ext.oxsit.XOX_SingletonDataAccess;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures;

import java.util.HashMap;

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
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

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
	
	protected DynamicLogger			m_logger;
	
	protected	XComponentContext		m_xCtx;
	protected	XMultiComponentFactory	m_MFC;

	private class DocumentDescriptor {
		public	String	DocumentId;
		public	XOX_DocumentSignatures DocumentState;
	};

	private HashMap<String, DocumentDescriptor>	theDocumentList = new HashMap<String, DocumentDescriptor>(10);
	
/*	public String	m_sDocumentId;   //the Id of this document
	// these are the listener on changes of all the signatures related variables
	public HashMap<XChangesListener,XChangesListener> listeners = new HashMap<XChangesListener, XChangesListener>(10);*/

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public SingletonGlobalVariables(XComponentContext _ctx) {		
		m_logger = new DynamicLogger(this,_ctx);
		if(m_logger == null)
			System.out.println("no DynamicLogger !");

		m_logger.enableLogging();
		m_logger.ctor();
		
		m_xCtx = _ctx;
		m_MFC = m_xCtx.getServiceManager();
	}

	@Override
	public String getImplementationName() {
		// TODO Auto-generated method stub
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		// TODO Auto-generated method stub
		m_logger.info("getSupportedServiceNames");
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
	 * @see it.plio.ext.oxsit.XOX_SingletonDataAccess#getDocumentSignatures(java.lang.String)
	 */
	@Override
	public XOX_DocumentSignatures getDocumentSignatures(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.XOX_SingletonDataAccess#initDocumentAndListener(java.lang.String, com.sun.star.util.XChangesListener)
	 * 
	 * FIXME set exception
	 */
	@Override
	public XOX_DocumentSignatures initDocumentAndListener(String _aDocumentId, XChangesListener _aListener) {
		// TODO Auto-generated method stub
		synchronized (theDocumentList) {				
			//see if the document already exists
			if(!theDocumentList.containsKey(_aDocumentId)) {
				DocumentDescriptor docuDescrip = new DocumentDescriptor();
				docuDescrip.DocumentId = _aDocumentId;			
				Object aObj;
				try {
					//doesn't exists: instantiate a DocumentSignatures service and
					//add it the list of available documents.
					aObj = m_MFC.createInstanceWithContext("", m_xCtx);
					XOX_DocumentSignatures aDoc = (XOX_DocumentSignatures)UnoRuntime.queryInterface(XOX_DocumentSignatures.class, aObj);
					docuDescrip.DocumentState = aDoc;
					theDocumentList.put(docuDescrip.DocumentId, docuDescrip);
					m_logger.log("initDocumentAndListener","added doc id: "+_aDocumentId);
//need to add the listenener to the doc, if needed
					
					return aDoc;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					m_logger.severe("initDocumentAndListener", "error instantionting a new DocumentSignatures service", e);
					return null;
				}
			}
			else {
				//if exists, returns the document signatures element
				m_logger.log("initDocumentAndListener","RETURNING doc id: "+_aDocumentId);
				return theDocumentList.get(_aDocumentId).DocumentState;
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.XOX_SingletonDataAccess#removeDocumentSignatures(java.lang.String)
	 */
	@Override
	public void removeDocumentSignatures(String _aDocumentId) {
		// TODO Auto-generated method stub
		//remove the DocumentSignatures element whose id is the one on parameter
		synchronized (theDocumentList) {
			m_logger.log("removeDocumentSignatures","doc id: "+_aDocumentId);		
			if(theDocumentList.containsKey(_aDocumentId)) {
				XOX_DocumentSignatures aDocu = theDocumentList.get(_aDocumentId).DocumentState;
				DocumentDescriptor docuDescrip = theDocumentList.remove(_aDocumentId);
			}		
		}
	}
}
