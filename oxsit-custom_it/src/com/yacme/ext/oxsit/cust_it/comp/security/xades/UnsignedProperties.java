/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.NotaryFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;

/**
 * Models the unsigned properties of
 * a signature.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class UnsignedProperties implements Serializable {

    /** signature reference */
    private Signature m_signature;
    /** CompleteCertificateRefs object */
    private CompleteCertificateRefs m_certRefs;
    /** CompleteRevocationRefs object */
    private CompleteRevocationRefs m_revRefs;
    /** Notary object */
    private Notary m_notary;
    
    /** 
     * Creates new UsignedProperties 
     * Initializes everything to null
     * @param sig signature reference
     */
    public UnsignedProperties(Signature sig) {
        m_signature = sig;
        m_certRefs = null;
        m_revRefs = null;
        m_notary = null;
    }
    
    /** 
     * Creates new UsignedProperties 
     * @param sig signature reference
     * @param crefs responders cert digest & info
     * @param rrefs OCSP response digest & info
     * @param rcert responders cert
     * @param not OCSP response
     */
    public UnsignedProperties(Signature sig, CompleteCertificateRefs crefs,
        CompleteRevocationRefs rrefs, X509Certificate rcert, Notary not) 
        throws SignedDocException
    {
        m_signature = sig;
        setCompleteCertificateRefs(crefs);
        setCompleteRevocationRefs(rrefs);
        setRespondersCertificate(rcert);
        setNotary(not);
    }
    
    /**
     * Accessor for completeCertificateRefs attribute
     * @return value of completeCertificateRefs attribute
     */
    public CompleteCertificateRefs getCompleteCertificateRefs() {
        return m_certRefs;
    }
    
    /**
     * Accessor for signature attribute
     * @return value of signature attribute
     */
    public Signature getSignature()
    {
    	return m_signature;
    }
    
    /**
     * Mutator for completeCertificateRefs attribute
     * @param str new value for completeCertificateRefs attribute
     * @throws SignedDocException for validation errors
     */    
    public void setCompleteCertificateRefs(CompleteCertificateRefs crefs) 
        throws SignedDocException
    {
        //ArrayList errs = crefs.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_certRefs = crefs;
    }
    
    /**
     * Accessor for completeRevocationRefs attribute
     * @return value of completeRevocationRefs attribute
     */
    public CompleteRevocationRefs getCompleteRevocationRefs() {
        return m_revRefs;
    }
    
    /**
     * Mutator for completeRevocationRefs attribute
     * @param str new value for completeRevocationRefs attribute
     * @throws SignedDocException for validation errors
     */    
    public void setCompleteRevocationRefs(CompleteRevocationRefs refs) 
        throws SignedDocException
    {
        //ArrayList errs = refs.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_revRefs = refs;
    }
    
    /**
     * Accessor for respondersCertificate attribute
     * @return value of respondersCertificate attribute
     */
    public X509Certificate getRespondersCertificate() {
    	X509Certificate cert = null;
    	//System.out.println("UnsignedProp sig: " + ((m_signature == null) ? "NULL" : "OK"));
    	if(m_signature != null) {
    		CertValue cval = m_signature.getCertValueOfType(CertValue.CERTVAL_TYPE_RESPONDER);
    		//System.out.println("UnsignedProp cval: " + ((cval == null) ? "NULL" : "OK"));
        	if(cval != null)
    			cert = cval.getCert();
    	}
        return cert;
    }
    
    /**
     * Mutator for respondersCertificate attribute
     * @param cert new value for respondersCertificate attribute
     * @throws SignedDocException for validation errors
     */    
    public void setRespondersCertificate(X509Certificate cert) 
        throws SignedDocException
    {
        SignedDocException ex = validateRespondersCertificate(cert);
        if(ex != null)
            throw ex;
        if(m_signature != null) {
    		CertValue cval = m_signature.getOrCreateCertValueOfType(CertValue.CERTVAL_TYPE_RESPONDER);
    		cval.setCert(cert);
    	}
    }
    
    /**
     * Helper method to validate a responders cert
     * @param cert input data
     * @return exception or null for ok
     */
    private SignedDocException validateRespondersCertificate(X509Certificate cert)
    {
        SignedDocException ex = null;
        if(cert == null)
            ex = new SignedDocException(SignedDocException.ERR_RESPONDERS_CERT, 
                "Notarys certificate is required", null);
        return ex;
    }
    
    /**
     * Accessor for notary attribute
     * @return value of notary attribute
     */
    public Notary getNotary() {
        return m_notary;
    }
    
    /**
     * Mutator for notary attribute
     * @param str new value for notary attribute
     * @throws SignedDocException for validation errors
     */    
    public void setNotary(Notary not) 
        throws SignedDocException
    {
        //ArrayList errs = not.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_notary = not;
    }
    

    /** 
     * Verifies this confirmation
     * @param sdoc parent doc object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList<SignedDocException> verify(SignedDoc sdoc)
    {
        ArrayList errs = new ArrayList();
        // verify notary certs serial number using CompleteCertificateRefs        
        X509Certificate cert = getRespondersCertificate();
        //System.out.println("Responders cert: " + getRespondersCertificate().getSerialNumber() +
        //    	           " complete cert refs nr: " + m_certRefs.getCertSerial());
        if(cert == null) {
        	errs.add(new SignedDocException(SignedDocException.ERR_RESPONDERS_CERT, 
                    "No notarys certificate!", null));
        	return errs;
        }
        if(cert != null && !cert.getSerialNumber().
            equals(m_certRefs.getCertSerial())) {
            errs.add(new SignedDocException(SignedDocException.ERR_RESPONDERS_CERT, 
                "Wrong notarys certificate!", null));
        }
        // verify notary certs digest using CompleteCertificateRefs
        try {
            byte[] digest = SignedDoc.digest(cert.getEncoded());
            if(!SignedDoc.compareDigests(digest, m_certRefs.getCertDigestValue()))
                errs.add(new SignedDocException(SignedDocException.ERR_RESPONDERS_CERT, 
                "Notary certificates digest doesn't match!", null));
        } catch(SignedDocException ex) {
            errs.add(ex);
        } catch(Exception ex) {
            errs.add(new SignedDocException(SignedDocException.ERR_RESPONDERS_CERT, 
                "Error calculating notary certificate digest!", null));
        }
        // verify notarys digest using CompleteRevocationRefs
        try {
            byte[] ocspData = m_notary.getOcspResponseData();
            //System.out.println("OCSP data len: " + ocspData.length); 
            byte[] digest1 = SignedDoc.digest(ocspData);
            //System.out.println("Calculated digest: " + Base64Util.encode(digest1, 0));
            byte[] digest2 = m_revRefs.getDigestValue();
            //System.out.println("Real digest: " + Base64Util.encode(digest2, 0));
            if(!SignedDoc.compareDigests(digest1, digest2))
                errs.add(new SignedDocException(SignedDocException.ERR_NOTARY_DIGEST, 
                "Notarys digest doesn't match!", null));
        } catch(SignedDocException ex) {
            errs.add(ex);
        } 
        // verify notary status
        try {
            NotaryFactory notFac = ConfigManager.instance().getNotaryFactory();
            notFac.parseAndVerifyResponse(m_signature, m_notary);
        } catch(SignedDocException ex) {
            errs.add(ex);
        } 
        return errs;
    }
    
    
    /**
     * Helper method to validate the whole
     * UnsignedProperties object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedDocException ex = null;
        X509Certificate cert = getRespondersCertificate();
        if(cert == null) 
        	ex = validateRespondersCertificate(cert);
        if(ex != null)
            errs.add(ex);        
        ArrayList e = null;
        if(m_certRefs != null) {
            e = m_certRefs.validate();
            if(!e.isEmpty())
                errs.addAll(e);
        }
        if(m_revRefs != null) {
            e = m_revRefs.validate();
            if(!e.isEmpty())
                errs.addAll(e);
        }
        // notary ???
        
        return errs;
    }
    
    /**
     * Converts the UnsignedProperties to XML form
     * @return XML representation of UnsignedProperties
     */
    public byte[] toXML()
        throws SignedDocException
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();
        try {
        	if(m_signature.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)) {
        		bos.write(ConvertUtils.str2data("<UnsignedProperties>"));
        	} else {
            	bos.write(ConvertUtils.str2data("<UnsignedProperties Target=\"#"));
            	bos.write(ConvertUtils.str2data(m_signature.getId()));
            	bos.write(ConvertUtils.str2data("\">"));
        	}
            bos.write(ConvertUtils.str2data("\n<UnsignedSignatureProperties>"));
            if(m_certRefs != null)
                bos.write(m_certRefs.toXML());
            if(m_revRefs != null) {
                bos.write(m_revRefs.toXML());
                bos.write(ConvertUtils.str2data("\n"));
            }
            bos.write(ConvertUtils.str2data("<CertificateValues>\n"));
            for(int i = 0; i < m_signature.countCertValues(); i++) {
              	CertValue cval = m_signature.getCertValue(i);
              	if(cval.getType() != CertValue.CERTVAL_TYPE_SIGNER)
              		bos.write(cval.toXML());
            }
            bos.write(ConvertUtils.str2data("</CertificateValues>"));
            if(m_notary != null) {
                bos.write(ConvertUtils.str2data("\n"));
                bos.write(m_notary.toXML(m_signature.getSignedDoc().getVersion()));
            }
            bos.write(ConvertUtils.str2data("</UnsignedSignatureProperties>\n</UnsignedProperties>"));
        } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of UnsignedProperties
     * @return UnsignedProperties string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }     
}
