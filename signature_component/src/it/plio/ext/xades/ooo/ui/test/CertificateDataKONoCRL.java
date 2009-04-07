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

import it.plio.ext.xades.ooo.ui.CertificateData;
import it.plio.ext.xades.ooo.ui.TreeNodeDescriptor;
import it.plio.ext.xades.ooo.ui.TreeNodeDescriptor.TreeNodeType;

public class CertificateDataKONoCRL extends CertificateData {

	public CertificateDataKONoCRL(String aUserName, String aUserSurname) {
		super(); // user name will disappear in finished code 
//fill in the custom data
		{
			String[] saDummy = getCertStrings(TreeNodeType.CERTIFICATE);			
			removeCertString(TreeNodeType.CERTIFICATE);

			saDummy[m_nEMPTY_FIELD_01] = "";
			saDummy[m_nINFO_LINE1] = "rIl certificato è adatto per la firma digitale.";
			saDummy[m_nINFO_LINE2] = "r(non ripudio attivato)";
			saDummy[m_nTITLE_SCOPE] = "bIl certificato è stato rilasciato per i seguenti scopi:";
			saDummy[m_nSCOPE_LINE1] = "..";
			saDummy[m_nSCOPE_LINE2] = "..";
			saDummy[m_nSCOPE_LINE3] = "..";
			saDummy[m_nSCOPE_LINE4] = "..";
			saDummy[m_nCERT_VALIDITY_STATE] = "bCERTIFICATO NON VALIDO.";
			saDummy[m_nCERT_VALIDITY_ERROR] = "bCRL non accessibile, verificate le opzioni.";
			saDummy[m_nCERT_VALIDITY_VERIFICATION_CONDITIONS] = ".";
			saDummy[m_nDATE_VALID_FROM] = "bValido dal 10/04/2006 16:06:04";
			saDummy[m_nDATE_VALID_TO] = "bal 10/04/2009 16:06:04";
					
			setCertString(TreeNodeType.CERTIFICATE, saDummy);
		}
		{
			String[] saDummy = {
					"dnQualifier=2006111605A1528\nSN="+aUserName.toUpperCase()+
					"\ngivenName="+aUserSurname.toUpperCase()+
					"\nE="+aUserName.toLowerCase()+
					"."+aUserSurname.toLowerCase()+
					"@acme.it\nserialNumber=GCMVRD01D12210Y\nCN=\"GCMVRD01D12210Y/12200910401149623.fmIW78mgkUVdmdQuXCrZbDsW9oQ=\"\nOU=UNKNOWN DI UNKNOWN\nO=Non Dichiarato\nC=IT",
				};
			removeCertString(TreeNodeType.SUBJECT);
			setCertString(TreeNodeDescriptor.TreeNodeType.SUBJECT,saDummy);
		}
	}
}
