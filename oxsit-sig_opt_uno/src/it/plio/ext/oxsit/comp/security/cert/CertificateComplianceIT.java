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

import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.CertificateAuthorityState;
import it.plio.ext.oxsit.security.cert.CertificateExtensionState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XProperty;
import com.sun.star.beans.XPropertyAccess;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XTypeProvider;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.lib.uno.helper.WeakAdapter;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.Any;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XAdapter;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.uno.XWeak;
import com.sun.star.util.DateTime;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

/**
 *  This service implements the CertificateComplianceIT service, used to check the
 *  certificate for compliance on Italian law.
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

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public CertificateComplianceIT(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
 //   	m_aLogger.enableLogging();
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#getCertificationAuthorityState()
	 */
	@Override
	public CertificateState getCertificationAuthorityState() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance(com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificateState verifyCertificateCertificateCompliance(
			XComponent arg0) throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		XOX_QualifiedCertificate xQc = (XOX_QualifiedCertificate)UnoRuntime.queryInterface(XOX_QualifiedCertificate.class, arg0);
		if(xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance wrong argument"));
		//convert the certificate to java internal rapresentation
        java.security.cert.X509Certificate javaCert = null;
        java.security.cert.CertificateFactory cf;
		try {
			cf = java.security.cert.CertificateFactory.getInstance("X.509");
			java.io.ByteArrayInputStream bais = null;
            bais = new java.io.ByteArrayInputStream(xQc.getDEREncoded());
            javaCert = (java.security.cert.X509Certificate) cf.generateCertificate(bais);
			int tempState = CertificateExtensionState.OK_value;
			if(!isKeyUsageNonRepudiationCritical(javaCert))
				tempState =  CertificateExtensionState.INVALID_value;
			xQc.setCertificateExtensionErrorState(X509Extensions.KeyUsage.getId(), tempState);
		} catch (CertificateException e) {
			m_aLogger.severe(e);
			throw (new Exception(" wrapped exception: "));		
		}

//convert to BC rapresentation		
/*		ByteArrayInputStream as = new ByteArrayInputStream(xQc.getDEREncoded()); 
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado = null;
		try {
			ado = aderin.readObject();
			X509CertificateStructure x509Str = new X509CertificateStructure((ASN1Sequence) ado);
			//check the key usage function
			int tempState = CertificateExtensionState.OK_value;
			if(!isKeyUsageNonRepudiationCritical(x509Str))
				tempState =  CertificateExtensionState.INVALID_value;
			xQc.setCertificateExtensionErrorState(X509Extensions.KeyUsage.getId(), tempState);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			m_aLogger.severe(e);
			throw (new Exception(" wrapped exception: "));
		}*/

		return null;
	}

  
    /**
     * checks Key Usage constraints of a java certificate.
     *
     * @param javaCert
     *            the certificate to check as java object.
     * @return true if the given certificate has a KeyUsage extension of 'non
     *         repudiation' (OID: 2.5.29.15) marked as critical.
     * @see PKCS11Signer#findCertificateWithNonRepudiationCritical()
     */
    boolean isKeyUsageNonRepudiationCritical(
            java.security.cert.X509Certificate javaCert) {

        boolean isNonRepudiationPresent = false;
        boolean isKeyUsageCritical = false;

        Set oids = javaCert.getCriticalExtensionOIDs();
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
