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

import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;
import it.infocamere.freesigner.gui.ReadCertsTask;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.comp.security.cert.test.TestOnCertificates;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.options.OptionsParametersAccess;
import it.plio.ext.oxsit.security.XOX_AvailableSSCDs;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;
import it.trento.comune.j4sign.pcsc.CardInReaderInfo;
import it.trento.comune.j4sign.pcsc.CardInfo;
import it.trento.comune.j4sign.pcsc.PCSCHelper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

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
public class AvailableSSCDs extends ComponentBase
		// help class, implements XTypeProvider, XInterface, XWeak
		implements XServiceInfo, XChangesNotifier, XComponent, XInitialization,
		XOX_AvailableSSCDs {

	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_MCF;
	
	// the name of the class implementing this object
	public static final String m_sImplementationName = AvailableSSCDs.class
			.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { GlobConstant.m_sAVAILABLE_SSCD_SERVICE };

	protected String m_sExtensionSystemPath;

	protected String m_sSSCDLibraryPath;
	protected boolean m_bSSCDAutomaticDetection;

	//the list of available devices
	protected Vector<XOX_SSCDevice>	m_aSSCDList; 
	
	protected IDynamicLogger m_aLogger;

	/**
	 * 
	 * 
	 * @param _ctx
	 *            the UNO context
	 */
	public AvailableSSCDs(XComponentContext _ctx) {
		m_aLogger = new DynamicLoggerDialog(this, _ctx);
		m_xCC = _ctx;
		m_MCF = m_xCC.getServiceManager();
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
	 * when instantiated, _oObj[0] first argument document URL _oObj[1]
	 * corresponding XStorage object
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
	 * @see it.plio.ext.oxsit.security.XOX_AvailableSSCDs#getAvailableSSCDevices()
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
	 * @see it.plio.ext.oxsit.security.XOX_AvailableSSCDs#getHasSSCDevices()
	 */
	@Override
	public int getHasSSCDevices() {
		return m_aSSCDList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.plio.ext.oxsit.security.XOX_AvailableSscdDevices#scanDevices()
	 * called to initiated a scan of the devices available on system.
	 */
	@Override
	public void scanDevices(boolean _bUseGUI) {
		m_aLogger.entering("scanDevices");
		try {
		//get the library path property
		//
		m_aLogger.log("java.class.path: \""+System.getProperty("java.class.path")+"\"");
		m_aLogger.log("java.library.path: \""+
				System.getProperty("java.library.path")+"\"");

		IDynamicLogger aLogger = null;
		if(_bUseGUI)
			aLogger = new DynamicLoggerDialog(this,m_xCC);
		else
			aLogger = new DynamicLogger(this,m_xCC);

		PCSCHelper pcsc = new PCSCHelper(true, Helpers.getLocalNativeLibraryPath(m_xCC, GlobConstant.m_sPCSC_WRAPPER_NATIVE), aLogger);
//		PCSCHelper pcsc = new PCSCHelper(true, null);

		m_aLogger.log("After 'new PCSCHelper'");

		java.util.List<CardInReaderInfo> infos = pcsc.findCardsAndReaders();

		CardInfo ci = null;
		Iterator<CardInReaderInfo> it = infos.iterator();
		int indexToken = 0;
		int indexReader = 0;

		while (it.hasNext()) {
			m_aLogger.log("Reader " + indexReader + ")");

			CardInReaderInfo cIr = it.next();
			String currReader = cIr.getReader();

			ci = cIr.getCard();
			
			if (ci != null) {
//instantiate a SSCDevice service object to hold the token device information and
				//the detected certificates
				
				Object oAnSSCD = null;
				XOX_SSCDevice xSSCDevice = null;
				try {
					oAnSSCD = m_MCF.createInstanceWithContext(GlobConstant.m_sSSCD_SERVICE, m_xCC);
					xSSCDevice = (XOX_SSCDevice)UnoRuntime.queryInterface(XOX_SSCDevice.class, oAnSSCD);

					xSSCDevice.setDescription(ci.getProperty("description"));
					xSSCDevice.setManufacturer(ci.getProperty("manufacturer"));
					xSSCDevice.setATRcode(ci.getProperty("atr"));
					xSSCDevice.setCryptoLibraryUsed(ci.getProperty("lib"));

					m_aLogger.log("\tLettura certificati");

					// set the library to be used, locally
					String Pkcs11WrapperLocal = Helpers.getLocalNativeLibraryPath(m_xCC, PKCS11Implementation.getPKCS11_WRAPPER());
					
					m_aLogger.info(Pkcs11WrapperLocal);
					
					ReadCertsTask rt = new ReadCertsTask(aLogger, Pkcs11WrapperLocal, cIr);
					Collection<X509Certificate> certsOnToken = rt.getCertsOnToken();
					if (certsOnToken != null) {
						Iterator<X509Certificate> certIt = certsOnToken.iterator();
						while (certIt.hasNext()) {
	//add this certificate to our structure
							X509Certificate cert = (X509Certificate) certIt.next();
							try {
								//this try will only check for correctness, before
								//instantiating the service
								cert.getEncoded();
//all seems right, instantiate the certificate service
								//now create the Certificate Control UNO objects
								//first the certificate compliance control
								//
								Object oACCObj = m_MCF.createInstanceWithContext(GlobConstant.m_sCERTIFICATE_COMPLIANCE_SERVICE_IT, m_xCC);

								//prepare objects for subordinate service
								Object[] aArguments = new Object[3];
//								byte[] aCert = cert.getEncoded();
								//set the certificate raw value
								aArguments[0] = cert.getEncoded();//aCert;
								aArguments[1] = new Boolean(_bUseGUI);//FIXME change according to UI (true) or not UI (false)
								aArguments[2] = oACCObj; //the compliance checker object, which implements the needed interface

								Object oACertificate = m_MCF.createInstanceWithArgumentsAndContext(GlobConstant.m_sQUALIFIED_CERTIFICATE_SERVICE,
										aArguments, m_xCC);
								//get the main interface
								XOX_QualifiedCertificate xQualCert = 
									(XOX_QualifiedCertificate)UnoRuntime.queryInterface(XOX_QualifiedCertificate.class, oACertificate);

//								xQualCert.setDEREncoded(cert.getEncoded());
								//add it to this token collection
								xSSCDevice.addAQualifiedCertificate(xQualCert);
							} catch (CertificateEncodingException e) {
								m_aLogger.severe("scanDevices",e);
							}	
						}
						rt.closeSession();
						rt.libFinalize();
						indexToken++;
					}
					//add the token to the list
					addSSCDevice(xSSCDevice);
				} catch (Exception e) {
					m_aLogger.severe("scanDevices",e);
				}
			} else {
				m_aLogger.log("No card in reader '" + currReader + "'!");

			}
			indexReader++;
		}
		} catch (Throwable e) {
			m_aLogger.severe("scanDevices",e);
		}
	}

	public void testCertificateDisplay() {
		TestOnCertificates xtest = new TestOnCertificates(m_xCC);
		xtest.testMethod();
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_AvailableSSCDs#addSSCDevice(it.plio.ext.oxsit.security.XOX_SSCDevice)
	 * 
	 * add the single parameter device
	 */
	@Override
	public void addSSCDevice(XOX_SSCDevice _aSSCD) {
		// TODO Auto-generated method stub
		// the single device
		m_aSSCDList.add(_aSSCD);
	}
}
