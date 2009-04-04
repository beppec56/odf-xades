/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) as published
 *  by the European Community, either version 1.1 of the License,
 *  or any later version.
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

package it.plio.ext.xades.signature.dispatchers;

import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;

import it.plio.ext.xades.dispatchers.ImplDispatchAsynch;
import it.plio.ext.xades.dispatchers.ImplDispatchSynch;
import it.plio.ext.xades.logging.XDynamicLogger;
import it.plio.ext.xades.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.xades.ooo.ui.DialogAbout;

/**
 * @author beppe
 *
 */
public class ImplOnHelpDispatch extends ImplDispatchAsynch {

	public ImplOnHelpDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
	}

	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {

		// TODO Auto-generated method stub
		m_logger.info("impl_dispatch (ImplDispatchAsynch)  "+aURL.Complete);		
		showAboutBox();
	}

	private void showAboutBox() {
		DialogAbout aDialog1 =
			new DialogAbout( m_xFrame, m_xCC, m_axMCF );
		try {
			//PosX e PosY devono essere ricavati dalla finestra genetrice (in questo caso la frame)
			//get the parente window data
//			com.sun.star.awt.XWindow xCompWindow = m_xFrame.getComponentWindow();
//			com.sun.star.awt.Rectangle xWinPosSize = xCompWindow.getPosSize();
			int BiasX = 100;
			int BiasY = 30;
//			System.out.println("Width: "+xWinPosSize.Width+ " height: "+xWinPosSize.Height);
//			XWindow xWindow = m_xFrame.getContainerWindow();
//			XWindowPeer xPeer = xWindow.
			aDialog1.initialize(BiasX,BiasY);
//center the dialog
			aDialog1.executeDialog();
		}
		catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		} catch (BasicErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
