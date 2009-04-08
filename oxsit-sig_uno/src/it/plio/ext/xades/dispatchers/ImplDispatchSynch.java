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


import it.plio.ext.oxsit.logging.XDynamicLogger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
//import com.sun.star.frame.XModel;
//import com.sun.star.frame.XNotifyingDispatch;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;

/** superclass of all dispatchers
 * @author beppe
 *
 */
public class ImplDispatchSynch implements XDispatch {

	protected XFrame m_xFrame;
	protected XMultiComponentFactory m_axMCF;
	protected XComponentContext m_xCC;
/*	protected String today;*/
	protected XDispatch m_aUnoSlaveDispatch = null;
	
	protected XDynamicLogger		m_logger;

	public ImplDispatchSynch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		m_xFrame = xFrame;
		m_axMCF = xMCF;
		m_xCC = xContext;
		m_aUnoSlaveDispatch = unoSaveSlaveDispatch;
		m_logger = new XDynamicLogger(this,xContext);

/*		DateFormat timeFormatter =
			DateFormat.getTimeInstance(DateFormat.DEFAULT, new Locale("it"));
		today =  timeFormatter.format(new Date());*/
		m_logger.info("ctor");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void addStatusListener(XStatusListener aListener, URL aURL) {
		m_logger.log("addStatusListener "+String.format("%8H",hashCode())+" "+aURL.Complete);		
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
		m_logger.log("dispatch (ImplDispatchSynch)  "+aURL.Complete);		
		if(m_aUnoSlaveDispatch!=null)
			m_aUnoSlaveDispatch.dispatch(aURL,lArguments);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void removeStatusListener(XStatusListener aListener, URL aURL) {
		m_logger.log("removeStatusListener "+String.format("%8H",hashCode())+" "+aURL.Complete);		
		if(m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.removeStatusListener(aListener, aURL);
	}
	
}
