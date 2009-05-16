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

import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/** This class describes the node representing a certificate obtained from
 * an SSCD.
 * This element shows on the right side of the dialog a series of text lines, with the status of
 * the element represented.
 * The subclass should add the relevant behavior
 * 
 * @author beppec56
 *
 */
public class BaseGeneralNodeTreeElement extends TreeElement {

	// describes the field for certificate/signature status common to both

	/**
	 * the string list showed on the left, allocated, according to global constants.
	 */
	protected String[]	m_sStringList;
	/** the control to print
	 *  the above strings
	 */
	private Object[] m_xControlLines;

	/**
	 * 
	 */
	private XControl m_xBackgroundControl;


	protected MessageConfigurationAccess m_aRegAcc = null; 

	public BaseGeneralNodeTreeElement(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		setNodeType(TreeNodeType.SSCDEVICE);
		setLogger(new DynamicLogger(this,_xContext));
		setMultiComponentFactory(_xMCF);
		setComponentContext(_xContext);
/*		setCertificateState(TreeElement.m_nCERTIFICATE_STATE_NOT_VERIFIED);
		setCertificateStateConditions(TreeElement.m_nCERTIFICATE_STATE_CONDT_DISABLED);
		setIssuerState(TreeElement.m_nISSUER_STATE_NO_CTRL);*/
		setNodeGraphic(null);
		m_sStringList = new String[CertifTreeDlgDims.m_nMAXIMUM_FIELDS];
		m_xControlLines = new Object[CertifTreeDlgDims.m_nMAXIMUM_FIELDS];
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#initialize()
	 * 
	 * initializes the element common to both the signature and certificate
	 */
	@Override
	public void initialize() {
		//init string to empty state
		//clear all string, so far
		for(int i=0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS;i++)
			m_sStringList[i] ="r";		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#EnableDisplay(boolean)
	 * this methods simply enabled/disable the display of the controls
	 */
	@Override
	void EnableDisplay(boolean bWhat) {
//		getLogger().log("EnableDisplay "+bWhat);
		XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, getBackgroundControl() );
		if(xaWNode != null )					
			xaWNode.setVisible(bWhat);

		if(bWhat == false) {	
			for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
				XControl xTFControl = (XControl)getControlLine()[i];
				xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
				if(xaWNode != null )					
					xaWNode.setVisible(bWhat);
			}
		}
		else {
			/*ouput them
			 * for every string meant for the multi fixed text display
			 *  devise a method to change the character font strike
			 *  the first character in the string marks the stroke type
			 *  (see function: it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.EnableDisplay(boolean) for
			 *  for information on how they are interpreted):
			 *   b	the string will be in bold
			 *   B
			 *   i	the string will be output in italic single underlined 
			 *   s  the string will striken out regular
			 *   S  the string will striken out bold
			 *   w	text background will be in ControlDims.DLG_CERT_TREE_STATE_WARNING_COLOR
			 *   e  text background will be in ControlDims.DLG_CERT_TREE_STATE_ERROR_COLOR;
			 */
			for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
				XControl xTFControl = (XControl)getControlLine()[i];
				xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
				if(xaWNode != null ) {
					//check the interface, should be the one for fixed text
					XFixedText aFText =  (XFixedText)UnoRuntime.queryInterface( XFixedText.class, xTFControl );
					if(aFText != null) {
						String sStr = m_sStringList[i];
						if(sStr != null && sStr.length() > 0 ) {
							//grab the font width property
							//								Utilities.showInterfaces(xTFControl);
							XPropertySet xTFModelPSet = (XPropertySet) UnoRuntime.queryInterface(
									XPropertySet.class, xTFControl.getModel());
							//DEBUG: print properties:
							/*									Utilities.showProperties(this, xTFModelPSet);*/
							//detect the first char
							String sFirst = m_sStringList[i].substring(0,1);
							// set the width accordingly 
							try {
								// back is the back color of the control, not the font
								int nBackCol =  ControlDims.DLG_CERT_TREE_BACKG_COLOR;
								// get the font descriptor props
								FontDescriptor xf = (FontDescriptor)xTFModelPSet.getPropertyValue("FontDescriptor");
								xf.Slant  = com.sun.star.awt.FontSlant.NONE;
								xf.Underline = com.sun.star.awt.FontUnderline.NONE;
								xf.Weight = FontWeight.NORMAL;
								if(sFirst.equalsIgnoreCase("b")) {
									xf.Weight = FontWeight.BOLD;
								}
								else if(sFirst.equalsIgnoreCase("w")) {
									xf.Weight = FontWeight.BOLD;
									//set backgound color for 'e' errors, bold+
									nBackCol = ControlDims.DLG_CERT_TREE_STATE_WARNING_COLOR;
								}
								else if(sFirst.equalsIgnoreCase("e")) {
									xf.Weight = FontWeight.BOLD;
									//set backgound color for 'e' errors, bold+
									nBackCol = ControlDims.DLG_CERT_TREE_STATE_ERROR_COLOR;
								}
								else if(sFirst.equalsIgnoreCase("i")) {
									// italic, underline single
									xf.Slant  = com.sun.star.awt.FontSlant.ITALIC;
									xf.Underline = com.sun.star.awt.FontUnderline.SINGLE;
								}
								xTFModelPSet.setPropertyValue("BackgroundColor", new Integer( nBackCol ));
								xTFModelPSet.setPropertyValue("FontDescriptor", xf);
							} catch (UnknownPropertyException e) {
								getLogger().severe("EnableDisplay", e);
							} catch (PropertyVetoException e) {
								getLogger().severe("EnableDisplay", e);
							} catch (IllegalArgumentException e) {
								getLogger().severe("EnableDisplay", e);
							} catch (WrappedTargetException e) {
								getLogger().severe("EnableDisplay", e);
							}
							// set the string
							aFText.setText(sStr.substring(1));
						}
						else
							aFText.setText("<found null string> "+i);//debug, remove after test
						xaWNode.setVisible(bWhat);
					}
				}
				else {
					getLogger().log("missing XFixedText interface");
/*					XTextComponent aText =  (XTextComponent)UnoRuntime.queryInterface( XTextComponent.class, xTFControl );
					if( aText != null) // then it's the background  control in background color
						xaWNode.setVisible(true); //match if edit control
					else
						xaWNode.setVisible(false);*/
				}
			}
		}
	}

	/**
	 * @param m_xControlLines the m_xControlLines to set
	 */
	public void setAControlLine(Object _xControlLine, int _nIndex) {
		this.m_xControlLines[_nIndex] = _xControlLine;
	}

	/**
	 * @return the m_xControlLines
	 */
	public Object[] getControlLine() {
		return m_xControlLines;
	}

	/**
	 * @param m_xBackgroundControl the m_xBackgroundControl to set
	 */
	public void setBackgroundControl(XControl m_xBackgroundControl) {
		this.m_xBackgroundControl = m_xBackgroundControl;
	}

	/**
	 * @return the m_xBackgroundControl
	 */
	public XControl getBackgroundControl() {
		return m_xBackgroundControl;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
	}
}
