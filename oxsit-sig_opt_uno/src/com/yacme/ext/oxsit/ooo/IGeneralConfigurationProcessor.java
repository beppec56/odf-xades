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

package com.yacme.ext.oxsit.ooo;

import com.sun.star.uno.XInterface;

/**
 * @author beppec56
 *
 */
public interface IGeneralConfigurationProcessor {
	
	/** process an OOo configuration value item.<br>
	 * Used when recursing into the OOo registry structure
	 * 
	 * @param _sPath a string holding the path to examine
	 * @param _aValue the value to process
	 * @param _aObject a user object to process
	 */
	public abstract void processValueElement(String _sPath, Object _aValue, Object _aObject);
	/**
	 * 
	 * @param _sPath a string holding the path to examine
	 * @param _xElement the element to examine
	 */
// process a structural item
  	public abstract void processStructuralElement(String _sPath, XInterface _xElement);
}
