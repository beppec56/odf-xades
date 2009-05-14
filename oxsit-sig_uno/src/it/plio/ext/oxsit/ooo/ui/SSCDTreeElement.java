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
import it.plio.ext.oxsit.security.XOX_SSCDevice;

/** This class describes the node representing a certificate obtained from
 * an SSCD.
 * This element shows on the right side of the dialog a series of text lines, with the status of
 * the certificate.
 * 
 * @author beppec56
 *
 */
public class SSCDTreeElement extends BaseGeneralMultilineTreeElement {

	//=============================================
	// describes the field for certificate general status
	// only for certificate
	public final int m_nFIELD_SSCD_TITLE_DESCRIPTION	= 0;
	public final int m_nFIELD_SSCD_DESCRIPTION			= 1;

	public final int m_nFIELD_SSCD_TITLE_MANUFACTURER	= 3;
	public final int m_nFIELD_SSCD_MANUFACTURER			= 4;

	public final int m_nFIELD_SSCD_TITLE_ATR			= 6;
	public final int m_nFIELD_SSCD_ATR1					= 7;
	public final int m_nFIELD_SSCD_ATR2					= 8;

	public final int m_nFIELD_SSCD_TITLE_LIB			= 10;
	public final int m_nFIELD_SSCD_LIB					= 11;

	public SSCDTreeElement(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		super(_xContext,_xMCF);
		setNodeType(TreeNodeType.SSCDEVICE);
		getLogger().enableLogging();
		getLogger().ctor();
		setMultiComponentFactory(_xMCF);
		setComponentContext(_xContext);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#initialize()
	 */
	@Override
	public void initialize() {
//inizialize string grabber
		super.initialize();
		m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
		//allocate string needed for display
		try {
			//initializes fixed string (titles)
			m_sStringList[m_nFIELD_SSCD_TITLE_DESCRIPTION] = m_aRegAcc.getStringFromRegistry("sscd_title_description" );  
			m_sStringList[m_nFIELD_SSCD_TITLE_MANUFACTURER] = m_aRegAcc.getStringFromRegistry("sscd_title_manuf" );
			m_sStringList[m_nFIELD_SSCD_TITLE_ATR] = m_aRegAcc.getStringFromRegistry("sscd_title_atr" );
			m_sStringList[m_nFIELD_SSCD_TITLE_LIB] = m_aRegAcc.getStringFromRegistry("sscd_title_lib" );
		} catch (Exception e) {
			getLogger().severe("initialize", e);
		}
		m_aRegAcc.dispose();
	}

	//custom node init function
	public void setSSCDDATA(XOX_SSCDevice _aSSCdev) {
		m_sStringList[m_nFIELD_SSCD_DESCRIPTION] = "r" + _aSSCdev.getDescription();
		m_sStringList[m_nFIELD_SSCD_MANUFACTURER] = "r" +  _aSSCdev.getManufacturer();
		String sAtrCode = _aSSCdev.getATRcode();
		if(sAtrCode.length() > 4) {
		m_sStringList[m_nFIELD_SSCD_ATR1] = "r"+ sAtrCode.substring(0, sAtrCode.length()/2);
		m_sStringList[m_nFIELD_SSCD_ATR2] = "r  "+ sAtrCode.substring(sAtrCode.length()/2, sAtrCode.length());
		}
		else if (sAtrCode.length() == 4) {
			m_sStringList[m_nFIELD_SSCD_ATR1] = "r"+ sAtrCode;
		}
		m_sStringList[m_nFIELD_SSCD_LIB] = "r"+ _aSSCdev.getCryptoLibraryUsed();
		setNodeName(_aSSCdev.getDescription());
	}
}
