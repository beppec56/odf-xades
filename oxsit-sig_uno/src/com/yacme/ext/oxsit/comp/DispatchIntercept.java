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

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.FrameActionEvent;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XDispatchProviderInterceptor;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XFrameActionListener;
import com.sun.star.frame.XInterceptorInfo;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.yacme.ext.oxsit.XOX_DispatchInterceptor;
import com.yacme.ext.oxsit.dispatchers.IDispatchBaseObject;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.signature.dispatchers.ImplDisplaySignedFileDispatch;
import com.yacme.ext.oxsit.signature.dispatchers.ImplInterceptSaveDispatch;
import com.yacme.ext.oxsit.signature.dispatchers.ImplXAdESSignatureDispatch;
import com.yacme.ext.oxsit.signature.dispatchers.ImplXAdESSignatureDispatchTB;

/**
 * Implements the interceptor for the dispatches that need to be 'tweaked'.<p>
 * A UNO component formed by this class is instantiated in
 * com.yacme.ext.oxsit.comp.{@link  com.yacme.ext.oxsit.comp.SyncJob#executeOnViewCreated}.<p>
 * This class is the real handler of the signature command.<br>
 * The dispatch is processed by a class instantiated in method {@link com.yacme.ext.oxsit.comp.DispatchIntercept#queryDispatch}
 * @see <a href="http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/OfficeDev/Dispatch_Interception" target="frame">'Dispatch Interception' in the OpenOffice.org Developer's Guide</a> for 
 * details on how the mechanism works.<p>
 * @author Giuseppe Castagno
 * 
 */
// FIXME the frameAction may need adjustment (in case of context changing)
public class DispatchIntercept extends ComponentBase 
		implements
		XServiceInfo,
		XDispatchProviderInterceptor,
		XInterceptorInfo,
		XDispatchProvider,
		XDispatch,
		XOX_DispatchInterceptor,
		XFrameActionListener
		 {

	
	// the name of the class implementing this object
	public static final String			m_sImplementationName	= DispatchIntercept.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDISPATCH_INTERCEPTOR_SERVICE };
	
	
	/**
	 * @member m_xSlave we can forward all unhandled requests to this slave
	 *         interceptor
	 */
	private com.sun.star.frame.XDispatchProvider	m_xSlave;

	/**
	 * @member m_xFrame intercepted frame
	 */
	private com.sun.star.frame.XFrame				m_xFrame;

	/**
	 * @member m_xMaster use this interceptor if he doesn't handle queried
	 *         dispatch request
	 */
	private com.sun.star.frame.XDispatchProvider	m_xMaster;

	private XMultiComponentFactory					m_axMCF;
	private XComponentContext						m_xCC;

	/**
	 * members used to really dispatch the save as command down on the chain
	 */
	private XDispatch								m_ImplIntSaveDispatch		= null;
	private XDispatch								m_ImplIntSaveAsDispatch		= null;
	
	private IDispatchBaseObject						m_aImplXAdESSignatureDispatchTB	= null;	
	private IDispatchBaseObject						m_aImplXAdESSignatureDispatch = null;
	private IDispatchBaseObject						m_aImplDisplaySigneFileDispatch = null;
	private Object									m_aMutex						= new Object();

	private boolean									m_bDead;
	private boolean									m_bIsInterceptorRegistered;				// we
	// are registered as dispatch interceptors
	private boolean									m_bIsFrameActionRegistered;				// we
	// are registered as frame action m_aListeners

	private static final String[]					m_InterceptedURLs			= {
			/*GlobConstant.m_sUnoSignatureURLComplete, */ GlobConstant.m_sUNO_SAVE_URL_COMPLETE,
			GlobConstant.m_sUNO_SSAVE_AS_URL_COMPLETE,
			GlobConstant.m_sSIGN_PROTOCOL_BASE_URL+GlobConstant.m_sSIGN_DIALOG_PATH_TB
			};

	protected	DynamicLogger						m_aLogger;
	/**
	 * ctor Initialize the new interceptor. Given frame reference can be used to
	 * register this interceptor on it automatically later.
	 * 
	 * @seealso startListening()
	 * 
	 * @param xFrame
	 *            this interceptor will register himself at this frame to
	 *            intercept dispatched URLs
	 */
	public DispatchIntercept(XComponentContext xContext) {
		m_xFrame = null;
		m_xSlave = null;
		m_xMaster = null;
		m_axMCF = xContext.getServiceManager();
		m_xCC = xContext;
		m_bDead = false;
		m_bIsInterceptorRegistered = false;
		m_bIsFrameActionRegistered = false;
		m_aLogger = new DynamicLogger(this, xContext);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	@Override
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.debug("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
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
	 * @see com.sun.star.frame.XDispatchProviderInterceptor#getMasterDispatchProvider()
	 */
	public XDispatchProvider getMasterDispatchProvider() {
		/* System.out.println("com.sun.star.frame.XDispatchProviderInterceptor#getMasterDispatchProvider"); */
		synchronized (this) {
			return m_xMaster;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProviderInterceptor#getSlaveDispatchProvider()
	 */
	public XDispatchProvider getSlaveDispatchProvider() {
		/* printlnName("com.sun.star.frame.XDispatchProviderInterceptor#getSlaveDispatchProvider"); */
		synchronized (this) {
			return m_xSlave;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProviderInterceptor#setMasterDispatchProvider(
	 *      com.sun.star.frame.XDispatchProvider)
	 */
	public void setMasterDispatchProvider(XDispatchProvider _xMaster) {
		/* printlnName("com.sun.star.frame.XDispatchProviderInterceptor#setSlaveDispatchProvider"); */
		synchronized (this) {
			m_xMaster = _xMaster;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProviderInterceptor#setSlaveDispatchProvider(
	 *      com.sun.star.frame.XDispatchProvider)
	 */
	public void setSlaveDispatchProvider(XDispatchProvider _xSlave) {
		/* printlnName("com.sun.star.frame.XDispatchProviderInterceptor#setSlaveDispatchProvider"); */
		synchronized (this) {
			m_xSlave = _xSlave;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XInterceptorInfo#getInterceptedURLs()
	 */
	public String[] getInterceptedURLs() {
		// printlnName("com.sun.star.frame.XInterceptorInfo#getInterceptedURLs");
		m_aLogger.entering("getInterceptedURLs");
		return m_InterceptedURLs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatch(com.sun.star.util.URL,
	 *      java.lang.String, int)
	 */

	/**
	 * This method analyzes the dispatch URL and determines the action to be taken. 
	 */
	public XDispatch queryDispatch(/* IN */com.sun.star.util.URL aURL,/* IN */
									String sTarget,	/* IN */int nSearchFlags) {
//DEBUG		m_aLogger.debug("queryDispatch:", aURL.Complete);
		try {
			synchronized (m_aMutex) {
				if (m_bDead)
					return null;
			}
			// intercept .uno:Signature, simply disable it
			//this DOES NOT COMPLETELY disable the digital signature mode of OOo
			// The button in the File > Properties > General tab is still available
			// so you can still sign a document with the standard OOo digital signature 
			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SIGNATURE_URL_COMPLETE ) == true) {
				synchronized (this) {
					return null;
				}
			}

			// intercept .uno:MacroSignature, simply disable it
			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_MACRO_SIGNATURE_URL_COMPLETE ) == true) {
				synchronized (this) {
					return null;
				}
			}

			// intercept .uno:Save
			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SAVE_URL_COMPLETE ) == true) {
//DEBUG				m_aLogger.debug("queryDispatch", aURL.Complete);
				synchronized (this) {
					/*
					 * http://www.mail-archive.com/dev@api.openoffice.org/msg03786.html ()
					 * com.sun.star.frame.XNotifyingDispatch
					 * xNotifyingDispatcher =
					 * (com.sun.star.frame.XNotifyingDispatch)UnoRuntime.queryInterface(
					 * com.sun.star.frame.XNotifyingDispatch.class,m_aUnoSaveSlaveDispatch);
					 * if( xNotifyingDispatcher != null )
					 * System.out.println(today+" no XNotifyingDispatch"); else
					 * printlnName("has XNotifyingDispatch");
					 */
					XDispatch aUnoSaveSlaveDispatch = null;
					if (m_xSlave != null)
						aUnoSaveSlaveDispatch = m_xSlave.queryDispatch( aURL, sTarget,
								nSearchFlags );
/*					XNotifyingDispatch Xnf = (XNotifyingDispatch)UnoRuntime.queryInterface(XNotifyingDispatch.class, aUnoSaveSlaveDispatch);
					m_aLoggerDialog.info("XNotifyingDispatch: "+Xnf);*/
					if (m_ImplIntSaveDispatch == null)
						m_ImplIntSaveDispatch = new ImplInterceptSaveDispatch( m_xFrame, m_xCC,
								m_axMCF, aUnoSaveSlaveDispatch );
					return this;
				}
			}
/*			disabled 'cause it locks the office 
			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SSAVE_AS_URL_COMPLETE ) == true) {
				m_aLoggerDialog.info("queryDispatch", aURL.Complete);
				XDispatch aUnoSaveSlaveDispatch = null;
				synchronized (this) {
					if (m_xSlave != null)
						aUnoSaveSlaveDispatch = m_xSlave.queryDispatch( aURL, sTarget,
								nSearchFlags );
					XNotifyingDispatch Xnf = (XNotifyingDispatch)UnoRuntime.queryInterface(XNotifyingDispatch.class, aUnoSaveSlaveDispatch);
					m_aLoggerDialog.info("XNotifyingDispatch: "+Xnf);
					if (m_ImplIntSaveAsDispatch == null)
						m_ImplIntSaveAsDispatch = new ImplInterceptSaveAsDispatch( m_xFrame, m_xCC,
								m_axMCF, aUnoSaveSlaveDispatch );
					return this;
//					return m_ImplIntSaveAsDispatch;
				}
			}*/
			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_TB_COMPLETE ) == true) {
//DEBUG				m_aLogger.debug("queryDispatch:", aURL.Complete);
				if (m_aImplXAdESSignatureDispatchTB == null)
					m_aImplXAdESSignatureDispatchTB = new ImplXAdESSignatureDispatchTB(
							m_xFrame, m_xCC, m_axMCF, null );
				return this;
			}
			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_COMPLETE ) == true) {
//DEBUG				m_aLogger.debug("queryDispatch", aURL.Complete);
				if (m_aImplXAdESSignatureDispatch == null)
					m_aImplXAdESSignatureDispatch = new ImplXAdESSignatureDispatch(
							m_xFrame, m_xCC, m_axMCF, null );
				return this;//m_aImplXAdESSignatureDispatch;
			}

			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sDISPLAY_SIGNED_FILE_PATH_COMPLETE ) == true) {
//DEBUG				m_aLogger.debug("queryDispatch:", aURL.Complete);
								if (m_aImplDisplaySigneFileDispatch == null)
									m_aImplDisplaySigneFileDispatch = new ImplDisplaySignedFileDispatch(
											m_xFrame, m_xCC, m_axMCF, null );
								return this;
							}

			synchronized (this) {
				if (m_xSlave != null)// if a slave exist pass the request
					return m_xSlave.queryDispatch( aURL, sTarget, nSearchFlags );
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.getMessage();
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 */
	@Override
	public void dispatch(URL _aURL, PropertyValue[] _lArguments) {
		//check the URL, then run the real dispatcher
		m_aLogger.debug("main dispatch", _aURL.Complete);
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SAVE_URL_COMPLETE ) == true) {
				if (m_ImplIntSaveDispatch != null)
					m_ImplIntSaveDispatch.dispatch(_aURL, _lArguments);				
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SSAVE_AS_URL_COMPLETE ) == true) {
				if (m_ImplIntSaveAsDispatch != null)
					m_ImplIntSaveAsDispatch.dispatch(_aURL, _lArguments);
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_TB_COMPLETE ) == true) {
			if (m_aImplXAdESSignatureDispatchTB != null)
				m_aImplXAdESSignatureDispatchTB.dispatch(_aURL, _lArguments);
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_COMPLETE ) == true) {
			if (m_aImplXAdESSignatureDispatch != null)
				m_aImplXAdESSignatureDispatch.dispatch(_aURL, _lArguments);
		}		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	@Override
	public void addStatusListener(XStatusListener _aListener, URL _aURL) {
		//check the URL then run the real method
		m_aLogger.debug("main addStatusListener", _aURL.Complete);
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SAVE_URL_COMPLETE ) == true) {
				if (m_ImplIntSaveDispatch != null)
					m_ImplIntSaveDispatch.addStatusListener(_aListener, _aURL);				
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SSAVE_AS_URL_COMPLETE ) == true) {
				if (m_ImplIntSaveAsDispatch != null)
					m_ImplIntSaveAsDispatch.addStatusListener(_aListener, _aURL);
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_TB_COMPLETE ) == true) {
			if (m_aImplXAdESSignatureDispatchTB != null)
				m_aImplXAdESSignatureDispatchTB.addStatusListener(_aListener, _aURL);
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_COMPLETE ) == true) {
			if (m_aImplXAdESSignatureDispatch != null)
				m_aImplXAdESSignatureDispatch.addStatusListener(_aListener, _aURL);
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	@Override
	public void removeStatusListener(XStatusListener _aListener, URL _aURL) {
		//check the URL then run the real method
		m_aLogger.debug("main removeStatusListener", _aURL.Complete);
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SAVE_URL_COMPLETE ) == true) {
				if (m_ImplIntSaveDispatch != null)
					m_ImplIntSaveDispatch.removeStatusListener(_aListener, _aURL);				
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUNO_SSAVE_AS_URL_COMPLETE ) == true) {
				if (m_ImplIntSaveAsDispatch != null)
					m_ImplIntSaveAsDispatch.removeStatusListener(_aListener, _aURL);
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_TB_COMPLETE ) == true) {
			if (m_aImplXAdESSignatureDispatchTB != null)
				m_aImplXAdESSignatureDispatchTB.removeStatusListener(_aListener, _aURL);
		}
		if (_aURL.Complete.equalsIgnoreCase( GlobConstant.m_sSIGN_DIALOG_PATH_COMPLETE ) == true) {
			if (m_aImplXAdESSignatureDispatch != null)
				m_aImplXAdESSignatureDispatch.removeStatusListener(_aListener, _aURL);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatches(com.sun.star.frame.DispatchDescriptor[])
	 */
	public XDispatch[] queryDispatches(
	/* IN */com.sun.star.frame.DispatchDescriptor[] lDescriptor) {
		// Resolve any request separately by using own "dispatch()" method.
		// Note: Don't pack return list if "null" objects occurs! (?? what does
		// this mean??? (beppec56))
		int nCount = lDescriptor.length;
		com.sun.star.frame.XDispatch[] lDispatcher = new com.sun.star.frame.XDispatch[nCount];
		for (int i = 0; i < nCount; ++i) {
			lDispatcher[i] = queryDispatch( lDescriptor[i].FeatureURL,
					lDescriptor[i].FrameName, lDescriptor[i].SearchFlags );
		}
		return lDispatcher;
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// //////// class implementation methods
	/**
	 * start working as frame action listener really. We will be frame action
	 * listener here. In case we get a frame action which indicates, that we
	 * should update our interception. Because such using of an interceptor
	 * isn't guaranteed - in case a newer one was registered ...
	 */
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.XOX_DispatchInterceptor#startListening(com.sun.star.frame.XFrame)
	 */
	@Override
	public boolean startListening(XFrame _xFrame) {
		// TODO Auto-generated method stub
		m_aLogger.entering("startListening");
		m_xFrame = _xFrame;
		synchronized (m_aMutex) {
			if (m_xFrame == null)
				return false;

			m_bIsFrameActionRegistered = true;
			m_xFrame.addFrameActionListener( this );
			// add the dispatchers interceptor
			com.sun.star.frame.XDispatchProviderInterception xRegistration = (com.sun.star.frame.XDispatchProviderInterception) UnoRuntime
					.queryInterface(
							com.sun.star.frame.XDispatchProviderInterception.class,
							m_xFrame );
			if (xRegistration == null)
				return false;
			xRegistration.registerDispatchProviderInterceptor( this );
			m_bIsInterceptorRegistered = true;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XFrameActionListener#frameAction(com.sun.star.frame.FrameActionEvent)
	 */
	/**
	 * TODO Check to see what happens when another dispatch interceptor is put
	 * in place.
	 * 
	 */
	public void frameAction(FrameActionEvent aEvent) {
		
		String aLog;

		if (aEvent.Action.getValue() == com.sun.star.frame.FrameAction.COMPONENT_DETACHING_value) {
			// this part should be run on another thread
			aLog = "frameAction COMPONENT_DETACHING_value,deregistering";

			synchronized (m_aMutex) {
				// check if we are already dead (through disposing())
				if (m_bDead)
					return;
				m_bDead = true; // we are going to unregister from this one and
				if (m_bIsFrameActionRegistered) {
					// stop the rest of the tasks
					aLog = aLog + ", deregistering";

					// unregister the listener
					m_xFrame.removeFrameActionListener( this );
					m_bIsFrameActionRegistered = false;
				}

				if (m_bIsInterceptorRegistered) {
					// remove the interceptor
					com.sun.star.frame.XDispatchProviderInterception xRegistration = (com.sun.star.frame.XDispatchProviderInterception) UnoRuntime
							.queryInterface(com.sun.star.frame.XDispatchProviderInterception.class,
									m_xFrame );
					if (xRegistration != null)
						xRegistration.releaseDispatchProviderInterceptor( this );

					m_bIsInterceptorRegistered = false;
					aLog = aLog +  ", remove interceptor";
				}
			}
			m_aLogger.debug( aLog );
		} else
			// give some status indication
			switch (aEvent.Action.getValue()) {
			case com.sun.star.frame.FrameAction.COMPONENT_DETACHING_value:
				// this part should be run on another thread
				// printlnName("frameAction COMPONENT_DETACHING_value");
				/*
				 * //unregister the listener
				 * m_xFrame.removeFrameActionListener(this); //remove the
				 * interceptor
				 * 
				 * System.out.println(today+" deregistering...");
				 * com.sun.star.frame.XDispatchProviderInterception
				 * xRegistration =
				 * (com.sun.star.frame.XDispatchProviderInterception)UnoRuntime.queryInterface(
				 * com.sun.star.frame.XDispatchProviderInterception.class,
				 * m_xFrame); if(xRegistration!=null)
				 * xRegistration.releaseDispatchProviderInterceptor(this);
				 */

				break;
			case com.sun.star.frame.FrameAction.COMPONENT_ATTACHED_value:
				m_aLogger.debug( "frameAction COMPONENT_ATTACHED_value" );
				break;
			case com.sun.star.frame.FrameAction.COMPONENT_REATTACHED_value:
				m_aLogger.debug( "frameAction COMPONENT_REATTACHED_value" );
				break;
			case com.sun.star.frame.FrameAction.FRAME_ACTIVATED_value:
				m_aLogger.debug( "frameAction FRAME_ACTIVATED_value" );
				break;
			case com.sun.star.frame.FrameAction.FRAME_DEACTIVATING_value:
				m_aLogger.debug( "frameAction FRAME_DEACTIVATING_value" );
				// ///////////// not good...
				// //check if we are frame action m_aListeners, if yes unregister,
				// surround with mutex
				// synchronized (m_aMutex) {
				// if( m_bIsFrameActionRegistered ) {
				// println("deregistering frame listener...");
				// m_xFrame.removeFrameActionListener(this);
				// m_bIsFrameActionRegistered = false;
				// }
				// // then check if we are dispatch interceptor, if yes
				// unregister
				// as well
				// if( m_bIsRegistered) {
				// println("deregistering...");
				// com.sun.star.frame.XDispatchProviderInterception
				// xRegistration =
				// (com.sun.star.frame.XDispatchProviderInterception)UnoRuntime.queryInterface(
				// com.sun.star.frame.XDispatchProviderInterception.class,
				// m_xFrame);
				// if(xRegistration!=null)
				// xRegistration.releaseDispatchProviderInterceptor(this);
				// }
				// }
				break;
			case com.sun.star.frame.FrameAction.CONTEXT_CHANGED_value:
				m_aLogger.debug( "frameAction CONTEXT_CHANGED_value" );
				// need to reregister??? It seems that this becomes and endless
				// loop...
				break;
			case com.sun.star.frame.FrameAction.FRAME_UI_ACTIVATED_value:
				m_aLogger.debug( "frameAction FRAME_UI_ACTIVATED_value" );
				break;
			case com.sun.star.frame.FrameAction.FRAME_UI_DEACTIVATING_value:
				m_aLogger.debug( "frameAction FRAME_UI_DEACTIVATING_value" );
				break;
			default:
				m_aLogger.debug( "frameAction other value" );
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(EventObject arg0) {
		m_aLogger.entering("disposing");
		synchronized (m_aMutex) {
			if (m_bDead)
				return;
			// if (!m_bIsInterceptorRegistered && !m_bIsFrameActionRegistered)
			// return;
			m_bDead = true; // we are going to unregister from this one

			if (m_bIsFrameActionRegistered) {
//				printName( "disposing" );
				m_xFrame.removeFrameActionListener( this );
				m_bIsFrameActionRegistered = false;
			}

//			println( ", deregistering..." );
			if (m_bIsInterceptorRegistered) {
				com.sun.star.frame.XDispatchProviderInterception xRegistration = (com.sun.star.frame.XDispatchProviderInterception) UnoRuntime
						.queryInterface(
								com.sun.star.frame.XDispatchProviderInterception.class,
								m_xFrame );

				if (xRegistration != null)
					xRegistration.releaseDispatchProviderInterceptor( this );
				m_bIsInterceptorRegistered = false;
			}
		}
	}
}
