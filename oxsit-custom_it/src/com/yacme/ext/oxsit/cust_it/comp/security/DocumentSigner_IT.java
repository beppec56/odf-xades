/* ***** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/DocumentSigner_IT.java.
 *
 * The Initial Developer of the Original Code is
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.cust_it.comp.security;

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.CK_TOKEN_INFO;
import iaik.pkcs.pkcs11.wrapper.PKCS11Constants;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.embed.XTransactedObject;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.XOutputStream;
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
import com.sun.star.presentation.XPresentationSupplier;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextEmbeddedObjectsSupplier;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;
import com.sun.star.util.XModifiable;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.Utilities;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.cust_it.comp.security.odfdoc.ODFSignedDoc;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.Signature;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.SAXSignedDocFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;
import com.yacme.ext.oxsit.custom_it.LogJarVersion;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import com.yacme.ext.oxsit.ooo.ui.DialogQueryPIN;
import com.yacme.ext.oxsit.ooo.ui.MessageASimilarCertExists;
import com.yacme.ext.oxsit.ooo.ui.MessageAskForSignatureRemoval;
import com.yacme.ext.oxsit.ooo.ui.MessageEmbededObjsPresentInTextDocument;
import com.yacme.ext.oxsit.ooo.ui.MessageError;
import com.yacme.ext.oxsit.ooo.ui.MessageNoSignatureToken;
import com.yacme.ext.oxsit.pkcs11.PKCS11Driver;
import com.yacme.ext.oxsit.security.PKCS11TokenAttributes;
import com.yacme.ext.oxsit.security.ReadCerts;
import com.yacme.ext.oxsit.security.XOX_DocumentSigner;
import com.yacme.ext.oxsit.security.XOX_SSCDevice;
import com.yacme.ext.oxsit.security.XOX_SignatureState;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;


/**
 * This service implements the real document signer.
 * receives the doc information from the task  
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every signature
 * ca be retrieved through properties 
 * 
 * @author beppec56
 *
 */
public class DocumentSigner_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
		implements XServiceInfo, XComponent, XInitialization, XOX_DocumentSigner {

	// the name of the class implementing this object
	public static final String m_sImplementationName = DocumentSigner_IT.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { GlobConstant.m_sDOCUMENT_SIGNER_SERVICE_IT };

	protected IDynamicLogger m_aLogger;

	// these are the listeners on this document signatures changes
	public HashMap<XChangesListener, XChangesListener> m_aListeners = new HashMap<XChangesListener, XChangesListener>(10);

	protected XComponentContext m_xCC;
	private XMultiComponentFactory m_xMCF;
	private XFrame m_xFrame;
	private ReadCerts m_sHelperCerts;
	private PKCS11Driver m_aHelperPkcs11;
	private String m_sPkcs11WrapperLocal;
	private String m_sPkcs11CryptoLib;
	private String m_sPINIsLocked = "";

	private static final int IS_ODF10_OR_11 = 0;
	private static final int IS_ODF12 = 2;

	private int m_nTypeOfDocumentToBeSigned = -1;
	private String m_sErrorNoDocumentType;
	private String m_sErrorNotYetSaved;
	private String m_sErrorGraphicNotEmbedded;
	private String m_sErroreIsNotReadOnly;
	private String m_sErrorMacroPresent;

	//ROB
	private static final String DIGEST_SHA1 = OIWObjectIdentifiers.idSHA1.getId();
	private static final String DIGEST_SHA256 = NISTObjectIdentifiers.id_sha256.getId();

	/**
	 * 
	 * 
	 * @param _ctx the UNO context
	 */
	public DocumentSigner_IT(XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
		m_aLogger = new DynamicLoggerDialog(this, _ctx);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		fillLocalizedStrings();
	}

	/**
	 * 
	 */
	private void fillLocalizedStrings() {
		MessageConfigurationAccess _aRegAcc = new MessageConfigurationAccess(m_xCC, m_xMCF);

		m_aLogger.enableLogging();

		try {
			m_sErrorNoDocumentType = _aRegAcc.getStringFromRegistry("id_wrong_format_document");
			m_sErrorNotYetSaved = _aRegAcc.getStringFromRegistry("id_wrong_docum_not_saved");
			m_sErrorGraphicNotEmbedded = _aRegAcc.getStringFromRegistry("id_url_linked_graphics");
			m_sErroreIsNotReadOnly = _aRegAcc.getStringFromRegistry("id_docum_is_not_readonly");
			m_sErrorMacroPresent = _aRegAcc.getStringFromRegistry("id_docum_contains_macro");
		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("", "", e);
		}
		_aRegAcc.dispose();
	}

	@Override
	public String getImplementationName() {
		m_aLogger.entering("getImplementationName");
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.debug("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.debug("supportsService", _sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals(m_sServiceNames[i]))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 * when instantiated, 
	 * 	_oObj[0] first argument document URL
	 *  _oObj[1] corresponding XStorage object
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		m_aLogger.entering("initialize");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSigner#removeDocumentSignature(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, com.yacme.ext.oxsit.security.XOX_SignatureState)
	 */
	@Override
	public boolean removeDocumentSignature(XFrame _xFrame, 
					XModel _xDocumentModel, XOX_SignatureState _aSignState)
			throws IllegalArgumentException, Exception {
		final String __FUNCTION__ ="removeDocumentSignature: ";
		boolean bSignatureRemoved = false;

		m_aLogger.debug(__FUNCTION__+"UUID: "+_aSignState.getSignatureUUID());
		
		ODFSignedDoc sdoc = null;

		ConfigManager.init("jar://ODFDocSigning.cfg");

		try {
			XStorage xDocumentStorage;

			String aPath = Helpers.fromURLtoSystemPath(_xDocumentModel.getURL());

			m_aLogger.debug(" aPath: "+aPath); // this is the host path
		
			//1) look for a signature file in the document
			//get URL, open the storage from url
			//we need to get the XStorage separately, from the document URL
			//But first we need a StorageFactory object
			Object xFact = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
			//then obtain the needed interface
			XSingleServiceFactory xStorageFact = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class,
					xFact);
	
			Object[] aArguments = new Object[2];
			aArguments[0] = aPath;//aURL.toString();//xDocumentModel.getURL();
			aArguments[1] = ElementModes.READWRITE;	//if readonly: ElementModes.READ
			//get the document storage object
			Object xStdoc = xStorageFact.createInstanceWithArguments(aArguments);

			//from the storage object (or better named, the service) obtain the interface we need
			xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);
			//chek if we have a signature already in place
			//to do so, we quickly check the document storage using standard Zip function, looking for the signature file
			File aZipFile;
			aZipFile = new File(Helpers.fromURLtoSystemPath(_xDocumentModel.getURL()));
			ZipFile aTheDocuZip = new ZipFile(aZipFile);
			
			if(aTheDocuZip != null) {
				//look for the right file element
				ZipEntry aSignaturesFileEntry = aTheDocuZip.getEntry(ConstantCustomIT.m_sSignatureStorageName+"/"+GlobConstant.m_sXADES_SIGNATURE_STREAM_NAME);
				if(aSignaturesFileEntry != null) {
					//2) if exists prepare a signed doc element accordingly
					InputStream	fTheSignaturesFile = aTheDocuZip.getInputStream(aSignaturesFileEntry);
					//a file with the signatures already present seems in place, parse it
					SAXSignedDocFactory aFactory = new SAXSignedDocFactory(m_xMCF, m_xCC, xDocumentStorage);
					sdoc = (ODFSignedDoc) aFactory.readSignedDoc(fTheSignaturesFile);
					
					//3) try to remove the signature selected,
					Signature sig = null;
					boolean bFoundTheSignatureID = false;
					for (int i = 0; i < sdoc.countSignatures(); i++) {
						sig = sdoc.getSignature(i);
						if(sig.getId().equalsIgnoreCase(_aSignState.getSignatureUUID())) {
							//found the signature
							//ask confirmation to remove it
							//remove only if given OK (YES)
							//prepare some information for the signature data display.
							MessageAskForSignatureRemoval	aMex = new MessageAskForSignatureRemoval(_xFrame,m_xMCF,m_xCC);
							//read the signature date
							String aSignDate = Helpers.date2string(sig.getSignedProperties().getSigningTime());
							//grab the certificate
							//create a new certificate UNO obj, to obtain the method to display the certificate signer
							String aSigner = _aSignState.getSigner();
							//retuned:
							// YES = 2
							// NO = 3
							short aret = aMex.executeDialogLocal(aSigner, aSignDate);
							m_aLogger.debug(__FUNCTION__+"returned: "+aret);
							//remove it
							sdoc.removeSignature(i);
							if(aret == 2) {
								bFoundTheSignatureID = true;
								bSignatureRemoved = true;
							}
							break;
						}
					}
					if(bFoundTheSignatureID) {
						//so, open the substorage META-INF from the main storage (e.g. the document)
						try {
							XStorage xMetaInfStorage = xDocumentStorage.openStorageElement(ConstantCustomIT.m_sSignatureStorageName,ElementModes.WRITE);
							if(sdoc.countSignatures() > 0) {
								//create the new file xadessignature.xml
								try {
									XStream xTheSignature = xMetaInfStorage.openStreamElement(ConstantCustomIT.m_sSignatureFileName, ElementModes.WRITE);
									XOutputStream xOutStream = xTheSignature.getOutputStream();
									//write signature file to the archive
									sdoc.writeSignaturesToXStream(xOutStream);
									xOutStream.flush();
									xOutStream.closeOutput();

									XTransactedObject xTransObj = (XTransactedObject) UnoRuntime.queryInterface(
											XTransactedObject.class, xMetaInfStorage);
									if (xTransObj != null) {
										m_aLogger.debug(__FUNCTION__+"XTransactedObject exists. Committing...");
										xTransObj.commit();
									}
	
									XComponent xStreamComp = (XComponent) UnoRuntime.queryInterface(XComponent.class,
											xTheSignature);
									if (xStreamComp == null)
										throw new com.sun.star.uno.RuntimeException();
									xStreamComp.dispose();
									xTransObj = (XTransactedObject) UnoRuntime.queryInterface(XTransactedObject.class,xDocumentStorage);
									if (xTransObj != null) {
										m_aLogger.debug(__FUNCTION__+"XTransactedObject(m_xDocumentStorage) exists. Committing...");
										xTransObj.commit();
									}
								} catch (InvalidStorageException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (IllegalArgumentException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (WrongPasswordException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (StorageWrappedTargetException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (com.sun.star.io.IOException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								}
							}
							else {
								try {
									XTransactedObject xTransObj = (XTransactedObject) UnoRuntime.queryInterface(XTransactedObject.class, xMetaInfStorage);
									if (xTransObj != null) {
										m_aLogger.debug(__FUNCTION__+"XTransactedObject exists. Committing...");
										xTransObj.commit();
									}
									xTransObj = (XTransactedObject) UnoRuntime.queryInterface(XTransactedObject.class,xDocumentStorage);
									if (xTransObj != null) {
										m_aLogger.debug(__FUNCTION__+"XTransactedObject(m_xDocumentStorage) exists. Committing... ");
										xTransObj.commit();
									}
								} catch (InvalidStorageException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (StorageWrappedTargetException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (com.sun.star.io.IOException e1) {
									m_aLogger.severe(__FUNCTION__, "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								}
//FIXME update the status and remove signatures								
							}
							xMetaInfStorage.dispose();
						} catch (Exception e1) {
							m_aLogger.severe("signAsFile", "\"" +  ConstantCustomIT.m_sSignatureStorageName + "\"" + " cannot open", e1);
						}
					}
				}
			}

			//get rid of the document storage: frees it and in the case of Windows the file is released as well
			//PLEASE NOTE: the following line of code has meaning ONLY
			//if the xDocumentStorage was created independently from the main document !
			//grab the needed XComponent interface
			((XComponent)UnoRuntime.queryInterface(XComponent.class, xStdoc)).dispose();			
				
//		} catch (CertificateException e) {
//			m_aLogger.log(e, true);
//		} catch (SignedDocException ex) {
//			m_aLogger.log(ex, true);
		} catch (com.sun.star.io.IOException e) {
			m_aLogger.log(e, true);
		} catch (Throwable ex) {
			m_aLogger.log(ex, true);
		}
		
		return bSignatureRemoved;
	}
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSigner#signDocument(com.sun.star.document.XStorageBasedDocument, com.yacme.ext.oxsit.security.cert.XOX_X509Certificate[])
	 * 
	 * gets called from dialog when a document should be signed with independent certificate signature
	 */
	@Override
	public boolean signDocument(XFrame xFrame, XModel xDocumentModel, XOX_X509Certificate[] _aCertArray, Object[] _oObjects)
			throws IllegalArgumentException, Exception {
		//init some localized error text

		m_xFrame = xFrame;
		m_aLogger.debug(this.getClass().getName() + "\n\t\tthe url of the document under signature is: " + xDocumentModel.getURL());
		LogJarVersion custom_itStart = new LogJarVersion(m_aLogger);

		m_aLogger.debug(custom_itStart.getVersion());

		//		//get the document storage,
		//		XStorageBasedDocument xDocStorage = (XStorageBasedDocument) UnoRuntime.queryInterface(XStorageBasedDocument.class,
		//				xDocumentModel);
		//
		//		m_xDocumentStorage = xDocStorage.getDocumentStorage();

		//		return signAsCMSFile(xFrame, xDocumentModel, _aCertArray);
		return signAsFile(xFrame, xDocumentModel, _aCertArray);

	}
	
	private void finalizePKCS11() throws Throwable {
		if( m_aHelperPkcs11 != null) {
			m_aHelperPkcs11.libFinalize();
			m_aHelperPkcs11 = null;
		}		
	}

	private boolean generateNewSignature(XFrame xFrame, XStorage xDocumentStorage, XOX_X509Certificate[] _aCertArray, ODFSignedDoc sdoc)
			throws CertificateException, Exception, SignedDocException {
		
		boolean retVal = false;
		//start 'real signing
		XOX_X509Certificate aCert = _aCertArray[0];
		X509Certificate certChild =  Helpers.getCertificate(aCert);
		m_aLogger.debug("cert label: " + aCert.getCertificateAttributes().getLabel());
//now check if an identical certificate is already present in some signature
		if(sdoc.countSignatures() != 0) {
			try {
				//form the sha1 sum of the new certificate
				MessageDigest digSigNew = MessageDigest.getInstance("SHA-256");
				byte[] certHashNew = digSigNew.digest(certChild.getEncoded());
				
				Signature sig = null;
				for (int i = 0; i < sdoc.countSignatures(); i++) {
					sig = sdoc.getSignature(i);
					X509Certificate aSigCert = sig.getKeyInfo().getSignersCertificate();
					
					MessageDigest digSig = MessageDigest.getInstance("SHA-256");
					byte[] certHashSig = digSig.digest(aSigCert.getEncoded());
					
					if(digSigNew.isEqual(certHashNew, certHashSig)) {
						//ask the user if the signature should be substituted
						MessageASimilarCertExists	aMex = new MessageASimilarCertExists(xFrame,m_xMCF,m_xCC);
						//read the signature date
						String aSignDate = Helpers.date2string(sig.getSignedProperties().getSigningTime());
						//retuned:
						// YES = 2
						// NO = 3
						// Cancel = 0
			            short aret = aMex.executeDialogLocal(aSignDate);

//			            m_aLogger.debug("returned: "+aret);
			            if(aret == 0) {
			            	//cancel, abort the signing process, all is left as is
			            	return retVal;
			            }
			            else if (aret == 2) {
			            	//yes, if yes, remove the one we are on and continue to search
			            	sdoc.removeSignature(i);
			            	i = -1; //so will began 0
			            	continue;
			            }
			            else if (aret == 3) {
							//if no, this signature will remain, a new will be added
			            	continue;
			            }
			            else {
			            	return  retVal;
			            }
					}
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// get the device this was seen on
		XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(XOX_SSCDevice.class, aCert.getSSCDevice());

		m_sPkcs11CryptoLib = xSSCD.getCryptoLibraryUsed();

		m_aLogger.debug("signDocument with: " + xSSCD.getDescription() + " cryptolib: " + m_sPkcs11CryptoLib);
		PKCS11TokenAttributes aTka = new PKCS11TokenAttributes(xSSCD.getManufacturer(), // from
				// device
				// description
				xSSCD.getDescription(), // from device description
				xSSCD.getTokenSerialNumber(), // from token
				xSSCD.getTokenMaximumPINLenght()); // from token

		Date dSigningDate = new Date();
		String sSigningDate = Helpers.date2string(dSigningDate);
		// try to get a pin from the user
		DialogQueryPIN aDialog1 = new DialogQueryPIN(xFrame, m_xCC, m_xMCF, aTka, sSigningDate);
		int BiasX = 100;
		int BiasY = 30;
		aDialog1.initialize(BiasX, BiasY);
		aDialog1.executeDialog();
		char[] myPin = aDialog1.getPin();
		String pin = aDialog1.getThePin();

		if (myPin != null && myPin.length > 0) {
			// user confirmed, check opening the session
			byte[] encDigestBytes = null;

			// add a Signature
			Date d1 = new Date();
			// add a Signature
			m_aLogger.debug("Prepare ODF signature");
			Signature sig = sdoc.prepareSignature(certChild, null, null, dSigningDate);
			byte[] sidigest = sig.calculateSignedInfoDigest();
			Date d2 = new Date();
			m_aLogger.debug("Preparing complete, time: " + ((d2.getTime() - d1.getTime()) / 1000) + " [sek]");
			byte[] sigval = null;
			//JDigiDoc
			//byte[] sigval = sigFac.sign(sidigest, 0, pin);
			// user confirmed, check opening the session
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				m_aLogger.debug("SecurityManager: " + sm);
			} else {
				m_aLogger.debug("no SecurityManager.");
			}
			try {
				m_sPkcs11WrapperLocal = Helpers.getPKCS11WrapperNativeLibraryPath(m_xCC);
				if (m_sHelperCerts == null)
					m_aHelperPkcs11 = new PKCS11Driver(m_aLogger, m_sPkcs11WrapperLocal, m_sPkcs11CryptoLib);
				if (isTokenPresent(xSSCD.getTokenLabel(), // from device description
						xSSCD.getTokenManufacturerID(), // from device description
						xSSCD.getTokenSerialNumber())) {
					//first get all supported mechanism (needed for logging, debug/tests
					m_aHelperPkcs11.getMechanismInfo(m_aHelperPkcs11.getTokenHandle());
					m_aHelperPkcs11.setMechanism(PKCS11Constants.CKM_RSA_PKCS);
					//it.infocamere.freesigner.gui.DigestSignTask.DigestSigner.encryptDigestAndGetCertificate(certHandle, helper);

					m_aHelperPkcs11.openSession(myPin);
					try {
						//now here start the true signature code, we sign the SHA1 sums we goto from
						//digesting process.
						long privateKeyHandle = m_aHelperPkcs11.findSignatureKeyFromID(aCert.getCertificateAttributes()
								.getID());
						//                    .findSignatureKeyFromCertificateHandle(m_aHelperPkcs11.getTokenHandle());
						m_aLogger.debug("privateKeyHandle: " + privateKeyHandle);
						if (privateKeyHandle > 0) {

							//ROB: commented out
							//////////// from JDigiDoc
							//SHA1 prefix ???
							/** SHA1 algorithm prefix */
							//final byte[] sha1AlgPrefix = { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a,
							//		0x05, 0x00, 0x04, 0x14 };

							//byte[] ddata = new byte[sha1AlgPrefix.length + sidigest.length];
							//System.arraycopy(sha1AlgPrefix, 0, ddata, 0, sha1AlgPrefix.length);
							//System.arraycopy(sidigest, 0, ddata, sha1AlgPrefix.length, sidigest.length);

							////////// end from JDigiDoc
							//ROB: end commented out

							//ROB: use encapsulateInDigestInfo instead
							byte[] ddata = encapsulateInDigestInfo(DIGEST_SHA256, sidigest);

							sigval = m_aHelperPkcs11.signDataSinglePart(privateKeyHandle, ddata);
							m_aLogger.debug("Finalize signature");
							sig.setSignatureValue(sigval);

							/// logging, only debug
							sdoc.writeSignaturesToXLogger(m_aLogger);

							////// end of debug only code
							//after this, add the file the signature just set.

							//and write it back to the storage

							//so, open the substorage META-INF from the main storage (e.g. the document)
							try {
								XStorage xMetaInfStorage = xDocumentStorage.openStorageElement(ConstantCustomIT.m_sSignatureStorageName,ElementModes.WRITE);

								//try to remove the previous signature
								//FIXME would be better to import the existent signatures and add the new one
								try {
									xMetaInfStorage.removeElement(ConstantCustomIT.m_sSignatureFileName);
								} catch (NoSuchElementException e1) {
									m_aLogger.debug("signAsFile", "\"" + ConstantCustomIT.m_sSignatureFileName + "\""
											+ " does not exist");
								}
								//create the file xadessignature.xml
								try {
									XStream xTheSignature = xMetaInfStorage.openStreamElement(ConstantCustomIT.m_sSignatureFileName, ElementModes.WRITE);
									//write to it (just a test now)

									byte[] theSignatureBytes = sig.toXML();

									XOutputStream xOutStream = xTheSignature.getOutputStream();

									sdoc.writeSignaturesToXStream(xOutStream);

									//							            xOutStream.writeBytes( theSignatureBytes );
									xOutStream.flush();
									xOutStream.closeOutput();

									XTransactedObject xTransObj = (XTransactedObject) UnoRuntime.queryInterface(
											XTransactedObject.class, xMetaInfStorage);
									if (xTransObj != null) {
										m_aLogger.debug("XTransactedObject exists. ===================");
										xTransObj.commit();
									}

									XComponent xStreamComp = (XComponent) UnoRuntime.queryInterface(XComponent.class,
											xTheSignature);
									if (xStreamComp == null)
										throw new com.sun.star.uno.RuntimeException();
									xStreamComp.dispose();

									//							            Utilities.showInterfaces(xDocStorage, xDocStorage);
									//							            Utilities.showInterfaces(m_xDocumentStorage, m_xDocumentStorage);
									//							            Utilities.showInterfaces(xMetaInfStorage, xMetaInfStorage);

									xTransObj = (XTransactedObject) UnoRuntime.queryInterface(XTransactedObject.class,
											xDocumentStorage);
									if (xTransObj != null) {
										m_aLogger.debug("XTransactedObject(m_xDocumentStorage) exists. ===================");
										xTransObj.commit();
									}

									//							            XCommonEmbedPersist xCommPer = UnoRuntime.queryInterface( XCommonEmbedPersist.class, m_xDocumentStorage );
									//							            if(xCommPer != null ) {
									//											m_aLogger.log("XCommonEmbedPersist exists. ===================");
									//											xCommPer.storeOwn();
									//							            }

								} catch (InvalidStorageException e1) {
									m_aLogger.severe("signAsFile", "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (IllegalArgumentException e1) {
									m_aLogger.severe("signAsFile", "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (WrongPasswordException e1) {
									m_aLogger.severe("signAsFile", "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (StorageWrappedTargetException e1) {
									m_aLogger.severe("signAsFile", "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								} catch (com.sun.star.io.IOException e1) {
									m_aLogger.severe("signAsFile", "\"" + ConstantCustomIT.m_sSignatureStorageName +"/" + ConstantCustomIT.m_sSignatureFileName
											+ "\"" + " error", e1);
								}

								xMetaInfStorage.dispose();
							} catch (Exception e1) {
								m_aLogger.severe("signAsFile", "\"" +  ConstantCustomIT.m_sSignatureStorageName + "\"" + " cannot open", e1);
							}
						}
						//at list one certificate was signed
						//					bRetValue = true;
						//					bRetry = false;
					} catch (Throwable e) {
						//any exception thrown during signing process comes here
						//close the pending session
						m_aLogger.debug("Throwable thrown! Closing session.");
						finalizePKCS11();
						throw (e);
					}
					m_aHelperPkcs11.closeSession();
					m_aLogger.debug("Closing session, all ok.");
					finalizePKCS11();
					retVal = true;
				} else {
					//0x000000E0 = CKR_TOKEN_NOT_PRESENT
					//see iaik/pkcs/pkcs11/wrapper/ExceptionMessages.properties
					finalizePKCS11();
					throw (new PKCS11Exception(0x000000E0));
				}
			} catch (TokenException e) {
				// session can not be opened
				//FIXME: change behavior if exception is: iaik.pkcs.pkcs11.wrapper.PKCS11Exception: CKR_PIN_INCORRECT
				/*
				 * 
				S 17:56:00.390 752D7D02 com.yacme.ext.oxsit.cust_it.comp.security.DocumentSigner_IT  >TokenException 
				iaik.pkcs.pkcs11.wrapper.PKCS11Exception: CKR_PIN_INCORRECT                      
				iaik.pkcs.pkcs11.wrapper.PKCS11Implementation.C_Login(Native Method) 
				com.yacme.ext.oxsit.pkcs11.PKCS11Driver.login(PKCS11Driver.java:728) 
				com.yacme.ext.oxsit.pkcs11.PKCS11Driver.openSession(PKCS11Driver.java:753) 
				com.yacme.ext.oxsit.cust_it.comp.security.DocumentSigner_IT.signAsFile(DocumentSigner_IT.java:431) 
				com.yacme.ext.oxsit.cust_it.comp.security.DocumentSigner_IT.signDocument(DocumentSigner_IT.java:288) 
				com.yacme.ext.oxsit.ooo.ui.DialogCertTreeSSCDs.addButtonPressed(DialogCertTreeSSCDs.java:187) 
				com.yacme.ext.oxsit.ooo.ui.DialogCertTreeBase.actionPerformed(DialogCertTreeBase.java:915) 
				com.sun.star.bridges.jni_uno.JNI_proxy.dispatch_call(Native Method) 
				com.sun.star.bridges.jni_uno.JNI_proxy.invoke(JNI_proxy.java:175) 
				$Proxy61.execute(Unknown Source) 
				com.yacme.ext.oxsit.ooo.ui.BasicDialog.executeDialog(BasicDialog.java:641) 
				com.yacme.ext.oxsit.ooo.ui.DialogCertTreeSSCDs.executeDialog(DialogCertTreeSSCDs.java:146) 
				com.yacme.ext.oxsit.ooo.ui.DialogSignatureTreeDocument.addButtonPressed(DialogSignatureTreeDocument.java:164) 
				com.yacme.ext.oxsit.ooo.ui.DialogCertTreeBase.actionPerformed(DialogCertTreeBase.java:915) 
				com.sun.star.bridges.jni_uno.JNI_proxy.dispatch_call(Native Method) 
				com.sun.star.bridges.jni_uno.JNI_proxy.invoke(JNI_proxy.java:175) 
				$Proxy61.execute(Unknown Source) 
				com.yacme.ext.oxsit.ooo.ui.BasicDialog.executeDialog(BasicDialog.java:641) 
				com.yacme.ext.oxsit.ooo.ui.DialogSignatureTreeDocument.executeDialog(DialogSignatureTreeDocument.java:135) 
				com.yacme.ext.oxsit.signature.dispatchers.ImplXAdESSignatureDispatchTB.signatureDialog(ImplXAdESSignatureDispatchTB.java:359) 
				com.yacme.ext.oxsit.signature.dispatchers.ImplXAdESSignatureDispatchTB.impl_dispatch(ImplXAdESSignatureDispatchTB.java:293) 
				com.yacme.ext.oxsit.dispatchers.threads.OnewayDispatchExecutor.run(OnewayDispatchExecutor.java:63)
				 *
				 */
				m_aLogger.warning("", ">TokenException", e);
				//FIXME ??				throw(e);
			} catch (Throwable e) {
				// session can not be opened
				m_aLogger.severe(e);
			}

			//            // get confirmation
			//            //ROB: Confirmation required by Estonian Law
			//            //System.out.println("Get confirmation");
			//            //sig.getConfirmation();
			//            //System.out.println("Confirmation OK!");
			//            
			//            //System.out.println("Signature: " + sig);
			//             
			//            // write it in a file
			//            System.out.println("Writing in file: " + xmloutputfile);
			//            sdoc.writeToFile(new File(xmloutputfile));
			//            
			//            //ROB: Generazione ODF di uscita
			//            // Generacion del ODF de salida
			//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//            ZipOutputStream zos = new ZipOutputStream(baos);
			//
			//            // Copiar el contenido existente al ODF de salida
			//            for (String fileName : odf.getFileList())
			//            {
			//                ZipEntry zeOut = new ZipEntry(fileName);
			//
			//                if (!fileName.equals("META-INF/xadessignatures.xml")
			//                        && !fileName.equals("META-INF/manifest.xml"))
			//                {
			//                    zos.putNextEntry(zeOut);
			//                    zos.write(odf.getEntry(fileName));
			//                }
			//            }
			//
			//            // Añadimos el documento de firmas al ODF de salida
			//            ZipEntry zeDocumentSignatures = new ZipEntry("META-INF/xadessignatures.xml");
			//            zos.putNextEntry(zeDocumentSignatures);
			//            ByteArrayOutputStream baosXML = new ByteArrayOutputStream();
			//            sdoc.writeToStream(baosXML);
			//            zos.write(baosXML.toByteArray());
			//            zos.closeEntry();
			//
			//            // Añadimos el manifest.xml al ODF de salida
			//            ZipEntry zeManifest = new ZipEntry("META-INF/manifest.xml");
			//            zos.putNextEntry(zeManifest);
			//            zos.write(manifestBytes);
			//            zos.closeEntry();
			//
			//            zos.close();
			//            
			//            new FileOutputStream(odfoutputfile).write(baos.toByteArray());
			//            
			//			d2 = new Date();
			//			System.out.println("Composing complete, time: " + ((d2.getTime() - d1.getTime()) / 1000) + " [sek]" );
			//			
			//			//System.out.println("Done!");
			//             
			//            
			//
		}
		return retVal;
	}

	/**
	 * The procedure should be the following:
	 * 
	 * form a digest for any of the document substorage (files) the document has
	 * according to the decided standard 
	 * 
	 * when the digests are done, iterate through the certificate list to be used to sign:
	 * for every certificate
	 *     check to see if the token where the certificate is contained is 'on-line'
	 *     the check is performed using data that where retrieve when looking
	 *     for available certificates
	 *       if not, alert the user:
	 *         - user 'next' go to next certificate
	 *     	   - user 'cancel' abort the sign process
	 *     	   - user 'retry' check again the token  
	 * 		token is ready, ask the user for a PIN code to access the private key
	 * 		the dialog shows token ids (description, model, serial number):
	 * 		the dialog expect the right number of characters for PIN, that should come
	 * 		from the token data, even though the right number of characters depends on 
	 * 		the token supplier (e.g. the one that initialized it).
	 * 			- user abort, go to next certificate 
	 * 			- user confirm, then proceed
	 * 
	 * 		open a login session to the token using the provided PIN
	 * 		if something goes wrong, alert the user:
	 * 			- user retry, goto the PIN input step
	 * 			- user abort, go to the next certificate
	 * 		all is ok, retrieve the private key id using the certificate data that came
	 * 		from the available certificate search,
	 * 		for every hash computed:
	 * 			sign the hash, get the signed has and attach it to the document substorage URL
	 * 
	 * 		goto next certificate
	 * 
	 * @param xFrame
	 * @param _xDocumentModel
	 * @param aCertArray
	 * @return
	 */
	private boolean signAsFile(XFrame xFrame, XModel _xDocumentModel, XOX_X509Certificate[] _aCertArray) {
		ODFSignedDoc sdoc = null;
		boolean retVal = false;

		ConfigManager.init("jar://ODFDocSigning.cfg");

		try {
			XStorage xDocumentStorage;

//		boolean openWithURL = true;
//		if(openWithURL) {
			//now, using the only method available, open the storage
//			URL aURL = new URL(_xDocumentModel.getURL());
//			
//			URI aURI = aURL.toURI();
//
//			m_aLogger.debug(_xDocumentModel.getURL());
//			m_aLogger.debug(" aURL: "+aURL.toString());
//			m_aLogger.debug(" aURL.getFile() "+aURL.getFile());
//			m_aLogger.debug(" aURI.getPath() "+aURI.getPath());
//			m_aLogger.debug(" aURL.getHost() "+aURL.getHost());
//			m_aLogger.debug(" aURI: "+aURL.toURI().toString());

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
			aArguments[1] = ElementModes.READWRITE;
//			aArguments[1] = ElementModes.READ;
			//get the document storage object 
			//get the document storage object 
			Object xStdoc = xStorageFact.createInstanceWithArguments(aArguments);

			//this is for debug purposes only, not really needed here
			//XStorageBasedDocument xDocStorage = (XStorageBasedDocument) UnoRuntime.queryInterface(XStorageBasedDocument.class, xDocumentModel);

			//from the storage object (or better named, the service) obtain the interface we need
			//will be needed to read the document the same way as OOo does
			xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);

			//chek if we have a signature already in place
			//to do so, we quickly check the document storage using standard Zip function, looking for the signature file
			File aZipFile;
			aZipFile = new File(Helpers.fromURLtoSystemPath(_xDocumentModel.getURL()));
			ZipFile aTheDocuZip = new ZipFile(aZipFile);

			if(aTheDocuZip != null) {
				//look for the right file element
				ZipEntry aSignaturesFileEntry = aTheDocuZip.getEntry(ConstantCustomIT.m_sSignatureStorageName+"/"+GlobConstant.m_sXADES_SIGNATURE_STREAM_NAME);
				if(aSignaturesFileEntry != null) {
					InputStream	fTheSignaturesFile = aTheDocuZip.getInputStream(aSignaturesFileEntry);
					//a file with the signatures already present seems in place, parse it
					SAXSignedDocFactory aFactory = new SAXSignedDocFactory(m_xMCF, m_xCC, xDocumentStorage);
					sdoc = (ODFSignedDoc) aFactory.readSignedDoc(fTheSignaturesFile);
				}
				else {
				//no signature present, so create a new SignedDoc 
					sdoc = new ODFSignedDoc(m_xMCF, m_xCC, xDocumentStorage, ODFSignedDoc.FORMAT_ODF_XADES, ODFSignedDoc.VERSION_1_3);
	
				// and then read the data from the document and prepare to sign
					sdoc.addODFData();
				}
			//do the 'real' signing stuff
				retVal = generateNewSignature(m_xFrame, xDocumentStorage, _aCertArray, sdoc);
			}
			//drop out the else if something was seriously wrong and this should NEVER happen
			//FIXME: how to alert the user ?

			//get rid of the document storage: frees it and in the case of Windows the file is released as well
			//PLEASE NOTE: the following line of code has meaning ONLY
			//if the xDocumentStorage was created independently from the main document !
			//grab the needed XComponent interface
			((XComponent)UnoRuntime.queryInterface(XComponent.class, xStdoc)).dispose();			

		} catch (CertificateException e) {
			m_aLogger.log(e, true);
		} catch (SignedDocException ex) {
			m_aLogger.log(ex, true);
		} catch (com.sun.star.io.IOException e) {
			m_aLogger.log(e, true);
		} catch (Throwable ex) {
			m_aLogger.log(ex, true);
		}
		return retVal;
	}

	private boolean signAsCMSFile(XFrame xFrame, XModel _documentModel, XOX_X509Certificate[] _aCertArray)
			throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		// for the time being only the first certificate is used
		/*
		 * The procedure should be the following:
		 * 
		 * form a digest for any of the document substorage (files) the document has according to the decided standard
		 */

		boolean bRetValue = false;
		boolean bCanCloseCertificateChooser = false;
		// A dummy digest:  sha1 hash of the word "ciao", encapsulated in a digestInfo asn.1 structure.
		byte[] baSha1 = { (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09, (byte) 0x06, (byte) 0x05, (byte) 0x2B, (byte) 0x0E,
				(byte) 0x03, (byte) 0x02, (byte) 0x1A, (byte) 0x05, (byte) 0x00, (byte) 0x04, (byte) 0x14, (byte) 0xD7,
				(byte) 0x54, (byte) 0xDA, (byte) 0xA6, (byte) 0xD2, (byte) 0xBA, (byte) 0xA7, (byte) 0x4F, (byte) 0x7B,
				(byte) 0x14, (byte) 0x75, (byte) 0xD0, (byte) 0x82, (byte) 0xA4, (byte) 0x6E, (byte) 0x89, (byte) 0x27,
				(byte) 0x91, (byte) 0xC6, (byte) 0x12 };
		// try to sign something simple
		/*
		 * String sTest = "Y6pN0POPYtz3b/IJp1sBTnjy8TE="
		 */

		/*
		 * 
		 * when the digests are done, iterate through the certificate list to be used to sign: for every certificate check to see if
		 * the token where the certificate is contained is 'on-line' the check is performed using data that where retrieve when
		 * looking for available certificates
		 */
		for (int certIDX = 0; certIDX < _aCertArray.length; certIDX++) {
			/*
			 * for every certificate check to see if the token where the certificate is contained is 'on-line' the check is
			 * performed using data that where retrieve when looking for available certificates
			 */

			XOX_X509Certificate aCert = _aCertArray[certIDX];

			m_aLogger.debug("cert label: " + aCert.getCertificateAttributes().getLabel());

			// get the device this was seen on
			XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(XOX_SSCDevice.class, aCert.getSSCDevice());

			m_sPkcs11CryptoLib = xSSCD.getCryptoLibraryUsed();

			m_aLogger.debug("signDocument with: " + xSSCD.getDescription() + " cryptolib: " + m_sPkcs11CryptoLib);
			PKCS11TokenAttributes aTka = new PKCS11TokenAttributes(xSSCD.getManufacturer(), // from
					// device
					// description
					xSSCD.getDescription(), // from device description
					xSSCD.getTokenSerialNumber(), // from token
					xSSCD.getTokenMaximumPINLenght()); // from token

			try {
				SecurityManager sm = System.getSecurityManager();
				if (sm != null) {
					m_aLogger.debug("SecurityManager: " + sm);
				} else {
					m_aLogger.debug("no SecurityManager.");
				}
				{
					m_sPkcs11WrapperLocal = Helpers.getPKCS11WrapperNativeLibraryPath(m_xCC);
					if (m_sHelperCerts == null)
						m_aHelperPkcs11 = new PKCS11Driver(m_aLogger, m_sPkcs11WrapperLocal, m_sPkcs11CryptoLib);
					boolean bRetry = true;
					while (bRetry) {
						try {
							if (isTokenPresent(xSSCD.getTokenLabel(), // from device description
									xSSCD.getTokenManufacturerID(), // from
									// device
									// description
									xSSCD.getTokenSerialNumber())) {
								/*
								 * the token is present and initialized in, go on with job check again the token token is ready, ask
								 * the user for a PIN code to access the private key
								 * the dialog shows token ids (description, model,
								 * serial number):
								 * the dialog expect the right number of characters for PIN, that should come from
								 * the token data, even though the right number of characters depends on the token supplier (e.g.
								 * the one that initialized it).
								 * - user abort, go to next certificate
								 * - user confirm, then proceed
								 */
								// try to get a pin from the user
								
								Date ndSigningDate = new Date();
								String sSigningDate = Helpers.date2string(ndSigningDate);

								DialogQueryPIN aDialog1 = new DialogQueryPIN(xFrame, m_xCC, m_xMCF, aTka,sSigningDate);
								int BiasX = 100;
								int BiasY = 30;
								aDialog1.initialize(BiasX, BiasY);
								aDialog1.executeDialog();
								char[] myPin = aDialog1.getPin();
								if (myPin != null && myPin.length > 0) {
									// user confirmed, check opening the session
									byte[] encDigestBytes = null;
									m_aLogger.debug("actual signing process started.");
									try {
										//first get all supported mechanism (needed for logging, debug/tests
										m_aHelperPkcs11.getMechanismInfo(m_aHelperPkcs11.getTokenHandle());

										m_aHelperPkcs11.setMechanism(PKCS11Constants.CKM_RSA_PKCS);
										//it.infocamere.freesigner.gui.DigestSignTask.DigestSigner.encryptDigestAndGetCertificate(certHandle, helper);

										m_aHelperPkcs11.openSession(myPin);
										//										m_aHelperPkcs11.openSession();
										try {
											//now here start the true signature code, we sign the SHA1 sums we goto from
											//digesting process.
											long privateKeyHandle = m_aHelperPkcs11.findSignatureKeyFromID(aCert
													.getCertificateAttributes().getID());
											//			                                .findSignatureKeyFromCertificateHandle(m_aHelperPkcs11.getTokenHandle());
											m_aLogger.debug("privateKeyHandle: " + privateKeyHandle);
											if (privateKeyHandle > 0) {
												encDigestBytes = m_aHelperPkcs11.signDataSinglePart(privateKeyHandle, baSha1);
											}
											//at list one certificate was signed
											bRetValue = true;
											bRetry = false;
										} catch (Throwable e) {
											//any exception thrown during signing process comes here
											//close the pending session
											m_aLogger.debug("Throwable thrown! Closing session.");
											m_aHelperPkcs11.closeSession();
											finalizePKCS11();
											throw (e);
										}
										m_aHelperPkcs11.closeSession();
										finalizePKCS11();

									} catch (TokenException e) {
										// session can not be opened
										m_aLogger.warning("", "TokenException", e);
										throw (e);
									} catch (Throwable e) {
										// session can not be opened
										m_aLogger.severe(e);
										bRetry = false;
										continue;
									}
								} else {
									// no pin or cancel
									//so go to next certificate
									bRetry = false;
									continue;
								}
								bRetry = false;
							} else {
								//0x000000E0 = CKR_TOKEN_NOT_PRESENT
								//see iaik/pkcs/pkcs11/wrapper/ExceptionMessages.properties
								throw (new PKCS11Exception(0x000000E0));
							}
						} catch (TokenException e) {
							/*
							 * if not, alert the user:
							 * - user 'Ok' go to next certificate
							 * - user 'cancel' abort the sign process, return to the
							 *   certificate chooser
							 * - user 'retry' go back and retry
							 */
							m_aLogger.warning("", "!TokenException", e);
							MessageNoSignatureToken aDlg = new MessageNoSignatureToken(m_xFrame, m_xMCF, m_xCC);
							String aMex = e.getMessage().trim();
							short ret = aDlg.executeDialogLocal(aTka.getLabel(), aTka.getModel(), aTka.getSerialNumber(),
									(aMex == null) ? "<no message>" : aMex);
							switch (ret) {
							//Retry (Riprova) = 4 =-> retry same certificate
							default:
							case 4:
								continue;

								//Ignore (Ignora) = 5 =-> next certificate
							case 5:
								bRetry = false;
								break;
							//Abort (Interrompi) = 0 =-> back to certificate selection
							case 0:
								if (m_aHelperPkcs11 != null) {
									m_aHelperPkcs11.libFinalize();
									m_aHelperPkcs11 = null;
								}
								return false;
							}
							// in case of cancel
							// return false;
							// this should be adapted
						} catch (Throwable e) {
							m_aLogger.warning("", ">Throwable", e);
							bRetry = false;
						}
					}
					finalizePKCS11();
				}
			} catch (IOException e) {
				m_aLogger.severe(e);
			} catch (TokenException e) {
				m_aLogger.severe(e);
			} catch (NullPointerException e) {
				m_aLogger.severe(e);
			} catch (URISyntaxException e) {
				m_aLogger.severe(e);
			} catch (Throwable e) {
				m_aLogger.severe(e);
			}
		} // next certificate

		/*
		 * check again the token token is ready, ask the user for a PIN code to access the private key the dialog shows token ids
		 * (description, model, serial number): the dialog expect the right number of characters for PIN, that should come from the
		 * token data, even though the right number of characters depends on the token supplier (e.g. the one that initialized it).
		 * - user abort, go to next certificate - user confirm, then proceed
		 * 
		 * open a login session to the token using the provided PIN if something goes wrong, alert the user: - user retry, goto the
		 * PIN input step - user abort, go to the next certificate all is ok, retrieve the private key id using the certificate data
		 * that came from the available certificate search, for every hash computed: sign the hash, get the signed has and attach it
		 * to the document substorage URL
		 * 
		 * goto next certificate
		 */
		// just for test, analyze the document package structure
		return bRetValue;
	}

	/**
	 * @param manufacturer
	 * @param description
	 * @param tokenSerialNumber
	 * @return
	 * @throws TokenException 
	 * @throws IOException 
	 */
	private boolean isTokenPresent(String _sTokenLabel, String _sTokenManufID, String _sTokenSerialNumber) throws IOException,
			TokenException {
		//the same as certificate search, examine the m_nTokens present for a correct information
		long[] tokens = null;
		try {
			tokens = m_aHelperPkcs11.getTokens();
			//grab all the m_nTokens
			for (int i = 0; i < tokens.length; i++) {
				//select a token and look for the indication requested
				CK_TOKEN_INFO aTkInfo = m_aHelperPkcs11.getTokenInfo(tokens[i]);
				String sString = new String(aTkInfo.label);
				String aLabel = sString.trim();
				sString = new String(aTkInfo.manufacturerID);
				String aManID = sString.trim();
				sString = new String(aTkInfo.serialNumber);
				String aSerial = sString.trim();

				if (aLabel.equals(_sTokenLabel) && aManID.equals(_sTokenManufID) && aSerial.equals(_sTokenSerialNumber)) {
					//token found, set it to work, return true
					m_aHelperPkcs11.setTokenHandle(tokens[i]);
					return true;
				}
			}
		} catch (PKCS11Exception e) {
			m_aLogger.warning("", "", e);
			throw (e);
		}
		return false;
	}

	/** Verify the text document only
	 * 
	 * @param _xFrame
	 * @param _xDocumentModel
	 * @return
	 */
	private boolean verifyTextDocumentBeforeSigning(XFrame _xFrame, XModel _xDocumentModel) {
		final String __FUNCTION__ = "verifyTextDocumentBeforeSigning: ";
		//check the filtername
		
		PropertyValue[] aPVal = _xDocumentModel.getArgs();
		/////////////// for debug only
		for (int i = 0; i < aPVal.length; i++) {
			PropertyValue aVal = aPVal[i];
			m_aLogger.debug(__FUNCTION__+Utilities.showPropertyValue(aVal));
		}

//DEBUG		Utilities.showInterfaces(_xDocumentModel, _xDocumentModel);

		///////////////////////////
		if (aPVal == null || aPVal.length == 0) {
			m_aLogger.warning("verifyDocumentBeforeSigning", "no opened document task properties, cannot sign");
			return false;
		}
		boolean bFilterOK = false;
		for (int i = 0; i < aPVal.length; i++) {
			PropertyValue aVal = aPVal[i];
			if (aVal.Name.equalsIgnoreCase("FilterName")) {
				String sDocumentFilter;
				try {
					sDocumentFilter = AnyConverter.toString(aVal.Value);
					//the filters can be:
					//writer8, draw8, impress8
					if (sDocumentFilter.equalsIgnoreCase("writer8")) {
						bFilterOK = true;
						break;
					}
				} catch (IllegalArgumentException e) {
					m_aLogger.severe(e);
				}
			}
		}

		if (!bFilterOK) {
			m_aLogger.warning("verifyDocumentBeforeSigning", "Only native Open Document Format for Writer can be signed.");
			//detect the document main type (Writer, Calc, Impress, etc...
			//and present a dialog explaining the reason why this can 't be signed
			MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
			aMex.executeDialogLocal(new String(String.format(m_sErrorNoDocumentType, "Writer")));
			return false;
		}
		m_aLogger.debug("document type ok !");

		//verify if the document has externally linked objects:
		//It cannot have any
		//first the images, this is for writer document only
		XTextGraphicObjectsSupplier xGrf = (XTextGraphicObjectsSupplier) UnoRuntime.queryInterface(
				XTextGraphicObjectsSupplier.class, _xDocumentModel);
		if (xGrf != null) {
			XNameAccess xNames = xGrf.getGraphicObjects();
			if (xNames != null) {
				//check all the names if they are linked rather then embedded
				String[] sAllNames = xNames.getElementNames();
				for (int i = 0; i < sAllNames.length; i++) {
					Object aObj;
					try {
						aObj = xNames.getByName(sAllNames[i]);
						//AnyConverter.getType(aObj).getTypeName();
						XTextContent xTc = (XTextContent) AnyConverter.toObject(XTextContent.class, aObj);
						XPropertySet xPset = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTc);
						XPropertySetInfo xPsi = xPset.getPropertySetInfo();
						if (xPsi.hasPropertyByName("GraphicURL")) {
							//got the right property, check if linked
							String sTheUrl = AnyConverter.toString(xPset.getPropertyValue("GraphicURL"));
							if (!sTheUrl.startsWith("vnd.sun.star.GraphicObject:")) {
								//that means it's not embedded, so, tell the user
								MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
								aMex.executeDialogLocal(m_sErrorGraphicNotEmbedded);
								return false;
							}
						}
					} catch (NoSuchElementException e1) {
						m_aLogger.severe("while looking for GraphicURL property: ", e1);
					} catch (WrappedTargetException e1) {
						m_aLogger.severe("while looking for GraphicURL property: ", e1);
					} catch (Throwable e1) {
						m_aLogger.severe("while looking for GraphicURL property: ", e1);
					}
					//				m_aLogger.log("graph: "+sAllNames[i]+" =-> "+AnyConverter.getType(aObj).getTypeName());				
				}
			}
		} else {
			m_aLogger.severe("", "Not found a needed service!");
			return false;
		}


		//then the embedded object and/or linked object, should be all embedded, and if embedded
		//only the right type is allowed

		//no embedded objects Calc, Writer, Math or Draw
		//at this time
		XTextEmbeddedObjectsSupplier xTxEmb = (XTextEmbeddedObjectsSupplier) UnoRuntime.queryInterface(
				XTextEmbeddedObjectsSupplier.class, _xDocumentModel);
		if (xTxEmb != null) {
			XNameAccess xNames = xTxEmb.getEmbeddedObjects();
			if (xNames != null) {
				//one or more embedded object is present
				//not allowed at this time !
				//but we count the object present and their kind:
				int	nDrawObject = 0;
				int	nCalcObject = 0;
				int nImpressObject = 0;
				int	nMathObject = 0;
				int nLinkedObject = 0;

//this part if for the time being commented out.
				//in a successive release the embedded objects can be inserted, provided they don't have any macro and can then be
				//signed

				String[] sAllNames = xNames.getElementNames();
				for (int i = 0; i < sAllNames.length; i++) {
					Object aObj;
					try {
						aObj = xNames.getByName(sAllNames[i]);
						//AnyConverter.getType(aObj).getTypeName();

						XTextContent xTc = (XTextContent) AnyConverter.toObject(XTextContent.class, aObj);
						XPropertySet xPset = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTc);
						XPropertySetInfo xPsi = xPset.getPropertySetInfo();
						
//DEBUG	m_aLogger.debug(__FUNCTION__+Utilities.showPropertiesString(this, xPset));
						xPsi.getProperties();
//display name for debug purposes only
						
/////////// DEBUG						
//						if(xPsi.hasPropertyByName("LinkDisplayName")) {
//							String sTheName = AnyConverter.toString(xPset.getPropertyValue("LinkDisplayName"));
//							System.out.println(__FUNCTION__+"object name: "+sTheName);
//							m_aLogger.debug(__FUNCTION__+"object name: "+sTheName);
//						}
//						m_aLogger.debug(__FUNCTION__+Utilities.showPropertiesString(this, xPset));
//						
/////////// END DEBUG						

						//check if this object is linked somehow
						if (xPsi.hasPropertyByName("GraphicURL")) {
							//Linked embedded object are not allowed
//disabled, gives false results							nLinkedObject++;
//							continue;
						}
						
						if (xPsi.hasPropertyByName("Model")) {
							//got the right property, check if Model match
							XModel	xAmodel = (XModel) AnyConverter.toObject(XModel.class, xPset.getPropertyValue("Model"));

							//check for Writer non done, since in a TextDocument you can't embed another writer document.
							//it can can be linked though, so see above for links
							
							//check for Calc
							// com.sun.star.sheet.XSpreadsheetDocument
							XSpreadsheetDocument xaSheet = (XSpreadsheetDocument)UnoRuntime.queryInterface(XSpreadsheetDocument.class, xAmodel);
							if(xaSheet != null) {
								//this is a Calc object
								nCalcObject++;
								continue;
							}
							//check for Draw
							//com.sun.star.drawing.XDrawPagesSupplier
							XDrawPagesSupplier xaDraw = (XDrawPagesSupplier)UnoRuntime.queryInterface(XDrawPagesSupplier.class, xAmodel);
							if(xaDraw != null) {
								//if we have
								//com.sun.star.presentation.XPresentationSupplier
								//is Impress
								XPresentationSupplier xaPresentation = (XPresentationSupplier)UnoRuntime.queryInterface(XPresentationSupplier.class, xAmodel);
								if(xaPresentation != null) {
									nImpressObject++;
									continue;
								}
								else {
									nDrawObject++;
									continue;
								}
							}

							//is something else -> Math ?
							nMathObject++;

/////////////DEBUG
//							Utilities.showInterfaces(this, xAmodel);
///////// END DEBUG
						}
					} catch (NoSuchElementException e1) {
						m_aLogger.severe(__FUNCTION__+"while looking for GraphicURL and Model property: ", e1);
					} catch (WrappedTargetException e1) {
						m_aLogger.severe(__FUNCTION__+"while looking for GraphicURL and Model property: ", e1);
					} catch (Throwable e1) {
						m_aLogger.severe(__FUNCTION__+"while looking for GraphicURL and Model property: ", e1);
					}
				}
				
				m_aLogger.debug(__FUNCTION__, "found: "+
							nDrawObject+" draw objs, "+
							nCalcObject+" calc objs, "+
							nMathObject+" math objs, "+
							nImpressObject+" impress obj, "+
							nLinkedObject+" linked objs.");
				
				if( nDrawObject != 0 ||
						nCalcObject != 0 ||
						nMathObject != 0 ||
						nImpressObject != 0 ||
						nLinkedObject != 0 ) {

					//there is one or more object not allowed
					MessageEmbededObjsPresentInTextDocument aMex = new MessageEmbededObjsPresentInTextDocument(_xFrame, m_xMCF, m_xCC);

					//mex: Ci sono  oggetti incorporati:
					//     %d oggetti Draw
					//     %d oggetti Impress
					//     %d oggetti Calc
					//     %d oggetti Math
					//     %d oggetti Collegati esternamente
					//     Gli oggetti incorporati non sono ammessi!
					//	   Non potete firmare questo documento.
					// nome messaggio id_MexEmbedObjsInText (o simile)
					aMex.executeDialogLocal(nDrawObject, nImpressObject, nCalcObject, nMathObject, nLinkedObject);
					return false;
				}
			}
		} else {
			m_aLogger.severe("", "Not found a needed service!");
			return false;
		}
		
		

		//then the forms, for now no forms are allowed in the text document.

		return true;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSigner#verifyDocumentBeforeSigning(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, java.lang.Object[])
	 */
	@Override
	public boolean verifyDocumentBeforeSigning(XFrame _xFrame, XModel _xDocumentModel, Object[] oObjects)
			throws IllegalArgumentException, Exception {

		//check if the document is modified, e.g. not yet saved
		//it must be saved
		XStorable xStore = (XStorable) UnoRuntime.queryInterface(XStorable.class, _xDocumentModel);
		XStorage xDocumentStorage;
		
		// decide if new or already saved
		XModifiable xMod = (XModifiable) UnoRuntime.queryInterface(XModifiable.class, _xDocumentModel);
		if ((xMod != null && xMod.isModified()) || (xStore != null && !xStore.hasLocation())) {
			MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
			aMex.executeDialogLocal(m_sErrorNotYetSaved);
			return false;
		}

		//check if the document is readonly: to sign it MUST be readonly!
		if (xStore == null  || !xStore.isReadonly()  ){
			MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
			aMex.executeDialogLocal(m_sErroreIsNotReadOnly);
			return false;
		}
		
		//check the main document types interfaces
		XTextDocument xText = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, _xDocumentModel);
		if (xText != null) {
			if (!verifyTextDocumentBeforeSigning(_xFrame, _xDocumentModel)) {
				return false;
			}
		} else {
//			Utilities.showInterfaces(_xDocumentModel, _xDocumentModel);
			m_aLogger.warning("verifyDocumentBeforeSigning", "Only native Open Document Format for Writer can be signed.");
			//present a dialog explaining the reason why this can 't be signed
			MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
			aMex.executeDialogLocal(new String(String.format(m_sErrorNoDocumentType, "Writer")));
			return false;
		}

		//find the storage, and see if the storage contains macros
		//get the document storage,
		XStorageBasedDocument xDocStorage = (XStorageBasedDocument) UnoRuntime.queryInterface(XStorageBasedDocument.class,
				_xDocumentModel);

		//		Utilities.showInterfaces(xDocStorage, _xDocumentModel);
		XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xDocStorage);
		//		Utilities.showProperties(xDocStorage, xPropSet);
		xDocumentStorage = xDocStorage.getDocumentStorage();

		//		Utilities.showInterfaces(xDocStorage, m_xDocumentStorage);
		xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xDocumentStorage);
		//		Utilities.showProperties(m_xDocumentStorage, xPropSet);

		if (xPropSet != null) { // grab the version
			String sVersion = "1.0";
			try {
				sVersion = (String) xPropSet.getPropertyValue("Version");
			} catch (UnknownPropertyException e) {
				m_aLogger.warning("makeTheElementList", "Version missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_aLogger.warning("makeTheElementList", "Version missing", e);
			}
			if (sVersion.length() > 0) {
				m_aLogger.debug("Version is: " + sVersion); // this should be 1.2 or more
				if (sVersion.equalsIgnoreCase("1.2"))
					m_nTypeOfDocumentToBeSigned = IS_ODF12;
			} else {
				m_aLogger.debug("Version is 1.0 or 1.1");
				m_nTypeOfDocumentToBeSigned = IS_ODF10_OR_11;
			}
			String sMediaType = "";
			try {
				sMediaType = (String) xPropSet.getPropertyValue("MediaType");
				m_aLogger.debug("main storage media type: " + sMediaType);
			} catch (UnknownPropertyException e) {
				m_aLogger.warning("makeTheElementList", "Mediatype missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_aLogger.warning("makeTheElementList", "Mediatype missing", e);
			}
		} else
			m_aLogger.debug("Version does not exists! May be this is not a ODF package?");

		//verify if there is a Basic substorage holding the basic script
		String[] aElements = xDocumentStorage.getElementNames();
		String sBasicElement = "Basic";
		for (int i = 0; i < aElements.length; i++) {
//DEBUG			m_aLogger.log(aElements[i]);
			if (aElements[i].equals(sBasicElement)) {
				m_aLogger.warning("verifyDocumentBeforeSigning",
						"This document contains OpenOffice.org macro. It cannot be signed.");
				//present a dialog explaining the reason why this can 't be signed
				MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
				aMex.executeDialogLocal(m_sErrorMacroPresent);
				return false;
			}
		}
		return true;
	}

	//ROB
	private byte[] encapsulateInDigestInfo(String digestAlg, byte[] digestBytes) throws IOException {

		byte[] bcDigestInfoBytes = null;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DEROutputStream dOut = new DEROutputStream(bOut);

		DERObjectIdentifier digestObjId = new DERObjectIdentifier(digestAlg);
		AlgorithmIdentifier algId = new AlgorithmIdentifier(digestObjId, null);
		DigestInfo dInfo = new DigestInfo(algId, digestBytes);

		dOut.writeObject(dInfo);
		return bOut.toByteArray();

	}
}
