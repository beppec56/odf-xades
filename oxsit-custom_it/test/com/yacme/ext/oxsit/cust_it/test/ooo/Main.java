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
import com.yacme.ext.oxsit.Utilities;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author __USER__
 */
public class Main {
    
    public OOoServerInfo SvrInfo = new OOoServerInfo();
    private static Main theInstance = null;
    public XComponent xComponent = null;
    private XContent xContent = null;
    private Object XActiveDataSink;

    /** Creates a new instance of __NAME__ */
    public Main() {
    }

    private XMultiComponentFactory xMCF = null;
    private XComponentContext xCC = null;

    /** try to access the document package low level elements
     * get the manifest
     * scan it to search for the pieces
     *  
     */

    Object executeCommand(XContent xContent, String aCommandName, Object aArgument)
            throws com.sun.star.ucb.CommandAbortedException, com.sun.star.uno.Exception {
 
      /////////////////////////////////////////////////////////////////////
      // Obtain command processor interface from given content.
      /////////////////////////////////////////////////////////////////////
 
        XCommandProcessor xCmdProcessor = (XCommandProcessor)UnoRuntime.queryInterface( 
                XCommandProcessor.class, xContent);
 
      /////////////////////////////////////////////////////////////////////
      // Assemble command to execute.
      /////////////////////////////////////////////////////////////////////
 
        Command aCommand = new Command();
        aCommand.Name = aCommandName;
        aCommand.Handle = -1; // not available
        aCommand.Argument = aArgument;
 
        // Note: throws CommandAbortedException and Exception since
        // we pass null for the XCommandEnvironment parameter
        return xCmdProcessor.execute(aCommand, 0, null);
    }

    public void examineODTPackageFolder(XContentProvider xProvider, 
                                        XContentIdentifierFactory xIdFactory, String aFolderURL,
                                        XContent xPackContent, int level ) {
        // scan the folder children(s) and list them
        /////////////////////////////////////////////////////////////////////
        // Open a folder content, request property values for the string
        // property Title and the boolean property IsFolder...
        /////////////////////////////////////////////////////////////////////
        // Fill argument structure...
 
        OpenCommandArgument2 aArg = new OpenCommandArgument2();
        aArg.Mode = OpenMode.ALL;// FOLDER, DOCUMENTS -> simple filter
        aArg.Priority = 32768;// Ignored by most implementations

        // Fill info for the properties wanted.
        Property[] aProps = new Property[2];
        Property prop1 = new Property();
        prop1.Name = "Title";
        prop1.Handle = -1;// n/a
        aProps[0] = prop1;
        Property prop2 = new Property();
        prop2.Name = "IsFolder";
        prop2.Handle = -1;// n/a
        aProps[1] = prop2;

        aArg.Properties = aProps;

        XDynamicResultSet xSet;
        try {
            Object oSet =  executeCommand(xPackContent, "open", aArg);
            //convert the object from Object to XDynamicResultSet
            Type aType = AnyConverter.getType(oSet);
            xSet = (XDynamicResultSet) AnyConverter.toObject(new com.sun.star.uno.Type
                                                             (com.sun.star.ucb.XDynamicResultSet.class), oSet);
// now list the xSet elements
            XResultSet xResultSet = xSet.getStaticResultSet();
            /////////////////////////////////////////////////////////////////////
            // Iterate over children, access children and property values...
            /////////////////////////////////////////////////////////////////////

            try {
                // Move to beginning
                if (xResultSet.first()) {
                    // obtain XContentAccess interface for child content access and XRow for properties
                    XContentAccess xContentAccess = (XContentAccess)UnoRuntime.queryInterface( 
                        XContentAccess.class, xResultSet);
                    XRow xRow = (XRow)UnoRuntime.queryInterface(XRow.class, xResultSet);
                    //
                    //int nElements = xResultSet.
                    do {
                        // Obtain URL of child.
                        String aId = xContentAccess.queryContentIdentifierString();
 
                        // First column: Title (column numbers are 1-based!)
                        String aTitle = xRow.getString(1);
                        if (aTitle.length() == 0 && xRow.wasNull())
                            System.out.println("Error aTitle.length() == 0 && xRow.wasNull()! ");
                        else {
                            for(int i = 0; i < level; i++)
                                System.out.print("\t");
                            System.out.print(aTitle+" ");
                        }
                        // Second column: IsFolder
                        boolean bFolder = xRow.getBoolean(2);
                        if (!bFolder && xRow.wasNull())
                            System.out.println("Error ((!bFolder && xRow.wasNull())! ");
                        else if(bFolder) {
                            System.out.println(" (folder)");
// then call recursively, after opening it
                            XContentIdentifier xIdMFolder = 
                                xIdFactory.createContentIdentifier(aFolderURL+aTitle+"/");
                            XContent xPackContentI = xProvider.queryContent(xIdMFolder);
// this is (supposed) to be an xml stream, we need to access it and examine the content
// as xml, of course
// then this is a the META-INF folder of the ODF package, examine it for manifest.xml
// some info are available here:1
//http://localhost/wiki/index.php/Documentation/DevGuide/AppendixC/The_Package_Content_Provider
// and:
// http://localhost/wiki/index.php/Documentation/DevGuide/UCB/Folders
//call the recursive method to print the folder content
// the following lines is of no use, since we already know it's a folder :-)
//                                String contentF = xPackContentI.getContentType();
//                                if( contentF.contentEquals("application/vnd.sun.star.pkg-folder" )) 

                            examineODTPackageFolder(xProvider, xIdFactory, aFolderURL+aTitle+"/",
                                                    xPackContentI, level+1 );
//check if folder was META-INF, somehow the stream manifest.xml is hidden from this scan
                            if(aTitle.equals("META-INF")) {
//check if manifest.xml is there
                                XContentIdentifier xIdManifest =
                                    xIdFactory.createContentIdentifier(
                                        aFolderURL+aTitle+"/manifest.xml");
                                XContent xPackStreamContent = xProvider.queryContent(xIdManifest);
                                String contentMan = xPackStreamContent.getContentType();

                                if( contentMan.contentEquals("application/vnd.sun.star.pkg-stream" )) {
                                    for(int i = 0; i < level+1; i++)
                                        System.out.print("\t");
                                    System.out.println("manifest.xml: " + contentMan);                                    
                                }
                            }
                        }
                        else
                            System.out.println(" (stream)");                                  
                    } while (xResultSet.next()); // next child
                }
            }
            catch (com.sun.star.ucb.ResultSetException e) {
//          ... error ...
                e.printStackTrace(System.out);
            }
        }
        catch (com.sun.star.ucb.CommandAbortedException e) {
            //          ... error ...
            e.printStackTrace(System.out);
        }
        catch (com.sun.star.uno.Exception e) {
            //          ... error ...
            e.printStackTrace(System.out);
        }
    }

/** examines the given package (an ODT package)
 * 
 */
    public void examinePackageODT_old(String aTheDocURL) {
        try {
            String[] keys = new String[2];
            keys[ 0 ] = "Local";
            keys[ 0 ] = "Office";             
// create the ucb, got from DevGuide at:
// http://localhost/wiki/index.php/Documentation/DevGuide/UCB/Instantiating_the_UCB
// Supply configuration to use for this UCB instance...
            Object oUCB = xMCF.createInstanceWithArgumentsAndContext(
                                "com.sun.star.ucb.UniversalContentBroker", keys, xCC );

            // Obtain required UCB interfaces XContentIdentifierFactory and XContentProvider
            XContentIdentifierFactory xIdFactory =
                    (XContentIdentifierFactory)UnoRuntime.queryInterface(XContentIdentifierFactory.class, oUCB);
            XContentProvider xProvider =
                    (XContentProvider)UnoRuntime.queryInterface(XContentProvider.class, oUCB);

/////////////////////////////////////////////////////////////////////
// Obtain content object from UCB...
// please NOTICE:
// the url: /home/beppe/OOo-Working-on/Firma-digitale-CNIPA/test-doc-1-signed.odt
// becomes:
//vnd.sun.star.pkg://file:%2F%2F%2Fhome%2Fbeppe%2FOOo-Working-on%2FFirma-digitale-CNIPA%2Ftest-doc-1-signed.odt/
// that is, the package url "file:///home/beppe/OOo-Working-on/Firma-digitale-CNIPA/test-doc-1-signed.odt"
// becomes the package name, where the '/' shall be converted to %2F (hex url mapping, not path!)
// the only valid '/' is the one at the end, marking the start of the package content
// more infos at:
// http://localhost/ooohs-sdk/docs/common/ref/com/sun/star/ucb/PackageContentProvider.html
            System.out.println("Original URL: \n"+aTheDocURL);
// need to encode the url to the correct standard.
// Will use com.sun.star.uri.VndSunStarPkgUrlReferenceFactory
// currently is unpublished, but see issue 87123, it's going to be published.
// obtain the factory
            
//First create the XURIReference factory            
            XUriReferenceFactory xURFact = (XUriReferenceFactory)UnoRuntime.queryInterface(
                            XUriReferenceFactory.class,
                                    xMCF.createInstanceWithContext("com.sun.star.uri.UriReferenceFactory", xCC));
//build the XUriReference
            XUriReference xUriReference = xURFact.parse(aTheDocURL);
// Obtain uri converter factory
            XVndSunStarPkgUrlReferenceFactory xUFact =
                    (XVndSunStarPkgUrlReferenceFactory)UnoRuntime.queryInterface(
                            XVndSunStarPkgUrlReferenceFactory.class, 
                                    xMCF.createInstanceWithContext(
                                    "com.sun.star.uri.VndSunStarPkgUrlReferenceFactory", xCC));

// finally, make the package uri needed            
             XUriReference xPkgUri = xUFact.createVndSunStarPkgUrlReference(xUriReference);

            String aThePackageUrl = xPkgUri.getUriReference() +"/";
            System.out.println("the UCB package is:\n" + aThePackageUrl+"\n");

            XContentIdentifier xIdMFolder = xIdFactory.createContentIdentifier(aThePackageUrl);
 
            XContent xPackContent = xProvider.queryContent(xIdMFolder);
            String contentF = xPackContent.getContentType();
            System.out.println("root folder type: " + contentF);
            if( contentF.contentEquals("application/vnd.sun.star.pkg-folder" )) {
// some info are available here:
//http://localhost/wiki/index.php/Documentation/DevGuide/AppendixC/The_Package_Content_Provider
// and:
// http://localhost/wiki/index.php/Documentation/DevGuide/UCB/Folders

//call the recursive method to print the folder content
                
                examineODTPackageFolder( xProvider, xIdFactory, aThePackageUrl, xPackContent, 0 );
                System.out.println("\n======== end of listing ====");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace(System.out);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    private void readPackageManifest(String aTheDocURL) {
        try {
            String[] keys = new String[2];
            keys[ 0 ] = "Local";
            keys[ 0 ] = "Office";             
// create the ucb, got from DevGuide at:
// http://localhost/wiki/index.php/Documentation/DevGuide/UCB/Instantiating_the_UCB
// Supply configuration to use for this UCB instance...
/*            Object oUCB = xMSF.createInstanceWithArguments(
                                "com.sun.star.ucb.UniversalContentBroker", keys );*/

            Object oUCB = xMCF.createInstanceWithContext("com.sun.star.ucb.UniversalContentBroker", xCC);

            // Obtain required UCB interfaces XContentIdentifierFactory and XContentProvider
            XContentIdentifierFactory xIdFactory =
                    (XContentIdentifierFactory)UnoRuntime.queryInterface(XContentIdentifierFactory.class, oUCB);
            XContentProvider xProvider =
                    (XContentProvider)UnoRuntime.queryInterface(XContentProvider.class, oUCB);

/////////////////////////////////////////////////////////////////////
// Obtain content object from UCB...
// please NOTICE:
// the url: /home/beppe/OOo-Working-on/Firma-digitale-CNIPA/test-doc-1-signed.odt
// becomes:
//vnd.sun.star.pkg://file:%2F%2F%2Fhome%2Fbeppe%2FOOo-Working-on%2FFirma-digitale-CNIPA%2Ftest-doc-1-signed.odt/
// that is, the package url "file:///home/beppe/OOo-Working-on/Firma-digitale-CNIPA/test-doc-1-signed.odt"
// becomes the package name, where the '/' shall be converted to %2F (hex url mapping, not path!)
// the only valid '/' is the one at the end, marking the start of the package content
// more infos at:
// http://localhost/ooohs-sdk/docs/common/ref/com/sun/star/ucb/PackageContentProvider.html
            System.out.println("Original URL: \n"+aTheDocURL);
// need to encode the url to the correct standard.
// Will use com.sun.star.uri.VndSunStarPkgUrlReferenceFactory
// currently is unpublished, but see issue 87123, it's going to be published.
// obtain the factory

//First create the XURIReference factory
            XUriReferenceFactory xURFact = (XUriReferenceFactory)UnoRuntime.queryInterface(
                            XUriReferenceFactory.class,
                                    xMCF.createInstanceWithContext("com.sun.star.uri.UriReferenceFactory", xCC));
//build the XUriReference
            XUriReference xUriReference = xURFact.parse(aTheDocURL);
// Obtain uri converter factory
            XVndSunStarPkgUrlReferenceFactory xUFact =
                    (XVndSunStarPkgUrlReferenceFactory)UnoRuntime.queryInterface(
                            XVndSunStarPkgUrlReferenceFactory.class, 
                                    xMCF.createInstanceWithContext("com.sun.star.uri.VndSunStarPkgUrlReferenceFactory", xCC));

// finally, make the package uri needed            
             XUriReference xPkgUri = xUFact.createVndSunStarPkgUrlReference(xUriReference);

            String aThePackageUrl = xPkgUri.getUriReference() +"/";
            System.out.println("the UCB package is:\n" + aThePackageUrl+"\n");

//            XContentIdentifier xIdPackStream = xIdFactory.createContentIdentifier(aThePackageUrl+"META-INF/manifest.xml");
            XContentIdentifier xIdPackStream = xIdFactory.createContentIdentifier(aThePackageUrl+"content.xml");
//            XContentIdentifier xIdPackStream = xIdFactory.createContentIdentifier(aThePackageUrl+"styles.xml");
 
            XContent xPackStreamContent = xProvider.queryContent(xIdPackStream);
            String contentF = xPackStreamContent.getContentType();
            System.out.println("root folder type: " + contentF);
            if( contentF.contentEquals("application/vnd.sun.star.pkg-stream" )) {
                System.out.println("Found a\n" + xPackStreamContent.getIdentifier().getContentIdentifier() + " element");
//now open the stream content and try to read it
//info on this on:
// http://localhost/ooohs-sdk/docs/common/ref/com/sun/star/ucb/OpenCommandArgument2.html
                OpenCommandArgument2 aArg2 = new OpenCommandArgument2();
                aArg2.Mode = OpenMode.DOCUMENT;//READ the content
                aArg2.Priority = 32768;// Ignored by most implementations
//add the data sink
 /*               XActiveDataSink xactiveDataSink = new PackActiveDataSink();
                aArg2.Sink = xactiveDataSink;            */

                Utilities.showInfo(xPackStreamContent);
                Utilities.showInterfaces(xPackStreamContent,xPackStreamContent);

//                Object oSet =  
//                        executeCommand(xPackStreamContent, "open", aArg2);
                try {
          // Execute command "open". The implementation of the command will
          // supply an XInputStream implementation to the data sink,
          // using helper method executeCommand (see [[Documentation/DevGuide/UCB/Executing Content Commands|Executing Content Commands]])
                    executeCommand(xPackStreamContent, "open", aArg2);
                }
                catch (com.sun.star.ucb.CommandAbortedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);            
                }
                catch (com.sun.star.uno.Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);            
                }
      // Get input stream supplied by the open command implementation.
 /*               XInputStream xData = xactiveDataSink.getInputStream();
                if (xData == null)
                    System.out.println("No data sink supplied by open command!");
                */
      /////////////////////////////////////////////////////////////////////
      // Read data from input stream...
      /////////////////////////////////////////////////////////////////////
//      try {
          // Data buffer. Will be allocated by input stream implementation!
          byte[][] aBuffer = new byte[1][1];
 /*
          int nRead = xData.readSomeBytes(aBuffer, 65536);
          while (nRead > 0) {
              // Process data contained in buffer.
              nRead = xData.readSomeBytes(aBuffer, 65536);
              if( nRead != 0) {
                  System.out.println("read "+nRead+"  elements");
                  for(int xy=0; xy< nRead;xy++) {
                      System.out.print( aBuffer[0][xy] + " ");
                  }                          
              }
          }

          xData.closeInput();
*/
          // EOF.
/*      }
      catch (com.sun.star.io.NotConnectedException e) {
          ... error ...
      }
      catch (com.sun.star.io.BufferSizeExceededException e) {
          ... error ...
      }
      catch (com.sun.star.io.IOException e) {
          ... error ...
      }                */
          
            //convert the object from Object to XDynamicResultSet
/*                Type aType = AnyConverter.getType(oSet);
                XDynamicResultSet xSet = (XDynamicResultSet) AnyConverter.toObject(new com.sun.star.uno.Type
                                                (com.sun.star.ucb.XDynamicResultSet.class), oSet);*/
                System.out.println("\n======== end of listing ====\n\n");
            }
        }
        catch( com.sun.star.ucb.InteractiveAugmentedIOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);            
        }
        catch(Exception ex) {
//            ex.printStackTrace(System.out);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    /** same as other similar, but this uses
     * :: com :: sun :: star :: packages module
     * @param aTheDocURL
     */
    public void examinePackageODT(String aTheDocURL) {
        try {
            // open the package, given the URL
            // get he interfaces of com.sun.star.packages.Package
            
            Object args[]=new Object[1];
            args[0] = aTheDocURL;

/*            XInterface oPkg = (XInterface) xMSF.createInstanceWithArguments(
                        "com.sun.star.packages.Package", args);*/
//            XInterface oPkg = (XInterface) xMSF.createInstance("com.sun.star.packages.Package");
            XInterface oPkg = (XInterface) xMCF.createInstanceWithContext("com.sun.star.packages.Package", xCC);

// lists the available services
            Utilities.showInfo(oPkg);
            Utilities.showInterfaces(oPkg,oPkg);

            // get the XInitialization interface
            XInitialization xInitPckg = (XInitialization) UnoRuntime.queryInterface(
                            XInitialization.class, oPkg);
            // init parameters
            xInitPckg.initialize(args);
            Utilities.showServiceProperties(oPkg,oPkg);
            
            

            XHierarchicalNameAccess xHierachAcc = (XHierarchicalNameAccess) UnoRuntime.queryInterface(
                                                            XHierarchicalNameAccess.class, oPkg);
            if( !xHierachAcc.hasByHierarchicalName("META-INF/manifest.xml") )
                System.out.println(" no manifest ");
            else
                System.out.println(" manifest exists");

//            Utilities.showInfo(xHierachAcc);
//            Utilities.showInterfaces(xHierachAcc);
            
            Object aObj = xHierachAcc.getByHierarchicalName("META-INF/manifest.xml");
//            Object aObj = xHierachAcc.getByHierarchicalName("contents.xml");

            Type aType = AnyConverter.getType(aObj);
            
// get the manifest
            Object oManfst = xMCF.createInstanceWithContext("com.sun.star.packages.manifest", xCC);
            XManifestReader xManfstRead = (XManifestReader) UnoRuntime.queryInterface(
                            XManifestReader.class, oManfst);
            
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void run() {
        try {
            if(SvrInfo.InitConnection()) {
                System.out.println("Connection successful !");           
            //try to get a document
                Object desktop = SvrInfo.getFactory().createInstanceWithContext("com.sun.star.frame.Desktop",
                        SvrInfo.getCompCtx());
            // get the remote service manager
            // query its XDesktop interface, we need the current component
                XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
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
                    }
//arrive here if the document is signable
// obtain the document URL and check the document package
//get this document model
                    XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent);
                    xMCF = SvrInfo.getFactory();
                    xCC = SvrInfo.getCompCtx();

//                    examinePackageODT(xModel.getURL());
//                    examinePackageODT(xModel.getURL());
//                    examinePackageODT("file:///home/beppe/OOo-Working-on/Firma-digitale-CNIPA/test-draw.odg");
                    readPackageManifest(xModel.getURL());
//                    readPackageManifest("file:///home/beppe/OOo-Working-on/Firma-digitale-CNIPA/test-draw.odg");
                }
/*                String[] srvc = xMCF.getAvailableServiceNames();
                for(int xx = 0; xx < srvc.length;xx++) {
                    System.out.println(srvc[xx]);
                }*/
            }
        }
        catch(Exception e) {
            System.out.println("WARNING: exception thrown !\nJob aborted");
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
            theInstance = new Main();
            theInstance.run();
            theInstance.SvrInfo.CloseConnection();            
    }
}

