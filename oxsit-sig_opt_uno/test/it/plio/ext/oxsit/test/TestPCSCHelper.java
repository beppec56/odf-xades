/**
 * 
 */
package it.plio.ext.oxsit.test;

import it.plio.ext.oxsit.pcsc.PCSCHelper;


/** A simple class to test the PCSCHelper stuff in OOo
 * 
 * @author beppec56
 *
 */
public class TestPCSCHelper {


	
	   //a simple test method..
    public static void main(String[] args) {

        PCSCHelper a = new PCSCHelper();
        a.createSSCDsXML();
        System.exit(0);
    }

}
