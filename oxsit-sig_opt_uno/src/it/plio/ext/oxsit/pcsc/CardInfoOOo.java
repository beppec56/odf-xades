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

package it.plio.ext.oxsit.pcsc;

/** A helper class to store the information about a SSCD retrieved
 * from OOo configuration
 * @author beppec56
 *
 */
public class CardInfoOOo {

	public static final int	m_sOS_LINUX = 0;
	public static final int	m_sOS_WINDOWS = 1;
	public static final int	m_sOS_MAC = 2;
	public static final int	m_sMAX_Os = 3;
	
	public String	m_sATRCode;
	public String	m_sDescription;
	public String	m_sManufacturer;
	public String	m_sCardType;
	
	public String	m_sOsLib;
	public String	m_sOsRes1;
	public String	m_sOsRes2;

	public CardInfoOOo() {
		m_sATRCode =
		m_sDescription =
		m_sManufacturer =
		m_sCardType = "";

			m_sOsLib = 
			m_sOsRes1 = 
			m_sOsRes2 = ""; 
	}
	public String toString() {
		String ret ="";
		String term = System.getProperty("line.separator");
		ret = m_sATRCode+term;
		ret = ret +"Description: "+m_sDescription+term;
		ret = ret +"Manufacturer: "+m_sManufacturer+term;
		ret = ret +"CardType: "+m_sCardType+term;
		ret = ret +"Library data: "+term;
		ret = ret +m_sOsLib+term;
		ret = ret +m_sOsRes1+term;
		ret = ret +m_sOsRes2+term;

		return ret;
	}

}
