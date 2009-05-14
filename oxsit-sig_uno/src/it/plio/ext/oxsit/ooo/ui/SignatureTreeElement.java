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
public class SignatureTreeElement extends BaseCertificateTreeElement {
	protected final int m_nFIELD_SIGNATURE_STATE = 0;
	protected final int m_nFIELD_DOCUMENT_VERF_STATE = 1;
	protected final int m_nFIELD_DOCUMENT_VERF_CONDT = 2;
	protected final int m_nFIELD_TITLE_SIGNED_BY = 3;

	public final int m_nFIELD_TITLE_DATE_SIGN				= 10;
	public final int m_nFIELD_DATE_SIGN						= 11;
	public final int m_nFIELD_DATE_SIGN_CONDT_L1			= 12;
	public final int m_nFIELD_DATE_SIGN_CONDT_L2			= 13;

	
	/**
	 * @param context
	 * @param _xmcf
	 */
	public SignatureTreeElement(XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(context, _xmcf);
		setNodeType(TreeNodeType.SIGNATURE);
		setSignatureState(m_nSIGNATURE_STATE_TO_BE_VERIFIED);
		setDocumentVerificationState(m_nDOCUMENT_VERIF_TO_BE_VERIFIED);
		setSignatureAndDocumentStateConditions(m_nDOCUMENT_VERIF_STATE_CONDT_DISAB);
		setSignatureDateMode(m_nDOCUMENT_SIGN_DATE_MANUAL);

//set the position of common nodes, different in signature display
		m_nFIELD_OWNER_NAME 					= 4;
		m_nFIELD_CERTIFICATE_STATE				= 5;
		m_nFIELD_CERTIFICATE_VERF_CONDITIONS	= 6;
		m_nFIELD_TITLE_ISSUER	 				= 7;
		m_nFIELD_ISSUER 						= 8;
		m_nFIELD_ISSUER_VERF_CONDITIONS			= 9;
	}

	public void initialize() {
//init common elements
		setNodeName("rthe signee name (GN+SN)");
		super.initialize();
//adds specif elements
		m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
		// allocate string needed for display
		// these are the string common to both the signature and the 
		// certificate
		try {
			//initializes fixed string (titles)
			m_sStringList[m_nFIELD_TITLE_SIGNED_BY] = m_aRegAcc.getStringFromRegistry("sign_title_signed_by");
			m_sStringList[m_nFIELD_TITLE_DATE_SIGN] = m_aRegAcc.getStringFromRegistry("sign_title_date");

			//initializes string for signature and document state
			m_sStringList[m_nFIELD_SIGNATURE_STATE] = 
				m_aRegAcc.getStringFromRegistry( m_sSIGNATURE_STATE[getSignatureState()]);

			m_sStringList[m_nFIELD_DOCUMENT_VERF_STATE] =
				m_aRegAcc.getStringFromRegistry( m_sDOCUMENT_VERIF_STATE[getDocumentVerificationState()]);
			//set the string for document and signature verification condt
			m_sStringList[m_nFIELD_DOCUMENT_VERF_CONDT] =
				m_aRegAcc.getStringFromRegistry( m_sDOCUMENT_VERIF_STATE_CONDT[getSignatureAndDocumentStateConditions()]);

			// set the strings for the signature date conditions 
			m_sStringList[m_nFIELD_DATE_SIGN] = "r<una data di firma>";
			int sigMode = getSignatureDateMode();
			m_sStringList[m_nFIELD_DATE_SIGN_CONDT_L1] =
				m_aRegAcc.getStringFromRegistry( m_sDOCUMENT_SIGN_DATE_L1[sigMode]);
				
			m_sStringList[m_nFIELD_DATE_SIGN_CONDT_L2] =
				m_aRegAcc.getStringFromRegistry( m_sDOCUMENT_SIGN_DATE_L2[sigMode]);

		} catch (Exception e) {
			getLogger().severe("initialize", e);
		}
		m_aRegAcc.dispose();
	}
}
