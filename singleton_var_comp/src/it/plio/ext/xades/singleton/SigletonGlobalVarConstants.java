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

package it.plio.ext.xades.singleton;

/**
 * this class contains the global variables needed by the singleton implementation
 * 
 * @author beppe
 * 
 */
public class SigletonGlobalVarConstants {
	
	// the property name operation to carry out
	public static final String m_sPROPERTY_OPERATION = "Operation";
	// constants correponding to the operation
	
	// if the following operation is requested, then the
	// other subproperties are ignored.
	public static final	int	m_nREMOVE_PROPERTY = 1;
	
	// the parent property should be added
	public static final	int	m_nADD_PROPERTY = 2;
	// the parent property should be set to the new value
	// only URL
	public static final	int	m_nSET_PROPERTY = 3;
	
	// the following will need only the m_sCHANGE_LISTER
	// subproperty
	public static final	int	m_nADD_CHANGES_LISTENER = 4;
	public static final	int	m_nREMOVE_CHANGES_LISTENER = 5;

	public static final String m_sURL_VALUE = "URL";
	public static final String m_sXADES_SIGNATURE_STATE = "XAdESSignatureState";
	public static final String m_sCHANGE_LISTER = "ChangesListener";
	
}
