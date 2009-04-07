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

package it.plio.ext.xades.ooo.ui;

import it.plio.ext.xades.ooo.registry.MessageConfigurationAccess;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XMouseListener;
import com.sun.star.awt.XWindowPeer;
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
public class DialogChooseSignatureTypes extends BasicDialog
		implements XActionListener, XMouseListener, XItemListener
		{

	private static final String m_sDialogName = "ChooseSignDlg";
	private static final String m_sCnipaBtn = "cnipasig";
	private static final String m_sOOoBtn = "oooasig";
	public static final int NoSignatureSelected = 0;
	public static final int OOoSignatureSelected = 1;
	public static final int CNIPASignatureSelected = 2;
	
	private short m_nRetValue = 0; //means default

	private XWindowPeer m_xParentWindow = null;
	
	private int returnValue = -1;
	private String	m_sCanc;
	private String	m_sSigCn;
	private String	m_sSigOOo;
	private String	m_sSelDig;
	
	public DialogChooseSignatureTypes(XFrame _xFrame, XComponentContext _xContext,
			XMultiComponentFactory _xMCF) {
		super(_xFrame, _xContext, _xMCF);		
		MessageConfigurationAccess m_aRegAcc = null;

		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF); 
		try {
			m_sCanc = m_aRegAcc.getStringFromRegistry( "id_cancel" );
			m_sSelDig = m_aRegAcc.getStringFromRegistry( "id_sel_dsig" );
			m_sSigCn = m_aRegAcc.getStringFromRegistry( "id_sel_dsig_xades" );
			m_sSigOOo = m_aRegAcc.getStringFromRegistry( "id_sel_dsig_ooo" );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_aRegAcc.dispose();	
	}
	
	public void initialize(int _nPosX, int _nPosY ) throws BasicErrorException, com.sun.star.uno.Exception {
/*		DigSignDlgDims.setSIGTP_HEIGH(68);
		DigSignDlgDims.setSIGTP_WIDTH(140);*/
		initialize(null, _nPosX, _nPosY );
	}
	
	public void initialize(XWindowPeer _xParentWindow, int _nPosX, int _nPosY ) throws com.sun.star.uno.Exception {
		super.initialize(m_sDialogName, m_sSelDig, 
				DigSignDlgDims.SIGTP_HEIGH(), DigSignDlgDims.SIGTP_WIDTH(), _nPosX, _nPosY);

		m_xParentWindow = _xParentWindow;
//		inserisce dei testi sulle righe, per definire la posizione delle righe 
//		(ne serve una in più rispetto allo standard OOo per il pulsante della controfirma)

		//cancel button
		insertButton(this,
				DigSignDlgDims.SIGTP_PUSHBUTTON_XPOS(),
				DigSignDlgDims.SIGTP_CANCEL_PUSHBUTTON_POS(),
				DigSignDlgDims.SIGTP_PUSHBUTTON_WIDTH(),
				"cancb",
				m_sCanc,
				(short) PushButtonType.CANCEL_value);

		insertButton(this,
				DigSignDlgDims.SIGTP_PUSHBUTTON_XPOS(),
				DigSignDlgDims.SIGTP_CNIPASIG_PUSHBUTTON_POS(),
				DigSignDlgDims.SIGTP_PUSHBUTTON_WIDTH(),
				m_sCnipaBtn,
				m_sSigCn,
				(short) PushButtonType.STANDARD_value);		

		//cancel button
		insertButton(this,
				DigSignDlgDims.SIGTP_PUSHBUTTON_XPOS(),
				DigSignDlgDims.SIGTP_OOOSIG_PUSHBUTTON_POS(),
				DigSignDlgDims.SIGTP_PUSHBUTTON_WIDTH(),
				m_sOOoBtn,
				m_sSigOOo,
				(short) PushButtonType.STANDARD_value);

		xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, super.m_xDialogControl);		
		createWindowPeer();
//		centerHighHalf();
		center();
	}
	
	/* (non-Javadoc)
	 * @see it.plio.ext.cnipa.ooo.ui.BasicDialog#actionPerformed(com.sun.star.awt.ActionEvent)
	 */
	public void actionPerformed(ActionEvent rEvent) {
//		super.actionPerformed(arg0);
        try{
            // get the control that has fired the event,
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, rEvent.Source);
            XControlModel xControlModel = xControl.getModel();
            XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
            String sName = (String) xPSet.getPropertyValue("Name");
            // just in case the listener has been added to several controls,
            // we make sure we refer to the right one
            if (sName.equals(m_sCnipaBtn)) {
            	m_nRetValue = CNIPASignatureSelected;
            	xDialog.endExecute();
            }
            else if (sName.equals(m_sOOoBtn)) {
            	m_nRetValue = OOoSignatureSelected;
            	xDialog.endExecute();
            }
        }catch (com.sun.star.uno.Exception ex){
            /* perform individual exception handling here.
             * Possible exception types are:
             * com.sun.star.lang.WrappedTargetException,
             * com.sun.star.beans.UnknownPropertyException,
             * com.sun.star.uno.Exception
             */
            ex.printStackTrace(System.out);
        }		
	}
	
    public short executeDialog() throws com.sun.star.script.BasicErrorException {
        if (m_xWindowPeer == null) {
            createWindowPeer();
        }
        xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
        m_xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, m_xDialogControl);
//        repaintDialogStep();
        xDialog.execute();
        m_xComponent.dispose();
        return m_nRetValue;
    }
}
