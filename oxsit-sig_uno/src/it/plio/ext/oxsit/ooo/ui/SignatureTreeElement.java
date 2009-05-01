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

import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/** to hold the signature data, for a single signature
 * 
 * @author beppec56
 *
 */
public class SignatureTreeElement extends CertificateTreeElementBase {
	protected final int m_nFIELD_SIGNATURE_STATE = 0;
	protected final int m_nFIELD_DOCUMENT_VERF_STATE = 1;
	protected final int m_nFIELD_TITLE_SIGNED_BY = 2;

	
	
	/**
	 * @param context
	 * @param _xmcf
	 */
	public SignatureTreeElement(XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(context, _xmcf);
		// TODO Auto-generated constructor stub
		setNodeType(TreeNodeType.SIGNATURE);
		setSignatureState(0);
//set the position of common nodes, different in signature display
		m_nFIELD_OWNER_NAME 					= 3;
		m_nFIELD_CERTIFICATE_STATE				= 4;
		m_nFIELD_CERTIFICATE_VERF_CONDITIONS	= 5;
		m_nFIELD_TITLE_ISSUER	 				= 6;
		m_nFIELD_ISSUER 						= 7;
		m_nFIELD_ISSUER_VERF_CONDITIONS			= 8;
	}

	public void initialize() {

//init common elements
		setNodeName("the signee name (GN+SN)");
		super.initialize();
//adds specif elements
		m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
		// allocate string needed for display
		// these are the string common to both the signature and the 
		// certificate
		try {

			//initializes fixed string (titles)
			m_sStringList[m_nFIELD_TITLE_SIGNED_BY] = m_aRegAcc.getStringFromRegistry("sign_title_signed_by");

		} catch (Exception e) {
			getLogger().severe("initialize", e);
		}
		m_aRegAcc.dispose();
	}
}
