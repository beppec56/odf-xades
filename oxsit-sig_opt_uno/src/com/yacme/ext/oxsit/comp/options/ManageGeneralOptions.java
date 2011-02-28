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

package com.yacme.ext.oxsit.comp.options;


import java.io.File;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XItemListener;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.ooo.ui.DialogAbout;
import com.yacme.ext.oxsit.options.SingleControlDescription;
import com.yacme.ext.oxsit.options.SingleControlDescription.ControlTypeCode;

/**
 * 
 * manages the General option page on Tools > Options...
 * 
 * @author beppe
 *
 */
public class ManageGeneralOptions extends ManageOptions  implements XItemListener {

	// needed for registration
	public static final String			m_sImplementationName	= ManageGeneralOptions.class.getName();
	public static final String[]		m_sServiceNames			= { "com.yacme.ext.oxsit.comp.options.ManageGeneralOptions" };

    private int m_nAboutButton = 0;
    private int m_nClearCRLCacheButton = 0;
    private int m_nDisableCRLControlCB = 0;
    private int m_nForceCRLDownloadCB = 0;

    private static final int m_nNumberOfControls = 6;

    /**
     * 
     * @param xCompContext
     */
	public ManageGeneralOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_general");
//		m_aLogger.enableLogging();// disabled in base class
		m_aLogger.ctor();
		//prepare the list of controls on the page

		int iter = 0;
		SingleControlDescription aControl;
//checkbox
		aControl = 
			new SingleControlDescription("OffLineCB", ControlTypeCode.CHECK_BOX, -1, "OperationOffLine", 0, 0, true);
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("DisableOCSPCB", ControlTypeCode.CHECK_BOX, -1, "DisableOCSPControl", 0, 0, true);
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("DisableCRLCB", ControlTypeCode.CHECK_BOX, -1, "DisableCRLControl", 0, 0, true);
		m_nDisableCRLControlCB = iter;
		aControl.m_xAnItemListener = this;
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("ForceCRLDownloadCB", ControlTypeCode.CHECK_BOX, -1, "ForceDownloadCRL", 0, 0, true);
		m_nForceCRLDownloadCB = iter;
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("ClearCRLCachePB", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nClearCRLCacheButton = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;
		
		aControl = 
			new SingleControlDescription("AboutButton", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nAboutButton = iter;
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

	@Override
	protected void loadData(com.sun.star.awt.XWindow aWindow)
	  throws com.sun.star.uno.Exception {
		super.loadData(aWindow);
//when return from load, we should have the container initialized, so activate the right state
		//for the subordinate controls
		
		if(m_xContainer != null) {
			//retrieve the file control checkbox
			XControl xControl = m_xContainer.getControl(ArrayOfControls[m_nDisableCRLControlCB].m_sControlName);
	        XControlModel xControlModel = xControl.getModel();
	        XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
// check the state and set a boolean accordingly
	        boolean bDisable = AnyConverter.toInt(xPSet.getPropertyValue("State")) == 1;
			enableTheCRLControls(!bDisable);
		}
		else
			m_aLogger.severe("enableTheFileControls", "there is no window!");
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
            else if (sName.equals(ArrayOfControls[m_nClearCRLCacheButton].m_sControlName)) {
            	m_aLogger.info("clear the CRL cache");
            	//
            	String sCRLCachePath = Helpers.getCRLCacheSystemPath(m_xComponentContext);
            	File aCacheDir = new File(sCRLCachePath);
            	if(aCacheDir.exists()) {
            		if(aCacheDir.isFile()) {
            			//wrong ! tell the user
            			
            		}
            		else {//is a directory, get the file list
            			String[] sDirContent = aCacheDir.list();
            			if(sDirContent != null) {
	            			for(int i = 0; i< sDirContent.length;i++) {
	            				File aFile = new File(
	            						aCacheDir+System.getProperty("file.separator")+
	            						sDirContent[i]
	            						);
	            				aFile.delete();
	            			}
            			}
            		}
            	}
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
        } catch (Throwable ex){
	        /* perform individual exception handling here.
	         * Possible exception types are:
	         * com.sun.star.lang.WrappedTargetException,
	         * com.sun.star.beans.UnknownPropertyException,
	         * com.sun.star.uno.Exception
	         */
        	m_aLogger.severe("", "", ex);
        }
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XItemListener#itemStateChanged(com.sun.star.awt.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent rIEvent) {
		m_aLogger.entering("itemStateChanged");
        try{
            // get the control that has fired the event,
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, rIEvent.Source);
            XControlModel xControlModel = xControl.getModel();
            XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
            
//FIXME DEBUG            Utilities1.showProperties(xControlModel, xPSet);
            
            String sName = (String) xPSet.getPropertyValue("Name");
            // just in case the listener has been added to several controls,
            // we make sure we refer to the right one
            if (sName.equals(ArrayOfControls[m_nDisableCRLControlCB].m_sControlName)) {
//            	m_aLoggerDialog.info("check box of file changed state");
            	// retrieve the status of the control
                int nState = AnyConverter.toInt(xPSet.getPropertyValue("State"));
//FIXME DEBUg                m_aLoggerDialog.info("itemStateChanged","State is "+nState);
            	// if the control is active, enables the relevant controls else disable them            	
                enableTheCRLControls(( nState == 0 ) ? true : false); 
            }
            else {
            	m_aLogger.info("Activated: "+sName);            	
            }
        } catch (com.sun.star.uno.Exception ex){
            // perform individual exception handling here.
            // Possible exception types are:
            // com.sun.star.lang.WrappedTargetException,
            // com.sun.star.beans.UnknownPropertyException,
            // com.sun.star.uno.Exception
        	m_aLogger.severe("", "", ex);
        }		
		
	}

	/**
	 * 
	 * @param _bEnable true enable the four controls the file checkbox enables/disables
	 */
	private void enableTheCRLControls(boolean _bEnable) {
	// retrieve the controls
			//grab the current control
			if(m_xContainer != null) {
				enableOneFileControl(_bEnable,m_nForceCRLDownloadCB);
			}
			else
				m_aLogger.severe("enableTheFileControls", "there is no window!");		
	}

	/**
	 * @param enable
	 * @param forceCRLDownloadCB
	 */
	private void enableOneFileControl(boolean _bEnable, int _index) {
		// TODO Auto-generated method stub
		XControl xControl = m_xContainer.getControl(ArrayOfControls[_index].m_sControlName);
		ArrayOfControls[_index].m_bEnableSave = _bEnable; // enable the saving
        XControlModel xControlModel = xControl.getModel();
        XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
        try {
			xPSet.setPropertyValue("Enabled", new Boolean((_bEnable) ? true : false));
		} catch (UnknownPropertyException e) {
			m_aLogger.severe("enableOneFileControl", "", e);
		} catch (PropertyVetoException e) {
			m_aLogger.severe("enableOneFileControl", "", e);
		} catch (IllegalArgumentException e) {
			m_aLogger.severe("enableOneFileControl", "", e);
		} catch (WrappedTargetException e) {
			m_aLogger.severe("enableOneFileControl", "", e);
		} catch (Throwable e) {
			m_aLogger.severe("enableOneFileControl", "", e);
		}
	}
}
