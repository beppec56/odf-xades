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
public class MessageError extends DialogGeneralMessage {

	public static String	m_sTitle;
	
	/**
	 * @param frame
	 * @param _axmcf
	 * @param _xcc
	 */
	public MessageError(XFrame frame, XMultiComponentFactory _axmcf,
			XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
	}
	
	public short executeDialogLocal(String _theError) {
		//read strings from resource
		if(m_sTitle == null) {
			MessageConfigurationAccess m_aRegAcc = null;
			m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
			try {
				m_sTitle = m_aRegAcc.getStringFromRegistry( "id_error_extension" );
			} catch (Exception e) {
				m_aLogger.severe(e);
			}			
			m_aRegAcc.dispose();
		}

		return super.executeDialog((m_sTitle == null) ? "Error! ": m_sTitle, 
				_theError,
				MessageBoxButtons.BUTTONS_OK ,
				MessageBoxButtons.DEFAULT_BUTTON_OK,
				MessageBoxType.ERRORBOX);
	}
}
