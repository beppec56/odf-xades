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
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.CertificateAuthorityState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.XOX_CertificateExtension;
import it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

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
import com.sun.star.uno.XAdapter;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.uno.XWeak;
import com.sun.star.util.DateTime;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

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

	protected DynamicLogger m_logger;
	
	protected CertificateAuthorityState m_CAState;
	protected CertificateState			m_CState;
	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public QualifiedCertificate(XComponentContext _ctx) {
		m_logger = new DynamicLogger(this, _ctx);
    	m_logger.enableLogging();
    	m_logger.ctor();
    	m_CAState = CertificateAuthorityState.NO_CNIPA_ROOT;
    	m_CState = CertificateState.NOT_VERIFIABLE;
    	
	}

	public String getImplementationName() {
		// TODO Auto-generated method stub
		m_logger.entering("getImplementationName");
		return m_sImplementationName;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	public String[] getSupportedServiceNames() {
		// TODO Auto-generated method stub
		m_logger.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_logger.info("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * 
	 * arg0[0] = the DER stream of the certificate
	 */
	@Override
	public void initialize(Object[] arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getVersion()
	 */
	@Override
	public short getVersion() {
		// TODO Auto-generated method stub
		m_logger.info("getVersion");
		return 3;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#findCertificateExtension(byte[])
	 */
	@Override
	public XOX_CertificateExtension findCertificateExtension(byte[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getEncoded()
	 */
	@Override
	public byte[] getEncoded() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getExtensions()
	 */
	@Override
	public XOX_CertificateExtension[] getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getIssuerName()
	 */
	@Override
	public String getIssuerName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getIssuerUniqueID()
	 */
	@Override
	public byte[] getIssuerUniqueID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getMD5Thumbprint()
	 */
	@Override
	public byte[] getMD5Thumbprint() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getNotValidAfter()
	 */
	@Override
	public DateTime getNotValidAfter() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getNotValidBefore()
	 */
	@Override
	public DateTime getNotValidBefore() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSHA1Thumbprint()
	 */
	@Override
	public byte[] getSHA1Thumbprint() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSerialNumber()
	 */
	@Override
	public byte[] getSerialNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSignatureAlgorithm()
	 */
	@Override
	public String getSignatureAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectName()
	 */
	@Override
	public String getSubjectName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectPublicKeyAlgorithm()
	 */
	@Override
	public String getSubjectPublicKeyAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectPublicKeyValue()
	 */
	@Override
	public byte[] getSubjectPublicKeyValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getSubjectUniqueID()
	 */
	@Override
	public byte[] getSubjectUniqueID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#verifyCAForCertificate()
	 */
	@Override
	public boolean verifyCAForCertificate() {
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
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateAuthorityState()
	 */
	@Override
	public CertificateAuthorityState getCertificateAuthorityState() {
		return m_CAState;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate#getCertificateState()
	 */
	@Override
	public CertificateState getCertificateState() {
		return m_CState;
	}
}
