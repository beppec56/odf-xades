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

import it.plio.ext.oxsit.dispatchers.ImplDispatchSynch;
import it.plio.ext.oxsit.ooo.GlobConstant;
//import it.plio.ext.oxsit.ooo.ui.DialogListCertificates;

import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.XComponentContext;

public class ImplBeforeSaveDispatch extends ImplDispatchSynch {

	public ImplBeforeSaveDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
	}

	public void dispatch( com.sun.star.util.URL aURL,
			com.sun.star.beans.PropertyValue[] aArguments ) {
		System.out.println("com.sun.star.frame.XDispatch#dispatch: "+GlobConstant.m_sBEFORE_SAVE_PATH);
		/** we will alert the user, save will clear the digital signatures
		 *, then we will return the user selection return
		 */
		
// implement a simple dialog, as the about dialog, with a message talking about what to do		
//		signatureDialog();
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
