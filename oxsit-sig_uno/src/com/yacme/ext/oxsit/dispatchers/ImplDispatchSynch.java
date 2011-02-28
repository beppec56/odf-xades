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

package com.yacme.ext.oxsit.dispatchers;



import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
//import com.sun.star.frame.XModel;
//import com.sun.star.frame.XNotifyingDispatch;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.yacme.ext.oxsit.logging.DynamicLogger;

/** superclass of all dispatchers
 * @author beppe
 *
 */
public class ImplDispatchSynch implements IDispatchBaseObject //XDispatch, XComponent 
{

	protected XFrame m_xFrame;
	protected XMultiComponentFactory m_axMCF;
	protected XComponentContext m_xCC;
	protected XDispatch m_aUnoSlaveDispatch = null;
	
	protected DynamicLogger		m_aLogger;

	public ImplDispatchSynch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		m_xFrame = xFrame;
		m_axMCF = xMCF;
		m_xCC = xContext;
		m_aUnoSlaveDispatch = unoSaveSlaveDispatch;
		m_aLogger = new DynamicLogger(this,xContext);
		m_aLogger.info("ctor");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void addStatusListener(XStatusListener aListener, URL aURL) {
//		m_aLoggerDialog.log("addStatusListener",aURL.Complete);		
		if(m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.addStatusListener(aListener, aURL);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 * 
	 */
	/** important: the derived class should implement itself the XNotifyingDispatch behavior 
	 * 
	 */
	public void dispatch(URL aURL, PropertyValue[] lArguments) {
		m_aLogger.log("dispatch (ImplDispatchSynch)  "+aURL.Complete);		
		if(m_aUnoSlaveDispatch!=null)
			m_aUnoSlaveDispatch.dispatch(aURL,lArguments);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void removeStatusListener(XStatusListener aListener, URL aURL) {
//		m_aLoggerDialog.log("removeStatusListener",aURL.Complete);		
		if(m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.removeStatusListener(aListener, aURL);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {		
		m_aLogger.severe("addEventListener", "implements it in subclass!");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		m_aLogger.severe("dispose", "implements it in subclass!");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		m_aLogger.severe("removeEventListener", "implements it in subclass!");		
	}	
}
