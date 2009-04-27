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

package it.plio.ext.oxsit;

import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.XOX_DocumentSignatures;

import com.sun.star.beans.*;
import com.sun.star.container.*;
import com.sun.star.frame.XModel;
import com.sun.star.lang.*;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.*;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import com.sun.star.util.XChangesListener;
import com.sun.star.beans.PropertyValue;
import java.util.*;

/** Helper class composed of static methods.
 *  
 * @author beppe
 */
public class Helpers {
	protected Helpers() {
	}
	
	/** Return the hex hash code of an object.
	 * 
	 * @param _oObj the object to be examined
	 * @return the returned hash code, in hex
	 */
	public static String getHashHex(Object _oObj) {
		String ret;
		try {
//grab from the Object the has code and returns it
			if(_oObj == null)
				ret ="NULL";
			else
				ret = String.format( "%8H", _oObj );
		} catch (java.lang.Exception e) {
			e.printStackTrace(System.out);
			ret = "caught exception!";
		}
		return ret;
	}

	/**
	 * returns the global data object interface
	 * 
	 * @param xContext
	 * @return
	 * @throws ClassCastException internal Java error
	 * @throws ServiceNotFoundException if the global singleton data service is not available
	 * @throws NoSuchMethodException if the XOX_SingletonDataAccess interface is not available.
	 */
	public static XOX_SingletonDataAccess getSingletonDataAccess(XComponentContext xContext) 
		throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			Object							m_SingletonDataObject = null;
			XOX_SingletonDataAccess			m_xSingletonDataAccess = null;
			m_SingletonDataObject = xContext.getValueByName(GlobConstant.m_sSINGLETON_SERVICE_INSTANCE);
			if(m_SingletonDataObject != null) {
				m_xSingletonDataAccess = (XOX_SingletonDataAccess)UnoRuntime.queryInterface(XOX_SingletonDataAccess.class, m_SingletonDataObject);
				if(m_xSingletonDataAccess == null)
					throw (new NoSuchMethodException("XOX_SingletonDataAccess missing") ); 					
			}
			else
				throw (new ServiceNotFoundException("service "+GlobConstant.m_sSINGLETON_SERVICE+" not found") ); 

			return m_xSingletonDataAccess;
		}
	}

	public static XOX_DocumentSignatures initDocumentSignaturesData(XComponentContext xContext, XModel _xModel, XChangesListener _xChg) 
	throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			XOX_SingletonDataAccess		m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
			XOX_DocumentSignatures		m_xDocumentSignatures =
								m_xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(_xModel), _xChg);
			if(m_xDocumentSignatures == null)
				throw (new NoSuchMethodException("XOX_DocumentSignatures missing") ); 									

			return m_xDocumentSignatures;
		}
	}

	public static XOX_DocumentSignatures getDocumentSignatures(XComponentContext xContext, XModel _xModel) 
	throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			XOX_SingletonDataAccess		m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
			XOX_DocumentSignatures		m_xDocumentSignatures =
								m_xSingletonDataAccess.getDocumentSignatures(Helpers.getHashHex(_xModel));
			if(m_xDocumentSignatures == null)
				throw (new NoSuchMethodException("XOX_DocumentSignatures missing") ); 									

			return m_xDocumentSignatures;
		}
	}
	
	/**
	 * converts a list of Integer values included in an Integer vector to a list
	 * of int values
	 * 
	 * 
	 * @param _aIntegerVector
	 * @return
	 */
	/*
	 * public static int[] IntegerTointList(Vector _aIntegerVector){ try {
	 * Integer[] nIntegerValues = new Integer[_aIntegerVector.size()]; int[]
	 * nintValues = new int[_aIntegerVector.size()];
	 * _aIntegerVector.toArray(nIntegerValues); for (int i = 0; i <
	 * nIntegerValues.length; i++) nintValues[i] = nIntegerValues[i].intValue();
	 * return nintValues; } catch (RuntimeException e) {
	 * e.printStackTrace(System.out); return null; } }
	 */

	/**
	 * converts a list of Boolean values included in a Boolean vector to a list
	 * of boolean values
	 * 
	 * 
	 * @param _aBooleanVector
	 * @return
	 */
	/*
	 * public static boolean[] BooleanTobooleanList(Vector _aBooleanVector){ try {
	 * Boolean[] bBooleanValues = new Boolean[_aBooleanVector.size()]; boolean[]
	 * bbooleanValues = new boolean[_aBooleanVector.size()];
	 * _aBooleanVector.toArray(bBooleanValues); for (int i = 0; i <
	 * bBooleanValues.length; i++) bbooleanValues[i] =
	 * bBooleanValues[i].booleanValue(); return bbooleanValues; } catch
	 * (RuntimeException e) { e.printStackTrace(System.out); return null; }}
	 */

	public static String[] multiDimListToArray(String[][] multidimlist) {
		String[] retlist = new String[] {};
		retlist = new String[multidimlist.length];
		for (int i = 0; i < multidimlist.length; i++) {
			retlist[i] = multidimlist[i][0];
		}
		return retlist;
	}

	public static String getlongestArrayItem(String[] StringArray) {
		String sLongestItem = "";
		int FieldCount = StringArray.length;
		int iOldLength = 0;
		int iCurLength = 0;
		for (int i = 0; i < FieldCount; i++) {
			iCurLength = StringArray[i].length();
			if (iCurLength > iOldLength) {
				iOldLength = iCurLength;
				sLongestItem = StringArray[i];
			}
		}
		return sLongestItem;
	}

	public static String ArraytoString(String[] LocArray) {
		String ResultString = "";
		int iLen = LocArray.length;
		for (int i = 0; i < iLen; i++) {
			ResultString += LocArray[i];
			if (i < iLen - 1)
				ResultString += ";";
		}
		return ResultString;
	}

	/**
	 * @author bc93774
	 * @param SearchList
	 * @param SearchString
	 * @return the index of the field that contains the string 'SearchString' or
	 *         '-1' if not it is not contained within the array
	 */
	public static int FieldInList(String[] SearchList, String SearchString) {
		int FieldLen = SearchList.length;
		int retvalue = -1;
		for (int i = 0; i < FieldLen; i++) {
			if (SearchList[i].compareTo(SearchString) == 0) {
				retvalue = i;
				break;
			}
		}
		return retvalue;
	}

	public static int FieldInList(String[] SearchList, String SearchString, int StartIndex) {
		int FieldLen = SearchList.length;
		int retvalue = -1;
		if (StartIndex < FieldLen) {
			for (int i = StartIndex; i < FieldLen; i++) {
				if (SearchList[i].compareTo(SearchString) == 0) {
					retvalue = i;
					break;
				}
			}
		}
		return retvalue;
	}

	public static int FieldInTable(String[][] SearchList, String SearchString) {
		int retvalue;
		if (SearchList.length > 0) {
			int FieldLen = SearchList.length;
			retvalue = -1;
			for (int i = 0; i < FieldLen; i++) {
				if (SearchList[i][0] != null) {
					if (SearchList[i][0].compareTo(SearchString) == 0) {
						retvalue = i;
						break;
					}
				}
			}
		} else
			retvalue = -1;
		return retvalue;
	}

	public static int FieldInIntTable(int[][] SearchList, int SearchValue) {
		int retvalue = -1;
		for (int i = 0; i < SearchList.length; i++) {
			if (SearchList[i][0] == SearchValue) {
				retvalue = i;
				break;
			}
		}
		return retvalue;
	}

	public static int FieldInIntTable(int[] SearchList, int SearchValue, int _startindex) {
		int retvalue = -1;
		for (int i = _startindex; i < SearchList.length; i++) {
			if (SearchList[i] == SearchValue) {
				retvalue = i;
				break;
			}
		}
		return retvalue;
	}

	public static int FieldInIntTable(int[] SearchList, int SearchValue) {
		return FieldInIntTable(SearchList, SearchValue, 0);
	}

	public static int getArraylength(Object[] MyArray) {
		int FieldCount = 0;
		if (MyArray != null)
			FieldCount = MyArray.length;
		return FieldCount;
	}

	/**
	 * @author bc93774 This function bubble sorts an array of with 2 dimensions.
	 *         The default sorting order is the first dimension Only if
	 *         sort2ndValue is True the second dimension is the relevant for the
	 *         sorting order
	 */
	public static String[][] bubblesortList(String[][] SortList) {
		String DisplayDummy;
		int SortCount = SortList[0].length;
		int DimCount = SortList.length;
		for (int s = 0; s < SortCount; s++) {
			for (int t = 0; t < SortCount - s - 1; t++) {
				if (SortList[0][t].compareTo(SortList[0][t + 1]) > 0) {
					for (int k = 0; k < DimCount; k++) {
						DisplayDummy = SortList[k][t];
						SortList[k][t] = SortList[k][t + 1];
						SortList[k][t + 1] = DisplayDummy;
					}
				}
			}
		}
		return SortList;
	}

	/**
	 * @param MainString
	 * @param Token
	 * @return
	 */
	/*
	 * public static String[] ArrayoutofString(String MainString, String Token) {
	 * String[] StringArray; if (MainString.equals("") == false) { Vector
	 * StringVector = new Vector(); String LocString = null; int iIndex; do {
	 * iIndex = MainString.indexOf(Token); if (iIndex < 0)
	 * StringVector.addElement(MainString); else {
	 * StringVector.addElement(MainString.substring(0, iIndex)); MainString =
	 * MainString.substring(iIndex + 1, MainString.length()); } } while (iIndex >=
	 * 0); int FieldCount = StringVector.size(); StringArray = new
	 * String[FieldCount]; StringVector.copyInto(StringArray); } else
	 * StringArray = new String[0]; return StringArray; }
	 */
	public static String replaceSubString(String MainString, String NewSubString,
			String OldSubString) {
		try {
			int NewIndex = 0;
			int OldIndex = 0;
			int NewSubLen = NewSubString.length();
			int OldSubLen = OldSubString.length();
			while (NewIndex != -1) {
				NewIndex = MainString.indexOf(OldSubString, OldIndex);
				if (NewIndex != -1) {
					MainString = MainString.substring(0, NewIndex) + NewSubString
							+ MainString.substring(NewIndex + OldSubLen);
					OldIndex = NewIndex + NewSubLen;
				}
			}
			return MainString;
		} catch (java.lang.Exception exception) {
			exception.printStackTrace(System.out);
			return null;
		}
	}

	/*
	 * public static String getFilenameOutOfPath(String sPath){ String[]
	 * Hierarchy = ArrayoutofString(sPath, "/"); return
	 * Hierarchy[Hierarchy.length - 1]; }
	 * 
	 * 
	 * public static String getFileDescription(String sPath){ String sFilename =
	 * getFilenameOutOfPath(sPath); String[] FilenameList =
	 * ArrayoutofString(sFilename, "."); String FileDescription = ""; for (int i =
	 * 0; i < FilenameList.length - 1; i++) { FileDescription +=
	 * FilenameList[i]; } return FileDescription; }
	 */

	public static long getTimeInMillis(Calendar _calendar) {
		java.util.Date dDate = _calendar.getTime();
		return dDate.getTime();
	}

	public static void setTimeInMillis(Calendar _calendar, long _timemillis) {
		java.util.Date dDate = new java.util.Date();
		dDate.setTime(_timemillis);
		_calendar.setTime(dDate);
	}

	public static long getMillis(DateTime time) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.set(time.Year, time.Month, time.Day, time.Hours, time.Minutes, time.Seconds);
		return getTimeInMillis(cal);
	}

	/**
	 * searches a multidimensional array for duplicate fields. According to the
	 * following example SlaveFieldName1 ;SlaveFieldName2; SlaveFieldName3
	 * MasterFieldName1;MasterFieldName2;MasterFieldName3 The entries
	 * SlaveFieldNameX and MasterFieldNameX are grouped together and then the
	 * created groups are compared If a group is duplicate the entry of the
	 * second group is returned.
	 * 
	 * @param _scomplist
	 * @return
	 */
	public static int getDuplicateFieldIndex(String[][] _scomplist) {
		int retvalue = -1;
		if (_scomplist.length > 0) {
			int fieldcount = _scomplist[0].length;
			String[] sDescList = new String[fieldcount];
			for (int m = 0; m < fieldcount; m++) {
				for (int n = 0; n < _scomplist.length; n++) {
					if (n == 0)
						sDescList[m] = new String();
					sDescList[m] += _scomplist[n][m];
				}
			}
			return getDuplicateFieldIndex(sDescList);
		}
		return retvalue;
	}

	/**
	 * not tested!!!!!
	 * 
	 * @param scomplist
	 * @return
	 */
	public static int getDuplicateFieldIndex(String[] scomplist) {
		for (int n = 0; n < scomplist.length; n++) {
			String scurvalue = scomplist[n];
			for (int m = n; m < scomplist.length; m++) {
				if (m != n) {
					if (scurvalue.equals(scomplist[m]))
						return m;
				}
			}
		}
		return -1;
	}

	public static int getDuplicateFieldIndex(String[] _scomplist, String _fieldname) {
		int iduplicate = 0;
		for (int n = 0; n < _scomplist.length; n++) {
			if (_scomplist[n].equals(_fieldname)) {
				iduplicate++;
				if (iduplicate == 2) {
					return n;
				}
			}
		}
		return -1;
	}

	public static boolean isEqual(PropertyValue firstPropValue, PropertyValue secPropValue) {
		if (!firstPropValue.Name.equals(secPropValue.Name))
			return false;
		// TODO replace 'equals' with
		// AnyConverter.getType(firstpropValue).equals(secPropValue) to check
		// content and Type

		if (!firstPropValue.Value.equals(secPropValue.Value))
			return false;
		return (firstPropValue.Handle == secPropValue.Handle);
	}

	public static int[] getDuplicateFieldIndex(PropertyValue[][] ocomplist) {
		for (int n = 0; n < ocomplist.length; n++) {
			PropertyValue[] ocurValue = ocomplist[n];
			for (int m = n; m < ocurValue.length; m++) {
				PropertyValue odetValue = ocurValue[m];
				for (int s = 0; s < ocurValue.length; s++) {
					if (s != m) {
						if (isEqual(odetValue, ocurValue[s]))
							return new int[] { n, s };
					}
				}
			}
		}
		return new int[] { -1, -1 };
	}

	public static String getSuffixNumber(String _sbasestring) {
		int suffixcharcount = 0;
		for (int i = _sbasestring.length() - 1; i >= 0; i--) {
			char b = _sbasestring.charAt(i);
			if ((b >= '0') && (b <= '9'))
				suffixcharcount++;
			else
				break;
		}
		int istart = _sbasestring.length() - suffixcharcount;
		return _sbasestring.substring(istart, _sbasestring.length());
	}

	/**
	 * compares two strings. If one of them is empty and the other one is null
	 * it also returns true
	 * 
	 * @param sFirstString
	 * @param sSecondString
	 * @return
	 */
	public static boolean isSame(String sFirstString, String sSecondString) {
		boolean bissame = false;
		if (sFirstString == null) {
			if (sSecondString != null)
				bissame = sSecondString.equals("");
			else
				bissame = (sSecondString == null);
		} else {
			if (sFirstString.equals(""))
				bissame = (sSecondString == null);
			else if (sSecondString != null)
				bissame = sFirstString.equals(sSecondString);
		}
		return bissame;
	}
	
	/**
	 * return the com.sun.star.util.Color formed from the fundamental color
	 * the color is an object of typecom.sun.star.util.Color (a long)
	 * its bytes are: ignore, RGB:red,RGB:green,RGB:blue, hence grey will be:
	 *  127*256*256+127*256+127
	 * @param nRed
	 * @param nGreen
	 * @param nBlue
	 * @return
	 */
	public static int getRGBColor(int nRed, int nGreen, int nBlue) {
		return (nRed*256*256+nGreen*256+nBlue);
	}

	/*    *//**
			 * Embeds the given Image into a Textdocument at the given cursor
			 * position (Anchored as character)
			 * 
			 * @param grProps
			 *            OOo-style URL and width &amp; height of the graphic
			 * @param xMSF
			 *            the factory to create services from
			 * @param xCursor
			 *            the cursor where to insert the graphic
			 */
	/*
	 * private void embedGraphic(GraphicInfo grProps, XMultiServiceFactory xMSF,
	 * XTextCursor xCursor) {
	 * 
	 * XNameContainer xBitmapContainer = null; XText xText = xCursor.getText();
	 * XTextContent xImage = null; String internalURL = null;
	 * 
	 * try { xBitmapContainer = (XNameContainer) UnoRuntime.queryInterface(
	 * XNameContainer.class, xMSF.createInstance(
	 * "com.sun.star.drawing.BitmapTable")); xImage = (XTextContent)
	 * UnoRuntime.queryInterface( XTextContent.class, xMSF.createInstance(
	 * "com.sun.star.text.TextGraphicObject")); XPropertySet xProps =
	 * (XPropertySet) UnoRuntime.queryInterface( XPropertySet.class, xImage);
	 *  // helper-stuff to let OOo create an internal name of the graphic //
	 * that can be used later (internal name consists of various checksums)
	 * xBitmapContainer.insertByName("someID", grProps.unoURL); internalURL =
	 * AnyConverter.toString(xBitmapContainer .getByName("someID"));
	 * 
	 * xProps.setPropertyValue("AnchorType",
	 * com.sun.star.text.TextContentAnchorType.AS_CHARACTER);
	 * xProps.setPropertyValue("GraphicURL", internalURL);
	 * xProps.setPropertyValue("Width", (int) grProps.widthOfGraphic);
	 * xProps.setPropertyValue("Height", (int) grProps.heightOfGraphic);
	 *  // inser the graphic at the cursor position
	 * xText.insertTextContent(xCursor, xImage, false);
	 *  // remove the helper-entry xBitmapContainer.removeByName("someID"); }
	 * catch (Exception e) { System.out.println("Failed to insert Graphic"); } }
	 */

}
