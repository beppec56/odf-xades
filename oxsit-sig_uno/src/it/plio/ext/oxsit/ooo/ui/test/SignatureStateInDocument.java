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

import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import java.util.HashMap;

import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/**
 * contains the certificate data, common for
 * some of the tree nodes.
 * In this implementation we have only some fake strings, not the
 * actual certificate data.
 * 
 * FIXME
 * WARNING: THIS CLASS IS TO BE REMOVED IN THE END.
 * 
 * @author beppe
 *
 */
public class SignatureStateInDocument {

	//=============================================
	// describes all the field for signature general status
	// obtained whe the user selects 
	public static final int m_nSIGNATURE_STATE 				= 0;
	public static final int m_nDOCUMENT_STATE 				= 1;
	public static final int m_nTITLE0 			= 2;
	public static final int m_nSUBJECT 				= 3;
	public static final int m_nTEXT_FIELD_04 		= 4;
	public static final int m_nTEXT_FIELD_05 		= 5;
	public static final int m_nTITLE1 			= 6;
	public static final int m_nISSUER 			= 7;
	public static final int m_nTEXT_FIELD_08 		= 8;
	public static final int m_nTITLE2 				= 9;
	public static final int m_nTEXT_FIELD_10 				= 10;
	public static final int m_nTEXT_FIELD_11	= 11;
	public static final int m_nTEXT_FIELD_12 				= 12;
	public static final int m_nTEXT_FIELD_13 				= 13;

	public static final int m_nMAXIMUM_FIELDS 				= 14;
//////////////////////////////////////////////////
	
	public final int m_nSIGNATURE_VALIDITY_VALID = 0;
	public final int m_nSIGNATURE_VALIDITY_NOT_VALID = 1;
	
	public final String[] m_sSignatureValidity = {
		"bLa firma è valida, firmato da:",
		"bLa firma NON È VALIDA, firmato da:"
	};
	
	public final int m_nSIGNEE_VALIDATION_VALID = 0;
	public final int m_nSIGNEE_VALIDATION_NOT_VALID = 1;
	
	public final String[] m_sSigneeValidity = {
		"bL'identità del firmatario è valida",
		"bL'identità del firmatario NON È VALIDA."
	};

	public final String m_sEmptyField = "r(this field is empty)";
	                    
	private String m_sUser; //name of the certificate holder
							//to simulate SN plus givenName fields on certificate
	// contains the certificate strings definitions
	private HashMap<TreeNodeDescriptor.TreeNodeType, String[]>	m_aSignatureDataStrings;

	public CertificateData m_aCert;

	/**
	 * this class will fill the certificate data with all the information needed
	 * @param _Context TODO
	 * @param _xMCF TODO
	 * 
	 * 
	 */
	public SignatureStateInDocument(String sUser, XComponentContext _Context, XMultiComponentFactory _xMCF) {
		// TODO Auto-generated constructor stub
		m_aSignatureDataStrings = new HashMap<TreeNodeDescriptor.TreeNodeType, String[]>(15);
		m_sUser = sUser;
		MessageConfigurationAccess m_aRegAcc = new MessageConfigurationAccess(_Context, _xMCF);

		// now init the certificate with dummy data
		// for every string meant for the multi fixed text display
		// devise a method to change the character font strike
		// the first character in the string marks the stroke type
		// (see function: it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.EnableDisplay(boolean) for
		// for information on how they are interpreted):
		// b	the string will be in bold
		// B
		// r
		// s    the string will striken out regular
		// S    the string will striken out bold
		// w	text background will be in orange4 (255,204,153) or orange2 (255,102,51)
		// 
		{
			String[] asArray = new String[m_nMAXIMUM_FIELDS];

			try {
				asArray[m_nSIGNATURE_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_sig_ok" );
				asArray[m_nDOCUMENT_STATE] = m_aRegAcc.getStringFromRegistry( "err_txt_docu_ok" );
				asArray[m_nTITLE0] = m_aRegAcc.getStringFromRegistry( "err_txt_title0" );
				asArray[m_nSUBJECT] = new String("r"+sUser);
				asArray[m_nTEXT_FIELD_04] = m_aRegAcc.getStringFromRegistry( "err_txt_cert_ok" );
				asArray[m_nTEXT_FIELD_05] = m_aRegAcc.getStringFromRegistry( "err_txt_crl_ok" );
				asArray[m_nTITLE1] = m_aRegAcc.getStringFromRegistry( "err_txt_title1" );
				asArray[m_nISSUER] = new String("rACME Authority Ltd");
				asArray[m_nTEXT_FIELD_08] = m_aRegAcc.getStringFromRegistry( "err_txt_ca_ok" );
				asArray[m_nTITLE2] = m_aRegAcc.getStringFromRegistry( "err_txt_title2" );
				asArray[m_nTEXT_FIELD_10] = new String("r2008.12.02 10:23:34 Z");
				asArray[m_nTEXT_FIELD_11] = m_aRegAcc.getStringFromRegistry( "txt_times_serv1" );
				asArray[m_nTEXT_FIELD_12] = m_aRegAcc.getStringFromRegistry( "txt_times_serv2" ) + "2008.12.02 10:23:34 Z"; 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			setCertString(TreeNodeType.SIGNATURE, asArray);
		}

		m_aRegAcc.dispose();	
		//create the standard user
		m_aCert = new CertificateData();
	}

	public void removeCertString(TreeNodeDescriptor.TreeNodeType aType){
		m_aSignatureDataStrings.remove(aType);
	}

	public String[] getCertStrings(TreeNodeDescriptor.TreeNodeType aType) {
		return m_aSignatureDataStrings.get(aType);
	}

	public void setCertString(TreeNodeDescriptor.TreeNodeType aType, String[] aStringArr) {
		m_aSignatureDataStrings.put(aType, aStringArr);
	}

	public String getUser() {
		// TODO Auto-generated method stub
		return m_sUser;
	}
	public boolean isCertificateValid() {
		return true;
	}	
}
