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

package com.yacme.ext.oxsit.ooo.ui;


import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.MessageBoxType;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;

/**
 * @author beppe
 *
 */
public class MessageNoSignatureToken extends DialogGeneralMessage {
	/**
	 * @param frame
	 * @param _axmcf
	 * @param _xcc
	 * 
	 * 
	 */
	public MessageNoSignatureToken(XFrame frame, XMultiComponentFactory _axmcf, XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
	}

	public short executeDialogLocal(String _SSCDManufacturer, String _SSCDModel, String _SSCDSerialNumber, String _CKR_error_found) {
		//read strings from resource
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xCC, m_axMCF);
		String sTitle = "id_error_extension";
		String sFormatErr = "id_mex_sign_no_token";
		String sErrortext ="";
		try {
			sTitle = m_aRegAcc.getStringFromRegistry(sTitle);
			sFormatErr = m_aRegAcc.getStringFromRegistry(sFormatErr);
			sErrortext = m_aRegAcc.getStringFromRegistry(_CKR_error_found);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		m_aRegAcc.dispose();

		return super.executeDialog(sTitle, String.format(sFormatErr,
				_SSCDManufacturer,
				_SSCDModel,
				_SSCDSerialNumber,
				sErrortext,
				_CKR_error_found),
				MessageBoxButtons.BUTTONS_ABORT_IGNORE_RETRY, MessageBoxButtons.DEFAULT_BUTTON_RETRY, MessageBoxType.ERRORBOX);
	}
}
