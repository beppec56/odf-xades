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

package it.plio.ext.xades.signature.dispatchers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.star.beans.NamedValue;
import com.sun.star.beans.PropertyValue;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.XEventListener;
import com.sun.star.frame.ControlCommand;
import com.sun.star.frame.ControlEvent;
import com.sun.star.frame.DispatchResultEvent;
import com.sun.star.frame.FeatureStateEvent;
import com.sun.star.frame.XControlNotificationListener;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchResultListener;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XNotifyingDispatch;
import com.sun.star.frame.XStatusListener;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XURLTransformer;

import it.plio.ext.xades.dispatchers.ImplDispatchAsynch;
import it.plio.ext.xades.logging.XDynamicLogger;
import it.plio.ext.xades.ooo.GlobConstant;
import it.plio.ext.xades.signature.dispatchers.DocumentURLStatusHelper;
import it.plio.ext.xades.ooo.ui.DialogCertificateTree;
//import it.plio.ext.xades.ooo.ui.DialogListCertificates;

/**
 * @author beppe
 * 
 */
public class ImplXAdESSignatureDispatch extends ImplDispatchAsynch implements
		com.sun.star.document.XEventListener  {

	private HashMap<XStatusListener, LinkingStatusListeners>	Listeners;

	protected String											m_aDocumentURL		= new String(
																							"-not existent frame or URL-" );
	public static int m_nState = 0;	
	private boolean m_bHasLocation = false;
	private XModel m_xModel = null;

	private boolean	m_bIsModified = false;
	DocumentURLStatusHelper								m_aFrameConf	= null;

	public ImplXAdESSignatureDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch );
		// form the complete Url being intercepted
		// may be we need to check for the interface existence...
		m_bHasLocation = false;
		grabModel();
//		Utilities.showInterfaces( m_xModel );
		Listeners = new HashMap<XStatusListener, LinkingStatusListeners>( 4 );		
		m_logger.info("main class");
	}

	private void grabModel() {
		 if (m_xFrame != null) {
				XController xCont = m_xFrame.getController();
				if (xCont != null) {
					m_xModel = xCont.getModel();
					if (m_xModel != null) {
						m_aDocumentURL = m_xModel.getURL();
						// we decide with kind of document this is.
						XStorable xStore = (XStorable) UnoRuntime.queryInterface( XStorable.class, m_xModel );
						// decide if new or already saved
						if(xStore != null)
							m_bHasLocation = xStore.hasLocation();
						if(m_bHasLocation)
							m_logger.info("URL: "+m_aDocumentURL);
//check to see if modified or not
						XModifiable xMod = (XModifiable) UnoRuntime.queryInterface( XModifiable.class, m_xModel );
						if(xMod != null)
							m_bIsModified = xMod.isModified();
					}
					else
						m_logger.info( "no model!" );
				} else
					m_logger.info( "no controller!" );
			} else
				m_logger.info( "no frame!" );		
	}

	protected void impl_dispatch_unused(URL aURL, PropertyValue[] lArguments) {
//call the dispatch of the toolbar, so the status will be updated
		try {
			com.sun.star.util.URL[] aParseURL = new com.sun.star.util.URL[1];
			aParseURL[0] = new com.sun.star.util.URL();
			aParseURL[0].Complete = it.plio.ext.xades.ooo.GlobConstant.m_sSIGN_PROTOCOL_BASE_URL
			+ it.plio.ext.xades.ooo.GlobConstant.m_sSIGN_DIALOG_PATH_TB;
			com.sun.star.beans.PropertyValue[] lProperties = new com.sun.star.beans.PropertyValue[1];

			com.sun.star.frame.XDispatchProvider xProvider = (com.sun.star.frame.XDispatchProvider) UnoRuntime
			.queryInterface( com.sun.star.frame.XDispatchProvider.class, m_xFrame );
			// need an URLTransformer
			Object obj;
			obj = m_axMCF.createInstanceWithContext( "com.sun.star.util.URLTransformer",m_xCC );
			XURLTransformer xTransformer = (XURLTransformer) UnoRuntime.queryInterface(
					XURLTransformer.class, obj );
			xTransformer.parseStrict( aParseURL );

			// Ask it for right dispatch object for our URL.
			// Force given frame as target for following dispatch by using "",
			// it's the same as "_self".
			if (xProvider != null) {
				com.sun.star.frame.XDispatch xDispatcher = null;
				xDispatcher = xProvider.queryDispatch( aParseURL[0], "", 0 );

				// Dispatch the URL into the frame.
				if (xDispatcher != null) {
					xDispatcher.dispatch( aParseURL[0], lProperties );
				}
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {
		// call the select signature dialog
		/*
		 * to access data: singleton PackageInformationProvider
		 * (http://localhost/ooohs-sdk/docs/common/ref/com/sun/star/deployment/PackageInformationProvider.html)
		 * XPackageInformationProvider xPkgInfo =
		 * PackageInformationProvider.get(m_xContext); String m_pkgRootUrl =
		 * xPkgInfo.getPackageLocation("org.openoffice.test.ResourceTest");
		 * 
		 */

		try {
		String m_iconsUrl = null;

		XPackageInformationProvider xPkgInfo = PackageInformationProvider.get( m_xCC );
		m_iconsUrl = xPkgInfo
				.getPackageLocation( "it.plio.ext.cnipa_signature_extension" )
				+ "/icons";

		m_logger.info( m_iconsUrl );

		/**
		 * returned value: 1 2 0 = Cancel
		 */
		short ret = signatureDialog();
		XModel xModel = m_xFrame.getController().getModel();
		System.out.println( this.getClass().getName()
				+ "\n\t\tthe url of the document under signature is: " + xModel.getURL() );

		//grab the frame configuration, point to the frame value
		if(m_xModel != null) {
			// init the status structure from the configuration
			m_aFrameConf = new DocumentURLStatusHelper( m_xCC, m_aDocumentURL );

//			register ourself at the document as listeners
//			we register the modified status (when modified signature becomes broken)
//			the end of save status (model can change, then the rest)

			//for test: if cancel reset status, if ok step to next status

			int localstate = GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES;
			if (ret != 0) {
				localstate = m_aFrameConf.getSignatureStatus();
				m_logger.info("localstate: "+localstate);
				localstate = localstate + 1;
				localstate = ( localstate > 4 ) ? 0 : localstate;
			}

			//now change the frame location
			m_aFrameConf.setSignatureStatus( localstate );
		}
		/**
		 * while returning we will do as follow Ok was hit: grab the added
		 * signature certificate(s) and sign the document (this may be something
		 * quite log, may be we need to add some user feedback using the status
		 * bar, same as it's done while loading the document)
		 * 
		 * report the signature status in the registry and (quickly) back to the
		 * dispatcher
		 * 
		 * the signature of the certificates eventually removed are deleted
		 * Current signature are left in place (e.g. depending on certificates
		 * that are left untouched by the dialog)
		 * 
		 * Cancel was hit: the added signature certificate(s) are discarded and
		 * the document is not signed. Current signature are left in place
		 * 
		 */

/*		if (m_aDispatchListener != null) {
			DispatchResultEvent aEvent = new DispatchResultEvent();
			aEvent.State = ret;
			aEvent.Source = this;
			m_aDispatchListener.dispatchFinished( aEvent );
			m_aDispatchListener = null; // we do not need the object anymore
		}
*/		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public short signatureDialog() {
//		DialogListCertificates aDialog1 = new DialogListCertificates( m_xFrame, m_xCC,
		DialogCertificateTree aDialog1 = new DialogCertificateTree( m_xFrame, m_xCC,
				m_axMCF );
		try {
			aDialog1.initialize( 10, 10 );
		} catch (BasicErrorException e) {
			e.printStackTrace();
		}
		try {
			return aDialog1.executeDialog();
		} catch (BasicErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 */
	public void addStatusListener(XStatusListener aListener, URL aURL) {
		try {
			if(aListener != null ) {
				LinkingStatusListeners MyListener = new LinkingStatusListeners( aListener, aURL,
						m_aDocumentURL );
				Listeners.put( aListener, MyListener );
//				println("+ listener: "+ new String(String.format("%8H",aListener.hashCode() ) ) + " URL: "+aURL.Complete);
//grab the document status

				// prepare the new image for the toolbar and send it

				aListener.statusChanged( prepareFeatureState() );
			}	

		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void impl_addStatusListener(XStatusListener aListener, URL aURL) {
		try {
			if(aListener != null ) {
				LinkingStatusListeners MyListener = new LinkingStatusListeners( aListener, aURL,
						m_aDocumentURL );
				Listeners.put( aListener, MyListener );
//				println("+ listener: "+ new String(String.format("%8H",aListener.hashCode() ) ) + " URL: "+aURL.Complete);
//grab the document status

				// prepare the new image for the toolbar and send it

				aListener.statusChanged( prepareFeatureState() );
			}	

		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	private FeatureStateEvent prepareFeatureState() {
		grabModel();				

		FeatureStateEvent aState = new FeatureStateEvent();
		aState.FeatureDescriptor = new String( "" );
		aState.FeatureURL.Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL
		+ GlobConstant.m_sSIGN_DIALOG_PATH;
		aState.FeatureURL.Protocol = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL;
		aState.FeatureURL.Path = GlobConstant.m_sSIGN_DIALOG_PATH;
		aState.IsEnabled = (m_bHasLocation & !m_bIsModified); //Boolean.TRUE;
		aState.Requery = Boolean.FALSE;
		aState.Source = this;
		return aState;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 */
	public void removeStatusListener(XStatusListener aListener, URL aURL) {
		try {
			Listeners.remove( aListener );
//			println("- listeners: "+Listeners.size()+ " URL: "+aURL.Complete);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * see
	 * http://wiki.services.openoffice.org/wiki/Framework/Article/Generic_UNO_Interfaces_for_complex_toolbar_controls
	 * for details on how this function is used here
	 * 
	 * this function implements the control of the toolbars introduced in OOo
	 * 2.3
	 */
	private void changeSignatureStatus() {
		// get the COllection of listeners
		Collection<LinkingStatusListeners> cListenters = Listeners.values();
		// printlnName("changeSignatureStatus "+cListenters.size());
		if (!cListenters.isEmpty()) {
			Iterator<LinkingStatusListeners> aIter = cListenters.iterator();
			grabModel();				
			
			// scan the array and for every one send the status
			while (aIter.hasNext()) {
				LinkingStatusListeners aLink = aIter.next();				
				aLink.m_aMaster.statusChanged( prepareFeatureState() );
				m_logger.info( "send listener:"
						+ new String( String.format( "%8H", aLink.m_aMaster.hashCode() ) ) );
			}
		}
	}

	/**
	 * our private status listener, used to grab the statusChanged event from
	 * the .uno:Signature slave dispatcher
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
			try {
			m_aMaster = _ParentListener;
			m_aDispatchCommandURL = _aDispatchURL;
			m_aDocumentURL = _DocumentURL;
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
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

				// // mix the two signature status
				// m_nOOoSignatureStatus = ( (Integer) aEvent.State
				// ).intValue();
				// aEvent.State = new Integer( m_nOOoSignatureStatus
				// | m_nCNIPASignatureStatus );
				// // print the status here
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

		public void disposing(EventObject arg0) {
			// TODO Auto-generated method stub
//			println("disposing");
		}
	}

	public void notifyEvent(com.sun.star.document.EventObject aEventObj) {
		// TODO Auto-generated method stub
		m_logger.info("notifyEvent");
	}

	public void disposing(com.sun.star.document.EventObject aEventObj) {
		// TODO Auto-generated method stub
		m_logger.info("disposing (doc)");		
	}

	public void disposing(com.sun.star.lang.EventObject aEventObj) {
		// TODO Auto-generated method stub
		
	}
}
