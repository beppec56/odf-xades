/*************************************************************************
 *
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 * 
 *  Part of the code is adapted from j4sign, hence part of
 *  the copyright is:
 *	j4sign - an open, multi-platform digital signature solution
 *	Copyright (c) 2004 Roberto Resoli - Servizio Sistema Informativo - Comune di Trento.
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

package it.plio.ext.oxsit.comp.security.ca;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.CertificationAuthorityState;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;
import it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;
import it.plio.ext.oxsit.security.crl.CertificationAuthorities;
import it.plio.ext.oxsit.security.crl.RootsVerifier;
import it.plio.ext.oxsit.security.crl.X509CertRL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.cms.CMSException;

import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 *  This service implements the CertificationPathCache_IT service, used to check the
 *  certificate for compliance on Italian law.
 *  
 *  This is basically a memory cache that remains active for the entire OOo session.
 *  It holds the CA root list, and the dowloaded CRLs.
 *  
 *  This object is made persistent through the use of the singleton service
 *  it.plio.ext.oxsit.singleton.SingleGlobalVariables
 *  It is saved and can be retrieved through a name
 *  
 *  
 * @author beppec56
 *
 */
public class CertificationPathCache_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_CertificationPathControlProcedure,
			XOX_CertificateRevocationStateControlProcedure
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= CertificationPathCache_IT.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT };

	private XComponentContext			m_xCC;
	private	XMultiComponentFactory		m_xMCF;
	protected IDynamicLogger m_aLogger;

	protected XOX_X509Certificate m_xQc;

	private CertificateState m_aCertificateState;
	private CertificateStateConditions	m_aCertificateStateConditions;
    
    private 	RootsVerifier	m_aRootVerifier;
    
    private		CertificationAuthorities	m_aCADbData;

	private String m_bUseGUI;

	private X509CertRL 					CRL;

	private XComponent[] 				m_aCAList;


	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public CertificationPathCache_IT(XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = m_xCC.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
		m_aLogger.enableLogging();
    	m_aLogger.ctor();
    	m_aCertificateState = CertificateState.NOT_YET_VERIFIED;
    	m_aCertificateStateConditions = CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED;
	}

	@Override
	public String getImplementationName() {
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
	 *				"it.plio.ext.oxsit.security.cert.CertificateExtension", aArguments, m_xContext);
	 *
	 * @param _eValue array of ? object:
	 * <p>_eValue[0] string ??</p>
	 * 
	 * (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 *  
	 */
	@Override
	public void initialize(Object[] _eValue) throws Exception {
		//the eValue is the byte stream of the extension
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
		m_aLogger.entering("dispose");
//		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#configureOptions(com.sun.star.frame.XFrame, com.sun.star.uno.XComponentContext)
	 */
	@Override
	public void configureOptions(XFrame arg0, XComponentContext arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#getCertificationAuthorityState()
	 */
	@Override
	public CertificationAuthorityState getCertificationAuthorityState() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#verifyCertificationPath(com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificationAuthorityState verifyCertificationPath(XFrame _aFrame, XComponent arg0)
			throws IllegalArgumentException, Exception {
		m_aLogger.log("verifyCertificationPath");
//check for certificate
		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, arg0);
		if(m_xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance wrong argument"));
		
		initializeCADataBase(_aFrame);
		isPathValid();
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#initializeProcedure(com.sun.star.frame.XFrame)
	 */
	@Override
	public void initializeProcedure(XFrame arg0) {
		initializeCADataBase(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#initialize(boolean)
	 */
	private void initializeCADataBase(XFrame _aFrame) {
        XStatusIndicator xStatusIndicator = null;
        if (_aFrame != null) {
        	//check interface
        	//
        	XStatusIndicatorFactory xFact = (XStatusIndicatorFactory)UnoRuntime.queryInterface(XStatusIndicatorFactory.class,_aFrame);
        	if(xFact != null) {
        		xStatusIndicator = xFact.createStatusIndicator();
        		if(xStatusIndicator != null)
        			xStatusIndicator.start("", 100); //meaning 100%
        	}
        }

		if(xStatusIndicator != null)
			xStatusIndicator.setText("Verify and confirm root signature");
		//here:
		//init the root authority elements,
		if( m_aRootVerifier == null)
			m_aRootVerifier = new RootsVerifier(_aFrame,m_xCC);
/* the following needs implementation, do we need it?
 * 		else
			if(m_aRootVerifier.getUserApprovedFingerprint() == null)
*/		
		if(xStatusIndicator != null) {
			xStatusIndicator.setText("Read CA root Data base");
			xStatusIndicator.setValue(5);
		}
		//fill the list of certificate authorities available
		if(m_aCADbData == null) {
//prepare file path
			URL aURL;
			try {
				aURL = new URL(
						Helpers.getExtensionInstallationPath(m_xCC)+
						System.getProperty("file.separator") + 
						"ca-list-signed-p7m-it"+ //fixed path, the directory containing the current root zip file
						System.getProperty("file.separator")+
						"LISTACER_20090303.zip.p7m"
						);
				m_aCADbData = new CertificationAuthorities(xStatusIndicator,m_xCC, aURL, false);
			} catch (MalformedURLException e) {
				m_aLogger.severe(e);
			} catch (GeneralSecurityException e) {
				m_aLogger.severe(e);
			} catch (IOException e) {
				m_aLogger.severe(e);
			} catch (CMSException e) {
				m_aLogger.severe(e);
			}
		}
		if(xStatusIndicator != null)
			xStatusIndicator.end();
	}

	//FIXME: a big one, needs to set state for certificate in graphic...
	//FIXME: another one, see behavior of this with a longer certification path
	//FIXME: check with cert path problem
	private boolean isPathValid() {
		//convert the certificate to java internal representation
        java.security.cert.CertificateFactory cf;
		try {
			cf = java.security.cert.CertificateFactory.getInstance("X.509");
			java.io.ByteArrayInputStream bais = null;
            bais = new java.io.ByteArrayInputStream(m_xQc.getDEREncoded());
            X509Certificate certChild = (java.security.cert.X509Certificate) cf.generateCertificate(bais);
            XOX_X509Certificate qCertChild = m_xQc;

//now loop, and add the certificate path to the current path, actually empty            
            X509Certificate certParent = null;
            boolean isPathValid = false;
			while (!certChild.getIssuerDN().equals(
                    certChild.
                    getSubjectDN())) {
                //until CA is self signed
	            boolean isInCA = false;

                try {
                    certParent = m_aCADbData.getCACertificate(certChild.getIssuerX500Principal());
//                    certParent = m_aCADbData.getIssuerCertificate(certChild);
                    isInCA = true;
                } catch (GeneralSecurityException ex) {
                    //la CA non � presente nella root
                	//set 'CA unknown to Italian PA'
                	//set the current XOX_X509Certificate state as well
                	//this can be an intermediate certificate, it's the last one that should be ok
                	//we need to set the certificate path of the current
                	//main XOX_X509Certificate as invalid for italian signature
                	m_xQc.setCertificateElementErrorState(
        					GlobConstant.m_sX509_CERTIFICATE_CERTPATH,
        					CertificateElementState.INVALID_value);			
                	
                	//set the CA state of m_xQc as not credited to Italian CNIPA structure

                	return isPathValid;
                }
                certChild = certParent;
//instantiate a qualified certificate to represent the parent,
                certParent.getEncoded();
				//all seems right, instantiate the certificate service
				//now create the Certificate Control UNO objects
				//first the certificate compliance control
				//FIXME, may be we can change this to a better behavior, moving the tests currently carried out in
                //CertificationAuthorities to here
				//Object oACCObj = m_xMCF.createInstanceWithContext(GlobConstant.m_sCERTIFICATE_COMPLIANCE_SERVICE_IT, m_xCC);
				//Object oCertPath = m_xMCF.createInstanceWithContext(GlobConstant.m_sCERTIFICATION_PATH_SERVICE_IT, m_xCC);

				//now the certification path control

				//prepare objects for subordinate service
				Object[] aArguments = new Object[3];
//												byte[] aCert = cert.getEncoded();
				Object oCertDisp = m_xMCF.createInstanceWithContext(GlobConstant.m_sX509_CERTIFICATE_DISPLAY_SERVICE_CA_IT, m_xCC);

				//set the certificate raw value
				aArguments[0] = certParent.getEncoded();//aCert;
				aArguments[1] = new Boolean(m_bUseGUI);//FIXME change according to UI (true) or not UI (false)
				aArguments[2] = oCertDisp; //the compliance checker object, which implements the needed interface
//				aArguments[2] = oACCObj; //the compliance checker object, which implements the needed interface
//				aArguments[3] = oCertPath;

				Object oACertificate = m_xMCF.createInstanceWithArgumentsAndContext(GlobConstant.m_sX509_CERTIFICATE_SERVICE,
						aArguments, m_xCC);
				//get the main interface
				XOX_X509Certificate xQualCert = 
					(XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, oACertificate);

//set it to the current child certificate, and put it as a new qualified certificate
//set the status flags of the new certificate as correct for a CA certificate
                qCertChild.setCertificationPath(xQualCert);
                qCertChild = xQualCert;
                m_aLogger.info("added a certificate");
            }
            ;
            return isPathValid;
		} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure#getCertificateState()
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#getCertificateState()
	 */
	@Override
	public CertificateState getCertificateState() {
		return m_aCertificateState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure#getCertificateStateConditions()
	 */
	@Override
	public CertificateStateConditions getCertificateStateConditions() {
		// TODO Auto-generated method stub
		return m_aCertificateStateConditions;
	}

	////////////////////// verify revocation state functions
	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateRevocationStateControlProcedure#verifyCertificateRevocationState(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificateState verifyCertificateRevocationState(XFrame _aFrame,
			XComponent arg1) throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		m_aLogger.log("verifyCertificateRevocationState");
    	m_aCertificateState = CertificateState.NOT_YET_VERIFIED;
    	m_aCertificateStateConditions = CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED;
		
		//check for certificate
		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, arg1);
		if(m_xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateRevocationStateControlProcedure#verifyCertificateRevocationState wrong argument"));

		initializeCADataBase(_aFrame);

		if(CRL == null) {
	        CRL = new X509CertRL(_aFrame,m_xCC,m_aCADbData);
		}
		//now check the revocation state using the crl
		//		m_aCertificateState = CertificateState.OK;
		//convert the certificate to java internal representation
	    java.security.cert.X509Certificate m_JavaCert = null;

        java.security.cert.CertificateFactory cf;
		try {
			cf = java.security.cert.CertificateFactory.getInstance("X.509");
			java.io.ByteArrayInputStream bais = null;
            bais = new java.io.ByteArrayInputStream(m_xQc.getDEREncoded());
            m_JavaCert = (java.security.cert.X509Certificate) cf.generateCertificate(bais);
            XStatusIndicator xStatusIndicator = null;
            if (_aFrame != null) {
            	//check interface
            	//
            	XStatusIndicatorFactory xFact = (XStatusIndicatorFactory)UnoRuntime.queryInterface(XStatusIndicatorFactory.class,_aFrame);
            	if(xFact != null) {
            		xStatusIndicator = xFact.createStatusIndicator();
            		if(xStatusIndicator != null)
            			xStatusIndicator.start("Control CRL", 100);
            	}
            }
            
            CRL.isNotRevoked(xStatusIndicator,m_JavaCert);
            xStatusIndicator.end();

    		//grab certificate state and conditions
    		m_aCertificateState = CRL.getCertificateState();
    		m_aCertificateStateConditions = CRL.getCertificateStateConditions();
		} catch (CertificateException e) {
			m_aLogger.severe(e);
			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
			throw (new com.sun.star.uno.Exception(" wrapped exception: "));
		}
		return m_aCertificateState;
	}
	
	/**
	 * check for priority of certificate state and set it accordingly
	 * @param _newState
	 */
	private void setCertificateStateHelper(CertificateState _newState) {
		if(_newState.getValue() > m_aCertificateState.getValue())
			m_aCertificateState = _newState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#getCertificationAuthorities()
	 */
	@Override
	public XComponent[] getCertificationAuthorities(XFrame _aFrame) {
		initializeCADataBase(_aFrame);
		if(m_aCADbData != null) {
			if(m_aCAList != null)
				return m_aCAList;

			int numCA = m_aCADbData.getCANumber();
			if(numCA > 0) {
				m_aCAList = new XComponent[numCA];

				Collection<X509Certificate> c = m_aCADbData.getCA();
				Iterator<X509Certificate> i = c.iterator();
				int idx = 0;
				while(i.hasNext()) {
					X509Certificate cert = i.next();
					//prepare objects for subordinate service
	//				byte[] aCert = cert.getEncoded();
					//set the certificate raw value
					try {
						Object[] aArguments = new Object[3];
						//this are supposed to be CA, so we'll add the 
						//ca test & display functions known by this component
						//note that this component is localized, no need for it to be adapted
						Object oCertDisp = m_xMCF.createInstanceWithContext(GlobConstant.m_sX509_CERTIFICATE_DISPLAY_SERVICE_CA_IT, m_xCC);

						aArguments[0] = cert.getEncoded();
						aArguments[1] = new Boolean(false);//FIXME change according to UI (true) or not UI (false)
						aArguments[2] = oCertDisp; //the compliance checker object, which implements the needed interface
	/*				aArguments[3] = oCertPath;
					aArguments[4] = oCertRev;*/

						Object oACertificate = m_xMCF.createInstanceWithArgumentsAndContext(GlobConstant.m_sX509_CERTIFICATE_SERVICE,
								aArguments, m_xCC);
						//get the main interface
						XOX_X509Certificate xQualCert = 
							(XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, oACertificate);
						if(xQualCert != null) {
							XComponent com = 
								(XComponent)UnoRuntime.queryInterface(XComponent.class, xQualCert);
							m_aCAList[idx++] = com;
						}
					} catch (CertificateEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//aCert;				
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return m_aCAList;
			}
		}
		return null;
	}
}