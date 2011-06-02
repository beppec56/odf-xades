/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security;

import java.util.Vector;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.embed.XTransactedObject;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.io.XStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.packages.WrongPasswordException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Utilities;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.cust_it.comp.security.odfdoc.ODFDataDescription;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

/** Verify a document signatures and the document
 * @author beppe
 *
 */
public class DocumentSignaturesVerifier_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
implements XServiceInfo, XComponent, XInitialization, XOX_DocumentSignaturesVerifier {

	protected IDynamicLogger m_aLogger;
	
	protected XComponentContext m_xCC;
	private XMultiComponentFactory m_xMCF;
	private XFrame m_xFrame;

	// the name of the class implementing this object
	public static final String m_sImplementationName = DocumentSignaturesVerifier_IT.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { ConstantCustomIT.m_sDOCUMENT_VERIFIER_SERVICE };
	
	public DocumentSignaturesVerifier_IT (XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
		m_aLogger = new DynamicLoggerDialog(this, _ctx);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		
//		fillLocalizedStrings();
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	@Override
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;
		m_aLogger.info("supportsService", _sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals(m_sServiceNames[i]))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 */
	@Override
	public void initialize(Object[] _args) throws Exception {
		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#getX509Certificates()
	 */
	@Override
	public XOX_X509Certificate[] getX509Certificates() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#removeDocumentSignature(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, int, java.lang.Object[])
	 */
	@Override
	public boolean removeDocumentSignature(XFrame _xFrame, 
					XModel _xDocumentModel, int _nCertificatePosition, Object[] args)
			throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param xThePackage the storage element to examine
	 * @param _List the list to be filled, or updated
	 * @param _rootElement the name of the root element of the package 'xThePackage' 
	 * @param _bRecurse if can recurse (true) or not (false)
	 */
	private void fillElementList(XStorage xThePackage, Vector<ODFPackageItem> _List, String _rootElement, boolean _bRecurse) {
		String[] aElements = xThePackage.getElementNames();
		/*		m_aLoggerDialog.info(_rootElement+" elements:");
				for(int i = 0; i < aElements.length; i++)
					m_aLoggerDialog.info("'"+aElements[i]+"'");*/
		for (int i = 0; i < aElements.length; i++) {
			m_aLogger.log("el: "+aElements[i]);
			try {
				if (xThePackage.isStreamElement(aElements[i])) {
					//try to open the element, read a few bytes, close it
					try {
						Object oObjXStreamSto = xThePackage.cloneStreamElement(aElements[i]);
						String sMediaType = "";
						int nSize = 0;
						XPropertySet xPset = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, oObjXStreamSto);
						if (xPset != null) {
							try {
								sMediaType = AnyConverter.toString(xPset.getPropertyValue("MediaType"));
							} catch (UnknownPropertyException e) {
								m_aLogger.severe("fillElementList", e);
							} catch (WrappedTargetException e) {
								m_aLogger.severe("fillElementList", e);
							}
						} else
							m_aLogger.log("properties don't exist!");
						XStream xSt = (XStream) UnoRuntime.queryInterface(XStream.class, oObjXStreamSto);
						XInputStream xI = xSt.getInputStream();
						nSize = xI.available();
						//							xI.closeInput();
						_List.add(new ODFPackageItem(_rootElement + aElements[i], sMediaType, xI, nSize));
						m_aLogger.info("element: "+_rootElement+aElements[i]);
					} catch (WrongPasswordException e) {
						m_aLogger.warning("fillElementList", aElements[i] + " wrong password", e);
					}
				} else if (_bRecurse && xThePackage.isStorageElement(aElements[i])) {
					try {
						XStorage xSubStore = xThePackage.openStorageElement(aElements[i], ElementModes.READ);
						m_aLogger.info("recurse into element: "+_rootElement+aElements[i]);							
						fillElementList(xSubStore, _List, _rootElement+aElements[i]+"/", _bRecurse);
						xSubStore.dispose();
					}
					catch (IOException e) {
							m_aLogger.info("fillElementList", "the substorage "+aElements[i]+" might be locked, get the last committed version of it");
							   try {
								   Object oObj = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
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
				m_aLogger.warning("fillElementList", aElements[i] + " missing", e);
			} catch (NoSuchElementException e) {
				m_aLogger.warning("fillElementList", aElements[i] + " missing", e);
			} catch (IllegalArgumentException e) {
				m_aLogger.warning("fillElementList", aElements[i] + " missing", e);
			} catch (StorageWrappedTargetException e) {
				m_aLogger.warning("fillElementList", aElements[i] + " missing", e);
			} catch (IOException e) {
				m_aLogger.warning("fillElementList", aElements[i] + " missing", e);
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
	private Vector<ODFPackageItem> makeTheElementList(Object _othePackage, XStorage _xStorage) {
		//TODO check for ODF 1.0 structure, see what to do in that case.
		Vector<ODFPackageItem> aElements = new Vector<ODFPackageItem>(20);

		//print the storage ODF version

		XStorage xThePackage;
		if (_xStorage == null) {
			xThePackage = (XStorage) UnoRuntime.queryInterface(XStorage.class, _othePackage);
			m_aLogger.info("makeTheElementList", "use the URL storage");
			Utilities.showInterfaces(this, xThePackage);
		} else {
			xThePackage = _xStorage;
			m_aLogger.info("makeTheElementList", "use the document storage");
		}

		//		Utilities.showInterfaces(this,_othePackage);
		XPropertySet xPropset = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, _othePackage);

		//this chunk of code should be at the top package level
		if (xPropset != null) { // grab the version
			String sVersion = "1.0";
			try {
				sVersion = (String) xPropset.getPropertyValue("Version");
			} catch (UnknownPropertyException e) {
				m_aLogger.warning("makeTheElementList", "Version missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_aLogger.warning("makeTheElementList", "Version missing", e);
			}
			if (sVersion.length() != 0)
				m_aLogger.log("Version is: " + sVersion); // this should be 1.2 or more
			else
				m_aLogger.log("Version is 1.0 or 1.1");
		}
		/*		else
					m_aLogger.log("Version does not exists! May be this is not a ODF package?");*/

		//if version <1.2 then all excluding META-INF
		// else only the ones indicated
		//main contents
		fillElementList(xThePackage, aElements, "", true);
		return aElements;
	}
		
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#verifyDocumentSignatures(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, java.lang.Object[])
	 */
	/*
	 * verifies the document signatures present.
	 * returns the document aggregated signature state
	 */
	@Override
	public int verifyDocumentSignatures(XFrame _xFrame, XModel _xDocumentModel, Object[] args) 
			throws IllegalArgumentException, Exception {
		final String __FUNCTION__ ="verifyDocumentSignatures";
// FIXME should return the status of the signatures, may be the state of the aggregate document signatures should be implemented as uno type
		m_aLogger.log("verifyDocumentSignatures called");
		
		//from the document model, get the docu storage
		//get URL, open the storage from url
		//we need to get the XStorage separately, from the document URL
		//But first we need a StorageFactory object
		Object xStorageFactService = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
		//then obtain the needed interface
		XSingleServiceFactory xStorageFact = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class,xStorageFactService);

		//now, using the only method available, open the storage
		Object[] aArguments = new Object[2];
		aArguments[0] = _xDocumentModel.getURL();
		aArguments[1] = ElementModes.READWRITE;
		//get the document storage object 
		Object xStdoc = xStorageFact.createInstanceWithArguments(aArguments);

		//from the storage object (or better named, the service) obtain the interface we need
		XStorage xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);
		
		//read it in, form a list of element, to be used while verifying		
		Vector<ODFPackageItem> aElements = makeTheElementList(null, xDocumentStorage); // use of the package object from document
		m_aLogger.log("\nThis package contains the following elements:");

		for (int i = 0; i < aElements.size(); i++) {
			ODFPackageItem aElm = aElements.get(i);
			m_aLogger.log("Type: " + aElm.m_sMediaType + " name: " + aElm.m_stheName + " size: " + aElm.m_nSize);
		}
		
		//open it to access the xadessignatures.xml file
		//so, open the substorage META-INF form the main storage (e.g. the document)
		try {
			XStorage xMetaInfStorage = null;
			String sMetaInfo = "META-INF";
			try {
				xMetaInfStorage = xDocumentStorage.openStorageElement(sMetaInfo,ElementModes.WRITE);
			}
			catch (IOException e) {
				m_aLogger.info(__FUNCTION__, "the substorage "+sMetaInfo+" might be locked, get the last committed version of it");
			   Object oMyStorage =xStorageFact.createInstance();
			   XStorage xAnotherSubStore = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
			   xDocumentStorage.copyStorageElementLastCommitTo( sMetaInfo, xMetaInfStorage );
			   xAnotherSubStore.dispose();						   
			}	

			//read the file xadessignature.xml
			try {
				XStream xTheSignature = xMetaInfStorage.openStreamElement(ConstantCustomIT.m_sSignatureFileName, ElementModes.READWRITE);

				XInputStream xInpStream = xTheSignature.getInputStream();

//				sdoc.writeSignaturesToXStream(xOutStream);

//				XTransactedObject xTransObj = (XTransactedObject) UnoRuntime.queryInterface(XTransactedObject.class, xMetaInfStorage);
//				if (xTransObj != null) {
//					m_aLogger.log("XTransactedObject exists. ===================");
//					xTransObj.commit();
//				}

				m_aLogger.log("file "+ConstantCustomIT.m_sSignatureFileName+" opened");
				XComponent xStreamComp = (XComponent) UnoRuntime.queryInterface(XComponent.class,
						xTheSignature);
				if (xStreamComp == null)
					throw new com.sun.star.uno.RuntimeException();
				xStreamComp.dispose();

				//							            Utilities.showInterfaces(xDocStorage, xDocStorage);
				//							            Utilities.showInterfaces(m_xDocumentStorage, m_xDocumentStorage);
				//							            Utilities.showInterfaces(xMetaInfStorage, xMetaInfStorage);

//				xTransObj = (XTransactedObject) UnoRuntime.queryInterface(XTransactedObject.class,xDocumentStorage);
//				if (xTransObj != null) {
//					m_aLogger.log("XTransactedObject(m_xDocumentStorage) exists. ===================");
//					xTransObj.commit();
//				}

				//							            XCommonEmbedPersist xCommPer = UnoRuntime.queryInterface( XCommonEmbedPersist.class, m_xDocumentStorage );
				//							            if(xCommPer != null ) {
				//											m_aLogger.log("XCommonEmbedPersist exists. ===================");
				//											xCommPer.storeOwn();
				//							            }

			} catch (InvalidStorageException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + "META-INF/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (IllegalArgumentException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + "META-INF/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (WrongPasswordException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + "META-INF/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (StorageWrappedTargetException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + "META-INF/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (com.sun.star.io.IOException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + "META-INF/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			}

			xMetaInfStorage.dispose();
		} catch (Exception e1) {
			m_aLogger.severe(__FUNCTION__, "\"" + "META-INF" + "\"" + " cannot open", e1);
				
		}
		//fill the signed document checker
		
		
		//verify the signature
		
		
		//examine the returned exception, 
		
		//if possible, fill the certificate structure, to be retrieved by the caller
		
		//returns the appropriate aggregate signatures state value
		
		
		return 0;
	}
}
