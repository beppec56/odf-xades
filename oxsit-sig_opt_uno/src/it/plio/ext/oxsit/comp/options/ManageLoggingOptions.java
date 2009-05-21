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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.ooo.ui.DialogFileOrFolderPicker;
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
public class ManageLoggingOptions extends ManageOptions  implements XItemListener {
	// needed for registration
	public static final String			m_sImplementationName	= ManageLoggingOptions.class.getName();
	public static final String[]		m_sServiceNames			= { "it.plio.ext.oxsit.test.options.ManageLoggingOptions" };

    private int m_nBrowseSystemPathPB = 0;
	private int m_nEnableFileCtl; // the file enable/disable check box
	//the following are the index inside ArrayOfControls of the controls that need to be
	//enabled/disabled according to the status of the file selection
	private int m_nLogFilePathIdxTF = 0;
	private int m_nLogFileSizeTF;
	private int m_nLogFileCountTF;

    private static final int m_nNumberOfControls = 8;
    
    private String	m_sDialogTitle = "id_opt_dlg_log_dir";

    /**
     * 
     * @param xCompContext
     */
	public ManageLoggingOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_logging");//leaf refers to OOo documentation about
															// extension options
//DEBUG	
		m_logger.enableLogging();// disabled in base class
		m_logger.ctor();
		//prepare the list of controls on the page

		//the parameter sName comes from basic dialog
		//the parameter sProperty comes from file AddonConfiguration.xcs.xml
		int iter = 0;
		//checkbox
		SingleControlDescription aControl = 
			new SingleControlDescription("CheckInfo", ControlTypeCode.CHECK_BOX, -1, "EnableInfoLevel", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//checkbox
		aControl = 
			new SingleControlDescription("CheckWarning", ControlTypeCode.CHECK_BOX, -1, "EnableWarningLevel", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("CheckEnConsole", ControlTypeCode.CHECK_BOX, -1, "EnableConsoleOutput", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("CheckEnFile", ControlTypeCode.CHECK_BOX, -1, "EnableFileOutput", 0, 0, true);
//add to control elements
		aControl.m_xAnItemListener = this;
		m_nEnableFileCtl = iter;
		ArrayOfControls[iter++] = aControl;
		aControl = 
				new	SingleControlDescription("LogFilePath", ControlTypeCode.EDIT_TEXT, -1, "LogFilePath", 0, 0, true);
		m_nLogFilePathIdxTF = iter;
		ArrayOfControls[iter++] = aControl;
//the actionPerformed pushbutton		
		aControl = 
			new SingleControlDescription("BrowseSystemPath", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseSystemPathPB = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;
//the file elements, counts and size
		aControl = 
			new	SingleControlDescription("LogFileSize", ControlTypeCode.EDIT_TEXT_INT, -1, "MaxFileSize", 100000, 1000, true );
//set the actionPerformed, for enable/disable
		//....
		m_nLogFileSizeTF = iter;
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new	SingleControlDescription("LogFileCount", ControlTypeCode.EDIT_TEXT_INT, -1, "FileRotationCount",100 , 1, true);
//set the actionPerformed, for enable/disable
		m_nLogFileCountTF = iter;
		ArrayOfControls[iter++] = aControl;
		
//grab the title string for configuration dialog
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xComponentContext, m_xMultiComponentFactory);
		try {
			m_sDialogTitle = m_aRegAcc.getStringFromRegistry( m_sDialogTitle );
		} catch (Exception e) {
			m_logger.severe("ctor",e);
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
//when return from load, we should have the container initialized, so activate the right state
		//for the subordinate controls
		
		if(m_xContainer != null) {
			//retrieve the file control checkbox
			XControl xControl = m_xContainer.getControl(ArrayOfControls[m_nEnableFileCtl].m_sControlName);
	        XControlModel xControlModel = xControl.getModel();
	        XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
// check the state and set a boolean accordingly
	        boolean bEnable = AnyConverter.toInt(xPSet.getPropertyValue("State")) == 1;
			enableTheFileControls(bEnable);
		}
		else
			m_logger.severe("enableTheFileControls", "there is no window!");
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
            if (sName.equals(ArrayOfControls[m_nBrowseSystemPathPB].m_sControlName)) {
            	m_logger.info("browse the system for a path");
//standard dialog for file/folder access
            	DialogFileOrFolderPicker aDlg = new DialogFileOrFolderPicker(m_xMultiComponentFactory,m_xComponentContext);
// we need to get the frame, the component context and from it the multiservice factory
            	//grab the current text value, expressed in system path
            	String sStartFolder = "";
            	{
	    		    xControl = ArrayOfControls[m_nLogFilePathIdxTF].m_xTheControl;
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
	    			//create a new file only with the parent of the full path, that is the directory
	    			//with this dirty trick we separate the two part, file and folder
	    			//to grab the path only
	    			File aFileFolder = new File(sTheText);
	    			URI aUri = aFileFolder.toURI();
	    			//then form the URL for the dialog
					sStartFolder = aUri.getScheme()+"://" + aUri.getPath();									    			
	    			m_logger.log(sStartFolder);
            	}
            	//call the dialog
            	String aPath = aDlg.runFolderPicker(m_sDialogTitle, sStartFolder);
            	//the returned path is a URL, change into the system path
            	if(aPath.length() > 0) {
					String aFile = "";
					try {
						aFile = Helpers.fromURLtoSystemPath(aPath);
					} catch (URISyntaxException e) {
						m_logger.severe("actionPerformed", e);
					} catch (IOException e) {
						m_logger.severe("actionPerformed", e);
					}
	    			//grab the current control
	    		    xControl = ArrayOfControls[m_nLogFilePathIdxTF].m_xTheControl;
	    	    	XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
	    	    			XPropertySet.class, xControl.getModel());
	    	    	if (xProp == null)
	    	    		throw new com.sun.star.uno.Exception(
	    	    				"Could not get XPropertySet from control.", this);
	    			xProp.setPropertyValue("Text", aFile);
            	}
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

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XItemListener#itemStateChanged(com.sun.star.awt.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent rIEvent) {
		// TODO Auto-generated method stub
		m_logger.entering("itemStateChanged");
        try{
            // get the control that has fired the event,
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, rIEvent.Source);
            XControlModel xControlModel = xControl.getModel();
            XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
            
//FIXME DEBUG            Utilities1.showProperties(xControlModel, xPSet);
            
            String sName = (String) xPSet.getPropertyValue("Name");
            // just in case the listener has been added to several controls,
            // we make sure we refer to the right one
            if (sName.equals(ArrayOfControls[m_nEnableFileCtl].m_sControlName)) {
//            	m_aLoggerDialog.info("check box of file changed state");
            	// retrieve the status of the control
                int nState = AnyConverter.toInt(xPSet.getPropertyValue("State"));
//FIXME DEBUg                m_aLoggerDialog.info("itemStateChanged","State is "+nState);
            	// if the control is active, enables the relevant controls else disable them            	
                enableTheFileControls(( nState == 0 ) ? false : true); 
            }
            else {
            	m_logger.info("Activated: "+sName);            	
            }
        } catch (com.sun.star.uno.Exception ex){
            // perform individual exception handling here.
            // Possible exception types are:
            // com.sun.star.lang.WrappedTargetException,
            // com.sun.star.beans.UnknownPropertyException,
            // com.sun.star.uno.Exception
        	m_logger.severe("", "", ex);
        }		
	}

	protected void enableOneFileControl(boolean _bEnable, int _index) {
		XControl xControl = m_xContainer.getControl(ArrayOfControls[_index].m_sControlName);
		ArrayOfControls[_index].m_bEnableSave = _bEnable; // enable the saving
        XControlModel xControlModel = xControl.getModel();
        XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
        try {
			xPSet.setPropertyValue("Enabled", new Boolean((_bEnable) ? true : false));
		} catch (UnknownPropertyException e) {
			// TODO Auto-generated catch block
			m_logger.severe("enableOneFileControl", "", e);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			m_logger.severe("enableOneFileControl", "", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			m_logger.severe("enableOneFileControl", "", e);
		} catch (WrappedTargetException e) {
			// TODO Auto-generated catch block
			m_logger.severe("enableOneFileControl", "", e);
		}
	}
	/**
	 * 
	 * @param _bEnable true enable the four controls the file checkbox enables/disables
	 */
	protected void enableTheFileControls(boolean _bEnable) {
// retrieve the controls
		//grab the current control
		if(m_xContainer != null) {
			enableOneFileControl(_bEnable,m_nLogFilePathIdxTF);
			enableOneFileControl(_bEnable,m_nBrowseSystemPathPB);
			enableOneFileControl(_bEnable,m_nLogFileCountTF);
			enableOneFileControl(_bEnable,m_nLogFileSizeTF);
		}
		else
			m_logger.severe("enableTheFileControls", "there is no window!");
	}
}
