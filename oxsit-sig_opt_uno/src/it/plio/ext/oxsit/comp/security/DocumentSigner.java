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

package it.plio.ext.oxsit.comp.security;

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.CK_INFO;
import iaik.pkcs.pkcs11.wrapper.CK_TOKEN_INFO;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.pack.DigitalSignatureHelper;
import it.plio.ext.oxsit.ooo.ui.DialogQueryPIN;
import it.plio.ext.oxsit.pkcs11.PKCS11SignerOOo;
import it.plio.ext.oxsit.security.XOX_DocumentSignaturesState;
import it.plio.ext.oxsit.security.XOX_DocumentSigner;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.embed.XStorage;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

/**
 * This service implements the real document signer.
 * receives the doc information from the task  
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every signature
 * ca be retrieved through properties 
 * 
 * @author beppec56
 *
 */
public class DocumentSigner extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XComponent,
			XInitialization,
			XOX_DocumentSigner
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= DocumentSigner.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDOCUMENT_SIGNER_SERVICE };

	protected DynamicLogger m_aLogger;

	// these are the listeners on this document signatures changes
	public HashMap<XChangesListener,XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);

	protected XStorage		m_xDocumentStorage;
	protected XComponentContext	m_xCC;
	private 	XMultiComponentFactory m_xMCF;
	// this document signature state

	/**
	 * 
	 * 
	 * @param _ctx the UNO context
	 */
	public DocumentSigner(XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
    	m_aLogger.enableLogging();
    	m_aLogger.ctor();
	}

	@Override
	public String getImplementationName() {
		// TODO Auto-generated method stub
		m_aLogger.entering("getImplementationName");
		return m_sImplementationName;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.info("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * when instantiated, 
	 * 	_oObj[0] first argument document URL
	 *  _oObj[1] corresponding XStorage object
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		// TODO Auto-generated method stub
		m_aLogger.entering("initialize");		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_DocumentSigner#signDocument(com.sun.star.document.XStorageBasedDocument, it.plio.ext.oxsit.security.cert.XOX_X509Certificate[])
	 * 
	 * gets called from dialog when a document should be signed with independednt certificate signature
	 */
	@Override
	public boolean signDocumentStandard(XFrame xFrame, XStorage xStorage,
			XOX_X509Certificate[] _aCertArray) throws IllegalArgumentException,
			Exception {
		// TODO Auto-generated method stub
		//for the time being only the first certificate is used
		XOX_X509Certificate aCert = _aCertArray[0];
		//get the device this was seen on
		XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(
				XOX_SSCDevice.class, aCert.getSSCDevice());

		
		//to see if all is all right, examine the document structure
		
		String cryptolibrary = xSSCD.getCryptoLibraryUsed();
		
		m_aLogger.log("signDocument with: " +xSSCD.getDescription()+ " cryptolib: "+cryptolibrary);
		//just for test, analyze the document package structure
		DigitalSignatureHelper dg = new DigitalSignatureHelper(m_xMCF,m_xCC);
		
		dg.verifyDocumentSignature(xStorage,null);
		
		//try to get a pin from the user
		DialogQueryPIN aDialog1 =
			new DialogQueryPIN( xFrame, m_xCC, m_xMCF );
		//set the device description, can be used to display information on the device the PIN is asked for
		
		try {
			//PosX e PosY devono essere ricavati dalla finestra genetrice (in questo caso la frame)
			//get the parente window data
//			com.sun.star.awt.XWindow xCompWindow = m_xFrame.getComponentWindow();
//			com.sun.star.awt.Rectangle xWinPosSize = xCompWindow.getPosSize();
			int BiasX = 100;
			int BiasY = 30;
//			System.out.println("Width: "+xWinPosSize.Width+ " height: "+xWinPosSize.Height);
//			XWindow xWindow = m_xFrame.getContainerWindow();
//			XWindowPeer xPeer = xWindow.
			aDialog1.initialize(BiasX,BiasY);
//center the dialog
			short test = aDialog1.executeDialog();
			String sThePin = aDialog1.getThePin();
			char[] chPin = sThePin.toCharArray();
			if( sThePin.length() > 0) {
				m_aLogger.log("sign!");
				PKCS11SignerOOo helper;
				try {
		            SecurityManager sm = System.getSecurityManager();
		            if (sm != null) {
		            	m_aLogger.info("SecurityManager: " + sm);
		            } else {
		            	m_aLogger.info("no SecurityManager.");
		            }
					String Pkcs11WrapperLocal = Helpers.getLocalNativeLibraryPath(m_xCC, PKCS11Implementation.getPKCS11_WRAPPER());
					helper = new PKCS11SignerOOo(m_aLogger,Pkcs11WrapperLocal,cryptolibrary);
//try to sign something simple
					
					long[] nTokens = null;
					try {
						nTokens = helper.getTokenList();
						nTokens = helper.getTokens();
					} catch (PKCS11Exception ex3) {
						m_aLogger.severe("detectTokens, PKCS11Exception "
								+ cryptolibrary, ex3);
					}

					if(nTokens != null) {
						
						for(int i=0;i<nTokens.length;i++) {
							m_aLogger.log("token: "+nTokens[i]);
						}
						helper.getModuleInfo();

						helper.getMechanismInfo();
						//open se
						helper.setTokenHandle(nTokens[0]);

				        helper.openSession();
					//find slots in first token only

						CK_TOKEN_INFO tokenInfo = helper.getTokenInfo(nTokens[0]);

						m_aLogger.log(tokenInfo.toString());
						
						
					
					//from the certificate get the mechanism needed (the subject signature algor)
					//this will be the mechanism used to sign ??
					
					//open again the token, using the saved library
					// search the private key of the certificate at hand


						helper.closeSession();
					}
					helper.libFinalize();					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TokenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				return true;
			}
		}
		catch (com.sun.star.uno.RuntimeException e) {
			m_aLogger.severe(e);
		} catch (BasicErrorException e) {
			m_aLogger.severe(e);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return false;
	}
}
