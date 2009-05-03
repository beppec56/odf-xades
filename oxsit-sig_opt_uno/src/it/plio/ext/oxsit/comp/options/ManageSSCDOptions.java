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

import it.plio.ext.oxsit.options.SingleControlDescription;
import it.plio.ext.oxsit.options.SingleControlDescription.ControlTypeCode;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XItemListener;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * 
 * manages the General option page on Tools > Options...
 * 
 * @author beppe
 *
 */
public class ManageSSCDOptions extends ManageOptions  {
	// needed for registration
	public static final String			m_sImplementationName	= ManageSSCDOptions.class.getName();
	public static final String[]		m_sServiceNames			= { "it.plio.ext.oxsit.options.ManageSSCDOptions" };

    private int m_nBrowseSystemPath1PB = 0;
    private int m_nBrowseSystemPath2PB = 0;
    private int m_nBrowseSystemPath3PB = 0;
    private int m_nBrowseSystemPath4PB = 0;

    private static final int m_nNumberOfControls = 8;

    /**
     * 
     * @param xCompContext
     */
	public ManageSSCDOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_sscd");//leaf refers to OOo documentation about
															// extension options
		m_logger.enableLogging();// disabled in base class
/*		m_logger.disableInfo();
		m_logger.disableWarning();*/
		m_logger.ctor();
		//prepare the list of controls on the page

		//the parameter sName comes from basic dialog
		//the parameter sProperty comes from file AddonConfiguration.xcs.xml
		int iter = 0;
		//checkbox
		SingleControlDescription aControl = 
				new	SingleControlDescription("SSCDFilePath1", ControlTypeCode.EDIT_TEXT, -1, "SSCDFilePath1", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//the actionPerformed pushbutton		
		aControl = 
			new SingleControlDescription("BrowseSystemPath1", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseSystemPath1PB = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;
		
		 aControl = 
				new	SingleControlDescription("SSCDFilePath2", ControlTypeCode.EDIT_TEXT, -1, "SSCDFilePath2", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//the actionPerformed pushbutton		
		aControl = 
			new SingleControlDescription("BrowseSystemPath2", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseSystemPath2PB = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;		

		 aControl = 
				new	SingleControlDescription("SSCDFilePath3", ControlTypeCode.EDIT_TEXT, -1, "SSCDFilePath3", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//the actionPerformed pushbutton		
		aControl = 
			new SingleControlDescription("BrowseSystemPath3", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseSystemPath3PB = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;		

		aControl = 
				new	SingleControlDescription("SSCDFilePath4", ControlTypeCode.EDIT_TEXT, -1, "SSCDFilePath4", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//the actionPerformed pushbutton		
		aControl = 
			new SingleControlDescription("BrowseSystemPath4", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseSystemPath4PB = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;		

		m_logger.ctor("2");

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

	protected void loadData(com.sun.star.awt.XWindow aWindow)
	  throws com.sun.star.uno.Exception {
		super.loadData(aWindow);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XActionListener#actionPerformed(com.sun.star.awt.ActionEvent)
	 */
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
            if (sName.equals(ArrayOfControls[m_nBrowseSystemPath1PB].m_sControlName)) {
            	m_logger.info("browse the system for a path, SSCD 1");
                //...
            	//... implement the function...
// we need to get the frame, the component context and from it the multiservice factory
            	
            	//
            }
            else {
            	m_logger.info("Activated: "+sName);            	
            }
        }catch (com.sun.star.uno.Exception ex){
            // perform individual exception handling here.
            // Possible exception types are:
            // com.sun.star.lang.WrappedTargetException,
            // com.sun.star.beans.UnknownPropertyException,
            // com.sun.star.uno.Exception
        	m_logger.severe("", "", ex);
        }		
	}
}
