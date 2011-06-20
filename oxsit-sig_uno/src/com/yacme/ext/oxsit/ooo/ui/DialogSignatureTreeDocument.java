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

import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
import com.yacme.ext.oxsit.security.XOX_DocumentSigner;
import com.yacme.ext.oxsit.security.XOX_SSCDManagement;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionChangeListener;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.ControlDims;

/**
 * @author beppe
 *
 */
public class DialogSignatureTreeDocument extends DialogCertTreeBase 
		implements	IDialogCertTreeBase,
		XActionListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener
		{

	private static final String DLG_SIGN_TREE = "DialogSignTree";

	private XOX_DocumentSignaturesVerifier m_axoxDocumentVerifier;

	/**
	 * Display the signatures present inside the ODF document
	 * @param frame
	 * @param context
	 * @param _xmcf
	 */
	public DialogSignatureTreeDocument(XFrame frame, XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(frame, context, _xmcf);
		m_sDlgListCertTitle = "id_title_cert_tree"; // local value, different !
		m_sFt_Hint_Doc = "id_title_cert_treew"; // local vale, different !
		fillLocalizedString();
// the next value should be read from configuration, when configuration is written...
//		CertifTreeDlgDims.setDialogSize(0, 0); //to test
		CertifTreeDlgDims.setDialogSize(300, 100, 0);
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
		final String __FUNCTION__ = "initialize: ";
		m_aLogger.entering("initialize");
		insertButton(this,
				CertifTreeDlgDims.DS_COL_PB2(),
//				CertifTreeDlgDims.DS_COL_1()+CertifTreeDlgDims.DS_BTNWIDTH_1()/2,
				CertifTreeDlgDims.DS_ROW_4(),
				CertifTreeDlgDims.dsBtnWidthCertTree(),
				m_sVerifyBtn,
				m_sBtn_Verify,
				(short) PushButtonType.STANDARD_value);

			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB3(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					m_sAddBtn,
					m_sBtn_AddCertLabel,
					(short) PushButtonType.STANDARD_value, 7);

			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB4(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					m_sRemoveBtn,
					m_sBtn_RemoveCertLabel,
					(short) PushButtonType.STANDARD_value);
			
			insertHorizontalFixedLine(
					0, 
					CertifTreeDlgDims.DLGS_BOTTOM_FL_Y(CertifTreeDlgDims.dsHeigh()), 
					CertifTreeDlgDims.dsWidth(), "");

		//must be called AFTER the local init
		m_sTreeCtl = "certsigntree"; // the tree element for this dialog, new name
		super.initializeLocal(DLG_SIGN_TREE, m_sDlgListCertTitle, posX, posY);

		center();
			
//			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
//					super.m_xDialogControl );
//			xTFWindow.addKeyListener( this );
//			Utilities.showControlNames(m_xDlgContainer);
//			Utilities.showNames(m_xDlgModelNameContainer);
			
			//Add init of signatures, check, verify and add certificates
//			/oxsit-sig_uno/src/com/yacme/ext/oxsit/ooo/ui/DialogCertTreeSSCDs.java
			
		//load the certificates from the document signatures (always when dialog starts)

		try {
			Object aDocVerService = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT, m_xContext);
			if(aDocVerService != null) {				
				m_axoxDocumentVerifier = (XOX_DocumentSignaturesVerifier)UnoRuntime.queryInterface(XOX_DocumentSignaturesVerifier.class, aDocVerService);
				if(m_axoxDocumentVerifier != null) {
					//grab the certificates and add them to the dialog						
					XOX_X509Certificate[] oCertifs = 
						m_axoxDocumentVerifier.loadAndGetCertificates(m_xParentFrame,getDocumentModel());
					for(int idx = 0; idx < oCertifs.length; idx++) {
						//add the certificate to the dialog tree
						m_aLogger.debug(__FUNCTION__+"certificate added");
						addASignature(oCertifs[idx]);
					}
				}
				else
					m_aLogger.warning("verifyButtonPressed and XOX_DocumentSignaturesVerifier interface NOT available");
		        // now clean up
		        ((XComponent) UnoRuntime.queryInterface(XComponent.class, aDocVerService)).dispose();
			}
			else
				m_aLogger.warning(__FUNCTION__+GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT+" Service NOT available");

		} catch (Throwable e) {
			m_aLogger.severe(__FUNCTION__, e);
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
		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#addButtonPressed()
	 */
	@Override
	public void addButtonPressed() {
		//before adding, check if the document can be signed with the current customization
		//instantiate a DocumentSigner object
		try {
			//Call the document verify pre-signature method
			DialogCertTreeSSCDs aDialog1 = new DialogCertTreeSSCDs(m_xParentFrame, m_xContext, m_xMCF);
			try {
				int BiasX = 0;//(CertifTreeDlgDims.dsWidth()-CertifTreeDlgDims.dsWidth())/2;
				int BiasY = ControlDims.RSC_CD_PUSHBUTTON_HEIGHT * 4;//to see the underlying certificates already in the document
				aDialog1.setDocumentModel(getDocumentModel());
				aDialog1.initialize(BiasX, BiasY);
				aDialog1.executeDialog();
				aDialog1.disposeElements();
			} catch (BasicErrorException e) {
				m_aLogger.severe("actionPerformed", "", e);
			}
		} catch (Throwable e1) {
			m_aLogger.severe("actionPerformed", "", e1);
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#removeButtonPressed()
	 */
	@Override
	public void removeButtonPressed() {
		//not implemented here
		//fake implementation!
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#reportButtonPressed()
	 */
	@Override
	public void reportButtonPressed() {
		//prints a report of the selected CERTIFICATE
		m_aLogger.info("reportButtonPressed","FAKE IMPLEMENTATION!");
//		addOneSignature();
		
//dirty trick, do not instantiate the UNO component this way !
		// this is just for test !
/*		AvailableSSCDs aSSCDS = new AvailableSSCDs(m_xContext);
		aSSCDS.testCertificateDisplay();*/
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#selectButtonPressed()
	 */
	@Override
	public void selectButtonPressed() {
		//select the certificate on tree for signature
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
	 */
	@Override
	public void verifyButtonPressed() {
//for the moment, use this to load the document at hand , verify it and display the certificates of every signature found.
		//first identify the type of selected element
//		XComponent xTheCurrentComp = (XComponent)UnoRuntime.queryInterface( XComponent.class, oTreeNodeObject );
		
		if(m_aTheCurrentlySelectedTreeNode != null) {
			//disable it, that is un-display it
			Object oTreeNodeObject  = m_aTheCurrentlySelectedTreeNode.getDataValue();
			if(oTreeNodeObject != null) {
				if(oTreeNodeObject instanceof TreeElement) {
					TreeElement aCurrentNode = (TreeElement)oTreeNodeObject;
					if(aCurrentNode.getNodeType() == com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.SIGNATURE) {
						try {
							Object aDocVerService = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT, m_xContext);
							if(aDocVerService != null) {				
								m_axoxDocumentVerifier = (XOX_DocumentSignaturesVerifier)UnoRuntime.queryInterface(XOX_DocumentSignaturesVerifier.class, aDocVerService);
								if(m_axoxDocumentVerifier != null) {
									m_axoxDocumentVerifier.verifyDocumentSignatures(m_xParentFrame, 
											getDocumentModel(), null);
								}
								else
									m_aLogger.warning("verifyButtonPressed and XOX_DocumentSignaturesVerifier interface NOT available");
						        // now clean up
						        ((XComponent) UnoRuntime.queryInterface(XComponent.class, aDocVerService)).dispose();
							}
							else
								m_aLogger.warning("verifyButtonPressed and "+GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT+" Service NOT available");

						} catch (Exception e) {
							m_aLogger.severe("verifyButtonPressed", e);
						}						
					}
					else if(aCurrentNode.getNodeType() == com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.CERTIFICATE) {
						//if it's a certificate, check the certificate
						//get the XOX_Certitificate and test it
						if(oTreeNodeObject instanceof CertificateTreeElement) {
							CertificateTreeElement aCurrentCertNode = (CertificateTreeElement)oTreeNodeObject;					
//							aCurrentNode.EnableDisplay(false);
		//duplicate the certificate, then simply do the check for revocation list					
							XOX_X509Certificate aCert = aCurrentCertNode.getCertificate();
							if(aCert != null) {
								try {
									aCert.verifyCertificateRevocationState(m_xParentFrame);
									//now update the string and the text on screen
									aCurrentCertNode.updateCertificateStates();
									aCurrentCertNode.updateString();
									aCurrentCertNode.EnableDisplay(true);
								} catch (IllegalArgumentException e) {
									m_aLogger.severe(e);
								} catch (Throwable e) {
									m_aLogger.severe(e);
								}
							}
						}
						else
							m_aLogger.warning("Wrong class type in tree control node data: "+oTreeNodeObject.getClass().getName());
						
					}
				}
				else
					m_aLogger.warning("Wrong class type in tree control node data: "+oTreeNodeObject.getClass().getName());
			}
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
			boolean bEnableButton = false;
			if(aCurrentNode.getNodeType() == com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.SIGNATURE) {
				bEnableButton = true;
			}
//			enableSingleButton(m_sVerifyBtn,bEnableButton);
			enableSingleButton(m_sRemoveBtn,bEnableButton);
			aCurrentNode.EnableDisplay(true);
		}
		else {
//			enableSingleButton(m_sVerifyBtn,false);
			enableSingleButton(m_sRemoveBtn,false);
		}

		//FIXME FOR TEST ONLY !
		enableSingleButton(m_sVerifyBtn,true);
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
