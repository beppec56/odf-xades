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

/**
 * @author beppe
 *
 */
public class SignatureStateInDocumentKOCertSignature extends SignatureStateInDocument {

	/**
	 * @param sUserName TODO
	 * @param sUserSurname TODO
	 * @param user
	 */
	public SignatureStateInDocumentKOCertSignature(String sUserName, String sUserSurname, XComponentContext _Context, XMultiComponentFactory _xMCF) {
		super(sUserName+" "+sUserSurname, _Context, _xMCF);
// now personalize the certificate: some of the field and set it OK
// set the right certificate state string
		{
			MessageConfigurationAccess m_aRegAcc = new MessageConfigurationAccess(_Context, _xMCF);
			String[] asArray = getCertStrings(TreeNodeType.SIGNATURE);		
			//remove olfd data
			removeCertString(TreeNodeType.SIGNATURE);
			try {
				asArray[m_nSIGNATURE_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_sig_ko" );
				asArray[m_nDOCUMENT_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_docu_mod" );
				asArray[m_nSUBJECT] = new String("r"+sUserName+" "+sUserSurname);
				asArray[m_nTEXT_FIELD_04] = m_aRegAcc.getStringFromRegistry( "err_txt_cert_ok" );
				asArray[m_nTEXT_FIELD_05] = m_aRegAcc.getStringFromRegistry( "err_txt_crl_dis" );
				asArray[m_nTEXT_FIELD_08] = m_aRegAcc.getStringFromRegistry( "err_txt_ca_ko" );
				asArray[m_nTEXT_FIELD_10] = new String("r2008.10.02 10:23:34 Z");
				asArray[m_nTEXT_FIELD_12] = m_aRegAcc.getStringFromRegistry( "txt_times_serv2" ) + "2008.10.02 10:23:34 Z"; 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_aRegAcc.dispose();
			setCertString(TreeNodeType.SIGNATURE, asArray);
		}

//create a fake certificate, with wrong data
		m_aCert = new CertificateDataKONoCRL(sUserName,sUserSurname); 			
	}

	@Override
	public boolean isCertificateValid() {
		return false;
	}
}
