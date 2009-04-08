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

package it.plio.ext.xades.dispatchers;

import it.plio.ext.xades.dispatchers.threads.IDispatchImplementer;
import it.plio.ext.xades.dispatchers.threads.OnewayDispatchExecutor;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;

/** implement the dispatch base class, used by all the dispatch implementors used by the interceptor
 * the dispatch method is executed in asynchronous way, using a Java thread
 * is derived from ImplDispatchSynch, which implements the base behavior.
 * @author beppe
 *
 */
public class ImplDispatchAsynch extends ImplDispatchSynch implements XDispatch, IDispatchImplementer {

	protected Thread m_aThread = null;

	public ImplDispatchAsynch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super(xFrame, xContext, xMCF, unoSaveSlaveDispatch);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 */
	public void dispatch(URL aURL, PropertyValue[] lArguments) {
//		println("dispatch (ImplDispatchAsynch)  "+aURL.Complete);		
		OnewayDispatchExecutor aExecutor = new OnewayDispatchExecutor((IDispatchImplementer) this,aURL, lArguments);
		m_aThread = aExecutor;
		aExecutor.start();
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.cnipa.dispatchers.IDispatchImplementer#impl_dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 */
	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {
//		println("impl_dispatch (ImplDispatchAsynch)  "+aURL.Complete);		
		if(m_aUnoSlaveDispatch!=null)
			m_aUnoSlaveDispatch.dispatch(aURL,lArguments);		
	}
}
