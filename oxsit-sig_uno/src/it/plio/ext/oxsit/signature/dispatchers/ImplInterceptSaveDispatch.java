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

import it.plio.ext.oxsit.dispatchers.threads.IDispatchImplementer;
import it.plio.ext.oxsit.dispatchers.threads.ImplDispatchAsynch;
import it.plio.ext.oxsit.ooo.ui.DialogQuery;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;

public class ImplInterceptSaveDispatch extends ImplDispatchAsynch implements XDispatch, IDispatchImplementer {

	public ImplInterceptSaveDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
	}

	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {

		m_aLogger.info("impl_dispatch","aURL "+aURL.Complete+" lArguments.length: "+lArguments.length);
		if(	lArguments.length > 0) {
			String aLog = "";
			for(int i = 0; i <lArguments.length; i++) {
				PropertyValue aValue = lArguments[i];
				
				aLog = aLog+ "name: "+aValue.Name+" "+aValue.Value.toString()+",";
			}
			m_aLogger.info(aLog);
		}

		// check the document status, if has XAdES signatures,
		// then alert the user the signatures are lost if saved.

		DialogQuery aDlg = new DialogQuery(m_xFrame, m_axMCF, m_xCC);		
		short ret = aDlg.executeDialog("Domanda", "Il documento contiene delle firme.\r\nSalvando, le firme verranno cancellate.\r\n\r\nConfermate salvataggio ?");
		m_aLogger.log("impl_dispatch", "ret = "+ret);
		// ret = 3: NO
		// ret = 2: SI
		
		if(ret == 3)
			return;
		
/*		try {
//			check the slave one
			com.sun.star.util.URL[] aParseURL = new com.sun.star.util.URL[1];
			aParseURL[0] = new com.sun.star.util.URL();
			aParseURL[0].Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL+ GlobConstant.m_sBEFORE_SAVE_PATH;
			com.sun.star.beans.PropertyValue[] lProperties = new com.sun.star.beans.PropertyValue[1];*/

/*			com.sun.star.frame.XDispatchProvider xProvider =
				(com.sun.star.frame.XDispatchProvider)UnoRuntime.queryInterface(
						com.sun.star.frame.XDispatchProvider.class, m_xFrame);
//			need an URLTransformer
			Object obj;
			obj = m_axMCF.createInstanceWithContext("com.sun.star.util.URLTransformer", m_xCC);
			XURLTransformer xTransformer = (XURLTransformer)UnoRuntime.queryInterface(
					XURLTransformer.class, obj);
			xTransformer.parseStrict( aParseURL );
			m_aLogger.info(aParseURL[0].Protocol+" "+aParseURL[0].Path);
*/
//			Ask it for right dispatch object for our URL.
//			Force given frame as target for following dispatch by using "",
//			it's the same as "_self".
/*			if( xProvider != null ) {
				com.sun.star.frame.XDispatch xDispatcher = null;
				xDispatcher = xProvider.queryDispatch(aParseURL[0],"",0);

				m_aLogger.info("impl_dispatch","xDispatcher "+(xDispatcher == null));
				// Dispatch the URL into the frame.
				if(xDispatcher != null) {
					com.sun.star.frame.XNotifyingDispatch xNotifyingDispatcher = 
						(com.sun.star.frame.XNotifyingDispatch)UnoRuntime.queryInterface(
								com.sun.star.frame.XNotifyingDispatch.class,xDispatcher);*/
/*					if( xNotifyingDispatcher != null )
						xNotifyingDispatcher.dispatchWithNotification(aParseURL[0], lArgumentslProperties, null);
					else*/
						//trow exception: unimplemented interface !...
//					m_aLogger.info("dispatching "+aParseURL[0].Complete);
//						xDispatcher.dispatch(aParseURL[0],lArguments/*lProperties*/);
//					then get from the Notify the value we need of the user answer.
/*
				}
				else
					m_aLogger.info("NO dispatcher for "+aParseURL[0].Complete);
			}
			else
				m_aLogger.info("NO provider for "+aParseURL[0].Complete);*/

			//Dispatch the URL into the frame.
			//please note that this last one is to be dispatched only if the save is enabled by the user
			m_aLogger.info("Drop down to superclass");
			super.impl_dispatch(aURL, lArguments);
/*		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public void signatureDialog() {

//		a simple debug dialog
//		showMessageBox( m_ProtocolBaseUrl , "Signature functions started..." );

/*		DialogListCertificates aDialog1 =
			new DialogListCertificates(
					m_xFrame,
					m_xCC,
					m_axMCF); 		
		try {
			aDialog1.initialize(10,30);
		} catch (BasicErrorException e) {
			e.printStackTrace();
		}
		aDialog1.executeDialog();
*/	}	
}
