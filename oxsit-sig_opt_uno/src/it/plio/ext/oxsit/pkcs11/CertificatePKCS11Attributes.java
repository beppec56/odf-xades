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

package it.plio.ext.oxsit.pkcs11;

import it.plio.ext.oxsit.security.PKCS11TokenAttributes;

import java.security.cert.X509Certificate;

/** Contains the additional information about a certificate that are
 * present in an SSCD conforming to RSA PKCS 11 interface
 * standard.<br>
 * 
 * @author beppec56
 *
 */
public class CertificatePKCS11Attributes {
	// The certificate retrieved
	private X509Certificate	m_oCertificateValue;
	//  The certificate retrieved in DER encoded format
	private byte[]			m_aCertificateValueDEREncoded;
	//This is filled with the PKCS 11 CKA_LABEL attribute
	//of the certificate
	private String			m_sCertificateLabel;
	//This is filled with the PKCS 11 CKA_ID attribute
	//of the certificate
	private byte[]			m_aCertificateID;
	
	//the token where the certificate was found
	private PKCS11TokenAttributes	m_aTheToken;

	public	CertificatePKCS11Attributes() {
		m_oCertificateValue = null;
		m_aCertificateValueDEREncoded = null;
		m_sCertificateLabel = null;
		m_aCertificateID = null;
	}

	/**
	 * @param m_oCertificateValue the m_oCertificateValue to set
	 */
	public void setCertificateValue(X509Certificate m_oCertificateValue) {
		this.m_oCertificateValue = m_oCertificateValue;
	}

	/**
	 * @return the m_oCertificateValue
	 */
	public X509Certificate getCertificateValue() {
		return m_oCertificateValue;
	}

	/**
	 * @param m_aCertificateValueDEREncoded the m_aCertificateValueDEREncoded to set
	 */
	public void setCertificateValueDEREncoded(
			byte[] m_aCertificateValueDEREncoded) {
		this.m_aCertificateValueDEREncoded = m_aCertificateValueDEREncoded;
	}

	/**
	 * @return the m_aCertificateValueDEREncoded
	 */
	public byte[] getCertificateValueDEREncoded() {
		return m_aCertificateValueDEREncoded;
	}

	/**
	 * @param m_sCertificateLabel the m_sCertificateLabel to set
	 */
	public void setCertificateLabel(String m_sCertificateLabel) {
		this.m_sCertificateLabel = m_sCertificateLabel;
	}

	/**
	 * @return the m_sCertificateLabel
	 */
	public String getCertificateLabel() {
		return m_sCertificateLabel;
	}

	/**
	 * @param m_aCertificateID the m_aCertificateID to set
	 */
	public void setCertificateID(byte[] m_aCertificateID) {
		this.m_aCertificateID = m_aCertificateID;
	}

	/**
	 * @return the m_aCertificateID
	 */
	public byte[] getCertificateID() {
		return m_aCertificateID;
	}

	/**
	 * @param m_aTheToken the m_aTheToken to set
	 */
	public void setToken(PKCS11TokenAttributes m_aTheToken) {
		this.m_aTheToken = m_aTheToken;
	}

	/**
	 * @return the m_aTheToken
	 */
	public PKCS11TokenAttributes getToken() {
		return m_aTheToken;
	}
}
