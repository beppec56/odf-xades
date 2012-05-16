/* ***** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/ConstantCustomIT.java.
 *
 * The Initial Developer of the Original Code is
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.cust_it;

import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 * this class contains the global variables needed by all this implemenatation
 * 
 * @author beppe
 * 
 */
public class ConstantCustomIT {
	
	/// base of configuration item
	public static final String m_sWEBIDENTBASE = "com.yacme.ext"; // extension owner, used in building it,
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

	// service to verify the document signatures present
	public static final String m_sDOCUMENT_VERIFIER_SERVICE = m_sWEBIDENTBASE + ".oxsit.cust_it.security.DocumentSignaturesVerifier_IT";

	// service to hold all the information available from SSCD devices available on system.
	public static final String m_sAVAILABLE_SSCD_SERVICE = m_sWEBIDENTBASE + ".oxsit.cust_it.security.AvailableSSCDs_IT";

	// service to hold all the information available for a single SSCD device available on system.
	public static final String m_sSSCD_SERVICE = m_sWEBIDENTBASE + ".oxsit.cust_it.security.SSCDevice";

	// service to implement the interfaces needed to hold the signature state after verification
	public static final String m_sSIGNATURE_STATE_SERVICE_IT = m_sWEBIDENTBASE + ".oxsit.cust_it.security.SignatureState_IT";
	
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
	
	
	//FIXME there will be a European site which will hold the distribution points for certificate lists of other
	//EU states, see here:
	//http://ec.europa.eu/information_society/policy/esignature/eu_legislation/trusted_lists/index_en.htm
	//for details
	//
	//name of the italian CA file (current version)
	//FIXME: this solution should be changed, for example checking if a new one is available
	//and downloading it in the user cache
	//IMPORTANT: if this is changed, the file oxsit-l10n/localization-master.ods
	//MUST be updated as well, on cell E154
	//after that a new message list should be generated
	//update the build.xml of the oxsit-ext_conf as well
	public static final String m_sCA_LIST_SIGNED_FILE = "LISTACER_20120404.zip.p7m";
	public static final String m_sCA_CNIPA_ROOT = "DigitPA.cer";
	
	public static final String m_sSignatureStorageName = "META-INF";
	public static final String m_sSignatureFileName = "xadessignatures.xml";
}
