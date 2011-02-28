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

package com.yacme.ext.oxsit.signature.dispatchers;


import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.yacme.ext.oxsit.dispatchers.threads.ImplDispatchAsynch;
import com.yacme.ext.oxsit.ooo.ui.DialogAbout;

/**
 * @author beppe
 *
 */
public class ImplOnHelpDispatch extends ImplDispatchAsynch {

	public ImplOnHelpDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
/*		m_aLoggerDialog.enableLogging();
		m_aLoggerDialog.ctor();*/
	}

	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {
//		m_aLoggerDialog.info("impl_dispatch (ImplDispatchAsynch)  "+aURL.Complete);
		DialogAbout.showDialog(m_xFrame, m_xCC, m_axMCF);
	}
}
