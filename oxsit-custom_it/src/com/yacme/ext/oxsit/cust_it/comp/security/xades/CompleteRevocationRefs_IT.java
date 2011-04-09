/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Models the ETSI CompleteRevocationRefs element
 * This contains some data from the OCSP response
 * and it's digest
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class CompleteRevocationRefs_IT implements Serializable {
    /** <OCSPIdentifier> URI attribute */
    private String m_uri;
    /** <ResponderId> element */
    private String m_responderId;
    /** ProducedAt element */
    private Date m_producedAt;
    /** digesta lgorithm uri/id */
    private String m_digestAlgorithm;
    /** digest value */
    private byte[] m_digestValue;
    /** parent object - UnsignedProperties ref */
    private UnsignedProperties_IT m_unsignedProps;
    
    /** 
     * Creates new CompleteRevocationRefs 
     * Initializes everything to null
     */
    public CompleteRevocationRefs_IT() {
        m_uri = null;
        m_responderId = null;
        m_producedAt = null;
        m_digestAlgorithm = null;
        m_digestValue = null;
        m_unsignedProps = null;
    }

    /** 
     * Creates new CompleteRevocationRefs 
     * @param uri notary uri value
     * @param respId responder id
     * @param producedAt OCSP producedAt timestamp
     * @param digAlg notary digest algorithm
     * @param digest notary digest
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CompleteRevocationRefs_IT(String uri, String respId,
        Date producedAt, String digAlg, byte[] digest) 
        throws SignedODFDocumentException_IT
    {
        setUri(uri);
        setResponderId(respId);
        setProducedAt(producedAt);
        setDigestAlgorithm(digAlg);
        setDigestValue(digest);
    }

    /** 
     * Creates new CompleteRevocationRefs 
     * by using data from an existing Notary object
     * @param not Notary object
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CompleteRevocationRefs_IT(Notary_IT not) 
        throws SignedODFDocumentException_IT
    {
        setUri("#" + not.getId());
        setResponderId(not.getResponderId());
        setProducedAt(not.getProducedAt());
        setDigestAlgorithm(SignedODFDocument_IT.SHA1_DIGEST_ALGORITHM);
        byte[] digest = null;
        try {
            byte[] ocspData = not.getOcspResponseData();
            digest = SignedODFDocument_IT.digest(ocspData);
            //System.out.println("OCSP data len: " + ocspData.length); 
            //System.out.println("Calculated digest: " + Base64Util.encode(digest, 0));            
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_CALCULATE_DIGEST); 
        }
        setDigestValue(digest);
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
     * Accessor for uri attribute
     * @return value of uri attribute
     */
    public String getUri() {
        return m_uri;
    }
    
    /**
     * Mutator for uri attribute
     * @param str new value for uri attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setUri(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateUri(str);
        if(ex != null)
            throw ex;
        m_uri = str;
    }
    
    /**
     * Helper method to validate an uri
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateUri(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_REVREFS_URI, 
                "Notary uri must be in form: #<notary-id>", null);
        return ex;
    }

    /**
     * Accessor for responderId attribute
     * @return value of responderId attribute
     */
    public String getResponderId() {
        return m_responderId;
    }
    
    /**
     * Mutator for responderId attribute
     * @param str new value for responderId attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setResponderId(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateResponderId(str);
        if(ex != null)
            throw ex;
        m_responderId = str;
    }
        
    /**
     * Returns reponder-ids CN
     * @returns reponder-ids CN or null
     */
    public String getResponderCommonName() {
        String name = null;
        if(m_responderId != null) {
        	int idx1 = m_responderId.indexOf("CN=");
        	if(idx1 != -1) {
            	idx1 += 2;
            	while(idx1 < m_responderId.length() && 
            		!Character.isLetter(m_responderId.charAt(idx1)))
                	idx1++;
            	int idx2 = idx1;
            	while(idx2 < m_responderId.length() && 
            		m_responderId.charAt(idx2) != ',' && 
            		m_responderId.charAt(idx2) != '/')
                	idx2++;
            	name = m_responderId.substring(idx1, idx2);            
        	}
        }
        return name;
    }

    /**
     * Helper method to validate a ResponderId
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateResponderId(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_REVREFS_RESP_ID, 
                "ResponderId cannot be empty!", null);
        return ex;
    }

    /**
     * Accessor for producedAt attribute
     * @return value of producedAt attribute
     */
    public Date getProducedAt() {
        return m_producedAt;
    }
    
    /**
     * Mutator for producedAt attribute
     * @param str new value for producedAt attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setProducedAt(Date d) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateProducedAt(d);
        if(ex != null)
            throw ex;
        m_producedAt = d;
    }
    
    /**
     * Helper method to validate producedAt timestamp
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateProducedAt(Date d)
    {
        SignedODFDocumentException_IT ex = null;
        if(d == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_REVREFS_PRODUCED_AT, 
                "ProducedAt timestamp cannot be empty!", null);
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
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setDigestAlgorithm(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateDigestAlgorithm(str);
        if(ex != null)
            throw ex;
        m_digestAlgorithm = str;
    }
    
    /**
     * Helper method to validate a digest algorithm
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateDigestAlgorithm(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null || !str.equals(SignedODFDocument_IT.SHA1_DIGEST_ALGORITHM))
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_CERT_DIGEST_ALGORITHM, 
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
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setDigestValue(byte[] data) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateDigestValue(data);
        if(ex != null)
            throw ex;
        m_digestValue = data;
    }
 
    /**
     * Helper method to validate a digest value
     * @param data input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateDigestValue(byte[] data)
    {
        SignedODFDocumentException_IT ex = null;
        if(data == null || data.length != SignedODFDocument_IT.SHA1_DIGEST_LENGTH)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_DIGEST_LENGTH, 
                "SHA1 digest data is allways 20 bytes of length", null);
        return ex;
    }
    
    /**
     * Helper method to validate the whole
     * CompleteRevocationRefs object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedODFDocumentException_IT ex = validateUri(m_uri);
        if(ex != null)
            errs.add(ex);
        ex = validateResponderId(m_responderId);
        if(ex != null)
            errs.add(ex);
        ex = validateProducedAt(m_producedAt);
        if(ex != null)
            errs.add(ex);
        ex = validateDigestAlgorithm(m_digestAlgorithm);
        if(ex != null)
            errs.add(ex);
        ex = validateDigestValue(m_digestValue);
        if(ex != null)
            errs.add(ex);
        return errs;
    }
    
    /**
     * Converts the CompleteRevocationRefs to XML form
     * @return XML representation of CompleteRevocationRefs
     */
    public byte[] toXML()
        throws SignedODFDocumentException_IT
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
            bos.write(ConvertUtils.str2data("<CompleteRevocationRefs>\n"));
            bos.write(ConvertUtils.str2data("<OCSPRefs>\n<OCSPRef>\n"));
            bos.write(ConvertUtils.str2data("<OCSPIdentifier URI=\""));
            bos.write(ConvertUtils.str2data(m_uri));
            bos.write(ConvertUtils.str2data("\">\n<ResponderID>"));
            bos.write(ConvertUtils.str2data(m_responderId));
            bos.write(ConvertUtils.str2data("</ResponderID>\n<ProducedAt>"));
            bos.write(ConvertUtils.str2data(ConvertUtils.date2string(m_producedAt, 
            	m_unsignedProps.getSignature().getSignedDoc())));
            bos.write(ConvertUtils.str2data("</ProducedAt>\n</OCSPIdentifier>\n<DigestAlgAndValue>\n<DigestMethod Algorithm=\""));
            bos.write(ConvertUtils.str2data(m_digestAlgorithm));
            bos.write(ConvertUtils.str2data("\"></DigestMethod>\n<DigestValue>"));
            bos.write(ConvertUtils.str2data(Base64Util.encode(m_digestValue, 0)));
            bos.write(ConvertUtils.str2data("</DigestValue>\n</DigestAlgAndValue>"));
            bos.write(ConvertUtils.str2data("</OCSPRef>\n</OCSPRefs>\n"));
            bos.write(ConvertUtils.str2data("</CompleteRevocationRefs>"));        
        } catch(IOException ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of CompleteRevocationRefs
     * @return CompleteRevocationRefs string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }   
}
