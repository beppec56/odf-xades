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

import java.net.URISyntaxException;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.XOX_AvailableSSCDs;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState;
import it.plio.ext.oxsit.security.cert.XOX_QualifiedCertificate;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.frame.XFrame;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionChangeListener;

/**
 * @author beppec56
 *
 */
public class DialogCertTreeSSCDs extends DialogCertTreeBase 
		implements	IDialogCertTreeBase,
		XActionListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener
		{

	private static final String DLG_CERT_TREE = "DialogCertTreeSSCDs";

	protected XOX_AvailableSSCDs	m_axoxAvailableSSCDs;
	
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
		fillLocalizedString();
// the next value should be read from configuration, when configuration is written...
//		CertifTreeDlgDims.setDialogSize(0, 0); //to test
		CertifTreeDlgDims.setDialogSize(310, 100, 0);
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
					m_sSelectBtn,
					m_sBtn_SelDevice,
					(short) PushButtonType.STANDARD_value, 6);
			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB4(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					m_sAddBtn,
					m_sBtn_AddCertLabel,
					(short) PushButtonType.STANDARD_value, 7);
			insertHorizontalFixedLine(
					0, 
					CertifTreeDlgDims.DLGS_BOTTOM_FL_Y(CertifTreeDlgDims.dsHeigh()), 
					CertifTreeDlgDims.dsWidth(), "");

			//must be called AFTER the local init
			super.initializeLocal(DLG_CERT_TREE, m_sDlgListCertTitle, posX, posY);

//			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
//					super.m_xDialogControl );
//			xTFWindow.addKeyListener( this );
//			Utilities.showControlNames(m_xDlgContainer);
//			Utilities.showNames(m_xDlgModelNameContainer);
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

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#addButtonPressed()
	 */
	@Override
	public void addButtonPressed() {
		// TODO Auto-generated method stub
		//add the certificate to ?? check the spec
		m_logger.info("cambio stato certificato");
		XMutableTreeNode xAnode = m_aTheCurrentlySelectedTreeNode;
		Object aObj = xAnode.getDataValue();
		if(aObj instanceof CertificateTreeElement) {
			CertificateTreeElement ct = (CertificateTreeElement)aObj;
			int avl = ct.getCertificateGraficStateValue();
			avl++;
			if(avl >= CertificateGraphicDisplayState.LAST_STATE_value)
				avl = CertificateGraphicDisplayState.NOT_VERIFIED_value;
			ct.setCertificateGraficStateValue(avl);
			xAnode.setNodeGraphicURL(m_sCertificateValidityGraphicName[avl]);
		}
//		addOneSignature();		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#removeButtonPressed()
	 */
	@Override
	public void removeButtonPressed() {
		//not implemented here
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#reportButtonPressed()
	 */
	@Override
	public void reportButtonPressed() {
		//prints a report of the selected CERTIFICATE
		String m_sExtensionSystemPath;
		//not implemented here, next code is for test only:
		try {
			m_sExtensionSystemPath = Helpers.getExtensionInstallationSystemPath(m_xContext);
			m_logger.ctor("extension installed in: "+m_sExtensionSystemPath);
			String libPath=System.getProperty("java.library.path");
			//first the current extension path into the library path
			libPath = m_sExtensionSystemPath + System.getProperty("path.separator") + libPath;
			System.setProperty("java.library.path", libPath);
			m_logger.log("library path is: "+ System.getProperty("java.library.path"));		
		} catch (URISyntaxException e) {
			m_logger.severe("ctor", "", e);
		} catch (java.io.IOException e) {
			m_logger.severe("ctor", "", e);
		}		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#selectButtonPressed()
	 */
	@Override
	public void selectButtonPressed() {
		//select the certificate on tree for signature
		m_logger.info("Seleziona dispositivo");
//		addOneCertificate();
		//instantiate the SSCDs service
		if(m_axoxAvailableSSCDs != null) {
			XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, m_axoxAvailableSSCDs);
			if(xComp != null)
				xComp.dispose();
		}
		m_axoxAvailableSSCDs = null;
		try {
			Object aObj = m_xMCF.createInstanceWithContext(GlobConstant.m_sAVAILABLE_SSCD_SERVICE, m_xContext);
			m_axoxAvailableSSCDs = (XOX_AvailableSSCDs)UnoRuntime.queryInterface(XOX_AvailableSSCDs.class, aObj);
			if(m_axoxAvailableSSCDs != null) {
				//FIXME: may be we should remove the element from the tree only when new device
				// scan finished?
				//empy the tree, then add the new certificates
				removeAllTreeNodes();
				m_axoxAvailableSSCDs.scanDevices(m_xParentFrame,m_xContext);//true because we are calling from a GUI interface
				m_axoxAvailableSSCDs.getAvailableSSCDevices();
				XOX_SSCDevice[] xDevices = m_axoxAvailableSSCDs.getAvailableSSCDevices();
				if(xDevices != null) {
					//add the new available certificates
					for(int idx = 0; idx < xDevices.length; idx++) {
						XOX_SSCDevice oSSCDev = xDevices[idx];
// add this node to the tree
						XMutableTreeNode xCertifNode = addSSCDToTreeRootHelper(oSSCDev);
						if(oSSCDev.getHasQualifiedCertificates() > 0) {
							XOX_QualifiedCertificate[] oCertifs = oSSCDev.getQualifiedCertificates();
							for(int idx1=0; idx1<oCertifs.length;idx1++) {
								//perform certificate verification
								oCertifs[idx1].verifyCertificate(m_xParentFrame);
								//then add to the tree control
								addQualifiedCertificateToTree(xCertifNode, oCertifs[idx1]);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			m_logger.severe("selectButtonPressed", e);
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
	 */
	@Override
	public void verifyButtonPressed() {
		//not implemented here
	}	

	/**
	 * 
	 * @param oTreeNodeObject this is the TreeElement present in the tree element data field
	 * 
	 * (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#checkButtonsEnable()
	 */
	public void checkButtonsEnable(Object oTreeNodeObject) {
		XComponent xTheCurrentComp = (XComponent)UnoRuntime.queryInterface( XComponent.class, oTreeNodeObject );
		if(xTheCurrentComp != null) {
//get node type and enable/disable	the pushbutton
			TreeElement aCurrentNode = (TreeElement)oTreeNodeObject;
			boolean bEnableButton = false;
			if(aCurrentNode.getNodeType() == it.plio.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.CERTIFICATE) {
				bEnableButton = true;
			}
			enableSingleButton(m_sAddBtn,bEnableButton);
			aCurrentNode.EnableDisplay(true);
		}
		else
			enableSingleButton(m_sAddBtn,false);		
	}

	private void enableSingleButton(String sButtonName, boolean bEnable) {
//		m_aLoggerDialog.entering("enableSingleButton");
		//grab the button...
		XControl xTFControl = m_xDlgContainer.getControl( sButtonName );
		if(xTFControl != null){
			//...and set state accordingly
			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
			if(xaWNode != null )
				xaWNode.setEnable(bEnable);
		}
	}		
}
