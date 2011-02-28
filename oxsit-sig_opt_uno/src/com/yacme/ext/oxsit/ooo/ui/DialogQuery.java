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

public class DialogQuery {

	private XComponentContext m_xCC;
	private XMultiComponentFactory m_axMCF;
	private XFrame m_xFrame;
	
	//FIXME: dialog to rewrite subclassing:
	//com.yacme.ext.oxsit.ooo.ui.MessageNoSSCD.MessageNoSSCD
	//
	public DialogQuery(XFrame _xFrame, XMultiComponentFactory _axMCF, XComponentContext _xCC) {
		m_xCC = _xCC;
		m_axMCF = _axMCF;
		m_xFrame = _xFrame;
	}

	public short executeDialog(String _sTitle, String _sMessage, int _nType, int _nDefault){
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
				XMessageBox xMessageBox = xMessageBoxFactory.createMessageBox(xWPeer, aRectangle, "querybox",
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
