/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.io.InputStream;
import java.security.cert.X509Certificate;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignatureXADES_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocumentException_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocument_IT;

/**
 * Interface for reading and writing
 * DigiDoc files
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface DigiDocFactory_IT
{
    /** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedODFDocumentException_IT;

    /**
     * Reads in a DigiDoc file
     * @param fileName file name
     * @return signed document object if successfully parsed
     */
    public SignedODFDocument_IT readSignedDoc(String fileName) 
        throws SignedODFDocumentException_IT;

    /**
     * Reads in a DigiDoc file
     * @param digiDocStream opened stream with DigiDoc data
     * The use must open and close it. 
     * @return signed document object if successfully parsed
     */
    public SignedODFDocument_IT readSignedDoc(InputStream digiDocStream) 
        throws SignedODFDocumentException_IT;
        
    /**
	 * Reads in only one <Signature>
	 * @param sdoc SignedDoc to add this signature to
	 * @param sigStream opened stream with Signature data
	 * The user must open and close it.
	 * @return signed document object if successfully parsed
	 */
	public SignatureXADES_IT readSignature(SignedODFDocument_IT sdoc, InputStream sigStream)
		throws SignedODFDocumentException_IT;
	
    /**
     * Verifies that the signers cert
     * has been signed by at least one
     * of the known root certs
     * @param cert certificate to check
     */
    public boolean verifyCertificate(X509Certificate cert)
    	throws SignedODFDocumentException_IT;       
    	
    /**
     * Finds the CA for this certificate
     * if the root-certs table is not empty
     * @param cert certificate to search CA for
     * @return CA certificate
     */
    public X509Certificate findCAforCertificate(X509Certificate cert); 
}
