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

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.ooo.ui.DialogFileOrFolderPicker;
import it.plio.ext.oxsit.options.SingleControlDescription;
import it.plio.ext.oxsit.options.SingleControlDescription.ControlTypeCode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
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
	public static final String[]		m_sServiceNames			= { "it.plio.ext.oxsit.comp.options.ManageSSCDOptions" };

    private int m_nBrowseSystemPath1PB = 0;
    private int m_nBrowseSystemPath1ET = 0;

    private static final int m_nNumberOfControls = 3;
    
    private	String	m_sDialogTitle = "id_opt_dlg_getsscd_lib";

    /**
     * 
     * @param xCompContext
     */
	public ManageSSCDOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_sscd");//leaf refers to OOo documentation about
															// extension options
//DEBUG		m_aLoggerDialog.enableLogging();// disabled in base class
/*		m_aLoggerDialog.disableInfo();
		m_aLoggerDialog.disableWarning();*/
		m_aLogger.ctor();
		//prepare the list of controls on the page

		//the parameter sName comes from basic dialog
		//the parameter sProperty comes from file AddonConfiguration.xcs.xml
		int iter = 0;
		//checkbox, auto detection
		SingleControlDescription aControl = 
			new SingleControlDescription("AutoSelect", ControlTypeCode.CHECK_BOX, -1, "SSCDAutomaticDetection", 0, 0, true);
		ArrayOfControls[iter++] = aControl;

		aControl = 
				new	SingleControlDescription("SSCDFilePath1", ControlTypeCode.EDIT_TEXT, -1, "SSCDFilePath1", 0, 0, true);
		m_nBrowseSystemPath1ET = iter;
		ArrayOfControls[iter++] = aControl;
//the actionPerformed pushbutton		
		aControl = 
			new SingleControlDescription("BrowseSystemPath1", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseSystemPath1PB = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;

//grab the title string for configuration dialog
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xComponentContext, m_xMultiComponentFactory);
		try {
			m_sDialogTitle = m_aRegAcc.getStringFromRegistry( m_sDialogTitle );
		} catch (Exception e) {
			m_aLogger.severe("ctor",e);
		}			
		m_aRegAcc.dispose();
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

	protected void loadData(com.sun.star.awt.XWindow aWindow)
	  throws com.sun.star.uno.Exception {
		super.loadData(aWindow);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XActionListener#actionPerformed(com.sun.star.awt.ActionEvent)
	 */
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
            if (sName.equals(ArrayOfControls[m_nBrowseSystemPath1PB].m_sControlName)) {
            	m_aLogger.info("browse the system for a path, SSCD 1");
                //...
            	//... implement the function...
// we need to get the frame, the component context and from it the multiservice factory
// then instantiate a file dialog to search for a path            	
            	DialogFileOrFolderPicker aDlg = new DialogFileOrFolderPicker(m_xMultiComponentFactory,m_xComponentContext);
            	
// the parameter is stored in configuration as the system native path, so change into URL...
//get the data from the control
            	String sStartFolder = "";
            	String sStartFile = "";
            	{
	    		    xControl = ArrayOfControls[m_nBrowseSystemPath1ET].m_xTheControl;
	    	    	XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
	    	    			XPropertySet.class, xControl.getModel());
	    	    	if (xProp == null)
	    	    		throw new com.sun.star.uno.Exception(
	    	    				"Could not get XPropertySet from control.", this);
		    		String sTheText = 
		    			AnyConverter.toString( xProp.getPropertyValue( "Text" ) );

		    		if(sTheText.length() == 0) {
		    			//init to user home directory
		    			sTheText = System.getProperty("user.home");
		    		}
	    			File aFile = new File(sTheText);
	    			sStartFile = aFile.getName();
	    			//create a new file only with the parent of the full path, that is the directory
	    			//with this dirty trick we separate the two part, file and folder
	    			//to grab the path only
	    			File aFileFolder = new File(aFile.getParent());
	    			URI aUri = aFileFolder.toURI();
	    			//then form the URL for the dialog
					sStartFolder = aUri.getScheme()+"://" + aUri.getPath();									    			
            	}
            	String aPath = aDlg.runOpenReadOnlyFileDialog(m_sDialogTitle, sStartFolder, sStartFile);
//the returned path is a URL, change into the system path
            	if(aPath.length() > 0) {
					String aFile = "";
					try {
						aFile = Helpers.fromURLtoSystemPath(aPath);
					} catch (URISyntaxException e) {
						m_aLogger.severe("actionPerformed", e);
					} catch (IOException e) {
						m_aLogger.severe("actionPerformed", e);
					}
	    			//grab the current control
	    		    xControl = ArrayOfControls[m_nBrowseSystemPath1ET].m_xTheControl;
	    	    	XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
	    	    			XPropertySet.class, xControl.getModel());
	    	    	if (xProp == null)
	    	    		throw new com.sun.star.uno.Exception(
	    	    				"Could not get XPropertySet from control.", this);
	    			xProp.setPropertyValue("Text", aFile);
            	}
            }
            else {
            	m_aLogger.info("Activated: "+sName);            	
            }
        }catch (com.sun.star.uno.Exception ex){
            // perform individual exception handling here.
            // Possible exception types are:
            // com.sun.star.lang.WrappedTargetException,
            // com.sun.star.beans.UnknownPropertyException,
            // com.sun.star.uno.Exception
        	m_aLogger.severe("", "", ex);
        }		
	}
}
