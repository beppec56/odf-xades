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


import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XTextComponent;
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
import com.yacme.ext.oxsit.logging.DynamicLogger;

/**
 * @author beppe
 *
 */
public class MultilineTreeElementBase extends TreeElement {

	protected String	m_sTheText;
	protected XControl	m_xTheTextControl;

	protected boolean	m_bSetFixedPitch;

	public MultilineTreeElementBase(XComponentContext _xContext,
			XMultiComponentFactory _xMCF,
			String _sContents,
			XControl _xTheTextControl) {
		setNodeType(TreeNodeType.VERSION); //default, must be changed by subclass
		setLogger(new DynamicLogger(this,_xContext));
		getLogger().enableLogging();
		setMultiComponentFactory(_xMCF);
		setComponentContext(_xContext);
		setNodeGraphic(null);
		m_sTheText = _sContents;		
		m_bSetFixedPitch = false;//will be set to true by the subclass
		m_xTheTextControl = _xTheTextControl;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.TreeElement#EnableDisplay(boolean)
	 */
	@Override
	void EnableDisplay(boolean bWhat) {
		if(bWhat == false) {	
			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, m_xTheTextControl );
			if(xaWNode != null )					
				xaWNode.setVisible(bWhat);
		}
		else {
			//prints only a single line in the multiline window, so we expect only one element
			XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, m_xTheTextControl );
			if(xaWNode != null ) {
				// first set the font style
				//grab the model
				XControl aControl =  (XControl)UnoRuntime.queryInterface( XControl.class, m_xTheTextControl );
				XControlModel aControlModel = aControl.getModel();
//				Utilities.showInterfaces(xTFControl);
//				Utilities.showServiceProperties(aControlModel);
//				com.sun.star.awt.FontDescriptor font;
				try {
					XPropertySet xTFModelPSet = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, aControlModel);
					
/*						font = (com.sun.star.awt.FontDescriptor) xTFModelPSet
							.getPropertyValue("FontDescriptor");
*/						// strangely enough, it seems that FontPitch alone doesn't work
					if (m_bSetFixedPitch){
//						font.Name = new String("Courier");
//						xTFModelPSet.setPropertyValue("FontHeight", new Float(12.0));
						xTFModelPSet.setPropertyValue("FontName", "Courier");
//						font.Pitch = FontPitch.FIXED; //doesn't work
					}
					else {
//						xTFModelPSet.setPropertyValue("FontHeight", new Float(12.0));
//						xTFModelPSet.setPropertyValue("FontWidth", new Short((short)FontWidth.EXPANDED));
						xTFModelPSet.setPropertyValue("FontName", "Verdana");
//						xTFModelPSet.setPropertyValue("FontName", "Helvetica");
//						xTFModelPSet.setPropertyValue("FontName", "Times");
					}
						
//						font.Name = new String("Helvetica");
//						font.Pitch = FontPitch.VARIABLE; //doesn't work
					
//					font.Family = FontFamily.SYSTEM;

//					font.Weight = (float)120.0;//100.0 = standard, 200.0 = double weight
//					font.Slant = FontSlant.ITALIC;//works
//					System.out.println("font: " + font.Pitch);

//					xTFModelPSet.setPropertyValue("FontDescriptor", font);
				} catch (UnknownPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (WrappedTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//check the interface, should be the one for fixed text
				XTextComponent aText =  (XTextComponent)UnoRuntime.queryInterface( XTextComponent.class, m_xTheTextControl );
				if( aText != null ) {
					 //match if edit control
					aText.setText("  ");												
					xaWNode.setVisible(true);
					aText.setText(m_sTheText);
				}
				else {
					xaWNode.setVisible(false);
				}
			}
			else
				getLogger().log("no window");
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.TreeElement#initialize()
	 */
	@Override
	void initialize() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	public void disposeUNOComponents() {
		//FIXME: clean up UNO components used here, if any
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.ooo.ui.TreeElement#updateForDisplay()
	 */
	@Override
	void updateForDisplay() {
		// TODO Auto-generated method stub
		
	}
}
