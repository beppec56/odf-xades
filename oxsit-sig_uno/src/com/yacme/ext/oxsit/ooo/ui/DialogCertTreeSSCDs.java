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

package com.yacme.ext.oxsit.ooo.ui;

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
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType;
import com.yacme.ext.oxsit.security.XOX_DocumentSigner;
import com.yacme.ext.oxsit.security.XOX_SSCDManagement;
import com.yacme.ext.oxsit.security.XOX_SSCDevice;
import com.yacme.ext.oxsit.security.cert.CertificateState;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

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

	private int m_nNumOfSSCD;

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
		m_aLogger.entering("initialize (DialogCertTreeSSCD)");
		insertButton(this,
				CertifTreeDlgDims.DS_COL_PB3(),
//				CertifTreeDlgDims.DS_COL_1()+CertifTreeDlgDims.DS_BTNWIDTH_1()/2,
				CertifTreeDlgDims.DS_ROW_4(),
				CertifTreeDlgDims.dsBtnWidthCertTree(),
				m_sVerifyBtn,		//use the verify, so we'll end up in similar function
				m_sBtn_ListCA,
				(short) PushButtonType.STANDARD_value);

//			insertButton(this,
//					CertifTreeDlgDims.DS_COL_PB3(),
//					CertifTreeDlgDims.DS_ROW_4(),
//					CertifTreeDlgDims.dsBtnWidthCertTree(),
//					m_sSelectBtn,
//					m_sBtn_SelDevice,
//					(short) PushButtonType.STANDARD_value, 6);
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
			if ((m_nNumOfSSCD = showSSCD()) == 0) {
            //give the user some feedback
				MessageNoTokens	aMex = new MessageNoTokens(m_xParentFrame,m_xMCF,m_xContext);
	            aMex.executeDialogLocal("");
			}
	}

	@Override
	public short executeDialog() throws BasicErrorException {
		if(m_nNumOfSSCD == 0) {
			endDialog();
			return 0; //FIXME: may be a different result is needed (OK or Cancel)
		}
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
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#addButtonPressed()
	 */
	@Override
	public void addButtonPressed() {
		//add the certificate to ?? check the spec
		m_aLogger.debug("firma document con un certificato");
		XMutableTreeNode xAnode = m_aTheCurrentlySelectedTreeNode;
		Object aObj = xAnode.getDataValue();
		if(aObj instanceof CertificateTreeElement) {
			//check if not CA
			CertificateTreeElement ct = (CertificateTreeElement)aObj;
			if(ct.getNodeType() == TreeNodeType.CERTIFICATE) {
				
				//instantiate the signer
				try {
					//FIXME get the object name from General Parameters
					Object oDocumSigner = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_SIGNER_SERVICE_IT, m_xContext);
					XOX_DocumentSigner xSigner = (XOX_DocumentSigner)UnoRuntime.queryInterface(XOX_DocumentSigner.class, oDocumSigner);
					if(xSigner != null) {
						XOX_X509Certificate[] aCert = new XOX_X509Certificate[1]; 						
						aCert[0] = ct.getCertificate();
						//FIXME add a check on the state and alert the user
						//meaning a check of the certificate type
						int nState = ct.getCertificateState();
						m_aLogger.debug("addButtonPressed", "certificate state = "+ nState);
						if( nState == CertificateState.OK_value) {
							if(xSigner.signDocument(m_xParentFrame,getDocumentModel(), aCert, null))
								endDialog();
							//FIXME mark signature status dirty if signed?
						}
						else {
							m_aLogger.debug("addButtonPressed", "CANNOT SIGN ! Certificate state = "+ nState);
							//display a message to the user about the failing certificate
							MessageWrongCertificateSelected	aMex = new MessageWrongCertificateSelected(m_xParentFrame,m_xMCF,m_xContext);
	                        aMex.executeDialogLocal();
						}
					}
					else
						throw (new NoSuchMethodException("Missing XOX_DocumentSigner interface !"));
				} catch (Throwable e) {
					m_aLogger.log(e, true);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#removeButtonPressed()
	 */
	@Override
	public void removeButtonPressed() {
		//not implemented here
	}

	private int showSSCD() {
		//select the certificate on tree for signature
		//FIXME: need to filter out the certificates already used to sign the current document

		int nNumberofSSCD = 0;
		m_aLogger.debug("Seleziona dispositivo");
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
				XOX_SSCDevice[] xDevices = m_axoxAvailableSSCDs.getAvailableSSCDevices();
				if(xDevices != null) {
					nNumberofSSCD = xDevices.length;
					//add the new available certificates
					for(int idx = 0; idx < xDevices.length; idx++) {
						XOX_SSCDevice oSSCDev = xDevices[idx];
// add this node to the tree
						XMutableTreeNode xCertifNode = addSSCDToTreeRootHelper(oSSCDev);
						if(oSSCDev.getHasCertificates() > 0) {
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
			m_aLogger.severe("showSSCD", e);
		}
		return nNumberofSSCD;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
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
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#checkButtonsEnable()
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
