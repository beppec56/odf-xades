/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.odfdoc;


import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.packages.WrongPasswordException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Utilities;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDoc;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;
import com.yacme.ext.oxsit.logging.DynamicLogger;

/**
 * describes the ODF document structure
 * @author beppe
 *
 */
public class ODFSignedDoc extends SignedDoc {

	/**
	 * 
	 */
    
//  public OOoServerInfo SvrInfo = new OOoServerInfo();

	private DynamicLogger m_aLogger;
	protected XComponentContext m_xCtx;
	protected XMultiComponentFactory m_xMFC;
	private XStorage m_xDocumentStorage;
	  
	private class APackageElement {
		public String m_stheName;
		public String m_sMediaType;
		public int	m_nSize;
		public XInputStream m_xInputStream;

		public APackageElement(String s,String mt, XInputStream _xInputStream, int sz ) {
			m_stheName = s;
			m_sMediaType = mt;
			m_xInputStream = _xInputStream;
			m_nSize = sz;
		}

		public String toString() {
			String ret = "media type: '"+m_sMediaType+"' size: "+m_nSize+" bytes, position: '"+m_stheName+"'";
			return ret;
		}
	}

	
	/** Creates a new instance of __NAME__ 
	 * @param version13 
	 * @param formatOdfXades 
	 * @param mXDocumentStorage */
//  public ODFSignedDoc(XMultiComponentFactory _xMFC, XComponentContext _context, XStorage mXDocumentStorage, String formatOdfXades, String version13) {
//  	m_xCtx = _context;
//  	m_xMFC = _xMFC;
//  	m_aLogger = new DynamicLogger(this, _context);
//  	m_aLogger.enableLogging();
//  	m_aLogger.info("ctor","");
//  }

	/**
	 * Creates new SignedDoc
	 * 
	 * @param format
	 *            file format name
	 * @param version
	 *            file version number
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public ODFSignedDoc(XMultiComponentFactory _xMFC, XComponentContext _context, XStorage _XDocumentStorage, String format, String version)
			throws SignedDocException {
		super(format, version);
	  	m_xCtx = _context;
	  	m_xMFC = _xMFC;
	  	m_aLogger = new DynamicLogger(this, _context);
	  	m_aLogger.enableLogging();
	  	m_aLogger.info("ctor","");
	  	m_xDocumentStorage = _XDocumentStorage;
	}
  
  public void fillElementList(XStorage xThePackage, Vector<APackageElement> _List, String _rootElement, boolean _bRecurse) {
		String[] aElements = xThePackage.getElementNames();
/*		m_aLoggerDialog.info(_rootElement+" elements:");
		for(int i = 0; i < aElements.length; i++)
			m_aLoggerDialog.info("'"+aElements[i]+"'");*/
		for(int i = 0; i < aElements.length; i++) {
			if( aElements[i] != "META-INF" ) {
				try {
					if( xThePackage.isStreamElement(aElements[i]) ) {
//try to open the element, read a few bytes, close it
						try {
							Object oObjXStreamSto = xThePackage.cloneStreamElement(aElements[i]);
							String sMediaType = "";
							int nSize = 0;
							XPropertySet xPset = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, oObjXStreamSto);
							if(xPset != null) { 
								try {
									sMediaType = AnyConverter.toString(xPset.getPropertyValue("MediaType"));
								} catch (UnknownPropertyException e) {
									m_aLogger.severe("fillElementList", e);
								} catch (WrappedTargetException e) {
									m_aLogger.severe("fillElementList", e);
								}
							}
							else
								m_aLogger.log("properties don't exist!");
							XStream xSt = (XStream)UnoRuntime.queryInterface(XStream.class, oObjXStreamSto);
							XInputStream xI = xSt.getInputStream();
							nSize = xI.available(); 
//							xI.closeInput();
							_List.add( new APackageElement(_rootElement+aElements[i],sMediaType,xI,nSize) );
						} catch (WrongPasswordException e) {
							// TODO Auto-generated catch block
							m_aLogger.warning("fillElementList", aElements[i]+" wrong password", e);
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
								m_aLogger.info("fillElementList", "the substorage "+aElements[i]+" might be locked, get the last committed version of it");
								   try {
									   Object oObj = m_xMFC.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCtx);
									   XSingleServiceFactory xStorageFactory = (XSingleServiceFactory)UnoRuntime.queryInterface(XSingleServiceFactory.class,oObj);
									   Object oMyStorage =xStorageFactory.createInstance();
									   XStorage xAnotherSubStore = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
									   xThePackage.copyStorageElementLastCommitTo( aElements[i], xAnotherSubStore );
									   fillElementList(xAnotherSubStore, _List,_rootElement+aElements[i]+"/", true);
									   xAnotherSubStore.dispose();						   
								   } catch (Exception e1) {
										m_aLogger.severe("fillElementList", "\""+aElements[i]+"\""+" missing", e1);
								   } // should create an empty temporary storage
						}
					}
				} catch (InvalidStorageException e) {
					m_aLogger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (NoSuchElementException e) {
					m_aLogger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (IllegalArgumentException e) {
					m_aLogger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (StorageWrappedTargetException e) {
					m_aLogger.warning("fillElementList", aElements[i]+" missing", e);
				} catch (IOException e) {
					m_aLogger.warning("fillElementList", aElements[i]+" missing", e);
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
  	//TODO check for ODF 1.0 structure, see what to do in that case.
  	Vector<APackageElement> aElements = new Vector<APackageElement>(20);

  	//print the storage ODF version

  	XStorage xThePackage;
  	if(_xStorage == null ){
  		xThePackage = (XStorage) UnoRuntime.queryInterface( XStorage.class, _othePackage );
  		m_aLogger.info("makeTheElementList", "use the URL storage");
  		Utilities.showInterfaces(this,xThePackage);
  	}
  	else {
  		xThePackage = _xStorage;
  		m_aLogger.info("makeTheElementList", "use the document storage");
  	}
  	
//		Utilities.showInterfaces(this,_othePackage);
  	XPropertySet xPropset = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, _othePackage);
  	
  	//this chunk of code should be at the top package level
		if(xPropset != null) { // grab the version
			String sVersion = "1.0";
			try {
				sVersion = (String)xPropset.getPropertyValue("Version");
			} catch (UnknownPropertyException e) {
				m_aLogger.warning("makeTheElementList", "Version missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_aLogger.warning("makeTheElementList", "Version missing", e);
			}
			if(sVersion.length() != 0)
				m_aLogger.log("Version is: "+sVersion); // this should be 1.2 or more
			else
				m_aLogger.log("Version is 1.0 or 1.1");
		}
/*		else
			m_aLogger.log("Version does not exists! May be this is not a ODF package?");*/
			

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
			m_aLoggerDialog.warning("makeTheElementList", "\"Thumbnails\" substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Thumbnails\" substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Thumbnails\" substorage missing", e);
		}*/

  	//Basic
/*		try {
			XStorage xSubStore = xThePackage.openStorageElement("Basic", ElementModes.READ);
			fillElementList(xSubStore, aElements,"Basic"+"/", true);
			xSubStore.dispose();
		}
		catch (IOException e) {
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Basic\" substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Basic\" substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Basic\" substorage missing", e);
		}*/

		//Pictures
/*		try {
			XStorage xSubStore = xThePackage.openStorageElement("Pictures", ElementModes.READ);
			fillElementList(xSubStore, aElements,"Pictures"+"/", true);
			xSubStore.dispose();
		}
		catch (IOException e) {
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Pictures\" substorage missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Pictures\" substorage missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\"Pictures\" substorage missing", e);
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
				m_aLoggerDialog.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
			} catch (StorageWrappedTargetException e) {
				// TODO Auto-generated catch block
				//no problem if not existent
				m_aLoggerDialog.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				//no problem if not existent
				m_aLoggerDialog.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
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
						m_aLoggerDialog.info("makeTheElementList", "the substorage "+aObjName[i]+" might be locked, get the last committed version of it");
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
							m_aLoggerDialog.severe("makeTheElementList", "\""+sElementName+"\""+" missing", e1);
					   } // should create an empty temporary storage
					} 					
				}
			}
		} */
/*		catch (IOException e) {
			//no problem if not existent
			m_aLoggerDialog.severe("makeTheElementList", "\""+sElementName+"\""+" missing", e);
		} catch (StorageWrappedTargetException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\""+sElementName+" missing", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "\""+sElementName+"\""+" missing", e);
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			//no problem if not existent
			m_aLoggerDialog.warning("makeTheElementList", "", e);
		}*/
  	return aElements;
  }

    /**
   * @return
   */
  public byte[] addODFData() {
		
		byte[] manifestBytes = null;
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// Acceso al manifest.xml y a la lista de elementos que contiene
//			InputStream manifest = new ByteArrayInputStream(odf
//					.getEntry("META-INF/manifest.xml"));
//
//			Document docManifest = documentBuilder.parse(manifest);
//			Element rootManifest = docManifest.getDocumentElement();
//			NodeList listFileEntry = rootManifest
//					.getElementsByTagName("manifest:file-entry");

//insert a loop to read all the stuff from the external document, using the internal
//OOo API
			
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
	            Vector<APackageElement> aElements = makeTheElementList(null, m_xDocumentStorage); // use of the package object from document
	            m_aLogger.log("\nThis package contains the following elements:");
	            
//loop to add the data to the internal object for signature
	            for(int i = 0; i < aElements.size();i++) {
	            	APackageElement aElm = aElements.get(i);
	            	m_aLogger.log(aElm.toString());
	            	if(aElm.m_sMediaType.equalsIgnoreCase("text/xml")) {
	            		//is an xml file
						ODFDataDescription df = new ODFDataDescription(aElm.m_xInputStream,
						 aElm.m_stheName, aElm.m_sMediaType, aElm.m_stheName,
						ExternalDataFile.CONTENT_ODF_PKG_XML_ENTRY,
						this);
						addDataFile(df);
	            	}
	            }
			}
			

//			for (int i = 0; i < listFileEntry.getLength(); i++) {
//				Element e = ((Element) listFileEntry.item(i));
//
//				String fullPath = e.getAttribute("manifest:full-path");
//				String mediaType = e.getAttribute("manifest:media-type");
//
//				// Solo procesamos los ficheros
//				if (!fullPath.endsWith("/")
//						&& !fullPath.equals("META-INF/documentsignatures.xml")) {
//					if ((odf.getEntry(fullPath).length != 0)
//							&& (fullPath.equals("manifest.rdf") || fullPath
//									.endsWith(".xml"))) {
//						// Obtenemos el fichero, canonizamos y calculamos el
//						// digest
//						InputStream xmlFile = new ByteArrayInputStream(odf
//								.getEntry(fullPath));
//
//
//						
//						
//						
//						ExternalDataFile df = new ExternalDataFile(xmlFile,
//								fullPath, mediaType, fullPath,
//								ExternalDataFile.CONTENT_ODF_PKG_XML_ENTRY,
//								this);
//						addDataFile(df);
//
//					} else {
//
//						InputStream binaryStream = new ByteArrayInputStream(odf
//								.getEntry(fullPath));
//						ExternalDataFile df = new ExternalDataFile(binaryStream,
//								fullPath, mediaType, fullPath,
//								ExternalDataFile.CONTENT_ODF_PKG_BINARY_ENTRY,
//								this);
//						addDataFile(df);
//
//					}
//
//				}
//			}
//			// ROB: mimetype
//			if (odf.hasEntry("mimetype")) {
//
//				InputStream xmlStream = new ByteArrayInputStream(odf
//						.getEntry("mimetype"));
//				ExternalDataFile df = new ExternalDataFile(xmlStream, "mimetype",
//						"text/text", "mimetype",
//						ExternalDataFile.CONTENT_ODF_PKG_BINARY_ENTRY, this);
//				addDataFile(df);
//			}
//
//			// ROB creazione del data file per manifest.xml aggiornato
//			// Añadimos el fichero de firma al manifest.xml
//			// Aggiungiamo a manifest.xml l'entry per documensignatures.xml
//			Element nodeDocumentSignatures = docManifest
//					.createElement("manifest:file-entry");
//			nodeDocumentSignatures.setAttribute("manifest:media-type", "");
//			nodeDocumentSignatures.setAttribute("manifest:full-path",
//					"META-INF/xadessignatures.xml");
//			rootManifest.appendChild(nodeDocumentSignatures);
//
//			Element nodeMetaInf = docManifest
//					.createElement("manifest:file-entry");
//			nodeMetaInf.setAttribute("manifest:media-type", "");
//			nodeMetaInf.setAttribute("manifest:full-path", "META-INF/");
//			rootManifest.appendChild(nodeMetaInf);
//			
//			ByteArrayOutputStream manifestOs = new ByteArrayOutputStream();
//			writeXML(manifestOs, rootManifest, false);
//			manifestBytes = manifestOs.toByteArray();
//			ByteArrayInputStream manifestIs = new ByteArrayInputStream(manifestBytes);
//			
//			ExternalDataFile df = new ExternalDataFile(manifestIs, "META-INF/manifest.xml",
//					"text/text", "META-INF/manifest.xml",
//					ExternalDataFile.CONTENT_ODF_PKG_XML_ENTRY, this);
//			addDataFile(df);
//			
//			
//			

		} catch (ParserConfigurationException e1) {
			m_aLogger.log(e1, true);
		} catch (IOException e) {
			m_aLogger.log(e, true);
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			m_aLogger.log(e, true);
//		} catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (SignedDocException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			m_aLogger.log(e, true);
		}
		
		return manifestBytes;
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
	            m_aLogger.log("\nThis package contains the following elements:");
	            for(int i = 0; i < aElements.size();i++) {
	            	m_aLogger.log(aElements.get(i).toString());	            	
	            }
//using the created element list, test the file signature	            

//just for testing, try to open the META-INF substorage for writing (test possibility to write the signature file)
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
	           				m_aLogger.log(sNames[i]);	         	           			
	           			xSubStore.dispose();
	           		}
	        		catch (IOException e) {
	        			//no problem if not existent
	        			m_aLogger.warning("verifyDocumentSignature", "\"META-INF\""+" missing", e);
	        		} catch (StorageWrappedTargetException e) {
	        			// TODO Auto-generated catch block
	        			//no problem if not existent
	        			m_aLogger.warning("verifyDocumentSignature", "\"META-INF\""+" missing", e);
	        		} catch (IllegalArgumentException e) {
	        			// TODO Auto-generated catch block
	        			//no problem if not existent
	        			m_aLogger.warning("verifyDocumentSignature", "\"META-INF\""+" missing", e);
	        		}
	           	}
	            
			}
			else
				m_aLogger.log("No package storage factory!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  
}
