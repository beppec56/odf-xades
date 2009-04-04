/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) as published
 *  by the European Community, either version 1.1 of the License,
 *  or any later version.
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

package it.plio.ext.xades.utilities;

import com.sun.star.lang.XMultiComponentFactory;
//import com.sun.star.lang.XMultiServiceFactory;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

//import com.sun.star.bridge.XUnoUrlResolver;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.lang.XComponent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * init OOo server connection, contains OOo Server connection objects
 * @author  Beppe
 *
 */
public class OOoServerInfo extends java.lang.Object 
            implements com.sun.star.lang.XEventListener {
    private XComponentContext xRemoteContext = null;
    
    private XMultiComponentFactory xRemoteServiceManager = null;
//    private XUnoUrlResolver xUnoUrlResolver = null;
    private XBridge bridge = null;
    private XComponent xBridgeComponent = null;
    private String unoUrl =
//    "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
//    "uno:socket,host=localhost,port=8105;urp;StarOffice.ServiceManager";
    "uno:socket,host=localhost,port=8106;urp;StarOffice.ServiceManager";
//    private XMultiServiceFactory  mxServiceManager = null;    // reference to remote service manager of singleton connection object

    /** Creates a new instance of OOoServerInfo */
    public OOoServerInfo() {
    }
    
    public XComponentContext getCompCtx()  {
        return xRemoteContext;
    }
   
/*    public synchronized XMultiServiceFactory getSMGR() {
        return mxServiceManager;
    }*/
    
    public XMultiComponentFactory getFactory()  {
        return xRemoteServiceManager;
    }
    
    public void CloseConnection()  {
         if( xBridgeComponent != null) {
             xBridgeComponent.dispose();
             xBridgeComponent = null;
         }
    }

    /** separates the uno-url into 3 different parts.
     */
    protected static String[] parseUnoUrl(  String url )
    {
        String [] aRet = new String [3];

        if( ! url.startsWith( "uno:" ) ) {
            return null;
        }

        int semicolon = url.indexOf( ';' );
        if( semicolon == -1 )
            return null;
        
        aRet[0] = url.substring( 4 , semicolon );
        int nextSemicolon = url.indexOf( ';' , semicolon+1);

        if( semicolon == -1 )
            return null;
        aRet[1] = url.substring( semicolon+1, nextSemicolon );

        aRet[2] = url.substring( nextSemicolon+1);
        return aRet;
    }
    
    protected XMultiComponentFactory getMultiComponentFactory()
        throws com.sun.star.uno.Exception, Exception
    {
        XComponentContext xLocalContext =
               com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);

            // instantiate connector service
            Object x = xLocalContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.connection.Connector", xLocalContext );
            
            XConnector xConnector = (XConnector )UnoRuntime.queryInterface(XConnector.class, x);

            String a[] = parseUnoUrl( unoUrl );
            if( null == a ) {
                throw new com.sun.star.uno.Exception( "Couldn't parse uno-url "+ unoUrl );
            }
            XConnection connection = null;
            // try to connect using the connection string part of the uno-url only.
            try {
                connection = xConnector.connect( a[0] );            
            }
            catch(com.sun.star.connection.NoConnectException ex) {
                return null;
            }

            x = xLocalContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.bridge.BridgeFactory", xLocalContext );

            XBridgeFactory xBridgeFactory = (XBridgeFactory) UnoRuntime.queryInterface(
                XBridgeFactory.class , x );

            // create a nameless bridge with no instance provider
            // using the middle part of the uno-url
            bridge = xBridgeFactory.createBridge( "" , a[1] , connection , null );

            // query for the XComponent interface and add this as event listener
            xBridgeComponent = (XComponent) UnoRuntime.queryInterface(
                                        XComponent.class, bridge );
            xBridgeComponent.addEventListener( this );          

            // get the remote instance 
            x = bridge.getInstance( a[2] );

            // Did the remote server export this object ?
            if( null == x )  {
                throw new com.sun.star.uno.Exception(
                    "Server didn't provide an instance for" + a[2], null );
            }
            // Query the initial object for its main factory interface
            XMultiComponentFactory xOfficeMultiComponentFactory = ( XMultiComponentFactory )
                            UnoRuntime.queryInterface( XMultiComponentFactory.class, x );

            // retrieve the component context (it's not yet exported from the office)
            // Query for the XPropertySet interface.
            XPropertySet xProperySet = ( XPropertySet )
                UnoRuntime.queryInterface( XPropertySet.class, xOfficeMultiComponentFactory );

            // Get the default context from the office server.
            Object oDefaultContext =
                xProperySet.getPropertyValue( "DefaultContext" );
            
            // Query for the interface XComponentContext.
            xRemoteContext = ( XComponentContext ) UnoRuntime.queryInterface(
                    XComponentContext.class, oDefaultContext );

        return xOfficeMultiComponentFactory;
    }

      public void disposing( com.sun.star.lang.EventObject event )
    {
        // remote bridge has gone down, because the office crashed or was terminated.
//        m_officeComponentLoader = null;
        xRemoteServiceManager = null;
    }

    public boolean InitConnection()  {
        try {
            xRemoteServiceManager = getMultiComponentFactory();
            if(xRemoteServiceManager == null)
                return false;
        } catch (Exception ex) {
            Logger.getLogger(OOoServerInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
}
