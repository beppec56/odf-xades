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

package it.plio.ext.oxsit.cust_it.comp.security;

import it.plio.ext.oxsit.cust_it.ConstantCustomIT;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.options.OptionsParametersAccess;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;

import java.util.Vector;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
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
public class SSCDevice_IT extends ComponentBase
		// help class, implements XTypeProvider, XInterface, XWeak
		implements XServiceInfo, XChangesNotifier, XComponent, XInitialization,
		XOX_SSCDevice {

	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_xMCF;
	
	// the name of the class implementing this object
	public static final String m_sImplementationName = SSCDevice_IT.class
			.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { ConstantCustomIT.m_sSSCD_SERVICE };

	protected String m_sSSCDLibraryPath;
	protected boolean m_bSSCDAutomaticDetection;
	
	protected Vector<XOX_X509Certificate>	m_xQualCertList;
	
	protected DynamicLogger m_aLogger;

	private String m_sATRCode;

	private String m_sCryptoLibraryUsed;

	private String m_sDescription;

	private String m_sManufacturer;
	private String m_sCryptoLibrariesConfigured;

	/**
	 * This Class implements the SSCDevice_IT service
	 * 
	 * @param _ctx
	 *            the UNO context
	 */
	public SSCDevice_IT(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();	
		m_aLogger.enableLogging();
		m_aLogger.ctor();

		// grab the configuration information
		OptionsParametersAccess xOptionsConfigAccess = new OptionsParametersAccess(
				_ctx);
		m_bSSCDAutomaticDetection = xOptionsConfigAccess
				.getBoolean("SSCDAutomaticDetection");
		m_sSSCDLibraryPath = xOptionsConfigAccess.getText("SSCDFilePath1");
		xOptionsConfigAccess.dispose();

		m_xQualCertList = new Vector<XOX_X509Certificate>(10,1);
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
	 * Called when instantiating using the CreateInstanceWithArgumentsAndContext() method 
	 * when instantiated, _oObj[0] first certificate object (service), _oObj[1] the second ... 
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		// TODO Auto-generated method stub
		m_aLogger.entering("initialize");
		throw(new com.sun.star.lang.NoSupportException("method com.sun.star.lang.XInitialization#initialize not yet supported"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
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
		// FIXME 
		// TODO need to check if this element is referenced somewhere before deallocating it		m_aLogger.entering("dispose");
		//dispose of all the certificate
/*		if(!m_xQualCertList.isEmpty()) {
			for(int i=0; i< m_xQualCertList.size();i++) {
				XOX_QualifiedCertificate xQC = m_xQualCertList.elementAt(i);
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xQC);
				if(xComp != null)
					xComp.dispose();
			}
		}
		super.dispose();*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#addCertificate(byte[])
	 */
	@Override
	public void addCertificate(byte[] _aDERencoded) {
		// instantiate the components needed to check this certificate
		// create the Certificate Control UNO objects
		// first the certificate compliance control
		try {
			Object oCertCompl;
			oCertCompl = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sCERTIFICATE_COMPLIANCE_SERVICE_IT, m_xCC);
			// now the certification path control
			Object oCertPath = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sCERTIFICATION_PATH_SERVICE_IT, m_xCC);
			Object oCertRev = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sCERTIFICATE_REVOCATION_SERVICE_IT, m_xCC);
			Object oCertDisp = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sX509_CERTIFICATE_DISPLAY_SERVICE_SUBJ_IT,
					m_xCC);

			// prepare objects for subordinate service
			Object[] aArguments = new Object[6];
			// byte[] aCert = cert.getEncoded();
			// set the certificate raw value
			aArguments[0] = _aDERencoded;// aCert;
			aArguments[1] = new Boolean(false);// FIXME change according to UI
												// (true) or not UI (false)
			// the order used for the following three certificate check objects
			// is the same that will be used for a full check of the certificate
			// if one of your checker object implements more than one interface
			// when XOX_X509Certificate.verifyCertificate will be called,
			// the checkers will be called in a fixed sequence (compliance,
			// certification path, revocation state).
			aArguments[2] = oCertCompl; // the compliance checker object, which
										// implements the needed interface
			aArguments[3] = oCertPath;// the certification path checker
			aArguments[4] = oCertRev; // the revocation state checker

			// the display formatter can be passed in any order, here it's the
			// last one
			aArguments[5] = oCertDisp;

			Object oACertificate;
			oACertificate = m_xMCF
					.createInstanceWithArgumentsAndContext(
							GlobConstant.m_sX509_CERTIFICATE_SERVICE,
							aArguments, m_xCC);
			// get the main interface
			XOX_X509Certificate xQualCert = (XOX_X509Certificate) UnoRuntime
					.queryInterface(XOX_X509Certificate.class, oACertificate);
			
			//add this device as the source device for this certificate
			//(will be handly if we sign with the corresponding private key)
			xQualCert.setSSCDevice(this);

			m_xQualCertList.add(xQualCert);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getQualifiedCertificates()
	 */
	@Override
	public XOX_X509Certificate[] getX509Certificates() {
		XOX_X509Certificate[] ret = null;
		//detect the number of vector present
		if(!m_xQualCertList.isEmpty()) {
			ret = new XOX_X509Certificate[m_xQualCertList.size()];
			try {
				m_xQualCertList.copyInto(ret);
			} catch(NullPointerException ex) {
				m_aLogger.severe("getQualifiedCertificates",ex);
			} catch(IndexOutOfBoundsException ex) {
				m_aLogger.severe("getQualifiedCertificates",ex);
			} catch(ArrayStoreException ex) {
				m_aLogger.severe("getQualifiedCertificates",ex);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getHasQualifiedCertificates()
	 */
	@Override
	public int getHasX509Certificates() {
		return m_xQualCertList.size();
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getATRcode()
	 */
	@Override
	public String getATRcode() {
		return m_sATRCode;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setATRcode(java.lang.String)
	 */
	@Override
	public void setATRcode(String _sArg) {
		m_sATRCode = _sArg;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getCryptoLibraryUsed()
	 */
	@Override
	public String getCryptoLibraryUsed() {
		return m_sCryptoLibraryUsed;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setCryptoLibraryUsed(java.lang.String)
	 */
	@Override
	public void setCryptoLibraryUsed(String _sArg) {
		m_sCryptoLibraryUsed = _sArg;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getCryptoLibrariesConfigured()
	 */
	@Override
	public String getCryptoLibrariesConfigured() {
		return m_sCryptoLibrariesConfigured;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setCryptoLibrariesConfigured(java.lang.String)
	 */
	@Override
	public void setCryptoLibrariesConfigured(String arg0) {
		m_sCryptoLibrariesConfigured = arg0;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getDescription()
	 */
	@Override
	public String getDescription() {
		return m_sDescription;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String _sArg) {
		m_sDescription = _sArg;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#getManufacturer()
	 */
	@Override
	public String getManufacturer() {
		return m_sManufacturer;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_SSCDevice#setManufacturer(java.lang.String)
	 */
	@Override
	public void setManufacturer(String _sArg) {
		m_sManufacturer = _sArg;
	}
}
