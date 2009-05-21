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

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.cert.CertificateAuthorityState;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 *  This service implements the QualifiedCertificate service.
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
public class QualifiedCertificate extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_QualifiedCertificate
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= QualifiedCertificate.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sQUALIFIED_CERTIFICATE_SERVICE };
	private XComponentContext m_xContext;
	private XMultiComponentFactory m_xMCF;

	protected String m_sTimeLocaleString = "id_validity_time_locale";//"%1$td %1$tB %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)";
	protected String m_sLocaleLanguage = "id_iso_lang_code"; //"it";

	protected IDynamicLogger m_aLogger;
	
	protected int m_nCAState;
	protected int m_nCertificateState;
	
	protected boolean	m_bDisplayOID;

	//the certificate representation
	private X509CertificateStructure m_aX509;

	private String m_sSubjectDisplayName;
	
	private String m_sSubjectName = "";

	private String m_sVersion = "";

	private String m_sSerialNumber = "";

	private String m_sIssuerDisplayName = "";

	private String m_sIssuerName = "";

	private String m_sNotValidAfter = "";

	private String m_sNotValidBefore = "";

	private String m_sSubjectPublicKeyAlgorithm = "";

	private String m_sSubjectPublicKeyValue = "";

	private String m_sSignatureAlgorithm = "";

	private String m_sMD5Thumbprint = "";

	private String m_sSHA1Thumbprint = "";

	private Locale m_lTheLocale;

	private XOX_CertificateExtension[] m_xCritExt = null;
	private XOX_CertificateExtension[] m_xExt = null;
	
	//the hash map of all the extensions
	//the String is the OID,
	private HashMap<String,X509Extension>	m_aExtensions = new HashMap<String, X509Extension>(20);
	private HashMap<String,String>			m_aExtensionLocalizedNames = new HashMap<String, String>(20);
	private HashMap<String,String>			m_aExtensionDisplayValues = new HashMap<String, String>(20);
	//the hash map of all the critical extensions
	private HashMap<String,X509Extension>	m_aCriticalExtensions = new HashMap<String, X509Extension>(20);
	//the hash map of all the non critical extensions
	private HashMap<String,X509Extension>	m_aNotCriticalExtensions = new HashMap<String, X509Extension>(20);
	//Hashmap of the extension or oid state
	private Hashtable<String,Integer>	m_aElementStates = new Hashtable<String, Integer>(20);

	private boolean m_bIsFromUI;

	private XOX_CertificationPathControlProcedure m_xoxCertificationPathControlProcedure;

	private XOX_CertificateComplianceControlProcedure m_xoxCertificateComplianceControlProcedure;

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public QualifiedCertificate(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
//
		m_aLogger.enableLogging();
    	m_aLogger.ctor();
    	m_nCAState = CertificateAuthorityState.UNCHECKED_value;
    	m_nCertificateState = CertificateState.NOT_VERIFIABLE_value;
    	m_xContext = _ctx;
    	m_xMCF = m_xContext.getServiceManager();
    	m_bDisplayOID = false;
    	m_bIsFromUI = false;
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
		m_lTheLocale = new Locale(m_sLocaleLanguage);
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
		if(xCert != null) {
			m_xoxCertificationPathControlProcedure = xCert;
			return;
		}
		else {
			XOX_CertificateComplianceControlProcedure xCert1 =
				(XOX_CertificateComplianceControlProcedure)UnoRuntime.queryInterface(
						XOX_CertificateComplianceControlProcedure.class, _arg);
			
			if(xCert1 != null) {
				m_xoxCertificateComplianceControlProcedure = xCert1;
				return;
			}
		}
		throw(new com.sun.star.lang.IllegalArgumentException());
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * _arg[0] the certificate (DER binary value), mandatory
	 * _arg[1] Boolean(true if from UI, else false), mandatory
	 * _arg[2] the interface object to control the Certification Path (e.g. the CA), optional 
	 * _arg[3] the interface object to control the CRL, optional
	 * _arg[4] the interface object to control the certificate compliance to the implmentation
	 */
	@Override
	public void initialize(Object[] _arg) throws Exception {
		byte[] theCert;
		if(_arg.length >0) {
			theCert = (byte[]) _arg[0];
			if(_arg.length >1) {
				m_bIsFromUI = ((Boolean)_arg[1]).booleanValue();
				//check the type of optional elements we have
				if(_arg.length>2) {
					initializeHelper(_arg[2]);
					if(_arg.length>3) {
						initializeHelper(_arg[3]);
						if(_arg.length>4)
							initializeHelper(_arg[4]);
					}
				}
				setDEREncoded(theCert);
				return;
			}
		}
		throw(new com.sun.star.lang.IllegalArgumentException("QualifiedCertificate#initialize: missing arguments"));
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectDisplayName()
	 */
	@Override
	public String getSubjectDisplayName() {
		return m_sSubjectDisplayName;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getVersion()
	 */
	@Override
	public String getVersion() {
		return m_sVersion;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getNotValidAfter()
	 */
	@Override
	public String getNotValidAfter() {
		return m_sNotValidAfter;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getNotValidBefore()
	 */
	@Override
	public String getNotValidBefore() {
		return m_sNotValidBefore;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getIssuerDisplayName()
	 */
	@Override
	public String getIssuerDisplayName() {
		return m_sIssuerDisplayName;
	}

	/* (non-Javadoc)

	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getIssuerName()
	 */
	@Override
	public String getIssuerName() {
		return m_sIssuerName;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getMD5Thumbprint()
	 */
	@Override
	public String getMD5Thumbprint() {
		return m_sMD5Thumbprint;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSHA1Thumbprint()
	 */
	@Override
	public String getSHA1Thumbprint() {
		return m_sSHA1Thumbprint;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSerialNumber()
	 */
	@Override
	public String getSerialNumber() {
		return m_sSerialNumber;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSignatureAlgorithm()
	 */
	@Override
	public String getSignatureAlgorithm() {
		return m_sSignatureAlgorithm;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectName()
	 */
	@Override
	public String getSubjectName() {
		return m_sSubjectName;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectPublicKeyAlgorithm()
	 */
	@Override
	public String getSubjectPublicKeyAlgorithm() {
		return m_sSubjectPublicKeyAlgorithm;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectPublicKeyValue()
	 */
	@Override
	public String getSubjectPublicKeyValue() {
		return m_sSubjectPublicKeyValue;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#verifyCAForCertificate()
	 */
	@Override
	public boolean verifyCertificateCertificationPath() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#verifyCRLForCertificate()
	 */
	@Override
	public boolean verifyCRLForCertificate() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateState()
	 */
	@Override
	public int getCertificateState() {
		return m_nCertificateState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateStateConditions()
	 */
	@Override
	public int getCertificateStateConditions() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificationAuthorityState()
	 */
	@Override
	public int getCertificationAuthorityState() {
		return m_nCAState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificationPath()
	 */
	@Override
	public XOX_QualifiedCertificate getCertificationPath() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getDEREncoded()
	 */
	@Override
	public byte[] getDEREncoded() {
		if(m_aX509 != null)
			return m_aX509.getDEREncoded();
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#setDEREncoded(byte[])
	 * 
	 * When this method is called, the DER image passed will be used as the new certificate representation
	 * and the certificate extensions will be evaluated again. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setDEREncoded(byte[] _DEREncoded) {
		//
		m_aX509 = null; //remove old certificate
						//remove old data from HashMaps
		m_aExtensions.clear();
		m_aExtensionLocalizedNames.clear();
		m_aExtensionDisplayValues.clear();
		m_aCriticalExtensions.clear();
		m_aNotCriticalExtensions.clear();

		ByteArrayInputStream as = new ByteArrayInputStream(_DEREncoded); 
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado;
		try {
			ado = aderin.readObject();
			m_aX509 = new X509CertificateStructure((ASN1Sequence) ado);
//initializes the certificate display information
			initSubjectName();
			m_sVersion = String.format("V%d", m_aX509.getVersion());
			m_sSerialNumber = new String(""+m_aX509.getSerialNumber().getValue());
			initIssuerName();
			m_sNotValidBefore = initCertDate(m_aX509.getStartDate().getDate());
			m_sNotValidAfter =  initCertDate(m_aX509.getEndDate().getDate());
			m_sSubjectPublicKeyAlgorithm = initPublicKeyAlgorithm();
			m_sSubjectPublicKeyValue = initPublicKeyData();
			m_sSignatureAlgorithm = initSignatureAlgorithm();
			initThumbPrints();
			//now initializes the Extension listing			
			X509Extensions aX509Exts = m_aX509.getTBSCertificate().getExtensions();
			//fill the internal extension HashMaps
			//at the same time we'll get the extension localized name from resources and
			//fill the display data
			MessageConfigurationAccess m_aRegAcc = null;
			m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);
//FIXME: may be we need to adapt this to the context: the following is valid ONLY if this
			//object is instantiated from within a dialog, is not true if instantiated from a not UI method (e.g. from basic for example).
			IDynamicLogger aDlgH = null;
			if(m_bIsFromUI)
				aDlgH = new DynamicLoggerDialog(this,m_xContext);
			else
				aDlgH = new DynamicLogger(this,m_xContext);				
			aDlgH.enableLogging();
			CertificateExtensionDisplayHelper aHelper = new CertificateExtensionDisplayHelper(m_xContext,m_bDisplayOID, aDlgH);

			for(Enumeration<DERObjectIdentifier> enume = aX509Exts.oids(); enume.hasMoreElements();) {
				DERObjectIdentifier aDERId = enume.nextElement();
				String aTheOID = aDERId.getId();
				X509Extension aext = aX509Exts.getExtension(aDERId);
				m_aExtensions.put(aTheOID, aext);
				//now grab the localized description
				try {
					m_aExtensionLocalizedNames.put(aTheOID, m_aRegAcc.getStringFromRegistry( aTheOID )+
							((m_bDisplayOID) ? (" (OID: "+aTheOID.toString()+")" ): ""));
				} catch (com.sun.star.uno.Exception e) {
					m_aLogger.severe("setDEREncoded", e);
					m_aExtensionLocalizedNames.put(aTheOID, aTheOID);
				}
				//and decode this extension
				m_aExtensionDisplayValues.put(aTheOID, aHelper.examineExtension(aext, aDERId));

				if(aext.isCritical())
					m_aCriticalExtensions.put(aTheOID, aext);
				else
					m_aNotCriticalExtensions.put(aTheOID, aext);					
			}
			m_aRegAcc.dispose();
//now check the certificate for the compliance
			verifyCertificatCompliance();
		} catch (IOException e) {
			m_aLogger.severe("setDEREncoded", e);
		}
	}

	
	
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateComplianceControl()
	 */
	@Override
	public XOX_CertificateComplianceControlProcedure getCertificateComplianceControl() {
		return m_xoxCertificateComplianceControlProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#setCertificateComplianceControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure)
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
	
	////////////////// internal functions
	/**
	 * 
	 */
	
	protected void verifyCertificatCompliance() {
		if(m_xoxCertificateComplianceControlProcedure != null) {
			XComponent xCtl = (XComponent)UnoRuntime.queryInterface(XComponent.class, this);
			if(xCtl != null) {
				try {
					//FIXME
					//add the result to the certificate status
					m_xoxCertificateComplianceControlProcedure.verifyCertificateCertificateCompliance(xCtl);
					m_aLogger.log("State: "+
							Helpers.mapCertificateStateToValue(m_xoxCertificateComplianceControlProcedure.getCertificateState()));
					m_nCertificateState = 
							Helpers.mapCertificateStateToValue(m_xoxCertificateComplianceControlProcedure.getCertificateState());
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				} catch (Exception e) {
					m_aLogger.severe(e);
				}
			}
		}
	}
	
	protected void initThumbPrints() {
		//obtain a byte block of the entire certificate data
		ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
		DEROutputStream         dOut = new DEROutputStream(bOut);
		try {
			dOut.writeObject(m_aX509);
			byte[] certBlock = bOut.toByteArray();

			//now compute the certificate SHA1 & MD5 digest
			SHA1Digest digsha1 = new SHA1Digest();
			digsha1.update(certBlock, 0, certBlock.length);
			byte[] hashsha1 = new byte[digsha1.getDigestSize()];
			digsha1.doFinal(hashsha1, 0);
			m_sSHA1Thumbprint = Helpers.printHexBytes(hashsha1);
			MD5Digest  digmd5 = new MD5Digest();
			digmd5.update(certBlock, 0, certBlock.length);
			byte[] hashmd5 = new byte[digmd5.getDigestSize()];
			digmd5.doFinal(hashmd5, 0);
			m_sMD5Thumbprint = Helpers.printHexBytes(hashmd5);
		} catch (IOException e) {
			m_aLogger.severe("initThumbPrints", e);
		}		
	}

	protected String initSignatureAlgorithm() {
		DERObjectIdentifier oi = m_aX509.getSignatureAlgorithm().getObjectId();
		return new String(""+(
				(oi.equals(X509CertificateStructure.sha1WithRSAEncryption)) ? 
				"pkcs-1 sha1WithRSAEncryption" : oi.toString())
				);
	}

	protected String initPublicKeyData() {
		byte[] sbjkd = m_aX509.getSubjectPublicKeyInfo().getPublicKeyData().getBytes();
		return Helpers.printHexBytes(sbjkd);
	}

	protected String initPublicKeyAlgorithm() {
//		AlgorithmIdentifier aid = m_aX509.getSignatureAlgorithm();
		DERObjectIdentifier oi = m_aX509.getSubjectPublicKeyInfo().getAlgorithmId().getObjectId();
		return new String(""+(
				(oi.equals(X509CertificateStructure.rsaEncryption)) ?
					"pkcs-1 rsaEncryption" : oi.getId())
					);
	}

	protected String initCertDate(Date _aTime) {
		//force UTC time
		TimeZone gmt = TimeZone.getTimeZone("UTC");
		GregorianCalendar calendar = new GregorianCalendar(gmt,m_lTheLocale);
		calendar.setTime(_aTime);	
//string with time only
//the locale should be the one of the extension not the Java one.
		String time = String.format(m_lTheLocale,m_sTimeLocaleString, calendar);
		return time;
	}	

	@SuppressWarnings("unchecked")
	protected void initSubjectName() {
		m_sSubjectName = "";
		//print the subject
		//order of printing is as got in the CNIPA spec
		//first, grab the OID in the subject name
		X509Name aName = m_aX509.getSubject();
		Vector<DERObjectIdentifier> oidv =  aName.getOIDs();
		Vector<?> values = aName.getValues();
		HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
		for(int i=0; i< oidv.size(); i++) {
			m_sSubjectName = m_sSubjectName + X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
					((m_bDisplayOID) ? (" (OID: "+oidv.elementAt(i).toString()+")" ): "") +" \n";
			hm.put(oidv.elementAt(i), values.elementAt(i).toString());
		}
		//extract data from subject name following CNIPA recommendation
		/*
		 * first lookup for givenname and surname, if not existent
		 * lookup for commonName (cn), if not existent
		 * lookup for pseudonym ()
		 */

		//look for givename (=nome di battesimo)
			m_sSubjectDisplayName = "";			
			//see BC source code for details about DefaultLookUp behaviour
			DERObjectIdentifier oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("givenname")); 
			if(hm.containsKey(oix)) {
				String tmpName = hm.get(oix).toString();
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("surname"));
				if(hm.containsKey(oix))
					m_sSubjectDisplayName = tmpName +" "+hm.get(oix).toString();
			}
			if(m_sSubjectDisplayName.length() == 0) {
				//check for CN
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
				if(hm.containsKey(oix)) {
					m_sSubjectDisplayName = hm.get(oix).toString();
				}
			}
			if(m_sSubjectDisplayName.length() == 0) {
				//if still not, check for pseudodym
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
				if(hm.containsKey(oix))
					m_sSubjectDisplayName = hm.get(oix).toString();						
			}
			if(m_sSubjectDisplayName.length() == 0)
				m_sSubjectDisplayName = m_sSubjectName;
	}

	@SuppressWarnings("unchecked")
	protected void initIssuerName() {
		m_sIssuerName = "";
		X509Name aName = m_aX509.getIssuer();
		Vector<DERObjectIdentifier> oidv =  aName.getOIDs();
		HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
		Vector<?> values = aName.getValues();
		for(int i=0; i< oidv.size(); i++) {
			m_sIssuerName = m_sIssuerName + X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
			((m_bDisplayOID) ? (" (OID: "+oidv.elementAt(i).toString()+")" ): "") +" \n";
			hm.put(oidv.elementAt(i), values.elementAt(i).toString());
		}		
		//look for givename (=nome di battesimo)
		m_sIssuerDisplayName = "";			
		//see BC source code for details about DefaultLookUp behaviour
		DERObjectIdentifier oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("givenname")); 
		if(hm.containsKey(oix)) {
			String tmpName = hm.get(oix).toString();
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("surname"));
			if(hm.containsKey(oix))
				m_sIssuerDisplayName = tmpName +" "+hm.get(oix).toString();
		}
		if(m_sIssuerDisplayName.length() == 0) {
			//check for CN
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
			if(hm.containsKey(oix)) {
				m_sIssuerDisplayName = hm.get(oix).toString();
			}
		}
		if(m_sIssuerDisplayName.length() == 0) {
			//if still not, check for pseudodym
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
			if(hm.containsKey(oix))
				m_sIssuerDisplayName = hm.get(oix).toString();						
		}
		if(m_sIssuerDisplayName.length() == 0)
			m_sIssuerDisplayName = m_sIssuerName;
	}
	
	//////////////////////////////////////////////////////////////////
	///////////////// area for extension display management

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateElementErrorState(java.lang.String)
	 */
	@Override
	public int getCertificateElementErrorState(String _oid) {
		Integer aInt = m_aElementStates.get(_oid);
		if(aInt != null)
			return aInt.intValue();
		return CertificateElementState.OK_value;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#setCertificateExtensionErrorState(java.lang.String, int)
	 */
	@Override
	public void setCertificateElementErrorState(String _oid, int arg1) {
		m_aElementStates.put(_oid, new Integer(arg1));		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateExtensionName(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionName(String _oid) {
		return m_aExtensionLocalizedNames.get(_oid);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateExtensionStringValue(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionStringValue(String _oid) {
		return m_aExtensionDisplayValues.get(_oid);
	}
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateExtensionOIDs()
	 */
	@Override
	public String[] getCertificateExtensionOIDs() {
		if(m_aExtensions.isEmpty())
			return null;
		Set<String> aTheOIDs = m_aExtensions.keySet();
		String[] ret = new String[aTheOIDs.size()]; 
		return aTheOIDs.toArray(ret);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCriticalCertificateExtensionOIDs()
	 */
	@Override
	public String[] getCriticalCertificateExtensionOIDs() {
		if(m_aCriticalExtensions.isEmpty())
			return null;
		Set<String> aTheOIDs = m_aCriticalExtensions.keySet();
		String[] ret = new String[aTheOIDs.size()]; 
		return aTheOIDs.toArray(ret);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getNotCriticalCertificateExtensionOIDs()
	 */
	@Override
	public String[] getNotCriticalCertificateExtensionOIDs() {
		// TODO Auto-generated method stub
		if(m_aNotCriticalExtensions.isEmpty())
			return null;
		Set<String> aTheOIDs = m_aNotCriticalExtensions.keySet();
		String[] ret = new String[aTheOIDs.size()]; 
		return aTheOIDs.toArray(ret);
	}

	private XOX_CertificateExtension[] getExtensionsHelper(String[] critOIDs, boolean _bIsCritical) {
		XOX_CertificateExtension[] retValue = new XOX_CertificateExtension[critOIDs.length];
//		X509Extensions aExts = m_aX509.getTBSCertificate().getExtensions();
		//fill the retValue
		for(int i=0;i< critOIDs.length;i++) {
			Object[] aArguments = new Object[4];
			aArguments[0] = new String(critOIDs[i]);//aExts.getExtension(new DERObjectIdentifier(critOIDs[i])).getValue().getOctets();
			aArguments[1] = new String(getCertificateExtensionName(critOIDs[i]));
			aArguments[2] = new String(getCertificateExtensionStringValue(critOIDs[i]));
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCriticalExtensions()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getNotCriticalExtensions()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateCertificationPathControl()
	 */
	@Override
	public XOX_CertificationPathControlProcedure getCertificateCertificationPathControl() {
		// TODO Auto-generated method stub
		return m_xoxCertificationPathControlProcedure;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#setCertificationPathControlObject(it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure)
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
}
