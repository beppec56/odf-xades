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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/CertValue_IT.java.
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * Models the ETSI <X509Certificate> 
 * and <EncapsulatedX509Certificate> elements.
 * Holds certificate data. Such elements will
 * be serialized under the <CertificateValues>
 * and <X509Data> elements
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class CertValue_IT {
    /** elements id atribute if present */
    private String m_id;
    /** parent object - SignatureXADES_IT ref */
    private SignatureXADES_IT m_signature;
    /** CertID type - signer, responder, tsa */
    private int m_type;
    /** certificate */
    private X509Certificate m_cert;
    
    /** possible cert value type values */
    public static final int CERTVAL_TYPE_UNKNOWN = 0;
    public static final int CERTVAL_TYPE_SIGNER = 1;
    public static final int CERTVAL_TYPE_RESPONDER = 2;
    public static final int CERTVAL_TYPE_TSA = 3;

    /** 
     * Creates new CertValue 
     * and initializes everything to null
     */
    public CertValue_IT() {
        m_id = null;
        m_signature = null;
        m_cert = null;
        m_type = CERTVAL_TYPE_UNKNOWN;
    }
    
    /**
     * Accessor for SignatureXADES_IT attribute
     * @return value of SignatureXADES_IT attribute
     */
    public SignatureXADES_IT getSignature()
    {
    	return m_signature;
    }
    
    /**
     * Mutator for SignatureXADES_IT attribute
     * @param uprops value of SignatureXADES_IT attribute
     */
    public void setSignature(SignatureXADES_IT sig)
    {
    	m_signature = sig;
    }
    
    /**
     * Accessor for id attribute
     * @return value of certId attribute
     */
    public String getId() {
        return m_id;
    }
    
    /**
     * Mutator for id attribute
     * @param str new value for certId attribute
     */    
    public void setId(String str) 
    {
    	m_id = str;
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
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setType(int n) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateType(n);
        if(ex != null)
            throw ex;
        m_type = n;
    }
    
    /**
     * Helper method to validate type
     * @param n input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateType(int n)
    {
        SignedODFDocumentException_IT ex = null;
        if(n < 0 || n > CERTVAL_TYPE_TSA)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_CERTID_TYPE, 
                "Invalid CertValue type", null);
        return ex;
    }    
        
    /**
     * Accessor for Cert attribute
     * @return value of Cert attribute
     */
    public X509Certificate getCert()
    {
    	return m_cert;
    }
    
    /**
     * Mutator for Cert attribute
     * @param uprops value of Cert attribute
     */
    public void setCert(X509Certificate cert)
    {
    	m_cert = cert;
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
        	if(m_type == CERTVAL_TYPE_SIGNER) {
        	bos.write(ConvertUtils.str2data("<X509Certificate>"));
            	try {
            		bos.write(ConvertUtils.str2data(Base64Util.encode(m_cert.getEncoded(), 64)));
            	} catch(CertificateEncodingException ex) {
            		SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_ENCODING);
            	}
            	bos.write(ConvertUtils.str2data("</X509Certificate>"));
        	}
        	if(m_type == CERTVAL_TYPE_RESPONDER ||
        	   m_type == CERTVAL_TYPE_TSA) {
        		bos.write(ConvertUtils.str2data("<EncapsulatedX509Certificate Id=\""));
                bos.write(ConvertUtils.str2data(m_id));
                bos.write(ConvertUtils.str2data("\">\n"));            
                try {
                    bos.write(ConvertUtils.str2data(Base64Util.encode(m_cert.getEncoded(), 64)));
                } catch(CertificateEncodingException ex) {
                    SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_ENCODING);
                }
                bos.write(ConvertUtils.str2data("</EncapsulatedX509Certificate>\n"));
        		
        	}
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
