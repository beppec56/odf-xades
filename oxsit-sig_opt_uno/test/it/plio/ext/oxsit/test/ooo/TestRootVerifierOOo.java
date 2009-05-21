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

import com.sun.star.beans.Property;
import com.sun.star.container.XHierarchicalNameAccess;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.IOException;
import com.sun.star.io.NotConnectedException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.text.XTextDocument;
import com.sun.star.ucb.Command;
import com.sun.star.ucb.XCommandProcessor;
import com.sun.star.ucb.XContent;
import com.sun.star.ucb.XContentIdentifier;
import com.sun.star.ucb.XContentIdentifierFactory;
import com.sun.star.ucb.XContentProvider;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.io.XActiveDataSink;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.*;
import com.sun.star.packages.*;
import com.sun.star.packages.manifest.XManifestReader;
import com.sun.star.sdbc.XResultSet;
import com.sun.star.sdbc.XRow;
import com.sun.star.ucb.OpenCommandArgument2;
import com.sun.star.ucb.OpenMode;
import com.sun.star.ucb.XContentAccess;
import com.sun.star.ucb.XDynamicResultSet;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.uri.XUriReference;
import com.sun.star.uri.XUriReferenceFactory;
import com.sun.star.uri.XVndSunStarPkgUrlReferenceFactory;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.comp.security.ca.RootsVerifier;

import java.util.logging.Level;
import java.util.logging.Logger;

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
//to get a frame, for check                
                RootsVerifier aVerif = new RootsVerifier(null,xCC);
//building it it's enough?

            }
        }
        catch(Exception e) {
            System.out.println("WARNING: exception thrown !\nJob aborted:\n"+e.toString());
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

