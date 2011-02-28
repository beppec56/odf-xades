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

package com.yacme.ext.oxsit.ooo.registry;


import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.ConfigurationAccess;
import com.yacme.ext.oxsit.ooo.GlobConstant;

public class MessageConfigurationAccess extends ConfigurationAccess implements XComponent {

	private Object m_oMessagesRegKey = null;

	public MessageConfigurationAccess(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		// printlnName("ctor");
		
		super(_xContext );
		try {
			m_oMessagesRegKey = createConfigurationReadOnlyView(GlobConstant.m_sEXTENSION_CONF_BASE_KEY+"Messages/");
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}

    public String getStringFromRegistry( String _stringIdToRetrieve ) throws Exception {
    	if(_stringIdToRetrieve == null)
    		return "";
    	if(m_oMessagesRegKey != null) {
    		//get the string at id
    	    // accessing a single nested value
    		XPropertySetInfo oPropIn = (XPropertySetInfo) UnoRuntime.queryInterface(XPropertySetInfo.class, m_oMessagesRegKey);  		
    		if(oPropIn.hasPropertyByName( _stringIdToRetrieve )) {
    			XNameAccess xNAccess =(XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, m_oMessagesRegKey);
    			// get the value
    			Object oObj = xNAccess.getByName( _stringIdToRetrieve );

    			XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, oObj);
    			String retVal = AnyConverter.toString( xPS.getPropertyValue( "Text" ) );
        		return retVal;
    		}
    		else
    			m_logger.info("no element id: "+_stringIdToRetrieve);
    	}
		return new String(_stringIdToRetrieve); 
    }

	public void addEventListener(XEventListener arg0) {
		m_logger.info("addEventListener");
	}

	public void dispose() {
		synchronized (this) {
			if(m_oMessagesRegKey != null) {
		        // now clean up
		        ((XComponent) UnoRuntime.queryInterface(XComponent.class, m_oMessagesRegKey)).dispose();
		        m_oMessagesRegKey = null;
			}
		}
	}

	public void removeEventListener(XEventListener arg0) {
		m_logger.info("removeEventListener");		
	}
}
