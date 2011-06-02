/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security;

import com.sun.star.io.XInputStream;

/** helper class. Defines a single element in an ODF package.
 * @author beppe
 *
 */
public class ODFPackageItem {
	public String m_stheName;
	public String m_sMediaType;
	public int m_nSize;
	public XInputStream m_xInputStream;

	public ODFPackageItem(String s, String mt, XInputStream _xInputStream, int sz) {
		m_stheName = s;
		m_sMediaType = mt;
		m_xInputStream = _xInputStream;
		m_nSize = sz;
	}

	public String toString() {
		String ret = "media type: '" + m_sMediaType + "' size: " + m_nSize + " bytes, position: '" + m_stheName + "'";
		return ret;
	}

	
}
