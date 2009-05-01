/**
 * 
 */
package it.plio.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import com.sun.star.awt.FocusEvent;
import com.sun.star.awt.KeyEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XKeyHandler;
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
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionChangeListener;

/**
 * @author beppe
 *
 */
public class DialogCertTreeBase extends BasicDialog implements
		XItemListener,
		XKeyListener,
		XTreeExpansionListener, 
		XSelectionChangeListener {

	protected String 				sAdd = "addcertb"; //this can be add certificate, or add signature
	protected String 				sTree = "certmodtree";

	private	Object 					m_oTreeDataModel;
	private XMutableTreeDataModel	m_xTreeDataModel;
	private XMutableTreeNode 		m_aTreeRootNode;
	private Object 					m_oTreeControlModel;	
	private XTreeControl 			m_xTreeControl = null;
	private XMutableTreeNode 		m_aTheOldNode = null;
	// the following two fields are needed to be able to change
	// the font at run-time
	private Object					m_xDisplElementModel;				// the service "com.sun.star.awt.UnoControlEditModel"

	private static final String 	m_sTextLinesBackground = "text_back";	//the control without text
	private static final String 	sEmptyTextLine = "text_L";		//the 1st line superimposed to the empty text control

	private static final String		m_sMultilineText	= "multi_l";  // the control general, with descriptive text in it
	
	private String				m_sBtnOKLabel = "id_ok";
	private String				m_sBtn_CancelLabel = "id_cancel";
	private String				m_sBtn_CreateReport = "id_pb_cert_report";

	protected String 			m_sFt_Hint_Doc = "id_title_mod_cert_treew";
	
	//Strings used for certificate elements
	private String			m_sLabelVersion = "id_cert_version";
	private String			m_sLabelSerialNumer = "id_cert_ser_numb";

	private String 				m_sLabelSubjectPublicKey = "id_cert_sbj_pub_key";

	/**
	 * @param frame
	 * @param context
	 * @param _xmcf
	 */
	public DialogCertTreeBase(XFrame frame, XComponentContext context,
			XMultiComponentFactory _xmcf) {
		super(frame, context, _xmcf);
		m_logger.enableLogging();
		m_logger.ctor();

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
//strings for certificate display
			m_sLabelVersion = m_aRegAcc.getStringFromRegistry( m_sLabelVersion );
			m_sLabelSerialNumer = m_aRegAcc.getStringFromRegistry( m_sLabelSerialNumer );

			m_sLabelSubjectPublicKey = m_aRegAcc.getStringFromRegistry( m_sLabelSubjectPublicKey );

		} catch (com.sun.star.uno.Exception e) {
			m_logger.severe("fillLocalizedString", e);
		}
		m_aRegAcc.dispose();	
	}

	public void initializeLocal(String _sName, String _sTitle, int posX, int posY) throws BasicErrorException {
		m_logger.entering("initialize");
		try {
			super.initialize(_sName, _sTitle, CertifTreeDlgDims.dsHeigh(), CertifTreeDlgDims.dsWidth(), posX, posY);
			//inserts the control elements needed to display properties
			//multiline text control used as a light yellow background
					//multiline text control for details
			Object oEdit = insertEditFieldModel(this, this,
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
			
			

					//insert the fixed text lines over the above mentioned element
			insertDisplayLinesOfText();

			//multiline text control for details of tree node element under selection
			m_xDisplElementModel = insertEditFieldModel(this, this,
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
							sTree,
							m_sFt_Hint_Doc, 10);
			insertButton(this,
							CertifTreeDlgDims.DS_COL_PB5(),
							CertifTreeDlgDims.DS_ROW_4(),
							CertifTreeDlgDims.dsBtnWidthCertTree(),
							"sprint",
							m_sBtn_CreateReport,
							(short) PushButtonType.STANDARD_value, 3);
					//cancel button
			insertButton(this,
							CertifTreeDlgDims.DLGS_BOTTOM_HELP_X(CertifTreeDlgDims.dsWidth()),
							CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
							ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
							"cancb",
							m_sBtn_CancelLabel,
							(short) PushButtonType.CANCEL_value, 1);
			// ok button
			insertButton(this,
							CertifTreeDlgDims.DLGS_BOTTOM_CANCEL_X(CertifTreeDlgDims.dsWidth()),
							CertifTreeDlgDims.DLGS_BOTTOM_BTN_Y(CertifTreeDlgDims.dsHeigh()),
							ControlDims.RSC_CD_PUSHBUTTON_WIDTH,
							"okb",
							m_sBtnOKLabel,
							(short) PushButtonType.OK_value, 2);

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

	protected XTreeControl insertTreeControl(XSelectionChangeListener _xActionListener,
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
			m_aTreeRootNode = m_xTreeDataModel.createNode(_sLabel, true);
			if(m_aTreeRootNode == null) {
				m_logger.severe("insertTreeControl", "the Node not available!");
				return null;
			}
			m_xTreeDataModel.setRoot(m_aTreeRootNode);

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
//					"RootDisplayed",
					"SelectionType",
//					"ShowsRootHandles",
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
	//				new Boolean( false ),
					new Integer( 0 ),
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
			m_logger.severe("insertTreeControl", ex);
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
	
	private XMutableTreeNode addOneCertificateHelper(CertificateTreeElementBase aCert) {		
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
			m_logger.severe("addOneCertificate", e);
		} catch (ExpandVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xaCNode;
	}

	/**
	 * FIXME: this method MUST be changed to the one needed to add a certificate to the tree
	 */
	public void addOneCertificate() {
//create a fake certificate description
		CertificateTreeElement aCert = new CertificateTreeElement(m_xContext, m_xMCF);
		aCert.initialize();
		XMutableTreeNode xNode = addOneCertificateHelper(aCert);
		
		addVariablePitchTreeElement(xNode, m_sLabelVersion,"V3");
		addFixedPitchTreeElement(xNode, m_sLabelSubjectPublicKey,"30 82 01 0A 02 82 01 01 00 C4 1C 77 1D AD 89 18\nB1 6E 88 20 49 61 E9 AD 1E 3F 7B 9B 2B 39 A3 D8\nBF F1 42 E0 81 F0 03 E8 16 26 33 1A B1 DC 99 97\n4C 5D E2 A6 9A B8 D4 9A 68 DF 87 79 0A 98 75 F8\n33 54 61 71 40 9E 49 00 00 06 51 42 13 33 5C 6C\n34 AA FD 6C FA C2 7C 93 43 DD 8D 6F 75 0D 51 99\n83 F2 8F 4E 86 3A 42 22 05 36 3F 3C B6 D5 4A 8E\nDE A5 DC 2E CA 7B 90 F0 2B E9 3B 1E 02 94 7C 00\n8C 11 A9 B6 92 21 99 B6 3A 0B E6 82 71 E1 7E C2\nF5 1C BD D9 06 65 0E 69 42 C5 00 5E 3F 34 3D 33\n2F 20 DD FF 3C 51 48 6B F6 74 F3 A5 62 48 C9 A8\nC9 73 1C 8D 40 85 D4 78 AF 5F 87 49 4B CD 42 08\n5B C7 A4 D1 80 03 83 01 A9 AD C2 E3 63 87 62 09\nFE 98 CC D9 82 1A CB AD 41 72 48 02 D5 8A 76 C0\nD5 59 A9 FF CA 3C B5 0C 1E 04 F9 16 DB AB DE 01\nF7 A0 BE CF 94 2A 53 A4 DD C8 67 0C A9 AF 60 5F\n53 3A E1 F0 71 7C D7 36 AB 02 03 01 00 01");
	}

	public void addOneSignature() {
		//create a fake certificate description
		SignatureTreeElement aCert = new SignatureTreeElement(m_xContext, m_xMCF);
		aCert.initialize();
		addOneCertificateHelper(aCert);
	}

	private XMutableTreeNode addMultilineTreeElementHelper(XMutableTreeNode _Node, MultilineTreeElementBase _aElm, String _sLabel) {
		//connect it to the right dialog pane
		//add it to the tree root node
		
		XMutableTreeNode xaCNode = m_xTreeDataModel.createNode(_sLabel, true);
		if(_aElm.getNodeGraphic() != null)
			xaCNode.setNodeGraphicURL(_aElm.getNodeGraphic());

		xaCNode.setDataValue(_aElm);
		try {
			_Node.appendChild(xaCNode);			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			m_logger.severe("addOneCertificate", e);
		}
		return xaCNode;		
	}

	private XMutableTreeNode addFixedPitchTreeElement(XMutableTreeNode _Node, String _sLabel, String _sContents) {
		FixedFontPitchTreeElement aElem = new FixedFontPitchTreeElement(m_xContext, m_xMCF, _sContents, m_xDlgContainer.getControl( m_sMultilineText ));
		return addMultilineTreeElementHelper(_Node, aElem, _sLabel);
	}
	
	private XMutableTreeNode addVariablePitchTreeElement(XMutableTreeNode _Node, String _sLabel, String _sContents) {
		MultilineTreeElementBase aElem = new MultilineTreeElementBase(m_xContext, m_xMCF, _sContents, m_xDlgContainer.getControl( m_sMultilineText ));
		return addMultilineTreeElementHelper(_Node, aElem, _sLabel);
	}

	////////////////////////////////////
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
//		m_logger.entering("selectionChanged");
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
//		m_logger.entering("enableSingleButton");
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
	 * @see com.sun.star.awt.XKeyListener#keyPressed(com.sun.star.awt.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
/*		m_logger.entering("keyPressed on subclass"+arg0.KeyCode);*/
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XKeyListener#keyReleased(com.sun.star.awt.KeyEvent)
	 * 
	 */
	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
//		m_logger.entering("keyReleased, on subclass! "+arg0.KeyCode);
		
		//if arg0.KeyCode = 773 (key F6), set focus to certificate tree element
		if(arg0.KeyCode == com.sun.star.awt.Key.F6) {
			XWindow xTFWindow = (XWindow) UnoRuntime.queryInterface( XWindow.class,
					m_xTreeControl );
			xTFWindow.setFocus();
		}

	}
	/* (non-Javadoc)
	 * @see com.sun.star.awt.XFocusListener#focusGained(com.sun.star.awt.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent arg0) {
//		m_logger.entering("focusGained, on subclass!");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.awt.XFocusListener#focusGained(com.sun.star.awt.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent arg0) {
//		m_logger.entering("focusLost, on subclass!");
	}

}
