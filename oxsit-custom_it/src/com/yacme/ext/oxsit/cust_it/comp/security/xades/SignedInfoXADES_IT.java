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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/.
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
import java.util.ArrayList;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager_IT;

/**
 * @author beppe
 *
 */
public class SignedInfoXADES_IT implements Serializable {
    /** reference to parent SignatureXADES_IT object */
    private SignatureXADES_IT m_signature;
    /** selected signature method */
    private String m_signatureMethod;
    /** selected canonicalization method */
    private String m_canonicalizationMethod;
    /** array of references */
    private ArrayList m_references;
    /** digest over the original bytes read from XML file  */
    private byte[] m_origDigest;


    /** 
     * Creates new SignedInfo. Initializes everything to null.
     * @param sig parent SignatureXADES_IT reference
     */
    public SignedInfoXADES_IT(SignatureXADES_IT sig) 
    {
        m_signature = sig;
        m_signatureMethod = null;
        m_canonicalizationMethod = null;
        m_references = null;
        m_origDigest = null;
    }
    
    /** 
     * Creates new SignedInfo 
     * @param sig parent SignatureXADES_IT reference
     * @param signatureMethod signature method uri
     * @param canonicalizationMethod xml canonicalization method uri
     * throws SignedODFDocumentException_IT
     */
    public SignedInfoXADES_IT(SignatureXADES_IT sig, String signatureMethod, String canonicalizationMethod) 
        throws SignedODFDocumentException_IT
    {
        m_signature = sig;
        setSignatureMethod(signatureMethod);
        setCanonicalizationMethod(canonicalizationMethod);
        m_references = null;
        m_origDigest = null;
    }
    
    /**
     * Accessor for signature attribute
     * @return value of signature attribute
     */
    public SignatureXADES_IT getSignature() {
        return m_signature;
    }
    
    /**
     * Mutator for signature attribute
     * @param sig new value for signature attribute
     */    
    public void setSignature(SignatureXADES_IT sig) 
    {
        m_signature = sig;
    }
    
    /**
     * Accessor for origDigest attribute
     * @return value of origDigest attribute
     */
    public byte[] getOrigDigest() {
        return m_origDigest;
    }
    
    /**
     * Mutator for origDigest attribute
     * @param str new value for origDigest attribute
     */    
    public void setOrigDigest(byte[] data) 
    {
        m_origDigest = data;
    }

    /**
     * Accessor for signatureMethod attribute
     * @return value of signatureMethod attribute
     */
    public String getSignatureMethod() {
        return m_signatureMethod;
    }
    
    /**
     * Mutator for signatureMethod attribute
     * @param str new value for signatureMethod attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setSignatureMethod(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateSignatureMethod(str);
        if(ex != null)
            throw ex;
        m_signatureMethod = str;
    }
    
    /**
     * Helper method to validate a signature method
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateSignatureMethod(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null || !str.equals(SignedODFDocument_IT.RSA_SHA1_SIGNATURE_METHOD))
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_SIGNATURE_METHOD, 
                "Currently supports only RSA-SHA1 signatures", null);
        return ex;
    }

    /**
     * Accessor for canonicalizationMethod attribute
     * @return value of canonicalizationMethod attribute
     */
    public String getCanonicalizationMethod() {
        return m_canonicalizationMethod;
    }
    
    /**
     * Mutator for canonicalizationMethod attribute
     * @param str new value for canonicalizationMethod attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setCanonicalizationMethod(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateCanonicalizationMethod(str);
        if(ex != null)
            throw ex;
        m_canonicalizationMethod = str;
    }
    
    /**
     * Helper method to validate a signature method
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateCanonicalizationMethod(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null || !str.equals(SignedODFDocument_IT.CANONICALIZATION_METHOD_20010315))
            ex= new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_CANONICALIZATION_METHOD, 
                "Currently supports only Canonical XML 1.0", null);
        return ex;
    }
    
    /**
     * Returns the count of ReferenceXADES_IT objects
     * @return count of ReferenceXADES_IT objects
     */
    public int countReferences() {
        return ((m_references == null) ? 0 : m_references.size());
    }
    
    /**
     * Adds a new reference object
     * @param ref ReferenceXADES_IT object to add
     */
    public void addReference(ReferenceXADES_IT ref) 
    {
        if(m_references == null)
            m_references = new ArrayList();
        m_references.add(ref);
    }
    
    /**
     * Returns the desired Reference object
     * @param idx index of the Reference object
     * @return desired Reference object
     */
    public ReferenceXADES_IT getReference(int idx) {
        return (ReferenceXADES_IT)m_references.get(idx);
    }
    
    /**
     * Returns the desired ReferenceXADES_IT object
     * @param df DataFile_IT whose digest we are searching
     * @return desired ReferenceXADES_IT object
     */
    public ReferenceXADES_IT getReferenceForDataFile(DataFile_IT df) {
        ReferenceXADES_IT ref = null;
        for(int i = 0; (m_references != null) && (i < m_references.size()); i++) {
            ReferenceXADES_IT r1 = (ReferenceXADES_IT)m_references.get(i);
            if(r1.getUri().equals("#" + df.getId())) {
                ref = r1;
                break;
            }
        }
        return ref;
    }
    
    /**
     * Returns the desired ReferenceXADES_IT object
     * @param sp SignedProperties whose digest we are searching
     * @return desired ReferenceXADES_IT object
     */
    public ReferenceXADES_IT getReferenceForSignedProperties(SignedPropertiesXADES_IT sp) {
        ReferenceXADES_IT ref = null;
        for(int i = 0; (m_references != null) && (i < m_references.size()); i++) {
            ReferenceXADES_IT r1 = (ReferenceXADES_IT)m_references.get(i);
            if(r1.getUri().equals("#" + sp.getId())) {
                ref = r1;
                break;
            }
        }
        return ref;
    }

    /**
     * Returns the last ReferenceXADES_IT object
     * @return desired ReferenceXADES_IT object
     */
    public ReferenceXADES_IT getLastReference() {
        return (ReferenceXADES_IT)m_references.get(m_references.size()-1);
    }
    
    /**
     * Helper method to validate references
     * @return exception or null for ok
     */
    private ArrayList validateReferences()
    {
        ArrayList errs = new ArrayList();
        if(countReferences() < 2) {
            errs.add(new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_NO_REFERENCES, 
                "At least 2 References are required!", null));
        } else {
            for(int i = 0; i < countReferences(); i++) {
                ReferenceXADES_IT ref = getReference(i);
                ArrayList e = ref.validate();
                if(!e.isEmpty())
                    errs.addAll(e);
            }
        }
        return errs;
    }
    
    /**
     * Helper method to validate the whole
     * SignedInfo object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedODFDocumentException_IT ex = validateSignatureMethod(m_signatureMethod);
        if(ex != null)
            errs.add(ex);
        ex = validateCanonicalizationMethod(m_canonicalizationMethod);
        if(ex != null)
            errs.add(ex);
        ArrayList e = validateReferences();
        if(!e.isEmpty())
            errs.addAll(e);                
        return errs;
    }
    
    /**
     * Calculates the digest of SignedInfo block
     * If the user has set origDigest attribute
     * which is allways done when reading the XML file,
     * then this digest is returned otherwise a new digest
     * is calculated.
     * @return SignedInfo block digest
     */
    public byte[] calculateDigest()
        throws SignedODFDocumentException_IT
    {
    	if(m_origDigest == null) {
        	CanonicalizationFactory_IT canFac = ConfigManager_IT.
                    instance().getCanonicalizationFactory();
        	byte[] tmp = canFac.canonicalize(toXML(),  
                    SignedODFDocument_IT.CANONICALIZATION_METHOD_20010315);
        	return SignedODFDocument_IT.digest(tmp);
    	}
    	else
    		return m_origDigest;
    }
    
    /**
     * Converts the SignedInfo to XML form
     * @return XML representation of SignedInfo
     */
    public byte[] toXML()
        throws SignedODFDocumentException_IT
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
            bos.write("<SignedInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">\n".getBytes());        
            bos.write("<CanonicalizationMethod Algorithm=\"".getBytes());
            bos.write(m_canonicalizationMethod.getBytes());
            bos.write("\">\n</CanonicalizationMethod>\n".getBytes());
            bos.write("<SignatureMethod Algorithm=\"".getBytes());
            bos.write(m_signatureMethod.getBytes());
            bos.write("\">\n</SignatureMethod>\n".getBytes());
            for(int i = 0; (m_references != null) && (i < m_references.size()); i++) {
                ReferenceXADES_IT ref = (ReferenceXADES_IT)m_references.get(i);
                bos.write(ref.toXML());
                bos.write("\n".getBytes());
            }
            bos.write("</SignedInfo>".getBytes());
        } catch(IOException ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of SignedInfo
     * @return SignedInfo string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }    
}
