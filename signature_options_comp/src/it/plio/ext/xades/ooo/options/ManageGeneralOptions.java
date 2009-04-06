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

package it.plio.ext.xades.ooo.options;

import it.plio.ext.xades.ooo.options.SingleControlDescription.ControlTypeCode;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * 
 * manages the General option page on Tools > Options...
 * 
 * @author beppe
 *
 */
public class ManageGeneralOptions extends ManageOptions  {

	// needed for registration
	public static final String			m_sImplementationName	= ManageGeneralOptions.class.getName();
	public static final String[]		m_sServiceNames			= { "it.plio.ext.xades.options.ManageGeneralOptions" };

    private int m_nAboutButton = 0;
    private int m_nBrowseInternetButton = 0;

    private static final int m_nNumberOfControls = 7;

    /**
     * 
     * @param xCompContext
     */
	public ManageGeneralOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_general");
//		m_logger.enableLogging();// disabled in base class
		m_logger.ctor();
		//prepare the list of controls on the page

		int iter = 0;
		SingleControlDescription aControl = 
				new	SingleControlDescription("GeneralCNIPA_URL", ControlTypeCode.EDIT_TEXT, -1, "GeneralCNIPA_URL");
		ArrayOfControls[iter++] = aControl;
//checkbox
		aControl = 
			new SingleControlDescription("GeneralCheckBox1", ControlTypeCode.CHECK_BOX, -1, "TestBooleanValue");
		ArrayOfControls[iter++] = aControl;
//the three radio buttons
		aControl = 
			new SingleControlDescription("OptionButton0", ControlTypeCode.RADIO_BUTTON, 0, "TestNumericValue");
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("OptionButton1", ControlTypeCode.RADIO_BUTTON, 1, "TestNumericValue");
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("OptionButton2", ControlTypeCode.RADIO_BUTTON, 2, "TestNumericValue");
		ArrayOfControls[iter++] = aControl;//the single About push button
		aControl = 
			new SingleControlDescription("AboutButton", ControlTypeCode.PUSH_BUTTON, -1, "");
		m_nAboutButton = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("BrowseInternet", ControlTypeCode.PUSH_BUTTON, -1, "");
		m_nBrowseInternetButton = iter; // set for further processing
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;
	}

	public String getImplementationName() {
		return m_sImplementationName;
	}

	public String[] getSupportedServiceNames() {
		return m_sServiceNames;
	}

	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_logger.info( "supportsService" );
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	public void actionPerformed(ActionEvent rEvent) {
		m_logger.entering("actionPerformed");
	// TODO Auto-generated method stub
        try{
            // get the control that has fired the event,
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, rEvent.Source);
            XControlModel xControlModel = xControl.getModel();
            XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
            String sName = (String) xPSet.getPropertyValue("Name");
            // just in case the listener has been added to several controls,
            // we make sure we refer to the right one
            if (sName.equals(ArrayOfControls[m_nAboutButton].m_sControlName)) {
// activate the about dialog box
            	
            	
            }
            else if (sName.equals(ArrayOfControls[m_nBrowseInternetButton].m_sControlName)) {
            	m_logger.info("browse the Internet for a URL");
                //...
            }
            else {
            	m_logger.info("Activated: "+sName);            	
            }
        }catch (com.sun.star.uno.Exception ex){
            /* perform individual exception handling here.
             * Possible exception types are:
             * com.sun.star.lang.WrappedTargetException,
             * com.sun.star.beans.UnknownPropertyException,
             * com.sun.star.uno.Exception
             */
        	m_logger.severe("", "", ex);
        }		
	}
}
