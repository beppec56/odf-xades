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

package it.plio.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.TreeNodeType;

/**
 * @author beppe
 *
 */
public class CertificateDataCA extends SignatureStateInDocument {

	/**
	 * @param user
	 */
	public CertificateDataCA() {
		super("The Certification Authority");
		String sUserN = "Giacomo";
		String sUserC = "Verdi";
		// TODO Auto-generated constructor stub
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{
			String[] asArray = new String[20];

			int i = 0;
			asArray[i++] = new String("bLa firma è valida, firmato da:");
			asArray[i++] = new String("r"+sUserN+" "+sUserC);
			asArray[i++] = new String("bL'identità del firmatario è valida");
			asArray[i++] = new String("");
			asArray[i++] = new String("rFirma qualificata valida");
			asArray[i++] = new String("");
			asArray[i++] = new String("rIl documento non è stato modificato");
			asArray[i++] = new String("rdopo la firma");
			asArray[i++] = new String("");
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
					"CN=The Certification Authority\nOU=The Folks\nserialNumber=02313821007\nO=The Wizards\nC=IT"
					};
			setCertString(TreeNodeType.ISSUER,saDummy);
		}
		{
			String[] saDummy = {
					"10/11/2005 16:06:04",
			};
			removeCertString(TreeNodeType.VALID_FROM);
			setCertString(TreeNodeDescriptor.TreeNodeType.VALID_FROM,saDummy);
		}
		{
			String[] saDummy = {
					"10/11/2015 16:06:04",
			};
			removeCertString(TreeNodeType.VALID_TO);
			setCertString(TreeNodeDescriptor.TreeNodeType.VALID_TO,saDummy);
		}
		{
			String[] saDummy = {
					"CN=\"The Certification Authority\"\nOU=UNKNOWN DI UNKNOWN\nO=The Certification Wizards\nC=IT",
			};
			removeCertString(TreeNodeType.SUBJECT);
			setCertString(TreeNodeDescriptor.TreeNodeType.SUBJECT,saDummy);
		}
		
	}

	@Override
	public boolean isCertificateValid() {
		return true;
	}
}
