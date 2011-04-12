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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/CertID.java.
 *
 * The Initial Developer of the Original Code is
 * AUTHOR:  Veiko Sinivee, S|E|B IT Partner Estonia
 * Copyright (C) AS Sertifitseerimiskeskus
 * from which ideas and part of the code are derived
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * FIXME add Rob e-mail
 * Roberto Resoli 
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

package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Models the ETSI <Cert> element
 * Holds info about a certificate but not
 * the certificate itself. Such elements will
 * be serialized under the <CompleteCertificateRefs>
 * element
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class CertID implements Serializable {
    /** certs digest algorithm */
    private String m_digestAlgorithm;
    /** elements id atribute if present */
    private String m_id;
    /** certs digest data */
    private byte[] m_digestValue;
    /** certs issuer DN */
    private String m_issuer;
    /** certs issuer serial number */
    private BigInteger m_serial;
    /** parent object - Signature ref */
    private Signature m_signature;
    /** CertID type - signer, responder, tsa */
    private int m_type;
    
    /** possible certid type values */
    public static final int CERTID_TYPE_UNKNOWN = 0;
    public static final int CERTID_TYPE_SIGNER = 1;
    public static final int CERTID_TYPE_RESPONDER = 2;
    public static final int CERTID_TYPE_TSA = 3;
    
    /** 
     * Creates new CertID 
     * and initializes everything to null
     */
    public CertID() {
        m_id = null;
        m_digestAlgorithm = null;
        m_digestValue = null;
        m_serial = null;
        m_issuer = null;
        m_signature = null;
        m_type = CERTID_TYPE_UNKNOWN;
    }
    
    /** 
     * Creates new CertID 
     * @param certId OCSP responders cert id (in XML)
     * @param digAlg OCSP responders certs digest algorithm id/uri
     * @param digest OCSP responders certs digest
     * @param serial OCSP responders certs issuers serial number
     * @param type CertID type: signer, responder or tsa
     * @throws SignedDocException for validation errors
     */
    public CertID(String certId, String digAlg, byte[] digest, 
    		BigInteger serial, String issuer, int type) 
        throws SignedDocException
    {
        setId(certId);
        setDigestAlgorithm(digAlg);
        setDigestValue(digest);
        setSerial(serial);
        if(issuer != null)
        	setIssuer(issuer);
        setType(type);
        m_signature = null;
    }

    /** 
     * Creates new CertID by using
     * default values for id and responders cert 
     * @param sig Signature object
     * @param cert OCSP certificate for creating this ref data
     * @param type CertID type: signer, responder or tsa
     * @throws SignedDocException for validation errors
     */
    public CertID(Signature sig, X509Certificate cert, int type) 
        throws SignedDocException
    {        
        setId(sig.getId() + "-RESPONDER_CERTINFO");
        setDigestAlgorithm(SignedDoc.SHA1_DIGEST_ALGORITHM);
        byte[] digest = null;
        try {
            digest = SignedDoc.digest(cert.getEncoded());
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_CALCULATE_DIGEST); 
        }
        setDigestValue(digest);
        setSerial(cert.getSerialNumber());
        setIssuer(cert.getIssuerX500Principal().getName("RFC1779"));
        setType(type);
    }
    
    /**
     * Accessor for Signature attribute
     * @return value of Signature attribute
     */
    public Signature getSignature()
    {
    	return m_signature;
    }
    
    /**
     * Mutator for Signature attribute
     * @param uprops value of Signature attribute
     */
    public void setSignature(Signature sig)
    {
    	m_signature = sig;
    }
    
    /**
     * Accessor for certId attribute
     * @return value of certId attribute
     */
    public String getId() {
        return m_id;
    }
    
    /**
     * Mutator for certId attribute
     * @param str new value for certId attribute
     * @throws SignedDocException for validation errors
     */    
    public void setId(String str) 
        throws SignedDocException
    {
    	if(m_signature != null && m_signature.getSignedDoc() != null &&
    	  !m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3) &&
    	  !m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_4)) {
        	SignedDocException ex = validateId(str);
        	if(ex != null)
            	throw ex;
    	}
        m_id = str;
    }
    
    /**
     * Helper method to validate an certificate id
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateId(String str)
    {
        SignedDocException ex = null;
        if(str == null && !m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)
        		&& !m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_4))
            ex = new SignedDocException(SignedDocException.ERR_RESPONDER_CERT_ID, 
                "Cert Id must be in form: <signature-id>-RESPONDER_CERTINFO", null);
        return ex;
    }
    
    /**
     * Accessor for digestAlgorithm attribute
     * @return value of digestAlgorithm attribute
     */
    public String getDigestAlgorithm() {
        return m_digestAlgorithm;
    }
    
    /**
     * Mutator for digestAlgorithm attribute
     * @param str new value for digestAlgorithm attribute
     * @throws SignedDocException for validation errors
     */    
    public void setDigestAlgorithm(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateDigestAlgorithm(str);
        if(ex != null)
            throw ex;
        m_digestAlgorithm = str;
    }
    
    /**
     * Helper method to validate a digest algorithm
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateDigestAlgorithm(String str)
    {
        SignedDocException ex = null;
        if(str == null || !str.equals(SignedDoc.SHA1_DIGEST_ALGORITHM))
            ex = new SignedDocException(SignedDocException.ERR_CERT_DIGEST_ALGORITHM, 
                "Currently supports only SHA1 digest algorithm", null);
        return ex;
    }
    
    /**
     * Accessor for digestValue attribute
     * @return value of digestValue attribute
     */
    public byte[] getDigestValue() {
        return m_digestValue;
    }
    
    /**
     * Mutator for digestValue attribute
     * @param data new value for digestValue attribute
     * @throws SignedDocException for validation errors
     */    
    public void setDigestValue(byte[] data) 
        throws SignedDocException
    {
        SignedDocException ex = validateDigestValue(data);
        if(ex != null)
            throw ex;
        m_digestValue = data;
    }
 
    /**
     * Helper method to validate a digest value
     * @param data input data
     * @return exception or null for ok
     */
    private SignedDocException validateDigestValue(byte[] data)
    {
        SignedDocException ex = null;
        if(data == null || data.length != SignedDoc.SHA1_DIGEST_LENGTH)
            ex = new SignedDocException(SignedDocException.ERR_DIGEST_LENGTH, 
                "SHA1 digest data is allways 20 bytes of length", null);
        return ex;
    }
    
    /**
     * Accessor for serial attribute
     * @return value of serial attribute
     */
    public BigInteger getSerial() {
        return m_serial;
    }
    
    /**
     * Mutator for serial attribute
     * @param str new value for serial attribute
     * @throws SignedDocException for validation errors
     */    
    public void setSerial(BigInteger i) 
        throws SignedDocException
    {
        SignedDocException ex = validateSerial(i);
        if(ex != null)
            throw ex;
        m_serial = i;
    }
    
    /**
     * Helper method to validate a serial
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateSerial(BigInteger i)
    {
        SignedDocException ex = null;
        if(i == null) // check the uri somehow ???
            ex = new SignedDocException(SignedDocException.ERR_CERT_SERIAL, 
                "Certificates serial number cannot be empty!", null);
        return ex;
    }

    /**
     * Accessor for issuer attribute
     * @return value of issuer attribute
     */
    public String getIssuer() {
        return m_issuer;
    }
    
    /**
     * Mutator for issuer attribute
     * @param str new value for issuer attribute
     * @throws SignedDocException for validation errors
     */    
    public void setIssuer(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateIssuer(str);
        if(ex != null)
            throw ex;
        m_issuer = str;
    }
    
    /**
     * Helper method to validate issuer
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateIssuer(String str)
    {
        SignedDocException ex = null;
        if(str == null && m_signature != null && 
           (m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3) ||
        		   m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_4)))
            ex = new SignedDocException(SignedDocException.ERR_CREF_ISSUER, 
                "Issuer name cannot be empty", null);
        return ex;
    }

    /**
     * Accessor for type attribute
     * @return value of type attribute
     */
    public int getType() {
        return m_type;
    }
    
    /**
     * Mutator for type attribute
     * @param n new value for issuer attribute
     * @throws SignedDocException for validation errors
     */    
    public void setType(int n) 
        throws SignedDocException
    {
        SignedDocException ex = validateType(n);
        if(ex != null)
            throw ex;
        m_type = n;
    }
    
    /**
     * Helper method to validate type
     * @param n input data
     * @return exception or null for ok
     */
    private SignedDocException validateType(int n)
    {
        SignedDocException ex = null;
        if(n < 0 || n > CERTID_TYPE_TSA)
            ex = new SignedDocException(SignedDocException.ERR_CERTID_TYPE, 
                "Invalid CertID type", null);
        return ex;
    }
    
    /**
     * Helper method to validate the whole
     * CertID object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedDocException ex = validateId(m_id);
        if(ex != null)
            errs.add(ex);
        ex = validateDigestAlgorithm(m_digestAlgorithm);
        if(ex != null)
            errs.add(ex);
        ex = validateDigestValue(m_digestValue);
        if(ex != null)
            errs.add(ex);
        ex = validateSerial(m_serial);
        if(ex != null)
            errs.add(ex);  
        ex = validateIssuer(m_issuer);
        if(ex != null)
            errs.add(ex);  
        ex = validateType(m_type);
        if(ex != null)
            errs.add(ex);
        return errs;
    }
    
    /**
     * Converts the CertID to XML form
     * @return XML representation of CertID
     */
    public byte[] toXML()
        throws SignedDocException
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
            if(m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3) ||
               m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_4)) {
            	bos.write(ConvertUtils.str2data("<Cert>"));
            } else {
            	bos.write(ConvertUtils.str2data("<Cert Id=\""));
            	bos.write(ConvertUtils.str2data(m_id));
            	bos.write(ConvertUtils.str2data("\">"));
            }
            bos.write(ConvertUtils.str2data("\n<CertDigest>\n<DigestMethod Algorithm=\""));
            bos.write(ConvertUtils.str2data(m_digestAlgorithm));
            bos.write(ConvertUtils.str2data("\">\n</DigestMethod>\n<DigestValue>"));
            bos.write(ConvertUtils.str2data(Base64Util.encode(m_digestValue, 0)));
            bos.write(ConvertUtils.str2data("</DigestValue>\n</CertDigest>\n"));
            // In version 1.3 we use correct <IssuerSerial> content 
            // e.g. subelements <X509IssuerName> and <X509SerialNumber>
            if(m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3) ||
            	m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_4)) {
            	bos.write(ConvertUtils.str2data("<IssuerSerial>"));
            	bos.write(ConvertUtils.str2data("\n<X509IssuerName xmlns=\""));
            	bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
            	bos.write(ConvertUtils.str2data("\">"));
            	bos.write(ConvertUtils.str2data(m_issuer));
            	bos.write(ConvertUtils.str2data("</X509IssuerName>"));
            	bos.write(ConvertUtils.str2data("\n<X509SerialNumber xmlns=\""));
            	bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
            	bos.write(ConvertUtils.str2data("\">"));
            	bos.write(ConvertUtils.str2data(m_serial.toString()));
            	bos.write(ConvertUtils.str2data("</X509SerialNumber>\n"));            		
               	bos.write(ConvertUtils.str2data("</IssuerSerial>\n"));
            } else { // in prior versions we used wrong <IssuerSerial> content
               	bos.write(ConvertUtils.str2data("<IssuerSerial>"));
               	bos.write(ConvertUtils.str2data(m_serial.toString()));
               	bos.write(ConvertUtils.str2data("</IssuerSerial>\n"));
            }            	
            bos.write(ConvertUtils.str2data("</Cert>"));
        } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of CompleteCertificateRefs
     * @return CompleteCertificateRefs string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }     
    
}
