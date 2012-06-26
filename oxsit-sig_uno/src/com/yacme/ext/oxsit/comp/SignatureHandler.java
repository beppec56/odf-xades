/* ***** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/AvailableSSCDs_IT.java.
 *
 * The Initial Developer of the Original Code is
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.comp;


import com.sun.star.awt.XToolkit;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseListener;
import com.sun.star.util.XCloseable;
import com.yacme.ext.oxsit.dispatchers.IDispatchBaseObject;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.signature.dispatchers.ImplOnHelpDispatch;

/**
 * This class implements the dispatch handler that should be called when the signature is requested.<p>
 * Actually it only displays some information about the extension, the real work is carried
 * out by the class {@link com.yacme.ext.oxsit.comp.DispatchIntercept}.
 * 
 * @author Giuseppe Castagno
 * 
 */
public class SignatureHandler extends ComponentBase
			implements XServiceInfo, // general
						XInitialization, // to implement the ProtocolHandler service
						XDispatchProvider, // to implement the ProtocolHandler service
						XDispatch,
						XCloseable
						{

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

	private IDispatchBaseObject			m_aImplXAdESSignatureDispatchTB	= null;
	private XDispatch					m_aImplOnHelpDispatch			= null;
	private IDispatchBaseObject			m_aImplXAdESSignatureDispatch	= null;
	private DynamicLogger				m_aLogger;

	/**
	 * Constructs a new instance
	 * 
	 * @param context
	 *            the XComponentContext
	 */
	public SignatureHandler(XComponentContext context) {
		m_aLogger = new DynamicLogger(this,context);
//FIXME DEBUG
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		m_xComponentContext = context;
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
		m_aLogger.entering("initialize");
		if (object.length > 0) {

			m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
					com.sun.star.frame.XFrame.class, object[0] );
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
		m_aLogger.debug("queryDispatch",aURL.Complete);
		try {
			if (aURL.Protocol.compareTo( GlobConstant.m_sSIGN_PROTOCOL_BASE_URL ) == 0) {
				if (aURL.Path.compareTo( GlobConstant.m_sON_HELP_ABOUT_PATH ) == 0) {
					if (m_aImplOnHelpDispatch == null)
						m_aImplOnHelpDispatch = new ImplOnHelpDispatch( m_xFrame,
								m_xComponentContext, m_xMultiComponentFactory, null );
					return this;
				}
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_aLogger.debug("queryDispatch","return null: "+aURL.Complete);
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	@Override
	public void addStatusListener(XStatusListener arg0, URL arg1) {
		if (arg1.Complete.equalsIgnoreCase( GlobConstant.m_sON_HELP_ABOUT_PATH_COMPLETE )) {
			if (m_aImplOnHelpDispatch != null)
				m_aImplOnHelpDispatch.addStatusListener(arg0, arg1);
		}				
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 */
	@Override
	public void dispatch(URL arg0, PropertyValue[] arg1) {
		if (arg0.Complete.equalsIgnoreCase( GlobConstant.m_sON_HELP_ABOUT_PATH_COMPLETE )) {
			if (m_aImplOnHelpDispatch != null)
				m_aImplOnHelpDispatch.dispatch(arg0, arg1);
		}		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	@Override
	public void removeStatusListener(XStatusListener arg0, URL arg1) {
		if (arg1.Complete.equalsIgnoreCase( GlobConstant.m_sON_HELP_ABOUT_PATH_COMPLETE )) {
			if (m_aImplOnHelpDispatch != null)
				m_aImplOnHelpDispatch.removeStatusListener(arg0, arg1);
		}						
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
		m_aLogger.entering("updateCurrentComponent");
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
		m_aLogger.debug("disposing");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseable#close(boolean)
	 */
	public void close(boolean arg0) throws CloseVetoException {
		m_aLogger.debug("close");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseBroadcaster#addCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void addCloseListener(XCloseListener arg0) {
		m_aLogger.debug("addCloseListener");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XCloseBroadcaster#removeCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void removeCloseListener(XCloseListener arg0) {
		m_aLogger.debug("removeCloseListener");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		m_aLogger.entering("addEventListener (XComponent)");
		super.addEventListener(arg0);		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		m_aLogger.entering("dispose (XComponent)");
//call disposing of internal classes, deregistering
		if(m_aImplXAdESSignatureDispatchTB != null)
			m_aImplXAdESSignatureDispatchTB.dispose();
		if(m_aImplXAdESSignatureDispatch != null)
			m_aImplXAdESSignatureDispatch.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		m_aLogger.entering("removeEventListener (XComponent)");
		super.removeEventListener(arg0);
	}

}
