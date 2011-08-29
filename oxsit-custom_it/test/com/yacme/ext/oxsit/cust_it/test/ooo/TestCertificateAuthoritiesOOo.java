/*************************************************************************
 *
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) version 1.1
 *  as published by the European Community.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the EUPL.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  EUPL for more details.
 *
 *  You should have received a copy of the EUPL along with this
 *  program.  If not, see:
 *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
 *
 ************************************************************************/

package com.yacme.ext.oxsit.cust_it.test.ooo;


import java.net.URL;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XContent;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.cust_it.security.crl.CertificationAuthorities;
import com.yacme.ext.oxsit.cust_it.test.ooo.OOoServerInfo;

/**
 *
 * @author __USER__
 */
public class TestCertificateAuthoritiesOOo {
    
    public OOoServerInfo SvrInfo = new OOoServerInfo();
    private static TestCertificateAuthoritiesOOo theInstance = null;
    public XComponent xComponent = null;
    private XContent xContent = null;
    private Object XActiveDataSink;

    /** Creates a new instance of __NAME__ */
    public TestCertificateAuthoritiesOOo() {
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
                
//building it it's enough?
                	//prepare the file URL
                	String position = Helpers.getExtensionInstallationPath(xCC);
                	
                	URL listURL = new URL(
                			position+System.getProperty("file.separator") + 
							"ca-list-signed-p7m-it"+ //fixed path, the directory containing the current root zip file
							System.getProperty("file.separator")+
							"LISTACER_20090303.zip.p7m"
                			);
                	
                	URL rootURL= new URL(
                			position+System.getProperty("file.separator") 
    						+ System.getProperty("file.separator") 
    						+ "ca-root-digitpa-it" 
    						+ System.getProperty("file.separator") + ConstantCustomIT.m_sCA_CNIPA_ROOT);
                	
                	CertificationAuthorities aCert = new CertificationAuthorities(null, xCC, listURL, rootURL, true);
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

    	theInstance = new TestCertificateAuthoritiesOOo();
            theInstance.run();

            theInstance.SvrInfo.CloseConnection();
    }
}
