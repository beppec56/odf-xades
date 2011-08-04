/****** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/SSCDevice_IT.java.
 *
 * The Initial Developer of the Original Code is
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK *********************************************/

package com.yacme.ext.oxsit.cust_it.comp.security;

import com.yacme.ext.oxsit.security.XOX_SSCDevice;
import com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

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
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.options.OptionsParametersAccess;

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
	private String m_sTokenLabel;
	private String m_sTokenManufID;
	private int m_nTokenMaxPinL;
	private int m_nTokenMinPinL;
	private String m_sTokenSerialNumber;

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
		m_aLogger.debug("getSupportedServiceNames");
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

		m_aLogger.debug("supportsService", _sService);
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
		// FIXME need to check if this element is referenced somewhere before deallocating it		m_aLogger.entering("dispose");
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
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#addCertificate(com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes)
	 */
	@Override
	public void addCertificate(XOX_CertificatePKCS11Attributes aCertificateAttributes) {
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
			aArguments[0] = aCertificateAttributes.getDEREncoded();//_aDERencoded;// aCert;
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
			
			xQualCert.setCertificateAttributes(aCertificateAttributes);

			m_xQualCertList.add(xQualCert);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getQualifiedCertificates()
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
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getHasCertificates()
	 */
	@Override
	public int getHasCertificates() {
		return m_xQualCertList.size();
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getATRcode()
	 */
	@Override
	public String getATRcode() {
		return m_sATRCode;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setATRcode(java.lang.String)
	 */
	@Override
	public void setATRcode(String _sArg) {
		m_sATRCode = _sArg;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getCryptoLibraryUsed()
	 */
	@Override
	public String getCryptoLibraryUsed() {
		return m_sCryptoLibraryUsed;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setCryptoLibraryUsed(java.lang.String)
	 */
	@Override
	public void setCryptoLibraryUsed(String _sArg) {
		m_sCryptoLibraryUsed = _sArg;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getCryptoLibrariesConfigured()
	 */
	@Override
	public String getCryptoLibrariesConfigured() {
		return m_sCryptoLibrariesConfigured;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setCryptoLibrariesConfigured(java.lang.String)
	 */
	@Override
	public void setCryptoLibrariesConfigured(String arg0) {
		m_sCryptoLibrariesConfigured = arg0;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getDescription()
	 */
	@Override
	public String getDescription() {
		return m_sDescription;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String _sArg) {
		m_sDescription = _sArg;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getManufacturer()
	 */
	@Override
	public String getManufacturer() {
		return m_sManufacturer;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setManufacturer(java.lang.String)
	 */
	@Override
	public void setManufacturer(String _sArg) {
		m_sManufacturer = _sArg;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getLabel()
	 */
	@Override
	public String getTokenLabel() {
		return m_sTokenLabel;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getManufacturerID()
	 */
	@Override
	public String getTokenManufacturerID() {
		return m_sTokenManufID;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getMaximumPINLenght()
	 */
	@Override
	public int getTokenMaximumPINLenght() {
		return m_nTokenMaxPinL;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getMinimumPINLenght()
	 */
	@Override
	public int getTokenMinimumPINLenght() {
		return m_nTokenMinPinL;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#getSerialNumber()
	 */
	@Override
	public String getTokenSerialNumber() {
		return m_sTokenSerialNumber;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setLabel(java.lang.String)
	 */
	@Override
	public void setTokenLabel(String arg0) {
		m_sTokenLabel = arg0;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setManufacturerID(java.lang.String)
	 */
	@Override
	public void setTokenManufacturerID(String arg0) {
		m_sTokenManufID = arg0;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setMaximumPINLenght(int)
	 */
	@Override
	public void setTokenMaximumPINLenght(int arg0) {
		m_nTokenMaxPinL = arg0;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setMinimumPINLenght(int)
	 */
	@Override
	public void setTokenMinimumPINLenght(int arg0) {
		m_nTokenMinPinL = arg0;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDevice#setSerialNumber(java.lang.String)
	 */
	@Override
	public void setTokenSerialNumber(String arg0) {
		m_sTokenSerialNumber = arg0;
	}
}
