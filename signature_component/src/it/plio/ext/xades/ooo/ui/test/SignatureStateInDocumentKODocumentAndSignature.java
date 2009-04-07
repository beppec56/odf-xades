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
import it.plio.ext.xades.ooo.ui.TreeNodeDescriptor;
import it.plio.ext.xades.ooo.ui.TreeNodeDescriptor.TreeNodeType;

/**
 * @author beppe
 *
 */
public class SignatureStateInDocumentKODocumentAndSignature extends SignatureStateInDocument {

	/**
	 * @param user
	 */
	public SignatureStateInDocumentKODocumentAndSignature() {
		super("Dave Rossini");
		String sUserN = "Dave";
		String sUserC = "Rossini";
		// TODO Auto-generated constructor stub
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{
			String[] asArray = new String[m_nMAXIMUM_FIELDS];

			int i = 0;			
			asArray[i++] = new String("bLa firma NON È VALIDA, firmato da:");
			asArray[i++] = new String("r"+sUserN+" "+sUserC);
			asArray[i++] = new String("bL'identità del firmatario NON È VALIDA.");
			asArray[i++] = new String("bNON È POSSIBILE VALIDARE IL CERTIFICATO!");
			asArray[i++] = new String("bFirma qualificata NON VALIDA");
			asArray[i++] = new String("");
			asArray[i++] = new String("bIL DOCUMENTO È STATO MODIFICATO");
			asArray[i++] = new String("bDOPO LA FIRMA.");
			asArray[i++] = new String("bIL DOCUMENTO NON È AFFIDABILE!");
			asArray[i++] = new String("bData e ora della firma:");
			asArray[i++] = new String("r2008.11.02 10:03:34 Z");
			asArray[i++] = new String("");
			asArray[i++] = new String("bL'ora è stata acquisita da un server di");
			asArray[i++] = new String("bmarca temporale il 2008.11.02 10:03:34 Z");
//remove olfd data
			removeCertString(TreeNodeType.SIGNATURE);
			setCertString(TreeNodeType.SIGNATURE, asArray);
		}
		{
			String[] saDummy = {
				"dnQualifier=2006111605A1528\nSN="+sUserN.toUpperCase()+
				"\ngivenName="+sUserC.toUpperCase()+
				"\nE="+sUserN.toLowerCase()+
				"."+sUserC.toLowerCase()+
				"@acme.it\nserialNumber=GCMVRD01D12210Y\nCN=\"GCMVRD01D12210Y/12200910401149623.fmIW78mgkUVdmdQuXCrZbDsW9oQ=\"\nOU=UNKNOWN DI UNKNOWN\nO=Non Dichiarato\nC=IT",
			};
			removeCertString(TreeNodeType.SUBJECT);
			setCertString(TreeNodeDescriptor.TreeNodeType.SUBJECT,saDummy);
		}
		{
			String[] saDummy = {"bInformazioni sul certificato",
					"bNON È POSSIBILE VALIDARE",
					"bIL CERTIFICATO. Ragione:",
					"....",
					"rIl certificato è stato rilasciato per i seguenti scopi:",
					".....",
					".....",
					"",
					"bValido dal 10/04/2006 16:06:04",
					"bal 10/04/2009 16:06:04",
			};
			removeCertString(TreeNodeType.CERTIFICATE);
			setCertString(TreeNodeType.CERTIFICATE, saDummy);
		}
		
	}

	@Override
	public boolean isCertificateValid() {
		return false;
	}
}
