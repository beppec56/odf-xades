/**
 * 
 */
package it.plio.ext.oxsit.test;

import it.trento.comune.j4sign.pcsc.PCSCHelper;

/** A simple class to test the PCSCHelper stuff in OOo
 * 
 * @author beppec56
 *
 */
public class TestPCSCHelper {

	
	   //a simple test method..
    public static void main(String[] args) {

        PCSCHelper a = new PCSCHelper(true);
        a.findCards();
        System.exit(0);

    }

}
