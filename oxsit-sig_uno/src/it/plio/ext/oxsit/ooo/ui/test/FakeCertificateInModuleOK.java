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

import it.plio.ext.oxsit.ooo.ui.SignatureStateInDocument;
import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.TreeNodeType;

/**
 * dummy class to add a fake certificate state ok
 * @author beppe
 *
 */
public class FakeCertificateInModuleOK extends SignatureStateInDocument {

	/** fake certificate, certificate OK
	 * @param user
	 */

	private static final String username = "Giacomo Verdi";

	public FakeCertificateInModuleOK(String sUserName, String sUserSurname) {
		super(sUserName+" "+sUserSurname);
		// TODO Auto-generated constructor stub
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{		
			String[] asArray = getCertStrings(TreeNodeType.SIGNATURE);		
			//remove olfd data
			removeCertString(TreeNodeType.SIGNATURE);
//			int i = 0;
			asArray[m_nSIGNATURE_VALIDITY] = new String("bCertificato conforme per");//new String(m_sSignatureValidity[m_nSIGNATURE_VALIDITY_VALID]);
			asArray[m_nSIGNEE_NAME] =  new String("bfirma qualificata");//new String("r"+sUser);
			asArray[m_nSIGNEE_VALIDATION] =  new String("rRilasciato a:");//new String(m_sSigneeValidity[m_nSIGNEE_VALIDATION_VALID]);
			asArray[m_nEMPTY_FIELD_03] =  new String("r"+sUserName);//new String("");
			asArray[m_nCERT_VALIDITY_STATE1] =  new String("r"+sUserSurname);//new String("bCertificato di firma non verificato");
			asArray[m_nCERT_VALIDITY_STATE2] =  new String("r5");//new String("bControllo certificato con CRL non abilitato");
			asArray[m_nDOCUMENT_VALIDITY1] =  new String("rValido da 2008/11/22");//new String("rIl documento non è stato modificato");
			asArray[m_nDOCUMENT_VALIDITY2] =  new String("rfino a 2011/11/22");//new String("rdopo la firma");
			asArray[m_nSIGNATURE_DATE_TITLE] =  new String("");//new String("bData e ora della firma:");
			asArray[m_nSIGNATURE_DATE] =  new String("");//new String("r2008.11.02 10:03:34 Z");
			asArray[m_nEMPTY_FIELD_10] =  new String("");//new String("sthis text striken out");
			asArray[m_nSIGNATURE_DATE_METHOD_TITLE] =  new String("");//new String("bL'ora è stata acquisita da un server di");
			asArray[m_nSIGNATURE_DATE_METHOD_DATE] =  new String("");//new String("bmarca temporale il 2008.11.02 10:03:34 Z");
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
