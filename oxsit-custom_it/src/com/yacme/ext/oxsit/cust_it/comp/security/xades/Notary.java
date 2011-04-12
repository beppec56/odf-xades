/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Models an OCSP confirmation of the
 * validity of a given signature in the
 * given context.
 * @author  Veiko Sinivee
 * @version 1.0
 * FIXME: needs to be adapted to IT env, where the OCSP usually doesn't work, due to lack of support by the CAs
 */
public class Notary {
    /** notary id (in XML) */
    private String m_id;
    /** OCSP response data */
    private byte[] m_ocspResponseData;
    /** OCSP responder id */
    private String m_responderId;
    /** response production timestamp */
    private Date m_producedAt;
    /** certificate serial number used for this notary */
    private String m_certNr;
    
    /** 
     * Creates new Notary and 
     * initializes everything to null
     */
    public Notary() {
        m_ocspResponseData = null;
        m_id = null;
        m_responderId = null;
        m_producedAt = null;
        m_certNr = null;
    }

    /** 
     * Creates new Notary and 
     * @param id new Notary id
     * @param resp OCSP response data
     */
    public Notary(String id, byte[] resp, String respId, Date prodAt) 
    {
        m_ocspResponseData = resp;
        m_id = id;
        m_responderId = respId;
        m_producedAt = prodAt;
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
     * @throws SignedDocException for validation errors
     */    
    public void setId(String str) 
        //throws SignedDocException
    {
        //SignedDocException ex = validateId(str);
        //if(ex != null)
        //    throw ex;
        m_id = str;
    }
    
    /**
     * Accessor for certNr attribute
     * @return value of certNr attribute
     */
    public String getCertNr() {
        return m_certNr;
    }
    
    /**
     * Mutator for certNr attribute
     * @param nr new value of certNr attribute
     */
    public void setCertNr(String nr) {
        m_certNr = nr;
    }
    
    /**
     * Accessor for producedAt attribute
     * @return value of producedAt attribute
     */
    public Date getProducedAt()
    {
        return m_producedAt;
    }
    
    /**
     * Mutator for producedAt attribute
     * @param dt new value for producedAt attribute
     */    
    public void setProducedAt(Date dt)
    {
        m_producedAt = dt;
    }

    /**
     * Accessor for responderId attribute
     * @return value of responderId attribute
     */
    public String getResponderId()
    {
        return m_responderId;
    }
    
    /**
     * Mutator for responderId attribute
     * @param str new value for responderId attribute
     */    
    public void setResponderId(String str)
    {
        m_responderId = str;
    }
    
    /**
     * Mutator for ocspResponseData attribute
     * @param data new value for ocspResponseData attribute
     */
    public void setOcspResponseData(byte[] data)
    {
        m_ocspResponseData = data;
    }
    
    /**
     * Accessor for ocspResponseData attribute
     * @return value of ocspResponseData attribute
     */
    public byte[] getOcspResponseData()
    {
        return m_ocspResponseData;
    }
    
    /**
     * Helper method to validate the whole
     * SignedProperties object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        
        return errs;
    }
    
    /**
     * Converts the Notary to XML form
     * @return XML representation of Notary
     */
    public byte[] toXML(String ver)
        throws SignedDocException
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
            bos.write(ConvertUtils.str2data("<RevocationValues>"));            
            if(ver.equals(SignedDoc.VERSION_1_3))
            	bos.write(ConvertUtils.str2data("<OCSPValues>"));
            bos.write(ConvertUtils.str2data("<EncapsulatedOCSPValue Id=\""));
            bos.write(ConvertUtils.str2data(m_id));
            bos.write(ConvertUtils.str2data("\">\n"));
            bos.write(ConvertUtils.str2data(Base64Util.encode(m_ocspResponseData, 64)));
            bos.write(ConvertUtils.str2data("</EncapsulatedOCSPValue>\n"));
            if(ver.equals(SignedDoc.VERSION_1_3))
            	bos.write(ConvertUtils.str2data("</OCSPValues>"));
            bos.write(ConvertUtils.str2data("</RevocationValues>"));
        } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of Notary
     * @return Notary string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML(SignedDoc.VERSION_1_3));
        } catch(Exception ex) { //cannot throw any exception!!!
        }
        return str;
    }    
}
