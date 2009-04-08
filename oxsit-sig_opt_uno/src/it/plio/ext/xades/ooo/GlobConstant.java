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

package it.plio.ext.xades.ooo;

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
	public static final String m_sEXT_NAME ="ooo_xades_sign_it"; //name of the extension, used in building it,
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
	public static final String	m_sSIGN_PROTOCOL_BASE_URL				= m_sWEBIDENTBASE + ".xades.comp.SignatureHandler:";
	//object to sign for menu item
	public static final String	m_sSIGN_DIALOG_PATH						= "SignDialog";
	// specific object for extended toolbar
	public static final String	m_sSIGN_DIALOG_PATH_TB					= "SignDialogTB";
	public static final String	m_sON_HELP_ABOUT_PATH					= "HelpAbout";

	/*
	 * public static final String m_sAfterLoadPath = "AfterLoad"; called when
	 * loaded terminated, to check signature and display error if signature is
	 * not valid May be better implemnt it in synch job, on the OnLoad command
	 */
	public static final String	m_sBEFORE_SAVE_PATH						= "BeforeSave";								// 
	public static final String	m_sBEFORE_SAVE_AS_PATH					= "BeforeSaveAs";
	public static final String	m_sSELECT_SIGN_PATH						= "SelectSignature";

	/** end of ProtocolHandler.xcu(.xml) Addon.xcu(.xml) constants */
	// / constant for uno dispatch URL
	public static final String	m_sUNO_SAVE_URL_COMPLETE					= ".uno:Save";
	public static final String	m_sUNO_SAVE_AS_URL_COMPLETE					= ".uno:SaveAs";
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
	
	public static final String	m_sXADES_SIGNATURE_STREAM_NAME					= "xades-it-signature.xml";
	
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
	public static final String m_sSINGLETON_SERVICE = "it.plio.ext.xades.singleton.GlobalVariables";
	public static final String m_sSINGLETON_SERVICE_INSTANCE = "/singletons/"+m_sSINGLETON_SERVICE;
	
	public static final int	m_nLOG_LEVEL_FINE									= 0;
	public static final int	m_nLOG_LEVEL_INFO									= 1;
	public static final int	m_nLOG_LEVEL_WARNING								= 2;
	public static final int	m_nLOG_LEVEL_SEVERE									= 3;	
}