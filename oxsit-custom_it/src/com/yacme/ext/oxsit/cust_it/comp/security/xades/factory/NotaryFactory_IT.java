/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.security.cert.X509Certificate;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.Notary_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignatureXADES_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocumentException_IT;

/**
 * Interface for notary functions
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface NotaryFactory_IT 
{
    /** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedODFDocumentException_IT;

    /**
     * Get confirmation from AS Sertifitseerimiskeskus
     * by creating an OCSP request and parsing the returned 
     * OCSP response
     * @param sig Signature object
     * @param signersCert signature owners cert
     * @param caCert CA cert for this signer
     * @param notaryCert notarys own cert
     * @returns Notary object
     */
    public Notary_IT getConfirmation(SignatureXADES_IT sig, 
        X509Certificate signersCert, X509Certificate caCert)
        throws SignedODFDocumentException_IT;
    
    /**
     * Get confirmation from AS Sertifitseerimiskeskus
     * by creating an OCSP request and parsing the returned
     * OCSP response. CA and reponders certs are read 
     * using paths in the config file or maybe from
     * a keystore etc.
     * @param sig Signature object
     * @param signersCert signature owners cert
     * @returns Notary object
     */
    public Notary_IT getConfirmation(SignatureXADES_IT sig, X509Certificate signersCert) 
        throws SignedODFDocumentException_IT;
    
    /**
     * Check the response and parse it's data
     * @param not initial Notary object that contains only the
     * raw bytes of an OCSP response
     * @returns Notary object with data parsed from OCSP response
     */
    public Notary_IT parseAndVerifyResponse(SignatureXADES_IT sig, Notary_IT not)
        throws SignedODFDocumentException_IT;
        
    /**
     * Returns the OCSP responders certificate
     * @param responderCN responder-id's CN
     * @param specificCertNr specific cert number that we search.
     * If this parameter is null then the newest cert is seleced (if many exist)
     * @returns OCSP responders certificate
     */
    public X509Certificate getNotaryCert(String responderCN, String specificCertNr);
    
    /**
     * Returns the CA certificate
     * @param CN CA certificates CN
     * @returns CA certificate
     */
    public X509Certificate getCACert(String responderCN);

    /**
     * Verifies the certificate by creating an OCSP request
     * and sending it to SK server.
     * @param cert certificate to verify
     * @throws SignedODFDocumentException_IT if the certificate is not valid
     */
    public void checkCertificate(X509Certificate cert) 
        throws SignedODFDocumentException_IT;
    
    /**
     * Verifies the certificate.
     * @param cert certificate to verify
     * @param bUseOcsp flag: use OCSP to verify cert. If false then use CRL instead
     * @throws SignedODFDocumentException_IT if the certificate is not valid
     */   
    public void checkCertificateOcspOrCrl(X509Certificate cert, boolean bUseOcsp) 
        throws SignedODFDocumentException_IT;
        
    /**
     * Get confirmation from AS Sertifitseerimiskeskus
     * by creating an OCSP request and parsing the returned
     * OCSP response
     * @param nonce signature nonce
     * @param signersCert signature owners cert
     * @param notId new id for Notary object
     * @returns Notary object
     */
    public Notary_IT getConfirmation(byte[] nonce, 
        X509Certificate signersCert, String notId) 
        throws SignedODFDocumentException_IT;
        
}
