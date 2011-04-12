/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.io.InputStream;
import java.security.cert.X509Certificate;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.Signature;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDoc;

/**
 * Interface for reading and writing
 * DigiDoc files
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface DigiDocFactory
{
    /** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedDocException;

    /**
     * Reads in a DigiDoc file
     * @param fileName file name
     * @return signed document object if successfully parsed
     */
    public SignedDoc readSignedDoc(String fileName) 
        throws SignedDocException;

    /**
     * Reads in a DigiDoc file
     * @param digiDocStream opened stream with DigiDoc data
     * The use must open and close it. 
     * @return signed document object if successfully parsed
     */
    public SignedDoc readSignedDoc(InputStream digiDocStream) 
        throws SignedDocException;
        
    /**
	 * Reads in only one <Signature>
	 * @param sdoc SignedDoc to add this signature to
	 * @param sigStream opened stream with Signature data
	 * The user must open and close it.
	 * @return signed document object if successfully parsed
	 */
	public Signature readSignature(SignedDoc sdoc, InputStream sigStream)
		throws SignedDocException;
	
    /**
     * Verifies that the signers cert
     * has been signed by at least one
     * of the known root certs
     * @param cert certificate to check
     */
    public boolean verifyCertificate(X509Certificate cert)
    	throws SignedDocException;       
    	
    /**
     * Finds the CA for this certificate
     * if the root-certs table is not empty
     * @param cert certificate to search CA for
     * @return CA certificate
     */
    public X509Certificate findCAforCertificate(X509Certificate cert); 
}
