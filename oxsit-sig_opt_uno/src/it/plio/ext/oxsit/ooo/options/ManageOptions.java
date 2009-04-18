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

package it.plio.ext.oxsit.ooo.options;

import it.plio.ext.oxsit.logging.XDynamicLogger;

import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * base class for options
 * Abstract class. The missing methods must be implemented by subclass 
 * @author beppe
 *
 */
public abstract class ManageOptions extends ComponentBase  implements
			XServiceInfo,
			XContainerWindowEventHandler,
			XActionListener {

	// needed for registration
    protected XComponentContext m_xComponentContext;
    protected XMultiComponentFactory m_xMultiComponentFactory;

	protected OptionsParametersAccess m_xOptionsConfigAccess;
	protected XDynamicLogger			m_logger;

    protected	SingleControlDescription[] ArrayOfControls = null;
    protected int m_nNumberOfControls = 0;

    //name of the single options page
    protected String m_sOptionPagename = "page_unused_for bugs";
    protected XControlContainer 	m_xContainer;
    
    /**
     * 
     * @param xCompContext
     */
	public ManageOptions(XComponentContext xCompContext, int _nNumberOfControls, String _sPageName) {
		m_xComponentContext = xCompContext;
		m_xMultiComponentFactory = m_xComponentContext.getServiceManager();                
		m_nNumberOfControls = _nNumberOfControls;
	    m_sOptionPagename = _sPageName;
		m_logger = new XDynamicLogger(this,m_xComponentContext);// must be enabled by the subclass (derived class)
		//prepare the list of controls on the single page
		// the key is the control name, there is a list every page,
		// when multiple pages, then a page hasjhmap of list of controls will be used
		
		if(m_nNumberOfControls > 0)
			ArrayOfControls = new SingleControlDescription[m_nNumberOfControls];

		//get configuration access, using standard registry functions
		m_xOptionsConfigAccess = new OptionsParametersAccess(xCompContext);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XContainerWindowEventHandler#callHandlerMethod(com.sun.star.awt.XWindow, java.lang.Object, java.lang.String)
	 */
	public boolean callHandlerMethod(XWindow xWindow, Object aEventObject, String sMethodName)
			throws WrappedTargetException {
		m_logger.entering("base callHandlerMethod");
        if (sMethodName.equals("external_event") ){
            try {
                return handleExternalEvent(xWindow, aEventObject);
            } catch (com.sun.star.uno.RuntimeException re) {
                throw re;
            } catch (com.sun.star.uno.Exception e) {
                throw new WrappedTargetException(sMethodName, this, e);
            }
        }            
        return false;
	}

	protected boolean handleExternalEvent(com.sun.star.awt.XWindow aWindow, Object aEventObject)
	throws com.sun.star.uno.Exception {
		try {
			String sMethod = AnyConverter.toString(aEventObject);
			if (sMethod.equals("ok")) {
				saveData(aWindow);
			} else if (sMethod.equals("back") || sMethod.equals("initialize")) {
				loadData(aWindow);
			}

		} catch (com.sun.star.lang.IllegalArgumentException e) {
			throw new com.sun.star.lang.IllegalArgumentException(
					"Method external_event requires a string in the event object argument.",
					this, (short) -1);
		}
		return true;
	}

	protected void saveData(com.sun.star.awt.XWindow aWindow)
	  				throws com.sun.star.lang.IllegalArgumentException, com.sun.star.uno.Exception {

		m_logger.entering("base saveData");
		  //Determine the name of the window. This serves two purposes. First, if this
		  //window is supported by this handler and second we use the name two locate
		  //the corresponding data in the registry.
		String sWindowName = getWindowName(aWindow);
		if (sWindowName == null)
		    throw new com.sun.star.lang.IllegalArgumentException(
		      "The window is not supported by this handler", this, (short) -1);
		 
		m_logger.info("page is: "+sWindowName);
		  //To acces the separate controls of the window we ne_theMethoded to obtain the
		  //XControlContainer from window implementation
		XControlContainer xContainer = (XControlContainer) UnoRuntime.queryInterface(
		    XControlContainer.class, aWindow);
		if (xContainer == null)
		    throw new com.sun.star.uno.Exception(
		      "Could not get XControlContainer from window.", this);

		  m_logger.log("saveData", "examine "+ArrayOfControls.length+" controls");
			//from the current window, scan the contained controls, then for every control
			//access the data and save them
		  for (int i = 0; i < ArrayOfControls.length; i++) {
		    //load the values from the registry

			//grab the current control
		    XControl xControl = xContainer.getControl(ArrayOfControls[i].m_sControlName);

		    if (xControl == null) {
		    	m_logger.info("control: "+ArrayOfControls[i].m_sControlName+" not found in window page.");
		      continue;
		    }
//	    	printlnName("control: "+ArrayOfControls[i].m_sControlName+" to save found in window page.");
		 
		    //From the control we get the model, which in turn supports the
		    //XPropertySet interface, which we finally use to set the data at the
		    //control
	    	XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
	    			XPropertySet.class, xControl.getModel());
		 
	    	if (xProp == null)
	    		throw new com.sun.star.uno.Exception(
	    				"Could not get XPropertySet from control.", this);

		    m_logger.log("saveData", "examine "+ArrayOfControls[i].m_sControlName+" control");
		    //now we need to set the property according to the control type
	    	switch(ArrayOfControls[i].m_eTheCode) {
	    	case EDIT_TEXT_INT:
	    		//call the get method and assign the text to the control
//	    		Utilities.showProperties(xProp);
	    		//get the value of the text
	    		String snTheText = 
	    			AnyConverter.toString( xProp.getPropertyValue( "Text" ) );
	    		//check for limits
	    		int nValue = Integer.valueOf(snTheText).intValue();
	    		if( nValue < ArrayOfControls[i].m_nMinValue)
	    			nValue = ArrayOfControls[i].m_nMinValue;
	    		else if(nValue > ArrayOfControls[i].m_nMaxValue)
	    			nValue = ArrayOfControls[i].m_nMaxValue;
	    		//save it
	    		if(ArrayOfControls[i].m_bEnableSave)
	    			m_xOptionsConfigAccess.setNumber(ArrayOfControls[i].m_sPropertyName,
	    					nValue);
	    		break;
	    	case EDIT_TEXT:
	    		//call the get method and assign the text to the control
//	    		Utilities.showProperties(xProp);
	    		//get the value of the text
	    		String sTheText = 
	    			AnyConverter.toString( xProp.getPropertyValue( "Text" ) );
	    		//save it
	    		if(ArrayOfControls[i].m_bEnableSave)
	    			m_xOptionsConfigAccess.setText(ArrayOfControls[i].m_sPropertyName,sTheText);
	    		break;
	    	case CHECK_BOX:
	    		//in this case we need the boolean property of the control
//	    		Utilities.showProperties(xProp);
	    		short nState = AnyConverter.toShort(xProp.getPropertyValue( "State" ));
	    		if(ArrayOfControls[i].m_bEnableSave)
	    			m_xOptionsConfigAccess.setBoolean(ArrayOfControls[i].m_sPropertyName,
	    					(nState == 0) ? false : true);	    		
	    		break;
	    	case RADIO_BUTTON:
	    		//this is different, so...
//	    		Utilities.showProperties(xProp);
	    		//...we grab the value from the registry, an integer
	    		//grab the status, when we find the selected one, set the vaue in the option.
	    		short nRbState = AnyConverter.toShort(xProp.getPropertyValue( "State" ));
	    		if( nRbState == 1 && ArrayOfControls[i].m_bEnableSave) {// set the option
	    			m_xOptionsConfigAccess.setNumber(ArrayOfControls[i].m_sPropertyName, ArrayOfControls[i].m_nTheRadioButtonPosition);
	    		}
	      		break;
	    	case PUSH_BUTTON:
	    		//do nothing the button don't save values
	    		break;
	    	default:
	    		//do nothing
	    	}	    
		  }
//		Utilities.showInfo(m_xOptionsConfigAccess);

		//commit changes
		m_xOptionsConfigAccess.commit();
	}

	/**
	 * 
	 * @param aWindow
	 * @throws com.sun.star.uno.Exception
	 */
	protected void loadData(com.sun.star.awt.XWindow aWindow)
	  throws com.sun.star.uno.Exception {

		m_logger.entering("base loadData");
	  //Determine the name of the window. This serves two purposes. First, if this
	  //window is supported by this handler and second we use the name two locate
	  //the corresponding data in the registry.
		//Determine the name of the window. This serves two purposes. First, if this
		//window is supported by this handler and second we use the name two locate
		//the corresponding data in the registry.
		String sWindowName = getWindowName(aWindow);
		if (sWindowName == null)
			throw new com.sun.star.lang.IllegalArgumentException(
					"The window is not supported by this handler", this, (short) -1);
	 
	  //To acces the separate controls of the window we need to obtain the
	  //XControlContainer from window implementation
		m_xContainer = (XControlContainer) UnoRuntime.queryInterface(
	    XControlContainer.class, aWindow);
	  if (m_xContainer == null)
	    throw new com.sun.star.uno.Exception(
	      "Could not get XControlContainer from window.", this);
	  
	  //iterate through the controls on this page and
	  //for each grab the parameter and set the value
	  m_logger.log("loadData", "examine "+ArrayOfControls.length+" controls");
	  for (int i = 0; i < ArrayOfControls.length; i++) {
	    //load the values from the registry
		  
		//grab the current control
	    XControl xControl = m_xContainer.getControl(ArrayOfControls[i].m_sControlName);

	    if (xControl == null) {
	    	m_logger.info("control: "+ArrayOfControls[i].m_sControlName+" not found in window page.");
	      continue;
	    }
	    
    //	printlnName("control: "+ArrayOfControls[i].m_sControlName+" found in window page.");
	 
	    //From the control we get the model, which in turn supports the
	    //XPropertySet interface, which we finally use to set the data at the
	    //control
    	XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
    			XPropertySet.class, xControl.getModel());

    	if (xProp == null)
    		throw new com.sun.star.uno.Exception(
    				"Could not get XPropertySet from control.", this);

	    m_logger.log("loadData", "examine "+ArrayOfControls[i].m_sControlName+" control");
	    //now we need to set the property according to the control type
//check if there is an action listener to associate with the control    	
		//change the event function of the button, to see
		//if we can intercept the behavior
    	switch(ArrayOfControls[i].m_eTheCode) {
    	case EDIT_TEXT_INT:
    		//call the get method and assign the text to the control
    		int nValue = m_xOptionsConfigAccess.getNumber(ArrayOfControls[i].m_sPropertyName);
    		//This handler supports only text controls, which are named "Pattern Field"
    		//in the dialog editor. We set the "Text" property.
   			xProp.setPropertyValue("Text", Integer.toString(nValue));
    		break;
    	case EDIT_TEXT:
    		//call the get method and assign the text to the control
    		String sValue = m_xOptionsConfigAccess.getText(ArrayOfControls[i].m_sPropertyName);
    		//This handler supports only text controls, which are named "Pattern Field"
    		//in the dialog editor. We set the "Text" property.
    		if(sValue != null)
    			xProp.setPropertyValue("Text", sValue);
    		break;
    	case CHECK_BOX:
    		//in this case we need the boolean property of the control
    		xProp.setPropertyValue("State",
    				new Short(
    						m_xOptionsConfigAccess.getBoolean(ArrayOfControls[i].m_sPropertyName) ?
    								(short)1 : (short)0));
    		if(ArrayOfControls[i].m_xAnItemListener != null) {
    			XControl xCbControl = m_xContainer.getControl( ArrayOfControls[i].m_sControlName );
    			XCheckBox xCb = (XCheckBox) UnoRuntime.queryInterface( XCheckBox.class, xCbControl );
    		// 	An ActionListener will be notified on the activation of the
    		// button...
    			if(xCb != null )
    				xCb.addItemListener(ArrayOfControls[i].m_xAnItemListener );
    		}        	
    		break;
    	case RADIO_BUTTON:
    		//this is different, so...
    		//...we grab the value from the registry, an integer
    		int	nState = m_xOptionsConfigAccess.getNumber(ArrayOfControls[i].m_sPropertyName);
    		int nThisControlState =  ArrayOfControls[i].m_nTheRadioButtonPosition;
       		//the we set on the radio button if the value in the registry is the same as
    		// the value in ArrayOfControls[i].m_nTheRadioButtonPosition
    		// else the radio button is turned off
    		Short aShort = new Short( ( nState == nThisControlState ) ?
    										(short)1 : (short)0);
    		xProp.setPropertyValue("State", aShort);
      		break;
     	case PUSH_BUTTON:
    		if(ArrayOfControls[i].m_xAnActionListener != null) {
    			XControl xButtonControl = m_xContainer.getControl( ArrayOfControls[i].m_sControlName );
    			XButton xButton = (XButton) UnoRuntime.queryInterface( XButton.class, xButtonControl );
    		// 	An ActionListener will be notified on the activation of the
    		// button...
    			if(xButton != null ) {
    				xButton.addActionListener( ArrayOfControls[i].m_xAnActionListener );
    			}
    		}        	
    		break;
    	default:
    		//do nothing
    	}	    
	  }
	}

    //Checks if the name property of the window is one of the supported names and returns
    //always a valid string or null
	protected String getWindowName(com.sun.star.awt.XWindow aWindow)
	throws com.sun.star.uno.Exception {
		m_logger.entering("base getWindowName, check against "+m_sOptionPagename);

		if (aWindow == null)
			new com.sun.star.lang.IllegalArgumentException(
					"Method external_event requires that a window is passed as argument",
					this, (short) -1);

		//We need to get the control model of the window. Therefore the first step is
		//to query for it.
		XControl xControlDlg = (XControl) UnoRuntime.queryInterface(
				XControl.class, aWindow);

		if (xControlDlg == null)
			throw new com.sun.star.uno.Exception(
					"Cannot obtain XControl from XWindow in method external_event.");
		//Now get model
		XControlModel xModelDlg = xControlDlg.getModel();

		if (xModelDlg == null)
			throw new com.sun.star.uno.Exception(
					"Cannot obtain XControlModel from XWindow in method external_event.", this);
		//The model itself does not provide any information except that its
		//implementation supports XPropertySet which is used to access the data.
		XPropertySet xPropDlg = (XPropertySet) UnoRuntime.queryInterface(
				XPropertySet.class, xModelDlg);
		if (xPropDlg == null)
			throw new com.sun.star.uno.Exception(
					"Cannot obtain XPropertySet from window in method external_event.", this);

		//Get the "Name" property of the window
		Object aWindowName = xPropDlg.getPropertyValue("Name");

		//Get the string from the returned com.sun.star.uno.Any
		String sName = null;
		try {
			sName = AnyConverter.toString(aWindowName);
		} catch (com.sun.star.lang.IllegalArgumentException e) {
			throw new com.sun.star.uno.Exception(
					"Name - property of window is not a string.", this);
		}

		m_logger.log("getWindowName","the window name is "+sName);
		if(sName.equals(m_sOptionPagename))
			return sName;

		return null;
	} 

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XContainerWindowEventHandler#getSupportedMethodNames()
	 */
	public String[] getSupportedMethodNames() {
		return new String[] {"external_event"};
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(EventObject arg0) {
		// TODO Auto-generated method stub
		m_logger.entering("disposing");				
	}
	
}
