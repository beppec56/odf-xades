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

import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
import com.yacme.ext.oxsit.security.XOX_DocumentSigner;
import com.yacme.ext.oxsit.security.XOX_SSCDManagement;
import com.yacme.ext.oxsit.security.XOX_SignatureState;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;
import com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XKeyListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.ChangesEvent;
import com.sun.star.util.XChangesListener;
import com.sun.star.view.XSelectionChangeListener;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.ControlDims;

/**
 * @author beppe
 *
 */
public class DialogSignatureTreeDocument extends DialogCertTreeBase 
		implements	IDialogCertTreeBase,
		XActionListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener, XChangesListener
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
		//load the document signature states (always when dialog starts)
		readAllSignatures(false);
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

	private void readAllSignatures(boolean _bVerifyTheSignatures) {
		final String __FUNCTION__ = "readAllSignatures: ";
		try {
			Object aDocVerService = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT, m_xContext);
			if(aDocVerService != null) {				
				m_axoxDocumentVerifier = (XOX_DocumentSignaturesVerifier)UnoRuntime.queryInterface(XOX_DocumentSignaturesVerifier.class, aDocVerService);
				if(m_axoxDocumentVerifier != null) {
					//grab the certificates and add them to the dialog
					//get this document resident data (a singleton)
					XOX_SingletonDataAccess	  xSingletonDataAccess = null;
					XOX_DocumentSignaturesState xDocumentSignatures = null;
					try {
						xSingletonDataAccess = Helpers.getSingletonDataAccess(m_xContext);
						m_aLogger.debug(" singleton service data "+Helpers.getHashHex(xSingletonDataAccess) );
						xDocumentSignatures = xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(getDocumentModel()), this);			
					}
					catch (ClassCastException e) {
						e.printStackTrace();
					} catch (ServiceNotFoundException e) {
						m_aLogger.severe("ctor",GlobConstant.m_sSINGLETON_SERVICE_INSTANCE+" missing!",e);
					} catch (NoSuchMethodException e) {
						m_aLogger.severe("ctor","XOX_SingletonDataAccess missing!",e);
					}
					
					XOX_SignatureState[] aSigStates = 
						m_axoxDocumentVerifier.loadAndGetSignatures(m_xParentFrame,getDocumentModel());
					if(aSigStates != null) {
						for(int idx = 0; idx < aSigStates.length; idx++) {
							//add the certificate to the dialog tree
							XOX_SignatureState aSignState = aSigStates[idx]; 
							if(xDocumentSignatures != null) {
								if(_bVerifyTheSignatures) {
//do a verification of this signature, the just verified signature is then immediately returned.
									m_axoxDocumentVerifier.verifyDocumentSignatures(m_xParentFrame,getDocumentModel(), aSignState);
//FIXME: the update of the aggregate state should be done here, needs to re-think the logic											
									XOX_X509Certificate xACert = null;
									if(aSignState != null)
										xACert = aSignState.getSignersCerficate();
									if(xACert != null) {
										try {
											xACert.verifyCertificate(m_xParentFrame);
//FIXME: to be removed later and rewritten elsewhere
											Helpers.updateAggregateSignaturesState(xDocumentSignatures, GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK);											
										} catch (Throwable e) {
											m_aLogger.severe(__FUNCTION__,e);
										}
									}
									//Update with the new updated state, if nothing went wrong
									XOX_SignatureState aNewSignState =  m_axoxDocumentVerifier.getSignatureState(aSignState.getSignatureUUID());
									if(aNewSignState != null) {
										aSignState = aNewSignState;
										//now update the aggregate state, using the certificate as well
//FIXME: to be written here and removed in m_axoxDocumentVerifier.verifyDocumentSignatures since this is a code structure matter
										// not jurisdiction, need to be written as priv method in this class.

									}

//now update the state in the global, if existent there
//FIXME, verify this when multiple signature are activated, e.g. the signature not yet verified are verified automatically by this !
									XOX_SignatureState aState = xDocumentSignatures.getSignatureState(aSignState.getSignatureUUID());
									if(aState != null && aNewSignState != null) {
										//state exists, update it
										xDocumentSignatures.addSignatureState(aNewSignState);
									}
								}
								
								//always reloaded from internal variables
								XOX_SignatureState aState = xDocumentSignatures.getSignatureState(aSignState.getSignatureUUID());
								if(aState != null) {
									//if the state was already loaded for this document, then use the former
									m_aLogger.debug(__FUNCTION__+"signature state retrieved");
									aSignState = aState;
								}
								else {
									//else use this and add to the local storage
									xDocumentSignatures.addSignatureState(aSignState);
									m_aLogger.debug(__FUNCTION__+"signature state added");
								}
							}
							addASignatureState(aSignState);
						}
					} //else there are no signatures !
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
				short valueRet = aDialog1.executeDialog();
				m_aLogger.log("returned: "+valueRet);
				aDialog1.disposeElements();
			} catch (BasicErrorException e) {
				m_aLogger.severe("actionPerformed", "", e);
			}
			//reload the signatures:
			//1) cleanup the signature tree
			removeAllTreeNodes();
			//2) add the new ones and force a verification
			readAllSignatures(true);
		} catch (Throwable e1) {
			m_aLogger.severe("actionPerformed", "", e1);
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#removeButtonPressed()
	 */
	@Override
	public void removeButtonPressed() {
		final String __FUNCTION__ = "removeButtonPressed: ";
		//determine the type of element we are on, we can remove only signatures
		m_aLogger.debug(__FUNCTION__);
		if(m_aTheCurrentlySelectedTreeNode != null) {
			Object oTreeNodeObject  = m_aTheCurrentlySelectedTreeNode.getDataValue();
			if(oTreeNodeObject != null) {
				if(oTreeNodeObject instanceof SignatureTreeElement) {
					SignatureTreeElement aCurrentSignature = (SignatureTreeElement)oTreeNodeObject;
					XOX_SignatureState  aSignature = aCurrentSignature.get_xSignatureState();
					m_aLogger.debug(__FUNCTION__+"UUID "+aSignature.getSignatureUUID());
// instantiate the signature verifier
					Object aDocVerService;
					try {
						aDocVerService = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT, m_xContext);
						if(aDocVerService != null) {
							m_axoxDocumentVerifier = (XOX_DocumentSignaturesVerifier)UnoRuntime.queryInterface(XOX_DocumentSignaturesVerifier.class, aDocVerService);
							if(m_axoxDocumentVerifier != null) {
								if(m_axoxDocumentVerifier.removeDocumentSignature(m_xParentFrame,getDocumentModel(), aSignature.getSignatureUUID())) {
									//signature removed update internal singleton data
									//update the GUI elements
									//reload the signatures:
									//1) cleanup the signature tree
									removeAllTreeNodes();
									//2) add the new ones and force a verification
									readAllSignatures(true);
								}
								else
									m_aLogger.warning(__FUNCTION__+"the requested signature doesn't exist in document !");
							}
							else
								m_aLogger.warning(__FUNCTION__+"XOX_DocumentSignaturesVerifier interface NOT available");
					        // now clean up the verifier
					        ((XComponent) UnoRuntime.queryInterface(XComponent.class, aDocVerService)).dispose();
						}
						else
							m_aLogger.warning(__FUNCTION__+GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT+" Service NOT available");

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					m_aLogger.warning("No signature in tree control node data: "+oTreeNodeObject.getClass().getName());
			}
		}		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
	 */
	@Override
	public void verifyButtonPressed() {
		//first identify the type of selected element
		final String __FUNCTION__ = "verifyButtonPressed: ";

		if(m_aTheCurrentlySelectedTreeNode != null) {
			//disable it, that is un-display it
			Object oTreeNodeObject  = m_aTheCurrentlySelectedTreeNode.getDataValue();
			if(oTreeNodeObject != null) {
				if(oTreeNodeObject instanceof TreeElement) {
					TreeElement aCurrentNode = (TreeElement)oTreeNodeObject;
					if(aCurrentNode.getNodeType() == com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.SIGNATURE) {
						try {
							SignatureTreeElement aSignature = (SignatureTreeElement)aCurrentNode; 
							Object aDocVerService = m_xMCF.createInstanceWithContext(GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT, m_xContext);
							if(aDocVerService != null) {				
								m_axoxDocumentVerifier = (XOX_DocumentSignaturesVerifier)UnoRuntime.queryInterface(XOX_DocumentSignaturesVerifier.class, aDocVerService);
								if(m_axoxDocumentVerifier != null) {
									XOX_SignatureState xTheState = aSignature.get_xSignatureState();
									//verify the signature, updates the aggregated state accordingly
									m_axoxDocumentVerifier.verifyDocumentSignatures(m_xParentFrame,getDocumentModel(), aSignature.get_xSignatureState());
									//get the signature state (all signatures), the signature just checked is updated
									//and update this GUI element accordingly
									aSignature.updateSignaturesStates();
									aSignature.updateSignatureStrings();
									aSignature.EnableDisplay(true);
									//now verify the certificate attached
									XOX_X509Certificate xACert = null;
									if(xTheState != null)
										xACert = xTheState.getSignersCerficate(); 
									if(xACert != null) {
										try {
											xACert.verifyCertificate(m_xParentFrame);
											//now update the string and the text on screen
											CertificateTreeElement aChild = aSignature.getChildCertificate(); 
											if(aChild != null) {
												aChild.updateCertificateStates();
												aChild.updateCertificateStrings();
												aChild.setNodeGraphic(setCertificateNodeGraficStringHelper(xACert));
												aChild.getTheTreeNode().setNodeGraphicURL(aChild.getNodeGraphic());
												//update again the signature string, so the certificate 
												//strings will be udated as
												aSignature.updateSignatureStrings();
												aSignature.EnableDisplay(true);
//update the signature aggregate graphic element
												XOX_SingletonDataAccess	  xSingletonDataAccess = null;
												XOX_DocumentSignaturesState xDocumentSignatures = null;
												
												try {
													xSingletonDataAccess = Helpers.getSingletonDataAccess(m_xContext);
													m_aLogger.debug(" singleton service data "+Helpers.getHashHex(xSingletonDataAccess) );
													xDocumentSignatures = xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(getDocumentModel()), this);
													if(xDocumentSignatures != null) {
														//grab the current aggregated state and update accordingly
														//please note that the signatures already updated it
														//first read the state from both the signature and the certificate, set the new value
														//set to signature ok
														Helpers.updateAggregateSignaturesState(xDocumentSignatures,GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK);
													}
												}
												catch (ClassCastException e) {
													e.printStackTrace();
												} catch (ServiceNotFoundException e) {
													m_aLogger.severe("ctor",GlobConstant.m_sSINGLETON_SERVICE_INSTANCE+" missing!",e);
												} catch (NoSuchMethodException e) {
													m_aLogger.severe("ctor","XOX_SingletonDataAccess missing!",e);
												}
											}
										} catch (Throwable e) {
											m_aLogger.severe(__FUNCTION__,e);
										}

										//copy the attached certificate state as well
										//
									}
								}
								else
									m_aLogger.warning(__FUNCTION__+"XOX_DocumentSignaturesVerifier interface NOT available");
						        // now clean up
						        ((XComponent) UnoRuntime.queryInterface(XComponent.class, aDocVerService)).dispose();
							}
							else
								m_aLogger.warning(__FUNCTION__+GlobConstant.m_sDOCUMENT_VERIFIER_SERVICE_IT+" Service NOT available");

						} catch (Exception e) {
							m_aLogger.severe(__FUNCTION__, e);
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
									aCert.verifyCertificate(m_xParentFrame);
									//now update the string and the text on screen
									aCurrentCertNode.updateCertificateStates();
									aCurrentCertNode.updateCertificateStrings();
									aCurrentCertNode.setNodeGraphic(setCertificateNodeGraficStringHelper(aCert));
									aCurrentCertNode.getTheTreeNode().setNodeGraphicURL(aCurrentCertNode.getNodeGraphic());
									//update the graphic simbol						
									aCurrentCertNode.EnableDisplay(true);
								} catch (Throwable e) {
									m_aLogger.severe(__FUNCTION__,e);
								}
							}
						}
						else
							m_aLogger.warning(__FUNCTION__+"(1) Wrong class type in tree control node data: "+oTreeNodeObject.getClass().getName());						
					}
				}
				else if(m_aTreeRootNode.equals(m_aTheCurrentlySelectedTreeNode)) {
						//check all the signatures
						removeAllTreeNodes();
						readAllSignatures(true);
					}
				else
					m_aLogger.warning(__FUNCTION__+"(2) Wrong class type in tree control node data: "+oTreeNodeObject.getClass().getName());
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
		boolean bEnableRemoveButton = false;
		boolean bEnableVerifyButton = false;
		if(xTheCurrentComp != null) {
			
//get node type and enable/disable	the pushbutton
			TreeElement aCurrentNode = (TreeElement)oTreeNodeObject;
			if(aCurrentNode.getNodeType() == com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.SIGNATURE) {
				bEnableRemoveButton = true;				
				bEnableVerifyButton = true;
			}
			else if(aCurrentNode.getNodeType() == com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.CERTIFICATE) {
				bEnableVerifyButton = true;
			}
//			enableSingleButton(m_sVerifyBtn,bEnableVerifyButton);
//			enableSingleButton(m_sRemoveBtn,bEnableRemoveButton);
			aCurrentNode.EnableDisplay(true);
		}
		else if (m_aTheCurrentlySelectedTreeNode.equals(m_aTreeRootNode)) {
			bEnableVerifyButton = true;
		}
		enableSingleButton(m_sVerifyBtn,bEnableVerifyButton);
		enableSingleButton(m_sRemoveBtn,bEnableRemoveButton);
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

	/* (non-Javadoc)
	 * @see com.sun.star.util.XChangesListener#changesOccurred(com.sun.star.util.ChangesEvent)
	 */
	@Override
	public void changesOccurred(ChangesEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
