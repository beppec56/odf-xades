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

package it.plio.ext.oxsit.ooo.pack;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.*;
import com.sun.star.packages.WrongPasswordException;
import com.sun.star.uno.XComponentContext;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.logging.DynamicLogger;

import java.util.Vector;

/**
 *
 * @author __USER__
 */
public class DigitalSignatureHelper {
    
//    public OOoServerInfo SvrInfo = new OOoServerInfo();

	private DynamicLogger m_logger;
	protected XComponentContext m_xCtx;
	protected XMultiComponentFactory m_xMFC;
    /** Creates a new instance of __NAME__ */
    public DigitalSignatureHelper(XMultiComponentFactory _xMFC, XComponentContext _context) {
    	m_xCtx = _context;
    	m_xMFC = _xMFC;
    	m_logger = new DynamicLogger(this, _context);
//    	m_logger.enableLogging();
    	m_logger.info("ctor","");
    }

    public void fillElementList(XStorage xThePackage, Vector<APackageElement> _List, String _rootElement, boolean _bRecurse) {
		String[] aElements = xThePackage.getElementNames();
/*		m_aLogger.info(_rootElement+" elements:");
		for(int i = 0; i < aElements.length; i++)
			m_aLogger.info("'"+aElements[i]+"'");*/
		for(int i = 0; i < aElements.length; i++) {
			if( aElements[i] != "META-INF" ) {
				try {
					if( xThePackage.isStreamElement(aElements[i]) ) {
// try to open the element, read a few bytes, close it
						try {
							Object oObjXStreamSto = xThePackage.cloneStreamElement(aElements[i]);
							String sMediaType = "";
							int nSize = 0;
							XPropertySet xPset = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, oObjXStreamSto);
							if(xPset != null) { 
								try {
									sMediaType = AnyConverter.toString(xPset.getPropertyValue("MediaType"));
								} catch (UnknownPropertyException e) {
									m_logger.severe("fillElementList", e);
								} catch (WrappedTargetException e) {
									m_logger.severe("fillElementList", e);
								}
							}
							else
								m_logger.log("properties don't exist!");
							XStream xSt = (XStream)UnoRuntime.queryInterface(XStream.class, oObjXStreamSto);
							XInputStream xI = xSt.getInputStream();
							nSize = xI.available(); 
							xI.closeInput();
							_List.add( new APackageElement(_rootElement+aElements[i],sMediaType,nSize) );
						} catch (WrongPasswordException e) {
							// TODO Auto-generated catch block
							m_logger.warning("fillElementList", aElements[i]+" wrong password", e);
						}
					}
					else if(_bRecurse && xThePackage.isStorageElement(aElements[i])) {
						try
						{
							XStorage xSubStore = xThePackage.openStorageElement(aElements[i], ElementModes.READ);
							fillElementList(xSubStore, _List, _rootElement+aElements[i]+"/", _bRecurse);
							xSubStore.dispose();
						} 
						catch (IOException e) {
								m_logger.info("fillElementList", "the substorage "+aElements[i]+" might be locked, get the last committed version of it");
								   try {
									   Object oObj = m_xMFC.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCtx);
									   XSingleServiceFactory xStorageFactory = (XSingleServiceFactory)UnoRuntime.queryInterface(XSingleServiceFactory.class,oObj);
									   Object oMyStorage =xStorageFactory.createInstance();
									   XStorage xAnotherSubStore = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
									   xThePackage.copyStorageElementLastCommitTo( aElements[i], xAnotherSubStore );
									   fillElementList(xAnotherSubStore, _List,_rootElement+aElements[i]+"/", true);
									   xAnotherSubStore.dispose();						   
								   } catch (Exception e1) {
										m_logger.severe("fillElementList", "\""+aElements[i]+"\""+" missing", e1);
								   } // should create an empty temporary storage
						}
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
    }

    /**
     * closely resembles the function  DocumentSignatureHelper::CreateElementList
     * FIXME but need to be redesigned, because of concurrent access to streams/elements 
     * this list list only the main components, but not all the substore
     * We need instead to check for all the available Names and check them
     * 
     * @param _thePackage
     * @return
     */
    private Vector<APackageElement> makeTheElementList(Object _othePackage, XStorage _xStorage) {
    	//TODO: check for ODF 1.0 structure, see what to do in that case.
    	Vector<APackageElement> aElements = new Vector<APackageElement>(20);

    	//print the storage ODF version
    	
    	XStorage xThePackage;
    	if(_xStorage == null ){
    		xThePackage = (XStorage) UnoRuntime.queryInterface( XStorage.class, _othePackage );
    		m_logger.info("makeTheElementList", "use the URL storage");
//    		Utilities.showInterfaces(this,xThePackage);
    	}
    	else {
    		xThePackage = _xStorage;
    		m_logger.info("makeTheElementList", "use the document storage");
    	}
 
    	XPropertySet xPropset = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, _othePackage);
		if(xPropset != null) { // grab the version
			String sVersion = "1.0";
			try {
				sVersion = (String)xPropset.getPropertyValue("Version");
			} catch (UnknownPropertyException e) {
				m_logger.warning("makeTheElementList", "Version missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_logger.warning("makeTheElementList", "Version missing", e);
			}
			if(sVersion.length() != 0)
				m_logger.log("Version is: "+sVersion); // this should be 1.2 or more
			else
				m_logger.log("Version is 1.0 or 1.1");
		}

		//if version <1.2 then all excluding META-INF
		// else only the ones indicated
    	//main contents
		fillElementList(xThePackage, aElements,"", true);

/*    	//Thumbnails
		try {
			XStorage xSubStore = xThePackage.openStorageElement("Thumbnails", ElementModes.READ);
			fillElementList(xSubStore, aElements,"Thumbnails"+"/", true);
			xSubStore.dispose();
		}
		catch (IOException e) {
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Thumbnails\" substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Thumbnails\" substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Thumbnails\" substorage missing", e);
		}*/

    	//Basic
/*		try {
			XStorage xSubStore = xThePackage.openStorageElement("Basic", ElementModes.READ);
			fillElementList(xSubStore, aElements,"Basic"+"/", true);
			xSubStore.dispose();
		}
		catch (IOException e) {
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Basic\" substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Basic\" substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Basic\" substorage missing", e);
		}*/

		//Pictures
/*		try {
			XStorage xSubStore = xThePackage.openStorageElement("Pictures", ElementModes.READ);
			fillElementList(xSubStore, aElements,"Pictures"+"/", true);
			xSubStore.dispose();
		}
		catch (IOException e) {
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Pictures\" substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Pictures\" substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\"Pictures\" substorage missing", e);
		}*/

    	//OLE
		String sElementName = "";
		
/*		try {
			try {
				sElementName = "ObjectReplacements";
				XStorage xSubStore = xThePackage.openStorageElement(sElementName, ElementModes.READ);
				fillElementList(xSubStore, aElements,sElementName+"/", true);
				xSubStore.dispose();
			}
			catch (IOException e) {
				//no problem if not existent
				m_aLogger.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
			} catch (StorageWrappedTargetException e) {
				// TODO Auto-generated catch block
				//no problem if not existent
				m_aLogger.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				//no problem if not existent
				m_aLogger.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
			}*/
			
			//Object folders
/*			String aObjectName = new String("Object ");
			String[] aObjName = xThePackage.getElementNames();
			for(int i = 0; i < aObjName.length; i++) {
				sElementName = aObjName[i];
				if((aObjName[i].indexOf(aObjectName) != -1) && xThePackage.isStorageElement(aObjName[i]))  {
					XStorage xAnotherSubStore;
					try
					{
					   xAnotherSubStore = xThePackage.openStorageElement(aObjName[i], ElementModes.READ);
						fillElementList(xAnotherSubStore, aElements,aObjName[i]+"/", true);
						xAnotherSubStore.dispose();					
					}
					catch (IOException e)
					{
						m_aLogger.info("makeTheElementList", "the substorage "+aObjName[i]+" might be locked, get the last committed version of it");
					   try {
						   Object oObj = m_xMFC.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCtx);
						   XSingleServiceFactory xStorageFactory = (XSingleServiceFactory)UnoRuntime.queryInterface(XSingleServiceFactory.class,oObj);
						   Object oMyStorage =xStorageFactory.createInstance();
						   xAnotherSubStore = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
						   xThePackage.copyStorageElementLastCommitTo( aObjName[i], xAnotherSubStore );
						   fillElementList(xAnotherSubStore, aElements,aObjName[i]+"/", true);
						   xAnotherSubStore.dispose();						   
					   } catch (Exception e1) {
						// TODO Auto-generated catch block
							m_aLogger.severe("makeTheElementList", "\""+sElementName+"\""+" missing", e1);
					   } // should create an empty temporary storage
					} 					
				}
			}
		} */
/*		catch (IOException e) {
			//no problem if not existent
			m_aLogger.severe("makeTheElementList", "\""+sElementName+"\""+" missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\""+sElementName+" missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLogger.warning("makeTheElementList", "", e);
		}*/
    	return aElements;
    }

    /**
     * this one uses storage facity to access the ODF package
     * 
     * @param aTheDocURL
     * @param _xMCF
     * @param _xCompCtx
     */
    public void verifyDocumentSignature(XStorage _xStorage, String aTheDocURL) {
    	try {
    		// try from url
			Object oObj = m_xMFC.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCtx);
			if(oObj != null) {
				XSingleServiceFactory xStorageFactory = (XSingleServiceFactory)
							UnoRuntime.queryInterface(XSingleServiceFactory.class,oObj);
/*	            Object args[]=new Object[2];
	            args[0] = aTheDocURL;
	            args[1] = ElementModes.READ;
	            Object oMyStorage = xStorageFactory.createInstanceWithArguments(args);*/

//	            Vector<String> aElements = makeTheElementList(oMyStorage, null); // force the use of the package object from URL
//	            Vector<String> aElements = makeTheElementList(oMyStorage, _xStorage); // use of the package object from document
	            Vector<APackageElement> aElements = makeTheElementList(null, _xStorage); // use of the package object from document
	            m_logger.log("\nThis package contains the following elements:");
	            for(int i = 0; i < aElements.size();i++) {
	            	m_logger.log(aElements.get(i).toString());	            	
	            }
// using the created element list, test the file signature	            

// just for testing, try to open the META-INF substorage for writing (test possibility to write the signature file)
//grab the storage interface
	           	XStorage xThePackage = (XStorage) UnoRuntime.queryInterface( XStorage.class, _xStorage );
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
	        			m_logger.warning("verifyDocumentSignature", "\"META-INF\""+" missing", e);
	        		} catch (StorageWrappedTargetException e) {
	        			// TODO Auto-generated catch block
	        			//no problem if not existent
	        			m_logger.warning("verifyDocumentSignature", "\"META-INF\""+" missing", e);
	        		} catch (IllegalArgumentException e) {
	        			// TODO Auto-generated catch block
	        			//no problem if not existent
	        			m_logger.warning("verifyDocumentSignature", "\"META-INF\""+" missing", e);
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

    private class APackageElement {
    	public String m_stheName;
    	public String m_sMediaType;
    	public int	m_nSize;

    	public APackageElement(String s,String mt,int sz ) {
        	m_stheName = s;
        	m_sMediaType = mt;
        	m_nSize = sz;
    	}
    	
    	public String toString() {
    		String ret = "media type: '"+m_sMediaType+"' size: "+m_nSize+" bytes, position: '"+m_stheName+"'";
			return ret;
    	}
    }
}
