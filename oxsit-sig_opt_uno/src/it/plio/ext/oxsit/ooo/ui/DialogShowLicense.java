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

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindowPeer;
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
public class DialogShowLicense extends BasicDialog {

	private static final String	DLS_SHOW_LIC	= "showlicdlg";
	private XWindowPeer			m_xParentWindow	= null;
	protected String				m_sTitle = new String("empty title");
	private String				m_sOk  = new String("Ok");
	protected String				m_sTheTextToShow = new String("License text missing...");

	/**
	 * @param frame
	 * @param context
	 * @param _xmcf
	 */
	public DialogShowLicense(XFrame _xFrame, XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(_xFrame, context, _xmcf );
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);

		try {
			m_sTitle = m_aRegAcc.getStringFromRegistry( "id_title_show_license" );
			m_sOk = m_aRegAcc.getStringFromRegistry( "id_ok" );
			m_sTheTextToShow = m_aRegAcc.getStringFromRegistry( "id_main_license" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_aRegAcc.dispose();
	}

	public void initialize(int _nPosX, int _nPosY) throws Exception {
		initialize( m_xParentWindow, _nPosX, _nPosY );
	}

	public void initialize(XWindowPeer _xParentWindow, int _nPosX, int _nPosY)
			throws Exception {

		super.initialize( DLS_SHOW_LIC, m_sTitle, ControlDims.DLG_SHOW_LICENSE_HEIGH, ControlDims.DLG_SHOW_LICENSE_WIDTH, _nPosX, _nPosY );

		int _nPosButton = ControlDims.DLG_SHOW_LICENSE_HEIGH - ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 4 / 3;
		m_xParentWindow = _xParentWindow;

		// cancel button
		insertButton( this,
				( ControlDims.DLG_SHOW_LICENSE_WIDTH - ControlDims.RSC_CD_PUSHBUTTON_WIDTH - 
						ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT), 
				// button
				_nPosButton, ControlDims.RSC_CD_PUSHBUTTON_WIDTH, "okb", m_sOk,
				(short) PushButtonType.OK_value );
		Object oEdit = insertEditFieldModel( this, this, ControlDims.RSC_SP_DLG_INNERBORDER_LEFT,
						ControlDims.RSC_SP_DLG_INNERBORDER_TOP,
						ControlDims.DLG_SHOW_LICENSE_HEIGH - ( ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 2 ),
						ControlDims.DLG_SHOW_LICENSE_WIDTH -
						ControlDims.RSC_SP_DLG_INNERBORDER_LEFT -
						ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT, 0, m_sTheTextToShow, "cyx",
				true, true, true, true );

		XPropertySet xPSet = (XPropertySet) UnoRuntime
							.queryInterface( XPropertySet.class, oEdit );
//		Utilities.showProperties(xPSet);
		
		xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_ABOUT_BACKG_COLOR));

		xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class,
				super.m_xDialogControl );
		createWindowPeer();
		// we do not center the dialog, since the master is already a dialog box
		// so comment it out
		// center();
	}
}
