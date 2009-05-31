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

import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.beans.*;
import com.sun.star.container.*;
import com.sun.star.lang.*;
import com.sun.star.uno.*;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import com.sun.star.beans.PropertyValue;
import java.util.*;

/**
 * 
 * @author beppe
 */
public class Utilities {
	/** Creates a new instance of Utilities */
	public Utilities() {
		// sMsgArea = SysMsgArea.Instance();
		// AnyConverter = new AnyConverter();
		// Conf = OOoPDFMkConfig.Instance();
	}
	
	public static void showProperty(XPropertySet xPropSet, String pName) {
		try {
			XPropertySetInfo xPInfo = xPropSet.getPropertySetInfo();
			if (xPInfo.hasPropertyByName(pName)
					&& !AnyConverter.isVoid(xPropSet.getPropertyValue(pName))) {
				if (AnyConverter.isObject(xPropSet.getPropertyValue(pName))) {
					Object obj = xPropSet.getPropertyValue(pName);
					System.out.println(" isObject =-> " + obj.toString());
				} else if (AnyConverter.isString(xPropSet.getPropertyValue(pName))) {
					String st = AnyConverter.toString(xPropSet.getPropertyValue(pName));
					System.out.println(" isString =-> " + st);
				} else if (AnyConverter.isByte(xPropSet.getPropertyValue(pName))) {
					byte byt = AnyConverter.toByte(xPropSet.getPropertyValue(pName));
					System.out.println(" isByte =-> " + byt);
				} else if (AnyConverter.isShort(xPropSet.getPropertyValue(pName))) {
					short sho = AnyConverter.toShort(xPropSet.getPropertyValue(pName));
					System.out.println(" isShort =-> " + sho);
				} else if (AnyConverter.isInt(xPropSet.getPropertyValue(pName))) {
					int sho = AnyConverter.toInt(xPropSet.getPropertyValue(pName));
					System.out.println(" isInt =-> " + sho);
				} else if (AnyConverter.isFloat(xPropSet.getPropertyValue(pName))) {
					float sho = AnyConverter.toFloat(xPropSet.getPropertyValue(pName));
					System.out.println(" isFloat =-> " + sho);
				} else if (AnyConverter.isBoolean(xPropSet.getPropertyValue(pName))) {
					boolean sho = AnyConverter
							.toBoolean(xPropSet.getPropertyValue(pName));
					System.out.println(" Boolean =-> " + sho);
				} else
					System.out.println(" getType =-> "
							+ AnyConverter.getType(xPropSet.getPropertyValue(pName))
									.toString());
			} else
				System.out.println("(void)");
		} catch (com.sun.star.uno.Exception e) {
			System.out
					.println(" showProperty " + pName + " EXCEPTION: " + e.getMessage());
			// e.printStackTrace();
		}
	}

	private static String showPropertyString(XPropertySet xPropSet, String pName) {
		String theMessage = "";
		try {
			XPropertySetInfo xPInfo = xPropSet.getPropertySetInfo();
			if (xPInfo.hasPropertyByName(pName)
					&& !AnyConverter.isVoid(xPropSet.getPropertyValue(pName))) {
				if (AnyConverter.isObject(xPropSet.getPropertyValue(pName))) {
					Object obj = xPropSet.getPropertyValue(pName);
					theMessage = theMessage + " isObject =-> " + obj.toString();
				} else if (AnyConverter.isString(xPropSet.getPropertyValue(pName))) {
					String st = AnyConverter.toString(xPropSet.getPropertyValue(pName));
					theMessage = theMessage + " isString =-> " + st;
				} else if (AnyConverter.isByte(xPropSet.getPropertyValue(pName))) {
					byte byt = AnyConverter.toByte(xPropSet.getPropertyValue(pName));
					theMessage = theMessage + " isByte =-> " + byt;
				} else if (AnyConverter.isShort(xPropSet.getPropertyValue(pName))) {
					short sho = AnyConverter.toShort(xPropSet.getPropertyValue(pName));
					theMessage = theMessage + " isShort =-> " + sho;
				} else if (AnyConverter.isInt(xPropSet.getPropertyValue(pName))) {
					int sho = AnyConverter.toInt(xPropSet.getPropertyValue(pName));
					theMessage = theMessage + " isInt =-> " + sho;
				} else if (AnyConverter.isFloat(xPropSet.getPropertyValue(pName))) {
					float sho = AnyConverter.toFloat(xPropSet.getPropertyValue(pName));
					theMessage = theMessage + " isFloat =-> " + sho;
				} else if (AnyConverter.isBoolean(xPropSet.getPropertyValue(pName))) {
					boolean sho = AnyConverter
							.toBoolean(xPropSet.getPropertyValue(pName));
					theMessage = theMessage + " Boolean =-> " + sho;
				} else
					theMessage = theMessage + " getType =-> "
							+ AnyConverter.getType(xPropSet.getPropertyValue(pName))
									.toString();
			} else
				theMessage = theMessage + "(void)";
		} catch (Throwable e) {
			theMessage = theMessage + pName + " EXCEPTION: " + e.getMessage();
			// e.printStackTrace();
		}
		return theMessage;
	}

	private static String showPropertyString(XMultiPropertySet xPropSet, String pName) {
		String theMessage = "";		
		try {
			XPropertySetInfo xPInfo = xPropSet.getPropertySetInfo();
			if (xPInfo.hasPropertyByName(pName)) {
				String aNames[] = new String[1];
				aNames[0] = pName;
				Object[] aValues = xPropSet.getPropertyValues(aNames);
				if(!AnyConverter.isVoid(aValues[0])) {
					if (AnyConverter.isObject(aValues[0])) {
						theMessage = theMessage + (" isObject =-> " + aValues[0].toString());
					} else if (AnyConverter.isString(aValues[0])) {
						String st = AnyConverter.toString(aValues[0]);
						theMessage = theMessage + (" isString =-> " + st);
					} else if (AnyConverter.isByte(aValues[0])) {
						byte byt = AnyConverter.toByte(aValues[0]);
						theMessage = theMessage + (" isByte =-> " + byt);
					} else if (AnyConverter.isShort(aValues[0])) {
						short sho = AnyConverter.toShort(aValues[0]);
						theMessage = theMessage + (" isShort =-> " + sho);
					} else if (AnyConverter.isInt(aValues[0])) {
						int sho = AnyConverter.toInt(aValues[0]);
						theMessage = theMessage + (" isInt =-> " + sho);
					} else if (AnyConverter.isFloat(aValues[0])) {
						float sho = AnyConverter.toFloat(aValues[0]);
						theMessage = theMessage + (" isFloat =-> " + sho);
					} else if (AnyConverter.isBoolean(aValues[0])) {
						boolean sho = AnyConverter.toBoolean(aValues[0]);
						theMessage = theMessage + (" Boolean =-> " + sho);
					} else
						theMessage = theMessage + (" getType =-> "
								+ AnyConverter.getType(aValues[0]).toString());
				} else
					theMessage = theMessage + "(void)";
			}
		} catch (Throwable e) {
			theMessage = theMessage + " showProperty " + pName + " EXCEPTION: " + e.getMessage();
			// e.printStackTrace();
		}
		return theMessage;
	}

	public static void showProperty(XMultiPropertySet xPropSet, String pName) {
		System.out.println(showPropertyString(xPropSet,pName));
	}

	public static void showServiceProperties(Object caller, XInterface oObj) {
		XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(
				XPropertySet.class, oObj);
			showPropertiesString(null, xPropSet);
	}

	public static void showProperties(Object caller, XMultiPropertySet xPropSet) {
		String theMessage = "";
		if(caller != null)
			theMessage = caller.getClass().toString();
		if(xPropSet == null) {
			theMessage = theMessage + " ERROR: multi the property set is null !";
			System.out.println(theMessage);
			return;
		}		
		XPropertySetInfo xPInfo = xPropSet.getPropertySetInfo();
		if (xPInfo != null) {
			Property[] allProp = xPInfo.getProperties();
			int i;
			for (i = 0; i < allProp.length; i++) {
				theMessage = theMessage +"\n"+ i + " : " + allProp[i].Name + ",   " + allProp[i].Type + 
					Utilities.showPropertyString(xPropSet, allProp[i].Name);
			}
		} else { 
			theMessage = theMessage + " No property set available!";
		}
		System.out.println(theMessage);
	}
	
	private static String showPropertiesString(Object caller, XPropertySet xPropSet) {
		String theMessage = "";		
		if(caller != null)
			theMessage = theMessage + "\n" + caller.getClass().toString();
		if(xPropSet == null) {
			theMessage = theMessage + " ERROR: the property set is null !";
		}
		else {
			XPropertySetInfo xPInfo = xPropSet.getPropertySetInfo();
			if (xPInfo != null) {
				Property[] allProp = xPInfo.getProperties();
				int i;
				for (i = 0; i < allProp.length; i++) {
					theMessage = theMessage + "\n" + i + " : " + allProp[i].Name + ",   " + 
									allProp[i].Type + Utilities.showPropertyString(xPropSet, allProp[i].Name);
				}
			} else {
				theMessage = theMessage + " No property set info available! You need to access the properties one by one.";
			}
		}
		return theMessage;
	}

	public static void showProperties(Object caller, XPropertySet xPropSet) {
		System.out.println(showPropertiesString(caller, xPropSet));
	}

	/**
	 * @descr Print all available services of the given object to the standard
	 *        output.
	 */
	public static void showServices(Object caller, XInterface xObject) {
		try {
			if(caller != null)
				System.out.print(caller.getClass().toString());
			System.out.println(" services:");	
			XMultiServiceFactory xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(
					XMultiServiceFactory.class, xObject);
			if (xMSF == null)
				System.out
						.println("    object does not support interface XMultiServiceFactory");
			else {
				String[] sServiceNames = xMSF.getAvailableServiceNames();
				System.out.println("    object can create " + sServiceNames.length
						+ " services");
				for (int i = 0; i < sServiceNames.length; i++)
					System.out.println("        service " + i + " : " + sServiceNames[i]);
			}
		} catch (java.lang.Exception e) {
			System.out.println("caught exception in showServices : " + e);
		}
	}

	/**
	 * @descr Print the service and implementation name of the given object.
	 */
	public static void showInfo(XInterface xObject) {
		try {
			System.out.println("Info:");
			// Use interface XServiceName to retrieve name of (main) service.
			XServiceName xSN = (XServiceName) UnoRuntime.queryInterface(
					XServiceName.class, xObject);
			if (xSN == null)
				System.out.print("    interface XServiceName not supported");
			else {
				System.out.print("    Service name        : " + xSN.getServiceName());
			}

			// Use interface XServiceInfo to retrieve information about
			// supported services.
			XServiceInfo xSI = (XServiceInfo) UnoRuntime.queryInterface(
					XServiceInfo.class, xObject);
			if (xSI == null)
				System.out.println("    interface XServiceInfo not supported");
			else {
				System.out.println("    Implementation name : "
						+ xSI.getImplementationName());
			}
		} catch (java.lang.Exception e) {
			System.out.println("caught exception in showInfo : " + e);
		}
	}

	public static void showControlNames(XControlContainer _xC) {
		
		XControl[] xctrs = _xC.getControls();
		
		for(int i = 0; i < xctrs.length; i++) {
			XControl xcs = xctrs[i];
//			System.out.println(xcs.toString());
			
			showInterfaces(xcs, xcs);
		}
		
	}

	public static void showNames(XNameContainer _xC) {
		
		String[] xctrs = _xC.getElementNames();
		
		for(int i = 0; i < xctrs.length; i++) {
			System.out.println(xctrs[i]);
		}		
	}

	/**
	 * @descr Print information about supported interfaces.
	 */
	public static void showInterfaces(Object caller, XInterface xObject) {
		String theMessage = "";
		try {
			if(caller != null)
				theMessage = theMessage + caller.getClass().toString();		
			theMessage = theMessage + (" interfaces:");
			// Use interface XTypeProvider to retrieve a list of supported
			// interfaces.
			XTypeProvider xTP = (XTypeProvider) UnoRuntime.queryInterface(
					XTypeProvider.class, xObject);
			if (xTP == null)
				theMessage = theMessage + ("\n    interface XTypeProvider not supported");
			else {
				Type[] aTypeList = xTP.getTypes();
				theMessage = theMessage + ("\n    object supports " + aTypeList.length
						+ " interfaces");
				for (int i = 0; i < aTypeList.length; i++)
					theMessage = theMessage + ("\n        " + i + " : "
							+ aTypeList[i].getTypeName());
			}
		} catch (java.lang.Exception e) {
			System.out.println("caught exception in showInterfaces : " + e);
		}
		System.out.println(theMessage);
	}

	/**
	 * @descr Print information about supported interfaces.
	 */
	public static void showInterfaces(Object caller, Object xObject) {
		showInterfaces(caller, (XInterface) xObject);
	}
	
	public static String[] copyStringArray(String[] FirstArray) {
		if (FirstArray != null) {
			String[] SecondArray = new String[FirstArray.length];
			for (int i = 0; i < FirstArray.length; i++) {
				SecondArray[i] = FirstArray[i];
			}
			return SecondArray;
		} else
			return null;
	}

	public static Object[] initializeArray(Object[] olist, Object ovalue) {
		for (int i = 0; i < olist.length; i++)
			olist[i] = ovalue;
		return olist;
	}

	public static Object[][] initializeMultiDimArray(Object[][] olist, Object[] ovalue) {
		for (int i = 0; i < olist.length; i++)
			olist[i] = ovalue;
		return olist;
	}

	public static String[] ArrayOutOfMultiDimArray(String _sMultiDimArray[][], int _index) {
		String[] sRetArray = null;
		if (_sMultiDimArray != null) {
			sRetArray = new String[_sMultiDimArray.length];
			for (int i = 0; i < _sMultiDimArray.length; i++) {
				sRetArray[i] = _sMultiDimArray[i][_index];
			}
		}
		return sRetArray;
	}

	public static int[] initializeintArray(int FieldCount, int nValue) {
		int[] LocintArray = new int[FieldCount];
		for (int i = 0; i < LocintArray.length; i++)
			LocintArray[i] = nValue;
		return LocintArray;
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

	public static void getNames(XNameAccess xNames) {
		try {
			// Store the names of all auto text groups in an array of strings
			String[] aGroupNames = xNames.getElementNames();
	
			// Make sure we have at least one group name
			if (aGroupNames.length > 0) {
				for (int i = 0; i < aGroupNames.length; i++) {
					System.out.println(" " + aGroupNames[i]);
					// sMsgArea.println(" " + aGroupNames[i]);
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace(System.out);
		}
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

	/*
	 * public static String convertfromURLNotation(String _sURLPath) { String
	 * sPath = ""; try { URL oJavaURL = new URL(_sURLPath); File oFile =
	 * UrlToFileMapper.mapUrlToFile(oJavaURL); sPath = oFile.getAbsolutePath(); }
	 * catch (MalformedURLException e) { e.printStackTrace(System.out); } catch
	 * (IOException e) { e.printStackTrace(System.out); } return sPath; }
	 */
	public static DateTime getDateTime(long timeMillis) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		setTimeInMillis(cal, timeMillis);
		DateTime dt = new DateTime();
		dt.Year = (short) cal.get(Calendar.YEAR);
		dt.Day = (short) cal.get(Calendar.DAY_OF_MONTH);
		dt.Month = (short) (cal.get(Calendar.MONTH) + 1);
		dt.Hours = (short) cal.get(Calendar.HOUR);
		dt.Minutes = (short) cal.get(Calendar.MINUTE);
		dt.Seconds = (short) cal.get(Calendar.SECOND);
		dt.HundredthSeconds = (short) cal.get(Calendar.MILLISECOND);
		return dt;
	}

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

	/*
	 * public static String[] removeOutdatedFields(String[] baselist, String[]
	 * _complist) { String[] retarray = new String[] {}; if ((baselist != null) &&
	 * (_complist != null)) { Vector retvector = new Vector(); // String[]
	 * orderedcomplist = new String[_complist.length]; //
	 * System.arraycopy(_complist, 0, orderedcomplist, 0, _complist.length); for
	 * (int i = 0; i < baselist.length; i++) // if
	 * (Arrays.binarySearch(orderedcomplist, baselist[i]) != -1) if
	 * (FieldInList(_complist, baselist[i]) > -1) retvector.add(baselist[i]); //
	 * else // here you could call the method of a defined interface to notify
	 * the calling method // } retarray = new String[retvector.size()];
	 * retvector.toArray(retarray); } return (retarray); }
	 */

	/*
	 * public static String[][] removeOutdatedFields(String[][] baselist,
	 * String[] _complist, int _compindex) { String[][] retarray = new
	 * String[][] {}; if ((baselist != null) && (_complist != null)) { if
	 * (baselist.length > 0) { Vector retvector = new Vector(); for (int i = 0;
	 * i < baselist.length; i++) { if (FieldInList(_complist,
	 * baselist[i][_compindex]) != -1) retvector.add(baselist[i]); // else //
	 * here you could call the method of a defined interface to notify the
	 * calling method } retarray = new String[retvector.size()][2];
	 * retvector.toArray(retarray); } } return (retarray); }
	 * 
	 * 
	 * public static String[][] removeOutdatedFields(String[][] baselist,
	 * String[] _complist) { return removeOutdatedFields(baselist, _complist,
	 * 0); }
	 * 
	 * public static PropertyValue[][] removeOutdatedFields(PropertyValue[][]
	 * baselist, String[] _complist) { PropertyValue[][] retarray = new
	 * PropertyValue[][] { }; if ((baselist != null) && (_complist != null)) {
	 * Vector firstdimvector = new Vector(); int b = 0; for (int n = 0; n <
	 * baselist.length; n++) { Vector secdimvector = new Vector();
	 * PropertyValue[] internalArray; int a = 0; for (int m = 0; m <
	 * baselist[n].length; m++) { if (FieldInList(_complist,
	 * baselist[n][m].Name) > -1) { secdimvector.add(baselist[n][m]); a++; } }
	 * if (a > 0) { internalArray = new PropertyValue[a];
	 * secdimvector.toArray(internalArray); firstdimvector.add(internalArray);
	 * b++; } } retarray = new PropertyValue[b][];
	 * firstdimvector.toArray(retarray); } return (retarray); }
	 */

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

	/*
	 * public static String[] removefromList(String[] _sbaselist, String[]
	 * _sdellist){ Vector tempbaselist = new Vector(); for (int i = 0; i <
	 * _sbaselist.length; i++){ if (FieldInList(_sdellist, _sbaselist[i]) == -1)
	 * tempbaselist.add(_sbaselist[i]); } String[] sretlist = new
	 * String[tempbaselist.size()]; tempbaselist.toArray(sretlist); return
	 * sretlist; }
	 */

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
