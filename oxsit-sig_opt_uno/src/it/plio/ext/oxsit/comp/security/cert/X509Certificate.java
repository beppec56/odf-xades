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

package it.plio.ext.oxsit.comp.security.cert;

import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;
import it.plio.ext.oxsit.security.cert.CertificationAuthorityState;
import it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;
import it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x509.X509CertificateStructure;

import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 *  This service implements the X509Certificate service.
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
public class X509Certificate extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_X509Certificate,
			XOX_X509CertificateDisplay
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= X509Certificate.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sX509_CERTIFICATE_SERVICE };
	private XComponentContext m_xContext;
	private XMultiComponentFactory m_xMCF;

	protected String m_sTimeLocaleString = "id_validity_time_locale";//"%1$td %1$tB %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)";
	protected String m_sLocaleLanguage = "id_iso_lang_code"; //"it";

	protected IDynamicLogger m_aLogger;
	
	private int m_nCertificateState;
	private int m_nCertificateStateConditions;
	
	private int m_nCAState;

	//the certificate representation
	private X509CertificateStructure m_aX509;

	private boolean m_bWasDisplayed;
	//Hashmap of the extension or oid state
	private Hashtable<String,Integer>	m_aElementStates = new Hashtable<String, Integer>(20);

	private boolean m_bIsFromUI;

	private XOX_CertificationPathControlProcedure m_xoxCertificationPathControlProcedure;
	private XOX_CertificateComplianceControlProcedure m_xoxCertificateComplianceControlProcedure;
	private XOX_CertificateRevocationStateControlProcedure m_xoxCertificateRevocationControlProcedure;
	// subordinate object to prepare the certificate display data according to
	// implementation requirement.
	private XOX_X509CertificateDisplay		m_xoxCertificateDisplayString;

	private XOX_X509Certificate m_xoxCertificationPath;

	private String m_sDisplayObjectKO = "Subordinate display UNO object missing!";

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public X509Certificate(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
//		m_aLogger.enableLogging();
    	m_aLogger.ctor();
    	m_nCAState = CertificationAuthorityState.NOT_YET_CHECKED_value;
    	m_nCertificateState = CertificateState.NOT_YET_VERIFIED_value;
    	m_xContext = _ctx;
    	m_xMCF = m_xContext.getServiceManager();
    	//grab the locale strings, we'll use the interface language as a locale
    	//e.g. if interface language is Italian, the locale will be Italy, Italian
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);

		try {
			m_sTimeLocaleString = m_aRegAcc.getStringFromRegistry( m_sTimeLocaleString );			
			m_sLocaleLanguage = m_aRegAcc.getStringFromRegistry( m_sLocaleLanguage );
		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("ctor", e);
		}
		m_aRegAcc.dispose();
		//locale of the extension
//		m_lTheLocale = new Locale(m_sLocaleLanguage);
	}

	public String getImplementationName() {
//		m_aLoggerDialog.entering("getImplementationName");
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	public String[] getSupportedServiceNames() {
//		m_aLoggerDialog.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.info("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	private void initializeHelper(Object _arg) throws IllegalArgumentException {
		//check the type of elements we have, can be CRL or CP control
		XOX_CertificationPathControlProcedure xCert =
			(XOX_CertificationPathControlProcedure)UnoRuntime.queryInterface(
							XOX_CertificationPathControlProcedure.class, _arg);
		if(xCert != null && m_xoxCertificationPathControlProcedure == null) {
			m_xoxCertificationPathControlProcedure = xCert;
		}
		XOX_CertificateComplianceControlProcedure xCert1 =
			(XOX_CertificateComplianceControlProcedure)UnoRuntime.queryInterface(
					XOX_CertificateComplianceControlProcedure.class, _arg);
		if(xCert1 != null && m_xoxCertificateComplianceControlProcedure == null) {
			m_xoxCertificateComplianceControlProcedure = xCert1;
		}
		XOX_CertificateRevocationStateControlProcedure xCert2 =
			(XOX_CertificateRevocationStateControlProcedure)UnoRuntime.queryInterface(
					XOX_CertificateRevocationStateControlProcedure.class, _arg);
		if(xCert2 != null && m_xoxCertificateRevocationControlProcedure == null) {
			m_xoxCertificateRevocationControlProcedure = xCert2;
		}
		XOX_X509CertificateDisplay xCert3 =
			(XOX_X509CertificateDisplay)UnoRuntime.queryInterface(
					XOX_X509CertificateDisplay.class, _arg);
		if(xCert3 != null && m_xoxCertificateDisplayString == null) {
			m_xoxCertificateDisplayString = xCert3;
		}
		if(xCert == null &&
				xCert1 == null &&
				xCert2 == null &&
				xCert3 == null
				)
		throw(new com.sun.star.lang.IllegalArgumentException());
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * _arg[0] the certificate (DER binary value), mandatory
	 * _arg[1] Boolean(true if from UI, else false), mandatory
	 * _arg[2] the interface object to control the Certification Path (e.g. the CA), optional 
	 * _arg[3] the interface object to control the CRL, optional
	 * _arg[4] the interface object to control the certificate compliance to the implementation
	 */
	@Override
	public void initialize(Object[] _arg) throws Exception {
		int argsLen = _arg.length;
		if(argsLen < 1)
			throw(new com.sun.star.lang.IllegalArgumentException("X509Certificate#initialize: missing arguments"));

		for (int idx = 1; idx <argsLen;idx++) {
			switch(idx) {
			case 1:
				m_bIsFromUI = ((Boolean)_arg[1]).booleanValue();
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				initializeHelper(_arg[idx]);				
				break;
			default:
				break;
			}
		}
		setDEREncoded((byte[]) _arg[0]);		
	}

	private void checkDisplayed() throws Exception {
		if (!m_bWasDisplayed) {
			//init the display object
			if(m_xoxCertificateDisplayString != null) {
				m_xoxCertificateDisplayString.prepareDisplayStrings(null, this);
				m_bWasDisplayed = true;
			}
			else
				throw (new Exception("m_sDisplayObjectKO") );
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectDisplayName()
	 */
	@Override
	public String getSubjectDisplayName() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSubjectDisplayName();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getVersion()
	 */
	@Override
	public String getVersion() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getVersion();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotValidAfter()
	 */
	@Override
	public String getNotValidAfter() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getNotValidAfter();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotValidBefore()
	 */
	@Override
	public String getNotValidBefore() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getNotValidBefore();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getIssuerDisplayName()
	 */
	@Override
	public String getIssuerDisplayName() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getIssuerDisplayName();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)

	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getIssuerName()
	 */
	@Override
	public String getIssuerName() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getIssuerName();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getIssuerCommonName()
	 */
	@Override
	public String getIssuerCommonName() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getIssuerCommonName();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getMD5Thumbprint()
	 */
	@Override
	public String getMD5Thumbprint() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getMD5Thumbprint();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSHA1Thumbprint()
	 */
	@Override
	public String getSHA1Thumbprint() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSHA1Thumbprint();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSerialNumber()
	 */
	@Override
	public String getSerialNumber() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSerialNumber();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSignatureAlgorithm()
	 */
	@Override
	public String getSignatureAlgorithm() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSignatureAlgorithm();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectName()
	 */
	@Override
	public String getSubjectName() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSubjectName();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectPublicKeyAlgorithm()
	 */
	@Override
	public String getSubjectPublicKeyAlgorithm() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSubjectPublicKeyAlgorithm();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectPublicKeyValue()
	 */
	@Override
	public String getSubjectPublicKeyValue() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSubjectPublicKeyValue();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#verifyCertificate(com.sun.star.frame.XFrame)
	 */
	@Override
	public void verifyCertificate(XFrame _aFrame) {
		//FIXME add the reset of states: certificate, CA, and the respective  state conditions
		m_nCertificateState = CertificateState.NOT_YET_VERIFIED_value;
		m_nCertificateStateConditions = CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED_value;

		//check the certificate for the compliance
		try {
			verifyCertificateCompliance(_aFrame);
		//check and fill the certification path
			verifyCertificationPath(_aFrame);
		//check the crl of the certificate
			verifyCertificateRevocationState(_aFrame);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#verifyCertificationPath(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void verifyCertificationPath(XFrame _aFrame) throws IllegalArgumentException, Exception {
		if(m_xoxCertificationPathControlProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					m_xoxCertificationPathControlProcedure.verifyCertificationPath(_aFrame,xCtl);
					setCertifPathStateHelper(m_xoxCertificationPathControlProcedure.getCertificationAuthorityState());
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Throwable e) {
					m_aLogger.severe(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#verifyCertificateRevocationState(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void verifyCertificateRevocationState(
			XFrame frame) throws IllegalArgumentException,
			Exception {
		if(m_xoxCertificateRevocationControlProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					m_xoxCertificateRevocationControlProcedure.initializeProcedure(frame);
					m_xoxCertificateRevocationControlProcedure.verifyCertificateRevocationState(frame,xCtl);
/*					m_aLogger.log("State: "+m_xoxCertificateRevocationControlProcedure.getCertificateState().getValue()+
							" conditions: "+
							m_xoxCertificateRevocationControlProcedure.getCertificateStateConditions().getValue());*/
					setCertificateStateHelper(m_xoxCertificateRevocationControlProcedure.getCertificateState());
					setCertificateStateConditionsHelper(m_xoxCertificateRevocationControlProcedure.getCertificateStateConditions());
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Throwable e) {
					m_aLogger.severe(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateState()
	 */
	@Override
	public int getCertificateState() {
		return m_nCertificateState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateStateConditions()
	 */
	@Override
	public int getCertificateStateConditions() {
		return m_nCertificateStateConditions;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificationAuthorityState()
	 */
	@Override
	public int getCertificationAuthorityState() {
		return m_nCAState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificationPath()
	 */
	@Override
	public XOX_X509Certificate getCertificationPath() {
		return m_xoxCertificationPath;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificationPath(it.plio.ext.oxsit.security.cert.XOX_X509Certificate)
	 */
	@Override
	public void setCertificationPath(XOX_X509Certificate arg0) {
		m_xoxCertificationPath = arg0;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getDEREncoded()
	 */
	@Override
	public byte[] getDEREncoded() {
		if(m_aX509 != null)
			return m_aX509.getDEREncoded();
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setDEREncoded(byte[])
	 * 
	 * When this method is called, the DER image passed will be used as the new certificate representation
	 * and the certificate extensions will be evaluated again. 
	 */
	@Override
	public void setDEREncoded(byte[] _DEREncoded) {
		//
		m_aX509 = null; //remove old certificate
						//remove old data from HashMaps
/*		m_aExtensions.clear();
		m_aExtensionLocalizedNames.clear();
		m_aExtensionDisplayValues.clear();
		m_aCriticalExtensions.clear();
		m_aNotCriticalExtensions.clear();*/

		ByteArrayInputStream as = new ByteArrayInputStream(_DEREncoded); 
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado;
		try {
			ado = aderin.readObject();
			m_aX509 = new X509CertificateStructure((ASN1Sequence) ado);
		} catch (IOException e) {
			m_aLogger.severe("setDEREncoded", e);
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateComplianceControl()
	 */
	@Override
	public XOX_CertificateComplianceControlProcedure getComplianceControlObj() {
		return m_xoxCertificateComplianceControlProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificateComplianceControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure)
	 */
	@Override
	public void setCertificateComplianceControlObject(
			XOX_CertificateComplianceControlProcedure arg0)
			throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		XOX_CertificateComplianceControlProcedure xCert = (XOX_CertificateComplianceControlProcedure)
		UnoRuntime.queryInterface(XOX_CertificateComplianceControlProcedure.class, arg0);
		if(xCert == null)
			throw(new com.sun.star.lang.IllegalArgumentException());
		m_xoxCertificateComplianceControlProcedure = xCert;	
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#verifyCertificateCompliance(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void verifyCertificateCompliance(XFrame arg0)
				throws IllegalArgumentException, Exception {
		XOX_X509Certificate m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, this);
		if(m_xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance wrong argument"));
		if(m_xoxCertificateComplianceControlProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					m_xoxCertificateComplianceControlProcedure.initializeProcedure(arg0);
					m_xoxCertificateComplianceControlProcedure.verifyCertificateCompliance(arg0,xCtl);
	//					m_aLogger.log("State: "+m_xoxCertificateComplianceControlProcedure.getCertificateState().getValue());
					setCertificateStateHelper(m_xoxCertificateComplianceControlProcedure.getCertificateState());
					setCertificateStateConditionsHelper(m_xoxCertificateComplianceControlProcedure.getCertificateStateConditions());
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Throwable e) {
					m_aLogger.severe(e);
				}
			}
		}
	}
	
	////////////////// internal functions

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateElementErrorState(java.lang.String)
	 */
	@Override
	public int getCertificateElementErrorState(String _oid) {
		Integer aInt = m_aElementStates.get(_oid);
		if(aInt != null)
			return aInt.intValue();
		return CertificateElementState.OK_value;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificateExtensionErrorState(java.lang.String, int)
	 */
	@Override
	public void setCertificateElementErrorState(String _oid, int arg1) {
		m_aElementStates.put(_oid, new Integer(arg1));		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateExtensionName(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionName(String _oid) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateExtensionName(_oid);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateExtensionStringValue(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionStringValue(String _oid) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateExtensionStringValue(_oid);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateExtensionOIDs()
	 */
	@Override
	public String[] getCertificateExtensionOIDs() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateExtensionOIDs();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCriticalCertificateExtensionOIDs()
	 */
	@Override
	public String[] getCriticalCertificateExtensionOIDs() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCriticalCertificateExtensionOIDs();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotCriticalCertificateExtensionOIDs()
	 */
	@Override
	public String[] getNotCriticalCertificateExtensionOIDs() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getNotCriticalCertificateExtensionOIDs();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCriticalExtensions()
	 */
	@Override
	public XOX_CertificateExtension[] getCriticalExtensions() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCriticalExtensions();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotCriticalExtensions()
	 */
	@Override
	public XOX_CertificateExtension[] getNotCriticalExtensions() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getNotCriticalExtensions();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
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
		// FIXME 
		// TODO need to check if this element is referenced somewhere before deallocating it
		m_aLogger.entering("dispose");
/*		if(m_xExt != null) {
			for(int i=0; i < m_xExt.length; i++) {
				XOX_CertificateExtension xExt = m_xExt[i];
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xExt);
				if(xComp != null)
					xComp.dispose();
			}
		}
		if(m_xCritExt != null) {
			for(int i=0; i < m_xCritExt.length; i++) {
				XOX_CertificateExtension xExt = m_xCritExt[i];
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xExt);
				if(xComp != null)
					xComp.dispose();
			}			
		}
		super.dispose();*/
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateCertificationPathControl()
	 */
	@Override
	public XOX_CertificationPathControlProcedure getCertificationPathControlObj() {
		// TODO Auto-generated method stub
		return m_xoxCertificationPathControlProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificationPathControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure)
	 */
	@Override
	public void setCertificationPathControlObject(
			XOX_CertificationPathControlProcedure arg0)
			throws IllegalArgumentException, Exception {

		XOX_CertificationPathControlProcedure xCert = (XOX_CertificationPathControlProcedure)
		UnoRuntime.queryInterface(XOX_CertificationPathControlProcedure.class, arg0);
		if(xCert == null)
			throw(new com.sun.star.lang.IllegalArgumentException());
		m_xoxCertificationPathControlProcedure = xCert;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateRevocationControlObj()
	 */
	@Override
	public XOX_CertificateRevocationStateControlProcedure getRevocationControlObj() {
		return m_xoxCertificateRevocationControlProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setRevocationStateControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure)
	 */
	@Override
	public void setRevocationStateControlObject(
			XOX_CertificateRevocationStateControlProcedure arg0)
			throws IllegalArgumentException, Exception {
		m_xoxCertificateRevocationControlProcedure = arg0;
	}
	
	/**
	 * set the certificate state according to the new value, as enum,
	 * mapped to numerical
	 */
	private void setCertifPathStateHelper(CertificationAuthorityState _newState) {
		int _nNewState;
		if((_nNewState = _newState.getValue()) > m_nCAState)
			m_nCAState = _nNewState;
	}

	/**
	 * set the certificate state according to the new value, as enum,
	 * mapped to numerical
	 */
	private void setCertificateStateHelper(CertificateState _newState) {
		int _nNewState;
		if((_nNewState = _newState.getValue()) > m_nCertificateState)
			m_nCertificateState = _nNewState;
	}

	/**
	 * set the certificate state according to the new value, as enum,
	 * mapped to numerical
	 */
	private void setCertificateStateConditionsHelper(CertificateStateConditions _newState) {
		int _nNewState;
		if((_nNewState = _newState.getValue())
				> m_nCertificateStateConditions)
			m_nCertificateStateConditions = _nNewState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#prepareDisplayStrings(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void prepareDisplayStrings(XFrame arg0, XComponent arg1)
			throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
//call the subcomponent method?	
	}

}
