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

package com.yacme.ext.oxsit.signature.dispatchers;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.dispatchers.threads.IDispatchImplementer;
import com.yacme.ext.oxsit.dispatchers.threads.ImplDispatchAsynch;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import com.yacme.ext.oxsit.ooo.ui.DialogQuery;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;

public class ImplInterceptSaveDispatch extends ImplDispatchAsynch implements XDispatch, IDispatchImplementer {

	protected String m_sTitle;
	protected String m_sMessage;

	public ImplInterceptSaveDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
//get strings
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xCC, m_axMCF);

		try {
			m_sTitle = m_aRegAcc.getStringFromRegistry( "id_descr" );
			m_sMessage = m_aRegAcc.getStringFromRegistry( "id_question_savedoc" );				
		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("", "", e);
		}
		m_aRegAcc.dispose();	
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
		
		if (m_xFrame != null) {
			XController xCont = m_xFrame.getController();
			if (xCont != null) {
				XModel m_xModel = xCont.getModel();
				if (m_xModel != null) {	
					try {
						 XOX_DocumentSignaturesState xoxDocSigns = Helpers.getDocumentSignatures(m_xCC,m_xModel);						 
						 int sigState = xoxDocSigns.getAggregatedDocumentSignatureStates();
						 if(sigState != GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES) {
							DialogQuery aDlg = new DialogQuery(m_xFrame, m_axMCF, m_xCC);		
							short ret = aDlg.executeDialog(m_sTitle, m_sMessage,
									MessageBoxButtons.BUTTONS_YES_NO, //message box type
									MessageBoxButtons.DEFAULT_BUTTON_NO);//default button
							// ret = 3: NO
							// ret = 2: Yes
							if(ret == 3)
								return;
						 }
					} catch (ClassCastException e) {
						// TODO Auto-generated catch block
						m_aLogger.severe("impl_dispatch", "", e);
					} catch (ServiceNotFoundException e) {
						// TODO Auto-generated catch block
						m_aLogger.severe("impl_dispatch", "", e);
					} catch (NoSuchMethodException e) {
						// we can drop here if the document signatures data doesn't
						//exist: it happens for a newly created document,
						//eg, with no storage
						m_aLogger.severe("impl_dispatch", "", e);
					}					
				} else
					m_aLogger.warning( "grabModel: no model!" );
			} else
				m_aLogger.warning( "grabModel: no controller!" );
		}
		else
			m_aLogger.warning( "grabModel: no frame!" );

		//Dispatch the URL into the frame.
		//please note that this last one is to be dispatched only if the save is enabled by the user
		m_aLogger.info("Drop down to superclass");
		super.impl_dispatch(aURL, lArguments);
		m_aLogger.info("return from superclass");		
	}	
}
