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

import java.util.LinkedList;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.ooo.ui.BasicDialog;
import it.plio.ext.oxsit.ooo.ui.ControlDims;
import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.TreeNodeType;
import it.plio.ext.oxsit.ooo.ui.test.FakeCertificateInModuleOK;
import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKOCertSignature;
import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKODocument;
import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKODocumentAndSignature;
import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentKOSignature2;
import it.plio.ext.oxsit.ooo.ui.test.SignatureStateInDocumentOK;
import it.plio.ext.oxsit.security.XOX_AvailableSSCDs;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XMouseListener;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeControl;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.EventObject;
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
public class DialogCertTreeSSCDs extends BasicDialog implements
		XActionListener, XMouseListener, XItemListener, XTreeExpansionListener, XSelectionChangeListener {

	private static final String DLG_CERT_TREE = "DialogCertTreeSSCDs";

	private static final String sTree = "certmodtree";
	private static final String sSelect = "selectb";
	private static final String sAdd = "addcertb";
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

	private XTreeControl 	m_xTreeControl = null;
	private Object 			m_oTreeControlModel;	

	private static final String	m_sDispElemsName	= "dispelems";  // the control general, with descriptive text in it
	// the following two fields are needed to be able to change
	// the font at run-time
	private Object				m_xDisplElementModel;				// the service "com.sun.star.awt.UnoControlEditModel"
	@SuppressWarnings("unused")
	private XTextComponent		m_xDisplElement;					// the XTextComponent interface of the control
																	// of the above model
	private XMutableTreeNode 		m_aTheOldNode = null;
	private	Object 					m_oTreeDataModel;
	private XMutableTreeDataModel	m_xTreeDataModel;
	private XMutableTreeNode 		m_aTreeRootNode;

	private String				m_sBtnOKLabel;
	private String				m_sBtn_CancelLabel;
	private String 				m_sBtn_SelDevice;
	private String 				m_sBtn_AddCertLabel;
	private String 				m_sDlgListCertTitle;	
	private String 				m_sFt_Hint_Doc;
	private String 				m_sBtn_RemoveCertLabel;
	private String				m_sBtn_AddCountCertLabel;
	private String				m_sBtn_CreateReport;
	
	private String 				sCertificateElementError;
	private String 				sCertificateElementBroken;

	private String sSignatureInvalid2;
	
	protected XOX_AvailableSSCDs	m_axoxAvailableSSCDs;

	private static final String sEmptyText = "notextcontrol";		//the control without text
	private static final String sEmptyTextLine = "notextcontrolL";		//the 1st line superimposed to the empty text contro
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
		m_logger.enableLogging();
		m_logger.ctor();
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
	private void fillLocalizedString() {
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);

		try {
			m_sBtnOKLabel = m_aRegAcc.getStringFromRegistry( "id_ok" );			
			m_sBtn_CancelLabel = m_aRegAcc.getStringFromRegistry( "id_cancel" );
			m_sBtn_SelDevice = m_aRegAcc.getStringFromRegistry( "id_pb_sel_device" );
			m_sBtn_AddCertLabel = m_aRegAcc.getStringFromRegistry( "id_pb_add_cert" );
			m_sDlgListCertTitle = m_aRegAcc.getStringFromRegistry( "id_title_mod_cert_tree" );
			m_sFt_Hint_Doc = m_aRegAcc.getStringFromRegistry( "id_title_mod_cert_treew" );
			m_sBtn_CreateReport = m_aRegAcc.getStringFromRegistry( "id_pb_cert_report" );
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
		try {
			super.initialize(DLG_CERT_TREE, m_sDlgListCertTitle, CertifTreeDlgDims.dsHeigh(), CertifTreeDlgDims.dsWidth(), posX, posY);

/*	        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
	                XPropertySet.class, m_xDialogControl.getModel());

	        if (xProp != null)
	        	xProp.setPropertyValue(new String("BackgroundColor"),
	        			new Integer(ControlDims.DLG_ABOUT_BACKG_COLOR));*/

			insertFixedText(this,
					CertifTreeDlgDims.DS_COL_1(),
					CertifTreeDlgDims.DS_ROW_0(), 
					CertifTreeDlgDims.dsWidth(), 
					0,
					m_sFt_Hint_Doc
					);
	//inserts the control elements needed to display properties
	//multiline text control used as a light yellow background
			//multiline text control for details
			Object oEdit = insertEditFieldModel(this, this,
					CertifTreeDlgDims.dsTextFieldColumn(),
					CertifTreeDlgDims.DS_ROW_1(),
					CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_1(),
					CertifTreeDlgDims.dsTextFieldWith(),
					2,
					"", sEmptyText, true, true, false, false);

	//now change its background color
			XPropertySet xPSet = (XPropertySet) UnoRuntime
						.queryInterface( XPropertySet.class, oEdit );
			xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_CERT_TREE_BACKG_COLOR));	
			//insert the fixed text lines over the above mentioned element
			insertDisplayLinesOfText();

	//multiline text control for details of tree node element under selection
			m_xDisplElementModel = insertEditFieldModel(this, this,
					CertifTreeDlgDims.dsTextFieldColumn(),
					CertifTreeDlgDims.DS_ROW_1(),
					CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_1(),
					CertifTreeDlgDims.dsTextFieldWith(),
					2,
					"", m_sDispElemsName, true, true, true, true);

			xPSet = (XPropertySet) UnoRuntime
						.queryInterface( XPropertySet.class, m_xDisplElementModel );
			xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_CERT_TREE_BACKG_COLOR));	

	//Insert the tree control
			m_xTreeControl = insertTreeControl(this,
					CertifTreeDlgDims.DS_COL_0(), 
					CertifTreeDlgDims.DS_ROW_1(), 
					CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_1(),
					CertifTreeDlgDims.dsTreeControlWith(), //CertifTreeDlgDims.DS_COL_4() - CertifTreeDlgDims.DS_COL_0(),
					sTree,
					m_sFt_Hint_Doc, 20);

//			XControl xTFControl = m_xDlgContainer.getControl(m_sDispElemsName);
			// add a textlistener that is notified on each change of the
			// controlvalue...
//			m_xDisplElement = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, xTFControl);

			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB3(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					sSelect,
					m_sBtn_SelDevice,
					(short) PushButtonType.STANDARD_value);

			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB4(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					sAdd,
					m_sBtn_AddCertLabel,
					(short) PushButtonType.STANDARD_value);

			insertButton(this,
					CertifTreeDlgDims.DS_COL_PB5(),
					CertifTreeDlgDims.DS_ROW_4(),
					CertifTreeDlgDims.dsBtnWidthCertTree(),
					"sprint",
					m_sBtn_CreateReport,
					(short) PushButtonType.STANDARD_value);

			insertHorizontalFixedLine(
					0, 
					CertifTreeDlgDims.DLGS_BOTTOM_FL_Y(CertifTreeDlgDims.dsHeigh()), 
					CertifTreeDlgDims.dsWidth(), "");		

	//cancel button
			insertButton(this,
					CertifTreeDlgDims.DLGS_BOTTOM_HELP_X(CertifTreeDlgDims.dsWidth()),
					CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
					ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
					"cancb",
					m_sBtn_CancelLabel,
					(short) PushButtonType.CANCEL_value);
	// ok button
			insertButton(this,
					CertifTreeDlgDims.DLGS_BOTTOM_CANCEL_X(CertifTreeDlgDims.dsWidth()),
					CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
					ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
					"okb",
					m_sBtnOKLabel,
					(short) PushButtonType.OK_value);
	
			xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, super.m_xDialogControl);		
			createWindowPeer();
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			m_logger.severe("initialize", e);
		} catch (PropertyVetoException e) {
			m_logger.severe("initialize", e);
		} catch (IllegalArgumentException e) {
			m_logger.severe("initialize", e);
		} catch (WrappedTargetException e) {
			m_logger.severe("initialize", e);
		}
	}

	private void insertDisplayLinesOfText() {
		//now inserts the fixed text lines over the above mentioned element
		for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
			insertFixedText(this,
					CertifTreeDlgDims.TEXT_0X(),
					CertifTreeDlgDims.TEXT_L0Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT*i,
					CertifTreeDlgDims.dsWidth()-CertifTreeDlgDims.TEXT_0X()-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT,
					0,
					"checkit"+i, // a dummy text
					sEmptyTextLine+i
					);
		}
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
		// TODO Auto-generated method stub
		return super.executeDialog();
	}

	private XTreeControl insertTreeControl(XSelectionChangeListener _xActionListener,
			int _nPosX,
			int _nPosY,
			int _nHeight,
			int _nWidth,
			String _sName,
			String _sLabel,
			int _nStep) {

		XTreeControl xTree = null;

		// create a controlmodel at the multiservicefactory of the dialog
		// model...
		try {
			m_oTreeDataModel = m_xMCF.createInstanceWithContext("com.sun.star.awt.tree.MutableTreeDataModel", m_xContext);
			if(m_oTreeDataModel == null) {
				m_logger.severe("insertTreeControl", "the com.sun.star.awt.tree.MutableTreeDataModel wasn't created!");
				return null;
			}

			m_xTreeDataModel = (XMutableTreeDataModel)UnoRuntime.queryInterface( XMutableTreeDataModel.class, m_oTreeDataModel );
			if(m_xTreeDataModel == null) {
				m_logger.severe("insertTreeControl", "the XMutableTreeDataModel not available!");
				return null;
			}

			m_oTreeControlModel = m_xMSFDialogModel.createInstance( "com.sun.star.awt.tree.TreeControlModel" );
			if(m_oTreeControlModel == null) {
				m_logger.severe("insertTreeControl", "the oTreeModel not available!");
				return null;
			}
			XMultiPropertySet xTreeMPSet = (XMultiPropertySet) UnoRuntime.queryInterface( XMultiPropertySet.class, m_oTreeControlModel );
			if(xTreeMPSet == null ) {
				m_logger.severe("insertTreeControl", "no XMultiPropertySet");
				return null;
			}
			
			// Set the properties at the model - keep in mind to pass the
			// property names in alphabetical order!
			xTreeMPSet.setPropertyValues( new String[] {
					"BackgroundColor",
					"DataModel",
					"Editable",
					"Height", 
//			"Label", 
					"Name",
					"PositionX", 
					"PositionY", 
					"RootDisplayed",
					"SelectionType",
//					"ShowsRootHandles",
					"Width"
					},
					new Object[] {
					new Integer( ControlDims.DLG_CERT_TREE_BACKG_COLOR ),
					m_oTreeDataModel, //where the DataModel is attached, need to reattach again?
					new Boolean( true ),
					new Integer( _nHeight ),
//			_sLabel,
					_sName,
					new Integer( _nPosX ),
					new Integer( _nPosY ),
					new Boolean( true /*false*/ ), //RootDisplayed
					new Integer(com.sun.star.view.SelectionType.SINGLE_value),
	//				new Boolean( false ),
					new Integer( _nWidth )					
					} );
			
			// add the model to the NameContainer of the dialog model
			m_xDlgModelNameContainer.insertByName( _sName, m_oTreeControlModel );
			XControl xTreeControl = m_xDlgContainer.getControl( _sName );

//			Utilities.showProperties(this, xTreeMPSet);

			xTree = (XTreeControl) UnoRuntime.queryInterface( XTreeControl.class, xTreeControl );
			m_aTreeRootNode = m_xTreeDataModel.createNode(_sLabel, true);
			if(m_aTreeRootNode == null) {
				m_logger.severe("insertTreeControl", "the Node not available!");
				return null;
			}
			m_xTreeDataModel.setRoot(m_aTreeRootNode);

//			Utilities.showProperties(this, xTreeMPSet);

			// An ActionListener will be notified on the activation of the
			// button...
//			xTree.addActionListener( _xActionListener );
			xTree.addSelectionChangeListener(_xActionListener);
			
			
		} catch (com.sun.star.uno.Exception ex) {
			m_logger.severe("insertTreeControl", ex);
		}
		return xTree;
	}

	private void disableTreeRootNode() {
//grab the master tree control model
		try {
			Object oTreeControlModel = m_xDlgModelNameContainer.getByName( sTree );
			XMultiPropertySet xTreeMPSet = (XMultiPropertySet) UnoRuntime.queryInterface( XMultiPropertySet.class, oTreeControlModel );
			if(xTreeMPSet == null ) {
				m_logger.severe("disableTreeRootNode", "no XMultiPropertySet");
				return;
			}
			Utilities.showProperties(this, xTreeMPSet);

//reattach the same tree model
			// Set the properties at the model - keep in mind to pass the
			// property names in alphabetical order!
			xTreeMPSet.setPropertyValues( new String[] {
					"DataModel",
					"RootDisplayed"
					},
					new Object[] {
					m_oTreeDataModel, //where the DataModel is attached, need to reattach again?
					new Boolean( false )
					} );
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrappedTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * FIXME: this method MUST be changed to the one needed to add a certificate to the tree
	 */
	public void addOneCertificate() {
//create a fake certificate description
		CertificateTreeElement aCert = new CertificateTreeElement(m_xContext, m_xMCF);
		aCert.initialize();
//connect it to the right dialog pane
		aCert.setBackgroundControl(m_xDlgContainer.getControl( sEmptyText ));
		for(int i=0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++ ) {
			aCert.setAControlLine(m_xDlgContainer.getControl( sEmptyTextLine+i ), i);
		}
//add it to the tree root node
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(aCert.getNodeName(), true);
		if(aCert.getNodeGraphic() != null)
			xaCNode.setNodeGraphicURL(aCert.getNodeGraphic());

		xaCNode.setDataValue(aCert);

		try {
//remove the tree control
			m_aTreeRootNode.appendChild(xaCNode);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			m_logger.severe("addOneCertificate", e);
		}
	}

	public void addOneSignature() {

	}

	private void addMultiLineTextDisplayElement(TreeNodeDescriptor aDesc) {		
		// a signature type, add the fillable text control
		XControl xTFControl = m_xDlgContainer.getControl( m_sDispElemsName );
		LinkedList<XControl> aList = aDesc.getList();			
		aList.add(xTFControl);
	}

	private void disableNamedControl(String sTheName) {
		XControl xTFControl = m_xDlgContainer.getControl( sTheName );
		if(xTFControl != null){
			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
			if(xaWNode != null )
				xaWNode.setVisible(false);
		}
	}

	private void disableAllNamedControls() {
		disableNamedControl(m_sDispElemsName);
		disableNamedControl(sEmptyText);
		for(int i = 0; i < SignatureStateInDocument.m_nMAXIMUM_FIELDS; i++)
			disableNamedControl(sEmptyTextLine+i);
	}	

	/* (non-Javadoc)
	 * @see com.sun.star.view.XSelectionChangeListener#selectionChanged(com.sun.star.lang.EventObject)
	 */
	@Override
	public void selectionChanged( com.sun.star.lang.EventObject arg0 ) {
		m_logger.entering("selectionChanged");
		Object oObject = m_xTreeControl.getSelection();
// check if it's a node		
		XMutableTreeNode xaENode = (XMutableTreeNode)UnoRuntime.queryInterface( XMutableTreeNode.class, 
				oObject );
		Object oTreeNodeObject = null;
		TreeElement aCurrentNode = null;
		XComponent xTheCurrentComp = null;
		//disable the previous Node
		if(m_aTheOldNode != null) {
			//disable it, that is un-display it
			oTreeNodeObject  = m_aTheOldNode.getDataValue();
			xTheCurrentComp = (XComponent)UnoRuntime.queryInterface( XComponent.class, oTreeNodeObject );
			if(xTheCurrentComp != null) {
				aCurrentNode = (TreeElement)oTreeNodeObject;
				aCurrentNode.EnableDisplay(false);
			}
		}
		else {// old node null, disable all all the display elements
			disableAllNamedControls();
//disable some of the pushbutton as well			
		}
		//...and set the new old node
		m_aTheOldNode = xaENode;

		if(xaENode != null) {
			oTreeNodeObject  = xaENode.getDataValue();
			xTheCurrentComp = (XComponent)UnoRuntime.queryInterface( XComponent.class, oTreeNodeObject );
			if(xTheCurrentComp != null) {
// get node type and enable/disable	the pushbutton
				aCurrentNode = (TreeElement)oTreeNodeObject;
				boolean bEnableButton = false;
				if(aCurrentNode.getNodeType() == it.plio.ext.oxsit.ooo.ui.TreeElement.TreeNodeType.CERTIFICATE) {
					bEnableButton = true;
				}
				enableSingleButton(sAdd,bEnableButton);
				aCurrentNode.EnableDisplay(true);
			}
			else
				enableSingleButton(sAdd,false);				
		}
	}

	private void enableSingleButton(String sButtonName, boolean bEnable) {
		m_logger.entering("enableSingleButton");
		//grab the button...
		XControl xTFControl = m_xDlgContainer.getControl( sButtonName );
		if(xTFControl != null){
//			Utilities.showInterfaces(xTFControl);
			//...and set state accordingly
			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
			if(xaWNode != null )
				xaWNode.setEnable(bEnable);
		}
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
