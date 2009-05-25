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
import it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;
import it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/** This class describes the node representing a certificate obtained from
 * an SSCD.
 * This element shows on the right side of the dialog a series of text lines, with the status of
 * the certificate.
 * 
 * @author beppec56
 *
 */
public class CertificateTreeElement extends BaseCertificateTreeElement {

	//=============================================
	// describes the field for certificate general status
	// only for certificate
	public final int m_nFIELD_TITLE_VALID_FROM 				= 3;
	public final int m_nFIELD_DATE_VALID_FROM 				= 4;
	public final int m_nFIELD_TITLE_VALID_TO 				= 5;
	public final int m_nFIELD_DATE_VALID_TO	 				= 6;
	public final int m_nFIELD_TEXT_FIELD_10 				= 10;
	public final int m_nFIELD_TEXT_FIELD_11					= 11;
	public final int m_nFIELD_TEXT_FIELD_12 				= 12;
	public final int m_nFIELD_TEXT_FIELD_13 				= 13;
	
	//state of this certificate, comes from controls of the certificate itself
	// the returned value will be in the range of it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState.
	private int m_nCertificateGraficStateValue;

	public CertificateTreeElement(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		super(_xContext,_xMCF);
		setNodeType(TreeNodeType.CERTIFICATE);
//		getLogger().enableLogging();
		getLogger().ctor();
		setMultiComponentFactory(_xMCF);
		setComponentContext(_xContext);
		m_nCertificateGraficStateValue = CertificateGraphicDisplayState.NOT_VERIFIED_value;
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#initialize()
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
//inizialize string grabber
		m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
		//allocate string needed for display
		try {
			//initializes fixed string (titles)
			m_sStringList[m_nFIELD_TITLE_VALID_FROM] = m_aRegAcc.getStringFromRegistry("cert_title_valid_from" );  
			m_sStringList[m_nFIELD_TITLE_VALID_TO] = m_aRegAcc.getStringFromRegistry("cert_title_valid_to" );
//fill emtpy fields
			m_sStringList[m_nFIELD_TEXT_FIELD_10] = "r";
			m_sStringList[m_nFIELD_TEXT_FIELD_11] = "r";
			m_sStringList[m_nFIELD_TEXT_FIELD_12] = "r";
			m_sStringList[m_nFIELD_TEXT_FIELD_13] = "r";
		} catch (Exception e) {
			getLogger().severe("initialize", e);
		}
		m_aRegAcc.dispose();
		super.initialize();
	}

	/** specific initialization for certificate
	 * 
	 * @param _aCertif
	 */
	public void setCertificateData(XOX_X509Certificate _aCertif) {
//set the node name
		XOX_X509CertificateDisplay aCertDisplay = 
			(XOX_X509CertificateDisplay)UnoRuntime.queryInterface(XOX_X509CertificateDisplay.class, _aCertif);
		setNodeName(aCertDisplay.getSubjectDisplayName());
//FIXME		setCertificateGraficStateValue(_aCertif.getCertificateGraficStateValue());
		setCertificateState(_aCertif.getCertificateState());
		setCertificateStateConditions(_aCertif.getCertificateStateConditions());
		setCertificationAutorityState(_aCertif.getCertificationAuthorityState());
		initialize();
		//init it correctly		
		m_sStringList[m_nFIELD_DATE_VALID_FROM] ="r"+ aCertDisplay.getNotValidBefore();
		m_sStringList[m_nFIELD_DATE_VALID_TO] = "r"+aCertDisplay.getNotValidAfter();
		//next should be set to the right certificate string to display
		m_sStringList[m_nFIELD_OWNER_NAME] = "b"+getNodeName();  // will got it from the certificate raw data		
		m_sStringList[m_nFIELD_ISSUER] = "r"+aCertDisplay.getIssuerDisplayName();
	}

	/**
	 * @param m_nCertificateGraficStateValue the m_nCertificateGraficStateValue to set
	 * value MUST be in the range of it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState.
	 * this value is normally driven from the XOX_X509Certificated interface object.
	 */
	public void setCertificateGraficStateValue(
			int m_nCertificateGraficStateValue) {
		this.m_nCertificateGraficStateValue = m_nCertificateGraficStateValue;
//set the right string
		
	}

	/**
	 * @return the CertificateGraficStateValue, 
	 * the returned value will be in the range of it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState.

	 */
	public int getCertificateGraficStateValue() {
		return m_nCertificateGraficStateValue;
	}
}
