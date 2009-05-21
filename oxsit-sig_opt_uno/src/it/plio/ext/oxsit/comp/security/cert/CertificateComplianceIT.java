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

package it.plio.ext.oxsit.comp.security.cert;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;
import it.trento.comune.j4sign.pkcs11.PKCS11Signer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.bouncycastle.asn1.x509.qualified.Iso4217CurrencyCode;
import org.bouncycastle.asn1.x509.qualified.MonetaryValue;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.i18n.filter.TrustedInput;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 *  This service implements the CertificateComplianceIT service, used to check the
 *  certificate for compliance on Italian law.
 *  
 *  The conformance will be checked for the certificate dates, certificate configuration
 *  and for extension  that are mandatori according to the following criteria/Norms,
 *  listed in ascending order, the precedence order is from top to below:
 *  - Deliberazione CNIPA del 17 febbraio 2005 n 4
 *  - ETSI TS 102 280 V1.1.1
 *  - ETSI TS 101 862 V1.3.2
 *  
 * @author beppec56
 *
 */
public class CertificateComplianceIT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_CertificateComplianceControlProcedure
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= CertificateComplianceIT.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sCERTIFICATE_COMPLIANCE_SERVICE_IT };

	protected DynamicLogger m_aLogger;

	protected XOX_QualifiedCertificate m_xQc;

	private CertificateState m_aCertificateState;
    private java.security.cert.X509Certificate m_JavaCert = null;

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public CertificateComplianceIT(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
//		m_aLogger.enableLogging();
    	m_aLogger.ctor();    	
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#configureOptions(com.sun.star.frame.XFrame, com.sun.star.uno.XComponentContext)
	 */
	@Override
	public void configureOptions(XFrame arg0, XComponentContext arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#getCertificateState()
	 */
	@Override
	public CertificateState getCertificateState() {
		return m_aCertificateState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance(com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificateState verifyCertificateCertificateCompliance(
			XComponent arg0) throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		m_xQc = (XOX_QualifiedCertificate)UnoRuntime.queryInterface(XOX_QualifiedCertificate.class, arg0);
		if(m_xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance wrong argument"));
		m_aCertificateState = CertificateState.OK;
		//convert the certificate to java internal representation
        java.security.cert.CertificateFactory cf;
		try {
			cf = java.security.cert.CertificateFactory.getInstance("X.509");
			java.io.ByteArrayInputStream bais = null;
            bais = new java.io.ByteArrayInputStream(m_xQc.getDEREncoded());
            m_JavaCert = (java.security.cert.X509Certificate) cf.generateCertificate(bais);
            //check for version, if version is not 3, exits, certificate cannot be used
            if(m_JavaCert.getVersion() != 3) {
    			m_xQc.setCertificateElementErrorState("Version", CertificateElementState.INVALID_value);			
    			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
            	return m_aCertificateState;
            }
			//check for validity date
			try {
/*				//test for date information
				//not yet valid
				GregorianCalendar aCal = new GregorianCalendar(2008,12,12);
				//expired
				GregorianCalendar aCal = new GregorianCalendar(2019,12,12);
				m_JavaCert.checkValidity(aCal.getTime());*/
				m_JavaCert.checkValidity();
			} catch (CertificateExpiredException e) {
				m_xQc.setCertificateElementErrorState("NotValidAfter", CertificateElementState.INVALID_value);
				setCertificateStateHelper(CertificateState.EXPIRED);
			} catch (CertificateNotYetValidException e) {
				m_xQc.setCertificateElementErrorState("NotValidBefore", CertificateElementState.INVALID_value);
				setCertificateStateHelper(CertificateState.NOT_ACTIVE);
			}

			//check the KeyUsage extension
			int tempState = CertificateElementState.OK_value;
			if(!isKeyUsageNonRepudiationCritical(m_JavaCert)) {
				tempState =  CertificateElementState.INVALID_value;
				setCertificateStateHelper(CertificateState.NOT_COMPLIANT);
			}
			m_xQc.setCertificateElementErrorState(X509Extensions.KeyUsage.getId(), tempState);
		} catch (CertificateException e) {
			m_aLogger.severe(e);
			setCertificateStateHelper(CertificateState.MALFORMED_CERTIFICATE);
			throw (new com.sun.star.uno.Exception(" wrapped exception: "));
		}

//convert to Bouncy Castle representation		
		ByteArrayInputStream as = new ByteArrayInputStream(m_xQc.getDEREncoded()); 
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

			//check if qcStatements are present
			//the function set the error itself
			if(!hasQcStatements(xTBSCert)) {
				return m_aCertificateState;
			}

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
		m_aLogger.log("detected spurious IssuerUniqueID od SubjectUniqueID");
		return false;
	}

	/**
	 * check for priority of certificate state and set it accordingly
	 * @param _newState
	 */
	private void setCertificateStateHelper(CertificateState _newState) {
		if(Helpers.mapCertificateStateToValue(_newState) >
		Helpers.mapCertificateStateToValue(m_aCertificateState))
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
    boolean isKeyUsageNonRepudiationCritical(
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
}
