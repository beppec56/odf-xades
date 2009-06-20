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
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.CertificateElementID;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;
import it.plio.ext.oxsit.security.cert.CertificationAuthorityState;
import it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes;
import it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificationPathProcedure;
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
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
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
			XOX_CertificatePKCS11Attributes,
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
	
	//used internally in initialization
	private int			m_nElementPosition;
	private	Object[]	m_oTheProcedures = new Object[3];

	//the certificate representation
	private X509CertificateStructure m_aX509;

	private boolean m_bWasDisplayed;
	//Hashmap of the extension or oid state
	private Hashtable<String,Integer>	m_aElementStates = new Hashtable<String, Integer>(20);

	private boolean m_bIsFromUI;

	private XOX_CertificationPathProcedure m_xoxCertificationPathProcedure;
	private XOX_CertificateComplianceProcedure m_xoxCertificateComplianceProcedure;
	private XOX_CertificateRevocationStateProcedure m_xoxCertificateRevocationProcedure;
	// subordinate object to prepare the certificate display data according to
	// implementation requirement.
	private XOX_X509CertificateDisplay		m_xoxCertificateDisplayString;

	private XOX_X509Certificate m_xoxCertificationPath;
	
	private XOX_SSCDevice			m_oSSCDevice;

	private String m_sDisplayObjectKO = "Subordinate display UNO object missing!";
	
	private XOX_CertificateExtension[] m_xCritExt = null;
	private XOX_CertificateExtension[] m_xExt = null;

	private String m_sCertficateLabel;

	private byte[] m_aCertificateID;

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
		XOX_CertificateComplianceProcedure xCert1 =
			(XOX_CertificateComplianceProcedure)UnoRuntime.queryInterface(
					XOX_CertificateComplianceProcedure.class, _arg);
		if(xCert1 != null && m_xoxCertificateComplianceProcedure == null
				&& m_nElementPosition < 3) {
			m_xoxCertificateComplianceProcedure = xCert1;
			m_oTheProcedures[m_nElementPosition++] = xCert1;
		}
		XOX_CertificationPathProcedure xCert =
			(XOX_CertificationPathProcedure)UnoRuntime.queryInterface(
							XOX_CertificationPathProcedure.class, _arg);
		if(xCert != null && m_xoxCertificationPathProcedure == null
				&& m_nElementPosition < 3) {
			m_xoxCertificationPathProcedure = xCert;
			m_oTheProcedures[m_nElementPosition++] = xCert;
		}
		XOX_CertificateRevocationStateProcedure xCert2 =
			(XOX_CertificateRevocationStateProcedure)UnoRuntime.queryInterface(
					XOX_CertificateRevocationStateProcedure.class, _arg);
		if(xCert2 != null && m_xoxCertificateRevocationProcedure == null
				&& m_nElementPosition < 3) {
			m_xoxCertificateRevocationProcedure = xCert2;
			m_oTheProcedures[m_nElementPosition++] = xCert2;
		}
		XOX_X509CertificateDisplay xCert3 =
			(XOX_X509CertificateDisplay)UnoRuntime.queryInterface(
					XOX_X509CertificateDisplay.class, _arg);
		if(xCert3 != null && m_xoxCertificateDisplayString == null) {
			m_xoxCertificateDisplayString = xCert3;
			return;
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

		m_nElementPosition = 0; 
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
				throw(new com.sun.star.lang.IllegalArgumentException("X509Certificate#initialize: too many arguments"));
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSubjectDisplayName()
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
	//@Override
	public String getSubjectPublicKeyValue() {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getSubjectPublicKeyValue();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	private void verifyCertificateHelper(XFrame _aFrame) {
		//grab the array of checkers
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#verifyCertificate(com.sun.star.frame.XFrame)
	 */
	@Override
	public void verifyCertificate(XFrame _aFrame) {
		//FIXME add the reset of states: certificate, CA, and the respective  state conditions
		m_nCertificateState = CertificateState.NOT_YET_VERIFIED_value;
		m_nCertificateStateConditions = CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED_value;

		for(int i = 0; i <3; i++) {
			Object oProc = m_oTheProcedures[i];
			
			if(oProc instanceof XOX_CertificateComplianceProcedure) {
				try {
					verifyCertificateCompliance(_aFrame);
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Exception e) {
					m_aLogger.severe(e);
				}
			}
			if(oProc instanceof XOX_CertificationPathProcedure) {
				try {
					verifyCertificationPath(_aFrame);
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Exception e) {
					m_aLogger.severe(e);
				}
			}
			if(oProc instanceof XOX_CertificateRevocationStateProcedure) {
				try {
					verifyCertificateRevocationState(_aFrame);
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Exception e) {
					m_aLogger.severe(e);
				}
			}
		}		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#verifyCertificationPath(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void verifyCertificationPath(XFrame _aFrame) throws IllegalArgumentException, Exception {
		if(m_xoxCertificationPathProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					m_xoxCertificationPathProcedure.verifyCertificationPath(_aFrame,xCtl);
					setCertifPathStateHelper(m_xoxCertificationPathProcedure.getCertificationAuthorityState());
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
		if(m_xoxCertificateRevocationProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					m_xoxCertificateRevocationProcedure.initializeProcedure(frame);
					m_xoxCertificateRevocationProcedure.verifyCertificateRevocationState(frame,xCtl);
/*					m_aLogger.log("State: "+m_xoxCertificateRevocationProcedure.getCertificateState().getValue()+
							" conditions: "+
							m_xoxCertificateRevocationProcedure.getCertificateStateConditions().getValue());*/
					setCertificateStateHelper(m_xoxCertificateRevocationProcedure.getCertificateState());
					setCertificateStateConditionsHelper(m_xoxCertificateRevocationProcedure.getCertificateStateConditions());
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
	public XOX_CertificateComplianceProcedure getComplianceControlObj() {
		return m_xoxCertificateComplianceProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificateComplianceControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure)
	 */
	@Override
	public void setCertificateComplianceControlObject(
			XOX_CertificateComplianceProcedure arg0)
			throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		XOX_CertificateComplianceProcedure xCert = (XOX_CertificateComplianceProcedure)
		UnoRuntime.queryInterface(XOX_CertificateComplianceProcedure.class, arg0);
		if(xCert == null)
			throw(new com.sun.star.lang.IllegalArgumentException());
		m_xoxCertificateComplianceProcedure = xCert;	
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
		if(m_xoxCertificateComplianceProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					m_xoxCertificateComplianceProcedure.initializeProcedure(arg0);
					m_xoxCertificateComplianceProcedure.verifyCertificateCompliance(arg0,xCtl);
	//					m_aLogger.log("State: "+m_xoxCertificateComplianceProcedure.getCertificateState().getValue());
					setCertificateStateHelper(m_xoxCertificateComplianceProcedure.getCertificateState());
					setCertificateStateConditionsHelper(m_xoxCertificateComplianceProcedure.getCertificateStateConditions());
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
	public String getCertificateExtensionLocalizedName(String _oid) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateExtensionLocalizedName(_oid);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateElementLocalizedName(it.plio.ext.oxsit.security.cert.CertificateElementID)
	 */
	@Override
	public String getCertificateElementLocalizedName(CertificateElementID _CertElId) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateElementLocalizedName(_CertElId);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}	
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateExtensionStringValue(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionValueString(String _oid) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateExtensionValueString(_oid);
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

	private XOX_CertificateExtension[] getExtensionsHelper(String[] critOIDs, boolean _bIsCritical) {
		XOX_CertificateExtension[] retValue = new XOX_CertificateExtension[critOIDs.length];
//		X509Extensions aExts = m_aX509.getTBSCertificate().getExtensions();
		//fill the retValue
		for(int i=0;i< critOIDs.length;i++) {
			Object[] aArguments = new Object[4];
			aArguments[0] = new String(critOIDs[i]);//aExts.getExtension(new DERObjectIdentifier(critOIDs[i])).getValue().getOctets();
			aArguments[1] = new String(getCertificateExtensionLocalizedName(critOIDs[i]));
			aArguments[2] = new String(getCertificateExtensionValueString(critOIDs[i]));
			aArguments[3] = new Boolean(_bIsCritical);

			try {
				Object	aExt = m_xMCF.createInstanceWithArgumentsAndContext(
							GlobConstant.m_sCERTIFICATE_EXTENSION_SERVICE, aArguments, m_xContext);
				retValue[i] = (XOX_CertificateExtension)UnoRuntime.queryInterface(XOX_CertificateExtension.class, aExt);
			} catch (Exception e) {
				m_aLogger.severe("getExtensionsHelper", e);
			} catch (RuntimeException e) {
				m_aLogger.severe("getExtensionsHelper", e);
			}
		}
		return retValue;		
	}
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCriticalExtensions()
	 */
/*	@Override
	public XOX_CertificateExtension[] getCriticalExtensions() {
		
		
		
		
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCriticalExtensions();
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
	}*/

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCriticalExtensions()
	 */
	@Override
	public XOX_CertificateExtension[] getCriticalExtensions() {
		//build all the critical extensions, returns the array
		if(m_xCritExt == null) {
			String[] critOIDs = getCriticalCertificateExtensionOIDs();
			m_xCritExt = getExtensionsHelper(critOIDs,true);
		}
		return m_xCritExt;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getNotCriticalExtensions()
	 */
	@Override
	public XOX_CertificateExtension[] getNotCriticalExtensions() {
		//build all the not critical extensions, returns the array
		if(m_xExt == null) {
			String[] critOIDs = getNotCriticalCertificateExtensionOIDs();
			m_xExt = getExtensionsHelper(critOIDs,false);
		}
		return m_xExt;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateElementCommentString(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionCommentString(String _Name) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateExtensionCommentString(_Name);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateElementCommentString(it.plio.ext.oxsit.security.cert.CertificateElementID)
	 */
	@Override
	public String getCertificateElementCommentString(CertificateElementID _CertElId) {
		try {
			checkDisplayed();
			return 	m_xoxCertificateDisplayString.getCertificateElementCommentString(_CertElId);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return m_sDisplayObjectKO;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#setCertificateElementCommentString(it.plio.ext.oxsit.security.cert.CertificateElementID, java.lang.String)
	 */
	@Override
	public void setCertificateElementCommentString(CertificateElementID _CertElID,
			String _Comment) {
		try {
			checkDisplayed();
			m_xoxCertificateDisplayString.setCertificateElementCommentString(_CertElID,_Comment);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
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

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#addCertificateReport(com.sun.star.text.XTextDocument)
	 */
	@Override
	public void addCertificateReport(XTextDocument _aTextDocument, XComponent _xCertificate)
			throws IllegalArgumentException, Exception {
		try {
			checkDisplayed();
			m_xoxCertificateDisplayString.addCertificateReport(_aTextDocument,_xCertificate);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#generateCertificateReport()
	 */
	@Override
	public void generateCertificateReport( XComponent _xCertificate)
	throws IllegalArgumentException, Exception {
		try {
			checkDisplayed();
			m_xoxCertificateDisplayString.generateCertificateReport(_xCertificate);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#setCertificateElementCommentString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setCertificateExtensionCommentString(String _Name, String _Comment) {
		try {
			checkDisplayed();
			m_xoxCertificateDisplayString.setCertificateExtensionCommentString(_Name,_Comment);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
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
		// FIXME need to check if this element is referenced somewhere before deallocating it
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
	public XOX_CertificationPathProcedure getCertificationPathControlObj() {
		return m_xoxCertificationPathProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificationPathControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificationPathProcedure)
	 */
	@Override
	public void setCertificationPathControlObject(
			XOX_CertificationPathProcedure arg0)
			throws IllegalArgumentException, Exception {

		XOX_CertificationPathProcedure xCert = (XOX_CertificationPathProcedure)
		UnoRuntime.queryInterface(XOX_CertificationPathProcedure.class, arg0);
		if(xCert == null)
			throw(new com.sun.star.lang.IllegalArgumentException());
		m_xoxCertificationPathProcedure = xCert;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateRevocationControlObj()
	 */
	@Override
	public XOX_CertificateRevocationStateProcedure getRevocationControlObj() {
		return m_xoxCertificateRevocationProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setRevocationStateControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateProcedure)
	 */
	@Override
	public void setRevocationStateControlObject(
			XOX_CertificateRevocationStateProcedure arg0)
			throws IllegalArgumentException, Exception {
		m_xoxCertificateRevocationProcedure = arg0;
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

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateDisplayObj()
	 */
	@Override
	public XOX_X509CertificateDisplay getCertificateDisplayObj() {
		return this;//it's implemented as well
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSSCDevice()
	 */
	@Override
	public Object getSSCDevice() {
		return m_oSSCDevice;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setSSCDevice(com.sun.star.lang.XComponent)
	 */
	@Override
	public void setSSCDevice(Object _SSCD) {
		m_oSSCDevice = (XOX_SSCDevice)UnoRuntime.queryInterface(XOX_SSCDevice.class, _SSCD);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateAttributes()
	 */
	@Override
	public XOX_CertificatePKCS11Attributes getCertificateAttributes() {
		return this;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setCertificateAttributes(it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes)
	 */
	@Override
	public void setCertificateAttributes(XOX_CertificatePKCS11Attributes _Attributes) {
		setID(_Attributes.getID());
		setLabel(_Attributes.getLabel());
		setDEREncoded(_Attributes.getDEREncoded());
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#getID()
	 */
	@Override
	public byte[] getID() {
		return m_aCertificateID;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#setID(byte[])
	 */
	@Override
	public void setID(byte[] _ID) {
		m_aCertificateID = _ID;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#getLabel()
	 */
	@Override
	public String getLabel() {
		return m_sCertficateLabel;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(String _Label) {
		m_sCertficateLabel = _Label;
	}
}
