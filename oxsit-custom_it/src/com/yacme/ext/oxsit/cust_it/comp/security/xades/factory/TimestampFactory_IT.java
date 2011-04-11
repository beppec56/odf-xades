/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.security.cert.X509Certificate;
import java.util.ArrayList;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignatureXADES_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocumentException_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.TimestampInfo_IT;

/**
 * Interface for timestamp functions
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface TimestampFactory_IT {

	/** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedODFDocumentException_IT;

    /**
     * Verifies this one timestamp
     * @param ts TimestampInfo object
     * @param tsaCert TSA certificate
     * @returns result of verification
     */
    public boolean verifyTimestamp(TimestampInfo_IT ts, X509Certificate tsaCert)
        throws SignedODFDocumentException_IT;
    
    /**
     * Verifies all timestamps in this signature and
     * return a list of errors.
     * @param sig signature to verify timestamps
     * @return list of errors. Empty if no errors.
     * @throws SignedODFDocumentException_IT
     */
    public ArrayList verifySignaturesTimestamps(SignatureXADES_IT sig);
    //	throws SignedODFDocumentException_IT;
    
}
