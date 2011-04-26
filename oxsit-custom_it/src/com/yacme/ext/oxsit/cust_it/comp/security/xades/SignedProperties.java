/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;


/**
 * @author beppe
 *
 */
public class SignedProperties implements Serializable {
    /** signature object to which this belongs */
    private Signature m_sig;
    /** id attribute */
    private String m_id;
    /** target attribute */
    private String m_target;
    /** signing time measured by signers own computer */
    private Date m_signingTime;
    /** signers certs digest algorithm */
    private String m_certDigestAlgorithm;
    /** signers cert id */
    private String m_certId;
    /** signers certs digest data */
    private byte[] m_certDigestValue;
    /** signers certs issuer serial number */
    private BigInteger m_certSerial;
    /** signature production place */
    private SignatureProductionPlace m_address;
    /** claimed roles */
    private ArrayList m_claimedRoles;
    /** digest over the original bytes read from XML file  */
    private byte[] m_origDigest;
    
    
    /** 
     * Creates new SignedProperties. Initializes
     * everything to null
     * @param sig parent signature
     */
    public SignedProperties(Signature sig) {
        m_sig = sig;
        m_id = null;
        m_target = null;
        m_signingTime = null;
        m_certDigestAlgorithm = null;
        m_certDigestValue = null;
        m_certSerial = null;
        m_claimedRoles = null;
        m_address = null;
        m_certId = null;
        m_origDigest = null;
    }

    /** 
     * Creates new SignedProperties. 
     * @param sig parent signature
     * @param id id attribute value
     * @param target target attribute value
     * @param signingTime signing timestamp
     * @param certId signers cert id (in XML)
     * @param certDigAlg signers cert digest algorithm id/uri
     * @param digest signers cert digest value
     * @param serial signers cert serial number
     * @throws SignedDocException for validation errors
     */
    public SignedProperties(Signature sig, String id, String target, Date signingTime, 
            String certId, String certDigAlg, byte[] digest, BigInteger serial) 
        throws SignedDocException
    {
        m_sig = sig;
        setId(id);
        setTarget(target);
        setSigningTime(signingTime);
        setCertId(certId);
        setCertDigestAlgorithm(certDigAlg);
        setCertDigestValue(digest);
        setCertSerial(serial);
        m_claimedRoles = null;
        m_address = null;
        m_origDigest = null;
    }

    /** 
     * Creates new SignedProperties with default
     * values taken from signers certificate and signature
     * @param sig Signature reference
     * @param cert signers certificate
     * @param claimedRoles signers claimed roles
     * @param adr signers address
     * @throws SignedDocException for validation errors
     */
    public SignedProperties(Signature sig, X509Certificate cert,
        String[] claimedRoles, SignatureProductionPlace adr) 
        throws SignedDocException
    {
		m_sig = sig;

		setId(sig.getId() + "-SignedProperties");
		setTarget("#" + sig.getId());
		setSigningTime(new Date());
		setCertId(sig.getId() + "-CERTINFO");
		// ROB
		setCertDigestAlgorithm(SignedDoc.SHA256_DIGEST_ALGORITHM);
		try {
			setCertDigestValue(SignedDoc.digest(cert.getEncoded()));
		} catch (Exception ex) {
			SignedDocException.handleException(ex,
					SignedDocException.ERR_CALCULATE_DIGEST);
		}
		setCertSerial(cert.getSerialNumber());
		if ((claimedRoles != null) && (claimedRoles.length > 0)) {
			for (int i = 0; i < claimedRoles.length; i++)
				addClaimedRole(claimedRoles[i]);
		}
		if (adr != null)
			setSignatureProductionPlace(adr);
		m_origDigest = null;
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
        throws SignedDocException
    {
        SignedDocException ex = validateId(str);
        if(ex != null)
            throw ex;
        m_id = str;
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
     * Helper method to validate an id
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateId(String str)
    {
        SignedDocException ex = null;
        if(str == null)
            ex = new SignedDocException(SignedDocException.ERR_SIGPROP_ID, 
                "Id must be in form: <signature-id>-SignedProperties", null);
        return ex;
    }

    /**
     * Accessor for target attribute
     * @return value of target attribute
     */
    public String getTarget() {
        return m_target;
    }
    
    /**
     * Mutator for target attribute
     * @param str new value for target attribute
     * @throws SignedDocException for validation errors
     */    
    public void setTarget(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateTarget(str);
        if(ex != null)
            throw ex;
        m_target = str;
    }
    
    /**
     * Helper method to validate a target
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateTarget(String str)
    {
        SignedDocException ex = null;
        if(str == null && !m_sig.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3))
            ex = new SignedDocException(SignedDocException.ERR_SIGPROP_TARGET, 
                "Target must be in form: #<signature-id>", null);
        return ex;
    }
    
    /**
     * Accessor for certId attribute
     * @return value of certId attribute
     */
    public String getCertId() {
        return m_certId;
    }
    
    /**
     * Mutator for certId attribute
     * @param str new value for certId attribute
     * @throws SignedDocException for validation errors
     */    
    public void setCertId(String str) 
        throws SignedDocException
    {
    	if(m_sig.getSignedDoc() != null &&
    	  !m_sig.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)) {
        	SignedDocException ex = validateCertId(str);
        	if(ex != null)
         	   throw ex;
    	}
        m_certId = str;
    }
    
    /**
     * Helper method to validate an certificate id
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateCertId(String str)
    {
        SignedDocException ex = null;
        if(str == null)
            ex = new SignedDocException(SignedDocException.ERR_SIGPROP_CERT_ID, 
                "Cert Id must be in form: <signature-id>-CERTINFO", null);
        return ex;
    }
    
    /**
     * Accessor for signatureProductionPlace attribute
     * @return value of SignatureProductionPlace attribute
     */
    public SignatureProductionPlace getSignatureProductionPlace() {
        return m_address;
    }
    
    /**
     * Mutator for signatureProductionPlace attribute
     * @param str new value for SignatureProductionPlace attribute
     */    
    public void setSignatureProductionPlace(SignatureProductionPlace adr) 
        throws SignedDocException
    {
        m_address = adr;
    }
     
    /**
     * Accessor for signingTime attribute
     * @return value of signingTime attribute
     */
    public Date getSigningTime() {
        return m_signingTime;
    }
    
    /**
     * Mutator for signingTime attribute
     * @param str new value for signingTime attribute
     * @throws SignedDocException for validation errors
     */    
    public void setSigningTime(Date d) 
        throws SignedDocException
    {
        SignedDocException ex = validateSigningTime(d);
        if(ex != null)
            throw ex;
        m_signingTime = d;
    }
    
    /**
     * Helper method to validate a signingTime
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateSigningTime(Date d)
    {
        SignedDocException ex = null;
        if(d == null) // check the uri somehow ???
            ex = new SignedDocException(SignedDocException.ERR_SIGNING_TIME, 
                "Singing time cannot be empty!", null);
        return ex;
    }

    /**
     * Accessor for certDigestAlgorithm attribute
     * @return value of certDigestAlgorithm attribute
     */
    public String getCertDigestAlgorithm() {
        return m_certDigestAlgorithm;
    }
    
    /**
     * Mutator for certDigestAlgorithm attribute
     * @param str new value for certDigestAlgorithm attribute
     * @throws SignedDocException for validation errors
     */    
    public void setCertDigestAlgorithm(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateCertDigestAlgorithm(str);
        if(ex != null)
            throw ex;
        m_certDigestAlgorithm = str;
    }
    
    /**
     * Helper method to validate a digest algorithm
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateCertDigestAlgorithm(String str)
    {
        SignedDocException ex = null;
        if(str == null || !str.equals(SignedDoc.SHA256_DIGEST_ALGORITHM))
            ex = new SignedDocException(SignedDocException.ERR_CERT_DIGEST_ALGORITHM, 
                "Currently supports only SHA256 digest algorithm", null);
        return ex;
    }
    
    /**
     * Accessor for certDigestValue attribute
     * @return value of certDigestValue attribute
     */
    public byte[] getCertDigestValue() {
        return m_certDigestValue;
    }
    
    /**
     * Mutator for certDigestValue attribute
     * @param data new value for certDigestValue attribute
     * @throws SignedDocException for validation errors
     */    
    public void setCertDigestValue(byte[] data) 
        throws SignedDocException
    {
        SignedDocException ex = validateCertDigestValue(data);
        if(ex != null)
            throw ex;
        m_certDigestValue = data;
    }
 
    /**
     * Helper method to validate a digest value
     * @param data input data
     * @return exception or null for ok
     */
    private SignedDocException validateCertDigestValue(byte[] data)
    {
    	SignedDocException ex = null;
		if (data == null || data.length != SignedDoc.SHA256_DIGEST_LENGTH)
			ex = new SignedDocException(SignedDocException.ERR_DIGEST_LENGTH,
					"SHA256 digest data is always "
							+ SignedDoc.SHA256_DIGEST_LENGTH
							+ " bytes of length", null);
		return ex;
    }
    
    /**
     * Accessor for certSerial attribute
     * @return value of certSerial attribute
     */
    public BigInteger getCertSerial() {
        return m_certSerial;
    }
    
    /**
     * Mutator for certSerial attribute
     * @param str new value for certSerial attribute
     * @throws SignedDocException for validation errors
     */    
    public void setCertSerial(BigInteger i) 
        throws SignedDocException
    {
        SignedDocException ex = validateCertSerial(i);
        if(ex != null)
            throw ex;
        m_certSerial = i;
    }
    
    /**
     * Helper method to validate a certSerial
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateCertSerial(BigInteger i)
    {
        SignedDocException ex = null;
        if(i == null) // check the uri somehow ???
            ex = new SignedDocException(SignedDocException.ERR_CERT_SERIAL, 
                "Certificates serial number cannot be empty!", null);
        return ex;
    }

    /**
     * Returns the count of claimedRole objects
     * @return count of Reference objects
     */
    public int countClaimedRoles() {
        return ((m_claimedRoles == null) ? 0 : m_claimedRoles.size());
    }
    
    /**
     * Adds a new reference object
     * @param ref Reference object to add
     */
    public void addClaimedRole(String role) 
    {
        if(m_claimedRoles == null)
            m_claimedRoles = new ArrayList();
        m_claimedRoles.add(role);
    }
    
    /**
     * Returns the desired claimedRole object
     * @param idx index of the claimedRole object
     * @return desired claimedRole object
     */
    public String getClaimedRole(int idx) {
        return (String)m_claimedRoles.get(idx);
    }
    
    /**
     * Helper method to validate the whole
     * SignedProperties object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedDocException ex = validateId(m_id);
        if(ex != null)
            errs.add(ex);
        ex = validateTarget(m_target);
        if(ex != null)
            errs.add(ex);
        if(!m_sig.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)) {
        	ex = validateCertId(m_certId);
        	if(ex != null)
         	   errs.add(ex);
        }
        ex = validateSigningTime(m_signingTime);
        if(ex != null)
            errs.add(ex);
        ex = validateCertDigestAlgorithm(m_certDigestAlgorithm);
        if(ex != null)
            errs.add(ex);
        ex = validateCertDigestValue(m_certDigestValue);
        if(ex != null)
            errs.add(ex);
        ex = validateCertSerial(m_certSerial);
        if(ex != null)
            errs.add(ex);  
        // claimed roles
        // and signature production place are optional
        return errs;
    }
    
    /*
    private void debugWriteFile(String name, byte[] data)
    {
        try {
            String str = "C:\\veiko\\work\\sk\\JDigiDoc\\" + name;
            System.out.println("Writing debug file: " + str);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(str);
            fos.write(data);
            fos.close();
        } catch(Exception ex) {
            System.out.println("Error: " + ex);
            ex.printStackTrace(System.out);
        }
    }
    */
    
    /**
     * Calculates the digest of SignedProperties block
     * @return SignedProperties block digest
     */
    public byte[] calculateDigest()
        throws SignedDocException
    {
    	if(m_origDigest == null) {
        	CanonicalizationFactory canFac = ConfigManager.
                    instance().getCanonicalizationFactory();
        	byte[] tmp = canFac.canonicalize(toXML(),  
                    SignedDoc.CANONICALIZATION_METHOD_20010315);
        	//debugWriteFile("SigProp2.xml", tmp);
        	//System.out.println("SigProp2: " + tmp.length 
        	//            + " digest" + Base64Util.encode(SignedDoc.digest(tmp), 0));
        	return SignedDoc.digest(tmp);
    	}
    	else
    		return m_origDigest;
    }
    
    /**
     * Converts the SignedProperties to XML form
     * @return XML representation of SignedProperties
     */
    public byte[] toXML()
        throws SignedDocException
    {
        ByteArrayOutputStream bos = 
        	new ByteArrayOutputStream();
        try {
          	// In version 1.3 we use xmlns atributes like specified in XAdES 
           	if(m_sig.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)) {
           		bos.write(ConvertUtils.str2data("<SignedProperties xmlns=\""));
               	bos.write(ConvertUtils.str2data(SignedDoc.xmlns_etsi));
               	bos.write(ConvertUtils.str2data("\" Id=\""));                	
               	bos.write(ConvertUtils.str2data(m_id));
               	bos.write(ConvertUtils.str2data("\">\n"));
            } else { // in prior versions we used the wrong namespace
               	bos.write(ConvertUtils.str2data("<SignedProperties xmlns=\""));
               	bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
               	bos.write(ConvertUtils.str2data("\" Id=\""));                	
               	bos.write(ConvertUtils.str2data(m_id));
               	bos.write(ConvertUtils.str2data("\" Target=\""));
               	bos.write(ConvertUtils.str2data(m_target));
               	bos.write(ConvertUtils.str2data("\">\n"));
           	}
            bos.write(ConvertUtils.str2data("<SignedSignatureProperties>\n<SigningTime>"));
            bos.write(ConvertUtils.str2data(ConvertUtils.date2string(m_signingTime, m_sig.getSignedDoc())));
            bos.write(ConvertUtils.str2data("</SigningTime>\n<SigningCertificate>\n"));
            if(m_sig.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)) {
              	bos.write(ConvertUtils.str2data("<Cert>"));
            } else {
               	bos.write(ConvertUtils.str2data("<Cert Id=\""));
               	bos.write(ConvertUtils.str2data(m_certId));
               	bos.write(ConvertUtils.str2data("\">"));
            }
            bos.write(ConvertUtils.str2data("\n<CertDigest>\n<DigestMethod xmlns=\""));
           		bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
           		bos.write(ConvertUtils.str2data("\" Algorithm=\""));
            bos.write(ConvertUtils.str2data(m_certDigestAlgorithm));
            //ROB for xades4j needings...
            //bos.write(ConvertUtils.str2data("\">\n</DigestMethod>\n<DigestValue>"));
            bos.write(ConvertUtils.str2data("\"/>\n<DigestValue xmlns=\""));
           		bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
           		bos.write(ConvertUtils.str2data("\">"));
            bos.write(ConvertUtils.str2data(Base64Util.encode(m_certDigestValue, 0)));
            bos.write(ConvertUtils.str2data("</DigestValue>\n</CertDigest>\n"));
            // In version 1.3 we use correct <IssuerSerial> content 
            // e.g. subelements <X509IssuerName> and <X509SerialNumber>
           	if(m_sig.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3)) {
           		bos.write(ConvertUtils.str2data("<IssuerSerial>"));
           		bos.write(ConvertUtils.str2data("\n<X509IssuerName xmlns=\""));
           		bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
           		bos.write(ConvertUtils.str2data("\">"));
           		bos.write(ConvertUtils.str2data(m_sig.getKeyInfo().getSignersCertificate().getIssuerX500Principal().getName("RFC1779")));
           		bos.write(ConvertUtils.str2data("</X509IssuerName>"));
           		bos.write(ConvertUtils.str2data("\n<X509SerialNumber xmlns=\""));
           		bos.write(ConvertUtils.str2data(SignedDoc.xmlns_xmldsig));
           		bos.write(ConvertUtils.str2data("\">"));
           		bos.write(ConvertUtils.str2data(m_certSerial.toString()));
           		bos.write(ConvertUtils.str2data("</X509SerialNumber>\n"));            		
               	bos.write(ConvertUtils.str2data("</IssuerSerial>"));
           	} else { // in prior versions we used wrong <IssuerSerial> content
               	bos.write(ConvertUtils.str2data("<IssuerSerial>"));
               	bos.write(ConvertUtils.str2data(m_certSerial.toString()));
               	bos.write(ConvertUtils.str2data("</IssuerSerial>"));
           	}
            bos.write(ConvertUtils.str2data("</Cert></SigningCertificate>"));
            //ROB: not needed for BES
            //bos.write(ConvertUtils.str2data("\n<SignaturePolicyIdentifier>\n<SignaturePolicyImplied>\n</SignaturePolicyImplied>\n</SignaturePolicyIdentifier>"));
            if(m_address != null) {
                bos.write(ConvertUtils.str2data("\n"));
                bos.write(m_address.toXML());
            }
            if(countClaimedRoles() > 0) {
               	if(m_address != null)
               		bos.write(ConvertUtils.str2data("\n"));
                bos.write(ConvertUtils.str2data("<SignerRole>\n<ClaimedRoles>\n"));
                for(int i = 0; i < countClaimedRoles(); i++) {
                    bos.write(ConvertUtils.str2data("<ClaimedRole>"));
                    bos.write(ConvertUtils.str2data(getClaimedRole(i)));
                    bos.write(ConvertUtils.str2data("</ClaimedRole>\n"));
                }
                bos.write(ConvertUtils.str2data("</ClaimedRoles>\n</SignerRole>"));
            }
            bos.write(ConvertUtils.str2data("\n</SignedSignatureProperties>"));
            bos.write(ConvertUtils.str2data("\n<SignedDataObjectProperties>\n</SignedDataObjectProperties>"));
            bos.write(ConvertUtils.str2data("\n</SignedProperties>"));        
        } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of SignedProperties
     * @return SignedProperties string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }    
}
