/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import org.apache.xml.security.c14n.Canonicalizer;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;

/**
 * Canonicalizes XML using DOM and XPath
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class DOMCanonicalizationFactory implements CanonicalizationFactory
{
   private static Canonicalizer m_c14n;

    /** 
     * Creates new DOMCanonicalizationFactory 
     */
    public DOMCanonicalizationFactory() {
    }
    
    /**
     * initializes the implementation class
     */
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory#init()
	 */
	@Override
    public void init()
        throws SignedDocException 
    {
        try {
        org.apache.xml.security.Init.init();
        //Canonicalizer.register(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS,
        // "org.apache.xml.security.c14n.implementations.Canonicalizer20010315OmitComments");
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_CAN_FAC_INIT);
        }
    }

    /**
     * Canonicalizes XML fragment using the
     * xml-c14n-20010315 algorithm
     * @param data input data
     * @param uri canonicalization algorithm
     * @returns canonicalized XML
     * @throws SignedDocException for all errors
	 * (non-Javadoc)
	 * @see com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory#canonicalize(byte[], java.lang.String)
	 */
	@Override
    public byte[] canonicalize(byte[] data, String uri)
        throws SignedDocException
    {
        byte[] result = null;
        try {
        	org.apache.xml.security.Init.init();
            Canonicalizer c14n = Canonicalizer.
                getInstance("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"); 
            result = c14n.canonicalize(data);            
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_CAN_ERROR);
        }
        return result;
    }
}
