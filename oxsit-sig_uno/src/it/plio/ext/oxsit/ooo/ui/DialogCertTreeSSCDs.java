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

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.XOX_AvailableSSCDs;

import java.util.LinkedList;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.FocusEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XKeyHandler;
import com.sun.star.awt.XKeyListener;
import com.sun.star.awt.XMouseListener;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.ExpandVetoException;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeControl;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionChangeListener;

/**
 * @author beppe
 *
 */
public class DialogCertTreeSSCDs extends DialogCertTreeBase implements
		XActionListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener {

	private static final String DLG_CERT_TREE = "DialogCertTreeSSCDs";

	private static final String sSelect = "selectb";
	//graphic indications
	private String sSignatureOK; //signature ok
	private String sSignatureNotValidated; //signature ok, but certificate not valid
	private String sSignatureBroken; //signature does not mach content: document changed after signature
	private String sSignatureInvalid; //signature cannot be validated
	private String sSignatureAdding;
	private String sSignatureRemoving;

	private String sCertificateValid = null; //
	private String sCertificateNotValidated = null; //

	private String	sCertificateElementWarning = null;


	private String 				m_sBtn_SelDevice = "id_pb_sel_device";
	private String 				m_sBtn_AddCertLabel = "id_pb_add_cert";
	private String 				m_sDlgListCertTitle = "id_title_mod_cert_tree";
	
	private String 				sCertificateElementError;
	private String 				sCertificateElementBroken;

	private String sSignatureInvalid2;
	
	protected XOX_AvailableSSCDs	m_axoxAvailableSSCDs;

//	public static final int	NUMBER_OF_DISPLAYED_TEST_LINES = 14;
	
	/**
	 * Note on the display:
	 * two ways on right pane:
	 * - a six line text on a white background for generals
	 * - a multiline text for internal elements.
	 * 
	 * tree structure:
	 *  tree ()
	 *      node ()
	 *      	
	 * 
	 * @param frame
	 * @param context
	 * @param _xmcf
	 */
	public DialogCertTreeSSCDs(XFrame frame, XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(frame, context, _xmcf);
		// TODO Auto-generated constructor stub
//fill string for graphics
		String sLoc = Helpers.getExtensionInstallationPath(context);
		if(sLoc != null){
			String aSize = "_26.png"; //for large icons toolbar
//				String aSize = "_16.png"; //for small icons toolbar
			String m_imagesUrl = sLoc + "/images";
//main, depends from application, for now. To be changed
			//TODO change to a name not depending from the application
			sSignatureOK = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_OK+aSize; //signature ok
			sSignatureNotValidated = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_WARNING+aSize; //signature ok, but certificate not valid
			sSignatureBroken = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID+aSize; //signature does not mach content: document changed after signature
			sSignatureInvalid = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN2+aSize; //
			sSignatureInvalid2 = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN+aSize; //
			sSignatureAdding = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_ADDING+aSize; //
			sSignatureRemoving = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_REMOVING+aSize; //

			sCertificateValid = m_imagesUrl + GlobConstant.m_nCERTIFICATE+aSize;
			sCertificateNotValidated = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID +aSize;
			sCertificateElementWarning = m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_WARNING +aSize;
			sCertificateElementError = m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_INVALID +aSize;
			sCertificateElementBroken = m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_BROKEN +aSize;
		}
		else
			m_logger.severe("ctor","no package location !");
		fillLocalizedString();
// the next value should be read from configuration, when configuration is written...
//		CertifTreeDlgDims.setDialogSize(0, 0); //to test
		CertifTreeDlgDims.setDialogSize(300, 100, 0);

//instantiate the SSCDs service
		try {
			Object aObj = m_xMCF.createInstanceWithContext(GlobConstant.m_sAVAILABLE_SSCD_SERVICE, m_xContext);
			m_axoxAvailableSSCDs = (XOX_AvailableSSCDs)UnoRuntime.queryInterface(XOX_AvailableSSCDs.class, aObj);
			if(m_axoxAvailableSSCDs != null)
				m_axoxAvailableSSCDs.scanDevices();
		} catch (Exception e) {
			m_logger.severe("ctor", e);
		}
	}

	/**
	 * prepare the strings for the dialogs
	 */
	protected void fillLocalizedString() {
		super.fillLocalizedString();
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);

		try {
			m_sBtn_SelDevice = m_aRegAcc.getStringFromRegistry( m_sBtn_SelDevice );
			m_sBtn_AddCertLabel = m_aRegAcc.getStringFromRegistry( m_sBtn_AddCertLabel );
			m_sDlgListCertTitle = m_aRegAcc.getStringFromRegistry( m_sDlgListCertTitle );
			m_sFt_Hint_Doc = m_aRegAcc.getStringFromRegistry( m_sFt_Hint_Doc );
		} catch (com.sun.star.uno.Exception e) {
			m_logger.severe("fillLocalizedString", e);
		}
		m_aRegAcc.dispose();	
	}

	public void initialize(int _nPosX, int _nPosY ) throws BasicErrorException {
		initialize(null, _nPosX, _nPosY );
	}

	/** Inizializza la finestra di dialogo
	 *  per la visualizzazione strutturata delle firme presenti
	 * @throws BasicErrorException 
	 * 
	 */
	public void initialize(XWindowPeer _xParentWindow, int posX, int posY) throws BasicErrorException {
		m_logger.entering("initialize");
			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB3(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					sSelect,
					m_sBtn_SelDevice,
					(short) PushButtonType.STANDARD_value, 4);
			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB4(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					sAdd,
					m_sBtn_AddCertLabel,
					(short) PushButtonType.STANDARD_value, 5);
			insertHorizontalFixedLine(
					0, 
					CertifTreeDlgDims.DLGS_BOTTOM_FL_Y(CertifTreeDlgDims.dsHeigh()), 
					CertifTreeDlgDims.dsWidth(), "");
			
			super.initializeLocal(DLG_CERT_TREE, m_sDlgListCertTitle, posX, posY);
			
			xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, super.m_xDialogControl);		
			createWindowPeer();
			disableAllNamedControls();
			
			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
					super.m_xDialogControl );
			xTFWindow.addKeyListener( this );
//			Utilities.showControlNames(m_xDlgContainer);
			Utilities.showNames(m_xDlgModelNameContainer);

	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XActionListener#actionPerformed(com.sun.star.awt.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent rEvent) {
		// TODO Auto-generated method stub
		try {
			// get the control that has fired the event,
			XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class,
					rEvent.Source);
			XControlModel xControlModel = xControl.getModel();
			XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(
					XPropertySet.class, xControlModel);
			String sName = (String) xPSet.getPropertyValue("Name");
			// just in case the listener has been added to several controls,
			// we make sure we refer to the right one
			System.out.println("action: "+sName);
			if (sName.equals(sAdd)) {
				m_logger.info("Aggiunto certificato");				
				addOneSignature();
			} else if (sName.equals(sSelect)) {
					// close dialog and will exit				
				m_logger.info("Seleziona dispositivo");
				addOneCertificate();
			}
			else {
				m_logger.info("Activated: " + sName);
			}
		} catch (com.sun.star.uno.Exception ex) {
			/*
			 * perform individual exception handling here. Possible exception
			 * types are: com.sun.star.lang.WrappedTargetException,
			 * com.sun.star.beans.UnknownPropertyException,
			 * com.sun.star.uno.Exception
			 */
			m_logger.severe("initialize", ex);
		}
	}

	@Override
	public short executeDialog() throws BasicErrorException {
		return super.executeDialog();
	}

	/** called when the dialog is closing, to dispose of available certificate list
	 * 
	 */
	public void disposeElements() {
		if(m_axoxAvailableSSCDs != null) {
			XComponent xC = (XComponent)UnoRuntime.queryInterface(XComponent.class, m_axoxAvailableSSCDs);
			if(xC != null)
				xC.dispose();
		}
	}
}
