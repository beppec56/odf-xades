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

package it.plio.ext.oxsit.ooo;

import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;

/**
 * this class contains the global variables needed by all this implemenatation
 * 
 * @author beppe
 * 
 */
public class GlobConstant {
	/**
	 * this boolean field indicates from where the program flow originates.
	 * false if from inside extension (e.g. called from ProtocolHandler service
	 * implementation) true if called from class MainStandAloneDebug the idea is
	 * to use this flag as a kind of software multiplex to use the
	 * implementation from net (true) or from the handler (false)
	 */
	public static boolean		m_bCalledOutsideExtention				= false;
	
	/// base of configuration item
	public static final String m_sWEBIDENTBASE = "it.plio.ext"; // extension owner, used in building it,
																// same name, same meaning in extension_conf_files/build.xml
	public static final String m_sEXT_NAME ="oxsit"; //name of the extension, used in building it,
																		// same name, same meaning in extension_conf_files/build.xml
	public static final String m_sEXTENSION_IDENTIFIER = m_sWEBIDENTBASE+"."+m_sEXT_NAME;
	public static final String m_sEXTENSION_BASE_KEY = "/"+m_sWEBIDENTBASE+"."+ m_sEXT_NAME;
	public static final String m_sEXTENSION_CONF_BASE_KEY = m_sEXTENSION_BASE_KEY+".Configuration/";
	public static final String m_sEXTENSION_CONF_FRAME_KEY = m_sEXTENSION_CONF_BASE_KEY	+ "Frames/";
	public static final String m_sEXTENSION_CONF_OPTIONS = m_sEXTENSION_CONF_BASE_KEY	+ "SignatureOptionsParameters/";
	public static final String m_sEXTENSION_CONF_FRAME_ID = "Fuhc";
	public static final String m_sOFFICE_ADDONS_BASE_CONF ="/org.openoffice.Office.Addons/";
	public static final String m_sTOOLBAR_CONF_BASE_KEY = m_sOFFICE_ADDONS_BASE_CONF+"AddonUI/OfficeToolBar";
	public static final String m_sEXTENSION_TOOLBAR_CONF_BASE_KEY = m_sTOOLBAR_CONF_BASE_KEY+m_sEXTENSION_BASE_KEY+".OfficeToolBar/";
	
	/**
	 * the following constant are used in ProtocolHandler.xcu(.xml) and
	 * Addon.xcu(.xml) in the extension pay attention before modifiying !
	 */
	public static final String	m_sSIGN_PROTOCOL_BASE_URL				= m_sWEBIDENTBASE + ".oxsit.comp.SignatureHandler:";
	//object to sign for menu item
	public static final String	m_sSIGN_DIALOG_PATH						= "SignDialog";
	public static final String	m_sSIGN_DIALOG_PATH_COMPLETE			= m_sSIGN_PROTOCOL_BASE_URL+m_sSIGN_DIALOG_PATH;
	// specific object for extended toolbar
	public static final String	m_sSIGN_DIALOG_PATH_TB					= "SignDialogTB";
	public static final String	m_sSIGN_DIALOG_PATH_TB_COMPLETE			= m_sSIGN_PROTOCOL_BASE_URL+m_sSIGN_DIALOG_PATH_TB;
	public static final String	m_sON_HELP_ABOUT_PATH					= "HelpAbout";
	public static final String	m_sON_HELP_ABOUT_PATH_COMPLETE			= m_sSIGN_PROTOCOL_BASE_URL+m_sON_HELP_ABOUT_PATH;

	/** end of ProtocolHandler.xcu(.xml) Addon.xcu(.xml) constants */
	// constant for UNO dispatch URL (intercepted url)
	public static final String	m_sUNO_PROTOCOL								= ".uno:";
	public static final String m_sUNO_SAVE_URL_COMPLETE 					= ".uno:Save";
	public static final String m_sUNO_SSAVE_AS_URL_COMPLETE 				= ".uno:SaveAs";
	public static final String	m_sUNO_SIGNATURE_URL_COMPLETE				= ".uno:Signature";
	public static final String	m_sUNO_MACRO_SIGNATURE_URL_COMPLETE			= ".uno:MacroSignature";

	// these come from sfx2/inc/sfx2/signaturestate.hxx
	public static final int		m_nSIGNATURESTATE_UNKNOWN					= -1;
	public static final int		m_nSIGNATURESTATE_NOSIGNATURES				= 0;
	public static final int		m_nSIGNATURESTATE_SIGNATURES_OK				= 1;
	/**
	 *  signature is OK, but certificate could not be validated
	 */
	public static final int		m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED	= 2;	
	public static final int		m_nSIGNATURESTATE_SIGNATURES_BROKEN			= 3;
	/** State was SIGNATURES_OK, but doc is modified now
	 * NOTE: this doesn't seem to be implemented in OOo as of 2.4. The
	 * behavior is different: when the document is modified, the signatures
	 * status disappears.
	 */
	public static final int		m_nSIGNATURESTATE_SIGNATURES_INVALID		= 4;

	//names of icons used to display state of certificate/certificate elements

	public static final String	m_sSSCD_ELEMENT = "sscd-device";

	public static final String	m_nCERTIFICATE = "certificato";
	public static final String	m_nCERTIFICATE_CHECKED_OK = "certificato-ok";
	public static final String	m_nCERTIFICATE_CHECKED_WARNING = "certificato-warning";
	public static final String	m_nCERTIFICATE_CHECKED_INVALID = "certificato-err";
	public static final String	m_nCERTIFICATE_CHECKED_BROKEN = "certificato-rotto"; //TODO check alternative
	public static final String	m_nCERTIFICATE_CHECKED_BROKEN2 = "certificato-rotto-2"; //TODO
	public static final String	m_nCERTIFICATE_CHECKED_UNKNOWN = "certificato-interrogativo";
	public static final String	m_nCERTIFICATE_REMOVING = "certificato-remove";
	public static final String	m_nCERTIFICATE_ADDING = "certificato-add";
	//state of single elements, when needed
	public static final	String	m_nCERT_ELEM_OK = "check_ok";
	public static final	String	m_nCERT_ELEM_WARNING = "warning";
	public static final	String	m_nCERT_ELEM_INVALID = "errore";
	public static final	String	m_nCERT_ELEM_BROKEN = "rotto";
	public static final	String	m_nCERT_ELEM_BROKEN2 = "rotto-2";

	public static final String	m_sXADES_SIGNATURE_STREAM_NAME					= "xadessignatures.xml";
	
	// Names used in configuration, the names are defined in file
	// extension_conf_files/extension/AddonConfiguration.xcs.xml
	//// logging configuration
	public static final String	m_sENABLE_INFO_LEVEL 							= "EnableInfoLevel";// boolean
	public static final String	m_sENABLE_WARNING_LEVEL 						= "EnableWarningLevel";// boolean
	public static final String	m_sENABLE_CONSOLE_OUTPUT 						= "EnableConsoleOutput";// boolean
	public static final String	m_sENABLE_FILE_OUTPUT 							= "EnableFileOutput";// boolean
	public static final String	m_sLOG_FILE_PATH 								= "LogFilePath";// string
	public static final String	m_sFILE_ROTATION_COUNT 							= "FileRotationCount";// int
	public static final String	m_sMAX_FILE_SIZE	 							= "MaxFileSize";// int

	//// for singleton and XLogger
	public static final String m_sSINGLETON_SERVICE = m_sWEBIDENTBASE + ".oxsit.singleton.SingleGlobalVariables";
	public static final String m_sSINGLETON_SERVICE_INSTANCE = "/singletons/"+m_sSINGLETON_SERVICE;
	
	//// for XLogger (still to be implemented
	public static final String m_sSINGLETON_LOGGER_SERVICE = m_sWEBIDENTBASE + ".oxsit.singleton.GlobalLogger";
	public static final String m_sSINGLETON_LOGGER_SERVICE_INSTANCE = "/singletons/"+m_sSINGLETON_LOGGER_SERVICE;
		
	public static final int	m_nLOG_LEVEL_FINE									= 0;
	public static final int	m_nLOG_LEVEL_INFO									= 1;
	public static final int	m_nLOG_LEVEL_WARNING								= 2;
	public static final int	m_nLOG_LEVEL_SEVERE									= 3;
	
	//service for document signatures, this service implements specific interfaces, not available in
	// standard OOo, with functionality similar to some stock OOo interfaces declared unpublished
	public static final String m_sDOCUMENT_SIGNATURES_SERVICE = m_sWEBIDENTBASE + ".oxsit.ooo.cert.DocumentSignatures";

	// service to hold all the information available from SSCD devices available on system.
	public static final String m_sAVAILABLE_SSCD_SERVICE = m_sWEBIDENTBASE + ".oxsit.security.AvailableSSCDs";

	// service to hold all the information available for a single SSCD device available on system.
	public static final String m_sSSCD_SERVICE = m_sWEBIDENTBASE + ".oxsit.security.SSCDevice";
	
	//service to hold a single certificate
	public static final String m_sX509_CERTIFICATE_SERVICE = m_sWEBIDENTBASE + ".oxsit.security.cert.X509Certificate";
	//names used to exchange extensions state
	public static final String m_sX509_CERTIFICATE_VERSION = "Version";		
	public static final String m_sX509_CERTIFICATE_ISSUER = "IssuerName";
	public static final String m_sX509_CERTIFICATE_NOT_BEFORE = "NotValidBefore";
	public static final String m_sX509_CERTIFICATE_NOT_AFTER = "NotValidAfter";	
	public static final String m_sX509_CERTIFICATE_CEXT = "CritExt";	
	public static final String m_sX509_CERTIFICATE_NCEXT = "NotCritExt";	
	public static final String m_sX509_CERTIFICATE_CERTPATH = "CertifPath";	
	
	//service to hold a single certificate extension
	public static final String m_sCERTIFICATE_EXTENSION_SERVICE = m_sWEBIDENTBASE + ".oxsit.security.cert.CertificateExtension";
	
	//service to hold a certificate compliance checker, for user certificate
	public static final String m_sCERTIFICATE_COMPLIANCE_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.security.cert.CertificateComplianceIT";
	//service to hold a certification path checker 
	public static final String m_sCERTIFICATION_PATH_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.security.cert.CertificationPathIT";
	//service to hold a certification revocation state checker 
	public static final String m_sCERTIFICATE_REVOCATION_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.security.cert.CertificationRevocationIT";
	//service to hold a certification path checker cache 
	public static final String m_sCERTIFICATION_PATH_CACHE_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.security.cert.CertificationPathCacheIT";
	
	//service to implement a dispatch interceptor
	public static final String m_sDISPATCH_INTERCEPTOR_SERVICE = m_sWEBIDENTBASE + ".oxsit.DipatchIntercept";
	
	
	public static final String m_sPKCS11_WRAPPER_NATIVE = PKCS11Implementation.getPKCS11_WRAPPER();
	
	public static final String m_sPCSC_WRAPPER_NATIVE = "OCFPCSC1";
}
