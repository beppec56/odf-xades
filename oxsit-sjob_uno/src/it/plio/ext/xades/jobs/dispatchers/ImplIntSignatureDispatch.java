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

package it.plio.ext.xades.jobs.dispatchers;

import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.xades.dispatchers.threads.IDispatchImplementer;
import it.plio.ext.xades.dispatchers.threads.OnewayDispatchExecutor;
import it.plio.ext.xades.jobs.sync.GlobConstantJobs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.DispatchResultEvent;
import com.sun.star.frame.FeatureStateEvent;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchResultListener;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XNotifyingDispatch;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseListener;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XURLTransformer;

/**
 * @author beppe
 * 
 */
/**
 * @author beppe
 * 
 */
public class ImplIntSignatureDispatch extends WeakBase implements XDispatch,
		IDispatchImplementer, XDispatchResultListener, XNotifyingDispatch, XCloseable {

	private HashMap<XStatusListener, LinkingStatusListeners>	Listeners;

	private short												m_nReturnValue;

	private int													m_signStatus			= 0;

	private XDispatchResultListener								m_aCallerDispatchListener;											// holds
	// the notifier given to us by the calling process
	private XDispatchResultListener								m_aCalledDispatchListener;											// hold
	// the notifier we gave to the .uno:Signature called dispatch

	protected Thread											m_aThread				= null;

	protected XFrame											m_xFrame;
	protected XMultiComponentFactory							m_axMCF;
	protected XComponentContext									m_xCC;
	/* protected String today; */
	protected XDispatch											m_aUnoSlaveDispatch		= null;

	protected String											m_aDocumentURL			= new String(
																								"-not existent frame or URL-" );

	private int													m_nOOoSignatureStatus	= 0;
	private int													m_nCNIPASignatureStatus	= 0;

	public ImplIntSignatureDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		m_xFrame = xFrame;
		m_axMCF = xMCF;
		m_xCC = xContext;
		m_aUnoSlaveDispatch = unoSaveSlaveDispatch;

		// form the complete Url being intercepted
		// may be we need to check for the interface existence...
		m_aDocumentURL = m_xFrame.getController().getModel().getURL();
		/*
		 * DateFormat timeFormatter =
		 * DateFormat.getTimeInstance(DateFormat.DEFAULT, new Locale("it"));
		 * today = timeFormatter.format(new Date());
		 */
		printlnName( "ctor" );
		Listeners = new HashMap<XStatusListener, LinkingStatusListeners>( 5 );
		m_aCallerDispatchListener = null;
		m_aCalledDispatchListener = null;

		com.sun.star.frame.XNotifyingDispatch test = (com.sun.star.frame.XNotifyingDispatch) UnoRuntime
				.queryInterface( com.sun.star.frame.XNotifyingDispatch.class, this );
		/*
		 * if(test != null) println("XNotifyingDispatch detected"); else
		 * println("XNotifyingDispatch NOT detected");
		 */
		// Utilities.showInterfaces(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.plio.ext.cnipa.ooo.interceptor.IDispatchInterceptor#impl_dispatch(com.sun.star.util.URL,
	 *      com.sun.star.beans.PropertyValue[])
	 */
	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {
		try {
			// ok, doc has location, meaning was saved the first time or was
			// read from permanent storage
			com.sun.star.util.URL[] aParseURL = new com.sun.star.util.URL[1];
			aParseURL[0] = new com.sun.star.util.URL();
			aParseURL[0].Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL
					+ GlobConstant.m_sSELECT_SIGN_PATH;
			com.sun.star.beans.PropertyValue[] lProperties = new com.sun.star.beans.PropertyValue[1];

			com.sun.star.frame.XDispatchProvider xProvider = (com.sun.star.frame.XDispatchProvider) UnoRuntime
					.queryInterface( com.sun.star.frame.XDispatchProvider.class, m_xFrame );
			// need an URLTransformer
			Object obj;
			obj = m_axMCF.createInstanceWithContext( "com.sun.star.util.URLTransformer",
					m_xCC );
			XURLTransformer xTransformer = (XURLTransformer) UnoRuntime.queryInterface(
					XURLTransformer.class, obj );
			xTransformer.parseStrict( aParseURL );

			m_nReturnValue = it.plio.ext.xades.ooo.ui.DialogChooseSignatureTypes.OOoSignatureSelected;
			// Ask it for right dispatch object for our URL.
			// Force given frame as target for following dispatch by using "",
			// it's the same as "_self".
			if (xProvider != null) {
				com.sun.star.frame.XDispatch xDispatcher = null;
				xDispatcher = xProvider.queryDispatch( aParseURL[0], "", 0 );

				// Dispatch the URL into the frame.
				if (xDispatcher != null) {
					com.sun.star.frame.XNotifyingDispatch xNotifyingDispatcher = (com.sun.star.frame.XNotifyingDispatch) UnoRuntime
							.queryInterface( com.sun.star.frame.XNotifyingDispatch.class,
									xDispatcher );
					if (xNotifyingDispatcher != null)
						xNotifyingDispatcher.dispatchWithNotification( aParseURL[0],
								lProperties, this );
					/*
					 * else // TODO throw unimplemented method???
					 * xDispatcher.dispatch(aParseURL[0],lProperties);
					 */
					// then get from the registry value of the user answer.
					// depending on that exits or do the operation
					printlnName( "dispatched, waiting" );
					try {
						while (true) {
							Thread.sleep( 10000 ); // ten seconds
							System.out.print( "." );
						}
					} catch (InterruptedException e) {
						println( "interrupted" );
					}

					if (m_nReturnValue == it.plio.ext.xades.ooo.ui.DialogChooseSignatureTypes.CNIPASignatureSelected) {
						// dispatch the sign stuff (asyncronous)
						aParseURL[0].Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL
								+ GlobConstant.m_sSIGN_DIALOG_PATH;
						xTransformer.parseStrict( aParseURL );
						xDispatcher = xProvider.queryDispatch( aParseURL[0], "", 0 );
						if (xDispatcher != null) {
							// this will sign the document, asynch, since the
							// user will have to check, select, sign, etc...
							/**
							 * allocate a XStatusListener to be notified of
							 * events: - document signed or not - if the
							 * document has bee signed then notify the listener
							 * (using XStatusListener.statusChanged() method,
							 * see below addStatusListener() method) we need to
							 * implement the XNotifyingDispatch interface there
							 * as well and when finished move the result
							 * 'upstair'
							 * 
							 */
							com.sun.star.frame.XNotifyingDispatch xaNotifyingDispatcher = (com.sun.star.frame.XNotifyingDispatch) UnoRuntime
									.queryInterface(
											com.sun.star.frame.XNotifyingDispatch.class,
											xDispatcher );
							if (xaNotifyingDispatcher != null)
								xaNotifyingDispatcher.dispatchWithNotification(
										aParseURL[0], lProperties, this );
							/*
							 * else // TODO throw unimplemented method???
							 * xDispatcher.dispatch(aParseURL[0],lProperties);
							 */

							// printlnName("finished dispatch");
							// wait for the sign process, when the process is
							// done,
//						printlnName( "dispatched, waiting(2)" );
							try {
								while (true) {
									Thread.sleep( 10000 ); // ten seconds
									System.out.print( "." );
								}
							} catch (InterruptedException e) {
								println( "interrupted" );
							}
//							println( "returned: " + m_nReturnValue );
							switch (m_nReturnValue) {
							default:
							case 0:
								m_nCNIPASignatureStatus = GlobConstantJobs.SIGNATURESTATE_NOSIGNATURES;
								changeSignatureStatus(); // no signatures
								break;
							case 1:
//								m_nCNIPASignatureStatus = GlobConstant.SIGNATURESTATE_SIGNATURES_NOTVALIDATED;
								m_nCNIPASignatureStatus = GlobConstantJobs.SIGNATURESTATE_SIGNATURES_OK;
								changeSignatureStatus(); // semi-broken
															// signatures
								break;
							}
						} else
							printlnName( "No dispatcher for " + aParseURL[0].Complete );
						return;
					} else if (m_nReturnValue == it.plio.ext.xades.ooo.ui.DialogChooseSignatureTypes.NoSignatureSelected)
						return; // no signature
				} else
					printlnName( "No dispatcher for " + aParseURL[0].Complete );
			} else
				printlnName( "No provider for " + aParseURL[0].Complete );
			// Dispatch the .uno:Signature URL into the frame.
			// check if the downmode has a Xn
			// super.impl_dispatch(aURL, lArguments);
			if (m_aUnoSlaveDispatch != null) {
				com.sun.star.frame.XNotifyingDispatch xNotifyingDispatcher = (com.sun.star.frame.XNotifyingDispatch) UnoRuntime
						.queryInterface( com.sun.star.frame.XNotifyingDispatch.class,
								m_aUnoSlaveDispatch );
				if (xNotifyingDispatcher != null) {// prepare a dispatcher
					// interface obiect to be
					// used as a link to upstair
					// create a new object to be notified by the downstair
					// dispatch

					printlnName( "xNotifyingDispatcher PRESENT!" );

					/*
					 * xNotifyingDispatcher.dispatchWithNotification(aURL,
					 * lArguments, m_aCalledDispatchListener ); } else {
					 */
				}
				m_aUnoSlaveDispatch.dispatch( aURL, lArguments );
			}
			return;
		} catch (com.sun.star.uno.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XNotifyingDispatch#dispatchWithNotification(com.sun.star.util.URL,
	 *      com.sun.star.beans.PropertyValue[],
	 *      com.sun.star.frame.XDispatchResultListener)
	 */
	public void dispatchWithNotification(URL aURL, PropertyValue[] lProps,
			XDispatchResultListener _xDispatchResultsListener) {
		// printlnName("dispatchWithNotification");
		m_aCallerDispatchListener = _xDispatchResultsListener;
		dispatch( aURL, lProps ); // this in turn will start the working
		// thread
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.plio.ext.cnipa.dispatchers.ImplDispatchSynch#addStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 */
	/**
	 * we generate a new XStatusListener object for every request to add one
	 * Then we: - generate our private status listener for every one that ask to
	 * be registered - add the newly generated to a list, using the hascode() of
	 * the requester listener as the access key in the list - pass our listener
	 * down the list to the original ".uno:Signature" dispatch
	 */
	public void addStatusListener(XStatusListener aListener, URL aURL) {
		// printName("addStatusListener
		// "+String.format("%8s",Integer.toHexString(aListener.hashCode()))+"
		// "+aURL.Complete+" listeners: "+Listeners.size());
		LinkingStatusListeners MyListener = new LinkingStatusListeners( aListener, aURL,
				m_aDocumentURL );
		Listeners.put( aListener, MyListener );
		// println("add listener:"+ new
		// String(String.format("%8H",aListener.hashCode())));

		if (m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.addStatusListener( MyListener, aURL );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 */
	/**
	 * remove the status listener, When the request to remove a status listener
	 * is received we: - retrieve using the XStatusListener our created listener
	 * from the list, - pass down the request to the slave telling it to remove
	 * our listener - remove the listener from our list
	 * 
	 */
	public void removeStatusListener(XStatusListener aListener, URL aURL) {
		// printName("removeStatusListener
		// "+String.format("%8s",Integer.toHexString(aListener.hashCode()))+"
		// "+aURL.Complete);
		LinkingStatusListeners MyListener = Listeners.get( aListener );
		if (m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.removeStatusListener( MyListener, aURL );
		Listeners.remove( aListener );
		// println(" listeners: "+Listeners.size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatchResultListener#dispatchFinished(com.sun.star.frame.DispatchResultEvent)
	 *      called by the dispatch which was called through
	 *      XNotifyingDispatcher.dispatchWithNotification() method
	 */
	public void dispatchFinished(DispatchResultEvent aResultEvent) {
		// TODO Auto-generated method stub
		if (aResultEvent != null) {
			m_nReturnValue = aResultEvent.State;
		}
		m_aThread.interrupt(); // interrupt ourselves
	}

	/*
	 * (non-Javadoc) TODO see what purpose has this?
	 * 
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(EventObject arg0) {
		// TODO Auto-generated method stub
		printlnName( "disposing" );
	}

	private void changeSignatureStatus() {
		// simply a try: force the signature change on when CNIPA signature is
		// selected
		// get the COllection of listener
		Collection<LinkingStatusListeners> cListenters = Listeners.values();
		// printlnName("changeSignatureStatus "+cListenters.size());
		if (!cListenters.isEmpty()) {
			Iterator<LinkingStatusListeners> aIter = cListenters.iterator();

			// scan the array and for every one send the status
			while (aIter.hasNext()) {
				LinkingStatusListeners aLink = aIter.next();
				FeatureStateEvent aState = new FeatureStateEvent();
				aState.FeatureDescriptor = new String( "" );
				aState.FeatureURL.Complete = GlobConstantJobs.m_sUnoSignatureURLComplete;
				aState.FeatureURL.Protocol = GlobConstantJobs.m_sUnoSignatureURLProtocol;
				aState.FeatureURL.Path = GlobConstantJobs.m_sUnoSignatureURLPath;
				aState.IsEnabled = Boolean.TRUE;
				aState.Requery = Boolean.FALSE;
				aState.Source = this;
				// we form the signature status to forward:
				// public static final int SIGNATURESTATE_UNKNOWN = -1;
				// public static final int SIGNATURESTATE_NOSIGNATURES = 0;
				// public static final int SIGNATURESTATE_SIGNATURES_OK = 1;
				// public static final int SIGNATURESTATE_SIGNATURES_BROKEN = 2;
				// /** State was SIGNATURES_OK, but doc is modified now
				// */
				// public static final int SIGNATURESTATE_SIGNATURES_INVALID =
				// 3;
				// /**
				// * signature is OK, but certificate could not be validated
				// */
				// public static final int
				// SIGNATURESTATE_SIGNATURES_NOTVALIDATED = 4;
				//				
				// TODO this logic need to be implemented correctly when all
				// done
				int StateToForwad = GlobConstantJobs.SIGNATURESTATE_NOSIGNATURES;
				switch (m_nCNIPASignatureStatus) {
				case GlobConstantJobs.SIGNATURESTATE_SIGNATURES_INVALID:
					StateToForwad = GlobConstantJobs.SIGNATURESTATE_SIGNATURES_INVALID;
					break;
				case GlobConstantJobs.SIGNATURESTATE_SIGNATURES_OK:
					if (m_nOOoSignatureStatus == GlobConstantJobs.SIGNATURESTATE_SIGNATURES_OK
							|| m_nOOoSignatureStatus == GlobConstantJobs.SIGNATURESTATE_NOSIGNATURES)
						StateToForwad = GlobConstantJobs.SIGNATURESTATE_SIGNATURES_OK;
					break;
				case GlobConstantJobs.SIGNATURESTATE_SIGNATURES_NOTVALIDATED:
					StateToForwad = GlobConstantJobs.SIGNATURESTATE_SIGNATURES_NOTVALIDATED;
					break;
				default:
					StateToForwad = m_nOOoSignatureStatus;
					break;
				}
				aState.State = new Integer( StateToForwad );
				aLink.m_aMaster.statusChanged( aState );
				// println("send listener:"+ new
				// String(String.format("%8H",aLink.m_aMaster.hashCode())));
			}
		}
	}

	/**
	 * our private status listener, used to grab the statusChanged event from the
	 * .uno:Signature slave dispatcher
	 * 
	 * @author beppe
	 * 
	 */
	public class LinkingStatusListeners implements XStatusListener {

		public XStatusListener	m_aMaster	= null;	// to be addressed and
		// used when notifying
		// signature status
		private String			m_aDocumentURL;
		public URL				m_aDispatchCommandURL;

		public LinkingStatusListeners(XStatusListener _ParentListener, URL _aDispatchURL,
				String _DocumentURL) {
			m_aMaster = _ParentListener;
			m_aDispatchCommandURL = _aDispatchURL;
			m_aDocumentURL = _DocumentURL;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.star.frame.XStatusListener#statusChanged(com.sun.star.frame.FeatureStateEvent)
		 * 
		 * what we do here: intercept the status change issued by the resident
		 * .uno:Signature process of OOo
		 * 
		 * add to the aEvent.Status the status of the CNIPA signature
		 * 
		 * The status of the current document signature is in the registry, or
		 * may be in this dispatcher? We need to check at run-time when
		 * signature is ready
		 * 
		 */
		public void statusChanged(FeatureStateEvent aEvent) {
			if (m_aMaster != null) {

				// mix the two signature status
				m_nOOoSignatureStatus = ( (Integer) aEvent.State ).intValue();
				aEvent.State = new Integer( m_nOOoSignatureStatus
						| m_nCNIPASignatureStatus );
				// print the status here
				/*
				 * printlnName("\n\t\tsignature status changed for document URL:
				 * "+m_aDocumentURL); println("StatusChanged called feature:
				 * '"+aEvent.FeatureDescriptor+"' URL:
				 * '"+aEvent.FeatureURL.Complete+ "' IsEnabled:
				 * "+aEvent.IsEnabled+" Requery "+ aEvent.Requery);
				 * println("path "+aEvent.FeatureURL.Path+" proto
				 * "+aEvent.FeatureURL.Protocol); if(aEvent.Source != null) {
				 * println(" Source is a:"+aEvent.Source.getClass().getName()); //
				 * Utilities.showInterfaces((XInterface) aEvent.Source); }
				 * if(aEvent.State != null) { println(" State is
				 * a:"+aEvent.State.getClass().getName()+" value:
				 * "+aEvent.State); }
				 * 
				 * if(m_signStatus != 0) { printlnName("é"); aEvent.State = new
				 * Integer(m_signStatus); }
				 */
				// aEvent.State = new Integer(0);// means no signatures present
				// on document
				// aEvent.State = new Integer(1);// means signatures present on
				// document and ALL validated
				// aEvent.State = new Integer(2);// means signatures present but
				// document can't be validated (hence certificates not checked)
				// aEvent.State = new Integer(3);// no effect
				// aEvent.State = new Integer(4);// signature valid wrt the
				// document, but certificates can't be validated
				//
				// forward 'upstair' the status changed event
				m_aMaster.statusChanged( aEvent );
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
		 */
		public void disposing(EventObject aEvent) {
			// TODO Auto-generated method stub
			printlnName( "disposing called" );
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XCloseable#close(boolean)
	 */
	public void close(boolean arg0) throws CloseVetoException {
		// TODO Auto-generated method stub
		printlnName("XCloseable#close");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XCloseBroadcaster#addCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void addCloseListener(XCloseListener arg0) {
		// TODO Auto-generated method stub
		printlnName("XCloseable#close");
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XCloseBroadcaster#removeCloseListener(com.sun.star.util.XCloseListener)
	 */
	public void removeCloseListener(XCloseListener arg0) {
		// TODO Auto-generated method stub
		printlnName("XCloseable#close");
		
	}

	public void dispatch(URL aURL, PropertyValue[] lArguments) {
		// println("dispatch (ImplDispatchAsynch) "+aURL.Complete);
		OnewayDispatchExecutor aExecutor = new OnewayDispatchExecutor(
				(IDispatchImplementer) this, aURL, lArguments );
		m_aThread = aExecutor;
		aExecutor.start();
	}

	// ////////////////debug methods
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
