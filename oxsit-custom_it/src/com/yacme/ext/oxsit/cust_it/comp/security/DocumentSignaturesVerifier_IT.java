/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
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
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.Utilities;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.cust_it.comp.security.odfdoc.ODFSignedDoc;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.Signature;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.SAXSignedDocFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

/** Verify a document signatures and the document
 * @author beppe
 *
 */
public class DocumentSignaturesVerifier_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
implements XServiceInfo, XComponent, XInitialization, XOX_DocumentSignaturesVerifier {

	protected IDynamicLogger m_aLogger;
	protected IDynamicLogger m_aLoggerDialog;
	
	protected XComponentContext m_xCC;
	private XMultiComponentFactory m_xMCF;
	private XFrame m_xFrame;

	//the certificates corresponding to this document
	//every certificate means one signature.
	// this list can be retrieved using the method getX509Certificates() 
	protected Vector<XOX_X509Certificate>	m_xQualCertList;

	// the name of the class implementing this object
	public static final String m_sImplementationName = DocumentSignaturesVerifier_IT.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { ConstantCustomIT.m_sDOCUMENT_VERIFIER_SERVICE };
	
	public DocumentSignaturesVerifier_IT (XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
		m_aLoggerDialog = new DynamicLoggerDialog(this, _ctx);
		m_aLogger.enableLogging();
		m_aLoggerDialog.enableLogging();
		m_aLogger.ctor();

//		fillLocalizedStrings();
		m_xQualCertList = new Vector<XOX_X509Certificate>(10,1);		
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
	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		super.addEventListener(arg0);
	}

	private void cleanUpCertificates() {
		if(!m_xQualCertList.isEmpty()) {
			for(int i=0; i< m_xQualCertList.size();i++) {
				XOX_X509Certificate xQC = m_xQualCertList.elementAt(i);
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xQC);
				if(xComp != null)
					xComp.dispose();
			}
		}		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.star.lang.XComponent#dispose() called to clean up the class
	 * before closing
	 */
	@Override
	public void dispose() {
		// FIXME need to check if this element is referenced somewhere before deallocating it		m_aLogger.entering("dispose");
		//dispose of all the certificate
		cleanUpCertificates();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.
	 * XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
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
				m_aLoggerDialog.warning("fillElementList", aElements[i] + " missing", e);
			} catch (NoSuchElementException e) {
				m_aLoggerDialog.warning("fillElementList", aElements[i] + " missing", e);
			} catch (IllegalArgumentException e) {
				m_aLoggerDialog.warning("fillElementList", aElements[i] + " missing", e);
			} catch (StorageWrappedTargetException e) {
				m_aLoggerDialog.warning("fillElementList", aElements[i] + " missing", e);
			} catch (IOException e) {
				m_aLoggerDialog.warning("fillElementList", aElements[i] + " missing", e);
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

	/* 
	 * 
	 */
	private void addCertificate(X509Certificate _aCert) {
		// instantiate the components needed to check this certificate
		// create the Certificate Control UNO objects
		// first the certificate compliance control
		try {
			Object oCertCompl;
			oCertCompl = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sCERTIFICATE_COMPLIANCE_SERVICE_IT, m_xCC);
			// now the certification path control
			Object oCertPath = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sCERTIFICATION_PATH_SERVICE_IT, m_xCC);
			Object oCertRev = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sCERTIFICATE_REVOCATION_SERVICE_IT, m_xCC);
			Object oCertDisp = m_xMCF.createInstanceWithContext(
					ConstantCustomIT.m_sX509_CERTIFICATE_DISPLAY_SERVICE_SUBJ_IT,
					m_xCC);

			// prepare objects for subordinate service
			Object[] aArguments = new Object[6];
			// byte[] aCert = cert.getEncoded();
			// set the certificate raw value
			// prepare the certificate DER encoded form
			aArguments[0] =  Helpers.getDEREncoded(_aCert); // _aCert.getTBSCertificate();//       aCertificateAttributes.getDEREncoded();//_aDERencoded;// aCert;
			aArguments[1] = new Boolean(false);// FIXME change according to UI
												// (true) or not UI (false)
			// the order used for the following three certificate check objects
			// is the same that will be used for a full check of the certificate
			// if one of your checker object implements more than one interface
			// when XOX_X509Certificate.verifyCertificate will be called,
			// the checkers will be called in a fixed sequence (compliance,
			// certification path, revocation state).
			aArguments[2] = oCertCompl; // the compliance checker object, which
										// implements the needed interface
			aArguments[3] = oCertPath;// the certification path checker
			aArguments[4] = oCertRev; // the revocation state checker

			// the display formatter can be passed in any order, here it's the
			// last one
			aArguments[5] = oCertDisp;

			Object oACertificate;
			oACertificate = m_xMCF.createInstanceWithArgumentsAndContext(GlobConstant.m_sX509_CERTIFICATE_SERVICE,
							aArguments, m_xCC);
			// get the main interface
			XOX_X509Certificate xQualCert = (XOX_X509Certificate) UnoRuntime
					.queryInterface(XOX_X509Certificate.class, oACertificate);
			
			//add this device as the source device for this certificate
			//(will be handly if we sign with the corresponding private key)
			xQualCert.setSSCDevice(null);
//			xQualCert.setCertificateAttributes(aCertificateAttributes);
			m_xQualCertList.add(xQualCert);
		} catch (Exception e) {
			m_aLogger.severe(e);
		} catch (CertificateEncodingException e) {
			m_aLogger.severe(e);
		} catch (java.io.IOException e) {
			m_aLogger.severe(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#getX509Certificates()
	 */
	@Override
	public XOX_X509Certificate[] getX509Certificates() {
		XOX_X509Certificate[] ret = null;
		//detect the number of vector present
		if(!m_xQualCertList.isEmpty()) {
			ret = new XOX_X509Certificate[m_xQualCertList.size()];
			try {
				m_xQualCertList.copyInto(ret);
			} catch(NullPointerException ex) {
				m_aLogger.severe("getQualifiedCertificates",ex);
			} catch(IndexOutOfBoundsException ex) {
				m_aLogger.severe("getQualifiedCertificates",ex);
			} catch(ArrayStoreException ex) {
				m_aLogger.severe("getQualifiedCertificates",ex);
			}
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#loadAndGetCertificates(com.sun.star.frame.XFrame, com.sun.star.frame.XModel)
	 */
	@Override
	public XOX_X509Certificate[] loadAndGetCertificates(XFrame _xFrame, XModel _xDocumentModel) throws IllegalArgumentException, Exception {
		final String __FUNCTION__ ="loadAndGetCertificates: ";
		XOX_X509Certificate[] ret = null;
		try {
			ConfigManager.init("jar://ODFDocSigning.cfg");
			//remove the certificates eventually present in the list.
			cleanUpCertificates();
			//load the signatures from the provided document references	
			XStorage xDocumentStorage;		
			//get URL, open the storage from url
			//we need to get the XStorage separately, from the document URL
			//But first we need a StorageFactory object
			Object xFact = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
			//then obtain the needed interface
			XSingleServiceFactory xStorageFact = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class,
					xFact);
			//now, using the only method available, open the storage
			Object[] aArguments = new Object[2];
			aArguments[0] = _xDocumentModel.getURL();
			aArguments[1] = ElementModes.READWRITE;
			//get the document storage object 
			Object xStdoc = xStorageFact.createInstanceWithArguments(aArguments);
			//from the storage object (or better named, the service) obtain the interface we need
			xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);
			
			//prepare a zip file from URL
			File aZipFile;
			aZipFile = new File(Helpers.fromURLtoSystemPath(_xDocumentModel.getURL()));
			ZipFile aTheDocuZip = new ZipFile(aZipFile);
			
			if(aTheDocuZip != null) {
				//do not verify it,
				//openup the signature in META-INF zipped directory
				//point to the signature file: "META-INF/xadessignatures.xml"
				ZipEntry aSignaturesFileEntry = aTheDocuZip.getEntry(ConstantCustomIT.m_sSignatureStorageName+"/"+GlobConstant.m_sXADES_SIGNATURE_STREAM_NAME);
				if(aSignaturesFileEntry != null) {
				//read in the signature
					InputStream	fTheSignaturesFile = aTheDocuZip.getInputStream(aSignaturesFileEntry);
					if(fTheSignaturesFile != null) {
						
//DEBUG						m_aLogger.log("=============>>> bytes: "+fTheSignaturesFile.available());
						// create a new SignedDoc 
//						DigiDocFactory digFac = ConfigManager.instance().getSignedDocFactory();
						SAXSignedDocFactory aFactory = new SAXSignedDocFactory(m_xMCF, m_xCC, xDocumentStorage);
						ODFSignedDoc sdoc = (ODFSignedDoc) aFactory.readSignedDoc(fTheSignaturesFile);
						// verify signature

					    // add BouncyCastle provider if not done yet
						Security.addProvider((Provider)Class.forName(ConfigManager.instance().getProperty("DIGIDOC_SECURITY_PROVIDER")).newInstance());

						Signature sig = null;
						for (int i = 0; i < sdoc.countSignatures(); i++) {
							sig = sdoc.getSignature(i);
							
							m_aLogger.log("Signature: " + sig.getId() + " - " + sig.getKeyInfo().getSubjectLastName() + ","
									+ sig.getKeyInfo().getSubjectFirstName() + "," + sig.getKeyInfo().getSubjectPersonalCode());
							// add the certificate of this signature to the certificate list and
							X509Certificate aCert = sig.getKeyInfo().getSignersCertificate();
							if(aCert != null) {
								// add the certificate the error status detected (not the check using OCSP or CRL
								//add the certificate to the internal list of certificates
								addCertificate(aCert);
							}
						}
						fTheSignaturesFile.close();
					}
					else
						m_aLogger.warning(__FUNCTION__+" cannot open the signatures file into the document file");
				
				}
				else
					m_aLogger.warning(__FUNCTION__+" cannot open the signatures file entry into the document file");
				//instantiate the document reader (a wrapper) 
				aTheDocuZip.close();
			}
			else
				m_aLogger.warning(__FUNCTION__+" cannot open the document file");
				//simply load certificates
				
				//and return them
				ret = getX509Certificates();
		} catch (URISyntaxException e) {
			m_aLogger.severe(e);
		} catch (java.io.IOException e) {
			m_aLogger.severe(e);
		} catch (InstantiationException e) {
			m_aLogger.severe(e);
		} catch (IllegalAccessException e) {
			m_aLogger.severe(e);
		} catch (ClassNotFoundException e) {
			m_aLogger.severe(e);
		} catch (SignedDocException e) {
			m_aLogger.severe(e);
		}
		return ret;
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
		final String __FUNCTION__ ="verifyDocumentSignatures: ";
		//from the document model, get the docu storage
		//get URL, open the storage from the url
		ConfigManager.init("jar://ODFDocSigning.cfg");
		if(_xDocumentModel == null || _xFrame == null)
			throw new IllegalArgumentException();
		
		try {
			XStorage xDocumentStorage;		
			//get URL, open the storage from url
			//we need to get the XStorage separately, from the document URL
			//But first we need a StorageFactory object
			Object xFact = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
			//then obtain the needed interface
			XSingleServiceFactory xStorageFact = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class,
					xFact);
			//now, using the only method available, open the storage
			Object[] aArguments = new Object[2];
			aArguments[0] = _xDocumentModel.getURL();
			aArguments[1] = ElementModes.READWRITE;
			//get the document storage object 
			Object xStdoc = xStorageFact.createInstanceWithArguments(aArguments);

			//from the storage object (or better named, the service) obtain the interface we need
			xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);
			
			//prepare a zip file from URL
			File aZipFile = new File(Helpers.fromURLtoSystemPath(_xDocumentModel.getURL()));
			ZipFile aTheDocuZip = new ZipFile(aZipFile);
			
			
			if(aTheDocuZip != null) {
				//openup the signature in META-INF zipped directory
				//point to the signature file: "META-INF/xadessignatures.xml"
				ZipEntry aSignaturesFileEntry = aTheDocuZip.getEntry(ConstantCustomIT.m_sSignatureStorageName+"/"+GlobConstant.m_sXADES_SIGNATURE_STREAM_NAME);
				if(aSignaturesFileEntry != null) {
				//read in the signature
					InputStream	fTheSignaturesFile = aTheDocuZip.getInputStream(aSignaturesFileEntry);
					if(fTheSignaturesFile != null) {
						
//DEBUG						m_aLogger.log("=============>>> bytes: "+fTheSignaturesFile.available());
						// create a new SignedDoc 
//						DigiDocFactory digFac = ConfigManager.instance().getSignedDocFactory();
						SAXSignedDocFactory aFactory = new SAXSignedDocFactory(m_xMCF, m_xCC, xDocumentStorage);
						ODFSignedDoc sdoc = (ODFSignedDoc) aFactory.readSignedDoc(fTheSignaturesFile);
						// verify signature

					    // add BouncyCastle provider if not done yet
						Security.addProvider((Provider)Class.forName(ConfigManager.instance().getProperty("DIGIDOC_SECURITY_PROVIDER")).newInstance());
						
						Signature sig = null;
						cleanUpCertificates(); //free the current certificate list						
						for (int i = 0; i < sdoc.countSignatures(); i++) {
							sig = sdoc.getSignature(i);
							
							m_aLogger.log("Signature: " + sig.getId() + " - " + sig.getKeyInfo().getSubjectLastName() + ","
									+ sig.getKeyInfo().getSubjectFirstName() + "," + sig.getKeyInfo().getSubjectPersonalCode());
							ArrayList<SignedDocException> errs = sig.verify(sdoc, true, false);
							if (errs.size() == 0)
								m_aLogger.log("Verification OK!");
							for (int j = 0; j < errs.size(); j++)
								m_aLogger.severe(errs.get(j));
							// add the certificate of this signature to the certificate list and
							X509Certificate aCert = sig.getKeyInfo().getSignersCertificate();
							if(aCert != null) {
//add the certificate to the internal list of certificates
								addCertificate(aCert);
							}
							
							// add the certificate the error status detected (not the check using OCSP or CRL
							
						}
						fTheSignaturesFile.close();
					}
					else
						m_aLogger.warning(__FUNCTION__+" cannot open the signatures file into the document file");
				
				}
				else
					m_aLogger.warning(__FUNCTION__+" cannot open the signatures file entry into the document file");
				//instantiate the document reader (a wrapper) 
				aTheDocuZip.close();
			}
			else
				m_aLogger.warning(__FUNCTION__+" cannot open the document file");
		} catch (MalformedURLException e) {
			m_aLogger.severe(e);
		} catch (ZipException e) {
			m_aLogger.severe(e);
		} catch (java.io.IOException e) {
			m_aLogger.severe(e);
		} catch (URISyntaxException e) {
			m_aLogger.severe(e);
		} catch (SignedDocException e) {
			m_aLogger.severe(e);
		} catch (InstantiationException e) {
			m_aLogger.severe(e);
		} catch (IllegalAccessException e) {
			m_aLogger.severe(e);
		} catch (ClassNotFoundException e) {
			m_aLogger.severe(e);
		}
		
		return 0;
	}	
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#verifyDocumentSignatures(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, java.lang.Object[])
	 */
	/*
	 * verifies the document signatures present.
	 * returns the document aggregated signature state
	 */
//	@Override
	public int verifyDocumentSignatures_notused(XFrame _xFrame, XModel _xDocumentModel, Object[] args) 
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
//			try {
				xMetaInfStorage = xDocumentStorage.openStorageElement(ConstantCustomIT.m_sSignatureStorageName,ElementModes.READ);
//			}
//			catch (IOException e) {
//				m_aLogger.info(__FUNCTION__, "the substorage "+ConstantCustomIT.m_sSignatureStorageName+" might be locked, get the last committed version of it");
//			   Object oMyStorage =xStorageFact.createInstance();
//			   XStorage xAnotherSubStore = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
//			   xDocumentStorage.copyStorageElementLastCommitTo( ConstantCustomIT.m_sSignatureStorageName, xMetaInfStorage );
//			   xAnotherSubStore.dispose();						   
//			}	

			//read the file xadessignature.xml
			try {
				//the only supported method appears
				XStream xTheSignature = xMetaInfStorage.openStreamElement(/*ConstantCustomIT.m_sSignatureFileName*/ "manifest.xml", ElementModes.READ);

				if(xTheSignature != null) {
					XInputStream xInpStream = xTheSignature.getInputStream();

					if(xInpStream != null)
						m_aLogger.log("file "+ConstantCustomIT.m_sSignatureFileName+" opened, size: "+xInpStream.available());
				
					XComponent xStreamComp = (XComponent) UnoRuntime.queryInterface(XComponent.class, xTheSignature);
					if (xStreamComp == null)
						throw new com.sun.star.uno.RuntimeException();
					xStreamComp.dispose();
				}
			} catch (InvalidStorageException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName+"/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (IllegalArgumentException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName+"/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (WrongPasswordException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName+"/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (StorageWrappedTargetException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName+"/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			} catch (com.sun.star.io.IOException e1) {
				m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName+"/" + ConstantCustomIT.m_sSignatureFileName
						+ "\"" + " error", e1);
			}
			xMetaInfStorage.dispose();
		} catch (Exception e1) {
			m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName+ "\"" + " cannot open", e1);
				
		}
		//fill the signed document checker
		
		
		//verify the signature
		
		
		//examine the returned exception, 
		
		//if possible, fill the certificate structure, to be retrieved by the caller
		
		//returns the appropriate aggregate signatures state value
		
		
		return 0;
	}
}
