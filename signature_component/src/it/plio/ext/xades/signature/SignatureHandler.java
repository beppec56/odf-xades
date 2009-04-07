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

package it.plio.ext.xades.signature;

import java.util.Date;

import it.plio.ext.xades.logging.XDynamicLogger;
import it.plio.ext.xades.ooo.GlobConstant;
import it.plio.ext.xades.ooo.ui.DialogChooseSignatureTypes;
//import it.plio.ext.xades.ooo.ui.DialogListCertificates;
import it.plio.ext.xades.signature.dispatchers.ImplBeforeSaveAsDispatch;
import it.plio.ext.xades.signature.dispatchers.ImplBeforeSaveDispatch;
import it.plio.ext.xades.signature.dispatchers.ImplXAdESSignatureDispatch;
import it.plio.ext.xades.signature.dispatchers.ImplXAdESSignatureDispatchTB;
import it.plio.ext.xades.signature.dispatchers.ImplOnHelpDispatch;
import it.plio.ext.xades.signature.dispatchers.ImplSelectSignatureDispatch;

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowAttribute;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XDispatchResultListener;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseListener;
import com.sun.star.util.XCloseable;

/**
 * This class represents the UNO objects that implements the signature engine
 * 
 * @author beppe
 * 
 */
public class SignatureHandler extends com.sun.star.lib.uno.helper.WeakBase
			implements XServiceInfo, // general
						XInitialization, // to implement the ProtocolHandler service
						XDispatchProvider, // to implement the ProtocolHandler service
						XCloseable {

	// needed for registration
	public static final String			m_sImplementationName			= SignatureHandler.class
																				.getName();
	public static final String[]		m_sServiceNames					= { "com.sun.star.frame.ProtocolHandler" };

	private XFrame						m_xFrame; // use when frame is needed as reference

	private XComponentContext			m_xComponentContext				= null;
	private XComponent					m_xCurrentComponent				= null;

	@SuppressWarnings("unused")
	// may be we will need it afterward...
	private XMultiServiceFactory		m_xFactory						= null;

	protected XMultiComponentFactory	m_xRemoteServiceManager			= null;
	protected XMultiComponentFactory	m_xMultiComponentFactory		= null;

	/**
	 * The toolkit, that we can create UNO dialogs.
	 */
	private static XToolkit				m_xToolkit						= null;

	// next 3 static vars are for debug only
	public static int					m_nOnLoadCount					= 0;
	public static int					m_nOnSaveCount					= 0;
	public static int					m_nOnSaveAsCount				= 0;

	private XDispatch					m_aImplSelSignatureDispatch		= null;
	private XDispatch					m_aImplBeforeSaveDispatch		= null;
	private XDispatch					m_aImplBeforeSaveAsDispatch		= null;
	private XDispatch					m_aImplXAdESSignatureDispatchTB	= null;
	private XDispatch					m_aImplOnHelpDispatch			= null;
	private XDispatch					m_aImplXAdESSignatureDispatch	= null;
	private XDynamicLogger				m_logger;

	/**
	 * Constructs a new instance
	 * 
	 * @param context
	 *            the XComponentContext
	 */
	public SignatureHandler(XComponentContext context) {
		m_logger = new XDynamicLogger(this,context);
		m_logger.info( "ctor" );

		m_xComponentContext = context;
		// passert("m_xComponentContext",m_xComponentContext);
		try {
			m_xRemoteServiceManager = this.getRemoteServiceManager();
			// get the service manager from the component context
			this.m_xMultiComponentFactory = this.m_xComponentContext.getServiceManager();
			if (m_xMultiComponentFactory != null && m_xComponentContext != null) {
				Object toolkit = m_xMultiComponentFactory.createInstanceWithContext(
						"com.sun.star.awt.Toolkit", m_xComponentContext );
				m_xToolkit = (XToolkit) UnoRuntime.queryInterface( XToolkit.class,
						toolkit );
			}
		} catch (java.lang.Exception ex) {
			ex.printStackTrace();
		}
	}

	public static XToolkit getToolkit() {
		return m_xToolkit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	public String[] getSupportedServiceNames() {
		// TODO Auto-generated method stub
		return m_sServiceNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 */
	public void initialize(Object[] object) throws com.sun.star.uno.Exception {
		if (object.length > 0) {

			m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
					com.sun.star.frame.XFrame.class, object[0] );
			m_logger.log( "frame Initialized!" );
		}
		// Create the toolkit to have access to it later
		m_xToolkit = (XToolkit) UnoRuntime.queryInterface( XToolkit.class,
				m_xComponentContext.getServiceManager().createInstanceWithContext(
						"com.sun.star.awt.Toolkit", m_xComponentContext ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatch(com.sun.star.util.URL,
	 *      java.lang.String, int)
	 */
	public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL,
			String sTargetFrameName, int iSearchFlags) {
		try {
			if (aURL.Protocol.compareTo( GlobConstant.m_sSIGN_PROTOCOL_BASE_URL ) == 0) {
				// if ( aURL.Path.compareTo(GlobConstant.m_sSelectSignPath) == 0
				// ) {
				// if(m_aImplSelSignatureDispatch == null)
				// m_aImplSelSignatureDispatch = new
				// ImplSelectSignatureDispatch(m_xFrame,
				// m_xComponentContext, m_xMultiComponentFactory,
				// null);
				// return m_aImplSelSignatureDispatch;
				// }
				if (aURL.Path.compareTo( GlobConstant.m_sSIGN_DIALOG_PATH ) == 0) {
					if (m_aImplXAdESSignatureDispatch == null)
						m_aImplXAdESSignatureDispatch = new ImplXAdESSignatureDispatch(
								m_xFrame, m_xComponentContext, m_xMultiComponentFactory,
								null );
					return m_aImplXAdESSignatureDispatch;
				}
				if (aURL.Path.compareTo( GlobConstant.m_sSIGN_DIALOG_PATH_TB ) == 0) {
					if (m_aImplXAdESSignatureDispatchTB == null)
						m_aImplXAdESSignatureDispatchTB = new ImplXAdESSignatureDispatchTB(
								m_xFrame, m_xComponentContext, m_xMultiComponentFactory,
								null );
					return m_aImplXAdESSignatureDispatchTB;
				}
				// if( aURL.Path.compareTo(GlobConstant.m_sBeforeSavePath) == 0
				// ) {
				// if(m_aImplBeforeSaveDispatch == null)
				// m_aImplBeforeSaveDispatch = new
				// ImplBeforeSaveDispatch(m_xFrame,
				// m_xComponentContext, m_xMultiComponentFactory,
				// null);
				// return m_aImplBeforeSaveDispatch;
				// }
				// if( aURL.Path.compareTo(GlobConstant.m_sBeforeSaveAsPath) ==
				// 0 ) {
				// if(m_aImplBeforeSaveAsDispatch == null)
				// m_aImplBeforeSaveAsDispatch = new
				// ImplBeforeSaveAsDispatch(m_xFrame,
				// m_xComponentContext, m_xMultiComponentFactory,
				// null);
				// return m_aImplBeforeSaveAsDispatch;
				// }
				if (aURL.Path.compareTo( GlobConstant.m_sON_HELP_ABOUT_PATH ) == 0) {
					if (m_aImplOnHelpDispatch == null)
						m_aImplOnHelpDispatch = new ImplOnHelpDispatch( m_xFrame,
								m_xComponentContext, m_xMultiComponentFactory, null );
					return m_aImplOnHelpDispatch;
				}
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatches(com.sun.star.frame.DispatchDescriptor[])
	 */
	public com.sun.star.frame.XDispatch[] queryDispatches(
			com.sun.star.frame.DispatchDescriptor[] seqDescriptors) {
		int nCount = seqDescriptors.length;
		com.sun.star.frame.XDispatch[] seqDispatcher = new com.sun.star.frame.XDispatch[seqDescriptors.length];

		for (int i = 0; i < nCount; ++i) {
			seqDispatcher[i] = queryDispatch( seqDescriptors[i].FeatureURL,
					seqDescriptors[i].FrameName, seqDescriptors[i].SearchFlags );
		}
		return seqDispatcher;
	}

	/**
	 * Get the remote office context
	 */
	private XMultiComponentFactory getRemoteServiceManager() throws java.lang.Exception {
		if (m_xMultiComponentFactory == null && m_xRemoteServiceManager == null) {

			m_xRemoteServiceManager = m_xComponentContext.getServiceManager();
		}
		return m_xRemoteServiceManager;
	}

	/**
	 * Updates the Desktop current component in case of opening, creating or
	 * swapping to other document
	 * 
	 * @return XComponent Returns the current component of Desktop object
	 * 
	 */
	public void updateCurrentComponent() {

		XComponent ret = null;
		Object desktop;
		try {
			desktop = m_xRemoteServiceManager.createInstanceWithContext(
					"com.sun.star.frame.Desktop", m_xComponentContext );
			XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface( XDesktop.class,
					desktop );
			ret = xDesktop.getCurrentComponent();

			this.m_xMultiComponentFactory = this.m_xComponentContext.getServiceManager();
			this.m_xFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(
					XMultiServiceFactory.class, this.m_xCurrentComponent );

		} catch (com.sun.star.uno.Exception ex) {
			ex.printStackTrace();
		}
		this.m_xCurrentComponent = ret;
	}

	public void disposing(com.sun.star.lang.EventObject arg0) {
		// TODO Auto-generated method stub
		m_logger.log("disposing");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseable#close(boolean)
	 */
	public void close(boolean arg0) throws CloseVetoException {
		// TODO Auto-generated method stub
		m_logger.log("close");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseBroadcaster#addCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void addCloseListener(XCloseListener arg0) {
		// TODO Auto-generated method stub
		m_logger.log("addCloseListener");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseBroadcaster#removeCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void removeCloseListener(XCloseListener arg0) {
		// TODO Auto-generated method stub
		m_logger.log("removeCloseListener");
	}
}
