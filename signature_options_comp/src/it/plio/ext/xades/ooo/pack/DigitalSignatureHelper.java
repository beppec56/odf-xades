/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) as published
 *  by the European Community, either version 1.1 of the License,
 *  or any later version.
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

package it.plio.ext.xades.ooo.pack;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.*;
import com.sun.star.uno.XComponentContext;

import it.plio.ext.xades.logging.XDynamicLogger;

import java.util.Vector;

/**
 *
 * @author __USER__
 */
public class DigitalSignatureHelper {
    
//    public OOoServerInfo SvrInfo = new OOoServerInfo();

	private XDynamicLogger m_logger;
	
    /** Creates a new instance of __NAME__ */
    public DigitalSignatureHelper(XComponentContext _context) {
    	
    	m_logger = new XDynamicLogger(this, _context);
    	m_logger.enableLogging();
    	m_logger.info("ctor","");
    }

    public void fillElementList(XStorage xThePackage, Vector<String> _List, String _rootElement, boolean _bRecurse) {
    	
		String[] aElements = xThePackage.getElementNames();
		for(int i = 0; i < aElements.length; i++) {
			if( aElements[i] != "META-INF" ) {
				try {
					if( xThePackage.isStreamElement(aElements[i]) ) {
						_List.add(_rootElement+aElements[i]);
					}
					else if(_bRecurse && xThePackage.isStorageElement(aElements[i])) {
						XStorage xSubStore = xThePackage.openStorageElement(aElements[i], ElementModes.READ);
						fillElementList(xSubStore, _List, _rootElement+aElements[i]+"/", _bRecurse);
						xSubStore.dispose();
					}
				} catch (InvalidStorageException e) {
					m_logger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (NoSuchElementException e) {
					m_logger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (IllegalArgumentException e) {
					m_logger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (StorageWrappedTargetException e) {
					m_logger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (IOException e) {
					m_logger.warning("fillElementList", aElements[i]+" missing", e);
				}
			}
		}
//			m_logger.log(aElements[i]);
    }

    /**
     * closly resembles the function  DocumentSignatureHelper::CreateElementList 
     * @param _thePackage
     * @return
     */
    private Vector<String> createElemeList(Object _othePackage) {

    	Vector<String> aElements = new Vector<String>(20);

    	//print the storage ODF version
    	XStorage xThePackage = (XStorage) UnoRuntime.queryInterface( XStorage.class, _othePackage );
    	XPropertySet xPropset = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, _othePackage);
		if(xPropset != null) { // grab the version
			String sVersion = "1.0";
			try {
				sVersion = (String)xPropset.getPropertyValue("Version");
			} catch (UnknownPropertyException e) {
				m_logger.warning("createElemeList", "Version missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_logger.warning("createElemeList", "Version missing", e);
			}
			if(sVersion.length() != 0)
				m_logger.log("Version is: "+sVersion); // this should be 1.2 or more
			else
				m_logger.log("Version is 1.0 or 1.1");
		}

		//if version <1.2 then all excluding META-INF
		// else only the ones indicated
    	//main contents
		fillElementList(xThePackage, aElements,"", false);

    	//Pictures
		try {
			XStorage xSubStore = xThePackage.openStorageElement("Pictures", ElementModes.READ);
			fillElementList(xSubStore, aElements,"Pictures"+"/", true);
			xSubStore.dispose();
		}
		catch (IOException e) {
			//no problem if not existent
			m_logger.warning("createElemeList", "Pictures substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_logger.warning("createElemeList", "Pictures substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_logger.warning("createElemeList", "Pictures substorage missing", e);
		}

    	//OLE
		String sElementName = "";
		
		try {
			sElementName = "ObjectReplacements";
			XStorage xSubStore = xThePackage.openStorageElement(sElementName, ElementModes.READ);
			fillElementList(xSubStore, aElements,sElementName+"/", true);
			xSubStore.dispose();
			
			//Object folders
			String aObjectName = new String("Object ");
			String[] aObjName = xThePackage.getElementNames();
			for(int i = 0; i < aObjName.length; i++) {
				if((aObjName[i].indexOf(aObjectName) != -1) && xThePackage.isStorageElement(aObjName[i]))  {
					XStorage xAnotherSubStore = xThePackage.openStorageElement(aObjName[i], ElementModes.READ);
					fillElementList(xAnotherSubStore, aElements,aObjName[i]+"/", true);
					xAnotherSubStore.dispose();					
				}
			}			
		}
		catch (IOException e) {
			//no problem if not existent
			m_logger.severe("createElemeList", sElementName+" missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_logger.warning("createElemeList",  sElementName+" missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_logger.warning("createElemeList",  sElementName+" missing", e);
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_logger.warning("createElemeList", "", e);
		}
    	return aElements;
    }

    /**
     * this one uses storage facity to access the ODF package
     * 
     * @param aTheDocURL
     * @param _xMCF
     * @param _xCompCtx
     */
    public void verifyDocumentSignature(String aTheDocURL, XMultiComponentFactory _xMCF, XComponentContext _xCompCtx) {
    	try {	
			Object oObj = _xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", _xCompCtx);
			if(oObj != null) {
				XSingleServiceFactory xStorageFactory = (XSingleServiceFactory)
							UnoRuntime.queryInterface(XSingleServiceFactory.class,oObj);
	            Object args[]=new Object[2];
	            args[0] = aTheDocURL;
	            args[1] = ElementModes.READ;
	            Object oMyStorage = xStorageFactory.createInstanceWithArguments(args);

	            Vector<String> aElements = createElemeList(oMyStorage);
	            m_logger.log("\nThis package contains the following elements:");
	            for(int i = 0; i < aElements.size();i++) {
	            	m_logger.log(aElements.get(i));	            	
	            }
// using the created element list, test the file signature	            
	         
// just for testing, try to open the META-INF substorage for writing (test possibility to write the signature file)
//grab the storage interface
	           	XStorage xThePackage = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
	           	if(xThePackage != null) {
	           		//open the META-INF subpackage
	           		try {
	           			XStorage xSubStore = xThePackage.openStorageElement("META-INF", ElementModes.READWRITE);
	           		
	           			// note that the file manifest.xml is not presented 
	           			// we only get the other files
	           			String[] sNames = xSubStore.getElementNames();
	           			
	           			for(int i = 0; i < sNames.length;i++)
	           				m_logger.log(sNames[i]);	         	           			
	           			xSubStore.dispose();
	           		}
	        		catch (IOException e) {
	        			//no problem if not existent
	        			e.printStackTrace();
	        		} catch (StorageWrappedTargetException e) {
	        			// TODO Auto-generated catch block
	        			//no problem if not existent
	        			e.printStackTrace();
	        		} catch (IllegalArgumentException e) {
	        			// TODO Auto-generated catch block
	        			//no problem if not existent
	        			e.printStackTrace();
	        		}
	           	}
	            
			}
			else
				m_logger.log("No package storage factory!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
