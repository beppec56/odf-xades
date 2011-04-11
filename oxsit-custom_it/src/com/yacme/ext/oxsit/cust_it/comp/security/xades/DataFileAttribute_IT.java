/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents and additional DataFile
 * attribute. All DataFile attributes 
 * will be signed.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class DataFileAttribute_IT implements Serializable
{
    /** attribute name */
    private String m_name;
    /** attribute value */
    private String m_value;
    
    /** 
     * Creates new DataFileAttribute 
     * @param name attribute name
     * @param value attribute value
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public DataFileAttribute_IT(String name, String value)
        throws SignedODFDocumentException_IT
    {
        setName(name);
        setValue(value);
    }
    
    /**
     * Accessor for name attribute
     * @return value of name attribute
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Mutator for name attribute
     * @param str new value for name attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setName(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateName(str);
        if(ex != null)
            throw ex;
        m_name = str;
    }
    
    /**
     * Helper method to validate attribute name
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateName(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_DATA_FILE_ATTR_NAME, 
                "Attribute name is required", null);
        return ex;
    }

    /**
     * Accessor for value attribute
     * @return value of value attribute
     */
    public String getValue() {
        return m_value;
    }
    
    /**
     * Mutator for value attribute
     * @param str new value for value attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setValue(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateValue(str);
        if(ex != null)
            throw ex;
        m_value = str;
    }
    
    /**
     * Helper method to validate attribute value
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateValue(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_DATA_FILE_ATTR_VALUE, 
                "Attribute value is required", null);
        return ex;
    }
    
    /**
     * Helper method to validate the whole
     * DataFileAttribute object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedODFDocumentException_IT ex = validateName(m_name);
        if(ex != null)
            errs.add(ex);
        ex = validateValue(m_value);
        if(ex != null)
            errs.add(ex);
        return errs;
    }
    
    /**
     * Converts the SignedInfo to XML form
     * @return XML representation of SignedInfo
     */
    public String toXML()
        throws SignedODFDocumentException_IT
    {
        StringBuffer sb = new StringBuffer(m_name);
        sb.append("=\"");
        sb.append(m_value);
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Returns the stringified form of SignedInfo
     * @return SignedInfo string representation
     */
    public String toString() {
        String str = null;
        try {
            str = toXML();
        } catch(Exception ex) {}
        return str;
    }
}
