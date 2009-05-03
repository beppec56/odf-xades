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

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.options.OptionsParametersAccess;
import it.plio.ext.oxsit.security.XOX_AvailableSSCDs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.sun.star.embed.XStorage;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;

/**
 * This is a specification, it may change!
 * This service implements a service to access the SSCDs available on system.
 * receives the doc information from the task  
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every signature
 * can be retrieved through properties 
 * 
 * @author beppec56
 *
 */
public class AvailableSSCDs extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XChangesNotifier,
			XComponent,
			XInitialization,
			XOX_AvailableSSCDs
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= AvailableSSCDs.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sAVAILABLE_SSCD_SERVICE };
	
	protected String 					m_sExtensionSystemPath;
	
	protected String[]					m_sSSCDLibraryPaths;	

	protected DynamicLogger m_aLogger;
	/**
	 * 
	 * 
	 * @param _ctx the UNO context
	 */
	public AvailableSSCDs(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
		m_aLogger.enableLogging();
		try {
			m_sExtensionSystemPath = Helpers.getExtensionInstallationSystemPath(_ctx);
			m_aLogger.ctor("extension installed in: "+m_sExtensionSystemPath);
		} catch (URISyntaxException e) {
			m_aLogger.severe("ctor", "", e);
		} catch (IOException e) {
			m_aLogger.severe("ctor", "", e);
		}

		m_sSSCDLibraryPaths = new String[4];
//grab the paths from the configuration
		OptionsParametersAccess xOptionsConfigAccess = new OptionsParametersAccess(_ctx);
		m_sSSCDLibraryPaths[0] = xOptionsConfigAccess.getText("SSCDFilePath1");
		m_sSSCDLibraryPaths[1] = xOptionsConfigAccess.getText("SSCDFilePath2");
		m_sSSCDLibraryPaths[2] = xOptionsConfigAccess.getText("SSCDFilePath3");
		m_sSSCDLibraryPaths[3] = xOptionsConfigAccess.getText("SSCDFilePath4");
		xOptionsConfigAccess.dispose();
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
		// TODO Auto-generated method stub
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

	// XChangesNotifier
	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#addChangesListener(com.sun.star.util.XChangesListener)
	 */
	@Override
	public void addChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star.util.XChangesListener)
	 */
	@Override
	public void removeChangesListener(XChangesListener _ChangesListener) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		m_aLogger.log("addEventListener");
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 * called to clean up the class before closing
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		m_aLogger.log("dispose");
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		m_aLogger.log("removeEventListener");		
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.security.XOX_AvailableSscdDevices#scanDevices()
	 * called to initiated a scan of the devices available on system.
	 * 
	 */
	@Override
	public void scanDevices() {
		// TODO Auto-generated method stub
		m_aLogger.entering("scanDevices");
	}
}
