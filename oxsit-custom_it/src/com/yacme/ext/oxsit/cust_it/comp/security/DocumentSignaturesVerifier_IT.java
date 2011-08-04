/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XStream;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.packages.WrongPasswordException;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.ChangesEvent;
import com.sun.star.util.XChangesListener;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.Utilities;
import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
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
import com.yacme.ext.oxsit.security.SignatureState;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesState;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
import com.yacme.ext.oxsit.security.XOX_SignatureState;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;
import com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

/** Verify a document signatures and the document
 * @author beppe
 *
 */
public class DocumentSignaturesVerifier_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
implements XServiceInfo, XComponent, XInitialization, XOX_DocumentSignaturesVerifier, XChangesListener {

	protected IDynamicLogger m_aLogger;
	protected IDynamicLogger m_aLoggerDialog;
	
	protected XComponentContext m_xCC;
	private XMultiComponentFactory m_xMCF;
	private XFrame m_xFrame;

	//the certificates corresponding to this document
	//every certificate means one signature.
	// this list can be retrieved using the method getX509Certificates() 
//	protected Vector<XOX_X509Certificate>	m_xQualCertList;

	//The signatures in this document
//	protected Vector<XOX_SignatureState>	m_xSignatures;
	
	protected HashMap<String, XOX_SignatureState> m_hSignatures;

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
		m_hSignatures = new HashMap<String, XOX_SignatureState>(10);
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

	private void cleanUpSignatures() {
		if(!m_hSignatures.isEmpty()) {
			Set<String> aSet =  m_hSignatures.keySet();
			Object	sUUIDs[] =  aSet.toArray();
			for(int i= 0; i < sUUIDs.length; i++) {
				//the signature states and the corresponding certificates are taken care of in the GUI side
				//which deallocate them when GUI is shut down.
//				XOX_SignatureState xQC = m_hSignatures.get(sUUIDs[i]);
//				if (xQC != null) {
//					XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xQC);
//					if(xComp != null)
//						xComp.dispose();					
//				}
				m_hSignatures.remove(sUUIDs[i]);
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
		cleanUpSignatures();
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
			m_aLogger.debug("el: "+aElements[i]);
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
				m_aLogger.debug("Version is: " + sVersion); // this should be 1.2 or more
			else
				m_aLogger.debug("Version is 1.0 or 1.1");
		}
		/*		else
					m_aLogger.debug("Version does not exists! May be this is not a ODF package?");*/

		//if version <1.2 then all excluding META-INF
		// else only the ones indicated
		//main contents
		fillElementList(xThePackage, aElements, "", true);
		return aElements;
	}

	/* 
	 * 
	 */
	private void addCertificate(XOX_SignatureState _xSignState, X509Certificate _aCert) {
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
			_xSignState.setSignersCerficate(xQualCert);
			
			//grab the signer name and set it
			XOX_X509CertificateDisplay aCertDisplay = 
				(XOX_X509CertificateDisplay)UnoRuntime.queryInterface(XOX_X509CertificateDisplay.class,xQualCert);
			
			if(aCertDisplay != null) {
				//set the certificate owner
				_xSignState.setSigner( aCertDisplay.getSubjectDisplayName());	
				//grab the CA
				//can be useful
				_xSignState.setCertificateIssuer(aCertDisplay.getIssuerDisplayName());
			}
		} catch (Exception e) {
			m_aLogger.severe(e);
		} catch (CertificateEncodingException e) {
			m_aLogger.severe(e);
		} catch (java.io.IOException e) {
			m_aLogger.severe(e);
		}
	}


	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#getSignatureState(java.lang.String)
	 */
	@Override
	public XOX_SignatureState getSignatureState(String _sSignatureID) {
		return m_hSignatures.get(_sSignatureID);
	}
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#getSignaturesState()
	 */
	@Override
	public XOX_SignatureState[] getSignatureStates() {
		final String __FUNCTION__ = "getSignatureStates: ";
		XOX_SignatureState[] ret = null;

		//detect the number of vector present
		if(!m_hSignatures.isEmpty()) {
			try {
				ret = new XOX_SignatureState[m_hSignatures.size()];
				Collection<XOX_SignatureState> retC = m_hSignatures.values();
				Object xObs[] = retC.toArray();
				for(int y = 0; y < xObs.length; y++)
					ret[y] = (XOX_SignatureState)xObs[y]; 

			} catch(NullPointerException ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			} catch(IndexOutOfBoundsException ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			} catch(ArrayStoreException ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			}  catch(Throwable ex) {
				m_aLogger.severe(__FUNCTION__,ex);
				ret = null;
			}
		}
		return ret;
	}

	/* this method should be called when there is need to load signatures from document into internal variables
	 * (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#loadAndGetSignatures(com.sun.star.frame.XFrame, com.sun.star.frame.XModel)
	 */
	@Override
	public XOX_SignatureState[] loadAndGetSignatures(XFrame _xFrame, XModel _xDocumentModel) throws IllegalArgumentException, Exception {
		final String __FUNCTION__ ="loadAndGetCertificates: ";
		XOX_SignatureState[] ret = null;
		try {
			ConfigManager.init("jar://ODFDocSigning.cfg");
			//remove the signature states currently in the list.
			cleanUpSignatures();
			//load the signatures from the provided document references	
			Object xStdoc;
			XStorage xDocumentStorage;
			
//			boolean openWithURL = true; //this needs to be set to false, when windows testing is finished
			
//		if(openWithURL) {

			//now, using the only method available, open the storage
			URL aURL = new URL(_xDocumentModel.getURL());
			
			URI aURI = aURL.toURI();

			m_aLogger.debug(_xDocumentModel.getURL());
			m_aLogger.debug(" aURL: "+aURL.toString());
			m_aLogger.debug(" aURL.getFile() "+aURL.getFile());
			m_aLogger.debug(" aURI.getPath() "+aURI.getPath());
			m_aLogger.debug(" aURL.getHost() "+aURL.getHost());
			m_aLogger.debug(" aURI: "+aURL.toURI().toString());

			String aPath = Helpers.fromURLtoSystemPath(_xDocumentModel.getURL());

			m_aLogger.debug(" aPath: "+aPath); // this is the host path
			
			//get URL, open the storage from url
			//we need to get the XStorage separately, from the document URL
			//But first we need a StorageFactory object
			Object xFact = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
			//then obtain the needed interface
			XSingleServiceFactory xStorageFact = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class,
					xFact);

			Object[] aArguments = new Object[2];
			aArguments[0] = aPath;//aURL.toString();//xDocumentModel.getURL();
//			aArguments[1] = ElementModes.READWRITE;
			aArguments[1] = ElementModes.READ;
			//get the document storage object 
			xStdoc = xStorageFact.createInstanceWithArguments(aArguments);

			xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);
//		}
//		else {
//			//from the storage object (or better named, the service) obtain the interface we need
//				XStorageBasedDocument xDocStorage =
//				(XStorageBasedDocument)UnoRuntime.queryInterface( XStorageBasedDocument.class, _xDocumentModel );
//				xDocumentStorage = xDocStorage.getDocumentStorage(); //(XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);
//		}
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
//DEBUG						m_aLogger.debug("=============>>> bytes: "+fTheSignaturesFile.available());
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
							//create a new signature state object
							Object aSigObj = m_xMCF.createInstanceWithContext(ConstantCustomIT.m_sSIGNATURE_STATE_SERVICE_IT, m_xCC);
							XOX_SignatureState aSignState = null;
							if(aSigObj != null) {
								aSignState = (XOX_SignatureState)UnoRuntime.queryInterface(XOX_SignatureState.class, aSigObj);
								if(aSignState == null)
									m_aLogger.severe(__FUNCTION__, "CANNOT OBTAIN A XOX_SignatureState INTERFACE !");
								else {
									m_aLogger.debug("Signature: " + sig.getId() + " - " + sig.getKeyInfo().getSubjectLastName() + ","
											+ sig.getKeyInfo().getSubjectFirstName() + "," + sig.getKeyInfo().getSubjectPersonalCode());

									aSignState.setState(SignatureState.NOT_YET_VERIFIED);
									aSignState.setSignatureUUID(sig.getId());
						            //pass the string to the signature state
						            aSignState.setSigningTime(
						            		Helpers.date2string(sig.getSignedProperties().getSigningTime()));
									
									// add the certificate of this signature to the certificate list and
									X509Certificate aCert = sig.getKeyInfo().getSignersCertificate();
									if(aCert != null) {
		//add the certificate to the internal list of certificates
										addCertificate(aSignState,aCert);

									}
									m_hSignatures.put(aSignState.getSignatureUUID(), aSignState);
								}
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
			ret = getSignatureStates();
			//get rid of the document storage: frees it and in the case of Windows the file is released as well
			((XComponent)UnoRuntime.queryInterface(XComponent.class, xStdoc)).dispose();				
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
	 * @see com.sun.star.util.XChangesListener#changesOccurred(com.sun.star.util.ChangesEvent)
	 */
	@Override
	public void changesOccurred(ChangesEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	@Override
	public void disposing(EventObject arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#verifyDocumentSignatures(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, java.lang.Object[])
	 */
	/*
	 * verifies the document signatures present.
	 * IMPORTANT, the loadAndGetSignatures MUST have been called first ! 
	 * returns the document aggregated signature state of the state of the single signature cheked
	 */
	@Override
	public int verifyDocumentSignatures(XFrame _xFrame, XModel _xDocumentModel, XOX_SignatureState xSignState) 
			throws IllegalArgumentException, Exception {
		final String __FUNCTION__ ="verifyDocumentSignatures: ";
		//from the document model, get the docu storage
		//get URL, open the storage from the url
		ConfigManager.init("jar://ODFDocSigning.cfg");
		if(_xDocumentModel == null || _xFrame == null)
			throw new IllegalArgumentException();

		XOX_SingletonDataAccess	  xSingletonDataAccess = null;
		XOX_DocumentSignaturesState xDocumentSignatures = null;
		try {
			xSingletonDataAccess = Helpers.getSingletonDataAccess(m_xCC);
			m_aLogger.debug(" singleton service data "+Helpers.getHashHex(xSingletonDataAccess) );
			xDocumentSignatures = xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(_xDocumentModel), this);			
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		} catch (ServiceNotFoundException e) {
			m_aLogger.severe("ctor",GlobConstant.m_sSINGLETON_SERVICE_INSTANCE+" missing!",e);
		} catch (NoSuchMethodException e) {
			m_aLogger.severe("ctor","XOX_SingletonDataAccess missing!",e);
		}

		try {
			XStorage xDocumentStorage;		
			//get URL, open the storage from url
			//we need to get the XStorage separately, from the document URL
			XStorageBasedDocument xDocStorage =
				(XStorageBasedDocument)UnoRuntime.queryInterface( XStorageBasedDocument.class, _xDocumentModel );
			
			//from the storage object (or better named, the service) obtain the interface we need
			//the document opened as such is read only
			xDocumentStorage = xDocStorage.getDocumentStorage(); //(XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);

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
//DEBUG						m_aLogger.debug("=============>>> bytes: "+fTheSignaturesFile.available());
						// create a new SignedDoc 
//						DigiDocFactory digFac = ConfigManager.instance().getSignedDocFactory();
						SAXSignedDocFactory aFactory = new SAXSignedDocFactory(m_xMCF, m_xCC, xDocumentStorage);
						ODFSignedDoc sdoc = (ODFSignedDoc) aFactory.readSignedDoc(fTheSignaturesFile);
						// verify signature

					    // add BouncyCastle provider if not done yet
						Security.addProvider((Provider)Class.forName(ConfigManager.instance().getProperty("DIGIDOC_SECURITY_PROVIDER")).newInstance());

						Signature sig = null;
//						cleanUpSignatures(); //free the current certificate list
						boolean foundTheSignatureID = false;
						for (int i = 0; i < sdoc.countSignatures(); i++) {
							sig = sdoc.getSignature(i);

							//FIXME, TODO: check in case of all the signatures checked, what to do in this case ?
							//now check, if the check is for all the signatures, this means we check the entire document signature list AND
							//update the signature list accordingly, if a single signature is to be checked
							if(xSignState == null || 
									(xSignState != null && xSignState.getSignatureUUID().equalsIgnoreCase(sig.getId()))) {
								foundTheSignatureID = true;
								//lookup in the list if the signature is contained within the list of already loaded signatures
								//create a new signature state object
								//or update the former one
								XOX_SignatureState aSignState = null;
								if(xSignState == null) {
									//need a new element
									Object aSigObj = m_xMCF.createInstanceWithContext(ConstantCustomIT.m_sSIGNATURE_STATE_SERVICE_IT, m_xCC);
									if(aSigObj != null)
										aSignState = (XOX_SignatureState)UnoRuntime.queryInterface(XOX_SignatureState.class, aSigObj);
								}
								else
									aSignState = xSignState;

									if(aSignState == null)
										m_aLogger.severe(__FUNCTION__, "CANNOT INSTANTIATE A: "+ConstantCustomIT.m_sSIGNATURE_STATE_SERVICE_IT+" SERVICE !");
									else {

										aSignState.setSignatureUUID(sig.getId());

							            //pass the string to the signature state
							            aSignState.setSigningTime(
							            		Helpers.date2string(sig.getSignedProperties().getSigningTime()));

										m_aLogger.debug("Signature: " + sig.getId() + " - " + sig.getKeyInfo().getSubjectLastName() + ","
												+ sig.getKeyInfo().getSubjectFirstName() + "," + sig.getKeyInfo().getSubjectPersonalCode());
										
										ArrayList<SignedDocException> errs = sig.verify(sdoc, true, false);
										
										if (errs.size() == 0) {
											m_aLogger.debug("Verification OK!");
											aSignState.setState(SignatureState.OK);
//FIXME: the update of the aggregate state should be done by whatever code calls this method...											
											Helpers.updateAggregateSignaturesState(xDocumentSignatures, GlobConstant.m_nSIGNATURESTATE_SIGNATURES_NOTVALIDATED);
										}
										else {
											aSignState.setState(SignatureState.ERR_VERIFY);
											for (int j = 0; j < errs.size(); j++)
												m_aLogger.severe(errs.get(j));
//FIXME: the update of the aggregate state should be done by whatever code calls this method...
											Helpers.updateAggregateSignaturesState(xDocumentSignatures, GlobConstant.m_nSIGNATURESTATE_SIGNATURES_BROKEN);
										}
										// add the certificate of this signature to the certificate list and
										X509Certificate aCert = sig.getKeyInfo().getSignersCertificate();
										if(aCert != null) {
			//add the certificate to the internal list of certificates
			// the signature just checked is updated in the list

	//										addCertificate(aSignState,aCert);
											//in case of all signatures verified, what need to be done about certificates ?
										}
										m_hSignatures.put(aSignState.getSignatureUUID(), aSignState);
									}
							}
						}
						if( xSignState != null && !foundTheSignatureID) {
							//then: no signature id found 
							
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
			//get rid of the document storage: frees it and in the case of Windows the file is released as well
//NOT HERE ! the storage is a copy of the main storage !	((XComponent)UnoRuntime.queryInterface(XComponent.class, xDocumentStorage)).dispose();				

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
}
