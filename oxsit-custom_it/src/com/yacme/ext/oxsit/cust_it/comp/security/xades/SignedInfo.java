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

import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;

/**
 * @author beppe
 *
 */
public class SignedInfo implements Serializable {
    /** reference to parent Signature object */
    private Signature m_signature;
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
     * @param sig parent Signature reference
     */
    public SignedInfo(Signature sig) 
    {
        m_signature = sig;
        m_signatureMethod = null;
        m_canonicalizationMethod = null;
        m_references = null;
        m_origDigest = null;
    }
    
    /** 
     * Creates new SignedInfo 
     * @param sig parent Signature reference
     * @param signatureMethod signature method uri
     * @param canonicalizationMethod xml canonicalization method uri
     * throws SignedDocException
     */
    public SignedInfo(Signature sig, String signatureMethod, String canonicalizationMethod) 
        throws SignedDocException
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
    public Signature getSignature() {
        return m_signature;
    }
    
    /**
     * Mutator for signature attribute
     * @param sig new value for signature attribute
     */    
    public void setSignature(Signature sig) 
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
     * @throws SignedDocException for validation errors
     */    
    public void setSignatureMethod(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateSignatureMethod(str);
        if(ex != null)
            throw ex;
        m_signatureMethod = str;
    }
    
    /**
     * Helper method to validate a signature method
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateSignatureMethod(String str)
    {
        SignedDocException ex = null;
        //ROB
        if(str == null || !str.equals(SignedDoc.RSA_SHA256_SIGNATURE_METHOD))
            ex = new SignedDocException(SignedDocException.ERR_SIGNATURE_METHOD, 
                "Currently supports only RSA-SHA256 signatures", null);
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
     * @throws SignedDocException for validation errors
     */    
    public void setCanonicalizationMethod(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateCanonicalizationMethod(str);
        if(ex != null)
            throw ex;
        m_canonicalizationMethod = str;
    }
    
    /**
     * Helper method to validate a signature method
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateCanonicalizationMethod(String str)
    {
        SignedDocException ex = null;
        if(str == null || !str.equals(SignedDoc.CANONICALIZATION_METHOD_20010315))
            ex= new SignedDocException(SignedDocException.ERR_CANONICALIZATION_METHOD, 
                "Currently supports only Canonical XML 1.0", null);
        return ex;
    }
    
    /**
     * Returns the count of Reference objects
     * @return count of Reference objects
     */
    public int countReferences() {
        return ((m_references == null) ? 0 : m_references.size());
    }
    
    /**
     * Adds a new reference object
     * @param ref Reference object to add
     */
    public void addReference(Reference ref) 
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
    public Reference getReference(int idx) {
        return (Reference)m_references.get(idx);
    }
    
    /**
     * Returns the desired Reference object
     * @param df DataFile whose digest we are searching
     * @return desired Reference object
     */
    public Reference getReferenceForDataFile(DataFile df) {
        Reference ref = null;
        for(int i = 0; (m_references != null) && (i < m_references.size()); i++) {
            Reference r1 = (Reference)m_references.get(i);
            if(r1.getUri().equals(/*"#" +*/ df.getId())) {
                ref = r1;
                break;
            }
        }
        return ref;
    }
    
    /**
     * Returns the desired Reference object
     * @param sp SignedProperties whose digest we are searching
     * @return desired Reference object
     */
    public Reference getReferenceForSignedProperties(SignedProperties sp) {
        Reference ref = null;
        for(int i = 0; (m_references != null) && (i < m_references.size()); i++) {
            Reference r1 = (Reference)m_references.get(i);
            if(r1.getUri().equals("#" + sp.getId())) {
                ref = r1;
                break;
            }
        }
        return ref;
    }

    /**
     * Returns the last Reference object
     * @return desired Reference object
     */
    public Reference getLastReference() {
        return (Reference)m_references.get(m_references.size()-1);
    }
    
    /**
     * Helper method to validate references
     * @return exception or null for ok
     */
    private ArrayList validateReferences()
    {
        ArrayList errs = new ArrayList();
        if(countReferences() < 2) {
            errs.add(new SignedDocException(SignedDocException.ERR_NO_REFERENCES, 
                "At least 2 References are required!", null));
        } else {
            for(int i = 0; i < countReferences(); i++) {
                Reference ref = getReference(i);
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
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedDocException ex = validateSignatureMethod(m_signatureMethod);
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
        throws SignedDocException
    {
    	if(m_origDigest == null) {
        	CanonicalizationFactory canFac = ConfigManager.
                    instance().getCanonicalizationFactory();
        	byte[] tmp = canFac.canonicalize(toXML(),  
                    SignedDoc.CANONICALIZATION_METHOD_20010315);
        	return SignedDoc.digest(tmp);
    	}
    	else
    		return m_origDigest;
    }
    
    /**
     * Converts the SignedInfo to XML form
     * @return XML representation of SignedInfo
     */
    public byte[] toXML()
        throws SignedDocException
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
                Reference ref = (Reference)m_references.get(i);
                bos.write(ref.toXML());
                bos.write("\n".getBytes());
            }
            bos.write("</SignedInfo>".getBytes());
        } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
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
