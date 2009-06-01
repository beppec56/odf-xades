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

package it.plio.ext.oxsit.comp.security;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.pack.DigitalSignatureHelper;
import it.plio.ext.oxsit.security.XOX_DocumentSignaturesState;
import it.plio.ext.oxsit.security.XOX_DocumentSigner;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.embed.XStorage;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

/**
 * This service implements the real document signer.
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
public class DocumentSigner extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XComponent,
			XInitialization,
			XOX_DocumentSigner
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= DocumentSigner.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDOCUMENT_SIGNER_SERVICE };

	protected DynamicLogger m_aLogger;

	// these are the listeners on this document signatures changes
	public HashMap<XChangesListener,XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);

	protected XStorage		m_xDocumentStorage;
	protected XComponentContext	m_xCC;
	private 	XMultiComponentFactory m_xMCF;
	// this document signature state

	/**
	 * 
	 * 
	 * @param _ctx the UNO context
	 */
	public DocumentSigner(XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
    	m_aLogger.enableLogging();
    	m_aLogger.ctor();
	}

	@Override
	public String getImplementationName() {
		// TODO Auto-generated method stub
		m_aLogger.entering("getImplementationName");
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

		m_aLogger.info("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
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
		m_aLogger.entering("initialize");		
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
	 * @see it.plio.ext.oxsit.security.XOX_DocumentSigner#signDocument(com.sun.star.document.XStorageBasedDocument, it.plio.ext.oxsit.security.cert.XOX_X509Certificate[])
	 * 
	 * gets called from dialog when a document should be signed with independednt certificate signature
	 */
	@Override
	public void signDocumentStandard(XFrame xFrame, XStorage xStorage,
			XOX_X509Certificate[] arg1) throws IllegalArgumentException,
			Exception {
		// TODO Auto-generated method stub
		
		//to see if all is all rught, examine the document structure
		
		
		m_aLogger.log("signDocument");
		//just for test, analyze the document package structure
		DigitalSignatureHelper dg = new DigitalSignatureHelper(m_xMCF,m_xCC);
		
		dg.verifyDocumentSignature(xStorage,null);
		
		//try to get a pin from the user
		
		
	}	
}
