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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/CompleteCertificateRefs_IT.java.
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
 * Models the ETSI CompleteCertificateRefs
 * element
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class CompleteCertificateRefs_IT implements Serializable {
    /** parent object - UnsignedProperties ref */
    private UnsignedProperties_IT m_unsignedProps;
    
    /** 
     * Creates new CompleteCertificateRefs 
     * and initializes everything to null
     */
    public CompleteCertificateRefs_IT() {
        m_unsignedProps = null;        
    }
    
    /** 
     * Creates new CompleteCertificateRefs.
     * Rerouted to set those values on responders certid.
     * @param certId OCSP responders cert id (in XML)
     * @param digAlg OCSP responders certs digest algorithm id/uri
     * @param digest OCSP responders certs digest
     * @param serial OCSP responders certs issuers serial number
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CompleteCertificateRefs_IT(String certId, String digAlg, byte[] digest, BigInteger serial) 
        throws SignedODFDocumentException_IT
    {
    	CertID_IT cid = new CertID_IT(certId, digAlg, digest, serial, null, CertID_IT.CERTID_TYPE_RESPONDER);
    	addCertID(cid);
        m_unsignedProps = null;
    }

    /** 
     * Creates new CompleteCertificateRefs by using
     * default values for id and responders cert 
     * Rerouted to set those values on responders certid.
     * @param sig Signature object
     * @param respCert OCSP responders cert
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CompleteCertificateRefs_IT(SignatureXADES_IT sig, X509Certificate respCert) 
        throws SignedODFDocumentException_IT
    {        
    	CertID_IT cid = new CertID_IT(sig, respCert, CertID_IT.CERTID_TYPE_RESPONDER);
    	sig.addCertID(cid);
     }
    
    /**
     * return the count of CertID_IT objects
     * @return count of CertID_IT objects
     */
    public int countCertIDs()
    {
        return m_unsignedProps.getSignature().countCertIDs();
    }
    
    /**
     * Adds a new CertID_IT object
     * @param cid new object to be added
     */
    public void addCertID(CertID_IT cid)
    {
    	m_unsignedProps.getSignature().addCertID(cid);
    }
    
    /**
     * Retrieves CertID_IT element with the desired index
     * @param idx CertID_IT index
     * @return CertID_IT element or null if not found
     */
    public CertID_IT getCertID(int idx)
    {
    	return m_unsignedProps.getSignature().getCertID(idx); 
    }
    
    /**
     * Retrieves the last CertID_IT element
     * @return CertID_IT element or null if not found
     */
    public CertID_IT getLastCertId()
    {
    	return m_unsignedProps.getSignature().getLastCertId(); 
    }
    
    /**
     * Retrieves CertID_IT element with the desired type
     * @param type CertID_IT type
     * @return CertID_IT element or null if not found
     */
    public CertID_IT getCertIdOfType(int type)
    {
    	return m_unsignedProps.getSignature().getCertIdOfType(type);
    }
    
    /**
     * Retrieves CertID_IT element with the desired type.
     * If not found creates a new one with this type.
     * @param type CertID_IT type
     * @return CertID_IT element
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CertID_IT getOrCreateCertIdOfType(int type)
    	throws SignedODFDocumentException_IT
    {
    	return m_unsignedProps.getSignature().getOrCreateCertIdOfType(type);
    }
    

    
    /**
     * Accessor for UnsignedProperties attribute
     * @return value of UnsignedProperties attribute
     */
    public UnsignedProperties_IT getUnsignedProperties()
    {
    	return m_unsignedProps;
    }
    
    /**
     * Mutator for UnsignedProperties attribute
     * @param uprops value of UnsignedProperties attribute
     */
    public void setUnsignedProperties(UnsignedProperties_IT uprops)
    {
    	m_unsignedProps = uprops;
    }
    
    /**
     * Accessor for certId attribute
     * Rerouted to get this attribute from CertID_IT sublement.
     * @return value of certId attribute
     */
    public String getCertId() {
    	CertID_IT cid = getCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	if(cid != null)
    		return cid.getId();
    	else
    		return null;
    }
    
    /**
     * Mutator for certId attribute.
     * Rerouted to set this attribute on CertID_IT sublement.
     * @param str new value for certId attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setCertId(String str) 
        throws SignedODFDocumentException_IT
    {
    	CertID_IT cid = getOrCreateCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	cid.setId(str);
    }
    
    /**
     * Accessor for certDigestAlgorithm attribute
     * Rerouted to get this attribute from CertID_IT sublement.
     * @return value of certDigestAlgorithm attribute
     */
    public String getCertDigestAlgorithm() {
    	CertID_IT cid = getCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	if(cid != null)
    		return cid.getDigestAlgorithm();
    	else
    		return null;
    }
    
    /**
     * Mutator for certDigestAlgorithm attribute.
     * Rerouted to set this attribute on CertID_IT sublement.
     * @param str new value for certDigestAlgorithm attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setCertDigestAlgorithm(String str) 
        throws SignedODFDocumentException_IT
    {
    	CertID_IT cid = getOrCreateCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	cid.setDigestAlgorithm(str);
    }
    
    /**
     * Accessor for certDigestValue attribute
     * Rerouted to get this attribute from CertID_IT sublement.
     * @return value of certDigestValue attribute
     */
    public byte[] getCertDigestValue() {
    	CertID_IT cid = getCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	if(cid != null)
    		return cid.getDigestValue();
    	else
    		return null;
    }
    
    /**
     * Mutator for certDigestValue attribute.
     * Rerouted to set this attribute on CertID_IT sublement.
     * @param data new value for certDigestValue attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setCertDigestValue(byte[] data) 
        throws SignedODFDocumentException_IT
    {
    	CertID_IT cid = getOrCreateCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	cid.setDigestValue(data);
    }
     
    /**
     * Accessor for certSerial attribute.
     * Rerouted to get this attribute from CertID_IT sublement.
     * @return value of certSerial attribute
     */
    public BigInteger getCertSerial() {
    	CertID_IT cid = getCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	if(cid != null)
    		return cid.getSerial();
    	else
    		return null;
    }
    
    /**
     * Mutator for certSerial attribute.
     * Rerouted to set this attribute on CertID_IT sublement.
     * @param str new value for certSerial attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setCertSerial(BigInteger i) 
        throws SignedODFDocumentException_IT
    {
    	CertID_IT cid = getOrCreateCertIdOfType(CertID_IT.CERTID_TYPE_RESPONDER);
    	cid.setSerial(i);
    }

    /**
     * Helper method to validate the whole
     * CompleteCertificateRefs object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        for(int i = 0; i < countCertIDs(); i++) {
    		CertID_IT cid = getCertID(i);
    		ArrayList a = cid.validate();
    		if(a.size() > 0)
    			errs.addAll(a);
    	}
        return errs;
    }
    
    /**
     * Converts the CompleteCertificateRefs to XML form
     * @return XML representation of CompleteCertificateRefs
     */
    public byte[] toXML()
        throws SignedODFDocumentException_IT
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
            bos.write(ConvertUtils.str2data("<CompleteCertificateRefs>"));
            if(m_unsignedProps.getSignature().getSignedDoc().getVersion().equals(SignedODFDocument_IT.VERSION_1_3) ||
               m_unsignedProps.getSignature().getSignedDoc().getVersion().equals(SignedODFDocument_IT.VERSION_1_4)) {
            	bos.write(ConvertUtils.str2data("<CertRefs>\n"));
            } 
            for(int i = 0; i < countCertIDs(); i++) {
        		CertID_IT cid = getCertID(i);
        		if(cid.getType() != CertID_IT.CERTID_TYPE_SIGNER) {
        			bos.write(cid.toXML());
        			bos.write(ConvertUtils.str2data("\n"));   
        		}
            }
            if(m_unsignedProps.getSignature().getSignedDoc().getVersion().equals(SignedODFDocument_IT.VERSION_1_3) ||
               m_unsignedProps.getSignature().getSignedDoc().getVersion().equals(SignedODFDocument_IT.VERSION_1_4)) {
            	bos.write(ConvertUtils.str2data("</CertRefs>"));
            }
            bos.write(ConvertUtils.str2data("</CompleteCertificateRefs>"));        
        } catch(IOException ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_XML_CONVERT);
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
