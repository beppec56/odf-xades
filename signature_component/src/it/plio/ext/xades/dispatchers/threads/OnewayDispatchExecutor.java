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

package it.plio.ext.xades.dispatchers.threads;


import com.sun.star.beans.PropertyValue;
import com.sun.star.util.URL;

public class OnewayDispatchExecutor extends Thread {
	private IDispatchImplementer m_rLink = null;
	private URL m_aURL = null;
	private PropertyValue[] m_lParams = null;

	public OnewayDispatchExecutor(IDispatchImplementer rLink    ,
			com.sun.star.util.URL aURL,
			com.sun.star.beans.PropertyValue[] lArguments  )
	{
		m_rLink    = rLink   ;
		m_aURL = aURL;
		m_lParams  = lArguments ;

		if (m_rLink==null)
			System.out.println("ctor ... m_rLink == null");
		if (m_lParams==null)
			System.out.println("ctor ... m_lParams == null");
	}

	/**
	 * implements the thread function
	 * Here we call the internal set link object back and
	 * give him all neccessary parameters.
	 * After that we die by ouerself ...
	 */
	public void run() {
//		System.out.println(String.format("%8s", Integer.toString(hashCode(), 16).toUpperCase())+
//				" "+this.getClass().getName()+" started");
		if (m_rLink == null)
			System.out.println("run ... m_rLink == null");
		if (m_lParams == null)
			System.out.println("run ... m_lParams == null");

		if (m_rLink != null) {
				m_rLink.impl_dispatch(m_aURL, m_lParams);
		}
	}	
}
