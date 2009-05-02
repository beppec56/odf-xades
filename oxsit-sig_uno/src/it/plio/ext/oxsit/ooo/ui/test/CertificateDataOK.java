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

import it.plio.ext.oxsit.ooo.ui.test.TreeNodeDescriptor.TreeNodeType;

public class CertificateDataOK extends CertificateData {

	public CertificateDataOK(String aUserName, String aUserSurname) {
		super(); // user name will disappear in finished code 
		// TODO Auto-generated constructor stub
//fill in the custom data
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
