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

import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;

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

/**
 * @author beppe
 *
 */
public class ImplInterceptSaveAsDispatch  extends ImplDispatchAsynch implements IDispatchImplementer {

	protected String m_sTitle;
	protected String m_sMessage;

	public ImplInterceptSaveAsDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
//get strings
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xCC, m_axMCF);

		try {
			m_sTitle = m_aRegAcc.getStringFromRegistry( "id_descr" );
			m_sMessage = m_aRegAcc.getStringFromRegistry( "id_question_saveasdoc" );				
		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("", "", e);
		}
		m_aRegAcc.dispose();			
	}

	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {

		m_aLogger.debug("impl_dispatch","aURL "+aURL.Complete+" lArguments.length: "+lArguments.length);
		if(	lArguments.length > 0) {
			String aLog = "";
			for(int i = 0; i <lArguments.length; i++) {
				PropertyValue aValue = lArguments[i];
				
				aLog = aLog+ "name: "+aValue.Name+" "+aValue.Value.toString()+",";
			}
			m_aLogger.debug(aLog);
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
						m_aLogger.severe("impl_dispatch", "", e);
					} catch (ServiceNotFoundException e) {
						m_aLogger.severe("impl_dispatch", "", e);
					} catch (NoSuchMethodException e) {
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
		m_aLogger.debug("Drop down to superclass");
		super.impl_dispatch(aURL, lArguments);
		m_aLogger.debug("return from superclass");		
	}
}
