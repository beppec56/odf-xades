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

package com.yacme.ext.oxsit.pcsc;

import java.io.IOException;
import iaik.pkcs.pkcs11.wrapper.PKCS11Connector;

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
	
	private String	m_sATRCode;
	public String	m_sDescription;
	public String	m_sManufacturer;
	public String	m_sCardType;
	
	private String  m_sDefaultLib;
	private String	m_sOsLib;
	private String	m_sOsLibAlt1;
	private String	m_sOsLibAlt2;
	private String	m_sOsLibAlt3;

	public CardInfoOOo() {
		m_sDescription =
		m_sManufacturer =
		m_sCardType = 
		m_sDefaultLib =
		m_sOsLib =
		m_sOsLibAlt1 =
		m_sOsLibAlt2 =
		m_sOsLibAlt3 = "";
	}

	public String toString() {
		String ret ="";
		String term = System.getProperty("line.separator");
		ret = getATRCode()+term;
		ret = ret +"Description: "+m_sDescription+term;
		ret = ret +"Manufacturer: "+m_sManufacturer+term;
		ret = ret +"CardType: "+m_sCardType+term;
		ret = ret +"Library data: "+term;
		ret = ret +getDefaultLib()+term;
		ret = ret +getOsLib()+term;
		ret = ret +getOsLibAlt1()+term;
		ret = ret +getOsLibAlt2()+term;
		ret = ret +getOsLibAlt3()+term;

		return ret;
	}

	//choose a library
	public String detectDefaultLib(String pkcs11WrapLib) {
		//return one of the library available on system, e.g. linked
		//try in sequence.
		//The one returned
		//is the one available on system, using the library load of the wrapper
			String[] sLibs = new String[5];
			String sLibOk = "";
			sLibs[0] = getDefaultLib();
			sLibs[1] = getOsLib();
			sLibs[2] = getOsLibAlt1();
			sLibs[3] = getOsLibAlt2();
			sLibs[4] = getOsLibAlt3();
			for(int i =0; i<sLibs.length;i++) {
				try {
					if(sLibs[i].length() > 0) {
						if(pkcs11WrapLib.length() > 0)
							PKCS11Connector.connectToPKCS11Module(sLibs[i],pkcs11WrapLib);
						else
							PKCS11Connector.connectToPKCS11Module(sLibs[i]);							
						sLibOk = sLibs[i];
						break;
					}
				} catch (IOException e) {
//					e.printStackTrace();
				} catch (SecurityException e) {
	//				e.printStackTrace();
				} catch (NoSuchMethodError e) {
					//thrown when the native wrapper library
					//is in the wrong place
//					throw (new NoSuchMethodError("Wrapper library "+pkcs11WrapLib+" not found! ") );
				} catch (UnsatisfiedLinkError e) {
		//			e.printStackTrace();
				} catch (NullPointerException e) {
			//		e.printStackTrace();
				}
			}
			if(sLibOk.length() >0)
				m_sDefaultLib = sLibOk;

			return m_sDefaultLib;
	}

	public String getLib() {
		return getOsLib();
	}

	/**
	 * @param _sOsLib the m_sOsLib to set
	 */
	public void setOsLib(String _sOsLib) {
		m_sDefaultLib = _sOsLib;
		this.m_sOsLib = _sOsLib;
	}

	/**
	 * @return the m_sOsLib
	 */
	public String getOsLib() {
		return this.m_sOsLib;
	}

	/**
	 * @param m_sOsLibAlt1 the m_sOsLibAlt1 to set
	 */
	public String setOsLibAlt1(String m_sOsLibAlt1) {
		this.m_sOsLibAlt1 = m_sOsLibAlt1;
		return m_sOsLibAlt1;
	}

	/**
	 * @return the m_sOsLibAlt1
	 */
	public String getOsLibAlt1() {
		return m_sOsLibAlt1;
	}

	/**
	 * @param m_sOsLibAlt2 the m_sOsLibAlt2 to set
	 */
	public String setOsLibAlt2(String m_sOsLibAlt2) {
		this.m_sOsLibAlt2 = m_sOsLibAlt2;
		return m_sOsLibAlt2;
	}

	/**
	 * @return the m_sOsLibAlt2
	 */
	public String getOsLibAlt2() {
		return m_sOsLibAlt2;
	}

	/**
	 * @param m_sOsLibAlt3 the m_sOsLibAlt3 to set
	 */
	public String setOsLibAlt3(String m_sOsLibAlt3) {
		this.m_sOsLibAlt3 = m_sOsLibAlt3;
		return m_sOsLibAlt3;
	}

	/**
	 * @return the m_sOsLibAlt3
	 */
	public String getOsLibAlt3() {
		return m_sOsLibAlt3;
	}

	/**
	 * @param m_sATRCode the m_sATRCode to set
	 */
	public void setATRCode(String m_sATRCode) {
		this.m_sATRCode = m_sATRCode;
	}

	/**
	 * @return the m_sATRCode
	 */
	public String getATRCode() {
		return m_sATRCode;
	}

	/**
	 * @param m_sDefaultLib the m_sDefaultLib to set
	 */
	public void setAvailableLib(String m_sAvailableLib) {
		this.m_sDefaultLib = m_sAvailableLib;
	}

	/**
	 * @return the m_sDefaultLib
	 */
	public String getDefaultLib() {
		return m_sDefaultLib;
	}
}
