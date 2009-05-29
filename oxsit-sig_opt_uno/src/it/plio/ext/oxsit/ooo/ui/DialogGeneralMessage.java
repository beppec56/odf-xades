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

import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XFramesSupplier;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class DialogGeneralMessage {

	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_axMCF;
	private XFrame m_xFrame;
	protected	IDynamicLogger m_aLogger;
	
	public DialogGeneralMessage(XFrame _xFrame, XMultiComponentFactory _axMCF, XComponentContext _xCC) {
		m_xCC = _xCC;
		m_axMCF = _axMCF;
		m_xFrame = _xFrame;
		m_aLogger = new DynamicLoggerDialog(this,_xCC);
	}

	/**
	 * 
	 * @param _sTitle title of the dialog
	 * @param _sMessage the message to display
	 * @param _nType a 'logical or' combination of com.sun.star.awt.MessageBoxButtons constant:<br>
	 * BUTTONS_OK 	specifies a message with "OK" button.<br>
	 * BUTTONS_OK_CANCEL 	specifies a message box with "OK" and "CANCEL" button.<br>
	 * BUTTONS_YES_NO 	specifies a message box with "YES" and "NO" button.<br>
	 * BUTTONS_YES_NO_CANCEL 	specifies a message box with "YES", "NO" and "CANCEL" button.<br>
	 * BUTTONS_RETRY_CANCEL 	specifies a message box with "RETRY" and "CANCEL" button.<br>
	 * BUTTONS_ABORT_IGNORE_RETRY 	specifies a message box with "ABORT", "IGNORE" and "RETRY" button.<br>
	 *   
	 * @param _nDefault a 'logical or' combination of com.sun.star.awt.MessageBoxButtons constant:<br>
	 * DEFAULT_BUTTON_OK 	specifies that OK is the default button.<br>
	 * DEFAULT_BUTTON_CANCEL 	specifies that CANCEL is the default button.<br>
	 * DEFAULT_BUTTON_RETRY 	specifies that RETRY is the default button.<br>
	 * DEFAULT_BUTTON_YES 	specifies that YES is the default button.<br>
	 * DEFAULT_BUTTON_NO 	specifies that NO is the default button.<br>
	 * DEFAULT_BUTTON_IGNORE 	specifies that IGNORE is the default button.<br>
	 * 
	 * @param _dialogType a string which determines the message box type.<br>
	 * The following strings are defined.
	 * 		infobox A message box to inform the user about a certain event. Attention:<br>
	 * 				This type of message box ignores the argument aButton because a info<br>
	 * 				box always shows a OK button.
	 * 		warningbox A message to warn the user about a certain problem.<br>
	 * 		errorbox A message box to provide an error message to the user.<br>
	 * 		querybox A message box to query information from the user.<br>
	 * 		messbox A normal message box.<br>
	 * 
	 * @return 	3: NO
	 * 			2: Yes
	 */
	public short executeDialog(String _sTitle, String _sMessage, int _nType, int _nDefault, String _dialogType){
		XComponent xComponent = null; 
		short nResult = 0;		  
		try {
			Object oToolkit = m_axMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", m_xCC);
			XMessageBoxFactory xMessageBoxFactory = (XMessageBoxFactory) UnoRuntime.queryInterface(XMessageBoxFactory.class, oToolkit);
			if (m_xFrame == null) {
				Object oDesktop = null;
				try {
					oDesktop = m_axMCF.createInstanceWithContext(
							"com.sun.star.frame.Desktop", m_xCC );
					XFramesSupplier xFramesSupplier = (XFramesSupplier) UnoRuntime
							.queryInterface( XFramesSupplier.class, oDesktop );
					m_xFrame = (XFrame) xFramesSupplier.getActiveFrame();
					// println("default Frame...");
				} catch (com.sun.star.uno.Exception oException) {
					oException.printStackTrace();
				}
			}

			XWindow xWindow = m_xFrame.getContainerWindow();
			XWindowPeer xWPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xWindow);
			if(xWPeer != null) {
				// rectangle may be empty if position is in the center of the parent peer
				Rectangle aRectangle = new Rectangle();
				XMessageBox xMessageBox = xMessageBoxFactory.createMessageBox(xWPeer, aRectangle, _dialogType,
						_nType | _nDefault, _sTitle, _sMessage);
				xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xMessageBox);
				if (xMessageBox != null) {
					nResult = xMessageBox.execute();
				}
			}
			else
				System.out.println("no peer");
		} catch (com.sun.star.uno.Exception ex) {
			ex.printStackTrace(System.out);
		}
		finally{
			//make sure always to dispose the component and free the memory!
			if (xComponent != null){
				xComponent.dispose();
			}
		}
		return nResult;
	}
}
