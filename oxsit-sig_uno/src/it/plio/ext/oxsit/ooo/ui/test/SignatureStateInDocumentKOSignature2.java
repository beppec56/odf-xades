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
import it.plio.ext.oxsit.ooo.ui.SignatureStateInDocument;
import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor;
import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.TreeNodeType;

public class SignatureStateInDocumentKOSignature2 extends SignatureStateInDocument {

	public SignatureStateInDocumentKOSignature2( XComponentContext _Context, XMultiComponentFactory _xMCF) {
		super("Gianni Rossi", _Context, _xMCF);
		String sUserN = "Gianni";
		String sUserC = "Rossi";
		// TODO Auto-generated constructor stub
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{			
			MessageConfigurationAccess m_aRegAcc = new MessageConfigurationAccess(_Context, _xMCF);
			String[] asArray = getCertStrings(TreeNodeType.SIGNATURE);		
			//remove olfd data
			removeCertString(TreeNodeType.SIGNATURE);
			try {
				asArray[m_nSIGNATURE_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_sig_to_ver" );
				asArray[m_nDOCUMENT_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_docu_mod" );
				asArray[m_nSUBJECT] = new String("r"+sUserN+" "+sUserC);
				asArray[m_nTEXT_FIELD_04] = m_aRegAcc.getStringFromRegistry( "err_txt_cert_exp" );
				asArray[m_nTEXT_FIELD_05] = m_aRegAcc.getStringFromRegistry( "err_txt_crl_dis" );
				asArray[m_nTEXT_FIELD_08] = m_aRegAcc.getStringFromRegistry( "err_txt_ca_noctrl" );
				asArray[m_nTEXT_FIELD_10] = new String("r2008.10.12 10:23:34 Z");
				asArray[m_nTEXT_FIELD_11] = m_aRegAcc.getStringFromRegistry( "txt_time_pc1" ); 
				asArray[m_nTEXT_FIELD_12] = m_aRegAcc.getStringFromRegistry( "txt_time_pc2" ); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_aRegAcc.dispose();
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
	}

	@Override
	public boolean isCertificateValid() {
		return true;
	}

}
