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

package it.plio.ext.oxsit.signature.dispatchers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.sun.star.beans.NamedValue;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XProperty;
import com.sun.star.beans.XPropertyAccess;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.document.XEventBroadcaster; // import
// com.sun.star.document.XEventListener;
import com.sun.star.frame.ControlCommand;
import com.sun.star.frame.ControlEvent;
import com.sun.star.frame.FeatureStateEvent;
import com.sun.star.frame.FrameActionEvent;
import com.sun.star.frame.XControlNotificationListener;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatch; // import
import com.sun.star.frame.XFrameActionListener; // com.sun.star.frame.XDispatchResultListener;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel; // import
// com.sun.star.frame.XNotifyingDispatch;
import com.sun.star.frame.XStatusListener;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.util.ChangesEvent;
import com.sun.star.util.URL;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XModifiable;
import com.sun.star.bridge.XInstanceProvider;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.dispatchers.ImplDispatchAsynch;
import it.plio.ext.oxsit.dispatchers.threads.ImplXAdESThread;
import it.plio.ext.oxsit.logging.XDynamicLogger;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.pack.TestWriteDigitalSignature;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.ooo.ui.DialogCertificateTree;
import it.plio.ext.oxsit.signature.dispatchers.DocumentURLStatusHelper;
import it.plio.ext.oxsit.singleton.SigletonGlobalVarConstants;
import it.plio.ext.oxsit.singleton.SingletonVariables;

// import it.plio.ext.cnipa.utilities.Utilities;

/**
 * specific for extended toolbar
 * 
 * register as listener for event at the document and at the frame
 * 
 * The document is needed to unregister from the frame data changes and remove the
 * 
 * The frame is needed for multiple windows on the same document
 * 
 * @author beppe
 * 
 */
public class ImplXAdESSignatureDispatchTB extends ImplDispatchAsynch implements
		com.sun.star.document.XEventListener,
		XFrameActionListener,
		XChangesListener,
		XComponent {

	private HashMap<XStatusListener, LinkingStatusListeners>	Listeners;

	// empty url, in case of a new document
	protected String											m_aDocumentURL				= "";
	private int													m_nState					= GlobConstant.m_nSIGNATURESTATE_UNKNOWN;
	private boolean												m_bHasLocation				= false;
	private boolean												m_bIsModified				= false;
	private XModel												m_xModel					= null;
	private XEventBroadcaster									m_DocBroad					= null;
	private Object												m_bIsDocEventRegisteredMutex = new Object();
	private boolean												m_bIsDocEventRegistered		= false;
	
	private boolean												m_bIsFrameActionRegistered	= false;

	private String												m_sToolBarDisabled;
	private String												m_sToolBarSignaturesUnknown;
	private String												m_sToolBarNoSignatures;
	private String												m_sToolBarSignatureValid;
	private String												m_sToolBarCertificateNotValid;
	private String												m_sToolBarBrokenSignatures;
	private String												m_sToolBarDocModified;
	private String												m_imagesUrl					= null;
	
	private Object												m_aFrameConfMutex			= new Object();
	DocumentURLStatusHelper										m_aDocumentConf				= null;
	
	private XComponentContext									m_aComponentContext;
	private XMultiComponentFactory								m_aMultiComponentFctry;

	private boolean m_bSignatureIsEnabled = false;

	public ImplXAdESSignatureDispatchTB(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {
		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch );
		Listeners = new HashMap<XStatusListener, LinkingStatusListeners>( 2 );
		// form the complete Url being intercepted
		// may be we need to check for the interface existence...
		// instantiate the class to get the strings
		m_aComponentContext = xContext;
		m_aMultiComponentFctry = xContext.getServiceManager();
		m_bHasLocation = false;
		m_bSignatureIsEnabled = false;

//FIXME DEBUG m_logger.enableLogging();
		m_logger.ctor("ctor (1)");

		final String sSingletonService = GlobConstant.m_sSINGLETON_SERVICE_INSTANCE;
		try {
			Object oObj = xContext.getValueByName(sSingletonService);
			if(oObj != null)
				m_logger.info(" singleton data "+String.format( "%8H", oObj.hashCode() ));
			else
				m_logger.info("No singleton data");
			
//			Utilities.showInterfaces(this, oObj);
// obtain the property service

// set an integer property, just to check
			com.sun.star.beans.PropertyValue aPathArgument = new com.sun.star.beans.PropertyValue();
			aPathArgument.Name = "signatureXAdESState";
			aPathArgument.Value = new Integer(5252);
			
			XPropertyAccess xPropAccess = (XPropertyAccess)UnoRuntime.queryInterface(XPropertyAccess.class, oObj);

			PropertyValue[] pValues = new PropertyValue[1];
			
			pValues[0] = new PropertyValue();
			
			pValues[0].Name = new String(SigletonGlobalVarConstants.m_sXADES_SIGNATURE_STATE);
			pValues[0].Value = new Integer(23);

			try {
				xPropAccess.setPropertyValues(pValues);
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

// send the property to the Singleton service			

			
/*			XServiceInfo xS = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class,oObj);
			if (xS != null) {
				String aS[] = xS.getSupportedServiceNames();
				m_logger.info(aS[0]);
			}
			else
				m_logger.info("no interface!");*/
/*			SingletonVariables xS = (SingletonVariables)UnoRuntime.queryInterface(SingletonVariables.class,oObj);
			if (xS != null) {
				xS.indentify();
			}
			else
				m_logger.info("no interface!");*/
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}

		m_logger.info("ctor (2)");

		grabModel();
		// Utilities.showInterfaces( m_xModel );
		if (m_xModel != null) {
			// init the status structure from the configuration
			m_aDocumentConf = new DocumentURLStatusHelper( xContext, m_aDocumentURL );
			if (m_aDocumentConf != null) {
				// we are listening on changes to the Frames/ structure, e.g all
				// the frames involved
				m_aDocumentConf.addDocumentChangesListener( this );
				// now check if we already have a status ready
			}

			// register ourself at the document as listeners
			// we register the modified status (when modified signature becomes
			// broken)
			// the end of save (hence location given) the status of the button
			// changes and
			// we unregister from document broadcast
			m_DocBroad = (XEventBroadcaster) UnoRuntime.queryInterface(
					XEventBroadcaster.class, m_xModel );
			if (m_DocBroad != null) {
					m_DocBroad.addEventListener( this );
					m_bIsDocEventRegistered = true;
			}
		}
		if (m_xFrame != null) {
				m_xFrame.addFrameActionListener( this );
				m_bIsFrameActionRegistered = true;
		}
		MessageConfigurationAccess aMex = new MessageConfigurationAccess( xContext, xMCF );
		try {
			m_sToolBarDisabled = aMex.getStringFromRegistry( "id_toolbar_disabled" );
			m_sToolBarNoSignatures = aMex.getStringFromRegistry( "id_toolbar_nosign" );
			m_sToolBarSignatureValid = aMex.getStringFromRegistry( "id_toolbar_sign_ok" );
			m_sToolBarCertificateNotValid = aMex
					.getStringFromRegistry( "id_toolbar_sign_no_cert" );
			m_sToolBarBrokenSignatures = aMex
					.getStringFromRegistry( "id_toolbar_sign_broken" );
			m_sToolBarDocModified = aMex
					.getStringFromRegistry( "id_toolbar_doc_modified" );
			m_sToolBarSignaturesUnknown = aMex
					.getStringFromRegistry( "id_toolbar_doc_unknown" );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_imagesUrl = null;
		XPackageInformationProvider xPkgInfo = PackageInformationProvider.get( m_xCC );
		if(xPkgInfo != null) {
			String sLoc = xPkgInfo
			.getPackageLocation( GlobConstant.m_sEXTENSION_IDENTIFIER );
			if(sLoc != null) 
				m_imagesUrl = sLoc + "/images";
			else
				m_logger.info("ctor: no package location !");
		}
		else
			m_logger.info("ctor: No pkginfo!");
		aMex.dispose();
	}

	private void grabModel() {
		m_aDocumentURL = ""; // init to empty, for new documents
			if (m_xFrame != null) {
				XController xCont = m_xFrame.getController();
				if (xCont != null) {
					m_xModel = xCont.getModel();
					if (m_xModel != null) {
						m_aDocumentURL = m_xModel.getURL();
						// we decide with kind of document this is.
						XStorable xStore = (XStorable) UnoRuntime.queryInterface(
								XStorable.class, m_xModel );
						// decide if new or already saved
						if (xStore != null)
							m_bHasLocation = xStore.hasLocation();
						if (m_bHasLocation) {
//							println( "URL: " + m_aDocumentURL );
							if (m_aDocumentConf != null)
								m_aDocumentConf.setFrameURL( m_aDocumentURL );
						}
						// check to see if modified or not
						XModifiable xMod = (XModifiable) UnoRuntime.queryInterface(
								XModifiable.class, m_xModel );
						if (xMod != null)
							m_bIsModified = xMod.isModified();
						m_bSignatureIsEnabled  = ( m_bHasLocation & !m_bIsModified );
					} else
						m_logger.warning( "grabModel: no model!" );
				} else
					m_logger.warning( "grabModel: no controller!" );
			} else
				m_logger.warning( "grabModel: no frame!" );
	}

	/**
	 * implemented as a one way function
	 * [oneway], arrives here from dispatch() method
	 */
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

		synchronized (this) {			
			try {
				/**
				 * returned value: 1 2 0 = Cancel
				 */
				short ret = signatureDialog();
				XModel xModel = m_xFrame.getController().getModel();
				m_logger.info("impl_dispatch: \tthe url of the document under signature is: "
						+ xModel.getURL() );
				
				TestWriteDigitalSignature aCls = new TestWriteDigitalSignature();				
				aCls.testWriteSignatureStream(xModel.getURL(),m_aMultiComponentFctry,m_aComponentContext);

				int localstate = GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES;
				if (ret != 0) {
					localstate = m_aDocumentConf.getSignatureStatus();
					localstate = localstate + 1;
					localstate = ( localstate > 4 ) ? 0 : localstate;
				}

				// now change the frame location
				m_aDocumentConf.setSignatureStatus( localstate );
				// println( "m_nState is: " + ImplCNIPASignatureDispatch.m_nState );
				/**
				 * while returning we will do as follow Ok was hit: grab the added
				 * signature certificate(s) and sign the document (this may be
				 * something quite log, may be we need to add some user feedback
				 * using the status bar, same as it's done while loading the
				 * document)
				 * 
				 * report the signature status in the registry and (quickly) back to
				 * the dispatcher
				 * 
				 * the signature of the certificates eventually removed are deleted
				 * Current signature are left in place (e.g. depending on
				 * certificates that are left untouched by the dialog)
				 * 
				 * Cancel was hit: the added signature certificate(s) are discarded
				 * and the document is not signed. Current signature are left in
				 * place
				 * 
				 */
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
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

	private String getImageURL() {
		// prepare the value to send the button the new image
		// add an image with a question mark to indicate the unknown status of
		// the signature
		// usually at the beginning
//depends from the document type (writer, calc, draw, impress)		
		
// FIXME detect the toolbar system size? auto?		
		String aSize = "_26.png"; //for large icons toolbar
//		aSize = "_16.png"; //for small icons toolbar
		if(m_imagesUrl != null) {
			switch (m_nState) {
			case GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES:
				return m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE+aSize;// image with certificate image
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK:
				return m_imagesUrl + "/"+"signature"+aSize;// image with certificate image + green tick
//				return m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_OK+aSize;// image with certificate image + green tick
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED:
				return m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_WARNING+aSize; // image with certificate image + warning
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_BROKEN:// image with broken + red cross
				return m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN2+aSize;
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_INVALID:// image with certificate image + danger
				return m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID+aSize;
			default:
			case GlobConstant.m_nSIGNATURESTATE_UNKNOWN: // add an image with a question mark
				// question mark
				return m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_UNKNOWN+aSize;
			}
		}
		return null;
	}

	private String getNewTooltip() {
		// prepare the value to send the button the new image
		if(!m_bSignatureIsEnabled)
			return m_sToolBarDocModified;
		switch (m_nState) {
		case GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES:
			return m_sToolBarNoSignatures;
		case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK:
			return m_sToolBarSignatureValid;
		case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED:
			return m_sToolBarCertificateNotValid;
		case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_BROKEN:
			return m_sToolBarBrokenSignatures;
		case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_INVALID:
			return m_sToolBarDocModified;
		default:
		case GlobConstant.m_nSIGNATURESTATE_UNKNOWN:
			return m_sToolBarSignaturesUnknown;
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
		// get the collection of listeners
		Collection<LinkingStatusListeners> cListenters = Listeners.values();
		// printlnName( "changeSignatureStatus " + cListenters.size() );
		if (!cListenters.isEmpty()) {
			Iterator<LinkingStatusListeners> aIter = cListenters.iterator();
			// grab the package image base url
			String m_imagesUrl = getImageURL();
			String m_NewTooltimp = getNewTooltip();
			grabModel();
			// println ("m_bHasLocation "+m_bHasLocation);
			// scan the array and for every one send the status
			while (aIter.hasNext()) {
				LinkingStatusListeners aLink = aIter.next();
				aLink.m_aMaster.statusChanged( prepareImageFeatureState( m_imagesUrl ) );
				aLink.m_aMaster
						.statusChanged( prepareTooltipFeatureState( m_NewTooltimp ) );
				 m_logger.info("changeSignatureStatus: send listener:" + 
						 new String( String.format( "%8H", aLink.m_aMaster.hashCode() ) ) );
			}
		}
	}

	private void changeSignatureStatus(int newState) {
		if (newState != m_nState) {
			m_logger.info(  "changeSignatureStatus: state changed" );
			m_nState = newState;
			changeSignatureStatus();
		}
	}

	/**
	 * 
	 * @param _sImageURL
	 * @return
	 */
	private FeatureStateEvent prepareImageFeatureState(String _sImageURL) {
		FeatureStateEvent aState = new FeatureStateEvent();
		aState.FeatureDescriptor = new String( "" );
		aState.FeatureURL.Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL
				+ GlobConstant.m_sSIGN_DIALOG_PATH;
		aState.FeatureURL.Protocol = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL;
		aState.FeatureURL.Path = GlobConstant.m_sSIGN_DIALOG_PATH;
		aState.IsEnabled = m_bSignatureIsEnabled; // Boolean.TRUE;
		aState.Requery = Boolean.FALSE;
		aState.Source = this;

		// prepare the value to send the button the new image
		com.sun.star.beans.NamedValue[] aArgs = new NamedValue[1];
		aArgs[0] = new com.sun.star.beans.NamedValue();
		aArgs[0].Name = new String( "URL" );
		aArgs[0].Value = _sImageURL;
		
		m_logger.log(_sImageURL);
		
//		println(_sImageURL);
		
		ControlCommand aCommand = new ControlCommand();
		aCommand.Command = "SetImage";
		aCommand.Arguments = aArgs;
		aState.State = aCommand;
		return aState;
	}

	/**
	 * 
	 * @param _sNewTooltip
	 * @return
	 */
	private FeatureStateEvent prepareTooltipFeatureState(String _sNewTooltip) {
		FeatureStateEvent aState = new FeatureStateEvent();
		aState.FeatureDescriptor = new String( "" );
		aState.FeatureURL.Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL
				+ GlobConstant.m_sSIGN_DIALOG_PATH;
		aState.FeatureURL.Protocol = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL;
		aState.FeatureURL.Path = GlobConstant.m_sSIGN_DIALOG_PATH;
		aState.IsEnabled = m_bSignatureIsEnabled; // Boolean.TRUE;
		aState.Requery = Boolean.FALSE;
		aState.Source = this;

		// prepare the value to send the button the new image
		if (!aState.IsEnabled)
			aState.State = new String( m_sToolBarDisabled );
		else
			aState.State = new String( _sNewTooltip );
		return aState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 *
	 */
	// [one way] not implemented because of initial image setting
	// only one listener is expected: from the custom image toolbar
	//
	public void addStatusListener(com.sun.star.frame.XStatusListener aListener, URL aURL) {
		try {
			synchronized (this) {
				if (aListener != null) {
					LinkingStatusListeners MyListener = new LinkingStatusListeners(
							aListener, aURL, m_aDocumentURL );
					Listeners.put( aListener, MyListener );
//					 println( "+ listener: "
//					 + new String( String.format( "%8H", aListener.hashCode() ) )
//					 + " URL: " + aURL.Complete );
					// grab the document status
					grabModel();//update model
					aListener.statusChanged( prepareImageFeatureState( getImageURL() ) );
					aListener.statusChanged( prepareTooltipFeatureState( getNewTooltip() ) );
				}
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 */
	public void removeStatusListener(com.sun.star.frame.XStatusListener aListener, URL aURL) {
		
		ImplXAdESThread aThread = new ImplXAdESThread(this, ImplXAdESThread.RUN_removeStatusListener, aListener, aURL);
		aThread.start();		
	}
	
	public void impl_removeStatusListener(com.sun.star.frame.XStatusListener aListener, URL aURL) {
		try {
			Listeners.remove( aListener );
			// println( "- listeners: " + Listeners.size() + " URL: " +
			// aURL.Complete );
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		m_logger.info( "addEventListener" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
		m_logger.info( "dispose called" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	public void removeEventListener(com.sun.star.lang.XEventListener arg0) {
		// TODO Auto-generated method stub
		m_logger.info( "removeEventListener(XComponent) called" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.document.XEventListener#notifyEvent(com.sun.star.document.EventObject)
	 * 
	 */
	public void notifyEvent(com.sun.star.document.EventObject aEventObj) {
		// TODO Auto-generated method stub
//		println( "notifyEvent: " + aEventObj.EventName );
		if (/*aEventObj.EventName.equalsIgnoreCase( "OnSaveAsDone" )
				||*/ aEventObj.EventName.equalsIgnoreCase( "OnModifyChanged" )) {
//set the modified status accordingly
			grabModel();
			changeSignatureStatus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(com.sun.star.lang.EventObject aEvent) {
		m_logger.info(" disposing (lang)" );
//		println( "disposing (lang)" );

		com.sun.star.document.XEventBroadcaster xBroad = 
			(XEventBroadcaster) UnoRuntime.queryInterface(com.sun.star.document.XEventBroadcaster.class, aEvent.Source);

		if(xBroad != null ) {
//it's the document, unregister from it and from the frame data changes			
			m_logger.info("disposing: we have an XEventBroadcaster (so we have a docu)");
			
			boolean bIsDocEventRegistered;
			synchronized (m_bIsDocEventRegisteredMutex) {
				bIsDocEventRegistered = m_bIsDocEventRegistered;
				m_bIsDocEventRegistered = false;
			}				
			if (bIsDocEventRegistered == true) {
				m_logger.info( "disposing: doc listening" );
				m_DocBroad.removeEventListener( this );
			}
			DocumentURLStatusHelper								aFrameConf;			
			synchronized(m_aFrameConfMutex) {
				aFrameConf = m_aDocumentConf;
				m_aDocumentConf = null;
			}
			if (aFrameConf != null) {
				m_logger.info("disposing: config changes listener" );
				aFrameConf.removeAllFrameChangesListener( this );
//remove this frame from the data store
				aFrameConf.removeFrameData();
//					aFrameConf.dispose();
			}
			return;
		}

//		Utilities.showInterfaces(aEvent.Source);

		com.sun.star.frame.XFrame xFrame = 
			(XFrame) UnoRuntime.queryInterface(com.sun.star.frame.XFrame.class, aEvent.Source);
			
		if(xFrame != null) {
			// it's the frame, unregister from the frame
			// and from the frame data changes (not needed anuymore because this object
			// it's gonna disposed of
			m_logger.info("disposing: got a frame");
			boolean bIsDocEventRegistered;
			synchronized (m_bIsDocEventRegisteredMutex) {
				bIsDocEventRegistered = m_bIsDocEventRegistered;
				m_bIsDocEventRegistered = false;
			}

			if (bIsDocEventRegistered == true) {
				m_logger.info("disposing: doc listening" );
				m_DocBroad.removeEventListener( this );
			}
				
			DocumentURLStatusHelper								aFrameConf;			
			synchronized(m_aFrameConfMutex) {
				aFrameConf = m_aDocumentConf;
				m_aDocumentConf = null;
			}
			if (aFrameConf != null) {
				m_logger.info("disposing: config changes listener" );
				aFrameConf.removeAllFrameChangesListener( this );
//remove this frame from the data store
				aFrameConf = null;
			}

			if (m_bIsFrameActionRegistered) {
				m_xFrame.removeFrameActionListener( this );
				m_bIsFrameActionRegistered = false;
				m_logger.info("disposing: frame listening" );
			}
		}
	}

/*	private void shutDown() {
		synchronized(this) {
			if(m_bDead)
				return;

			if (m_bIsFrameActionRegistered) {
				m_xFrame.removeFrameActionListener( this );
				m_bIsFrameActionRegistered = false;
				println( "unregister from frame" );
			}
			m_bDead = true;
		}
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XChangesListener#changesOccurred(com.sun.star.util.ChangesEvent)
	 */
	//start the Java thread to manage the task, this is a [oneway] method
	public void changesOccurred(com.sun.star.util.ChangesEvent aChangesEvent) {
		m_logger.info("changesOccurred()" );
		ImplXAdESThread aWorkerThread = new ImplXAdESThread(this,ImplXAdESThread.RUN_changesOccurred, aChangesEvent);
		aWorkerThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.util.XChangesListener#changesOccurred(com.sun.star.util.ChangesEvent)
	 */
	// oneway function: implemented as separate task, comes from changesOccurred above
	public void impl_changesOccurred(com.sun.star.util.ChangesEvent aChangesEvent) {
		m_logger.log(" impl_changesOccurred()" );
		// refresh status of the document model/component
			grabModel();
			
			synchronized(m_aFrameConfMutex) {

				if(m_aDocumentConf == null)
					return;

				if (m_aDocumentConf.isFrameExistent()) {
					// verify if the new URL is different than the one originally
					// recorded
					//may be the SaveAs function was used...
					if (m_aDocumentConf.isFrameChanged( m_aDocumentURL )) {
						m_logger.info(" changing frame view\n" );
						
						m_aDocumentConf.changeDocumentURL(m_aDocumentURL);
						// set the current frame view to the new frame
						m_aDocumentConf.setFrameURL( m_aDocumentURL );
						// check the status updates only if necessary
						changeSignatureStatus( m_aDocumentConf.getSignatureStatus() );
					} else { // frame was already in place, simply update the status
						m_logger.info(" refreshing the signature status\n" );
						changeSignatureStatus( m_aDocumentConf.getSignatureStatus() );
					}
				} else { // a frame was not there, so add it
					m_logger.info(" adding frame view\n" );
					m_aDocumentConf.setFrameURL( m_aDocumentURL );
					// reopen a view and add ourselves as listeners
//					m_aDocumentConf.activateSigleFrameView();
//					m_aDocumentConf.addSingleFrameChangesListener( this );
					// check the status updates only if necessary
					changeSignatureStatus( m_aDocumentConf.getSignatureStatus() );
				}

			}
		// grab the new status, updates ours
			m_logger.info(" state: " + m_nState );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XFrameActionListener#frameAction(com.sun.star.frame.FrameActionEvent)
	 * 
	 * we do nothing here.
	 * The frame listener is used as a means to unregister and dispose of this object (ImpXAdES....)
	 */
	// [oneway] function, not implemented because short function
	public void frameAction(FrameActionEvent aEvent) {
		// give some status indication
/*		printHash("frameAction");
		switch (aEvent.Action.getValue()) {
		case com.sun.star.frame.FrameAction.COMPONENT_DETACHING_value:
			print( "COMPONENT_DETACHING_value" );
			break;
		case com.sun.star.frame.FrameAction.COMPONENT_ATTACHED_value:
			print( "COMPONENT_ATTACHED_value" );
			break;
		case com.sun.star.frame.FrameAction.COMPONENT_REATTACHED_value:
			print( "COMPONENT_REATTACHED_value" );
			break;
		case com.sun.star.frame.FrameAction.FRAME_ACTIVATED_value:
			print( "FRAME_ACTIVATED_value" );
			break;
		case com.sun.star.frame.FrameAction.FRAME_DEACTIVATING_value:
			print( "FRAME_DEACTIVATING_value" );
			break;
		case com.sun.star.frame.FrameAction.CONTEXT_CHANGED_value:
			print( "CONTEXT_CHANGED_value" );
			break;
		case com.sun.star.frame.FrameAction.FRAME_UI_ACTIVATED_value:
			print( "FRAME_UI_ACTIVATED_value" );
			break;
		case com.sun.star.frame.FrameAction.FRAME_UI_DEACTIVATING_value:
			print( "FRAME_UI_DEACTIVATING_value" );
			break;
		default:
			print( "frameAction other value" );
		}
		print("\n");*/
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
		public URL				m_aDispatchCommandURL;

		public LinkingStatusListeners(XStatusListener _ParentListener, URL _aDispatchURL,
				String _DocumentURL) {
			try {
				m_aMaster = _ParentListener;
				m_aDispatchCommandURL = _aDispatchURL;
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
				m_aMaster.statusChanged( aEvent );
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
		 */
		public void disposing(EventObject arg0) {
			// TODO Auto-generated method stub
			m_logger.info( "LinkingStatusListeners disposing" );
		}
	}
}
