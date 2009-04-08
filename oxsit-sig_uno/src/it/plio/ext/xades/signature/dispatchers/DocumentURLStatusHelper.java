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

import it.plio.ext.xades.ooo.GlobConstant;
import it.plio.ext.xades.ooo.GlobalVariables;

import com.sun.star.lang.XEventListener;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;

public class DocumentURLStatusHelper /*extends ConfigurationAccess implements
		XComponent */{
// configuration for all frames, check for changes from frames
//	private Object				m_oAllFramesConfView	= null;
//	private XChangesNotifier	m_xAllFramesNotifier			= null;
//configuration for a single frame, active only if the frame is already there
//the file save clears it	
//	private Object				m_oOneFrameConfView	= null;
//	private XChangesNotifier	m_xOneFrameNotifier	= null;

//	private String				m_sFrameURLHash;
	private String				m_sFrameURL;
	GlobalVariables 			globalVar = GlobalVariables.getInstance();

	/**
	 * instantiate the class, initialize it
	 * if the requested frame data doesn't exist, will do nothing
	 * the initialization is user's task! 
	 * @param _xContext the UNO context
	 * @param _FrameURL the frame url we are working on
	 */
	public DocumentURLStatusHelper(XComponentContext _xContext, String _FrameURL) {

//		super( _xContext );
//		printlnName("ctor");
		setFrameURL( _FrameURL );
	}

	public boolean isFrameChanged(String aURL) {
		return !aURL.equals( m_sFrameURL );
	}

	public boolean isFrameExistent( ) {
		return globalVar.existDocumentURL(m_sFrameURL);
	}

	public void setFrameURL(String aURL) {
		m_sFrameURL = aURL;
		// FrameURLHashCode =-> Fuhc
	}
	
	public synchronized void changeDocumentURL(String newURL) {
		globalVar.changeDocumentURL(m_sFrameURL, newURL);
		m_sFrameURL = newURL;
	}
	/**
	 * open the view on the frame element
	 * to be called before doing init, which would add the frame instead
	 */

	public short getSignatureStatus() {
		short retVal = GlobConstant.m_nSIGNATURESTATE_UNKNOWN;
		int nState = globalVar.getSignatureState(m_sFrameURL);

		retVal = (short)nState; 
		return retVal;
	}

	/**
	 * to be called only if frame as been initialized
	 * @param newState
	 */
	public void setSignatureStatus(short newState) {
		globalVar.setDocumentSignatureState(m_sFrameURL, newState);
	}

	public void setSignatureStatus(int newState) {
		setSignatureStatus( (short) newState );
	}

/*	public void setSignatureURL(String _theURL) {
	
		
		// add the property or change it if not available
		if (m_oAllFramesConfView != null) {
			// we are on the correct spot
//prepare the name container
			XNameContainer xNC =  (XNameContainer) UnoRuntime.queryInterface(
					XNameContainer.class, m_oAllFramesConfView );

			if(xNC.hasByName( m_sFrameURLHash )) {
//grab properties and set the new value
				Object oObj = null;
				XPropertySet xPS = null;
					try {
						oObj = xNC.getByName( m_sFrameURLHash);
						xPS = (XPropertySet) UnoRuntime.queryInterface(
								XPropertySet.class, oObj );
						if(xPS != null) {
							xPS.setPropertyValue( "URL", new String( _theURL ) );
							// commit immediately to persistent storage
							commitChanges( m_oAllFramesConfView );				
						} else
							println("no property set: internal initialization error!");
							
					} catch (NoSuchElementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WrappedTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownPropertyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (PropertyVetoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			else
				println("no frame: class "+getClass().getName()+" initialization error!");
		}
		else
			println("no view: class "+getClass().getName()+" initialization error!");
	}
	*/
	public void removeFrameData() {
		globalVar.removeDocumentURL(m_sFrameURL);
	}

	/**
	 * this one remove all the name container from the structure to be called at
	 * the start and at the end of the application by the Sync Job IMPORTANT:
	 * MUST be called when all the frame are closed (and no other view are open
	 * on this element)
	 * 
	 */
	public void removeAllElements() {
		globalVar.removeAllDocumentURL();
	}

	/**
	 * @param xListener
	 */
	public void addDocumentChangesListener(XChangesListener xListener) {
		globalVar.addListenerToADocument(m_sFrameURL, xListener);
	}

	/**
	 * @param xListener
	 */
	public void removeAllFrameChangesListener(XChangesListener xListener) {
		globalVar.removeListenerFromADocument(m_sFrameURL, xListener);
	}

/*	public boolean hasSingleFrameView() {
		return m_oOneFrameConfView != null;
	}*/

	/**
	 * activate the single frame view, only for listening
	 * changes 
	 */
/*	public	void activateSigleFrameView() {
		if(m_oOneFrameConfView == null) {
			if(isFrameExistent()) {
				//ok, open the view and set the correct parameters
				try {
					m_oOneFrameConfView = createConfigurationReadWriteView( GlobConstant.m_sEXTENSION_CONF_FRAME_KEY+ m_sFrameURLHash );
				} catch (Exception e) {
					e.printStackTrace();
					printlnName( "creation problem: main frame view has not been created ("
							+ GlobConstant.m_sEXTENSION_CONF_FRAME_KEY + m_sFrameURLHash+")." );
				}
				if(m_oOneFrameConfView != null) {
					//add the listener interface
					m_xOneFrameNotifier = (XChangesNotifier) UnoRuntime.queryInterface(
							XChangesNotifier.class, m_oOneFrameConfView );					
				}				
			}
			else
				println("the frame record does not exist!");
		}
		else
			println("close the view first (release the listeners if any))!");
	}*/

	/**
	 * opposite of the above
	 * IMPORTANT: the caller shall call the respective
	 * function to remove listeners before changing the view
	 */
/*	public void disposeSingleFrameView() {
		if(m_oOneFrameConfView != null) {
			//dispose of the view and close it
			( (XComponent) UnoRuntime.queryInterface( XComponent.class,
					m_oOneFrameConfView ) ).dispose();			
			m_oOneFrameConfView = null;
			m_xOneFrameNotifier = null;
		}		
	}*/

/*	public void addSingleFrameChangesListener(XChangesListener xListener) {
		if (m_xOneFrameNotifier != null) {
			m_xOneFrameNotifier.addChangesListener( xListener );
			println("single frame listener added");
		}
	}*/

/*	public void removeSingleFrameChangesListener(XChangesListener xListener) {
		if (m_xOneFrameNotifier != null)
			m_xOneFrameNotifier.removeChangesListener( xListener );
		println("single frame listener removed");
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#dispose()
	 * 
	 * dispose the view, call the base class to dispose of the provider
	 */
/*	public void dispose() {
		synchronized (this) {
			if (m_oAllFramesConfView != null) {
				( (XComponent) UnoRuntime.queryInterface( XComponent.class,
						m_oAllFramesConfView ) ).dispose();
				m_oAllFramesConfView = null;
			}
		}
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub

	}

}
