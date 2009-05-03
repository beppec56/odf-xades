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

import it.plio.ext.oxsit.logging.DynamicLogger;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;

/** The base class of the data element to be shown in the tree control.
 * 
 * From this class are subclassed the other specialized classes needed
 * to manage the single node of the tree.
 * 
 * @author beppec56
 *
 */
public abstract class TreeElement
		implements XComponent // XComponent implement for convenience, to be able to examine the interface
							// using UNO functions
		{

	/** enum to mark the type of the node.
	 * The node type is used in rendering the node on the right side
	 * of the tree control dialog.
	 * 
	 * @author beppec56
	 *
	 */
	public enum TreeNodeType {
		SIGNATURE, /* general state of the signature:
		state: valid/not valid
		short description of signee: name and Italian fiscal code
		a confirmation of identity check */		
		CERTIFICATE, /*	 */
		VERSION, /* */
		SERIAL_NUMBER, /* */
		ISSUER, /* */
		VALID_FROM, /* */
		VALID_TO, /* */
		SUBJECT,
		SUBJECT_ALGORITHM,
		PUBLIC_KEY,
		SIGNATURE_ALGORITHM,
		THUMBPRINT_SHA1,
		THUMBPRINT_MD5,
		EXTENSIONS_NON_CRITICAL,
			QC_STATEMENTS,
			AUTHORITY_INFORMATION_ACCESS,
			X509V3_CERTIFICATE_POLICIES,
			X509V3_SUBJECT_DIRECTORY_ATTRIBUTES,
			X509V3_ISSUER_ALTERNATIVE_NAME,
			X509V3_AUTHORITY_KEY_IDENTIFIER,
			X509V3_SUBJECT_KEY_IDENTIFIER,
			X509V3_CRL_DISTRIBUTION_POINTS,
		EXTENSIONS_CRITICAL,
			X509V3_KEY_USAGE,
		CERTIFICATION_PATH,
	};

	/**
	 * holds the parent element of this node, used in case of
	 * a node displaying for example the  VERSION type, in that
	 * case it will point to the real certificate node
	 */
	public TreeElement m_sParentElement; 

	/**
	 * constants for signature state
	 * 
	 */
	public static final int m_nSIGNATURE_STATE_VALID = 0;
	public static final int m_nSIGNATURE_STATE_NOT_VALID = 1;
	public static final int m_nSIGNATURE_STATE_TO_BE_VERIFIED = 2;
	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sSIGNATURE_STATE =  { 
							"err_txt_sign_ok",
							"err_txt_sign_not_val",
							"err_txt_sign_to_ver"
						};

	/**
	 * constants for document verification state
	 * 
	 */
	public static final int m_nDOCUMENT_VERIF_STATE_VALID = 0;
	public static final int m_nDOCUMENT_VERIF_MOD = 1;
	public static final int m_nDOCUMENT_VERIF_TO_BE_VERIFIED = 2;
	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sDOCUMENT_VERIF_STATE =  { 
							"err_txt_docum_ok",
							"err_txt_docum_mod",
							"err_txt_docum_to_ver"
						};

	/**
	 * constants for signature and document verification state conditions
	 * 
	 */
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_ENABLED = 0;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_NO_SIG_OPT = 1;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_NO_DOC_OPT = 2;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_DISAB = 3;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_NO_INET = 4;
	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sDOCUMENT_VERIF_STATE_CONDT =  { 
							"err_txt_docum_verf_condt_ok",
							"err_txt_docum_verf_condt_nosig",
							"err_txt_docum_verf_condt_nodocu",
							"err_txt_docum_verf_condt_disb",
							"err_txt_verf_condt_no_inet"
						};

	/**
	 * some constant for certificate validation state
	 */
	public static final int m_nCERTIFICATE_STATE_VALID = 0;
	public static final int m_nCERTIFICATE_STATE_EXPIRED = 1;
	public static final int m_nCERTIFICATE_STATE_REVOKED = 2;
	public static final int m_nCERTIFICATE_STATE_NOT_ACTIVE = 3;
	public static final int m_nCERTIFICATE_STATE_INCONSISTENT = 4;
	public static final int m_nCERTIFICATE_STATE_NOT_VERIFIED = 5;

	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sCERTIFICATE_STATE =  { 
							"err_txt_cert_ok",
							"err_txt_cert_exp",							
							"err_txt_cert_rev",
							"err_txt_cert_noact",
							"err_txt_cert_noconf",
							"err_txt_cert_nover"
						};

	/**
	 * some constant for certificate validation conditions
	 */
	public static final int m_nCERTIFICATE_STATE_CONDT_ENABLED = 0;
	public static final int m_nCERTIFICATE_STATE_CONDT_DISABLED = 1;
	public static final int m_nCERTIFICATE_STATE_CONDT_CRL_KO = 2;
	public static final int m_nCERTIFICATE_STATE_CONDT_NO_PATH_ROOT = 3;
	public static final int m_nCERTIFICATE_STATE_CONDT_NO_PATH_MIDDLE = 4;
	public static final int m_nCERTIFICATE_STATE_CONDT_NO_PATH = 5;
	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sCERTIFICATE_STATE_CONDT =  { 
									"err_txt_crl_ok",
									"err_txt_crl_dis",
									"err_txt_crl_cannot",
									"err_txt_crl_noroot",
									"err_txt_crl_noint",
									"err_txt_crl_noclo"
						};
	
	/**
	 * some constant for certificate validation state
	 */
	public static final int m_nISSUER_STATE_VALID = 0;
	public static final int m_nISSUER_STATE_NO_CTRL = 1;
	public static final int m_nISSUER_STATE_KO = 2;
	public static final int m_nISSUER_STATE_DB_NO_UPDT = 3;
	public static final int m_nISSUER_STATE_NO_THUMB = 4;
	public static final int m_nISSUER_STATE_DB_UPDT_KO = 5;
	public static final int m_nISSUER_STATE_DB_KO_CERT = 6;
	public static final int m_nISSUER_STATE_EXPIRED = 7;
	public static final int m_nISSUER_STATE_KO_EXPIRED = 8;
	public static final int m_nISSUER_STATE_UNKNOWN = 9;

	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sISSUER_STATE =  { 
							"err_txt_ca_ok",
							"err_txt_ca_noctrl",
							"err_txt_ca_ko",
							"err_txt_ca_stale",
							"err_txt_ca_nover",
							"err_txt_ca_noact",
							"err_txt_ca_noroot",
							"err_txt_ca_exp",
							"err_txt_ca_ko_exp",
							"err_txt_ca_unkn",
						};

	/**
	 * constants for document signature date mode 
	 * 
	 */
	public static final int m_nDOCUMENT_SIGN_DATE_TEMP_CERT = 0;
	public static final int m_nDOCUMENT_SIGN_DATE_PC = 1;
	public static final int m_nDOCUMENT_SIGN_DATE_MANUAL = 2;
	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sDOCUMENT_SIGN_DATE_L1 =  { 
							"sign_txt_l1_temp_cert",
							"sign_txt_l1_date_pc",
							"sign_txt_l1_date_manual"
						};
	public static final String[]  m_sDOCUMENT_SIGN_DATE_L2 =  { 
							"sign_txt_l2_temp_cert",
							"sign_txt_l2_date_pc",
							"sign_txt_l2_date_manual"
	};

	
	/**
	 * the node type for this node
	 */
	private TreeNodeType m_nType;
	private String	m_nNodeDescriptiveName;
	
	private DynamicLogger	m_aLogger;
	
	private	int				m_nCertificateState;
	private int				m_nCertificateStateConditions;
	
	private int				m_nIssuerState;

	private int				m_nSignatureState;
	private int				m_nSignatureAndDocumentStateConditions;
	
	private int				m_nDocumentVerificationState;
	
	private int				m_nSignatureDateMode;
	
	private String			m_sNodeGraphic;
	
	private XComponentContext		m_xCC;
	
	private XMultiComponentFactory	m_xMCF;
	
	/**
	 * @param m_nType the m_nType to set
	 */
	public void setNodeType(TreeNodeType m_nType) {
		this.m_nType = m_nType;
	}

	/**
	 * @return the m_nType
	 */
	public TreeNodeType getNodeType() {
		return m_nType;
	}

	/**
	 * @param m_aLogger the m_aLogger to set
	 */
	public void setLogger(DynamicLogger m_aLogger) {
		this.m_aLogger = m_aLogger;
	}

	/**
	 * @return the m_aLogger
	 */
	public DynamicLogger getLogger() {
		return m_aLogger;
	}
	
	abstract void initialize();

	/**
	 * @param m_nCertificateState the m_nCertificateState to set
	 */
	public void setCertificateState(int m_nCertificateState) {
		this.m_nCertificateState = m_nCertificateState;
	}

	/**
	 * @return the m_nCertificateState
	 */
	public int getCertificateState() {
		return m_nCertificateState;
	}

	/**
	 * @param m_nCertificateStateConditions the m_nCertificateStateConditions to set
	 */
	public void setCertificateStateConditions(
			int m_nCertificateVerificationConditions) {
		this.m_nCertificateStateConditions = m_nCertificateVerificationConditions;
	}

	/**
	 * @return the m_nCertificateStateConditions
	 */
	public int getCertificateStateConditions() {
		return m_nCertificateStateConditions;
	}

	/**
	 * @param m_xCC the m_xCC to set
	 */
	public void setComponentContext(XComponentContext m_xCC) {
		this.m_xCC = m_xCC;
	}

	/**
	 * @return the m_xCC
	 */
	public XComponentContext getComponentContext() {
		return m_xCC;
	}

	/**
	 * @param m_xMCF the m_xMCF to set
	 */
	public void setMultiComponentFactory(XMultiComponentFactory m_xMCF) {
		this.m_xMCF = m_xMCF;
	}

	/**
	 * @return the m_xMCF
	 */
	public XMultiComponentFactory getMultiComponentFactory() {
		return m_xMCF;
	}

	/**
	 * @param m_nIssuerState the m_nIssuerState to set
	 */
	public void setIssuerState(
			int m_nCertificationAuthorityState) {
		this.m_nIssuerState = m_nCertificationAuthorityState;
	}

	/**
	 * @return the m_nIssuerState
	 */
	public int getIssuerState() {
		return m_nIssuerState;
	}

	/**
	 * @param m_nSignatureState the m_nSignatureState to set
	 */
	public void setSignatureState(int m_nSignatureState) {
		this.m_nSignatureState = m_nSignatureState;
	}

	/**
	 * @return the m_nSignatureState
	 */
	public int getSignatureState() {
		return m_nSignatureState;
	}

	/**
	 * @param m_nSignatureAndDocumentStateConditions the m_nSignatureAndDocumentStateConditions to set
	 */
	public void setSignatureAndDocumentStateConditions(int m_nSignatureStateConditions) {
		this.m_nSignatureAndDocumentStateConditions = m_nSignatureStateConditions;
	}

	/**
	 * @return the m_nSignatureAndDocumentStateConditions
	 */
	public int getSignatureAndDocumentStateConditions() {
		return m_nSignatureAndDocumentStateConditions;
	}

	/**
	 * @param m_sNodeGraphic the m_sNodeGraphic to set
	 */
	public void setNodeGraphic(String m_sNodeGraphic) {
		this.m_sNodeGraphic = m_sNodeGraphic;
	}

	/**
	 * @return the m_sNodeGraphic
	 */
	public String getNodeGraphic() {
		return m_sNodeGraphic;
	}
	
	abstract void EnableDisplay(boolean bWhat);

	/**
	 * @param m_nNodeDescriptiveName the m_nNodeDescriptiveName to set
	 */
	public void setNodeName(String m_nNodeDescriptiveName) {
		this.m_nNodeDescriptiveName = m_nNodeDescriptiveName;
	}

	/**
	 * @return the m_nNodeDescriptiveName
	 */
	public String getNodeName() {
		return m_nNodeDescriptiveName;
	}

	/**
	 * @param m_nDocumentVerificationState the m_nDocumentVerificationState to set
	 */
	public void setDocumentVerificationState(int m_nDocumentVerificationState) {
		this.m_nDocumentVerificationState = m_nDocumentVerificationState;
	}

	/**
	 * @return the m_nDocumentVerificationState
	 */
	public int getDocumentVerificationState() {
		return m_nDocumentVerificationState;
	}

	/**
	 * @param m_nSignatureDateMode the m_nSignatureDateMode to set
	 */
	public void setSignatureDateMode(int m_nSignatureDateMode) {
		this.m_nSignatureDateMode = m_nSignatureDateMode;
	}

	/**
	 * @return the m_nSignatureDateMode
	 */
	public int getSignatureDateMode() {
		return m_nSignatureDateMode;
	}

}
