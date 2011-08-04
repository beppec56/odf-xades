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

package com.yacme.ext.oxsit.signature.dispatchers;

import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
import com.yacme.ext.oxsit.security.XOX_DocumentSigner;

import java.util.HashMap;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.frame.FeatureStateEvent;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStatusListener;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XModifiable;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.dispatchers.threads.ImplDispatchAsynch;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.DialogSignatureTreeDocument;

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
	
	protected XOX_SingletonDataAccess							m_xSingletonDataAccess;
	protected XOX_DocumentSignaturesState							m_xDocumentSignatures;
		
	public ImplXAdESSignatureDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch );
		// form the complete Url being intercepted
		// may be we need to check for the interface existence...
		Listeners = new HashMap<XStatusListener, LinkingStatusListeners>( 4 );
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		m_bHasLocation = false;
		try {
				m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
				m_aLogger.debug(" singleton service data "+Helpers.getHashHex(m_xSingletonDataAccess) );
		}
		catch (ClassCastException e) {
			m_aLogger.severe("ctor","",e);
		} catch (ServiceNotFoundException e) {
			m_aLogger.severe("ctor","",e);
		} catch (NoSuchMethodException e) {
			m_aLogger.severe("ctor","",e);
		}

		grabModel();
		if (m_xModel != null) {
			// init the status structure from the configuration
			if(m_xSingletonDataAccess != null) {
				//add this to the document-signatures list
				 m_xDocumentSignatures = m_xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(m_xModel), null);
			}
			else
				m_aLogger.severe("ctor","XOX_SingletonDataAccess missing!");
		}		
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
/*						if(m_bHasLocation)
							m_aLoggerDialog.info("URL: "+m_aDocumentURL);*/
//check to see if modified or not
						XModifiable xMod = (XModifiable) UnoRuntime.queryInterface( XModifiable.class, m_xModel );
						if(xMod != null)
							m_bIsModified = xMod.isModified();
					}
					else
						m_aLogger.debug( "no model!" );
				} else
					m_aLogger.debug( "no controller!" );
			} else
				m_aLogger.debug( "no frame!" );		
	}
	
	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {
		// call the select signature dialog
		/*
		 * to access data: singleton PackageInformationProvider
		 * (http://localhost/ooohs-sdk/docs/common/ref/com/sun/star/deployment/
		 * PackageInformationProvider.html) XPackageInformationProvider xPkgInfo
		 * = PackageInformationProvider.get(m_xContext); String m_pkgRootUrl =
		 * xPkgInfo.getPackageLocation("org.openoffice.test.ResourceTest");
		 */

		try {
			/**
			 * returned value: 1 2 0 = Cancel
			 */
			grabModel();

			XStorageBasedDocument xDocStorage = (XStorageBasedDocument) UnoRuntime
					.queryInterface(XStorageBasedDocument.class, m_xModel);
			short ret;
			//first check if the document can be signed
			if(m_xModel != null) {
				Object oDocumSigner;
				try {
					//the object name is resident in configuration, should be loaded from there,
					//getting it from the currently active signature type configuration
					oDocumSigner = m_xCC.getServiceManager().createInstanceWithContext(GlobConstant.m_sDOCUMENT_SIGNER_SERVICE_IT, m_xCC);
					XOX_DocumentSigner xSigner = (XOX_DocumentSigner) UnoRuntime.queryInterface(XOX_DocumentSigner.class, oDocumSigner);
					if (xSigner != null) {
						try {
							//Call the document verify pre-signature method
							if (!xSigner.verifyDocumentBeforeSigning(m_xFrame, getDocumentModel(), null)) {
								return;
							}
						} catch (IllegalArgumentException e) {
							m_aLogger.severe("actionPerformed", "", e);
						} catch (Exception e) {
							m_aLogger.severe("actionPerformed", "", e);
						}
					}
				} catch (Exception e1) {
					m_aLogger.severe("actionPerformed", "", e1);
				} catch (Throwable e1) {
					m_aLogger.severe("actionPerformed", "", e1);
				}
			}
			else
				return;
			
			try {
				
				
				
				ret = signatureDialog();

				// grab the frame configuration, point to the frame value
				if (m_xModel != null) {
					// init the status structure from the configuration
					if (m_xSingletonDataAccess != null) {
						// add this to the document-signatures list
						m_xDocumentSignatures = m_xSingletonDataAccess
								.initDocumentAndListener(Helpers.getHashHex(m_xModel), null);
//						int localstate = GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES;
//						if (ret != 0) {
//							localstate = m_xDocumentSignatures
//									.getDocumentSignatureState();
//							m_aLogger.debug("localstate: " + localstate + " "
//									+ m_xDocumentSignatures.getDocumentId());
//							localstate = localstate + 1;
//							localstate = (localstate > 4) ? 0 : localstate;
//						}
//
//						// now change the frame location
//						m_xDocumentSignatures
//								.setDocumentSignatureState(localstate);
					} else
						m_aLogger.severe("ctor",
								"XOX_SingletonDataAccess missing!");
				}
/*			} catch (IOException e) {
				m_aLogger.severe(e);
			} catch (Exception e) {
				m_aLogger.severe(e);*/
			} catch (Throwable e) {
				m_aLogger.severe(e);
			}
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

			/*
			 * if (m_aDispatchListener != null) { DispatchResultEvent aEvent =
			 * new DispatchResultEvent(); aEvent.State = ret; aEvent.Source =
			 * this; m_aDispatchListener.dispatchFinished( aEvent );
			 * m_aDispatchListener = null; // we do not need the object anymore
			 * }
			 */
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public short signatureDialog() {
		DialogSignatureTreeDocument aDialog1 = new DialogSignatureTreeDocument( m_xFrame, m_xCC,
				m_axMCF );
		try {
			aDialog1.setDocumentModel(getDocumentModel());
			aDialog1.initialize( 10, 10 );
		} catch (BasicErrorException e) {
			e.printStackTrace();
		}
		try {
			return aDialog1.executeDialog();
		} catch (BasicErrorException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener,
	 *      com.sun.star.util.URL)
	 * this method is called by the framework to asses the status
	 * of the  menu enabled/disabled. We'll set it
	 * according to the rest of the document state (save/non saved/changed, etc...)
	 */
	public void addStatusListener(XStatusListener aListener, URL aURL) {
		m_aLogger.entering("addStatusListener");
		try {
			if(aListener != null ) {
				LinkingStatusListeners MyListener = new LinkingStatusListeners( aListener, aURL,
						m_aDocumentURL );
				Listeners.put( aListener, MyListener );
				aListener.statusChanged( prepareFeatureState() );
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		}
	}

	private void impl_addStatusListener(XStatusListener aListener, URL aURL) {
		m_aLogger.entering("impl_addStatusListener");
		try {
			if(aListener != null ) {
				LinkingStatusListeners MyListener = new LinkingStatusListeners( aListener, aURL,
						m_aDocumentURL );
				Listeners.put( aListener, MyListener );
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
		aState.IsEnabled = Boolean.TRUE;//always enabled, test done before performing the action (m_bHasLocation & !m_bIsModified); //Boolean.TRUE;
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
		m_aLogger.entering("removeStatusListener");
		try {
			Listeners.remove( aListener );
		} catch (RuntimeException e) {
			e.printStackTrace();
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
		m_aLogger.debug("notifyEvent");
	}

	public void disposing(com.sun.star.document.EventObject aEventObj) {
		// TODO Auto-generated method stub
		m_aLogger.debug("disposing (doc)");		
	}

	public void disposing(com.sun.star.lang.EventObject aEventObj) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {

		m_aLogger.entering("dispose");
	}

	/**
	 * @return the m_xDocumentStorage
	 */
	public XModel getDocumentModel() {
		return m_xModel;
	}
}
