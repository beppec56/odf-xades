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

package it.plio.ext.oxsit.cust_it;

import it.plio.ext.oxsit.ooo.GlobConstant;

/**
 * this class contains the global variables needed by all this implemenatation
 * 
 * @author beppe
 * 
 */
public class ConstantCustomIT {
	
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
	public static final String m_sEXTENSION_CONF_SSCDS = m_sEXTENSION_CONF_BASE_KEY	+ "SSCDCollection";
	public static final String m_sEXTENSION_CONF_FRAME_ID = "Fuhc";
	public static final String m_sOFFICE_ADDONS_BASE_CONF ="/org.openoffice.Office.Addons/";
	public static final String m_sTOOLBAR_CONF_BASE_KEY = m_sOFFICE_ADDONS_BASE_CONF+"AddonUI/OfficeToolBar";
	public static final String m_sEXTENSION_TOOLBAR_CONF_BASE_KEY = m_sTOOLBAR_CONF_BASE_KEY+m_sEXTENSION_BASE_KEY+".OfficeToolBar/";

	public static final String m_sDOCUMENT_SIGNER_SERVICE = m_sWEBIDENTBASE + ".oxsit.ooo.cust_it.security.DocumentSigner_IT";

	// service to hold all the information available from SSCD devices available on system.
	public static final String m_sAVAILABLE_SSCD_SERVICE = m_sWEBIDENTBASE + ".oxsit.cust_it.security.AvailableSSCDs_IT";

	// service to hold all the information available for a single SSCD device available on system.
	public static final String m_sSSCD_SERVICE = m_sWEBIDENTBASE + ".oxsit.cust_it.security.SSCDevice";

	////////// the following UNO service names should go to the registry
	//services to display data of a certificate in a human readable way
	public static final String m_sX509_CERTIFICATE_DISPLAY_SERVICE_SUBJ_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.X509CertDisplaySubj_IT";

	public static final String m_sX509_CERTIFICATE_DISPLAY_SERVICE_CA_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.X509CertDisplayIssuer_IT";
	//service to hold a certificate compliance checker, for CA certificate
	public static final String m_sCERTIFICATE_COMPLIANCE_SERVICE_CA_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.CertificateComplianceCA_IT";
	//service to hold a certification path checker, for CA certificate
	//The italian version is the same UNO object
	public static final String m_sCERTIFICATE_PATH_SERVICE_CA_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.CertificateComplianceCA_IT";

	//////////////// services to check Italian user certificates (ussued to 'Natural Persons').
	//service to hold a certificate compliance checker, for user certificate
	public static final String m_sCERTIFICATE_COMPLIANCE_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.CertificateCompliance_IT";
	//service to hold a certification path checker 
	public static final String m_sCERTIFICATION_PATH_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.CertificationPath_IT";
	//service to hold a certification revocation state checker 
	public static final String m_sCERTIFICATE_REVOCATION_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.CertificateRevocation_IT";

	//service to hold a certification path checker cache moved to CertificationPath_IT class, specific of Italian implementation 
//	public static final String m_sCERTIFICATION_PATH_CACHE_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.security.cert.CertificationPathCache_IT";
	/////////// end of service to be mocved to registryS

	//service to hold a certification path checker cache, used for Italian custom implementation 
	public static final String m_sCERTIFICATION_PATH_CACHE_SERVICE_IT = GlobConstant.m_sWEBIDENTBASE + ".oxsit.cust_it.security.cert.CertificationPathCache_IT";	
	
	//service to implement a dispatch interceptor
	public static final String m_sDISPATCH_INTERCEPTOR_SERVICE = m_sWEBIDENTBASE + ".oxsit.DipatchIntercept";
	
	
	//name of the italian CA file (current version)
	//FIXME: this solution should be changed, for example checking if a new one is available
	//and downloading it in the user cache
	public static final String m_sCA_LIST_SIGNED_FILE = "LISTACER_20100311.zip.p7m";
}
