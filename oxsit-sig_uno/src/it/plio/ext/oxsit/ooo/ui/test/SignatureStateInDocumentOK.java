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

package it.plio.ext.oxsit.ooo.ui.test;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.ooo.ui.test.TreeNodeDescriptor.TreeNodeType;

/**
 * dummy class to add a fake certificate state ok
 * @author beppe
 *
 */
public class SignatureStateInDocumentOK extends SignatureStateInDocument {

	/** fake certificate, certificate OK
	 * @param user
	 */

	private static final String username = "Giacomo Verdi";

	public SignatureStateInDocumentOK(String sUserName, String sUserSurname, XComponentContext _Context, XMultiComponentFactory _xMCF) {
		super(sUserName+" "+sUserSurname, _Context, _xMCF);
		// TODO Auto-generated constructor stub
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{		
			MessageConfigurationAccess m_aRegAcc = new MessageConfigurationAccess(_Context, _xMCF);
			String[] asArray = getCertStrings(TreeNodeType.SIGNATURE);		
			//remove olfd data
			removeCertString(TreeNodeType.SIGNATURE);
//			int i = 0;
			try {
				asArray[m_nSIGNATURE_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_sig_ok" );
				asArray[m_nDOCUMENT_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_docu_ok" );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setCertString(TreeNodeType.SIGNATURE, asArray);
			m_aRegAcc.dispose();	
		}
		//create the standard user
		m_aCert = new CertificateDataOK(sUserName, sUserSurname);
	}

	@Override
	public boolean isCertificateValid() {
		return true;
	}
}
