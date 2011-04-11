/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.security.cert.X509Certificate;
import java.util.Date;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocumentException_IT;

/**
 * Interface for handling CRL-s
 * DigiDoc files
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface CRLFactory_IT 
{
    /** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedODFDocumentException_IT;

   /**
    * Checks the cert
    * @return void
    * @param cert cert to be verified
    * @param checkDate java.util.Date
    * @throws SignedODFDocumentException_IT for all errors
    */
  public void checkCertificate(X509Certificate cert, Date checkDate) 
        throws SignedODFDocumentException_IT;
        
   /**
    * Checks the cert
    * @return void
    * @param b64cert Certificate in base64 form
    * @param checkDate java.util.Date
    */
  //public void checkCertificateBase64(String b64cert, Date checkDate) 
   //     throws SignedODFDocumentException_IT;
        
        
}
