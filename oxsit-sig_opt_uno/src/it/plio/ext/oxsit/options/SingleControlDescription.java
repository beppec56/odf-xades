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

package it.plio.ext.oxsit.options;

import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XItemListener;

/**
 * describes a single control on an option page
 * @author beppe
 *
 */
public class SingleControlDescription {
	public enum ControlTypeCode {
		EDIT_TEXT, //edit text box, configuration text
		EDIT_TEXT_INT,  //edit text box, configuration integer
		CHECK_BOX, // check box, configuration a boolean
		RADIO_BUTTON, //check box, configuration a number
		PUSH_BUTTON // a push button
	}
	
	public XControl			m_xTheControl;		//the control interface of this control
	public XActionListener m_xAnActionListener; // this is a general action listener
												// that cound be used to implement actions on controls, when needed
	public XItemListener m_xAnItemListener;
	
	public String m_sControlName; 	// the name of the control, as set by basic editor
									// during editing
	public ControlTypeCode m_eTheCode; // what type is this control?

	public int	m_nTheRadioButtonPosition; // the number corresponding to this radio button 

	public String m_sPropertyName; //corresponding property in configuration structure
	
	public int	m_nMaxValue;

	public int	m_nMinValue;
	
	public boolean	m_bEnableSave;
	

	/**
	 * 
	 * @param sName Name of the control
	 * @param eType type of the control, in form of enum SingleControlDescription.ControlTypeCode.
	 * @param nRadioPos radio control button value, other elements get -1, it's the positional
	 * code of the Radio button in the group of radio buttons (only if eType is ControlTypeCode.RADIO_BUTTON)
	 * @param sProperty name of the property on configuration structure (AddonConfiguration.xcs.xml)
	 * @param _nMax TODO
	 * @param _nMin TODO
	 * @param _bEnableSave TODO
	 */
	public SingleControlDescription(String sName, ControlTypeCode eType, int nRadioPos, String sProperty, int _nMax, int _nMin, boolean _bEnableSave) {
		m_sControlName = sName;
		m_eTheCode = eType;
		m_nTheRadioButtonPosition = nRadioPos; 
		m_sPropertyName = sProperty;
		m_xAnActionListener = null;
		m_nMaxValue = _nMax;
		m_nMinValue = _nMin;
		m_bEnableSave = _bEnableSave;
		
	}	
}
