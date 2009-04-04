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

package it.plio.ext.xades.ooo.ui;

import it.plio.ext.xades.ooo.registry.MessageConfigurationAccess;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 * 
 */
public class DialogAbout extends BasicDialog {

	private static final String	DLG_ABOUT_NAME	= "aboutdlg";
	private static final String SHOW_LICENSE_PB = "show_license";
	private XWindowPeer			m_xParentWindow	= null;
	private String				m_sTitle;
	private String				m_sMessage;
	private String				m_sBtnOKLabel;
	private String				m_sShowLicense;

	public DialogAbout(XFrame _xFrame, XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super( _xFrame, context, _xmcf );
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);

		try {
			m_sTitle = m_aRegAcc.getStringFromRegistry( "id_about_title" );
			m_sMessage = m_aRegAcc.getStringFromRegistry( "id_about_mex" ) + "\n" + m_aRegAcc.getStringFromRegistry( "id_credits" );				
			m_sBtnOKLabel = m_aRegAcc.getStringFromRegistry( "id_ok" );
			m_sShowLicense = m_aRegAcc.getStringFromRegistry( "id_show_license" );
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace();
		}
		m_aRegAcc.dispose();
	}

	public void initialize(int _nPosX, int _nPosY) throws Exception {
		initialize( m_xParentWindow, _nPosX, _nPosY );
	}

	public void initialize(XWindowPeer _xParentWindow, int _nPosX, int _nPosY)
			throws Exception {

		super.initialize( DLG_ABOUT_NAME, m_sTitle, ControlDims.DLG_ABOUT_HEIGH, ControlDims.DLG_ABOUT_WIDTH, _nPosX, _nPosY );
//set white backgroung
//we need to set the property BackgroundColor del modello della dialog
        // From the control we get the model, which in turn supports the
        // XPropertySet interface, which we finally use to get the data from
        // the control.
        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
          XPropertySet.class, m_xDialogControl.getModel());

        if (xProp != null)
        	xProp.setPropertyValue(new String("BackgroundColor"),
        		new Integer(ControlDims.DLG_ABOUT_BACKG_COLOR));

		int _nPosButton = ControlDims.DLG_ABOUT_HEIGH - ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 4 / 3;
		m_xParentWindow = _xParentWindow;

		// cancel button
		insertButton( this,
				( ControlDims.DLG_ABOUT_WIDTH - ControlDims.RSC_CD_PUSHBUTTON_WIDTH -
						ControlDims.RSC_SP_CTRL_DESC_X -
						ControlDims.RSC_CD_PUSHBUTTON_WIDTH - 
						ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT), 
				// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, "okb", m_sBtnOKLabel,
				(short) PushButtonType.OK_value );

		// display license button
		insertButton( this,
				( ControlDims.DLG_ABOUT_WIDTH - ControlDims.RSC_CD_PUSHBUTTON_WIDTH - ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT), //right
				// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, SHOW_LICENSE_PB, m_sShowLicense,
				(short) PushButtonType.STANDARD_value );

		Object oEdit = insertEditFieldModel( this, this, ControlDims.RSC_SP_DLG_INNERBORDER_LEFT,
				ControlDims.RSC_SP_DLG_INNERBORDER_TOP, ControlDims.DLG_ABOUT_HEIGH
						- ( ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 2 ), ControlDims.DLG_ABOUT_WIDTH
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
				new Integer( ControlDims.DLG_ABOUT_BACKG_COLOR ),
				new Short( (short)0 ) } );

		xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class,
				super.m_xDialogControl );
		createWindowPeer();
		// center the dialog, using physical coordinates, MUST be called after
		// CreateWindowPeer
		center();
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
			if (sName.equals(SHOW_LICENSE_PB)) {
				//show the license...
				DialogShowLicense dlg = new DialogShowLicense(m_xParentFrame,m_xContext,m_xMCF);
				int BiasX = (ControlDims.DLG_ABOUT_WIDTH-ControlDims.DLG_SHOW_LICENSE_WIDTH)/2;
				int BiasY = ControlDims.RSC_CD_PUSHBUTTON_HEIGHT;
				dlg.initialize( BiasX, BiasY);
				dlg.executeDialog();
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
}

