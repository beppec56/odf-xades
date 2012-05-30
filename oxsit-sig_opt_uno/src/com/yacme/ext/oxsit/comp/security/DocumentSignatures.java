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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/DocumentSigner_IT.java.
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

package com.yacme.ext.oxsit.comp.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.sun.star.embed.XStorage;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XChangesNotifier;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
import com.yacme.ext.oxsit.security.XOX_SignatureState;

/**
 * This service implements the ?? service.<br>
 * receives the doc information from the task  
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * @author beppec56
 *
 */
public class DocumentSignatures extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XChangesNotifier,
			XComponent,
			XInitialization,
			XOX_DocumentSignaturesState
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= DocumentSignatures.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { GlobConstant.m_sDOCUMENT_SIGNATURES_SERVICE };

	protected DynamicLogger m_aLogger;

	// these are the listeners on this document signatures changes
	public HashMap<XChangesListener,XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);
	
	public HashMap<String,XOX_SignatureState> m_aSignatureStates = new HashMap<String, XOX_SignatureState>(10);

	protected XStorage		m_xDocumentStorage;
	// this document signature state
	protected int			m_nDocumentSignatureState;

	protected Boolean		m_aMtx_setDocumentSignatureState;
	protected boolean		m_bThreadNotifyChangesCanRun;
	
	protected String		m_sDocumentId;

	/**
	 * 
	 * 
	 * @param _ctx the UNO context
	 */
	public DocumentSignatures(XComponentContext _ctx) {
		m_aLogger = new DynamicLogger(this, _ctx);
    	m_aLogger.enableLogging();
    	m_aLogger.ctor();
    	m_aMtx_setDocumentSignatureState = new Boolean(false);
    	
    	//prepare and start the thread to notify changes
    	m_bThreadNotifyChangesCanRun = true;
		//call all the listeners, start a new thread for this
		(new Thread(new Runnable() {
			public void run() {
				while(m_bThreadNotifyChangesCanRun) {
					m_aLogger.info("inter thread started");
					synchronized (m_aMtx_setDocumentSignatureState) {
						try {
							m_aMtx_setDocumentSignatureState.wait();
						}
						catch (InterruptedException e) {
						}
						if(m_bThreadNotifyChangesCanRun) {
							m_aLogger.info("inter thread started");
							Collection<XChangesListener> aColl = m_aListeners.values();
							if(!aColl.isEmpty()) {
								Iterator<XChangesListener> aIter = aColl.iterator();
								// scan the array and for every one send the status
								while (aIter.hasNext()) {
									XChangesListener aThisOne =aIter.next();
									aThisOne.changesOccurred(null);
								}
							}
							m_aLogger.info("inter thread wraps, there were ", ((aColl.isEmpty()) ? "no" : aColl.size())+" listener");
						}
					}
				}
				m_aLogger.info("inter thread removed");
			}
		}
		)).start();
	}

	@Override
	public String getImplementationName() {
		m_aLogger.entering("getImplementationName");
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

		m_aLogger.debug("supportsService",_sService);
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
		if(!m_aListeners.containsKey(_ChangesListener) &&
				_ChangesListener != null ) {
			m_aListeners.put(_ChangesListener, _ChangesListener);
			m_aLogger.entering("addChangesListener "+Helpers.getHashHex(_ChangesListener));
		}		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesNotifier#removeChangesListener(com.sun.star.util.XChangesListener)
	 */
	@Override
	public void removeChangesListener(XChangesListener _ChangesListener) {
		if(m_aListeners.containsKey(_ChangesListener) ) {
			m_aListeners.remove(_ChangesListener);
			m_aLogger.entering("removeChangesListener "+Helpers.getHashHex(_ChangesListener));		
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * when instantiated, 
	 * 	_oObj[0] first argument document URL
	 *  _oObj[1] corresponding XStorage object
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		m_aLogger.entering("initialize");		
	}	

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#getDocumentStorage()
	 * 
	 * IMPORTANT the manipulation of storage variable is Sync Job only responsability!
	 */
	@Override
	public XStorage getDocumentStorage() {
		m_aLogger.debug("getDocumentStorage");		
		return m_xDocumentStorage;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#setDocumentStorage(com.sun.star.embed.XStorage)
	 * IMPORTANT the manipulation of storage variable is Sync Job only responsability!
	 */
	@Override
	public void setDocumentStorage(XStorage _xStore) {
		m_aLogger.debug("setDocumentStorage");		
		m_xDocumentStorage = _xStore;		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#getDocumentId()
	 */
	@Override
	public String getDocumentId() {
		m_aLogger.debug("getDocumentId");
		return m_sDocumentId;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_DocumentSignaturesState#setDocumentId(java.lang.String)
	 */
	@Override
	public void setDocumentId(String arg0) {
		m_sDocumentId = arg0;
		m_aLogger.debug("setDocumentId");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		m_aLogger.debug("addEventListener");
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		m_aLogger.debug("dispose");
		m_bThreadNotifyChangesCanRun = false;
		synchronized (m_aMtx_setDocumentSignatureState) {
//this whill notifies the listeners still attached to detach.			
			m_aMtx_setDocumentSignatureState.notify();
		}
//free the SignatureStat elements, if any
		try {
			Set aSignIDs = m_aSignatureStates.keySet();
			Iterator<String> aIter = aSignIDs.iterator();
			while(aIter.hasNext()) {
				XOX_SignatureState aState = m_aSignatureStates.remove(aIter.next());
				((XComponent) UnoRuntime.queryInterface(XComponent.class, aState)).dispose();				
			}
		}
		catch (Throwable ex) {
			m_aLogger.severe(ex);
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		m_aLogger.debug("removeEventListener");		
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#getAggregatedDocumentSignatureStates()
	 */
	@Override
	public int getAggregatedDocumentSignatureStates() {
		m_aLogger.debug("getAggregatedDocumentSignatureStates");
		synchronized (m_aMtx_setDocumentSignatureState) {
			return m_nDocumentSignatureState;			
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#getDocumentSignatureStates()
	 */
	@Override
	public XOX_SignatureState[] getDocumentSignatureStates() {
		final String __FUNCTION__ = "getDocumentSignatureStates: ";
		XOX_SignatureState[] ret = null;

		//detect the number of vector present
		if(!m_aSignatureStates.isEmpty()) {
			try {
				ret = new XOX_SignatureState[m_aSignatureStates.size()];
				Collection<XOX_SignatureState> retC = m_aSignatureStates.values();
				Object xObs[] = retC.toArray();
				for(int y = 0; y < xObs.length; y++)
					ret[y] = (XOX_SignatureState)xObs[y]; 

			} catch(NullPointerException ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			} catch(IndexOutOfBoundsException ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			} catch(ArrayStoreException ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			}  catch(Throwable ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#setAggregatedDocumentSignatureStates(int)
	 * when this method is called, the signature state is notified to all the m_aListeners
	 */
	@Override
	public void setAggregatedDocumentSignatureStates(int _nState) {
		m_aLogger.entering("setDocumentSignatureState","_nState is: "+_nState);
		synchronized (m_aMtx_setDocumentSignatureState) {			
			m_nDocumentSignatureState = _nState;
			m_aMtx_setDocumentSignatureState.notify();
		}		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#addSignatureState(com.yacme.ext.oxsit.security.XOX_SignatureState)
	 */
	@Override
	public int addSignatureState(XOX_SignatureState _xSignatureState) {
		m_aSignatureStates.put(_xSignatureState.getSignatureUUID(), _xSignatureState);
		return 1;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#getSignatureState(java.lang.String)
	 */
	@Override
	public XOX_SignatureState getSignatureState(String _sSignatureID) {
		return m_aSignatureStates.get(_sSignatureID);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState#removeSignatureState(java.lang.String)
	 */
	@Override
	public int removeSignatureState(String _sSignatureID) {
		XOX_SignatureState aState = m_aSignatureStates.remove(_sSignatureID);
		if(aState != null) {
//clean up the object retrieved
			try {
				((XComponent) UnoRuntime.queryInterface(XComponent.class, aState)).dispose();
			}
			catch (Throwable e) {
				m_aLogger.severe(e);
			}
			return 1;
		}
		else
			return 0;
	}
}
