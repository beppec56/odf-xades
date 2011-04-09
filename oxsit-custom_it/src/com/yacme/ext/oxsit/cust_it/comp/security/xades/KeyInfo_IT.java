/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

/**
 * Models the KeyInfo block of an XML-DSIG 
 * signature. In DigiDoc library the key info 
 * allways contains only one subject certificate,
 * e.g. no uplinks and the smaller items like
 * RSA public key modulus and export are not
 * kept separately but calculated online from the
 * signers certificate. That means they are read-only
 * attributes.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class KeyInfo_IT implements Serializable {
	/** parent object - Signature ref */
    private SignatureXADES_IT m_signature;
    
    
    /** 
     * Creates new KeyInfo 
     */
    public KeyInfo_IT() {
    	m_signature = null;
    }
    
    /** 
     * Creates new KeyInfo 
     * @param cert signers certificate
     */
    public KeyInfo_IT(X509Certificate cert) 
        throws SignedODFDocumentException_IT
    {
        setSignersCertificate(cert);
    }
    
    /**
     * Accessor for Signature attribute
     * @return value of Signature attribute
     */
    public SignatureXADES_IT getSignature()
    {
    	return m_signature;
    }
    
    /**
     * Mutator for Signature attribute
     * @param uprops value of Signature attribute
     */
    public void setSignature(SignatureXADES_IT sig)
    {
    	m_signature = sig;
    }
    
    /**
     * Accessor for signersCert attribute
     * @return value of signersCert attribute
     */
    public X509Certificate getSignersCertificate() {
    	X509Certificate cert = null;
    	if(m_signature != null) {
    		CertValue_IT cval = m_signature.getCertValueOfType(CertValue_IT.CERTVAL_TYPE_SIGNER);
    		if(cval != null) {
    			cert = cval.getCert();
    		}
    	}
        return cert;
    }
    
    /**
     * return certificate owners first name
     * @return certificate owners first name or null
     */
    public String getSubjectFirstName() {
    	X509Certificate cert = getSignersCertificate();
    	if(cert != null)
    		return SignedODFDocument_IT.getSubjectFirstName(cert);
    	else
    		return null;
    }

    /**
     * return certificate owners last name
     * @return certificate owners last name or null
     */
    public String getSubjectLastName() {
    	X509Certificate cert = getSignersCertificate();
    	if(cert != null)
    		return SignedODFDocument_IT.getSubjectLastName(cert);
    	else
    		return null;
    }

    /**
     * return certificate owners personal code
     * @return certificate owners personal code or null
     */
    public String getSubjectPersonalCode() {
    	X509Certificate cert = getSignersCertificate();
    	if(cert != null)
    		return SignedODFDocument_IT.getSubjectPersonalCode(cert);
    	else
    		return null;
    }
    
    /**
     * Mutator for signersCert attribute
     * @param cert new value for signersCert attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setSignersCertificate(X509Certificate cert) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateSignersCertificate(cert);
        if(ex != null)
            throw ex;
        if(m_signature != null) {
    		CertValue_IT cval = m_signature.getOrCreateCertValueOfType(CertValue_IT.CERTVAL_TYPE_SIGNER);
    		cval.setCert(cert);
        }
    }
    
    /**
     * Helper method to validate a signers cert
     * @param cert input data
     * @return exception or null for ok
     */
    private SignedODFDocumentException_IT validateSignersCertificate(X509Certificate cert)
    {
        SignedODFDocumentException_IT ex = null;
        if(cert == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_SIGNERS_CERT, 
                "Signers certificate is required", null);
        return ex;
    }
    
    /**
     * return the signers certificates key modulus
     * @return signers certificates key modulus
     */
    public BigInteger getSignerKeyModulus()
    {
    	X509Certificate cert = getSignersCertificate();
    	if(cert != null)
    		return ((RSAPublicKey)cert.getPublicKey()).getModulus();
    	else
    		return null;
    }
    
    /**
     * return the signers certificates key exponent
     * @return signers certificates key exponent
     */
    public BigInteger getSignerKeyExponent()
    {
    	X509Certificate cert = getSignersCertificate();
    	if(cert != null)
    		return ((RSAPublicKey)cert.getPublicKey()).getPublicExponent();
    	else
    		return null;
    }
    
    /**
     * Helper method to validate the whole
     * KeyInfo object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedODFDocumentException_IT ex = null;
        X509Certificate cert = getSignersCertificate();
    	if(cert != null)
    		ex = validateSignersCertificate(cert);
        if(ex != null)
            errs.add(ex);        
        return errs;
    }
    
    /**
     * Converts the KeyInfo to XML form
     * @return XML representation of KeyInfo
     */
    public byte[] toXML()
        throws SignedODFDocumentException_IT
    {
        ByteArrayOutputStream bos = 
                new ByteArrayOutputStream();
        try {
            bos.write(ConvertUtils.str2data("<KeyInfo>\n"));
            bos.write(ConvertUtils.str2data("<KeyValue>\n<RSAKeyValue>\n<Modulus>"));
            
            //ROB: cryptoBinary! http://www.w3.org/TR/xmldsig-core/#sec-CryptoBinary
            //bos.write(ConvertUtils.str2data(Base64Util.encode(getSignerKeyModulus().toByteArray(), 64)));
            bos.write(ConvertUtils.str2data(Base64Util.encode(getBytes(getSignerKeyModulus(), getSignerKeyModulus().bitLength()), 64)));
            bos.write(ConvertUtils.str2data("</Modulus>\n<Exponent>"));
            //ROB: cryptoBinary! http://www.w3.org/TR/xmldsig-core/#sec-CryptoBinary
            //bos.write(ConvertUtils.str2data(Base64Util.encode(getSignerKeyExponent().toByteArray(), 64)));
            bos.write(ConvertUtils.str2data(Base64Util.encode(getBytes(getSignerKeyExponent(), getSignerKeyExponent().bitLength()), 64)));
            bos.write(ConvertUtils.str2data("</Exponent>\n</RSAKeyValue>\n</KeyValue>\n"));
            bos.write(ConvertUtils.str2data("<X509Data>"));
            CertValue_IT cval = null;
            if(m_signature != null) {
            	cval = m_signature.getCertValueOfType(CertValue_IT.CERTVAL_TYPE_SIGNER);
            	if(cval != null)
            		bos.write(cval.toXML());
            }
            bos.write(ConvertUtils.str2data("</X509Data>"));
            bos.write(ConvertUtils.str2data("</KeyInfo>"));
         } catch(IOException ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }
    
    /**
     * Returns a byte-array representation of a <code>{@link BigInteger}<code>.
     * No sign-bit is outputed.
     *
     * <b>N.B.:</B> <code>{@link BigInteger}<code>'s toByteArray
     * retunrs eventually longer arrays because of the leading sign-bit.
     *
     * @param big <code>BigInteger<code> to be converted
     * @param bitlen <code>int<code> the desired length in bits of the representation
     * @return a byte array with <code>bitlen</code> bits of <code>big</code>
     */
    static byte[] getBytes(BigInteger big, int bitlen) {

       //round bitlen
       bitlen = ((bitlen + 7) >> 3) << 3;

       if (bitlen < big.bitLength()) {
          throw new IllegalArgumentException();
       }

       byte[] bigBytes = big.toByteArray();

       if (((big.bitLength() % 8) != 0)
               && (((big.bitLength() / 8) + 1) == (bitlen / 8))) {
          return bigBytes;
       }

          // some copying needed
          int startSrc = 0;    // no need to skip anything
          int bigLen = bigBytes.length;    //valid length of the string

          if ((big.bitLength() % 8) == 0) {    // correct values
             startSrc = 1;    // skip sign bit

             bigLen--;    // valid length of the string
          }

          int startDst = bitlen / 8 - bigLen;    //pad with leading nulls
          byte[] resizedBytes = new byte[bitlen / 8];

          System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, bigLen);

          return resizedBytes;

    }


    /**
     * return the stringified form of KeyInfo
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
