/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.security.cert.X509Certificate;
import java.util.ArrayList;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.Signature;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.TimestampInfo;

/**
 * Interface for timestamp functions
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface TimestampFactory {

	/** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedDocException;

    /**
     * Verifies this one timestamp
     * @param ts TimestampInfo object
     * @param tsaCert TSA certificate
     * @returns result of verification
     */
    public boolean verifyTimestamp(TimestampInfo ts, X509Certificate tsaCert)
        throws SignedDocException;
    
    /**
     * Verifies all timestamps in this signature and
     * return a list of errors.
     * @param sig signature to verify timestamps
     * @return list of errors. Empty if no errors.
     * @throws SignedDocException
     */
    public ArrayList<SignedDocException> verifySignaturesTimestamps(Signature sig);
    //	throws SignedDocException;
    
}
