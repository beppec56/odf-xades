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

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

/** This class describes the node representing a certificate obtained from
 * an SSCD.
 * This element shows on the right side of the dialog a series of text lines, with the status of
 * the certificate.
 * 
 * @author beppec56
 *
 */
public class CertificateTreeElementBase extends TreeElement {

	// describes the field for certificate/signature status common to both
	protected  int m_nFIELD_OWNER_NAME 					= 0;
	protected  int m_nFIELD_CERTIFICATE_STATE 			= 1;
	protected  int m_nFIELD_CERTIFICATE_VERF_CONDITIONS	= 2;
	protected  int m_nFIELD_TITLE_ISSUER	 			= 7;
	protected  int m_nFIELD_ISSUER 						= 8;
	protected  int m_nFIELD_ISSUER_VERF_CONDITIONS		= 9;

	/**
	 * the string list showed on the left, allocated, according to global constants.
	 */
	protected String[]	m_sStringList;
	
	protected MessageConfigurationAccess m_aRegAcc = null; 

	public CertificateTreeElementBase(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		setNodeType(TreeNodeType.CERTIFICATE);
		setLogger(new DynamicLogger(this,_xContext));
		setMultiComponentFactory(_xMCF);
		setComponentContext(_xContext);
		setCertificateState(TreeElement.m_nCERTIFICATE_STATE_NOT_VERIFIED);
		setCertificateStateConditions(TreeElement.m_nCERTIFICATE_STATE_CONDT_DISABLED);
		setIssuerState(TreeElement.m_nISSUER_STATE_NO_CTRL);
		setNodeGraphic(null);
		m_sStringList = new String[CertifTreeDlgDims.m_nMAXIMUM_FIELDS];
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#initialize()
	 * 
	 * initializes the element common to both the signature and certificate
	 */
	@Override
	void initialize() {
//inizialize string grabber
		m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
		// allocate string needed for display
		// these are the string common to both the signature and the 
		// certificate
		try {
			//just for test, should be added by subclass
			m_sStringList[m_nFIELD_OWNER_NAME] = "the owner: Goofy";  // will got it from the certificate raw data

			//initializes fixed string (titles)
			m_sStringList[m_nFIELD_TITLE_ISSUER] = m_aRegAcc.getStringFromRegistry("cert_title_issuer" );

			//grab the string for certificate status
			m_sStringList[m_nFIELD_CERTIFICATE_STATE] =
					m_aRegAcc.getStringFromRegistry( m_sCERTIFICATE_STATE[getCertificateState()] );
			m_sStringList[m_nFIELD_CERTIFICATE_VERF_CONDITIONS] =
				m_aRegAcc.getStringFromRegistry( m_sCERTIFICATE_STATE_CONDT[getCertificateStateConditions()] );
	
			m_sStringList[m_nFIELD_ISSUER] = "The Issuer Name";
			m_sStringList[m_nFIELD_ISSUER_VERF_CONDITIONS] =
				m_aRegAcc.getStringFromRegistry( m_sISSUER_STATE[getIssuerState()] );			

		} catch (Exception e) {
			getLogger().severe("initialize", e);
		}
		m_aRegAcc.dispose();
	}
}
