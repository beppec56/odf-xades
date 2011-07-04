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

package com.yacme.ext.oxsit.comp.security;

import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
import com.yacme.ext.oxsit.security.XOX_SignatureState;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.star.embed.XStorage;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 * This is a specification, it may change!
 * This service implements the X509Certificate service.
 * receives the doc information from the task  
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every signature
 * ca be retrieved through properties 
 * 
 * @author beppec56
 *
 */
public class DocumentSignatures extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XChangesNotifier,
			XComponent,
			XInitialization,
			XOX_DocumentSignaturesState
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= DocumentSignatures.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDOCUMENT_SIGNATURES_SERVICE };

	protected DynamicLogger m_logger;

	// these are the listeners on this document signatures changes
	public HashMap<XChangesListener,XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);

	protected XStorage		m_xDocumentStorage;
	// this document signature state
	protected int			m_nDocumentSignatureState;

	protected Boolean		m_aMtx_setDocumentSignatureState;
	protected boolean		m_bThreadNotifyChangesCanRun;
	
	protected String		m_sDocumentId;

	/**
	 * 
	 * 
	 * @param _ctx the UNO context
	 */
	public DocumentSignatures(XComponentContext _ctx) {
		m_logger = new DynamicLogger(this, _ctx);
    	m_logger.enableLogging();
    	m_logger.ctor();
    	m_aMtx_setDocumentSignatureState = new Boolean(false);
    	
    	//prepare and start the thread to notify changes
    	m_bThreadNotifyChangesCanRun = true;
		//call all the listeners, start a new thread for this
		(new Thread(new Runnable() {
			public void run() {
				m_logger.log("inter thread created");
				while(m_bThreadNotifyChangesCanRun) {
					synchronized (m_aMtx_setDocumentSignatureState) {
						try {
							m_aMtx_setDocumentSignatureState.wait();
						}
						catch (InterruptedException e) {
						}
						if(m_bThreadNotifyChangesCanRun) {
							m_logger.log("inter thread started");
							Collection<XChangesListener> aColl = m_aListeners.values();
							if(!aColl.isEmpty()) {
								Iterator<XChangesListener> aIter = aColl.iterator();
								// scan the array and for every one send the status
								while (aIter.hasNext()) {
									XChangesListener aThisOne =aIter.next();
									aThisOne.changesOccurred(null);
								}
							}
							m_logger.log("inter thread wraps, there were ", ((aColl.isEmpty()) ? "no" : aColl.size())+" listener");
						}
					}
				}
				m_logger.log("inter thread removed");
			}
		}
		)).start();
	}

	@Override
	public String getImplementationName() {
		// TODO Auto-generated method stub
		m_logger.entering("getImplementationName");
		return m_sImplementationName;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_logger.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_logger.info("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	// XChangesNotifier
	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#addChangesListener(com.sun.star.util.XChangesListener)
	 */
	@Override
	public void addChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
		
		if(!m_aListeners.containsKey(_ChangesListener) &&
				_ChangesListener != null ) {
			m_aListeners.put(_ChangesListener, _ChangesListener);
			m_logger.entering("addChangesListener "+Helpers.getHashHex(_ChangesListener));
		}		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star.util.XChangesListener)
	 */
	@Override
	public void removeChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
		if(m_aListeners.containsKey(_ChangesListener) ) {
			m_aListeners.remove(_ChangesListener);
			m_logger.entering("removeChangesListener "+Helpers.getHashHex(_ChangesListener));		
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * when instantiated, 
	 * 	_oObj[0] first argument document URL
	 *  _oObj[1] corresponding XStorage object
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		// TODO Auto-generated method stub
		m_logger.entering("initialize");		
	}	

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#getDocumentStorage()
	 * 
	 * IMPORTANT the manipulation of storage variable is Sync Job only responsability!
	 */
	@Override
	public XStorage getDocumentStorage() {
		// TODO Auto-generated method stub
		m_logger.info("getDocumentStorage");		
		return m_xDocumentStorage;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#setDocumentStorage(com.sun.star.embed.XStorage)
	 * IMPORTANT the manipulation of storage variable is Sync Job only responsability!
	 */
	@Override
	public void setDocumentStorage(XStorage _xStore) {
		// TODO Auto-generated method stub
		m_logger.info("setDocumentStorage");		
		m_xDocumentStorage = _xStore;		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#getDocumentId()
	 */
	@Override
	public String getDocumentId() {
		// TODO Auto-generated method stub
		m_logger.log("getDocumentId");
		return m_sDocumentId;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#setDocumentId(java.lang.String)
	 */
	@Override
	public void setDocumentId(String arg0) {
		// TODO Auto-generated method stub
		m_sDocumentId = arg0;
		m_logger.log("setDocumentId");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		m_logger.log("addEventListener");
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		m_logger.log("dispose");
		m_bThreadNotifyChangesCanRun = false;
		synchronized (m_aMtx_setDocumentSignatureState) {
			m_aMtx_setDocumentSignatureState.notify();
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		m_logger.log("removeEventListener");		
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#getAggregatedDocumentSignatureStates()
	 */
	@Override
	public int getAggregatedDocumentSignatureStates() {
		m_logger.debug("getAggregatedDocumentSignatureStates");
		synchronized (m_aMtx_setDocumentSignatureState) {
			return m_nDocumentSignatureState;			
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#getDocumentSignatureStates()
	 */
	@Override
	public XOX_SignatureState[] getDocumentSignatureStates() {
		// TODO Auto-generated method stub
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#setAggregatedDocumentSignatureStates(int)
	 * when this method is called, the signature state is notified to all the m_aListeners
	 */
	@Override
	public void setAggregatedDocumentSignatureStates(int _nState) {
		m_logger.entering("setDocumentSignatureState","_nState is: "+_nState);
		synchronized (m_aMtx_setDocumentSignatureState) {			
			m_nDocumentSignatureState = _nState;
			m_aMtx_setDocumentSignatureState.notify();
		}		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#addSignatureState(com.yacme.ext.oxsit.security.XOX_SignatureState)
	 */
	@Override
	public int addSignatureState(XOX_SignatureState _xSignatureState) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#getSignatureState(java.lang.String)
	 */
	@Override
	public XOX_SignatureState getSignatureState(String _sSignatureID) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#removeSignatureState(java.lang.String)
	 */
	@Override
	public int removeSignatureState(String _sSignatureID) {
		// TODO Auto-generated method stub
		return 0;
	}
}
