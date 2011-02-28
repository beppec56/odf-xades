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

package com.yacme.ext.oxsit.security;

/**
 * Describes in details the attributes of the token (e.g. the logical SSCD)
 * 
 * @author beppec56
 *
 */
public class PKCS11TokenAttributes {

	private long m_lTokenHandle;

	private String m_sLabel;

	private String m_sManufacturerID;

	private String m_sModel;

	private String m_sSerialNumber;

	private long m_ulMaxPinLen; /* in bytes */

	private long m_ulMinPinLen; /* in bytes */

	public PKCS11TokenAttributes() {
		setLabel(setManufacturerID(setModel(setSerialNumber(""))));
		setMaxPinLen(setMinPinLen(0));
		m_lTokenHandle = -1L;
	}

	public PKCS11TokenAttributes(String _sLabel, String _sModel, String _sSerialNumber, long _nlMaxPin) {
		super();
		setLabel(_sLabel);
		setModel(_sModel);
		setSerialNumber(_sSerialNumber);
		setMaxPinLen(_nlMaxPin);
	}
	/**
	 * @param _sLabel the m_sLabel to set
	 */
	public String setLabel(String _sLabel) {
		this.m_sLabel = _sLabel;
		return _sLabel;
	}

	/**
	 * @return the m_sLabel
	 */
	public String getLabel() {
		return m_sLabel;
	}

	/**
	 * @param _sManufacturerID the m_sManufacturerID to set
	 */
	public String setManufacturerID(String _sManufacturerID) {
		this.m_sManufacturerID = _sManufacturerID;
		return _sManufacturerID;
	}

	/**
	 * @return the m_sManufacturerID
	 */
	public String getManufacturerID() {
		return m_sManufacturerID;
	}

	/**
	 * @param _sModel the m_sModel to set
	 */
	public String setModel(String _sModel) {
		this.m_sModel = _sModel;
		return _sModel;
	}

	/**
	 * @return the m_sModel
	 */
	public String getModel() {
		return m_sModel;
	}

	/**
	 * @param _sSerialNumber the m_sSerialNumber to set
	 */
	public String setSerialNumber(String _sSerialNumber) {
		this.m_sSerialNumber = _sSerialNumber;
		return _sSerialNumber;
	}

	/**
	 * @return the m_sSerialNumber
	 */
	public String getSerialNumber() {
		return m_sSerialNumber;
	}

	/**
	 * @param _ulMaxPinLen the m_ulMaxPinLen to set
	 */
	public long setMaxPinLen(long _ulMaxPinLen) {
		this.m_ulMaxPinLen = _ulMaxPinLen;
		return _ulMaxPinLen;
	}

	/**
	 * @return the m_ulMaxPinLen
	 */
	public long getMaxPinLen() {
		return m_ulMaxPinLen;
	}

	/**
	 * @param _ulMinPinLen the m_ulMinPinLen to set
	 */
	public long setMinPinLen(long _ulMinPinLen) {
		this.m_ulMinPinLen = _ulMinPinLen;
		return _ulMinPinLen;
	}

	/**
	 * @return the m_ulMinPinLen
	 */
	public long getMinPinLen() {
		return m_ulMinPinLen;
	}

	/**
	 * @param _lTokenHandle the m_lTokenHandle to set
	 */
	public long setTokenHandle(long _lTokenHandle) {
		this.m_lTokenHandle = _lTokenHandle;
		return _lTokenHandle;
	}

	/**
	 * @return the m_lTokenHandle
	 */
	public long getTokenHandle() {
		return m_lTokenHandle;
	}
	
	public String toString() {
		String term = System.getProperty("line.separator");
		
		String ret = new String("Token handle: "+m_lTokenHandle+term+
								"Label: "+m_sLabel+term+
								"Manufacturer ID: "+m_sManufacturerID+term+
								"Model: "+m_sModel+term+
								"Serial number: "+m_sSerialNumber+term+
								"Max PIN length: "+m_ulMaxPinLen+term+
								"Min PIN length: "+m_ulMinPinLen);
		return ret;
	}
}
