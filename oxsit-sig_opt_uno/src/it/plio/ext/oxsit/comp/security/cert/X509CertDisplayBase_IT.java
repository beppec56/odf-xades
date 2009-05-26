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
import it.plio.ext.oxsit.security.cert.CertificationAuthorityState;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;
import it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;
import it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

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

import com.sun.star.frame.XFrame;
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
public abstract class X509CertDisplayBase_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XInitialization,
			XOX_X509CertificateDisplay
			 {

	private XComponentContext m_xContext;
	private XMultiComponentFactory m_xMCF;

	protected String m_sTimeLocaleString = "id_validity_time_locale";//"%1$td %1$tB %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)";
	protected static String m_sTimeLocaleStringValue;
	protected String m_sLocaleLanguage = "id_iso_lang_code"; //"it";
	protected static String m_sLocaleLanguageValue; //"it";

	protected IDynamicLogger m_aLogger;

//	private int m_nCertificateState;
//	private int m_nCertificateStateConditions;
	
	protected boolean	m_bDisplayOID;

	//the certificate representation
	protected X509CertificateStructure m_aX509;

	protected String m_sSubjectDisplayName;	
	protected String m_sSubjectName = "";

	private String m_sVersion = "";

	private String m_sSerialNumber = "";

	protected String m_sIssuerDisplayName = "";
	protected String m_sIssuerName = "";

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

	private XOX_X509Certificate m_xQc;

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public X509CertDisplayBase_IT(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
//
		m_aLogger.enableLogging();
    	m_aLogger.ctor();
    	m_xContext = _ctx;
    	m_xMCF = m_xContext.getServiceManager();
    	m_bDisplayOID = false;
//    	m_bIsFromUI = false;
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

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * _arg[0] 
	 */
	@Override
	public void initialize(Object[] _arg) throws Exception {
	}
	
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectDisplayName()
	 */
	@Override
	public String getSubjectDisplayName() {
		return m_sSubjectDisplayName;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getVersion()
	 */
	@Override
	public String getVersion() {
		return m_sVersion;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotValidAfter()
	 */
	@Override
	public String getNotValidAfter() {
		return m_sNotValidAfter;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotValidBefore()
	 */
	@Override
	public String getNotValidBefore() {
		return m_sNotValidBefore;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getIssuerDisplayName()
	 */
	@Override
	public String getIssuerDisplayName() {
		return m_sIssuerDisplayName;
	}

	/* (non-Javadoc)

	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getIssuerName()
	 */
	@Override
	public String getIssuerName() {
		return m_sIssuerName;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getMD5Thumbprint()
	 */
	@Override
	public String getMD5Thumbprint() {
		return m_sMD5Thumbprint;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSHA1Thumbprint()
	 */
	@Override
	public String getSHA1Thumbprint() {
		return m_sSHA1Thumbprint;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSerialNumber()
	 */
	@Override
	public String getSerialNumber() {
		return m_sSerialNumber;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSignatureAlgorithm()
	 */
	@Override
	public String getSignatureAlgorithm() {
		return m_sSignatureAlgorithm;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectName()
	 */
	@Override
	public String getSubjectName() {
		return m_sSubjectName;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectPublicKeyAlgorithm()
	 */
	@Override
	public String getSubjectPublicKeyAlgorithm() {
		return m_sSubjectPublicKeyAlgorithm;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getSubjectPublicKeyValue()
	 */
	@Override
	public String getSubjectPublicKeyValue() {
		return m_sSubjectPublicKeyValue;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#setDEREncoded(byte[])
	 * 
	 * When this method is called, the DER image passed will be used as the new certificate representation
	 * and the certificate extensions will be evaluated again. 
	 */
//	@Override
	private void setDEREncoded(byte[] _DEREncoded) {
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
			CertificateExtensionDisplayHelper aHelper = new CertificateExtensionDisplayHelper(m_xContext,m_bDisplayOID, m_aLogger);

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
		} catch (IOException e) {
			m_aLogger.severe("setDEREncoded", e);
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

	abstract void initSubjectName();

	abstract void initIssuerName();
	
	//////////////////////////////////////////////////////////////////
	///////////////// area for extension display management

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateExtensionStringValue(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionStringValue(String _oid) {
		return m_aExtensionDisplayValues.get(_oid);
	}
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCertificateExtensionOIDs()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCriticalCertificateExtensionOIDs()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotCriticalCertificateExtensionOIDs()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getCriticalExtensions()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509Certificate#getNotCriticalExtensions()
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateExtensionName(java.lang.String)
	 */
	@Override
	public String getCertificateExtensionName(String _oid) {
		// TODO Auto-generated method stub
		return m_aExtensionLocalizedNames.get(_oid);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#prepareDisplayStrings(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void prepareDisplayStrings(XFrame _xFrame, XComponent _xComp)
			throws IllegalArgumentException, Exception {
		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, _xComp);
		if(m_xQc == null)
			throw (new IllegalArgumentException("it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay#prepareDisplayStrings wrong argument"));

		//
		m_aX509 = null; //remove old certificate
						//remove old data from HashMaps
		m_aExtensions.clear();
		m_aExtensionLocalizedNames.clear();
		m_aExtensionDisplayValues.clear();
		m_aCriticalExtensions.clear();
		m_aNotCriticalExtensions.clear();

		ByteArrayInputStream as = new ByteArrayInputStream(m_xQc.getDEREncoded()); 
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
			CertificateExtensionDisplayHelper aHelper = new CertificateExtensionDisplayHelper(m_xContext,m_bDisplayOID, m_aLogger);

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
		} catch (IOException e) {
			m_aLogger.severe("setDEREncoded", e);
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
}