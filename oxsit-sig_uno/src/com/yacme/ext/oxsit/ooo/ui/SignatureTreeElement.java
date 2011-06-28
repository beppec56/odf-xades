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

package com.yacme.ext.oxsit.ooo.ui;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import com.yacme.ext.oxsit.security.SignatureState;
import com.yacme.ext.oxsit.security.cert.CertificateState;

/** to hold the signature data, for a single signature
 * 
 * @author beppec56
 *
 */
public class SignatureTreeElement extends BaseCertificateTreeElement {
	
	//the signature UUID inside a document.
	private String	m_sSignatureUUID;

	protected final int m_nFIELD_SIGNATURE_STATE = 0;
	protected final int m_nFIELD_DOCUMENT_VERF_STATE = 1;
	protected final int m_nFIELD_DOCUMENT_VERF_CONDT = 2;
	protected final int m_nFIELD_TITLE_SIGNED_BY = 3;

	public final int m_nFIELD_TITLE_DATE_SIGN				= 10;
	public final int m_nFIELD_DATE_SIGN						= 11;
	public final int m_nFIELD_DATE_SIGN_CONDT_L1			= 12;
	public final int m_nFIELD_DATE_SIGN_CONDT_L2			= 13;

	//hash tables to convert the enum of various states to the string IDs in resources
	/* the mapping from strings to emun state is:
	 * 1) the enum is edited/changed in the IDL file
	 * 2) the corresponding string plus id is added to the localization file
	 * 3) change this hashmap to connect the enum to the id of the string
	 * 
	 *   the code will take care of the rest
	 */
	public static Hashtable<SignatureState,String>	m_aSIGNATURE_STATE = new Hashtable< SignatureState , String>(15);
	static {
		m_aSIGNATURE_STATE.put(SignatureState.OK, "err_txt_sign_ok");
		m_aSIGNATURE_STATE.put(SignatureState.NOT_YET_VERIFIED, "err_txt_sign_to_ver");
		m_aSIGNATURE_STATE.put(SignatureState.NOT_VALID, "err_txt_sign_not_val");
		//FIXME: add the strings !
		m_aSIGNATURE_STATE.put(SignatureState.ERR_DIGEST_COMPARE, "err_txt_sign_err_dig_comp");
		m_aSIGNATURE_STATE.put(SignatureState.ERR_DATA_FILE_NOT_SIGNED, "err_txt_sign_err_data_file_not_signed");
		m_aSIGNATURE_STATE.put(SignatureState.ERR_SIG_PROP_NOT_SIGNED, "err_txt_sign_err_sig_prop_not_signed");
//		m_aSIGNATURE_STATE.put(SignatureState., "err_txt_sign_err_sig_prop_verif");
		m_aSIGNATURE_STATE.put(SignatureState.ERR_CERT_EXPIRED, "err_txt_sign_err_sig_prop_cert_exp");
		m_aSIGNATURE_STATE.put(SignatureState.ERR_TIMESTAMP_VERIFY, "err_txt_sign_err_sig_prop_time_stamp_verf");
	};
	//hash table to convert the enum of the certificate state to the actual strings in resources
	public static Hashtable<SignatureState,String>	m_aSIGNATURE_STATE_STRINGS;

	private	CertificateTreeElement	m_aChildCertificate;

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
		
		initStaticSignatureStaticStrings();
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

	/**
	 * 
	 */
	private void initStaticSignatureStaticStrings() {
		if(m_aSIGNATURE_STATE_STRINGS == null) {
			//init it once per element
			m_aSIGNATURE_STATE_STRINGS = new Hashtable<SignatureState, String>(10);
			if(m_aRegAcc == null)
				m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
			if(m_aRegAcc != null) {
				Set<SignatureState> aKeySet = m_aSIGNATURE_STATE.keySet();
				Iterator<SignatureState> it = aKeySet.iterator();
		        while (it.hasNext()) {
					try {
						SignatureState cs = it.next();
						m_aSIGNATURE_STATE_STRINGS.put(cs, 
								m_aRegAcc.getStringFromRegistry(
										m_aSIGNATURE_STATE.get(cs)
															   )
													   );
					} catch (com.sun.star.uno.Exception e) {
						getLogger().severe(e);
					}
		        }
			}
		}		
	}
	
	public void updateSignaturesStates() {
		if(get_xSignatureState() != null) {
			setSignatureState(get_xSignatureState().getState().getValue()); //grab the signature state from the signature verifier
//			setDocumentVerificationState(0);
//			setSignatureAndDocumentStateConditions(0);
			
//			setCertificateGraficStateValue(getCertificate().getCertificateState());
//			setCertificateState(getCertificate().getCertificateState());
//			setCertificateStateConditions(getCertificate().getCertificateStateConditions());
//			setCertificationAutorityState(getCertificate().getCertificationAuthorityState());			
		}
	}

	public void updateSignatureStrings() {
		//initializes string for signature and document state
			//grab the string for certificate status
			m_sStringList[m_nFIELD_SIGNATURE_STATE] =
							m_aSIGNATURE_STATE_STRINGS.get(
									SignatureState.fromInt(getCertificateState())
											);
	}

	/**
	 * @param _sSignatureUUID the m_s_SignatureUUID to set
	 */
	public void setSignatureUUID(String _sSignatureUUID) {
		this.m_sSignatureUUID = _sSignatureUUID;
	}

	/**
	 * @return the m_s_SignatureUUID
	 */
	public String getSignatureUUID() {
		return m_sSignatureUUID;
	}

	/**
	 * @param m_aChildCertificate the m_aChildCertificate to set
	 */
	public void setChildCertificate(CertificateTreeElement m_aChildCertificate) {
		this.m_aChildCertificate = m_aChildCertificate;
	}

	/**
	 * @return the m_aChildCertificate
	 */
	public CertificateTreeElement getChildCertificate() {
		return m_aChildCertificate;
	}
}
