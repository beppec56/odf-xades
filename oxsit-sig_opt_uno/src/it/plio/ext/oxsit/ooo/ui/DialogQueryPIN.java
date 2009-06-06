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

package it.plio.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.PKCS11TokenAttributes;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 * 
 */
public class DialogQueryPIN extends BasicDialog {

	private static final String	DLG_QUERY_PIN_NAME	= "querypindlg";
	private XWindowPeer			m_xParentWindow	= null;
	private String				m_sTitle;
	private String				m_sBtnOKLabel;
	private String				m_sBtnCancLabel;
	private static final String				m_sCONFIRM_PB = "confirm";
	
	private	String				m_sThePin = "";
	private Object 				m_oEdit;
	private String m_sEditField = "editfld";
	private String				m_sPinError = "";
	private String				m_sPinCharOnly = "id_mex_err_only_num";
	private int					m_nMaxPinLen = 0;
	private	char[]				m_cPin;
	
	private PKCS11TokenAttributes	m_aTokenAttributes;
	private String m_sTokenDescriptionFormat = "id_mex_pin_token";
	private String	m_sTokenDescriptioMessage;
	private String	m_sMaxPINLenFormat = "id_mex_max_pin_len";

	public DialogQueryPIN(XFrame _xFrame, XComponentContext context,
			XMultiComponentFactory _xmcf, PKCS11TokenAttributes _Token) {
		super( _xFrame, context, _xmcf );
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);
		m_aTokenAttributes = _Token;

		m_aLogger.enableLogging();

		try {
			m_sTitle = m_aRegAcc.getStringFromRegistry( "id_pin_title" );
			m_sBtnOKLabel = m_aRegAcc.getStringFromRegistry( "id_ok" );
			m_sBtnCancLabel = m_aRegAcc.getStringFromRegistry( "id_cancel" );
			m_sPinCharOnly = m_aRegAcc.getStringFromRegistry( m_sPinCharOnly );
			m_sTokenDescriptionFormat = m_aRegAcc.getStringFromRegistry( m_sTokenDescriptionFormat );
			m_sMaxPINLenFormat = m_aRegAcc.getStringFromRegistry( m_sMaxPINLenFormat );
		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("", "", e);
		}
		if(_Token == null)
			_Token = new PKCS11TokenAttributes();
		m_nMaxPinLen = (int)_Token.getMaxPinLen();
		m_sTokenDescriptioMessage = String.format(m_sTokenDescriptionFormat, 
				_Token.getLabel(),
				_Token.getModel(),
				_Token.getSerialNumber(),
				_Token.getMaxPinLen());
		m_aRegAcc.dispose();
	}
	
	public void initialize(int _nPosX, int _nPosY) throws Exception {
		initialize( m_xParentWindow, _nPosX, _nPosY );
	}

	public void initialize(XWindowPeer _xParentWindow, int _nPosX, int _nPosY)
			throws Exception {

		super.initialize( DLG_QUERY_PIN_NAME, m_sTitle, PINDlgDims.DS_HEIGHT(), PINDlgDims.DLGS_WIDTH(), _nPosX, _nPosY );
//set white backgroung
//we need to set the property BackgroundColor del modello della dialog
        // From the control we get the model, which in turn supports the
        // XPropertySet interface, which we finally use to get the data from
        // the control.
 /*       XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
          XPropertySet.class, m_xDialogControl.getModel());

        if (xProp != null)
        	xProp.setPropertyValue(new String("BackgroundColor"),
        		new Integer(ControlDims.DLG_ABOUT_BACKG_COLOR));*/
		
		//insert a fixed text for message

		int _nPosButton = PINDlgDims.DS_HEIGHT() - ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 4 / 3;
		int _nPosEdit   = _nPosButton - ControlDims.RSC_CD_PUSHBUTTON_HEIGHT-ControlDims.RSC_SP_CTRL_X;
		m_xParentWindow = _xParentWindow;

			m_oEdit = insertEditFieldModel( this, /*this*/null, 
					(PINDlgDims.DLGS_WIDTH() - PINDlgDims.ED_WIDTH()) /2,
/*					PINDlgDims.DLGS_WIDTH() - PINDlgDims.ED_WIDTH() -
					 ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT,*/
					 _nPosEdit,
//					 ControlDims.RSC_SP_DLG_INNERBORDER_BOTTOM,
					 ControlDims.RSC_CD_EDIT_FIELD_HEIGHT,
					 PINDlgDims.ED_WIDTH(), 0, "", m_sEditField,
				false,
				false,
				false,	//Vscroll, should be auto
				false );//HScroll
//properties of edit ctrl
/*		XPropertySet xPSet = (XPropertySet) UnoRuntime
		.queryInterface( XPropertySet.class, m_oEdit );
		Utilities.showProperties(this,xPSet);*/

//now set some properties of the edit control:
		XMultiPropertySet xMPSet = (XMultiPropertySet) UnoRuntime
								.queryInterface( XMultiPropertySet.class, m_oEdit );

		/*Utilities.showProperties(this, xMPSet);*/
		// Set the properties at the model - keep in mind to pass the
		// property names in alphabetical order!
		xMPSet.setPropertyValues( new String[] { 
				"BackgroundColor",
				"EchoChar"}, new Object[] { 
				new Integer( ControlDims.DLG_ABOUT_BACKG_COLOR ),
				new Short((short)0x2a) } );
		
		// OK button
		int _PosXOK = ((PINDlgDims.DLGS_WIDTH() - (ControlDims.RSC_CD_PUSHBUTTON_WIDTH*2+ControlDims.RSC_SP_CTRL_DESC_X))) / 2;
		insertButton( this,
				_PosXOK,
/*						( PINDlgDims.DLGS_WIDTH() - ControlDims.RSC_CD_PUSHBUTTON_WIDTH -
								ControlDims.RSC_SP_CTRL_DESC_X -
								ControlDims.RSC_CD_PUSHBUTTON_WIDTH - 
								ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT), 
*/
						// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, m_sCONFIRM_PB, m_sBtnOKLabel,
				(short) PushButtonType.STANDARD_value );

		// Cancel button
		insertButton( this,
				_PosXOK+ControlDims.RSC_SP_CTRL_DESC_X+ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
				// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, "cancb", m_sBtnCancLabel,
				(short) PushButtonType.CANCEL_value );

		insertFixedText(this, 
				ControlDims.RSC_SP_DLG_INNERBORDER_BOTTOM,
				ControlDims.RSC_SP_DLG_INNERBORDER_LEFT,
				80,
				PINDlgDims.DLGS_WIDTH()-ControlDims.RSC_SP_DLG_INNERBORDER_LEFT-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT,
				0, m_sTokenDescriptioMessage,"fixmex");
		
		xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class,
				super.m_xDialogControl );
		createWindowPeer();
		// center the dialog, using physical coordinates, MUST be called after
		// CreateWindowPeer
//		center();
	}

	@Override
	public void actionPerformed(ActionEvent rEvent) {
		try {
			// get the control that fired the event,
			XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class,
					rEvent.Source);
			XControlModel xControlModel = xControl.getModel();
			XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(
					XPropertySet.class, xControlModel);
			String sName = (String) xPSet.getPropertyValue("Name");
			// just in case the listener has been added to several controls,
			// we make sure we refer to the right one
			if (sName.equals(m_sCONFIRM_PB)) {
				//show the license...
				//get text from control
				
				XControlContainer xContainer = (XControlContainer) UnoRuntime.queryInterface(
					    XControlContainer.class, m_xDialogControl);
					if (xContainer == null)
					    throw new com.sun.star.uno.Exception(
					      "Could not get XControlContainer from window.", this);

					  m_aLogger.log("actionPerformed", "examine  control");
						//from the current window, scan the contained controls, then for every control
						//access the data and save them
					    //load the values from the registry
						//grab the current control
					    xControl = xContainer.getControl(m_sEditField);
				
		    	XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
		    			XPropertySet.class, xControl.getModel());
			 
		    	if (xProp == null)
		    		throw new com.sun.star.uno.Exception(
		    				"Could not get XPropertySet from control.", this);
	    		String sThePin = 
	    			AnyConverter.toString( xProp.getPropertyValue( "Text" ) );

	    		if(sThePin.length() == 0)
	    			return;
	    		if(sThePin.length() > m_nMaxPinLen) {
	    			String mex = String.format(m_sMaxPINLenFormat, m_nMaxPinLen);
                    MessageError	aMex = new MessageError(null,m_xMCF,m_xContext);
                    aMex.executeDialogLocal(mex);
    				setThePin("");	    			
	    			return;
	    		}
				//check if it's composed only of numbers
	    		//FIXME: force charset to std iso
	    		byte[] thes = sThePin.getBytes();
				boolean bOk = true;
				for(int i= 0; i <thes.length; i++) {
					if(thes[i] < 0x30 || thes[i] > 0x39) {
						bOk = false;
						break;
					}
				}
				if(!bOk) {
					//no, alert with a dialog, then exits
                    //give the user some feedback
                    MessageError	aMex = new MessageError(null,m_xMCF,m_xContext);
                    aMex.executeDialogLocal(m_sPinCharOnly);
    				setThePin("");
					return;
				}
				//yes, transfer to the internal var and exit.
				setThePin(sThePin);

	            char[] p = new char[sThePin.length()];
	            for (int i = 0; i < sThePin.length(); i++) {
	                p[i] = sThePin.charAt(i);
	            }
	            setPin(p);
				endDialog();
			}
		} catch (com.sun.star.uno.Exception ex) {
			/*
			 * perform individual exception handling here. Possible exception
			 * types are: com.sun.star.lang.WrappedTargetException,
			 * com.sun.star.beans.UnknownPropertyException,
			 * com.sun.star.uno.Exception
			 */
			ex.printStackTrace();
		}
	}

	/**
	 * @param m_sThePin the m_sThePin to set
	 */
	public void setThePin(String m_sThePin) {
		this.m_sThePin = m_sThePin;
	}

	/**
	 * @return the m_sThePin
	 */
	public String getThePin() {
		return m_sThePin;
	}

	/**
	 * @param m_cPin the m_cPin to set
	 */
	public void setPin(char[] m_cPin) {
		this.m_cPin = m_cPin;
	}

	/**
	 * @return the m_cPin
	 */
	public char[] getPin() {
		return m_cPin;
	}


}

