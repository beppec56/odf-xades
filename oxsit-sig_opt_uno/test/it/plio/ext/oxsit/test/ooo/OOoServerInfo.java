/*
 * OOoServerInfo.java
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
 * librerie OOo usate:
 * jurt.jar, jut.jar, juh.jar, ridl.jar, unoidl.jar
 */

package it.plio.ext.oxsit.test.ooo;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import com.sun.star.bridge.XUnoUrlResolver;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
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
    private XUnoUrlResolver xUnoUrlResolver;
    private XBridge bridge = null;
    private XComponent xBridgeComponent;
    private String unoUrl =
//    "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
    "uno:socket,host=localhost,port=9000;urp;StarOffice.ServiceManager";
    private XMultiServiceFactory  mxServiceManager = null;    // reference to remote service manager of singleton connection object

//    private com.sun.star.frame.XComponentLoader m_officeComponentLoader;
//    private XComponentContext m_ctx;
//    private XMultiComponentFactory xOfficeMultiComponentFactory;

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
    
/*    protected XMultiComponentFactory getRemoteServiceManager(String unoUrl)  {
        //SysMsgArea sMsgArea = SysMsgArea.Instance();
        try {
            if (xRemoteContext == null) {
                    // First step: create local component context, get local servicemanager and
                    // ask it to create a UnoUrlResolver object with an XUnoUrlResolver interface
                XComponentContext xLocalContext =
                com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);
                XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
                Object urlResolver  =
                            xLocalServiceManager.createInstanceWithContext(
                                    "com.sun.star.bridge.UnoUrlResolver", xLocalContext );
                    // query XUnoUrlResolver interface from urlResolver object
                xUnoUrlResolver = (XUnoUrlResolver) UnoRuntime.queryInterface(
                            XUnoUrlResolver.class, urlResolver );
                    
                    // Second step: use xUrlResolver interface to import the remote StarOffice.ServiceManager,
                    // retrieve its property DefaultContext and get the remote servicemanager
                Object initialObject = xUnoUrlResolver.resolve( unoUrl );
                XPropertySet xPropertySet =
                    (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, initialObject);
                Object context = xPropertySet.getPropertyValue("DefaultContext");
                xRemoteContext = (XComponentContext)UnoRuntime.queryInterface(
                    XComponentContext.class, context);
                mxServiceManager =
                                (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class,
                                        initialObject);
            }
            return xRemoteContext.getServiceManager();
        }
        catch (java.lang.Exception e) {
            System.out.println("\nWARNING: Cannot connect to Open Office instance, did you start it as a server ?");
            System.out.println("The connection URL is:\n"+unoUrl);
        }
        return null;
    }

    public boolean InitConnection()  {
        xRemoteServiceManager = this.getRemoteServiceManager(unoUrl);
        if(xRemoteServiceManager == null)
            return false;
        else
            return true;
    }*/
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
            // connect using the connection string part of the uno-url only.
            XConnection connection = xConnector.connect( a[0] );
        
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

    public boolean InitConnection() throws Exception  {
        try {
            xRemoteServiceManager = getMultiComponentFactory();
            if(xRemoteServiceManager == null)
                return false;
        } catch (Exception ex) {
            Logger.getLogger(OOoServerInfo.class.getName()).log(Level.SEVERE, null, ex);
            throw (ex);
        }
        return true;
    }
}
