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

package com.yacme.ext.oxsit;

import iaik.pkcs.pkcs11.wrapper.PKCS11Connector;
import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.TimeZone;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.frame.XModel;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.DateTime;
import com.sun.star.util.XChangesListener;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/** Helper class composed of static methods.
 *  
 * @author beppe
 */
public class Helpers {

	protected Helpers() {
	}

	/**
	 * @param xDocumentSignatures
	 * @param _nNewSignatureState
	 */
	//FIXME: better use a XOX_SignatureState ? So we will update the state of the aggragate signature state accordingly
	public static void updateAggregateSignaturesState(XOX_DocumentSignaturesState xDocumentSignatures, int _nNewSignatureState) {
		if(xDocumentSignatures != null) {
			int currentState = xDocumentSignatures.getAggregatedDocumentSignatureStates();
			int newState =  GlobConstant.m_nSIGNATURESTATE_SIGNATURES_BROKEN;
			
			switch(_nNewSignatureState) {
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK: //only if both cert and sign are check ok
				switch(currentState) {
				case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED:
				case GlobConstant.m_nSIGNATURESTATE_UNKNOWN:
				case GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES:
				case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK:
					newState = GlobConstant.m_nSIGNATURESTATE_SIGNATURES_OK; 
					break;
				default:
					newState = currentState; 
					break;					
				}
				break;
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_BROKEN:
				newState = GlobConstant.m_nSIGNATURESTATE_SIGNATURES_BROKEN; 
				break;
			case GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES:
				newState = GlobConstant.m_nSIGNATURESTATE_NOSIGNATURES;
				break;
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_INVALID:
				//switch?
				newState = GlobConstant.m_nSIGNATURESTATE_SIGNATURES_INVALID; 
				break;
			case GlobConstant.m_nSIGNATURESTATE_UNKNOWN:
				newState = GlobConstant.m_nSIGNATURESTATE_UNKNOWN;
				break;
			case GlobConstant.m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED:
				newState = GlobConstant.m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED;
				break;
			default:
				newState = currentState; 
				break;
			}
			xDocumentSignatures.setAggregatedDocumentSignatureStates(newState);
		}
	}

	//checks for connection to the internet through dummy request
    public static boolean isInternetReachable()
    {
            try {
                    //make a URL to a known source
                    URL url = new URL("http://www.google.com");

                    
                    //open a connection to that source
                    
                    HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

                    urlConnect.setConnectTimeout(20000);
                    //trying to retrieve data from the source. If there
                    //is no connection, this line will fail
                    @SuppressWarnings("unused")
					Object objData = urlConnect.getContent();

            } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
            }
            catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
            }
            return true;
    }

	public static String date2string(Date _aDate) {
		final String m_dateFormatXAdES = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        SimpleDateFormat f = new SimpleDateFormat(m_dateFormatXAdES);
        f.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String dateStr = f.format(_aDate);
        return dateStr;
	}

	public static String getIssuerName(X509Certificate _Cert) {
		//convert to bouncycaste
		String sRet = "";
		
		ByteArrayInputStream as;
		try {
			as = new ByteArrayInputStream(_Cert.getEncoded());
			ASN1InputStream aderin = new ASN1InputStream(as);
			DERObject ado;
			ado = aderin.readObject();
			X509CertificateStructure _aX509 = new X509CertificateStructure((ASN1Sequence) ado);
//extract the name, same as in display			
			X509Name aName = _aX509.getIssuer();
			Vector<DERObjectIdentifier> oidv =  aName.getOIDs();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			Vector<?> values = aName.getValues();
			for(int i=0; i< oidv.size(); i++) {
				hm.put(oidv.elementAt(i), values.elementAt(i).toString());
			}
			//look for givename (=nome di battesimo)
			//see BC source code for details about DefaultLookUp behaviour
			DERObjectIdentifier oix; 
			if(sRet.length() == 0) {
				//check for O
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("o")); 
				if(hm.containsKey(oix)) {
					sRet = hm.get(oix).toString();
				}
			}
			if(sRet.length() == 0) {
				//check for CN
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
				if(hm.containsKey(oix)) {
					sRet = hm.get(oix).toString();
				}
			}
			if(sRet.length() == 0) {
				//if still not, check for pseudodym
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
				if(hm.containsKey(oix))
					sRet = hm.get(oix).toString();						
			}
			//check for CN
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
			if(hm.containsKey(oix)) {
				sRet = sRet+((sRet.length()>0)? ", ":"")+hm.get(oix).toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		return sRet;
	}

	public static X509Certificate getCertificate(XOX_X509Certificate _aCert)
			throws CertificateException {
		java.security.cert.CertificateFactory cf;
		cf = java.security.cert.CertificateFactory.getInstance("X.509");
		java.io.ByteArrayInputStream bais = null;
		bais = new java.io.ByteArrayInputStream(_aCert.getCertificateAttributes().getDEREncoded());
		X509Certificate certJava = (java.security.cert.X509Certificate) cf.generateCertificate(bais);
		return certJava;
	}

	/** Returns the DER encoded form of a X509 certificate.
	 * @param _aCert the X509Certificate to encode
	 * @return a byte array representing the DER encoded form of the certificate
	 * @throws CertificateEncodingException
	 * @throws IOException
	 */
	public static byte[] getDEREncoded(X509Certificate _aCert) throws CertificateEncodingException, IOException {
		ByteArrayInputStream as;
		as = new ByteArrayInputStream(_aCert.getEncoded());
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado;
		ado = aderin.readObject();
		return  ado.getEncoded("DER"); // _aCert.getTBSCertificate();//       aCertificateAttributes.getDEREncoded();//_aDERencoded;// aCert;
	}
	                   
	public static String getCRLCacheSystemPath(XComponentContext _xCC) throws Exception, URISyntaxException, IOException {
		String filesep = System.getProperty("file.separator");
		return Helpers.getExtensionStorageSystemPath(_xCC)+
								filesep+GlobConstant.m_sCRL_CACHE_PATH;
    }
    
	public static String getExtensionStorageSystemPath(XComponentContext _xCC) throws Exception, URISyntaxException, IOException {		
		String filesep = System.getProperty("file.separator");
		return getUserStorageSystemPath(_xCC)+filesep+"extdata"+filesep+GlobConstant.m_sEXTENSION_IDENTIFIER;
	}

	public static String getUserStorageSystemPath(XComponentContext _xCC) throws Exception, URISyntaxException, IOException {
		String aPath = getUserStoragePathURL(_xCC);
		return fromURLtoSystemPath(aPath);
	}

	public static String getUserStoragePathURL(XComponentContext _xCC) throws Exception {
		XMultiComponentFactory xMCF = _xCC.getServiceManager();
		Object oPathServ = xMCF.createInstanceWithContext("com.sun.star.util.PathSettings", _xCC);
		if(oPathServ != null){
			XPropertySet xPS = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, oPathServ);
			String aPath = (String)xPS.getPropertyValue("Storage");
			return aPath;
		}
		else
			throw (new Exception("The PathSetting service can not be retrieved") );
	}

	//This works only for OCPS
	public static String getPKCS11WrapperNativeLibraryPath(XComponentContext _xContext)
	throws URISyntaxException, IOException, java.lang.NullPointerException
		{
			//detect the PKCS11 library class path 
			CodeSource aCs = PKCS11Connector.class.getProtectionDomain().getCodeSource();
			if(aCs != null) {
				URL aURL = aCs.getLocation(); // where this class is 'seen' by the java runtime
				URI aUri = new URI(aURL.toString());
				File aFile = new File(aUri);
				String classPath = aFile.getCanonicalPath();
				String sExtensionSystemPath = Helpers.getExtensionInstallationSystemPath(_xContext)+System.getProperty("file.separator");
				if(classPath.startsWith(sExtensionSystemPath))
					return getLocalNativeLibraryPath(_xContext, GlobConstant.PKCS11_WRAPPER);
				
			}
			return ""; 
		}

	/**
	 * Returns the complete path to the native binary library in the root extension directory
	 * Takes into account the architecture of the machine it is running on.
	 * FIXME: to be written into the user manual:
	 * IMPORTANT: this work right ONLY if in OOo you have a Oracle Java VM installed with the correct architecture, eg:
	 * a 64bit Java VM on a 64bit machine and:
	 * a 32bit Java VM on a 32bit machine.
	 * FIXME:
	 * to be checked on an OpenJDK installation 
	 * 
	 * @param _xContext
	 * @param _libName
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception 
	 */
	public static String getLocalNativeLibraryPath(XComponentContext _xContext, String _libName)
					throws URISyntaxException, IOException, java.lang.NullPointerException
						{
		String sExtensionSystemPath = Helpers.getExtensionInstallationSystemPath(_xContext)+System.getProperty("file.separator");		
		//now add the library name depending on os
        String osName = System.getProperty("os.name");
        String architecture = System.getProperty("os.arch");
    	//check the arch:
    	if(architecture.equalsIgnoreCase("amd64"))
    		architecture = "lib64"+System.getProperty("file.separator");
    	else
    		architecture = "lib32"+System.getProperty("file.separator");

        if(osName.toLowerCase().indexOf("windows") != -1){
        	// Windows OS detected
        	return sExtensionSystemPath+architecture+_libName+".dll";
        } else if(osName.toLowerCase().indexOf("linux") != -1){
            // Linux OS detected
        	return sExtensionSystemPath+architecture+"lib"+_libName+".so";
        } else //something else...
        	throw(new java.lang.NullPointerException("Native libraries for '"+osName+"' not available! Giving up."));
	}

	public static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
        	int halfbyte = (data[i] >>> 4) & 0x0F;
        	int two_halfs = 0;
        	do {
	        	if ((0 <= halfbyte) && (halfbyte <= 9))
	                buf.append((char) ('0' + halfbyte));
	            else
	            	buf.append((char) ('a' + (halfbyte - 10)));
	        	halfbyte = data[i] & 0x0F;
        	} while(two_halfs++ < 1);
        }
        return buf.toString();
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

	public static XOX_DocumentSignaturesState initDocumentSignaturesData(XComponentContext xContext, XModel _xModel, XChangesListener _xChg) 
	throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			XOX_SingletonDataAccess		m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
			XOX_DocumentSignaturesState		m_xDocumentSignatures =
								m_xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(_xModel), _xChg);
			if(m_xDocumentSignatures == null)
				throw (new NoSuchMethodException("XOX_DocumentSignaturesState missing") ); 									

			return m_xDocumentSignatures;
		}
	}

	public static XOX_DocumentSignaturesState getDocumentSignatures(XComponentContext xContext, XModel _xModel) 
	throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			XOX_SingletonDataAccess		m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
			XOX_DocumentSignaturesState		m_xDocumentSignatures =
								m_xSingletonDataAccess.getDocumentSignatures(Helpers.getHashHex(_xModel));
			if(m_xDocumentSignatures == null)
				throw (new NoSuchMethodException("XOX_DocumentSignaturesState missing") ); 									

			return m_xDocumentSignatures;
		}
	}
	
	/**
	 * returns the string URL of the path where the extension is installed
	 * @param context
	 * @return
	 */
	public static String getExtensionInstallationPath(XComponentContext context) {
		XPackageInformationProvider xPkgInfo = PackageInformationProvider.get( context );
		if(xPkgInfo != null)
			return xPkgInfo.getPackageLocation( GlobConstant.m_sEXTENSION_IDENTIFIER );
		return null;
	}
	
	public static String fromURLtoSystemPath(String _aUrl) throws URISyntaxException, IOException {
		if(_aUrl != null) {
			URL aURL = new URL(_aUrl);
			URI aUri = new URI(aURL.toString());
			File aFile = new File(aUri);
			return aFile.getCanonicalPath();
		}
		else
			return "";
	}

	/**
	 * returns the string URL of the path where the extension is installed
	 * @param context
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static String getExtensionInstallationSystemPath(XComponentContext context) throws URISyntaxException, IOException {
		String aPath = getExtensionInstallationPath(context);
		return fromURLtoSystemPath(aPath);
	}

	/**
	 * @param abyte
	 */
	public static String getCompactHexStringFromString(String _String) {
		//FIXME see if this conversion is ok, what about the char set?
		byte[] ret = _String.getBytes();
		
		String rets = "";
		for(int i=0;i<ret.length;i++) {
			rets = rets+ String.format("%02X", ret[i] );
		}
		return rets;
	}

	public static String printHexBytes(byte[] _theBytes) {
		String _sRet ="";
		for(int i = 0; i < _theBytes.length;i++) {
			if(i !=  0 && i % 16 == 0)
				_sRet = _sRet + " \n";
			try {
				_sRet = _sRet + String.format(" %02X", ( _theBytes[i] & 0xff ) );
			} catch(IllegalFormatException e) {
				e.printStackTrace();
			}
		}
		return _sRet;
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
