/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;

/**
 * Interface for canonicalization functions
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface CanonicalizationFactory {

    /** 
     * initializes the implementation class 
     */
    public void init()
        throws SignedDocException;
    
    /**
     * Canonicalizes XML fragment using the
     * xml-c14n-20010315 algorithm
     * @param data input data
     * @param uri canonicalization algorithm
     * @returns canonicalized XML
     * @throws SignedDocException for all errors
     */
    byte[] canonicalize(byte[] data, String uri)
        throws SignedDocException;

}
