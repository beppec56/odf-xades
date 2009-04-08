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

package it.plio.ext.oxsit.ooo.registry;

import it.plio.ext.oxsit.logging.XDynamicLogger;
import it.plio.ext.oxsit.ooo.ConfigurationAccess;
import it.plio.ext.oxsit.ooo.GlobConstant;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesBatch;

public class ToolbarButtonTitleRegistryAccess extends ConfigurationAccess {

	private Object	m_oToolbarTitleRegKey	= null;
	private XDynamicLogger			m_logger;	

	public ToolbarButtonTitleRegistryAccess(XComponentContext _xContext,
			XMultiComponentFactory _xMCF, String _sButtonName) {
		// printlnName("ctor");

		super( _xContext );
		m_logger = new XDynamicLogger(this,_xContext);
		
		try {
			m_oToolbarTitleRegKey = createConfigurationReadWriteView( GlobConstant.m_sEXTENSION_TOOLBAR_CONF_BASE_KEY
					+ _sButtonName );
			m_logger.info(GlobConstant.m_sEXTENSION_TOOLBAR_CONF_BASE_KEY + _sButtonName);
			// m_oToolbarTitleRegKey = createConfigurationReadWriteView(
			// "org.openoffice.Office.Addons/AddonUI/OfficeToolbar/");
			// m_oToolbarTitleRegKey = createConfigurationReadOnlyView(
			// "org.openoffice.Office.Addons/AddonUI/OfficeToolBar/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getString() {
		String retVal = "Title";
		if (m_oToolbarTitleRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oToolbarTitleRegKey );
				String[] elements = xNAccess.getElementNames();
				m_logger.info( "Available property names: " );

				for (int i = 0; i < elements.length; i++) {
					m_logger.log( elements[i] + ", " );
				}

				if (xNAccess.hasByName( retVal )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oToolbarTitleRegKey );
					retVal = AnyConverter.toString( xPS.getPropertyValue( retVal ) );
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			m_logger.log( "no element" );
		return retVal;
	}

	public void setString(String _sString) {
		String retVal = "Title2";
		if (m_oToolbarTitleRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oToolbarTitleRegKey );
//				String[] elements = xNAccess.getElementNames();
//				printlnName( "Available property names: " );
//
//				for (int i = 0; i < elements.length; i++) {
//					print( elements[i] + ", " );
//				}
//				print( "\n" );

				if (xNAccess.hasByName( retVal )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oToolbarTitleRegKey );		
					xPS.setPropertyValue( retVal, _sString );
//commit
		            // changes have been applied to the view here
		            XChangesBatch xUpdateControl = 
		                (XChangesBatch) UnoRuntime.queryInterface(XChangesBatch.class,m_oToolbarTitleRegKey);
		               xUpdateControl.commitChanges();
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			m_logger.log( "no element" );
	}
}
