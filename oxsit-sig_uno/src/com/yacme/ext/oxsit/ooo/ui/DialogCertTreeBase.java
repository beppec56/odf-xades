/**
 * 
 */
package com.yacme.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.security.XOX_SSCDevice;
import it.plio.ext.oxsit.security.cert.CertificateElementID;
import it.plio.ext.oxsit.security.cert.CertificateElementState;
import it.plio.ext.oxsit.security.cert.CertificateGraphicDisplayState;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.XOX_X509Certificate;
import it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.KeyEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XKeyListener;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.tree.ExpandVetoException;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeControl;
import com.sun.star.awt.tree.XTreeExpansionListener;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.embed.XStorage;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionChangeListener;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import com.yacme.ext.oxsit.ooo.ui.BasicDialog;
import com.yacme.ext.oxsit.ooo.ui.ControlDims;
import com.yacme.ext.oxsit.ooo.ui.TreeElement.TreeNodeType;


/**
 * @author beppe
 *
 */
public class DialogCertTreeBase extends BasicDialog implements
		IDialogCertTreeBase,
		XItemListener,
		XKeyListener,
		XTreeExpansionListener, 
		XSelectionChangeListener {
	protected String 				m_sListCABtn = "listcab"; //lists the available CA
	protected String 				m_sVerifyBtn = "verifyb"; //verify a signature
	protected String 				m_sRemoveBtn = "remob";  //remove a signature
//	protected String 				sCountSig = "countsigb";	//countersign (not yet implemented) 
	protected String 				m_sAddBtn = "addcertb"; //this can be add certificate, or add signature
	protected String 				m_sSelectBtn = "selectb"; //select SSCD
	protected String 				m_sReportBtn = "reportb"; //select SSCD
	protected String 				m_sTreeCtl = "certmodtree"; // the tree element
	private static final String 	m_sTextLinesBackground = "text_back";	//the control without text
	private static final String 	sEmptyTextLine = "text_L";		//the 1st line superimposed to the empty text control
	private static final String		m_sMultilineText	= "multi_l";  // the control general, with descriptive text in it

	private	Object 					m_oTreeDataModel;
	private XMutableTreeDataModel	m_xTreeDataModel;
	private XMutableTreeNode 		m_aTreeRootNode;
	private Object 					m_oTreeControlModel;	
	private XTreeControl 			m_xTreeControl = null;
	protected XMutableTreeNode 		m_aTheCurrentlySelectedTreeNode = null;
	// the following two fields are needed to be able to change
	// the font at run-time
	private Object					m_xDisplElementModel;				// the service "com.sun.star.awt.UnoControlEditModel"

	private String				m_sBtnOKLabel = "id_ok";
	private String				m_sBtn_CancelLabel = "id_cancel";
	private String				m_sBtn_CreateReport = "id_pb_cert_report";

	protected String 			m_sBtn_Verify = "id_pb_verif_sign";
	protected String 			m_sBtn_AddCertLabel = "id_pb_add_cert";
	protected String 			m_sBtn_RemoveCertLabel = "id_pb_rem_cert";
	protected String 			m_sBtn_SelDevice = "id_pb_sel_device";
	protected String 			m_sBtn_ListCA = "id_pb_list_ca";

	//title for dialog and tree structure root element
	protected String 			m_sDlgListCertTitle = "id_title_mod_cert_tree";	
	protected String 			m_sFt_Hint_Doc = "id_title_mod_cert_treew";
	protected String 			m_sDlgListCACertTitle = "id_title_mod_ca_cert";
	protected String			m_sDlgListCACertStatus = "id_mex_build_ca_tree";

	//graphic name string for indications for tree elements 
	private String 				m_sSignatureOrCertificateOK; //signature/certificate ok
	private String 				m_sSignatureNotValidatedOrCertificateNotVerified; //signature ok, but certificate not valid
	private String 				m_sSignatureOrCertificateInvalid; //signature does not mach content: document changed after signature
	private String 				m_sSignatureOrCertificateBroken2; //signature cannot be validated
	private String 				m_sSignatureOrCertificateBroken;
	private String 				sSignatureAdding;
	private String 				sSignatureRemoving;

	//certificate graphic path holders
	protected String			m_sSSCDeviceElement;
	protected String[]			m_sCertificateValidityGraphicName = new String[CertificateGraphicDisplayState.LAST_STATE_value]; 
	private String[] 			m_sCertificateElementGraphicName = new String[CertificateElementState.LAST_STATE_value];

	private XModel			m_xDocumentModel;
	
	///////////// end of graphic name string

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
	 * @param frame
	 * @param context
	 * @param _xmcf
	 */
	public DialogCertTreeBase(XFrame frame, XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(frame, context, _xmcf);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		//fill string for graphics
		String sLoc = Helpers.getExtensionInstallationPath(context);
		if(sLoc != null){
			String aSize = "_26.png"; //for large icons toolbar
//				String aSize = "_16.png"; //for small icons toolbar
			String m_imagesUrl = sLoc + "/images";
//main, depends from application, for now. To be changed
			//TODO change to a name not depending from the application
			m_sSignatureOrCertificateOK = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_OK+aSize; //signature ok
			m_sSignatureNotValidatedOrCertificateNotVerified = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_WARNING+aSize; //signature ok, but certificate not valid
			m_sSignatureOrCertificateInvalid = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID+aSize; //signature does not mach content: document changed after signature
			m_sSignatureOrCertificateBroken2 = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN2+aSize; //
			m_sSignatureOrCertificateBroken = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN+aSize; //
			sSignatureAdding = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_ADDING+aSize; //
			sSignatureRemoving = m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_REMOVING+aSize; //

			m_sSSCDeviceElement = m_imagesUrl + "/"+GlobConstant.m_sSSCD_ELEMENT +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.NOT_VERIFIED_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_UNKNOWN +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.OK_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_OK+aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.WARNING_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_WARNING +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.NO_DATE_VALID_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN2 +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.NOT_COMPLIANT_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_BROKEN +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.NOT_VALID_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_CHECKED_INVALID +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.MARKED_TO_BE_ADDED_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_ADDING +aSize;
			m_sCertificateValidityGraphicName[CertificateGraphicDisplayState.MARKED_TO_BE_REMOVED_value]
			                                  	= m_imagesUrl + "/"+GlobConstant.m_nCERTIFICATE_REMOVING +aSize;
//the certificate elements graphic name string
			m_sCertificateElementGraphicName[CertificateElementState.NOT_VERIFIED_value] = "";
			m_sCertificateElementGraphicName[CertificateElementState.OK_value] = "";
			m_sCertificateElementGraphicName[CertificateElementState.BROKEN_value] =
								m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_BROKEN +aSize;
			m_sCertificateElementGraphicName[CertificateElementState.WARNING_value] =
								m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_WARNING +aSize;
			m_sCertificateElementGraphicName[CertificateElementState.INVALID_value] =
								m_imagesUrl + "/"+GlobConstant.m_nCERT_ELEM_INVALID +aSize;
		}
		else
			m_aLogger.severe("ctor","no package location !");
	}

	/**
	 * prepare the strings for the dialogs
	 */
	protected void fillLocalizedString() {
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);

		try {
			m_sBtnOKLabel = m_aRegAcc.getStringFromRegistry( m_sBtnOKLabel );			
			m_sBtn_CancelLabel = m_aRegAcc.getStringFromRegistry( m_sBtn_CancelLabel );
			m_sBtn_CreateReport = m_aRegAcc.getStringFromRegistry( m_sBtn_CreateReport );
			m_sBtn_Verify = m_aRegAcc.getStringFromRegistry( m_sBtn_Verify );
			m_sBtn_RemoveCertLabel = m_aRegAcc.getStringFromRegistry( m_sBtn_RemoveCertLabel );
			m_sDlgListCertTitle = m_aRegAcc.getStringFromRegistry( m_sDlgListCertTitle );
			m_sBtn_SelDevice = m_aRegAcc.getStringFromRegistry( m_sBtn_SelDevice );
			m_sBtn_AddCertLabel = m_aRegAcc.getStringFromRegistry( m_sBtn_AddCertLabel );
			m_sFt_Hint_Doc = m_aRegAcc.getStringFromRegistry( m_sFt_Hint_Doc );
			m_sBtn_ListCA = m_aRegAcc.getStringFromRegistry( m_sBtn_ListCA );
			m_sDlgListCACertTitle = m_aRegAcc.getStringFromRegistry( m_sDlgListCACertTitle );
			m_sDlgListCACertStatus = m_aRegAcc.getStringFromRegistry( m_sDlgListCACertStatus );

		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("fillLocalizedString", e);
		}
		m_aRegAcc.dispose();
	}

	public void initializeLocal(String _sName, String _sTitle, int posX, int posY) throws BasicErrorException {
		m_aLogger.entering("initialize");
		try {
			super.initialize(_sName, _sTitle, CertifTreeDlgDims.dsHeigh(), CertifTreeDlgDims.dsWidth(), posX, posY);
			//inserts the control elements needed to display properties
			//multiline text control used as a light yellow background
					//multiline text control for details
			Object oEdit = insertEditFieldModel(this, /*this*/null,
							CertifTreeDlgDims.dsTextFieldColumn(),
							CertifTreeDlgDims.DS_ROW_0(),
							CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_0(),
							CertifTreeDlgDims.dsTextFieldWith(),
							-1,
							"", m_sTextLinesBackground, true, true, false, false);
			//now change its background color
			XPropertySet xPSet = (XPropertySet) UnoRuntime
								.queryInterface( XPropertySet.class, oEdit );
			xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_CERT_TREE_BACKG_COLOR));

			//insert the fixed text lines layed over the above mentioned element
			insertDisplayLinesOfText();

			//multiline text control for details of tree node element under selection
			m_xDisplElementModel = insertEditFieldModel(this, /*this*/null,
							CertifTreeDlgDims.dsTextFieldColumn(),
							CertifTreeDlgDims.DS_ROW_0(),
							CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_0(),
							CertifTreeDlgDims.dsTextFieldWith(),
							-1,
							"", m_sMultilineText, true, true, true, true);

			xPSet = (XPropertySet) UnoRuntime
								.queryInterface( XPropertySet.class, m_xDisplElementModel );
			xPSet.setPropertyValue(new String("BackgroundColor"), new Integer(ControlDims.DLG_CERT_TREE_BACKG_COLOR));	

			//Insert the tree control
			m_xTreeControl = insertTreeControl(this,
							CertifTreeDlgDims.DS_COL_0(), 
							CertifTreeDlgDims.DS_ROW_0(), 
							CertifTreeDlgDims.DS_ROW_3()-CertifTreeDlgDims.DS_ROW_0(),
							CertifTreeDlgDims.dsTreeControlWith(), //CertifTreeDlgDims.DS_COL_4() - CertifTreeDlgDims.DS_COL_0(),
							m_sTreeCtl,
							m_sFt_Hint_Doc, 10);
			insertButton(this,
							CertifTreeDlgDims.DS_COL_PB5(),
							CertifTreeDlgDims.DS_ROW_4(),
							CertifTreeDlgDims.dsBtnWidthCertTree(),
							m_sReportBtn,
							m_sBtn_CreateReport,
							(short) PushButtonType.STANDARD_value, 8);
					//cancel button
			insertButton(this,
							CertifTreeDlgDims.DLGS_BOTTOM_HELP_X(CertifTreeDlgDims.dsWidth()),
							CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
							ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
							"cancb",
							m_sBtn_CancelLabel,
							(short) PushButtonType.CANCEL_value, 2);
			// ok button
			insertButton(this,
							CertifTreeDlgDims.DLGS_BOTTOM_CANCEL_X(CertifTreeDlgDims.dsWidth()),
							CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
							ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
							"okb",
							m_sBtnOKLabel,
							(short) PushButtonType.OK_value, 1);

			xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, super.m_xDialogControl);		
			createWindowPeer();
			disableAllNamedControls();

		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			m_aLogger.severe("initialize", e);
		} catch (PropertyVetoException e) {
			m_aLogger.severe("initialize", e);
		} catch (IllegalArgumentException e) {
			m_aLogger.severe("initialize", e);
		} catch (WrappedTargetException e) {
			m_aLogger.severe("initialize", e);
		}
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
				m_aLogger.severe("insertTreeControl", "the com.sun.star.awt.tree.MutableTreeDataModel wasn't created!");
				return null;
			}

			m_xTreeDataModel = (XMutableTreeDataModel)UnoRuntime.queryInterface( XMutableTreeDataModel.class, m_oTreeDataModel );
			if(m_xTreeDataModel == null) {
				m_aLogger.severe("insertTreeControl", "the XMutableTreeDataModel not available!");
				return null;
			}
			m_aTreeRootNode = m_xTreeDataModel.createNode(_sLabel, true);
			if(m_aTreeRootNode == null) {
				m_aLogger.severe("insertTreeControl", "the Node not available!");
				return null;
			}
			m_xTreeDataModel.setRoot(m_aTreeRootNode);

			m_oTreeControlModel = m_xMSFDialogModel.createInstance( "com.sun.star.awt.tree.TreeControlModel" );
			if(m_oTreeControlModel == null) {
				m_aLogger.severe("insertTreeControl", "the oTreeModel not available!");
				return null;
			}

			XMultiPropertySet xTreeMPSet = (XMultiPropertySet) UnoRuntime.queryInterface( XMultiPropertySet.class, m_oTreeControlModel );
			if(xTreeMPSet == null ) {
				m_aLogger.severe("insertTreeControl", "no XMultiPropertySet");
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
//					"RootDisplayed",
					"SelectionType",
					"ShowsRootHandles",
					"Step",
					"TabIndex",
					"Width"
					},
					new Object[] {
					new Integer( ControlDims.DLG_CERT_TREE_BACKG_COLOR ),
					m_oTreeDataModel, //where the DataModel is attached
					new Boolean( true ),
					new Integer( _nHeight ),
//			_sLabel,
					_sName,
					new Integer( _nPosX ),
					new Integer( _nPosY ),
//					new Boolean( false ), //RootDisplayed, but does not function...
					new Integer(com.sun.star.view.SelectionType.SINGLE_value),
					new Boolean( true ),
					new Integer( 0 ),//Step
					new Short( (short)_nStep ), //TabIndex
					new Integer( _nWidth )					
					} );

//			Utilities.showProperties(m_oTreeControlModel, xTreeMPSet);
			// add the control model to the NameContainer of the dialog model
			m_xDlgModelNameContainer.insertByName( _sName, m_oTreeControlModel );
			XControl xTreeControl = m_xDlgContainer.getControl( _sName );

			xTree = (XTreeControl) UnoRuntime.queryInterface( XTreeControl.class, xTreeControl );
			xTree.addSelectionChangeListener(_xActionListener);

			//////////////////////////
/*			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
					xTreeControl );
			xTFWindow.addFocusListener( this );
			xTFWindow.addKeyListener( this );*/
			
		} catch (com.sun.star.uno.Exception ex) {
			m_aLogger.severe("insertTreeControl", ex);
		}
		return xTree;
	}

	private void insertDisplayLinesOfText() {
		//now inserts the fixed text lines over the above mentioned element
		for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
			insertFixedText(this,
					CertifTreeDlgDims.TEXT_0X(),
					CertifTreeDlgDims.TEXT_L0Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT*i,
					CertifTreeDlgDims.dsWidth()-CertifTreeDlgDims.TEXT_0X()-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT,
					-1,
					"l "+i, // a dummy text
					sEmptyTextLine+i
					);
		}
	}

	private void removeTreeNodeHelper(XMutableTreeNode _aStartNode) {
//		m_aLogger.entering("removeTreeNodeHelper "+m_nNestedLevel);
		//get the XTreeNode interface
		//child index
		Object oObj = new Object();
		int childIndex = _aStartNode.getChildCount();
		try {
			while(childIndex > 0) {
				childIndex--;
				oObj = _aStartNode.getChildAt(childIndex);
				XMutableTreeNode aNode = (XMutableTreeNode)UnoRuntime.queryInterface(XMutableTreeNode.class, oObj);
				m_xTreeControl.collapseNode(aNode);
				if(aNode.getChildCount() == 0) {
					//node with no child, remove it
					//FIXME: first remove the child node data
					// for now, only cleared
					Object oTreeNodeObject  = aNode.getDataValue();
					if(oTreeNodeObject != null) {
						if(oTreeNodeObject instanceof TreeElement) {
							TreeElement aCurrentNode = (TreeElement)oTreeNodeObject;
							aCurrentNode.dispose();
						}
						else
							m_aLogger.warning("Wrong class type in tree control node data: "+oTreeNodeObject.getClass().getName());
					}
					
					aNode.setDataValue(null);
					m_xTreeControl.collapseNode(aNode);
					//then remove it
					_aStartNode.removeChildByIndex(childIndex);
				}
				else
					removeTreeNodeHelper(aNode); //recursive call
				childIndex = _aStartNode.getChildCount();
			}
		} catch (java.lang.Exception e) {
			m_aLogger.severe("Index: "+childIndex,e);
		}
	}

	protected void removeAllTreeNodes() {
//first remove all selection from the tree control
		m_xTreeControl.clearSelection();
		removeTreeNodeHelper(m_aTreeRootNode);		
	}

	private XMutableTreeNode addMultiLineToTreeRootHelper(BaseGeneralNodeTreeElement aSSCDnode) {		
		//connect it to the right dialog pane
		aSSCDnode.setBackgroundControl(m_xDlgContainer.getControl( m_sTextLinesBackground ));
		for(int i=0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++ ) {
			aSSCDnode.setAControlLine(m_xDlgContainer.getControl( sEmptyTextLine+i ), i);
		}
		//add it to the tree root node
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(aSSCDnode.getNodeName(), true);
		if(aSSCDnode.getNodeGraphic() != null)
			xaCNode.setNodeGraphicURL(aSSCDnode.getNodeGraphic());

		xaCNode.setDataValue(aSSCDnode);
		try {
			m_aTreeRootNode.appendChild(xaCNode);			
			m_xTreeControl.expandNode(m_aTreeRootNode);			
		} catch (IllegalArgumentException e) {
			m_aLogger.severe("addMultiLineToTreeRootHelper", e);
		} catch (ExpandVetoException e) {
			m_aLogger.severe("addMultiLineToTreeRootHelper", e);
		}
		return xaCNode;
	}

	protected XMutableTreeNode addToTreeRootHelper() {		
		//add the device to the dialog, a single node, with the device as a description
		return 		m_xTreeDataModel.createNode("Elenco CA", true);
	}
	
	protected XMutableTreeNode addSSCDToTreeRootHelper(XOX_SSCDevice _aSSCDev) {		
		//add the device to the dialog, a single node, with the device as a description
		SSCDTreeElement aSSCDnode = new SSCDTreeElement(m_xContext,m_xMCF);
		aSSCDnode.initialize();
		aSSCDnode.setNodeGraphic(m_sSSCDeviceElement);
		aSSCDnode.setSSCDDATA(_aSSCDev);
		return  addMultiLineToTreeRootHelper(aSSCDnode);
	}

	/**
	 * analyzes the certificate value to build a graphic representation for the certificate
	 * 
	 * @param _aCertif
	 * @return
	 */
	protected String setCertificateNodeGraficStringHelper(XOX_X509Certificate _aCertif) {
		//get the certificate state
		int nCertState = _aCertif.getCertificateState();
		//get the certificate state check conditions
		int ncertState = _aCertif.getCertificateStateConditions();
		//get the Certification Path state
		//get the Certification Path state conditions
		//now check
		switch(nCertState) {
			case CertificateState.EXPIRED_value:
			case CertificateState.NOT_ACTIVE_value:
				return m_sSignatureOrCertificateBroken2;
			case CertificateState.ERROR_IN_EXTENSION_value:
			case CertificateState.MISSING_EXTENSION_value:
			default:
				return m_sSignatureNotValidatedOrCertificateNotVerified;
			case CertificateState.OK_value:
				return m_sSignatureOrCertificateOK;
			case CertificateState.NOT_YET_VERIFIED_value:
			case CertificateState.NOT_VERIFIABLE_value:
			case CertificateState.NOT_COMPLIANT_value:
				return m_sSignatureOrCertificateBroken;
			case CertificateState.MALFORMED_CERTIFICATE_value:
			case CertificateState.CORE_CERTIFICATE_ELEMENT_INVALID_value:
			case CertificateState.REVOKED_value:
				return m_sSignatureOrCertificateInvalid;
		}
	}

	protected void addCACertificateToTree(XMutableTreeNode _aParentNode, XOX_X509Certificate _aCertif) {

    //this adds the starting point of the certification path, from now on we have only CA certificates
		//grab its state
		//add the certificate path
		//first see if there is a path
		String sPathGraph = "";
		XOX_X509Certificate xCPath = _aCertif.getCertificationPath();

		int aState = _aCertif.getCertificateElementErrorState(GlobConstant.m_sX509_CERTIFICATE_CERTPATH);
		sPathGraph = m_sCertificateElementGraphicName[aState];

		XOX_X509CertificateDisplay xoxCDisp = _aCertif.getCertificateDisplayObj();

		XMutableTreeNode xNode;
		//check if the certification path is available
		if(xCPath != null) {
			xNode = addEmptyDataTreeElement(_aParentNode, TreeNodeType.CERTIFICATION_PATH,
					xoxCDisp.getCertificateElementLocalizedName(CertificateElementID.CERTIFICATION_PATH), 
				sPathGraph);

			//call the certificate adder...
			addX509CertificateToTree(xNode, xCPath, TreeNodeType.CERTIFICATE_CA);
		}
		else {//add the empty node only if there is an error
			if( aState != CertificateElementState.OK_value)
				addEmptyDataTreeElement(_aParentNode, TreeNodeType.CERTIFICATION_PATH,
							xoxCDisp.getCertificateElementLocalizedName(CertificateElementID.CERTIFICATION_PATH), 
						sPathGraph);				
		}
	}

	////// methods to manage the certificate display
	protected void addX509CertificateToTree(XMutableTreeNode _aParentNode, XOX_X509Certificate _aCertif, TreeNodeType _NodeType) {
		//instantiate a certificate node
		CertificateTreeElement aNewNode = new CertificateTreeElement(m_xContext,m_xMCF);
		//set the type
		aNewNode.setNodeType(_NodeType);
		//now set the certificate graphic state
		//and then sets the strings, according to the state from the certificate itself
//		aNewNode.m_sStringList[CertificateTreeElement.] = m_sCertificateState[_aCertif.getCertificateState()]; 
		aNewNode.setCertificateData(_aCertif);
		//get the data set and add a string to the certificate object
		String sCertificateAbstract = "";
		
		for(int i = 0; i< CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
			String sData = aNewNode.m_sStringList[i];
			if(sData.length() > 1)
				sCertificateAbstract = sCertificateAbstract+sData.substring(1)+"\n";
			else
				sCertificateAbstract = sCertificateAbstract+"\n";
		}

		_aCertif.getCertificateDisplayObj().setCertificateElementCommentString(CertificateElementID.GENERAL_CERTIFICATE_ABSTRACT,
				sCertificateAbstract);
		//connect it to the right dialog pane
		aNewNode.setBackgroundControl(m_xDlgContainer.getControl( m_sTextLinesBackground ));
		for(int i=0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++ ) {
			aNewNode.setAControlLine(m_xDlgContainer.getControl( sEmptyTextLine+i ), i);
		}
		//create a new node to be used for this element
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(aNewNode.getNodeName(), true);

		aNewNode.setNodeGraphic(setCertificateNodeGraficStringHelper(_aCertif));
		if(aNewNode.getNodeGraphic() != null)
			xaCNode.setNodeGraphicURL(aNewNode.getNodeGraphic());

		//link to our data
		xaCNode.setDataValue(aNewNode);
		//add it to the parent node
		try {
			_aParentNode.appendChild(xaCNode);			
			m_xTreeControl.expandNode(_aParentNode);
		} catch (IllegalArgumentException e) {
			m_aLogger.severe("addQualifiedCertificateToTree", e);
		} catch (ExpandVetoException e) {
			// TODO Auto-generated catch block
			m_aLogger.severe("addQualifiedCertificateToTree", e);
		}
		
		//grab the display interface of the certificate
		XOX_X509CertificateDisplay certDisp = (XOX_X509CertificateDisplay)UnoRuntime.queryInterface(XOX_X509CertificateDisplay.class, _aCertif);
		
		//now add the rest of the data
		//add the version
		addVariablePitchTreeElementAndState(xaCNode, TreeNodeType.VERSION, 
				certDisp.getCertificateElementLocalizedName(CertificateElementID.VERSION), certDisp.getVersion(),
				_aCertif.getCertificateElementErrorState(GlobConstant.m_sX509_CERTIFICATE_VERSION));		
		//add the serial number
		addVariablePitchTreeElement(xaCNode,TreeNodeType.SERIAL_NUMBER,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.SERIAL_NUMBER),certDisp.getSerialNumber());
		//add the issuer full description		
		addVariablePitchTreeElementAndState(xaCNode,TreeNodeType.ISSUER,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.ISSUER),
				certDisp.getIssuerName(),
				_aCertif.getCertificateElementErrorState(GlobConstant.m_sX509_CERTIFICATE_ISSUER));
		//add the not valid before
		addVariablePitchTreeElementAndState(xaCNode,TreeNodeType.VALID_FROM,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.NOT_BEFORE),
				certDisp.getNotValidBefore(),
				_aCertif.getCertificateElementErrorState(GlobConstant.m_sX509_CERTIFICATE_NOT_BEFORE));
		//add the not valid after
		addVariablePitchTreeElementAndState(xaCNode,TreeNodeType.VALID_TO,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.NOT_AFTER),
				certDisp.getNotValidAfter(),
				_aCertif.getCertificateElementErrorState(GlobConstant.m_sX509_CERTIFICATE_NOT_AFTER));
		//add the subject
		addVariablePitchTreeElement(xaCNode,TreeNodeType.SUBJECT,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.SUBJECT),certDisp.getSubjectName());
		//add the subject signature algorithm
		addVariablePitchTreeElement(xaCNode,TreeNodeType.SUBJECT_ALGORITHM,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.SUBJECT_ALGORITHM),
				certDisp.getSubjectPublicKeyAlgorithm());
		//add the subject public key (multiline, fixed pitch)
		addFixedPitchTreeElement(xaCNode,TreeNodeType.PUBLIC_KEY,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.SUBJECT_PUBLIC_KEY),
				certDisp.getSubjectPublicKeyValue());
		//add the thumbprint signature algorithm
		addVariablePitchTreeElement(xaCNode,TreeNodeType.SIGNATURE_ALGORITHM,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.THUMBPRINT_SIGNATURE_ALGORITHM),certDisp.getSignatureAlgorithm());
		//add the SHA1 thumbprint 
		addFixedPitchTreeElement(xaCNode,TreeNodeType.THUMBPRINT_SHA1,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.CERTIFICATE_SHA1_THUMBPRINT),
				certDisp.getSHA1Thumbprint());
		//add the MDA5 thumbprint
		addFixedPitchTreeElement(xaCNode,TreeNodeType.THUMBPRINT_MD5,
				certDisp.getCertificateElementLocalizedName(CertificateElementID.CERTIFICATE_MD5_THUMBPRINT),
				certDisp.getMD5Thumbprint());

		//add the critical extensions
		try {
			String[] aCritExt = certDisp.getCriticalCertificateExtensionOIDs();
			if(aCritExt != null) {
				//then there are extension marked critical
				//add the main node
				//the root node for extensions should see for the aggregate state of all
				//the critical extensions
				int aggregateState = 0;
				for(int i=0; i<aCritExt.length;i++) {
					int temp = _aCertif.getCertificateElementErrorState(aCritExt[i]);
					if(temp > aggregateState)
						aggregateState = temp;
				}
				XMutableTreeNode xNode = addEmptyDataTreeElement(xaCNode,
							TreeNodeType.EXTENSIONS_CRITICAL,
							certDisp.getCertificateElementLocalizedName(CertificateElementID.CRITICAL_EXTENSION),
							m_sCertificateElementGraphicName[aggregateState]);
				for(int i=0; i<aCritExt.length;i++) {
					addVariablePitchTreeElementAndState(xNode,TreeNodeType.EXTENSIONS_CRITICAL,
							certDisp.getCertificateExtensionLocalizedName(aCritExt[i]),
							certDisp.getCertificateExtensionValueString(aCritExt[i]),
							_aCertif.getCertificateElementErrorState(aCritExt[i]));
				}
			}
		} catch (Exception e) {
			m_aLogger.severe("addQualifiedCertificateToTree", e);
		}

		//add the non critical extensions
		try {
			String[] aNotCtritExt = certDisp.getNotCriticalCertificateExtensionOIDs();
			if(aNotCtritExt != null) {
			//then there are extension NOT marked critical
			//add the main node
				//the root node for extensions build the aggregate state of all
				//the NOT critical extensions
				int aggregateState = 0;
				for(int i=0; i<aNotCtritExt.length;i++) {
					int temp = _aCertif.getCertificateElementErrorState(aNotCtritExt[i]);
					if(temp > aggregateState)
						aggregateState = temp;
				}
				XMutableTreeNode xNode = addEmptyDataTreeElement(xaCNode,
							TreeNodeType.EXTENSIONS_NON_CRITICAL, 
							certDisp.getCertificateElementLocalizedName(CertificateElementID.NOT_CRITICAL_EXTENSION),
							m_sCertificateElementGraphicName[aggregateState]);
				for(int i=0; i<aNotCtritExt.length;i++) {
					if(aNotCtritExt[i].equalsIgnoreCase("2.5.29.14") ||
							aNotCtritExt[i].equalsIgnoreCase("2.5.29.35")) 
						addFixedPitchTreeElementAndState(xNode,TreeNodeType.EXTENSIONS_NON_CRITICAL,
								certDisp.getCertificateExtensionLocalizedName(aNotCtritExt[i]),
								certDisp.getCertificateExtensionValueString(aNotCtritExt[i]),
								_aCertif.getCertificateElementErrorState(aNotCtritExt[i]));
					else
						addVariablePitchTreeElementAndState(xNode,TreeNodeType.EXTENSIONS_NON_CRITICAL,
								certDisp.getCertificateExtensionLocalizedName(aNotCtritExt[i]),
								certDisp.getCertificateExtensionValueString(aNotCtritExt[i]),
								_aCertif.getCertificateElementErrorState(aNotCtritExt[i]));
				}
			}
		} catch (Exception e) {
			m_aLogger.severe("addQualifiedCertificateToTree", e);
		}
		//add the certificate path
		//first see if there is a path
		addCACertificateToTree(xaCNode, _aCertif);
	}

	//test function, remove when ready!
	public void addOneSignature() {
		//create a fake certificate description
		SignatureTreeElement aCert = new SignatureTreeElement(m_xContext, m_xMCF);
		aCert.initialize();
		addOneFakeCertificateToTreeRootHelper(aCert);
	}

	protected XMutableTreeNode addOneFakeCertificateToTreeRootHelper(BaseCertificateTreeElement aCert) {		
		//connect it to the right dialog pane
		aCert.setBackgroundControl(m_xDlgContainer.getControl( m_sTextLinesBackground ));
		for(int i=0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++ ) {
			aCert.setAControlLine(m_xDlgContainer.getControl( sEmptyTextLine+i ), i);
		}
		//add it to the tree root node
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(aCert.getNodeName(), true);
		if(aCert.getNodeGraphic() != null)
			xaCNode.setNodeGraphicURL(aCert.getNodeGraphic());

		xaCNode.setDataValue(aCert);
		try {
			m_aTreeRootNode.appendChild(xaCNode);			
			m_xTreeControl.expandNode(m_aTreeRootNode);			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			m_aLogger.severe("addOneCertificate", e);
		} catch (ExpandVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xaCNode;
	}

	//////////////////////////
	private XMutableTreeNode addMultilineTreeElementHelper(XMutableTreeNode _Node, MultilineTreeElementBase _aElm, String _sLabel) {
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(_sLabel, true);
		if(_aElm.getNodeGraphic() != null)
			xaCNode.setNodeGraphicURL(_aElm.getNodeGraphic());

		xaCNode.setDataValue(_aElm);
		try {
			//add it to the tree node
			_Node.appendChild(xaCNode);			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			m_aLogger.severe("addMultilineTreeElementHelper", e);
		}
		return xaCNode;		
	}

	//////////////////////////
	private XMutableTreeNode addFixedPitchTreeElement(XMutableTreeNode _Node, TreeNodeType _aType, String _sLabel, String _sContents) {
		FixedFontPitchTreeElement aElem = new FixedFontPitchTreeElement(m_xContext, m_xMCF, _sContents, m_xDlgContainer.getControl( m_sMultilineText ));
		aElem.setNodeType(_aType);
		return addMultilineTreeElementHelper(_Node, aElem, _sLabel);
	}

	//////////////////////////
	private XMutableTreeNode addFixedPitchTreeElementAndState(XMutableTreeNode _Node, TreeNodeType _aType, String _sLabel, String _sContents, int _nExtensionState) {
		FixedFontPitchTreeElement aElem = new FixedFontPitchTreeElement(m_xContext, m_xMCF, _sContents, m_xDlgContainer.getControl( m_sMultilineText ));
		aElem.setNodeGraphic(
				m_sCertificateElementGraphicName[_nExtensionState]);
		aElem.setNodeType(_aType);
		return addMultilineTreeElementHelper(_Node, aElem, _sLabel);
	}

	//////////////////////////
	private XMutableTreeNode addVariablePitchTreeElement(XMutableTreeNode _Node, TreeNodeType _aType, String _sLabel, String _sContents) {
		MultilineTreeElementBase aElem = new MultilineTreeElementBase(m_xContext, m_xMCF, _sContents, m_xDlgContainer.getControl( m_sMultilineText ));
		aElem.setNodeType(_aType);
		return addMultilineTreeElementHelper(_Node, aElem, _sLabel);
	}

	//////////////////////////
	private XMutableTreeNode addVariablePitchTreeElementAndState(XMutableTreeNode _Node, 
							TreeNodeType _aType, String _sLabel, String _sContents, int _nExtensionState) {
		MultilineTreeElementBase aElem = new MultilineTreeElementBase(m_xContext, m_xMCF, _sContents, m_xDlgContainer.getControl( m_sMultilineText ));
		aElem.setNodeGraphic(
				m_sCertificateElementGraphicName[_nExtensionState]);
		aElem.setNodeType(_aType);
		return addMultilineTreeElementHelper(_Node, aElem, _sLabel);
	}

	//////////////////////////
	private XMutableTreeNode addEmptyDataTreeElement(XMutableTreeNode _Node, TreeNodeType _aType, String _sLabel, String _sGraphic) {
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(_sLabel, true);
		xaCNode.setDataValue(null);
		xaCNode.setNodeGraphicURL(_sGraphic);
		try {
			//add it to the tree node
			_Node.appendChild(xaCNode);			
		} catch (IllegalArgumentException e) {
			m_aLogger.severe("addEmptyDataTreeElement", e);
		}
		return xaCNode;		
	}

	////////////////////////////////////
	/// methods to manage the UI

	// next five methods MUST be implemented by subclasses to
	// manage the buttons behavior 

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#addButtonPressed()
	 */
	@Override
	public void addButtonPressed() {
		// TODO Auto-generated method stub
		m_aLogger.log("addButtonPressed");		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#removeButtonPressed()
	 */
	@Override
	public void removeButtonPressed() {
		// TODO Auto-generated method stub
		m_aLogger.log("removeButtonPressed");
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#reportButtonPressed()
	 */
	@Override
	public void reportButtonPressed() {
		//prints a report of the selected CERTIFICATE
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
//obtain the Display interface							
							XOX_X509CertificateDisplay aDisplay = aCert.getCertificateDisplayObj();
							XComponent aCeComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, aCert);
							if(aDisplay != null)
								aDisplay.generateCertificateReport(aCeComp);
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

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#selectButtonPressed()
	 */
	@Override
	public void selectButtonPressed() {
		// TODO Auto-generated method stub
		m_aLogger.log("selectButtonPressed");
		//very crl status of the selected element
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.IDialogCertTreeBase#verifyButtonPressed()
	 */
	@Override
	public void verifyButtonPressed() {
		// TODO Auto-generated method stub
		m_aLogger.log("verifyButtonPressed");		
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
			if (sName.equals(m_sAddBtn)) {
				m_aLogger.log("actionPerformed","action: "+sName);
				addButtonPressed();
			} else if (sName.equals(m_sSelectBtn)) {
				selectButtonPressed();
			} else if (sName.equals(m_sReportBtn)) {
				reportButtonPressed();
			} else if (sName.equals(m_sVerifyBtn)) {
				m_aLogger.log("actionPerformed","action: "+sName);
				verifyButtonPressed();
			} else if (sName.equals(m_sRemoveBtn)) {
				m_aLogger.log("actionPerformed","action: "+sName);
				removeButtonPressed();
			}
			else {
				m_aLogger.warning("actionPerformed","Activated, unimplemented: " + sName);
			}
		} catch (com.sun.star.uno.Exception ex) {
			/*
			 * perform individual exception handling here. Possible exception
			 * types are: com.sun.star.lang.WrappedTargetException,
			 * com.sun.star.beans.UnknownPropertyException,
			 * com.sun.star.uno.Exception
			 */
			m_aLogger.severe("actionPerformed", ex);
		}
	}

	private void disableNamedControl(String sTheName) {
		XControl xTFControl = m_xDlgContainer.getControl( sTheName );
		if(xTFControl != null){
			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
			if(xaWNode != null )
				xaWNode.setVisible(false);
		}
	}

	protected void disableAllNamedControls() {
		disableNamedControl(m_sMultilineText);
		disableNamedControl(m_sTextLinesBackground);
		for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++)
			disableNamedControl(sEmptyTextLine+i);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.view.XSelectionChangeListener#selectionChanged(com.sun.star.lang.EventObject)
	 */
	@Override
	public void selectionChanged( com.sun.star.lang.EventObject _eventObject ) {
		Object oObject = m_xTreeControl.getSelection();
// check if it's a node		
		XMutableTreeNode xaENode = (XMutableTreeNode)UnoRuntime.queryInterface( XMutableTreeNode.class, 
				oObject );
		//disable the previous Node (which still is the current one)
		if(m_aTheCurrentlySelectedTreeNode != null) {
			//disable it, that is un-display it
			Object oTreeNodeObject  = m_aTheCurrentlySelectedTreeNode.getDataValue();
			if(oTreeNodeObject != null) {
				if(oTreeNodeObject instanceof TreeElement) {
					TreeElement aCurrentNode = (TreeElement)oTreeNodeObject;
					aCurrentNode.EnableDisplay(false);
				}
				else
					m_aLogger.warning("Wrong class type in tree control node data: "+oTreeNodeObject.getClass().getName());
			}
		}
		else {// old node null, disable all all the display elements
			disableAllNamedControls();
		}
		//...and set the new old node
		m_aTheCurrentlySelectedTreeNode = xaENode;

		if(xaENode != null) {
			checkButtonsEnable(xaENode.getDataValue());
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
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XKeyListener#keyPressed(com.sun.star.awt.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
/*		m_aLoggerDialog.entering("keyPressed on subclass"+arg0.KeyCode);*/
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XKeyListener#keyReleased(com.sun.star.awt.KeyEvent)
	 * 
	 */
	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
//		m_aLoggerDialog.entering("keyReleased, on subclass! "+arg0.KeyCode);
		
		//if arg0.KeyCode = 773 (key F6), set focus to certificate tree element
		if(arg0.KeyCode == com.sun.star.awt.Key.F6) {
			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
					m_xTreeControl );
			xTFWindow.setFocus();
		}
	}

	/**
	 * @param m_xDocumentStorage the m_xDocumentStorage to set
	 */
	public void setDocumentModel(XModel m_xDocumentModel) {
		this.m_xDocumentModel = m_xDocumentModel;
	}

	/**
	 * @return the m_xDocumentStorage
	 */
	public XModel getDocumentModel() {
		return m_xDocumentModel;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XFocusListener#focusGained(com.sun.star.awt.FocusEvent)
	 */
/*	@Override
	public void focusGained(FocusEvent arg0) {
//		m_aLoggerDialog.entering("focusGained, on subclass!");
	}*/

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XFocusListener#focusGained(com.sun.star.awt.FocusEvent)
	 */
/*	@Override
	public void focusLost(FocusEvent arg0) {
//		m_aLoggerDialog.entering("focusLost, on subclass!");
	}*/
}
