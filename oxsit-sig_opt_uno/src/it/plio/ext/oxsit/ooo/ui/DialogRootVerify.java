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

import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;
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
	private short 				m_nRetValue = 0;	

	public DialogRootVerify(XFrame _xFrame, XComponentContext context,
			XMultiComponentFactory _xmcf, String _Message) {
		super( _xFrame, context, _xmcf );
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

	/**
	 * static function: non thread safe, to be called only once per application 
	 * @param _xFrame
	 * @param _xCC
	 * @param _axMCF
	 * @return
	 */
	public static short showDialog( XFrame _xFrame, XComponentContext _xCC,
					XMultiComponentFactory _axMCF, String _Message) {
		DialogRootVerify aDialog1 =
			new DialogRootVerify( _xFrame, _xCC, _axMCF,_Message );
		try {
			//PosX e PosY devono essere ricavati dalla finestra genetrice (in questo caso la frame)
			//get the parent window data
			//the problem is that we get the pixel, but we need the logical pixel, so for now it doesn't work...
//			com.sun.star.awt.XWindow xCompWindow = m_xFrame.getComponentWindow();
//			com.sun.star.awt.Rectangle xWinPosSize = xCompWindow.getPosSize();
			int BiasX = 100;
			int BiasY = 30;
//			System.out.println("Width: "+xWinPosSize.Width+ " height: "+xWinPosSize.Height);
//			XWindow xWindow = m_xFrame.getContainerWindow();
//			XWindowPeer xPeer = xWindow.
			aDialog1.initialize(BiasX,BiasY);
//center the dialog
			return aDialog1.executeDialog();
		}
		catch (com.sun.star.uno.RuntimeException e) {
			e.printStackTrace();
		} catch (BasicErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public short executeDialog() throws com.sun.star.script.BasicErrorException {
		if (m_xWindowPeer == null) {
			createWindowPeer();
		}
		xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class, m_xDialogControl );
		m_xComponent = (XComponent) UnoRuntime.queryInterface( XComponent.class,
				m_xDialogControl );
		short Ret = xDialog.execute();
		m_xComponent.dispose();
		return Ret;
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
			// ret = 0: NO
			// ret = 1: Yes
			
			m_logger.log(""+sName);

			if (sName.equals(m_sNO_PB)) {
				//return a NO
				m_nRetValue = (short)3;
				xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class, m_xDialogControl );
				xDialog.endExecute();
				m_logger.log(""+m_sNO_PB);
			}
			else
				if (sName.equals(m_sYES_PB)) {
					//return a YES
					m_nRetValue = (short)2;
					xDialog = (XDialog) UnoRuntime.queryInterface( XDialog.class, m_xDialogControl );
					xDialog.endExecute();
					m_logger.log(""+m_sYES_PB);
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

