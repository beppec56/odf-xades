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
import it.plio.ext.oxsit.options.OptionsParametersAccess;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

/**
 * This is a specification, it may change! This service implements a service to
 * access the SSCDs available on system. receives the doc information from the
 * task
 * 
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every
 * signature can be retrieved through properties
 * 
 * @author beppec56
 * 
 */
public class SSCDevice extends ComponentBase
		// help class, implements XTypeProvider, XInterface, XWeak
		implements XServiceInfo, XChangesNotifier, XComponent, XInitialization,
		XOX_SSCDevice {

	protected XComponentContext m_xCC;
	
	// the name of the class implementing this object
	public static final String m_sImplementationName = SSCDevice.class
			.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { GlobConstant.m_sSSCD_SERVICE };

	protected String m_sExtensionSystemPath;

	protected String m_sSSCDLibraryPath;
	protected boolean m_bSSCDAutomaticDetection;
	
	protected Vector<XOX_QualifiedCertificate>	m_vQualifiedCertList;
	
	protected DynamicLogger m_aLogger;

	private String m_sATRCode;

	private String m_sCryptoLibraryUsed;

	private String m_sDescription;

	private String m_sManufacturer;

	/**
	 * This Class implements the SSCDevice service
	 * 
	 * @param _ctx
	 *            the UNO context
	 */
	public SSCDevice(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
		m_xCC = _ctx;
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		try {
			m_sExtensionSystemPath = Helpers
					.getExtensionInstallationSystemPath(_ctx);
			m_aLogger.ctor("extension installed in: " + m_sExtensionSystemPath);
		} catch (URISyntaxException e) {
			m_aLogger.severe("ctor", "", e);
		} catch (IOException e) {
			m_aLogger.severe("ctor", "", e);
		}

		// grab the configuration information
		OptionsParametersAccess xOptionsConfigAccess = new OptionsParametersAccess(
				_ctx);
		m_bSSCDAutomaticDetection = xOptionsConfigAccess
				.getBoolean("SSCDAutomaticDetection");
		m_sSSCDLibraryPath = xOptionsConfigAccess.getText("SSCDFilePath1");
		xOptionsConfigAccess.dispose();

		m_vQualifiedCertList = new Vector<XOX_QualifiedCertificate>();
	}

	@Override
	public String getImplementationName() {
		m_aLogger.entering("getImplementationName");
		return m_sImplementationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.info("supportsService", _sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals(m_sServiceNames[i]))
				return true;
		}
		return false;
	}

	// XChangesNotifier
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.star.util.XChangesNotifier#addChangesListener(com.sun.star.util
	 * .XChangesListener)
	 */
	@Override
	public void addChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star
	 * .util.XChangesListener)
	 */
	@Override
	public void removeChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * Called when instantiating using the CreateInstanceWithArguments() method 
	 * when instantiated, _oObj[0] first certificate object (service), _oObj[1] the second ... 
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		// TODO Auto-generated method stub
		m_aLogger.entering("initialize");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		m_aLogger.log("addEventListener");
		super.addEventListener(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#dispose() called to clean up the class
	 * before closing
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		m_aLogger.log("dispose");
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		m_aLogger.log("removeEventListener");
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#addAQualifiedCertificate(it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate)
	 * 
	 * To be called for every qualified certificate that will be added
	 * 
	 */
	@Override
	public void addAQualifiedCertificate(XOX_QualifiedCertificate _aCertif) {
		// TODO Auto-generated method stub
		m_aLogger.severe("addAQualifiedCertificate", "method still to be written!");
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getQualifiedCertificates()
	 */
	@Override
	public XOX_QualifiedCertificate[] getQualifiedCertificates() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getATRcode()
	 */
	@Override
	public String getATRcode() {
		// TODO Auto-generated method stub
		return m_sATRCode;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setATRcode(java.lang.String)
	 */
	@Override
	public void setATRcode(String _sArg) {
		m_aLogger.log("setATRcode", _sArg);
		m_sATRCode = _sArg;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getCryptoLibraryUsed()
	 */
	@Override
	public String getCryptoLibraryUsed() {
		// TODO Auto-generated method stub
		return m_sCryptoLibraryUsed;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setCryptoLibraryUsed(java.lang.String)
	 */
	@Override
	public void setCryptoLibraryUsed(String _sArg) {
		m_aLogger.log("setCryptoLibraryUsed", _sArg);
		m_sCryptoLibraryUsed = _sArg;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return m_sDescription;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String _sArg) {
		m_aLogger.log("setDescription", _sArg);
		m_sDescription = _sArg;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getManufacturer()
	 */
	@Override
	public String getManufacturer() {
		// TODO Auto-generated method stub
		return m_sManufacturer;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setManufacturer(java.lang.String)
	 */
	@Override
	public void setManufacturer(String _sArg) {
		m_aLogger.log("setManufacturer", _sArg);
		m_sManufacturer = _sArg;
	}
}
