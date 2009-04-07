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

package it.plio.ext.xades.ooo.ui.test;

import it.plio.ext.xades.ooo.ui.SignatureStateInDocument;
import it.plio.ext.xades.ooo.ui.TreeNodeDescriptor.TreeNodeType;

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

	public SignatureStateInDocumentOK(String sUserName, String sUserSurname) {
		super(sUserName+" "+sUserSurname);
		// TODO Auto-generated constructor stub
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{		
			String[] asArray = getCertStrings(TreeNodeType.SIGNATURE);		
			//remove olfd data
			removeCertString(TreeNodeType.SIGNATURE);
//			int i = 0;
			asArray[m_nSIGNATURE_VALIDITY] = new String(m_sSignatureValidity[m_nSIGNATURE_VALIDITY_VALID]);
			asArray[m_nSIGNEE_NAME] = new String("r"+username);
			asArray[m_nSIGNATURE_DATE] = new String("r2008.11.02 10:23:34 Z");
			asArray[m_nSIGNATURE_DATE_METHOD_DATE] = new String("bmarca temporale il 2008.11.02 10:23:34 Z");
			setCertString(TreeNodeType.SIGNATURE, asArray);
		}
		//create the standard user
		m_aCert = new CertificateDataOK(sUserName, sUserSurname);
	}

	@Override
	public boolean isCertificateValid() {
		return true;
	}
}
