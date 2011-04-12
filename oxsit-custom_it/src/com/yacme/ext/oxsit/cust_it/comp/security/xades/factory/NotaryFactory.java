/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.security.cert.X509Certificate;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.Notary;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.Signature;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;

/**
 * Interface for notary functions
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface NotaryFactory 
{
    /** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedDocException;

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
    public Notary getConfirmation(Signature sig, 
        X509Certificate signersCert, X509Certificate caCert)
        throws SignedDocException;
    
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
    public Notary getConfirmation(Signature sig, X509Certificate signersCert) 
        throws SignedDocException;
    
    /**
     * Check the response and parse it's data
     * @param not initial Notary object that contains only the
     * raw bytes of an OCSP response
     * @returns Notary object with data parsed from OCSP response
     */
    public Notary parseAndVerifyResponse(Signature sig, Notary not)
        throws SignedDocException;
        
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
     * @throws SignedDocException if the certificate is not valid
     */
    public void checkCertificate(X509Certificate cert) 
        throws SignedDocException;
    
    /**
     * Verifies the certificate.
     * @param cert certificate to verify
     * @param bUseOcsp flag: use OCSP to verify cert. If false then use CRL instead
     * @throws SignedDocException if the certificate is not valid
     */   
    public void checkCertificateOcspOrCrl(X509Certificate cert, boolean bUseOcsp) 
        throws SignedDocException;
        
    /**
     * Get confirmation from AS Sertifitseerimiskeskus
     * by creating an OCSP request and parsing the returned
     * OCSP response
     * @param nonce signature nonce
     * @param signersCert signature owners cert
     * @param notId new id for Notary object
     * @returns Notary object
     */
    public Notary getConfirmation(byte[] nonce, 
        X509Certificate signersCert, String notId) 
        throws SignedDocException;
        
}
