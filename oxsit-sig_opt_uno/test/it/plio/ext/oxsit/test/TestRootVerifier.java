/**
 * 
 */
package it.plio.ext.oxsit.test;

import it.infocamere.freesigner.gui.RootsVerifier;

/**
 * @author beppe
 *
 */
public class TestRootVerifier {
//class to test the Root Verifier behavior
	
    public RootsVerifier rootsVerifier = null;

	public TestRootVerifier() {
		// TODO Auto-generated constructor stub
	}

	public void doTest() {
	
		rootsVerifier = RootsVerifier.getInstance();
		
	}
	
	
	public static void main(String[] args) {
		
		TestRootVerifier test = new TestRootVerifier();
		
		test.doTest();
	}
	
}
