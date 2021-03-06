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

package com.yacme.ext.oxsit.comp.security.cert;

import com.yacme.ext.oxsit.security.cert.XOX_CertificateExtension;

import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 *  This service implements the CertificateExtension service.
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * This service represents a single certificate extension. 
 * 
 * @author beppec56
 *
 */
public class CertificateExtension extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_CertificateExtension
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= CertificateExtension.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sCERTIFICATE_EXTENSION_SERVICE };

	protected DynamicLogger m_logger;

	private boolean m_bIsCritical;
	
	private String m_sExtensionStringValue;

	private String m_sExtensionStringName;

	private String m_sExtensionId;
	
	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public CertificateExtension(XComponentContext _ctx) {
		m_logger = new DynamicLogger(this, _ctx);
 //   	m_aLogger.enableLogging();
    	m_logger.ctor();    	
	}

	@Override
	public String getImplementationName() {
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

	/** Function used internally when instantiating the object
	 * 
	 * Called using:
	 *
	 *		Object[] aArguments = new Object[4];
	 *
	 *		aArguments[0] = new String(OID);
	 *
	 *		aArguments[1] = new String("TheName"));
	 *		aArguments[2] = new String("The Value");
	 *		aArguments[3] = new Boolean(true or false);
	 * 
	 * Object aExt = m_xMCF.createInstanceWithArgumentsAndContext(
	 *				"com.yacme.ext.oxsit.security.cert.CertificateExtension", aArguments, m_xContext);
	 *
	 * @param _eValue array of 4 object:
	 * <p>_eValue[0] string OID</p>
	 * <p>_eValue[1] string Localized Name</p>
	 * <p>_eValue[2] string Value </p>
	 * <p>_eValue[3] Boolean(isCritical)</p>
	 * 
	 * (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 *  
	 */
	@Override
	public void initialize(Object[] _eValue) throws Exception {
		//the eValue is the byte stream of the extension
		m_sExtensionId = (String)_eValue[0];
		m_sExtensionStringName = (String)_eValue[1];
		m_sExtensionStringValue = (String)_eValue[2];
		m_bIsCritical = ((Boolean)_eValue[3]).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateExtension#getExtensionId()
	 */
	@Override
	public String getExtensionOID() {
		return m_sExtensionId;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateExtension#getExtensionStringName()
	 */
	@Override
	public String getExtensionLocalizedName() {
		return m_sExtensionStringName;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateExtension#getExtensionStringValue()
	 */
	@Override
	public String getExtensionStringValue() {
		return m_sExtensionStringValue;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.security.cert.XOX_CertificateExtension#isCritical()
	 */
	@Override
	public boolean isCritical() {
		return m_bIsCritical;
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
		// TODO Auto-generated method stub
		m_logger.entering("dispose");
//		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}
}
