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

package it.plio.ext.xades.ooo.ui;

import it.plio.ext.xades.ooo.ui.TreeNodeDescriptor.TreeNodeType;

import java.util.HashMap;

import com.sun.star.lang.XEventListener;

/**
 * contains the certificate data, common for
 * some of the tree nodes.
 * In this implementation we have only some fake strings, not the
 * actual certificate data.
 * 
 * @author beppe
 *
 */
public class SignatureStateInDocument {

	//=============================================
	// describes all the field for signature general status
	// obtained whe the user selects 
	public static final int m_nSIGNATURE_VALIDITY 			= 0;
	public static final int m_nSIGNEE_NAME 					= 1;
	public static final int m_nSIGNEE_VALIDATION 			= 2;
	public static final int m_nEMPTY_FIELD_03 				= 3;
	public static final int m_nCERT_VALIDITY_STATE1 		= 4;
	public static final int m_nCERT_VALIDITY_STATE2 		= 5;
	public static final int m_nDOCUMENT_VALIDITY1 			= 6;
	public static final int m_nDOCUMENT_VALIDITY2 			= 7;
	public static final int m_nSIGNATURE_DATE_TITLE 		= 8;
	public static final int m_nSIGNATURE_DATE 				= 9;
	public static final int m_nEMPTY_FIELD_10 				= 10;
	public static final int m_nSIGNATURE_DATE_METHOD_TITLE	= 11;
	public static final int m_nSIGNATURE_DATE_METHOD_DATE 	= 12;
	public static final int m_nEMPTY_FIELD_13 				= 13;

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
	 * 
	 * 
	 */
	public SignatureStateInDocument(String sUser) {
		// TODO Auto-generated constructor stub
		m_aSignatureDataStrings = new HashMap<TreeNodeDescriptor.TreeNodeType, String[]>(15);
		m_sUser = sUser;
		// now init the certificate with dummy data
		// for every string meant for the multi fixed text display
		// devise a method to change the character font strike
		// the first character in the string marks the stroke type
		// (see function: it.plio.ext.xades.ooo.ui.TreeNodeDescriptor.EnableDisplay(boolean) for
		// for information on how they are interpreted):
		// b	the string will be in bold
		// B
		// r
		// s    the string will striken out regular
		// S    the string will striken out bold
		// 
		{
			String[] asArray = new String[m_nMAXIMUM_FIELDS];

			asArray[m_nSIGNATURE_VALIDITY] = new String(m_sSignatureValidity[m_nSIGNATURE_VALIDITY_VALID]);
			asArray[m_nSIGNEE_NAME] = new String("r"+sUser);
			asArray[m_nSIGNEE_VALIDATION] = new String(m_sSigneeValidity[m_nSIGNEE_VALIDATION_VALID]);
			asArray[m_nEMPTY_FIELD_03] = new String("");
			asArray[m_nCERT_VALIDITY_STATE1] = new String("bCertificato di firma non verificato");
			asArray[m_nCERT_VALIDITY_STATE2] = new String("bControllo certificato con CRL non abilitato");
			asArray[m_nDOCUMENT_VALIDITY1] = new String("rIl documento non è stato modificato");
			asArray[m_nDOCUMENT_VALIDITY2] = new String("rdopo la firma");
			asArray[m_nSIGNATURE_DATE_TITLE] = new String("bData e ora della firma:");
			asArray[m_nSIGNATURE_DATE] = new String("r2008.11.02 10:03:34 Z");
			asArray[m_nEMPTY_FIELD_10] = new String("sthis text striken out");
			asArray[m_nSIGNATURE_DATE_METHOD_TITLE] = new String("bL'ora è stata acquisita da un server di");
			asArray[m_nSIGNATURE_DATE_METHOD_DATE] = new String("bmarca temporale il 2008.11.02 10:03:34 Z");

			setCertString(TreeNodeType.SIGNATURE, asArray);
		}

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
