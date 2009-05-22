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

import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 * 
 */
public class DialogRootVerify extends BasicDialog {

	private static final String	DLG_ROOT_VERIFY_NAME	= "rootverifdlg";
	private XWindowPeer			m_xParentWindow	= null;
	private String				m_sTitle =  "id_root_verify_message_title" ;
	private String				m_sMessage;
	private String				m_sBtnYesLabel = "id_pb_yes";
	private String				m_sBtnNoLabel = "id_pb_no";
	private String 				m_sNO_PB = "theNoPb";
	private String 				m_sYES_PB = "theYesPb";

	public DialogRootVerify(XFrame _xFrame, XComponentContext context,
			XMultiComponentFactory _xmcf, String _Message) {
		super ( _xFrame, context, _xmcf );
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);
		m_sMessage = _Message;				

		try {
			m_sTitle = m_aRegAcc.getStringFromRegistry( m_sTitle );
			m_sBtnYesLabel = m_aRegAcc.getStringFromRegistry( m_sBtnYesLabel );
			m_sBtnNoLabel = m_aRegAcc.getStringFromRegistry( m_sBtnNoLabel );
		} catch (com.sun.star.uno.Exception e) {
			m_logger.severe("", "", e);
		}
		m_aRegAcc.dispose();
	}

	public void initialize(int _nPosX, int _nPosY) throws Exception {
		initialize( m_xParentWindow, _nPosX, _nPosY );
	}

	public void initialize(XWindowPeer _xParentWindow, int _nPosX, int _nPosY)
			throws Exception {

		super.initialize( DLG_ROOT_VERIFY_NAME, m_sTitle, ControlDims.DLG_ROOT_VERIFY_HEIGH, ControlDims.DLG_ROOT_VERIFY_WIDTH, _nPosX, _nPosY );
//set white backgroung
//we need to set the property BackgroundColor del modello della dialog
        // From the control we get the model, which in turn supports the
        // XPropertySet interface, which we finally use to get the data from
        // the control.
        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
          XPropertySet.class, m_xDialogControl.getModel());

        if (xProp != null)
        	xProp.setPropertyValue(new String("BackgroundColor"),
        		new Integer(ControlDims.DLG_ROOT_VERIFY_BACKG_COLOR));

		int _nPosButton = ControlDims.DLG_ROOT_VERIFY_HEIGH - ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 4 / 3;
		m_xParentWindow = _xParentWindow;

		// No button
		insertButton( this,
				( ControlDims.DLG_ROOT_VERIFY_WIDTH - ControlDims.RSC_CD_PUSHBUTTON_WIDTH - ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT), //right
				// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, m_sNO_PB, m_sBtnNoLabel,
				(short) PushButtonType.CANCEL_value );

		// The yes button
		insertButton( this,
				( ControlDims.DLG_ROOT_VERIFY_WIDTH - ControlDims.RSC_CD_PUSHBUTTON_WIDTH -
						ControlDims.RSC_SP_CTRL_DESC_X -
						ControlDims.RSC_CD_PUSHBUTTON_WIDTH - 
						ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT), 
				// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, m_sYES_PB, m_sBtnYesLabel,
				(short) PushButtonType.OK_value );

		Object oEdit = insertEditFieldModel( this, /*this*/null, ControlDims.RSC_SP_DLG_INNERBORDER_LEFT,
				ControlDims.RSC_SP_DLG_INNERBORDER_TOP, ControlDims.DLG_ROOT_VERIFY_HEIGH
						- ( ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 2 ), ControlDims.DLG_ROOT_VERIFY_WIDTH
						- ControlDims.RSC_SP_DLG_INNERBORDER_LEFT
						- ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT, 0, m_sMessage, "cyx",
				true,
				true,
				false,	//Vscroll, should be auto
				false );//HScroll
//properties of edit ctrl
/*		XPropertySet xPSet = (XPropertySet) UnoRuntime
		.queryInterface( XPropertySet.class, oEdit );
		Utilities.showProperties(this,xPSet);*/

//now set some properties of the edit control:
		XMultiPropertySet xMPSet = (XMultiPropertySet) UnoRuntime
								.queryInterface( XMultiPropertySet.class, oEdit );

		/*Utilities.showProperties(this, xMPSet);*/
		// Set the properties at the model - keep in mind to pass the
		// property names in alphabetical order!
		xMPSet.setPropertyValues( new String[] { 
				"AutoHScroll", 
				"AutoVScroll", 
				"BackgroundColor",
				"Border" }, new Object[] { 
				new Boolean( true ),
				new Boolean( true ),
				new Integer( ControlDims.DLG_ROOT_VERIFY_BACKG_COLOR ),
				new Short( (short)0 ) } );

		xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class,
				super.m_xDialogControl );
		createWindowPeer();
		// center the dialog, using physical coordinates, MUST be called after
		// CreateWindowPeer
		center();
	}
}

