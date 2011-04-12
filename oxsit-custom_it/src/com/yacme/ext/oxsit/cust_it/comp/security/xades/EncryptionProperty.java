/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;



/**
 * Contains the data of an <EncryptionProperty>
 * subelement of an <EncryptedData> object
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class EncryptionProperty  implements Serializable
{
	/** Id atribute value (optional) */
	private String m_id;
	/** Target atribute value (optional) */
	private String m_target;
	/** Name atribute value (required in this implementation) */
	private String m_name;
	/** encryption property value itself (required) */
	private String m_content;
	/** we use this property to store the original filename of the encrypted file */
	public static String ENCPROP_FILENAME = "Filename";
	/** we use this property to store the original size of encrypted file */
	public static String ENCPROP_ORIG_SIZE = "OriginalSize";
	/** we use this property to store the original mime type of encrypted file */
	public static String ENCPROP_ORIG_MIME = "OriginalMimeType";

	/**
	 * Default constructor for EncryptionProperty object.
	 * This should be used only in parser because it intializes
	 * instance variable to default values that might be an invalid object state
	*/
	public EncryptionProperty()
	{
		m_id = null;
		m_target = null;
		m_name = null;
		m_content = null;
	}
	
	/**
	 * Constructor for EncryptionProperty object
	 * @param name Name atribute value (required in this implementation)
	 * @param content encryption property value itself (required)
	 * @throws SignedDocException for validation errors
	 */
	public EncryptionProperty(String name, String content)
		throws SignedDocException
	{
		setName(name);
		setContent(content);
	}
	
	/**
	 * Constructor for EncryptionProperty object
	 * @param id Id atribute value (optional)
	 * @param target Target atribute value (optional) 
	 * @param name Name atribute value (required in this implementation)
	 * @param content encryption property value itself (required)
	 * @throws SignedDocException for validation errors
	 */
	public EncryptionProperty(String id, String target, String name, String content)
		throws SignedDocException
	{
		setId(id);
		setTarget(target);
		setName(name);
		setContent(content);
	}
	
    /**
     * Accessor for id attribute
     * @return value of Id attribute
     */
    public String getId() {
        return m_id;
    }
	
	/**
     * Mutator for Id attribute
     * @param str new value for Id attribute
     */    
    public void setId(String str) {
        m_id = str;
    }
    
    /**
     * Accessor for Target attribute
     * @return value of Target attribute
     */
    public String getTarget() {
        return m_target;
    }
	
	/**
     * Mutator for Target attribute
     * @param str new value for Target attribute
     */    
    public void setTarget(String str) {
    	m_target = str;
    }

    /**
     * Accessor for Name attribute
     * @return value of Name attribute
     */
    public String getName() {
        return m_name;
    }
	
	/**
     * Mutator for Name attribute
     * @param str new value for Name attribute
     * @throws SignedDocException for validation errors
     */    
    public void setName(String str) 
    	throws SignedDocException
    {
    	SignedDocException ex = validateName(str);
        if(ex != null)
            throw ex;
    	m_name = str;
    }

    /**
     * Helper method to validate Name atribute
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateName(String str)
    {
        SignedDocException ex = null;
        if(str == null)
            ex = new SignedDocException(SignedDocException.ERR_XMLENC_ENCPROP_NAME, 
                "Name atribute is required", null);
        return ex;
    }

    /**
     * Accessor for Content attribute
     * @return value of Content attribute
     */
    public String getContent() {
        return m_content;
    }
	
	/**
     * Mutator for Content attribute
     * @param str new value for Content attribute
     * @throws SignedDocException for validation errors
     */    
    public void setContent(String str) 
    	throws SignedDocException
    {
    	SignedDocException ex = validateContent(str);
        if(ex != null)
            throw ex;
    	m_content = str;
    }

    /**
     * Helper method to validate Content atribute
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateContent(String str)
    {
        SignedDocException ex = null;
        if(str == null)
            ex = new SignedDocException(SignedDocException.ERR_XMLENC_ENCPROP_CONTENT, 
                "content of <EncryptionProperty> element is required", null);
        return ex;
    }
    
    /**
     * Converts the KeyInfo to XML form
     * @return XML representation of KeyInfo
     */
    public byte[] toXML()
        throws SignedDocException
    {
        ByteArrayOutputStream bos = 
                new ByteArrayOutputStream();
        try {
            bos.write(ConvertUtils.str2data("<denc:EncryptionProperty"));
            if(m_id != null) 
            	bos.write(ConvertUtils.str2data(" Id=\"" + m_id + "\""));
            if(m_target != null) 
            	bos.write(ConvertUtils.str2data(" Target=\"" + m_target + "\""));
            if(m_name != null) 
            	bos.write(ConvertUtils.str2data(" Name=\"" + m_name + "\""));            
            bos.write(ConvertUtils.str2data(">"));
            if(m_name != null) 
            	bos.write(ConvertUtils.str2data(m_content));            
            bos.write(ConvertUtils.str2data("</denc:EncryptionProperty>"));
         } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }
	
    /**
     * Helper method to validate the whole
     * EncrypteionProperty object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedDocException ex = validateName(m_name);
        if(ex != null)
            errs.add(ex);
        ex = validateContent(m_content);
        if(ex != null)
            errs.add(ex);
        return errs;
    }

    /**
     * Returns the stringified form of KeyInfo
     * @return KeyInfo string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    } 
}
