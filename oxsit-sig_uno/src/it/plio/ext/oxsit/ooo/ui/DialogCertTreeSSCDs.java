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
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.ui.TreeElement.TreeNodeType;
import it.plio.ext.oxsit.security.XOX_DocumentSigner;
import it.plio.ext.oxsit.security.XOX_SSCDManagement;
import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;
import it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

import java.net.URISyntaxException;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.frame.XFrame;
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

	protected XOX_SSCDManagement	m_axoxAvailableSSCDs;

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
		m_aLogger.entering("initialize");
		insertButton(this,
				CertifTreeDlgDims.DS_COL_PB2(),
//				CertifTreeDlgDims.DS_COL_1()+CertifTreeDlgDims.DS_BTNWIDTH_1()/2,
				CertifTreeDlgDims.DS_ROW_4(),
				CertifTreeDlgDims.dsBtnWidthCertTree(),
				m_sVerifyBtn,		//use the verify, so we'll end up in similar function
				m_sBtn_ListCA,
				(short) PushButtonType.STANDARD_value);

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
		enableSingleButton(m_sAddBtn,false);
		enableSingleButton(m_sReportBtn,false);
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
		m_aLogger.info("firma document con un certificato");
		XMutableTreeNode xAnode = m_aTheCurrentlySelectedTreeNode;
		Object aObj = xAnode.getDataValue();
		if(aObj instanceof CertificateTreeElement) {
			//check if not CA
			CertificateTreeElement ct = (CertificateTreeElement)aObj;
			if(ct.getNodeType() == TreeNodeType.CERTIFICATE) {
				//add a check on the state and alert the user
				//FIXME
				
				//instantiate the signer
				try {
					//FIXME get the object name form the parameters
					Object oDocumSigner = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_SIGNER_SERVICE_IT, m_xContext);
					
					XOX_DocumentSigner xSigner = (XOX_DocumentSigner)UnoRuntime.queryInterface(XOX_DocumentSigner.class, oDocumSigner);
					
					if(xSigner != null) {
						XOX_X509Certificate[] aCert = new XOX_X509Certificate[1]; 						
						aCert[0] = ct.getCertificate();

						if(xSigner.signDocumentStandard(m_xParentFrame,getDocumentStorage(), aCert))
							endDialog();
						//mark signature status dirty if signed?
						//TODO
					}
					else
						throw (new NoSuchMethodException("Missing XOX_DocumentSigner interface !"));
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
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
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#selectButtonPressed()
	 */
	@Override
	public void selectButtonPressed() {
		//select the certificate on tree for signature
		//FIXME: need to filter out the certificates already used to sign the current document
		
		m_aLogger.info("Seleziona dispositivo");
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
			m_axoxAvailableSSCDs = (XOX_SSCDManagement)UnoRuntime.queryInterface(XOX_SSCDManagement.class, aObj);
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
						if(oSSCDev.getHasX509Certificates() > 0) {
							XOX_X509Certificate[] oCertifs = oSSCDev.getX509Certificates();
							for(int idx1=0; idx1<oCertifs.length;idx1++) {
								//perform certificate verification (a full one!)
								oCertifs[idx1].verifyCertificate(m_xParentFrame);
								//then add to the tree control
								addX509CertificateToTree(xCertifNode, oCertifs[idx1], TreeNodeType.CERTIFICATE);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			m_aLogger.severe("selectButtonPressed", e);
		}
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
	 */
	@Override
	public void verifyButtonPressed() {
		//call the dialog to show the CA tree list
		DialogCertTreeCA aDialog1 = new DialogCertTreeCA( m_xParentFrame, m_xContext, m_xMCF );
		try {
			int BiasX = 0;//(CertifTreeDlgDims.dsWidth()-CertifTreeDlgDims.dsWidth())/2;
			int BiasY = ControlDims.RSC_CD_PUSHBUTTON_HEIGHT;//to see the underlying certificates already in the document
			aDialog1.initialize( BiasX, BiasY);
			aDialog1.executeDialog();
			aDialog1.disposeElements();
		} catch (BasicErrorException e) {
			m_aLogger.severe("actionPerformed", "", e);
		}
		try {
			aDialog1.executeDialog();
			return;
		} catch (BasicErrorException e) {
			// TODO Auto-generated catch block
			m_aLogger.severe("actionPerformed", "", e);
			return;
		}
		
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
			boolean bEnableAddButton = false;
			boolean bEnableRepButton = false;
			TreeNodeType aNdType = aCurrentNode.getNodeType();
			if(aNdType == TreeNodeType.CERTIFICATE ||
					aNdType == TreeNodeType.CERTIFICATE_CA)
				bEnableRepButton = true;
			if(aNdType == TreeNodeType.CERTIFICATE)
				bEnableAddButton = true;
			enableSingleButton(m_sAddBtn,bEnableAddButton);
			enableSingleButton(m_sReportBtn,bEnableRepButton);
			aCurrentNode.EnableDisplay(true);
		}
		else {
			enableSingleButton(m_sAddBtn,false);
			enableSingleButton(m_sReportBtn,false);
		}
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
