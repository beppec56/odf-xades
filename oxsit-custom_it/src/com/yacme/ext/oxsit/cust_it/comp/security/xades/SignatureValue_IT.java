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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/SignatureValue_IT.java.
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

/**
 * Models the SignatureValue element of
 * XML-DSIG
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class SignatureValue_IT implements Serializable {
    /** signature value id */
    private String m_id;
    /** actual signature value data */
    private byte[] m_value;
    
    /** RSA signatures have 128 bytes */
    public static final int SIGNATURE_VALUE_LENGTH = 128;
    
    /** 
     * Creates new SignatureValue 
     */
    public SignatureValue_IT() {
        m_id = null;
        m_value = null;
    }
    
    /** 
     * Creates new SignatureValue 
     * @param id SignatureValue id
     * @param value actual RSA signature value
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public SignatureValue_IT(String id, byte[] value)
        throws SignedODFDocumentException_IT
    {
        setId(id);
        setValue(value);
    }

    /** 
     * Creates new SignatureValue 
     * @param id SignatureValue id
     * @param value actual RSA signature value
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public SignatureValue_IT(SignatureXADES_IT sig, byte[] value)
        throws SignedODFDocumentException_IT
    {
        setId(sig.getId() + "-SIG");
        setValue(value);
    }
    
    /**
     * Accessor for id attribute
     * @return value of id attribute
     */
    public String getId() {
        return m_id;
    }
    
    /**
     * Mutator for id attribute
     * @param str new value for id attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setId(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateId(str);
        if(ex != null)
            throw ex;
        m_id = str;
    }
    
    /**
     * Helper method to validate an id
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateId(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_SIGNATURE_VALUE_ID, 
                "Id is a required attribute", null);
        return ex;
    }

    /**
     * Accessor for value attribute
     * @return value of value attribute
     */
    public byte[] getValue() {
        return m_value;
    }
    
    /**
     * Mutator for value attribute
     * @param str new value for value attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setValue(byte[] data) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateValue(data);
        if(ex != null)
            throw ex;
        m_value = data;
    }
    
    /**
     * Helper method to validate a signature value
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateValue(byte[] value)
    {
        SignedODFDocumentException_IT ex = null;
        if(value == null || value.length < SIGNATURE_VALUE_LENGTH)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_SIGNATURE_VALUE_ID, 
                "RSA signature value must be at least 128 bytes", null);
        return ex;
    }

    /**
     * Helper method to validate the whole
     * SignatureValue object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        // VS: 2.3.24 - fix to allowe SignatureValue without Id atribute
		SignedODFDocumentException_IT ex = null; //validateId(m_id);
        if(ex != null)
            errs.add(ex);
        ex = validateValue(m_value);
        if(ex != null)
            errs.add(ex);
        return errs;
    }
    
    /**
     * Converts the SignatureValue to XML form
     * @return XML representation of SignatureValue
     */
    public byte[] toXML()
        throws SignedODFDocumentException_IT
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
            bos.write(ConvertUtils.str2data("<SignatureValue"));
            // VS: 2.3.24 - fix to allowe SignatureValue without Id atribute
			if(m_id != null) {
            	bos.write(ConvertUtils.str2data(" Id=\""));
            	bos.write(ConvertUtils.str2data(m_id));
                bos.write(ConvertUtils.str2data("\""));
            }
            bos.write(ConvertUtils.str2data(">"));
            bos.write(ConvertUtils.str2data(Base64Util.encode(m_value, 64)));
            bos.write(ConvertUtils.str2data("</SignatureValue>"));
        } catch(IOException ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of SignatureValue
     * @return SignatureValue string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }

}
