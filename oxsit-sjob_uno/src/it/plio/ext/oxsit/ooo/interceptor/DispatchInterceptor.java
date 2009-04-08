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

package it.plio.ext.oxsit.ooo.interceptor;

import it.plio.ext.oxsit.jobs.dispatchers.ImplIntSaveAsDispatch;
import it.plio.ext.oxsit.jobs.dispatchers.ImplIntSaveDispatch;
import it.plio.ext.oxsit.jobs.sync.GlobConstantJobs;

import com.sun.star.frame.FrameActionEvent;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XDispatchProviderInterceptor;
import com.sun.star.frame.XFrameActionListener;
import com.sun.star.frame.XInterceptorInfo;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * implements the interceptor for the dispatches that need to be 'tweaked'
 * 
 * @author beppe
 * 
 */
// FIXME the frameAction may need adjustment (in case of context changing)
public class DispatchInterceptor extends WeakBase implements
		XDispatchProviderInterceptor, XInterceptorInfo, XDispatchProvider,
		XFrameActionListener {

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
//	private XDispatch								m_ImplIntSignatureDispatch	= null;
	private XDispatch								m_ImplIntSaveDispatch		= null;
	private XDispatch								m_ImplIntSaveAsDispatch		= null;

	private Object									aMutex						= new Object();

	private boolean									m_bDead;
	private boolean									m_bIsInterceptorRegistered;				// we
	// are registered as dispatch interceptors
	private boolean									m_bIsFrameActionRegistered;				// we
	// are registered as frame action listeners

	private static final String[]					m_InterceptedURLs			= {
			/*GlobConstant.m_sUnoSignatureURLComplete, */ GlobConstantJobs.m_sUnoSaveURLComplete,
			GlobConstantJobs.m_sUnoSaveAsURLComplete								};

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
	public DispatchInterceptor(
	/* IN */com.sun.star.frame.XFrame xFrame,
			XComponentContext xContext,
			XMultiComponentFactory xMCF) {
		m_xFrame = xFrame;
		m_xSlave = null;
		m_xMaster = null;
		m_axMCF = xMCF;
		m_xCC = xContext;
		m_bDead = false;
		m_bIsInterceptorRegistered = false;
		m_bIsFrameActionRegistered = false;
		printlnName( " ctor" );
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
		return m_InterceptedURLs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchProvider#queryDispatch(com.sun.star.util.URL,
	 *      java.lang.String, int)
	 */
	public XDispatch queryDispatch(/* IN */com.sun.star.util.URL aURL,/* IN */
	String sTarget,
	/* IN */int nSearchFlags) {
		try {
			synchronized (aMutex) {
				if (m_bDead)
					return null;
			}

			// interpret .uno:Signature
/*			if (aURL.Complete.equalsIgnoreCase( GlobConstant.m_sUnoSignatureURLComplete ) == true) {
				// Utilities.showInterfaces(this);
				// printlnName("com.sun.star.frame.XDispatchProvider#queryDispatch
				// intercept: "+aURL.Complete);
				synchronized (this) {
					XDispatch aUnoSaveSlaveDispatch = null;
					if (m_xSlave != null)
						aUnoSaveSlaveDispatch = m_xSlave.queryDispatch( aURL, sTarget,
								nSearchFlags );
					if (m_ImplIntSignatureDispatch == null)
						m_ImplIntSignatureDispatch = new ImplIntSignatureDispatch(
								m_xFrame, m_xCC, m_axMCF, aUnoSaveSlaveDispatch );
					return m_ImplIntSignatureDispatch;
				}
			}

*/			// intercept .uno:Save
			if (aURL.Complete.equalsIgnoreCase( GlobConstantJobs.m_sUnoSaveURLComplete ) == true) {
				// printlnName("com.sun.star.frame.XDispatchProvider#queryDispatch
				// intercept: "+aURL.Complete);
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
					if (m_ImplIntSaveDispatch == null)
						m_ImplIntSaveDispatch = new ImplIntSaveDispatch( m_xFrame, m_xCC,
								m_axMCF, aUnoSaveSlaveDispatch );
					return m_ImplIntSaveDispatch;
				}
			}
			if (aURL.Complete.equalsIgnoreCase( GlobConstantJobs.m_sUnoSaveAsURLComplete ) == true) {
				// printlnName("com.sun.star.frame.XDispatchProvider#queryDispatch
				// intercept: "+aURL.Complete);
				synchronized (this) {
					XDispatch aUnoSaveSlaveDispatch = null;
					if (m_xSlave != null)
						aUnoSaveSlaveDispatch = m_xSlave.queryDispatch( aURL, sTarget,
								nSearchFlags );
					if (m_ImplIntSaveAsDispatch == null)
						m_ImplIntSaveAsDispatch = new ImplIntSaveAsDispatch( m_xFrame,
								m_xCC, m_axMCF, aUnoSaveSlaveDispatch );
					return m_ImplIntSaveAsDispatch;
				}
			}

			synchronized (this) {
				if (m_xSlave != null)// if a slave exist pass the request
					// down the chain of responsability
					return m_xSlave.queryDispatch( aURL, sTarget, nSearchFlags );
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.getMessage();
			e.printStackTrace();
		}
		return null;
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
	public void startListening() {
		synchronized (aMutex) {
			if (m_xFrame == null)
				return;

			m_bIsFrameActionRegistered = true;
			m_xFrame.addFrameActionListener( this );
			// add the dispatchers interceptor
			com.sun.star.frame.XDispatchProviderInterception xRegistration = (com.sun.star.frame.XDispatchProviderInterception) UnoRuntime
					.queryInterface(
							com.sun.star.frame.XDispatchProviderInterception.class,
							m_xFrame );
			if (xRegistration == null)
				return;
			xRegistration.registerDispatchProviderInterceptor( this );
			m_bIsInterceptorRegistered = true;
		}
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

		if (aEvent.Action.getValue() == com.sun.star.frame.FrameAction.COMPONENT_DETACHING_value) {
			// this part should be run on another thread
			print( "frameAction COMPONENT_DETACHING_value,deregistering" );

			synchronized (aMutex) {
				// check if we are already dead (through disposing())
				if (m_bDead)
					return;
				m_bDead = true; // we are going to unregister from this one and
				if (m_bIsFrameActionRegistered) {
					// stop the rest of the tasks
					print( ", deregistering" );

					// unregister the listener
					m_xFrame.removeFrameActionListener( this );
					m_bIsFrameActionRegistered = false;
				}

				if (m_bIsInterceptorRegistered) {
					// remove the interceptor
					com.sun.star.frame.XDispatchProviderInterception xRegistration = (com.sun.star.frame.XDispatchProviderInterception) UnoRuntime
							.queryInterface(
									com.sun.star.frame.XDispatchProviderInterception.class,
									m_xFrame );
					if (xRegistration != null)
						xRegistration.releaseDispatchProviderInterceptor( this );

					m_bIsInterceptorRegistered = false;
					print( ", remove interceptor" );
				}
			}
			println( "" );
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
				println( "frameAction COMPONENT_ATTACHED_value" );
				break;
			case com.sun.star.frame.FrameAction.COMPONENT_REATTACHED_value:
				println( "frameAction COMPONENT_REATTACHED_value" );
				break;
			case com.sun.star.frame.FrameAction.FRAME_ACTIVATED_value:
				println( "frameAction FRAME_ACTIVATED_value" );
				break;
			case com.sun.star.frame.FrameAction.FRAME_DEACTIVATING_value:
				println( "frameAction FRAME_DEACTIVATING_value" );
				// ///////////// not good...
				// //check if we are frame action listeners, if yes unregister,
				// surround with mutex
				// synchronized (aMutex) {
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
				println( "frameAction CONTEXT_CHANGED_value" );
				// need to reregister??? It seems that this becomes and endless
				// loop...
				break;
			case com.sun.star.frame.FrameAction.FRAME_UI_ACTIVATED_value:
				println( "frameAction FRAME_UI_ACTIVATED_value" );
				break;
			case com.sun.star.frame.FrameAction.FRAME_UI_DEACTIVATING_value:
				println( "frameAction FRAME_UI_DEACTIVATING_value" );
				break;
			default:
				println( "frameAction other value" );
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(EventObject arg0) {
		synchronized (aMutex) {
			if (m_bDead)
				return;
			// if (!m_bIsInterceptorRegistered && !m_bIsFrameActionRegistered)
			// return;
			m_bDead = true; // we are going to unregister from this one

			if (m_bIsFrameActionRegistered) {
				printName( "disposing" );
				m_xFrame.removeFrameActionListener( this );
				m_bIsFrameActionRegistered = false;
			}

			println( ", deregistering..." );
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

	private void shutdown() {
		// synchronized (aMutex) {
		
		// if(m_bDead)
		// return;
		// //check if we are frame action listener
		// }
		printlnName("shutdown");
	}

	// //////////////// debug methods
	public String getHashHex() {
		return String.format( "%8H", hashCode() );
	}

	public void printlnName(String _sMex) {
		System.out
				.println( getHashHex() + " " + this.getClass().getName() + ": " + _sMex );
	}

	public void printName(String _sMex) {
		System.out.print( getHashHex() + " " + this.getClass().getName() + ": " + _sMex );
	}

	public void print(String _sMex) {
		System.out.print( " " + _sMex );
	}

	public void println(String _sMex) {
		System.out.println( getHashHex() + " " + _sMex );
	}
}
