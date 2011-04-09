/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

import org.bouncycastle.tsp.TimeStampResponse;


/**
 * Models the ETSI timestamp element(s)
 * Holds timestamp info and TS_RESP response.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class TimestampInfo_IT {
	/** elements Id atribute */
    private String m_id;
    /** parent object - SignatureXADES_IT ref */
    private SignatureXADES_IT m_signature;
    /** timestamp type */
    private int m_type;
    /** Include sublements */
    private ArrayList m_includes;
    /** timestamp response */
    private TimeStampResponse m_tsResp;
    /** real hash calculated over the corresponding xml block */
    private byte[] m_hash;
    
    /** possible values for type atribute */
    public static final int TIMESTAMP_TYPE_UNKNOWN = 0;
    public static final int TIMESTAMP_TYPE_ALL_DATA_OBJECTS = 1;
    public static final int TIMESTAMP_TYPE_INDIVIDUAL_DATA_OBJECTS = 2;
    public static final int TIMESTAMP_TYPE_SIGNATURE = 3;
    public static final int TIMESTAMP_TYPE_SIG_AND_REFS = 4;
    public static final int TIMESTAMP_TYPE_REFS_ONLY = 5;
    public static final int TIMESTAMP_TYPE_ARCHIVE = 6;
    
    /** 
     * Creates new TimestampInfo 
     * and initializes everything to null
     */
    public TimestampInfo_IT() {
    	m_id = null;
    	m_signature = null;
    	m_includes = null;
    	m_tsResp = null;
    	m_hash = null;
    	m_type = TIMESTAMP_TYPE_UNKNOWN;
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
     * Creates new TimestampInfo 
     * @param id Id atribute value
     * @param type timestamp type
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public TimestampInfo_IT(String id, int type) 
    	throws SignedODFDocumentException_IT
    {
        setId(id);
        setType(type);
        m_includes = null;
    }
    
    /**
     * Accessor for Hash attribute
     * @return value of Hash attribute
     */
    public byte[] getHash() {
        return m_hash;
    }
    
    /**
     * Mutator for Hash attribute
     * @param str new value for Hash attribute
     */    
    public void setHash(byte[] b) 
    {
        m_hash = b;
    }
    
    /**
     * Accessor for Id attribute
     * @return value of Id attribute
     */
    public String getId() {
        return m_id;
    }
    
    /**
     * Mutator for Id attribute
     * @param str new value for Id attribute
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
     * Helper method to validate Id
     * @param str input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateId(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_ID, 
                "Id atribute cannot be empty", null);
        return ex;
    }
    
    /**
     * Accessor for Type attribute
     * @return value of Type attribute
     */
    public int getType() {
        return m_type;
    }
    
    /**
     * Mutator for Type attribute
     * @param n new value for Type attribute
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
     * Helper method to validate Type
     * @param n input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateType(int n)
    {
        SignedODFDocumentException_IT ex = null;
        if(n < TIMESTAMP_TYPE_ALL_DATA_OBJECTS || n > TIMESTAMP_TYPE_ARCHIVE)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_TYPE, 
                "Invalid timestamp type", null);
        return ex;
    } 
    
    /**
     * Accessor for TimeStampResponse attribute
     * @return value of TimeStampResponse attribute
     */
    public TimeStampResponse getTimeStampResponse() {
        return m_tsResp;
    }
    
    /**
     * Mutator for TimeStampResponse attribute
     * @param tsr new value for TimeStampResponse attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setTimeStampResponse(TimeStampResponse tsr) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateTimeStampResponse(tsr);
        if(ex != null)
            throw ex;
        m_tsResp = tsr;
    }
    
    /**
     * Helper method to validate TimeStampResponse
     * @param tsr input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateTimeStampResponse(TimeStampResponse tsr)
    {
        SignedODFDocumentException_IT ex = null;
        if(tsr == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_RESP, 
                "timestamp cannot be null", null);
        return ex;
    }    
    
    /**
     * return the count of IncludeInfo_IT objects
     * @return count of IncludeInfo_IT objects
     */
    public int countIncludeInfos()
    {
        return ((m_includes == null) ? 0 : m_includes.size());
    }
    
    /**
     * Adds a new IncludeInfo_IT object
     * @param inc new object to be added
     */
    public void addIncludeInfo(IncludeInfo_IT inc)
    {
    	if(m_includes == null)
    		m_includes = new ArrayList();
    	inc.setTimestampInfo(this);
    	m_includes.add(inc);
    }
    
    /**
     * Retrieves IncludeInfo_IT element with the desired index
     * @param idx IncludeInfo_IT index
     * @return IncludeInfo_IT element or null if not found
     */
    public IncludeInfo_IT getIncludeInfo(int idx)
    {
    	if(m_includes != null && idx < m_includes.size()) {
    		return (IncludeInfo_IT)m_includes.get(idx);
    	} else
    	return null; // not found
    }
    
    /**
     * Retrieves the last IncludeInfo_IT element 
     * @return IncludeInfo_IT element or null if not found
     */
    public IncludeInfo_IT getLastIncludeInfo()
    {
    	if(m_includes != null && m_includes.size() > 0) {
    		return (IncludeInfo_IT)m_includes.get(m_includes.size()-1);
    	} else
    	return null; // not found
    }
    
    /**
     * Retrieves timestamp responses signature
     * algorithm OID.
     * @return responses signature algorithm OID
     */
    public String getAlgorithmOid()
    {
    	String oid = null;
    	if(m_tsResp != null) {
    		oid = m_tsResp.getTimeStampToken().
				getTimeStampInfo().getMessageImprintAlgOID();
    	}
    	return oid;
    }
    
    /**
     * Retrieves timestamp responses policy
     * @return responses policy
     */
    public String getPolicy()
    {
    	String oid = null;
    	if(m_tsResp != null) {
    		oid = m_tsResp.getTimeStampToken().
				getTimeStampInfo().getPolicy();
    	}
    	return oid;
    }
    
    /**
     * Retrieves timestamp issuing time
     * @return timestamp issuing time
     */
    public Date getTime()
    {
    	Date d = null;
    	if(m_tsResp != null) {
    		d = m_tsResp.getTimeStampToken().
				getTimeStampInfo().getGenTime();
    	}
    	return d;
    }
    
    /**
     * Retrieves timestamp msg-imprint digest
     * @return timestamp msg-imprint digest
     */
    public byte[] getMessageImprint()
    {
    	byte[] b = null;
    	if(m_tsResp != null) {
    		b = m_tsResp.getTimeStampToken().
				getTimeStampInfo().getMessageImprintDigest();
    	}
    	return b;
    }
    
    /**
     * Retrieves timestamp nonce
     * @return timestamp nonce
     */
    public BigInteger getNonce()
    {
    	BigInteger b = null;
    	if(m_tsResp != null) {
    		b = m_tsResp.getTimeStampToken().
				getTimeStampInfo().getNonce();
    	}
    	return b;
    }
    
    /**
     * Retrieves timestamp serial number
     * @return timestamp serial number
     */
    public BigInteger getSerialNumber()
    {
    	BigInteger b = null;
    	if(m_tsResp != null) {
    		b = m_tsResp.getTimeStampToken().
				getTimeStampInfo().getSerialNumber();
    	}
    	return b;
    }
    
    /**
     * Retrieves timestamp is-ordered atribute
     * @return timestamp is-ordered atribute
     */
    public boolean isOrdered()
    {
    	boolean b = false;
    	if(m_tsResp != null) {
    		b = m_tsResp.getTimeStampToken().
				getTimeStampInfo().isOrdered();
    	}
    	return b;
    }
   
    /**
     * Retrieves timestamp is-ordered atribute
     * @return timestamp is-ordered atribute
     */
    public String getSignerCN()
    {
    	String s = null;
    	if(m_tsResp != null) {
    		//SignerId = m_tsResp.getTimeStampToken().getSignedAttributes()
    		//org.bouncycastle.cms.CMSSignedData cms = m_tsResp.getTimeStampToken().
    		
    	}
    	return s;
    }
    
    /**
     * Helper method to validate the whole
     * TimestampInfo object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedODFDocumentException_IT ex = validateId(m_id);
        if(ex != null)
            errs.add(ex);
        ex = validateType(m_type);
        if(ex != null)
            errs.add(ex);
        return errs;
    }
    
    /**
     * Converts the TimestampInfo to XML form
     * @return XML representation of TimestampInfo
     */
    public byte[] toXML()
        throws SignedODFDocumentException_IT
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
        	switch(m_type) {
        		case TIMESTAMP_TYPE_ALL_DATA_OBJECTS:
        			bos.write(ConvertUtils.str2data("<AllDataObjectsTimeStamp Id=\""));
        			break;
        		case TIMESTAMP_TYPE_INDIVIDUAL_DATA_OBJECTS:
        			bos.write(ConvertUtils.str2data("<IndividualDataObjectsTimeStamp Id=\""));
        			break;
        	    case TIMESTAMP_TYPE_SIGNATURE:
        	    	bos.write(ConvertUtils.str2data("<SignatureTimeStamp Id=\""));
        			break;
        	    case TIMESTAMP_TYPE_SIG_AND_REFS:
        	    	bos.write(ConvertUtils.str2data("<SigAndRefsTimeStamp Id=\""));
        			break;
        	    case TIMESTAMP_TYPE_REFS_ONLY:
        	    	bos.write(ConvertUtils.str2data("<RefsOnlyTimeStamp Id=\""));
        			break;
        	    case TIMESTAMP_TYPE_ARCHIVE:
        	    	bos.write(ConvertUtils.str2data("<ArchiveTimeStamp Id=\""));
        			break;
        	}            
            bos.write(ConvertUtils.str2data(m_id));            
            bos.write(ConvertUtils.str2data("\">"));
            for(int i = 0; i < countIncludeInfos(); i++) {
            	IncludeInfo_IT inc = getIncludeInfo(i);
            	bos.write(inc.toXML());
            }
            bos.write(ConvertUtils.str2data("<EncapsulatedTimeStamp>"));
            if(m_tsResp != null)
            	bos.write(ConvertUtils.str2data(Base64Util.encode(m_tsResp.getEncoded(), 64)));
            bos.write(ConvertUtils.str2data("</EncapsulatedTimeStamp>"));            
            switch(m_type) {
    		case TIMESTAMP_TYPE_ALL_DATA_OBJECTS:
    			bos.write(ConvertUtils.str2data("</AllDataObjectsTimeStamp>"));
    			break;
    		case TIMESTAMP_TYPE_INDIVIDUAL_DATA_OBJECTS:
    			bos.write(ConvertUtils.str2data("</IndividualDataObjectsTimeStamp>"));
    			break;
    	    case TIMESTAMP_TYPE_SIGNATURE:
    	    	bos.write(ConvertUtils.str2data("</SignatureTimeStamp>"));
    			break;
    	    case TIMESTAMP_TYPE_SIG_AND_REFS:
    	    	bos.write(ConvertUtils.str2data("</SigAndRefsTimeStamp>"));
    			break;
    	    case TIMESTAMP_TYPE_REFS_ONLY:
    	    	bos.write(ConvertUtils.str2data("</RefsOnlyTimeStamp>"));
    			break;
    	    case TIMESTAMP_TYPE_ARCHIVE:
    	    	bos.write(ConvertUtils.str2data("</ArchiveTimeStamp>"));
    			break;
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
