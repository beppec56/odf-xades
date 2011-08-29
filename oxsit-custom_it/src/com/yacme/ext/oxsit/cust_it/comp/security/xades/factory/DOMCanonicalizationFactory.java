/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.c14n.Canonicalizer;
import org.w3c.dom.Document;

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
       	
        	DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        	dfactory.setNamespaceAware(true);
        	
        	//Avoid xml validation.
        	dfactory.setValidating(false);
        	dfactory.setFeature("http://xml.org/sax/features/validation", false);
        	dfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        	dfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        	
        	DocumentBuilder documentBuilder = dfactory.newDocumentBuilder();
        	// this is to throw away all validation warnings
        	documentBuilder.setErrorHandler(new org.apache.xml.security.utils.IgnoreAllErrorHandler());
        	
        	//System.out.println("Before dom parsing");
        	Document doc = documentBuilder.parse(new ByteArrayInputStream(data));
        	//System.out.println("After dom parsing");
        	
            Canonicalizer c14n = Canonicalizer.getInstance(uri);
            
            //System.out.println("Before dom c14n");
            result = c14n.canonicalizeSubtree(doc);
            //result = c14n.canonicalize(data);
            //System.out.println("After dom c14n");

        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_CAN_ERROR);
        }
        return result;
    }
}
