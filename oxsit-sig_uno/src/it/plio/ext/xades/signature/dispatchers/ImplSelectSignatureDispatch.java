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

import it.plio.ext.oxsit.logging.XDynamicLogger;
import it.plio.ext.xades.dispatchers.ImplDispatchAsynch;
import it.plio.ext.xades.dispatchers.ImplDispatchSynch;
import it.plio.ext.xades.ooo.ui.DialogChooseSignatureTypes;
import it.plio.ext.xades.ooo.ui.DialogInformation;
import it.plio.ext.xades.ooo.ui.DialogQuery;
import it.plio.ext.xades.signature.SignatureHandler;

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XReschedule;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.DispatchResultEvent;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchResultListener;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XNotifyingDispatch;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XURLTransformer;

public class ImplSelectSignatureDispatch extends ImplDispatchAsynch implements
		XNotifyingDispatch {

	private XDispatchResultListener	m_aDispatchListener;

	public ImplSelectSignatureDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch );
		m_aDispatchListener = null;
	}

	public void impl_dispatch(com.sun.star.util.URL aURL,
			com.sun.star.beans.PropertyValue[] aArguments) {
		/**
		 * TODO first see if the doc is saved (that is if has location) if the
		 * document has location, go on, else we need to alert the user to save
		 * the document before anything (TO BE IMPLEMENTED) then, when saved,
		 * (trough a save) start the signature process
		 */

		short ret = DialogChooseSignatureTypes.NoSignatureSelected;
		XModel xModel = m_xFrame.getController().getModel();
		XStorable xStore = (XStorable) UnoRuntime
				.queryInterface( XStorable.class, xModel );
		if (xStore.hasLocation() != true) {
			// query if need be saved
			// build a message box
			DialogInformation aInformation = new DialogInformation( m_xFrame, m_axMCF,
					m_xCC );
			ret = aInformation
					.executeDialog(
							"OpenOffice.org CNIPA signatures",
							"The document has to be saved before it can be signed.\nUse the normal Save command to do it." );
			ret = 0; // do nothing, save is carried out outside

			/*
			 * DialogQuery aQuery = new DialogQuery(m_xFrame, m_axMCF, m_xCC);
			 * ret = aQuery.executeDialog("OpenOffice.org CNIPA signatures","The
			 * document has to be saved before it can be signed.\nDo you want to
			 * save the document?");
			 */
			if (ret != 0) {
				// yes save the current document
				com.sun.star.util.URL[] aParseURL = new com.sun.star.util.URL[1];
				aParseURL[0] = new com.sun.star.util.URL();
				aParseURL[0].Complete = it.plio.ext.oxsit.ooo.GlobConstant.m_sUNO_SAVE_AS_URL_COMPLETE;
				com.sun.star.beans.PropertyValue[] lProperties = new com.sun.star.beans.PropertyValue[1];

				com.sun.star.frame.XDispatchProvider xProvider = (com.sun.star.frame.XDispatchProvider) UnoRuntime
						.queryInterface( com.sun.star.frame.XDispatchProvider.class,
								m_xFrame );
				// need an URLTransformer
				try {
					Object obj;
					obj = m_axMCF.createInstanceWithContext(
							"com.sun.star.util.URLTransformer", m_xCC );
					XURLTransformer xTransformer = (XURLTransformer) UnoRuntime
							.queryInterface( XURLTransformer.class, obj );
					xTransformer.parseStrict( aParseURL );
					if (xProvider != null) {
						com.sun.star.frame.XDispatch xDispatcher = null;
						xDispatcher = xProvider.queryDispatch( aParseURL[0], "", 0 );

						// Dispatch the URL into the frame.
						if (xDispatcher != null) {
							xDispatcher.dispatch( aParseURL[0], lProperties );
							m_logger.info( "dispatch done  " + ret );
							try {
								Thread.sleep( 5000 );
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (com.sun.star.uno.Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				// no, exit with ret = 0 (cancel)
				ret = 0; // force to exit
		} else {// has location then we can sign it
			ret = selectSignatureDialog();
		}
		if (m_aDispatchListener != null) {
			DispatchResultEvent aEvent = new DispatchResultEvent();
			aEvent.State = ret;
			m_aDispatchListener.dispatchFinished( aEvent );
			m_aDispatchListener = null; // we do not need the object anymore
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
		m_aDispatchListener = _xDispatchResultsListener;
		dispatch( aURL, lProps ); // this in turn will start the working
									// thread
	}

	private short selectSignatureDialog() {
		short nRet = -1;
		DialogChooseSignatureTypes aDialog1 = new DialogChooseSignatureTypes( m_xFrame,
				m_xCC, m_axMCF );
		try {
			aDialog1.initialize( 0, 0 ); 
			nRet = aDialog1.executeDialog();
		} catch (BasicErrorException e) {
			e.printStackTrace();
		} catch (com.sun.star.uno.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nRet;
	}
}
