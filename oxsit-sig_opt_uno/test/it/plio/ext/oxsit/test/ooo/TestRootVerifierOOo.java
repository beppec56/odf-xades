/*
 * __NAME__.java
 *
 * Created on __DATE__, __TIME__
 * 
 * a bare Java application to study the ucb stuff on OOo
 * in order to access an ODT package from OOo API
 * 
 *  The Contents of this file are made available subject to
 *  the terms of GNU Lesser General Public License Version 2.1.
 *
 *
 *    GNU Lesser General Public License Version 2.1
 *    =============================================
 *    Copyright 2008 by Giuseppe Castagno
 *    giuseppe.castagno at acca-esse.eu
 *    beppec56 at openoffice.org
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License version 2.1, as published by the Free Software Foundation.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *
 * 
 * used OOo Java libraries:
 * jurt.jar, jut.jar, juh.jar, ridl.jar, unoidl.jar
 */

package it.plio.ext.oxsit.test.ooo;

import it.plio.ext.oxsit.comp.security.ca.RootsVerifier;
import it.plio.ext.oxsit.test.ooo.OOoServerInfo;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XContent;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/**
 *
 * @author __USER__
 */
public class TestRootVerifierOOo {
    
    public OOoServerInfo SvrInfo = new OOoServerInfo();
    private static TestRootVerifierOOo theInstance = null;
    public XComponent xComponent = null;
    private XContent xContent = null;
    private Object XActiveDataSink;

    /** Creates a new instance of __NAME__ */
    public TestRootVerifierOOo() {
    }

    private XMultiComponentFactory xMCF = null;
    private XComponentContext xCC = null;

    private void run() {
        try {
            if(SvrInfo.InitConnection()) {
                System.out.println("Connection successful !");
                xCC = SvrInfo.getCompCtx();
                xMCF = xCC.getServiceManager();
            //try to get a document
                Object desktop = SvrInfo.getFactory().createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
            // get the remote service manager
            // query its XDesktop interface, we need the current component
 /*               XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
                // retrieve the current component and access the controller
                    xComponent = xDesktop.getCurrentComponent();
                //check to see if a writer component is ready
                    if(xComponent == null) {
                        System.out.println("WARNING: there is no open document on Open Office !\nJob aborted");
                    }
                    else {
                    // we query the interface XSpreadsheetDocument from xComponent
                        XSpreadsheetDocument xSpreadsheetDocument =
                                (XSpreadsheetDocument)UnoRuntime.queryInterface(XSpreadsheetDocument.class,
                                    xComponent);
                    // we query the interface XTextDocument from xComponent
                        XTextDocument xTextDocument =
                                (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, xComponent); 
                    // we query the interface XDrawPagesSupplier from xComponent
                        XDrawPagesSupplier xDrawDocument =
                                (XDrawPagesSupplier)UnoRuntime.queryInterface(XDrawPagesSupplier.class, xComponent); 
                        if(xSpreadsheetDocument != null) {
                            System.out.println("The document is a calc document");
                        }
                        else if(xTextDocument != null) {
                            System.out.println("The document is a writer document");
                        }
                        else if(xDrawDocument != null) {
                            System.out.println("The document is a Draw or Impress document");
                        }
                        else {
                            System.out.println("The document is NOT a known type or a signable one");
                            return;
                        }                */
                
//to get a frame, for check                
                RootsVerifier aVerif = new RootsVerifier(null,xCC);
//building it it's enough?

                
                
            }
        }
        catch(Exception e) {
            System.out.println("WARNING: exception thrown !\nJob aborted:\n"+e.toString());
        } catch (java.lang.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    	theInstance = new TestRootVerifierOOo();
            theInstance.run();
            theInstance.SvrInfo.CloseConnection();
    }
}
