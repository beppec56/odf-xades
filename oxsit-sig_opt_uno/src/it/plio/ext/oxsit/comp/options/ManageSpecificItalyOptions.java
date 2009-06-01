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

package it.plio.ext.oxsit.comp.options;

import it.plio.ext.oxsit.ooo.ui.DialogAbout;
import it.plio.ext.oxsit.options.SingleControlDescription;
import it.plio.ext.oxsit.options.SingleControlDescription.ControlTypeCode;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.beans.XPropertySet;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * 
 * manages the General option page on Tools > Options...
 * 
 * @author beppe
 *
 */
public class ManageSpecificItalyOptions extends ManageOptions  {

	// needed for registration
	public static final String			m_sImplementationName	= ManageSpecificItalyOptions.class.getName();
	public static final String[]		m_sServiceNames			= { "it.plio.ext.oxsit.comp.options.ManageSpecItalyOptions" };

    private int m_nAboutButton = 0;
    private int m_nBrowseInternetButton = 0;

    private static final int m_nNumberOfControls = 7;

    /**
     * 
     * @param xCompContext
     */
	public ManageSpecificItalyOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_general");
//		m_aLoggerDialog.enableLogging();// disabled in base class
		m_aLogger.ctor();
		//prepare the list of controls on the page

		int iter = 0;
		SingleControlDescription aControl = 
				new	SingleControlDescription("GeneralCNIPA_URL", ControlTypeCode.EDIT_TEXT, -1, "GeneralCNIPA_URL", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//checkbox
		aControl = 
			new SingleControlDescription("GeneralCheckBox1", ControlTypeCode.CHECK_BOX, -1, "TestBooleanValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//the three radio buttons
		aControl = 
			new SingleControlDescription("OptionButton0", ControlTypeCode.RADIO_BUTTON, 0, "TestNumericValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("OptionButton1", ControlTypeCode.RADIO_BUTTON, 1, "TestNumericValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("OptionButton2", ControlTypeCode.RADIO_BUTTON, 2, "TestNumericValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;//the single About push button
		aControl = 
			new SingleControlDescription("AboutButton", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nAboutButton = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("BrowseInternet", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
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

		m_aLogger.info( "supportsService" );
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	public void actionPerformed(ActionEvent rEvent) {
		m_aLogger.entering("actionPerformed");
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
            	DialogAbout.showDialog(null, m_xComponentContext, m_xMultiComponentFactory);
            }
            else if (sName.equals(ArrayOfControls[m_nBrowseInternetButton].m_sControlName)) {
            	m_aLogger.info("browse the Internet for a URL");
                //...
            }
            else {
            	m_aLogger.info("Activated: "+sName);            	
            }
        }catch (com.sun.star.uno.Exception ex){
            /* perform individual exception handling here.
             * Possible exception types are:
             * com.sun.star.lang.WrappedTargetException,
             * com.sun.star.beans.UnknownPropertyException,
             * com.sun.star.uno.Exception
             */
        	m_aLogger.severe("", "", ex);
        }		
	}
}