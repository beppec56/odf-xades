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

import it.plio.ext.oxsit.security.XOX_SSCDManagement;
import it.plio.ext.oxsit.security.cert.XOX_CertificationPathProcedure;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionChangeListener;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType;

/**
 * lists the CA certificates available
 * @author beppec56
 *
 */
public class DialogCertTreeCA extends DialogCertTreeBase 
		implements	IDialogCertTreeBase,
		XActionListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener
		{

	private static final String DLG_CERT_TREE = "DialogCertTreeCA";

	protected XOX_SSCDManagement	m_axoxAvailableSSCDs;
	
	protected String	m_sVerCRLBtn = "vercrlbtn";
	
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
	public DialogCertTreeCA(XFrame frame, XComponentContext context,
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
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					m_sVerifyBtn,
					"Verifica revoca",
					(short) PushButtonType.STANDARD_value, 6);
/*			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB4(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					m_sAddBtn,
					m_sBtn_AddCertLabel,
					(short) PushButtonType.STANDARD_value, 7);*/
			insertHorizontalFixedLine(
					0, 
					CertifTreeDlgDims.DLGS_BOTTOM_FL_Y(CertifTreeDlgDims.dsHeigh()), 
					CertifTreeDlgDims.dsWidth(), "");

			//must be called AFTER the local init
			super.initializeLocal(DLG_CERT_TREE, m_sDlgListCACertTitle, posX, posY);

//			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
//					super.m_xDialogControl );
//			xTFWindow.addKeyListener( this );
//			Utilities.showControlNames(m_xDlgContainer);
//			Utilities.showNames(m_xDlgModelNameContainer);
			//instantiate the control to get the certificate list
			try {
				Object oCertPath = m_xMCF.createInstanceWithContext(GlobConstant.m_sTRUSTED_ENTITIES_MANAGEMENT_SERVICE_IT, m_xContext);
//object created, we can procced
				XOX_CertificationPathProcedure aCtl =
					(XOX_CertificationPathProcedure)
					UnoRuntime.queryInterface(
							XOX_CertificationPathProcedure.class, oCertPath);
				
		        XStatusIndicator xStatusIndicator = null;
		        if (m_xParentFrame != null) {
		        	//check interface
		        	//
		        	XStatusIndicatorFactory xFact = (XStatusIndicatorFactory)UnoRuntime.queryInterface(XStatusIndicatorFactory.class,m_xParentFrame);
		        	if(xFact != null) {
		        		xStatusIndicator = xFact.createStatusIndicator();
		        		if(xStatusIndicator != null)
		        			xStatusIndicator.start(m_sDlgListCACertStatus, 
		        					aCtl.getCertificationAuthoritiesNumber()); //meaning 100%
		        	}
		        }

		        xStatusIndicator.setValue(1);
		        
				XComponent[] aList = aCtl.getCertificationAuthorities(m_xParentFrame);

				if(aList != null) {
					XMutableTreeNode xCertifNode = addToTreeRootHelper();
					//prepare the status object to give the user some feedback
					//iterate through the list and set the element in the tree, before display
					for(int idx1=0; idx1<aList.length;idx1++) {
						XOX_X509Certificate xoxCert = (XOX_X509Certificate)
									UnoRuntime.queryInterface(XOX_X509Certificate.class, aList[idx1]);
						//perform certificate verification
						//only conformance and path verification
						xoxCert.verifyCertificateCompliance(m_xParentFrame);
						xoxCert.verifyCertificationPath(m_xParentFrame);
						//then add to the tree control
						addX509CertificateToTree(xCertifNode, xoxCert, TreeNodeType.CERTIFICATE_CA);
						if( idx1 % 4 == 0 && xStatusIndicator != null)
							xStatusIndicator.setValue(idx1);
					}

				}
				if(xStatusIndicator != null)
					xStatusIndicator.end();
			} catch (Throwable e) {
				m_aLogger.severe(e);
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

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#addButtonPressed()
	 */
	@Override
	public void addButtonPressed() {
		// TODO Auto-generated method stub
		//add the certificate to ?? check the spec
		m_aLogger.info("cambio stato certificato");
//		addOneSignature();		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#removeButtonPressed()
	 */
	@Override
	public void removeButtonPressed() {
		//not implemented here
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#selectButtonPressed()
	 */
	@Override
	public void selectButtonPressed() {
		//select the certificate on tree for signature
		m_aLogger.info("do operation on selected CA");
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
	 */
	@Override
	public void verifyButtonPressed() {
		//not implemented here
		//execute a verification of the CA certificate, using the available filters 
		if(m_aTheCurrentlySelectedTreeNode != null) {
			Object oTreeNodeObject  = m_aTheCurrentlySelectedTreeNode.getDataValue();
			if(oTreeNodeObject != null) {
				if(oTreeNodeObject instanceof CertificateTreeElement) {
					CertificateTreeElement aCurrentNode = (CertificateTreeElement)oTreeNodeObject;					
//					aCurrentNode.EnableDisplay(false);
//duplicate the certificate, then simply do the check for revocation list					
					XOX_X509Certificate aCert = aCurrentNode.getCertificate();
					if(aCert != null) {
						try {
							aCert.verifyCertificateRevocationState(m_xParentFrame);
							//now update the string and the text on screen
							aCurrentNode.updateCertificateStates();
							aCurrentNode.updateString();
							aCurrentNode.EnableDisplay(true);
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
			boolean bEnableRepButton = false;
			TreeNodeType aNdType = aCurrentNode.getNodeType();
			if(aNdType == TreeNodeType.CERTIFICATE_CA)
				bEnableRepButton = true;
			enableSingleButton(m_sReportBtn,bEnableRepButton);
			aCurrentNode.EnableDisplay(true);
		}
		else {
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
