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

package com.yacme.ext.oxsit.cust_it.comp.security;

import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;
import it.plio.ext.oxsit.security.XOX_SSCDManagement;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.options.OptionsParametersAccess;
import com.yacme.ext.oxsit.pcsc.CardInReaderInfo;
import com.yacme.ext.oxsit.pcsc.CardInfoOOo;
import com.yacme.ext.oxsit.pcsc.PCSCHelper;
import com.yacme.ext.oxsit.pkcs11.CertificatePKCS11Attributes;
import com.yacme.ext.oxsit.security.PKCS11TokenAttributes;
import com.yacme.ext.oxsit.security.ReadCerts;

/**
 * This is a specification, it may change! This service implements a service to
 * access the SSCDs available on system. receives the doc information from the
 * task
 * 
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every
 * signature can be retrieved through properties
 * 
 * @author beppec56
 * 
 */
public class AvailableSSCDs_IT extends ComponentBase
		// help class, implements XTypeProvider, XInterface, XWeak
		implements XServiceInfo, XChangesNotifier, XComponent, XInitialization,
		XOX_CertificatePKCS11Attributes, //added for convenience
										//so we use this UNO component to pass
										//certificate incormation when addig a certificate
										//to the list
		XOX_SSCDManagement {

	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_xMCF;
	protected XFrame				m_xFrame;

	// the name of the class implementing this object
	public static final String m_sImplementationName = AvailableSSCDs_IT.class
			.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { ConstantCustomIT.m_sAVAILABLE_SSCD_SERVICE };

	protected String m_sExtensionSystemPath;

	protected String m_sSSCDLibraryPath;
	protected boolean m_bSSCDAutomaticDetection;

	//the list of available devices
	protected Vector<XOX_SSCDevice>	m_aSSCDList; 

	protected IDynamicLogger m_aLogger;
	
	//the new three fields are the one needed
	//to pass data when instantiating the sscd device
	//to make the SSCD able to retrieve certificate PKCS11 attributes
	//without the need to create a new service
	private byte[] m_aDEREncoded;
	private byte[] m_aCertID;
	private String m_sCertLabel;

	/**
	 * 
	 * 
	 * @param _ctx
	 *            the UNO context
	 */
	public AvailableSSCDs_IT(XComponentContext _ctx) {
		m_aLogger = new DynamicLoggerDialog(this, _ctx);
		m_xCC = _ctx;
		m_xMCF = m_xCC.getServiceManager();
		m_aLogger.enableLogging();
		try {
			m_sExtensionSystemPath = Helpers
					.getExtensionInstallationSystemPath(_ctx);
			m_aLogger.ctor("extension installed in: " + m_sExtensionSystemPath);
		} catch (URISyntaxException e) {
			m_aLogger.severe("ctor", "", e);
		} catch (IOException e) {
			m_aLogger.severe("ctor", "", e);
		}

		// grab the configuration information
		OptionsParametersAccess xOptionsConfigAccess = new OptionsParametersAccess(
				_ctx);
		m_bSSCDAutomaticDetection = xOptionsConfigAccess
				.getBoolean("SSCDAutomaticDetection");
		m_sSSCDLibraryPath = xOptionsConfigAccess.getText("SSCDFilePath1");
		xOptionsConfigAccess.dispose();
		m_aSSCDList = new Vector<XOX_SSCDevice>(10,1);
	}

	@Override
	public String getImplementationName() {
//		m_aLoggerDialog.entering("getImplementationName");
		return m_sImplementationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
//		m_aLoggerDialog.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.info("supportsService", _sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals(m_sServiceNames[i]))
				return true;
		}
		return false;
	}

	// XChangesNotifier
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.star.util.XChangesNotifier#addChangesListener(com.sun.star.util
	 * .XChangesListener)
	 */
	@Override
	public void addChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star
	 * .util.XChangesListener)
	 */
	@Override
	public void removeChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * when instantiated, 
	 * _oObj[0] a frame if a frame is needed for the interface
	 * _oObj[1]
	 * corresponding XStorage object
	 * _oObj 
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		// TODO Auto-generated method stub
		m_aLogger.entering("initialize");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		super.addEventListener(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#dispose() called to clean up the class
	 * before closing
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		m_aLogger.entering("dispose");
//remove all the device, calling the dispose method of each one
		if(!m_aSSCDList.isEmpty()) {
			for(int i=0; i< m_aSSCDList.size();i++) {
				XOX_SSCDevice aSSCD = m_aSSCDList.get(i);
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, aSSCD);
				if(xComp != null)
					xComp.dispose();
			}
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDManagement#getAvailableSSCDevices()
	 */
	@Override
	public XOX_SSCDevice[] getAvailableSSCDevices() {
		XOX_SSCDevice[] ret = null;
		//detect the number of vector present
		if(!m_aSSCDList.isEmpty()) {
			ret = new XOX_SSCDevice[m_aSSCDList.size()];
			try {
			m_aSSCDList.copyInto(ret);
			} catch(NullPointerException ex) {
				m_aLogger.severe("getAvailableSSCDevices",ex);
			} catch(IndexOutOfBoundsException ex) {
				m_aLogger.severe("getAvailableSSCDevices",ex);
			} catch(ArrayStoreException ex) {
				m_aLogger.severe("getAvailableSSCDevices",ex);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDManagement#getHasSSCDevices()
	 */
	@Override
	public int getHasSSCDevices() {
		return m_aSSCDList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yacme.ext.oxsit.security.XOX_AvailableSscdDevices#scanDevices()
	 * called to initiated a scan of the devices available on system.
	 */
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDManagement#scanDevices(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
	 */
	@Override
	public void scanDevices(XFrame _aFrame, XComponentContext arg1) throws Exception {
		m_aLogger.entering("scanDevices");
        XStatusIndicator xStatusIndicator = null;
		try {
			//get the library path property
			//
			m_aLogger.log("java.class.path: \""+System.getProperty("java.class.path")+"\"");
			m_aLogger.log("java.library.path: \""+
					System.getProperty("java.library.path")+"\"");
	
			IDynamicLogger aLogger = null;
			if(_aFrame != null)
				aLogger = new DynamicLoggerDialog(this,m_xCC);
			else
				aLogger = new DynamicLogger(this,m_xCC);
	        if (_aFrame != null) {
	        	//check interface
	        	//
	        	XStatusIndicatorFactory xFact = (XStatusIndicatorFactory)UnoRuntime.queryInterface(XStatusIndicatorFactory.class,_aFrame);
	        	if(xFact != null) {
	        		xStatusIndicator = xFact.createStatusIndicator();
	        		if(xStatusIndicator != null)
	        			xStatusIndicator.start("", 100); //meaning 100%
	        	}
	        }
	
			PCSCHelper pcsc = new PCSCHelper(_aFrame,m_xCC, true, Helpers.getLocalNativeLibraryPath(m_xCC, GlobConstant.m_sPCSC_WRAPPER_NATIVE), aLogger);

//			m_aLogger.log("After 'new PCSCHelper'");
			
			if(pcsc.getReaders() != null ) {
				java.util.List<CardInReaderInfo> infos = pcsc.findCardsAndReaders();
		
				CardInfoOOo ci = null;
				Iterator<CardInReaderInfo> it = infos.iterator();
				int indexReader = 0;

				while (it.hasNext()) {
					m_aLogger.log("Reader " + indexReader + ")");
		
					CardInReaderInfo cIr = it.next();
					String currReader = cIr.getReader();
					ci = cIr.getCard();
					
					if (ci != null) {
		//instantiate a SSCDevice_IT service object to hold the token device information and
						//the detected certificates
						
						Object oAnSSCD = null;
						XOX_SSCDevice xSSCDevice = null;
						try {

							// set the library to be used, locally
							String Pkcs11WrapperLocal = Helpers.getPKCS11WrapperNativeLibraryPath(m_xCC);

							m_aLogger.info(Pkcs11WrapperLocal);
							ReadCerts rt = new ReadCerts(xStatusIndicator, aLogger, Pkcs11WrapperLocal, cIr);
							//get the number of token/slot (1 token = 1 slot)
							long[] availableToken = rt.getTokens();

							if (availableToken != null) {
								for (int i = 0; i < availableToken.length; i++) {
									PKCS11TokenAttributes aTk = rt.getTokenAttributes(availableToken[i]);
									oAnSSCD = m_xMCF.createInstanceWithContext(ConstantCustomIT.m_sSSCD_SERVICE, m_xCC);
									xSSCDevice = (XOX_SSCDevice) UnoRuntime.queryInterface(XOX_SSCDevice.class, oAnSSCD);

									xSSCDevice.setDescription(ci.m_sDescription);
									xSSCDevice.setManufacturer(ci.m_sManufacturer);
									xSSCDevice.setATRcode(ci.getATRCode());
									m_aLogger.log("ATR code: " + ci.getATRCode());
									String sLibs = ci.getDefaultLib() + " ("
											+ ((ci.getOsLib().length() > 0) ? (ci.getOsLib()) : "")
											+ ((ci.getOsLibAlt1().length() > 0) ? (", " + ci.getOsLibAlt1()) : "")
											+ ((ci.getOsLibAlt2().length() > 0) ? (", " + ci.getOsLibAlt2()) : "")
											+ ((ci.getOsLibAlt3().length() > 0) ? (", " + ci.getOsLibAlt3()) : "") + ")";

									xSSCDevice.setCryptoLibrariesConfigured(sLibs);
									xSSCDevice.setCryptoLibraryUsed(ci.getDefaultLib());

									m_aLogger.log("\tRecupero certificati");
									if (xStatusIndicator != null) {
										xStatusIndicator.setText("Recupero certificati");
										xStatusIndicator.setValue(5);
									}

									Collection<CertificatePKCS11Attributes> certsOnToken = rt.getCertsOnToken(i);
									if (certsOnToken != null && !certsOnToken.isEmpty()) {
										Iterator<CertificatePKCS11Attributes> certIt = certsOnToken.iterator();
										while (certIt.hasNext()) {
											//add this certificate to our structure
											CertificatePKCS11Attributes cert = certIt.next();
						                	cert.setToken(aTk);
											m_aLogger.log("found on token: " + cert.getToken().toString());
											//all seems right, add the device the certificate
											xSSCDevice.setTokenLabel(aTk.getLabel());
											xSSCDevice.setTokenSerialNumber(aTk.getSerialNumber());
											xSSCDevice.setTokenManufacturerID(aTk.getManufacturerID());
											xSSCDevice.setTokenMinimumPINLenght((int) aTk.getMinPinLen());
											xSSCDevice.setTokenMaximumPINLenght((int) aTk.getMaxPinLen());
											setDEREncoded(cert.getCertificateValueDEREncoded());
											setID(cert.getCertificateID());
											setLabel(cert.getCertificateLabel());
											xSSCDevice.addCertificate(this);
										}
										//add the token to the list
										addSSCDevice(xSSCDevice);
									}
								}
								rt.libFinalize();
							}
						} catch (java.io.IOException e) {
							//thrown when there is something wrong on the pkcs#11 library...
							m_aLogger.severe("scanDevices: ATR code:\n" + ci.getATRCode() + "\n", e);
						} catch (java.lang.Exception e) {
							m_aLogger.severe("scanDevices: ATR code: " + ci.getATRCode(), e);
						}
					} else {
						m_aLogger.log("No card in reader '" + currReader + "'!");
					}
					indexReader++;
				}
			}
		} catch (Throwable e) {
			m_aLogger.severe("scanDevices",e);
		}
		if(xStatusIndicator != null)
			xStatusIndicator.end();
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_SSCDManagement#addSSCDevice(com.yacme.ext.oxsit.security.XOX_SSCDevice)
	 * 
	 * add the single parameter device
	 */
	@Override
	public void addSSCDevice(XOX_SSCDevice _aSSCD) {
		// the single device
		m_aSSCDList.add(_aSSCD);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#getDEREncoded()
	 */
	@Override
	public byte[] getDEREncoded() {
		return m_aDEREncoded;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#setDEREncoded(byte[])
	 */
	@Override
	public void setDEREncoded(byte[] aEncoding) {
		m_aDEREncoded = aEncoding;
		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#getID()
	 */
	@Override
	public byte[] getID() {
		return m_aCertID;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#setID(char[])
	 */
	@Override
	public void setID(byte[] aID) {
		m_aCertID = aID;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#getLabel()
	 */
	@Override
	public String getLabel() {
		return m_sCertLabel;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificatePKCS11Attributes#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(String sLabel) {
		m_sCertLabel = sLabel;
	}
}