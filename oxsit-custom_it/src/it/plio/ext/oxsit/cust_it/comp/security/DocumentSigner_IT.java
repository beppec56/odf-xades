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

package it.plio.ext.oxsit.cust_it.comp.security;

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.CK_TOKEN_INFO;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.pack.DigitalSignatureHelper;
import it.plio.ext.oxsit.ooo.ui.DialogQueryPIN;
import it.plio.ext.oxsit.pkcs11.PKCS11SignerOOo;
import it.plio.ext.oxsit.security.XOX_DocumentSigner;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

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
public class DocumentSigner_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XComponent,
			XInitialization,
			XOX_DocumentSigner
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= DocumentSigner_IT.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDOCUMENT_SIGNER_SERVICE_IT };

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
	public DocumentSigner_IT(XComponentContext _ctx) {
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
		// for the time being only the first certificate is used
		XOX_X509Certificate aCert = _aCertArray[0];
		
		m_aLogger.log("cert label: "+aCert.getCertificateAttributes().getLabel());

		// get the device this was seen on
		XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(
				XOX_SSCDevice.class, aCert.getSSCDevice());

		// to see if all is all right, examine the document structure

		String cryptolibrary = xSSCD.getCryptoLibraryUsed();

		m_aLogger.log("signDocument with: " + xSSCD.getDescription()
				+ " cryptolib: " + cryptolibrary);
		// just for test, analyze the document package structure
		DigitalSignatureHelper dg = new DigitalSignatureHelper(m_xMCF, m_xCC);

		dg.verifyDocumentSignature(xStorage, null);

		// try to get a pin from the user
		DialogQueryPIN aDialog1 = new DialogQueryPIN(xFrame, m_xCC, m_xMCF);
		// set the device description, can be used to display information on the
		// device the PIN is asked for

		try {
			// PosX e PosY devono essere ricavati dalla finestra genetrice (in
			// questo caso la frame)
			// get the parente window data
			// com.sun.star.awt.XWindow xCompWindow =
			// m_xFrame.getComponentWindow();
			// com.sun.star.awt.Rectangle xWinPosSize =
			// xCompWindow.getPosSize();
			int BiasX = 100;
			int BiasY = 30;
			// System.out.println("Width: "+xWinPosSize.Width+
			// " height: "+xWinPosSize.Height);
			// XWindow xWindow = m_xFrame.getContainerWindow();
			// XWindowPeer xPeer = xWindow.
			aDialog1.initialize(BiasX, BiasY);
			// center the dialog
			aDialog1.executeDialog();
			char[] myPin = aDialog1.getPin();
			if (myPin.length > 0) {
				m_aLogger.log("sign!");
				// convert certificate in Java format

				X509Certificate signatureCert = Helpers.getCertificate(aCert);

				PKCS11SignerOOo helper;
				try {
					SecurityManager sm = System.getSecurityManager();
					if (sm != null) {
						m_aLogger.info("SecurityManager: " + sm);
					} else {
						m_aLogger.info("no SecurityManager.");
					}
					String Pkcs11WrapperLocal = Helpers
							.getPKCS11WrapperNativeLibraryPath(m_xCC);
					helper = new PKCS11SignerOOo(m_aLogger, Pkcs11WrapperLocal,
							cryptolibrary);

					byte[] baSha1 = { 0x63, (byte) 0xAA, 0x4D, (byte) 0xD0,
							(byte) 0xF3, (byte) 0x8F, 0x62, (byte) 0xDC,
							(byte) 0xF7, (byte) 0x6F, (byte) 0xF2, (byte) 0x09,
							(byte) 0xA7, 0x5B, 0x01, 0x4E, 0x78, (byte) 0xF2,
							(byte) 0xF1, 0x31 };
					// try to sign something simple
					/*
					 * String sTest = "Y6pN0POPYtz3b/IJp1sBTnjy8TE="
					 */

					long[] nTokens = null;
					try {
						nTokens = helper.getTokenList();
						nTokens = helper.getTokens();
					} catch (PKCS11Exception ex3) {
						m_aLogger.severe("detectTokens, PKCS11Exception "
								+ cryptolibrary, ex3);
					}

					if (nTokens != null) {
						// search in the available tokens the one with the
						// certificate
						// selected
						for (int i = 0; i < nTokens.length; i++) {
							m_aLogger.log("token: " + nTokens[i]);
						}
						// helper.getModuleInfo(); info on pkcs#11 library

						helper.getMechanismInfo();
						// open session on this token
						helper.setTokenHandle(nTokens[0]);
						try {
							// helper.openSession(myPin);
							helper.openSession();

							// find private key and certificate in first token
							// only
							try {
								CK_TOKEN_INFO tokenInfo = helper
										.getTokenInfo(nTokens[0]);

								m_aLogger.log(tokenInfo.toString());

								// from the certificate get the mechanism needed
								// (the subject signature algor)
								// this will be the mechanism used to sign ??

								// from the certificate get the certificate
								// handle
								try {
									long certHandle = helper
											.findCertificate(signatureCert);

									// then the certificate handle get the
									// corresponding
									// private key handle
									// long sigKeyHandle =
									// helper.findSignatureKeyFromCertificateHandle(certHandle);
									// get key label as well
								} catch (PKCS11Exception e) {
									m_aLogger.severe(e);
								} catch (CertificateEncodingException e) {
									m_aLogger.severe(e);
								} catch (IOException e) {
									m_aLogger.severe(e);
								}
							} catch (Throwable e) {
								m_aLogger.severe(e);
							}
							// helper.logout();
							helper.closeSession();
						} catch (TokenException ex) {
							m_aLogger.log("Messaggio helper.openSession(): "
									+ ex.getMessage());
							if (ex
									.toString()
									.startsWith(
											"iaik.pkcs.pkcs11.wrapper.PKCS11Exception: CKR_PIN_INCORRECT")
									|| ex.toString().startsWith(
											"CKR_PIN_INCORRECT")) {
								// errorPresent = true;
								m_aLogger.log("PIN sbagliato.");
								// current = ERROR;

							}

							else if (ex.getMessage().startsWith(
									"CKR_PIN_LOCKED")) {
								// errorPresent = true;
								m_aLogger.log("PIN bloccato.");
							} else if (ex.getMessage().startsWith(
									"CKR_PIN_LEN_RANGE")) {
								// errorPresent = true;
								m_aLogger
										.log("PIN sbagliato: Lunghezza sbagliata.");
							} else if (ex.getMessage().startsWith(
									"CKR_TOKEN_NOT_RECOGNIZED")) {
								// errorPresent = true;
								m_aLogger.log("CKR_TOKEN_NOT_RECOGNIZED.");
							} else if (ex.getMessage().startsWith(
									"CKR_FUNCTION_FAILED")) {
								// errorPresent = true;
								m_aLogger.log("CKR_FUNCTION_FAILED.");
							}

							else if (ex.getMessage().startsWith(
									"CKR_ARGUMENTS_BAD")) {
								// errorPresent = true;
								m_aLogger.log("CKR_ARGUMENTS_BAD.");
							} else {
								// inserisci tutte le TokenException!!!
								// errorPresent = true;
								// errorMsg =
								// "PKCS11Exception:\n"+ex.getMessage()+".";
							}
						}

					}
					helper.libFinalize();
				} catch (IOException e) {
					m_aLogger.severe(e);
				} catch (TokenException e) {
					m_aLogger.severe(e);
				} catch (NullPointerException e) {
					m_aLogger.severe(e);
				} catch (URISyntaxException e) {
					m_aLogger.severe(e);
				} catch (Throwable e) {
					m_aLogger.severe(e);
				}

				return true;
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			m_aLogger.severe(e);
		} catch (BasicErrorException e) {
			m_aLogger.severe(e);
		} catch (Exception e) {
			m_aLogger.severe(e);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
