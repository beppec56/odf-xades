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

package com.yacme.ext.oxsit.dispatchers.threads;

import java.util.Date;


import com.sun.star.beans.PropertyValue;
import com.sun.star.util.URL;
import com.yacme.ext.oxsit.signature.dispatchers.ImplXAdESSignatureDispatchTB;

public class ImplXAdESThread extends Thread {
	short m_nRunCode = 0;
	private URL m_aURL = null;
	private PropertyValue[] m_lParams = null;
	
	private ImplXAdESSignatureDispatchTB m_theClassTB = null;
	
	private com.sun.star.util.ChangesEvent m_aChangesEvent = null;
	private com.sun.star.document.EventObject m_aEventObj = null;
	private com.sun.star.frame.XStatusListener m_aListener = null;
	
	
	public static final short RUN_changesOccurred = 1;
	public static final short RUN_removeStatusListener = 2;

	public ImplXAdESThread(ImplXAdESSignatureDispatchTB theClass,
						short nCode, 
						com.sun.star.util.ChangesEvent aChangesEvent) {
		m_nRunCode = nCode;
		m_theClassTB = theClass;
		m_aChangesEvent = aChangesEvent;
	}

	public ImplXAdESThread(ImplXAdESSignatureDispatchTB theClass,
						short nCode,
						com.sun.star.frame.XStatusListener aListener,
						URL aURL) {
		m_nRunCode = nCode;
		m_theClassTB = theClass;
		m_aListener = aListener;
		m_aURL = aURL;
	}
	
	public void run() {
//		println("started, code: "+m_nRunCode);
/*		if(m_theClassTB != null) {
			switch(m_nRunCode) {
			case RUN_changesOccurred:
				m_theClassTB.impl_changesOccurred(m_aChangesEvent);
				break;
			case RUN_removeStatusListener:
				m_theClassTB.impl_removeStatusListener(m_aListener, m_aURL);
				break;
			default:
//				m_aLoggerDialog.info("no code supplied!");
			}
		}*/
	}

	public static String getTimeMs() {
		Date aDate = new Date();
		String time = String.format( "%20d ", aDate.getTime() );
		return time.substring( 14,17 ) +"."+ time.substring( 17 );
	}
}
