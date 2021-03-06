/* ***** BEGIN LICENSE BLOCK ********************************************
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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/cert/CertificateComplianceCA_IT.java.
 *
 * The Initial Developers of the Original Code are
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * Roberto Resoli - Servizio Sistema Informativo - Comune di Trento.
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 * Part of the code is adapted from j4sign, hence part of
 * the copyright is:
 * j4sign - an open, multi-platform digital signature solution
 * Copyright (c) 2004 Roberto Resoli - Servizio Sistema Informativo - Comune di Trento.
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
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.cust_it.comp.security.cert;

import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
import com.yacme.ext.oxsit.security.cert.CertificateElementID;
import com.yacme.ext.oxsit.security.cert.CertificateElementState;
import com.yacme.ext.oxsit.security.cert.CertificateState;
import com.yacme.ext.oxsit.security.cert.CertificateStateConditions;
import com.yacme.ext.oxsit.security.cert.CertificationAuthorityState;
import com.yacme.ext.oxsit.security.cert.XOX_CertificateComplianceProcedure;
import com.yacme.ext.oxsit.security.cert.XOX_CertificateRevocationStateProcedure;
import com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Set;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.qualified.QCStatement;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.cust_it.comp.security.ca.CertificationPath_IT;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 *  This service implements the CertificationPath_IT service, used to check the
 *  certificate for compliance on Italian law.
 *  
 *  The conformance will be checked for the certificate dates, certificate configuration
 *  and for extension  that are mandatory according to the following criteria/Norms,
 *  listed in ascending order, the precedence order is from top to below:
 *  - Deliberazione CNIPA del 17 febbraio 2005 n 4
 *  - ETSI TS 102 280 V1.1.1
 *  - ETSI TS 101 862 V1.3.2
 *  
 * @author beppec56
 *
 */
public class CertificateComplianceCA_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_CertificateComplianceProcedure,
			XOX_CertificationPathProcedure,
			XOX_CertificateRevocationStateProcedure
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= CertificateComplianceCA_IT.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { ConstantCustomIT.m_sCERTIFICATE_COMPLIANCE_SERVICE_CA_IT };

	protected DynamicLogger m_aLogger;

	protected XOX_X509Certificate m_xQc;

	private CertificateState m_aCertificateState;
	private CertificateStateConditions m_aCertStateConds;
	private	CertificationAuthorityState m_aCAState;
    private java.security.cert.X509Certificate m_JavaCert = null;
    private XComponentContext m_xCC;
    private XMultiComponentFactory	m_xMCF;
    private XFrame	m_xFrame;
	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public CertificateComplianceCA_IT(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
//		m_aLogger.enableLogging();
		m_xCC = _ctx;
		m_xMCF = m_xCC.getServiceManager();
    	m_aLogger.ctor();
    	m_aCAState = CertificationAuthorityState.NOT_YET_CHECKED;
    	m_aCertStateConds = CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED;
    	m_aCertificateState = CertificateState.NOT_YET_VERIFIED;
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
	 *				"com.yacme.ext.oxsit.security.cert.CertificateExtension", aArguments, m_xContext);
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
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#initializeProcedure(com.sun.star.frame.XFrame)
	 */
	@Override
	public void initializeProcedure(XFrame arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#configureOptions(com.sun.star.frame.XFrame, com.sun.star.uno.XComponentContext)
	 */
	@Override
	public void configureOptions(XFrame arg0, XComponentContext arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#getCertificateState()
	 */
	@Override
	public CertificateState getCertificateState() {
		return m_aCertificateState;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance(com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificateState verifyCertificateCompliance(XFrame _xFrame,
			Object _xComponent) throws IllegalArgumentException, Exception {
		m_xFrame = _xFrame;
		// TODO Auto-generated method stub
		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, _xComponent);
		if(m_xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance wrong argument"));
		m_aCertificateState = CertificateState.OK;
		//convert the certificate to java internal representation
		try {
            m_JavaCert = Helpers.getCertificate(m_xQc);// (java.security.cert.X509Certificate) cf.generateCertificate(bais);
            //check for version, if version is not 3, exits, certificate cannot be used
            
			m_aCAState = CertificationAuthorityState.TRUSTED;

            if(m_JavaCert.getVersion() != 3) {
    			m_xQc.setCertificateElementErrorState(
    					GlobConstant.m_sX509_CERTIFICATE_VERSION,
    					CertificateElementState.INVALID_value);			
    			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
            	return m_aCertificateState;
            }
			//check for validity date
			try {
/*				// test for date information
				// not yet valid: 
				// GregorianCalendar aCal = new GregorianCalendar(2008,12,12);
				// expired:
				// GregorianCalendar aCal = new GregorianCalendar(2019,12,12);
				m_JavaCert.checkValidity(aCal.getTime());*/
				m_JavaCert.checkValidity();
				//valid, set no CRL needed
				m_aCertStateConds = CertificateStateConditions.REVOCATION_CONTROL_NOT_NEEDED;
			} catch (CertificateExpiredException e) {
				m_xQc.setCertificateElementErrorState(
						GlobConstant.m_sX509_CERTIFICATE_NOT_AFTER,
						CertificateElementState.INVALID_value);
				setCertificateStateHelper(CertificateState.EXPIRED);
				m_aCAState = CertificationAuthorityState.TRUSTED_WITH_WARNING;
				m_xQc.getCertificateDisplayObj().setCertificateElementCommentString(CertificateElementID.NOT_AFTER,
						"The date is elapsed.");				
//check CRL of this certificate
//commented due to excessive time out			verifyCertifRevocHelper();
			} catch (CertificateNotYetValidException e) {
				m_xQc.setCertificateElementErrorState(
						GlobConstant.m_sX509_CERTIFICATE_NOT_BEFORE,
						CertificateElementState.INVALID_value);
				setCertificateStateHelper(CertificateState.NOT_ACTIVE);
				m_aCAState = CertificationAuthorityState.TRUSTED_WITH_WARNING;
				m_xQc.getCertificateDisplayObj().setCertificateElementCommentString(CertificateElementID.NOT_BEFORE,
				"The date is not yet arrived.");
			}

			//check the KeyUsage extension
/*			int tempState = CertificateElementState.OK_value;
			if(!isKeyUsageNonRepudiationCritical(m_JavaCert)) {
				tempState =  CertificateElementState.INVALID_value;
				setCertificateStateHelper(CertificateState.NOT_COMPLIANT);
			}
			m_xQc.setCertificateElementErrorState(X509Extensions.KeyUsage.getId(), tempState);*/
		} catch (CertificateException e) {
			m_aLogger.severe(e);
			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
			throw (new com.sun.star.uno.Exception(" wrapped exception: "));
		}

//convert to Bouncy Castle representation		
		ByteArrayInputStream as = new ByteArrayInputStream(m_xQc.getCertificateAttributes().getDEREncoded()); 
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado = null;
		try {
			ado = aderin.readObject();
			X509CertificateStructure x509Str = new X509CertificateStructure((ASN1Sequence) ado);
			//check issuer field for conformance
			TBSCertificateStructure xTBSCert = x509Str.getTBSCertificate();

			//check if either one of IssuerUniqueID or SubjectUniqueID is present
			//ETSI 102 280 5.3
			if(!isOKUniqueIds(xTBSCert)) {
				setCertificateStateHelper(CertificateState.CORE_CERTIFICATE_ELEMENT_INVALID);
				return m_aCertificateState;
			}

			if(!isIssuerIdOk(xTBSCert)) {
				m_xQc.setCertificateElementErrorState("IssuerName", CertificateElementState.INVALID_value);
				setCertificateStateHelper(CertificateState.NOT_COMPLIANT);
			}

/*			//check if qcStatements are present
			//the function set the error itself
			if(!hasQcStatements(xTBSCert)) {
				return m_aCertificateState;
			}*/

		} catch (java.io.IOException e) {
			m_aLogger.severe(e);
			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
			throw (new com.sun.star.uno.Exception(" wrapped exception: "));
		} catch (java.lang.Exception e) {
			m_aLogger.severe(e);
			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
			throw (new com.sun.star.uno.Exception(" wrapped exception: "));
		}
		return m_aCertificateState;
	}

	/**
	 * @param cert
	 * @return
	 */
	private boolean isOKUniqueIds(TBSCertificateStructure cert) {
		//check if either one of IssuerUniqueID or SubjectUniqueID is present
		//ETSI 102 280 5.3
		DERString isUid = cert.getIssuerUniqueId();
		DERString isSid = cert.getSubjectUniqueId();
		if(isUid == null && isSid == null)
			return true;
		m_aLogger.log("detected spurious IssuerUniqueID or SubjectUniqueID");
		return false;
	}

	/**
	 * check for priority of certificate state and set it accordingly
	 * @param _newState
	 */
	private void setCertificateStateHelper(CertificateState _newState) {
		if(_newState.getValue() >m_aCertificateState.getValue())
			m_aCertificateState = _newState;
	}

    /**
	 * @param _TbsC 
     * @return
	 */
	private boolean isIssuerIdOk(TBSCertificateStructure _TbsC) {
		//check if issuer element has both organizationName and countryName
		boolean isOk = false;
		//the CNIPA requirement are identical to
		//ETSI 102 280 and ETSI 101 862 requirements
		Vector<DERObjectIdentifier> oidv =  _TbsC.getIssuer().getOIDs();
		if(oidv.contains(X509Name.O) && 		//organizationName
				oidv.contains(X509Name.C))		//countryName
			isOk = true;
		return isOk;
	}

	/**
	 * check if qcStatements are present as per ETSI 
	 * @param _TbsC 
	 * @return
	 */
	private boolean hasQcStatements(TBSCertificateStructure _TbsC) {
		//first check for CNIPA requirement
		//then check for ETSI 102 280 requirements
		//then check for ETSI 101 862		
		//qcstatements are defined in ETSI 101 862
		X509Extensions xExt = _TbsC.getExtensions();
		X509Extension qcStats = xExt.getExtension(X509Extensions.QCStatements);

		if(qcStats == null) {
			//no qcStatement
			setCertificateStateHelper(CertificateState.MISSING_EXTENSION);
			m_aLogger.log("missing qcStatements");
			return false;
		}
		int numberOfChecksOk = 4; //if this drops to zero,

		//it's not marked critical
		if(!qcStats.isCritical())
			numberOfChecksOk--;

        ASN1Sequence    dns = (ASN1Sequence)X509Extension.convertValueToObject(qcStats);
        for(int i= 0; i<dns.size();i++) {
            QCStatement qcs = QCStatement.getInstance(dns.getObjectAt(i));
            if (QCStatement.id_etsi_qcs_QcCompliance.equals(qcs.getStatementId()))
            	numberOfChecksOk--;
            if(QCStatement.id_etsi_qcs_QcSSCD.equals(qcs.getStatementId()))
            	numberOfChecksOk--;
            if(QCStatement.id_etsi_qcs_RetentionPeriod.equals(qcs.getStatementId()))
            	numberOfChecksOk--;
        }

        if(numberOfChecksOk != 0) {
			m_xQc.setCertificateElementErrorState(X509Extensions.QCStatements.getId(), CertificateElementState.INVALID_value);			
			setCertificateStateHelper(CertificateState.ERROR_IN_EXTENSION);
			return false;
        }
		
		return true;
	}

	/**
     * checks Key Usage constraints of a java certificate.
     *
     * @param m_JavaCert
     *            the certificate to check as java object.
     * @return true if the given certificate has a KeyUsage extension of 'non
     *         repudiation' (OID: 2.5.29.15) marked as critical.
     * @see PKCS11Signer#findCertificateWithNonRepudiationCritical()
     */
    private boolean isKeyUsageNonRepudiationCritical(
            java.security.cert.X509Certificate javaCert) {

        boolean isNonRepudiationPresent = false;
        boolean isKeyUsageCritical = false;

        Set<String> oids = javaCert.getCriticalExtensionOIDs();
        if (oids != null) {
            // check presence between critical extensions of oid:2.5.29.15
            // (KeyUsage)
            isKeyUsageCritical = oids.contains(X509Extensions.KeyUsage.getId());
        }

        boolean[] keyUsages = javaCert.getKeyUsage();
        if (keyUsages != null) {
            //check non repudiation (index 1)
            /*
             * now check the elements on KeyUsage, only nonRepudiation should be there
             * nothing else is allowed, see (only Italian available):
             * Deliberazione CNIPA del 17 febbraio 2005 n 4
             * Articolo 4 - Profilo dei certificati qualificati
             * comma 5, punto a
             * 
             */
        	//keyusage
            isNonRepudiationPresent = (keyUsages[1] & //nonRepudiation
            		!(
            		keyUsages[0] | // digitalSignature (0),
            		keyUsages[2] | // keyEncipherment  (2),
            		keyUsages[3] | // dataEncipherment (3),
            		keyUsages[4] | // keyAgreement     (4),
            		keyUsages[5] | // keyCertSign      (5),
            		keyUsages[6] | // cRLSign          (6),
            		keyUsages[7] | // encipherOnly     (7),
               		keyUsages[8]   // decipherOnly     (8) }
            		));
        }
        return (isKeyUsageCritical && isNonRepudiationPresent);
    }


	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#getCertificationAutorities()
	 */
	@Override
	public int getCertificationAuthoritiesNumber() {
		return 0;
	}
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#getCertificationAuthorities(com.sun.star.frame.XFrame)
	 */
	@Override
	public XComponent[] getCertificationAuthorities(XFrame arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#getCertificationAuthorityState()
	 */
	@Override
	public CertificationAuthorityState getCertificationAuthorityState() {
		return m_aCAState;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathControlProcedure#verifyCertificationPath(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 * 
	 * not implemented, same as the checker
	 */
	@Override
	public CertificationAuthorityState verifyCertificationPath(XFrame arg0,
			Object arg1) throws IllegalArgumentException, Exception {
		verifyCertificateCompliance(arg0,arg1);
		return m_aCAState;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateRevocationStateProcedure#getCertificateStateConditions()
	 */
	@Override
	public CertificateStateConditions getCertificateStateConditions() {
		return m_aCertStateConds;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificateRevocationStateProcedure#verifyCertificateRevocationState(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificateState verifyCertificateRevocationState(XFrame arg0,
			Object arg1) throws IllegalArgumentException, Exception {
		verifyCertificateCompliance(arg0, arg1);
		// FIXME check if revocation control is enabled or not
		m_aLogger.log("verifyCertificateRevocationState");
		try {
			XOX_CertificateRevocationStateProcedure axoxChildProc = null;
			XOX_SingletonDataAccess xSingletonDataAccess = Helpers.getSingletonDataAccess(m_xCC);

			try {
				XComponent xComp = xSingletonDataAccess.getUNOComponent(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT);					
				//yes, grab it and set our component internally
				m_aLogger.info("Cache found!");
				axoxChildProc = 
					(XOX_CertificateRevocationStateProcedure)
					UnoRuntime.queryInterface(
							XOX_CertificateRevocationStateProcedure.class, xComp);
			} catch (NoSuchElementException ex ) {
				//no, instantiate it and add to the singleton 
				m_aLogger.info("Cache NOT found!");
				//create the object
				Object oCertPath = m_xMCF.createInstanceWithContext(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT, m_xCC);
				//add it the singleton
				//now use it
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, oCertPath); 
				if(xComp != null) {
					xSingletonDataAccess.addUNOComponent(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT, xComp);
					axoxChildProc = (XOX_CertificateRevocationStateProcedure)
								UnoRuntime.queryInterface(XOX_CertificateRevocationStateProcedure.class, xComp);
				}
				else
					throw (new IllegalArgumentException());
			} catch (IllegalArgumentException ex ) {
				m_aLogger.severe(ex);
			} catch (Throwable ex ) { 
				m_aLogger.severe(ex);
			}

			if(axoxChildProc != null) {
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, m_xQc); 
				if(xComp != null)
					axoxChildProc.verifyCertificateRevocationState(m_xFrame,xComp);
					m_aCertificateState = axoxChildProc.getCertificateState();
					m_aCertStateConds = axoxChildProc.getCertificateStateConditions();
			}
			else
				m_aLogger.info("CANNOT execute child");
			//instantiate the cache, init it and add it
		} catch (ClassCastException ex) {
			m_aLogger.severe(ex);
		} catch (ServiceNotFoundException ex) {
			m_aLogger.severe(ex);
		} catch (Throwable ex ) { 
			m_aLogger.severe(ex);
		}
		return m_aCertificateState;
	}
}
