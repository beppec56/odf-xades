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

package it.plio.ext.xades.ooo;

import it.plio.ext.xades.logging.XDynamicLogger;

import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;

public class ConfigurationAccess {
	protected XComponentContext			m_xContext;	 
	protected XMultiServiceFactory	m_ConfProvider = null;
	private XMultiComponentFactory	m_xServiceManager = null;
	protected XDynamicLogger			m_logger;

	public ConfigurationAccess(XComponentContext _xContext) {
		m_xContext = _xContext;
		m_xServiceManager = m_xContext.getServiceManager(); 
		getProvider();
		m_logger = new XDynamicLogger(this, _xContext); // should be enabled by subclass
	}

	/**
	 * Create a default configuration provider
	 * Important: in run-time environment the provider should NEVER be disposed of.
	 * It is used everywhere!
	 */
	private void getProvider() {
		final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";

		// create the provider and return it as a XMultiServiceFactory
		try {
			m_ConfProvider =(XMultiServiceFactory)
			    UnoRuntime.queryInterface(XMultiServiceFactory.class, 
			        m_xServiceManager.createInstanceWithContext(sProviderService,
			                                                   m_xContext));
		} catch (Exception e) {
			m_logger.severe("getProvider", "cannot get a Provider Service", e);
		}
	}

	/**
	 * Create a specified read-only configuration view
	 */
	protected Object createConfigurationReadOnlyView(String sPath)
			throws com.sun.star.uno.Exception {

		// The service name: Need only read access:
		final String sReadOnlyView = "com.sun.star.configuration.ConfigurationAccess";

		// creation arguments: nodepath
		com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
		aPathArgument.Name = "nodepath";
		aPathArgument.Value = sPath;

		Object[] aArguments = new Object[1];
		aArguments[0] = aPathArgument;

		Object xViewRoot = m_ConfProvider.createInstanceWithArguments( sReadOnlyView,
				aArguments );
//		Utilities.showInterfaces( (XInterface) xViewRoot );
//get a interface where a listener can add itself for changes
		return xViewRoot;
	}

	/**
	 * Create a specified updatable configuration view using default
	 * synchronicity
	 */
	protected Object createConfigurationReadWriteView(String sPath)
			throws com.sun.star.uno.Exception {
		// The service name: Need update access:
		final String cUpdatableView = "com.sun.star.configuration.ConfigurationUpdateAccess";

		// creation arguments: nodepath
		com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
		aPathArgument.Name = "nodepath";
		aPathArgument.Value = sPath;
		// creation arguments: commit mode - write-through or write-back
		com.sun.star.beans.PropertyValue aModeArgument = new com.sun.star.beans.PropertyValue();
		aModeArgument.Name = "enableasync";
		aModeArgument.Value = new Boolean( false );

		Object[] aArguments = new Object[2];
		aArguments[0] = aPathArgument;
		aArguments[1] = aModeArgument;

		// create the view
		Object xViewRoot = m_ConfProvider.createInstanceWithArguments( cUpdatableView,
				aArguments );
		return xViewRoot;
	}

	public void commitChanges(Object _oFramesConfView) {
		// update the store
		// commit the changes
		if (_oFramesConfView != null) {
			XChangesBatch xUpdateControl = (XChangesBatch) UnoRuntime.queryInterface(
					XChangesBatch.class, _oFramesConfView );
			try {
				xUpdateControl.commitChanges();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * to be used by derived class, when needed
	 */
	public void enableLogging() {
		m_logger.enableLogging();
	}
	
	/**
	 * to be used by derived class, when needed
	 */
	public void disableLogging() {
		m_logger.disableLogging();
	}
}
