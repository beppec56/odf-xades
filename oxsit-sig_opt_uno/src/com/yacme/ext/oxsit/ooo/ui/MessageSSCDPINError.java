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
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;

/**
 * @author beppe
 *
 */
public class MessageSSCDPINError extends DialogGeneralMessage {
	/**
	 * @param frame
	 * @param _axmcf
	 * @param _xcc
	 * 
	 * 
	 */
	public MessageSSCDPINError(XFrame frame, XMultiComponentFactory _axmcf,
			XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
	}

	public short executeDialogLocal(String _PKCS11ErrorCode) {
		//read strings from resource
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
		String sTitle = "id_error_extension";
		String sPINErr = "id_mex_err_pin";
		String sPinErrDescr = "";
		try {
			sTitle = m_aRegAcc.getStringFromRegistry( sTitle );
			sPINErr = m_aRegAcc.getStringFromRegistry( sPINErr );			
			sPinErrDescr = m_aRegAcc.getStringFromRegistry( _PKCS11ErrorCode );			
		} catch (Exception e) {
			m_aLogger.severe(e);
		}			
		m_aRegAcc.dispose();

		return super.executeDialog(sTitle,
				String.format(sPINErr, sPinErrDescr),
				MessageBoxButtons.BUTTONS_RETRY_CANCEL , MessageBoxButtons.DEFAULT_BUTTON_RETRY, "errorbox");
	}
}
