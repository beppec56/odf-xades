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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.cms.CMSSignedDataGenerator;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.document.XStorageBasedDocument;
import com.sun.star.embed.ElementModes;
import com.sun.star.embed.InvalidStorageException;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XCommonEmbedPersist;
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
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.linguistic2.XThesaurus;
import com.sun.star.packages.WrongPasswordException;
import com.sun.star.script.BasicErrorException;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
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
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.DigiDocFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;
import com.yacme.ext.oxsit.custom_it.LogJarVersion;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.pack.DigitalSignatureHelper;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import com.yacme.ext.oxsit.ooo.ui.DialogQueryPIN;
import com.yacme.ext.oxsit.ooo.ui.MessageError;
import com.yacme.ext.oxsit.ooo.ui.MessageNoSignatureToken;
import com.yacme.ext.oxsit.pkcs11.PKCS11Driver;
import com.yacme.ext.oxsit.security.PKCS11TokenAttributes;
import com.yacme.ext.oxsit.security.ReadCerts;
import com.yacme.ext.oxsit.security.XOX_DocumentSigner;
import com.yacme.ext.oxsit.security.XOX_SSCDevice;
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

	protected XStorage m_xDocumentStorage;
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
	private String m_sErroreIsReadOnly;
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
			m_sErroreIsReadOnly = _aRegAcc.getStringFromRegistry("id_docum_is_readonly");
			m_sErrorMacroPresent = _aRegAcc.getStringFromRegistry("id_docum_contains_macro");
		} catch (com.sun.star.uno.Exception e) {
			m_aLogger.severe("", "", e);
		}
		_aRegAcc.dispose();
	}

	@Override
	public String getImplementationName() {
		// TODO Auto-generated method stub
		m_aLogger.entering("getImplementationName");
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.info("getSupportedServiceNames");
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
	 * when instantiated, 
	 * 	_oObj[0] first argument document URL
	 *  _oObj[1] corresponding XStorage object
	 */
	@Override
	public void initialize(Object[] _oObj) throws Exception {
		// TODO Auto-generated method stub
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
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSigner#signDocument(com.sun.star.document.XStorageBasedDocument, com.yacme.ext.oxsit.security.cert.XOX_X509Certificate[])
	 * 
	 * gets called from dialog when a document should be signed with independednt certificate signature
	 */
	@Override
	public boolean signDocument(XFrame xFrame, XModel xDocumentModel, XOX_X509Certificate[] _aCertArray, Object[] _oObjects)
			throws IllegalArgumentException, Exception {
		//init some localized error text

		m_xFrame = xFrame;
		m_aLogger.log(this.getClass().getName() + "\n\t\tthe url of the document under signature is: " + xDocumentModel.getURL());
		LogJarVersion custom_itStart = new LogJarVersion(m_aLogger);

		m_aLogger.log(custom_itStart.getVersion());

		//		//get the document storage,
		//		XStorageBasedDocument xDocStorage = (XStorageBasedDocument) UnoRuntime.queryInterface(XStorageBasedDocument.class,
		//				xDocumentModel);
		//
		//		m_xDocumentStorage = xDocStorage.getDocumentStorage();

		//		return signAsCMSFile(xFrame, xDocumentModel, _aCertArray);
		return signAsFile(xFrame, xDocumentModel, _aCertArray);

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
	 * @param xDocumentModel
	 * @param aCertArray
	 * @return
	 */
	private boolean signAsFile(XFrame xFrame, XModel xDocumentModel, XOX_X509Certificate[] _aCertArray) {
		Date d1, d2;

		ODFSignedDoc sdoc = null;

		ConfigManager.init("jar://ODFDocSigning.cfg");

		try {

			//get URL, open the storage from url
			//we need to get the XStorage separately, from the document URL
			//But first we need a StorageFactory object
			Object xFact = m_xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", m_xCC);
			//then obtain the needed interface
			XSingleServiceFactory xStorageFact = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class,
					xFact);
			//now, using the only method available, open the storage
			Object[] aArguments = new Object[2];
			aArguments[0] = xDocumentModel.getURL();
			aArguments[1] = ElementModes.READWRITE;
			//get the document storage object 
			Object xStdoc = xStorageFact.createInstanceWithArguments(aArguments);

			//this is for debug purposes only, not really needed here
			//XStorageBasedDocument xDocStorage = (XStorageBasedDocument) UnoRuntime.queryInterface(XStorageBasedDocument.class, xDocumentModel);

			//from the storage object (or better named, the service) obtain the interface we need
			m_xDocumentStorage = (XStorage) UnoRuntime.queryInterface(XStorage.class, xStdoc);

			// create a new SignedDoc 
			sdoc = new ODFSignedDoc(m_xMCF, m_xCC, m_xDocumentStorage, ODFSignedDoc.FORMAT_ODF_XADES, ODFSignedDoc.VERSION_1_3);

			//now read the data from the document and prepare to sign
			//			byte[] manifestBytes = 
			sdoc.addODFData();

			XOX_X509Certificate aCert = _aCertArray[0];

			CertificateFactory cf;
			cf = CertificateFactory.getInstance("X.509");
			java.io.ByteArrayInputStream bais = null;

			bais = new java.io.ByteArrayInputStream(aCert.getCertificateAttributes().getDEREncoded());
			X509Certificate certChild = (java.security.cert.X509Certificate) cf.generateCertificate(bais);

			m_aLogger.log("cert label: " + aCert.getCertificateAttributes().getLabel());

			// get the device this was seen on
			XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(XOX_SSCDevice.class, aCert.getSSCDevice());

			m_sPkcs11CryptoLib = xSSCD.getCryptoLibraryUsed();

			m_aLogger.log("signDocument with: " + xSSCD.getDescription() + " cryptolib: " + m_sPkcs11CryptoLib);
			PKCS11TokenAttributes aTka = new PKCS11TokenAttributes(xSSCD.getManufacturer(), // from
					// device
					// description
					xSSCD.getDescription(), // from device description
					xSSCD.getTokenSerialNumber(), // from token
					xSSCD.getTokenMaximumPINLenght()); // from token

			// try to get a pin from the user
			DialogQueryPIN aDialog1 = new DialogQueryPIN(xFrame, m_xCC, m_xMCF, aTka);
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
				d1 = new Date();
				// add a Signature
				m_aLogger.log("Prepare ODF signature");
				Signature sig = sdoc.prepareSignature(certChild, null, null);
				byte[] sidigest = sig.calculateSignedInfoDigest();
				d2 = new Date();
				m_aLogger.log("Preparing complete, time: " + ((d2.getTime() - d1.getTime()) / 1000) + " [sek]");
				byte[] sigval = null;
				//JDigiDoc
				//byte[] sigval = sigFac.sign(sidigest, 0, pin);
				// user confirmed, check opening the session
				SecurityManager sm = System.getSecurityManager();
				if (sm != null) {
					m_aLogger.info("SecurityManager: " + sm);
				} else {
					m_aLogger.info("no SecurityManager.");
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
						//				m_aHelperPkcs11.openSession();
						try {
							//now here start the true signature code, we sign the SHA1 sums we goto from
							//digesting process.
							long privateKeyHandle = m_aHelperPkcs11.findSignatureKeyFromID(aCert.getCertificateAttributes()
									.getID());
							//                    .findSignatureKeyFromCertificateHandle(m_aHelperPkcs11.getTokenHandle());
							m_aLogger.log("privateKeyHandle: " + privateKeyHandle);
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
								m_aLogger.log("Finalize signature");
								sig.setSignatureValue(sigval);

								////////// BeppeC The following chunk of code is here for debug purposes, in the future it might be moved elsewhere 								
								byte[] theSignatureXML = sig.toXML();

								String sUserHome = System.getProperty("user.home");

								File aFile = new File(sUserHome + "/" + ConstantCustomIT.m_sSignatureFileName);

								aFile.createNewFile();
								FileOutputStream os = new FileOutputStream(aFile);

								sdoc.writeSignaturesToStream(os);

								os.close();

								/// logging, only debug
								sdoc.writeSignaturesToXLogger(m_aLogger);

								//

								////// end of debug only code
								//after this, add the file the signature just set.

								//and write it back to the storage

								//so, open the substorage META-INF from the main storage (e.g. the document)
								try {
									XStorage xMetaInfStorage = m_xDocumentStorage.openStorageElement(ConstantCustomIT.m_sSignatureStorageName,ElementModes.WRITE);

									//try to remove the previous signature
									//FIXME would be better to import the existent signatures and add the new one
									try {
										xMetaInfStorage.removeElement(ConstantCustomIT.m_sSignatureFileName);
									} catch (NoSuchElementException e1) {
										m_aLogger.log("signAsFile", "\"" + ConstantCustomIT.m_sSignatureFileName + "\""
												+ " does not exist");
									}
									//create the file xadessignature.xml
									try {
										XStream xTheSignature = xMetaInfStorage.openStreamElement(ConstantCustomIT.m_sSignatureFileName, ElementModes.WRITE);
										//write to it (just a test now)
										String aString = "a simple test...";

										byte[] theSignatureBytes = sig.toXML();

										XOutputStream xOutStream = xTheSignature.getOutputStream();

										sdoc.writeSignaturesToXStream(xOutStream);

										//							            xOutStream.writeBytes( theSignatureBytes );
										xOutStream.flush();
										xOutStream.closeOutput();

										XTransactedObject xTransObj = (XTransactedObject) UnoRuntime.queryInterface(
												XTransactedObject.class, xMetaInfStorage);
										if (xTransObj != null) {
											m_aLogger.log("XTransactedObject exists. ===================");
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
												m_xDocumentStorage);
										if (xTransObj != null) {
											m_aLogger.log("XTransactedObject(m_xDocumentStorage) exists. ===================");
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

									//save the document

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
							m_aLogger.log("Throwable thrown! Closing session.");
							m_aHelperPkcs11.closeSession();
							throw (e);
						}
						m_aHelperPkcs11.closeSession();
					} else {
						//0x000000E0 = CKR_TOKEN_NOT_PRESENT
						//see iaik/pkcs/pkcs11/wrapper/ExceptionMessages.properties
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
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			m_aLogger.log("Exception: " + e.getMessage());
			e.printStackTrace(System.err);
		} catch (SignedDocException ex) {
			m_aLogger.log("Exception: " + ex.getMessage());
			System.err.println(ex);
			ex.printStackTrace(System.err);
		} catch (com.sun.star.io.IOException e) {
			m_aLogger.log(e, true);
		} catch (Throwable ex) {
			m_aLogger.log("Exception: " + ex.getMessage());
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}

		/*
		 * form a digest for any of the document substorage (files) the document has according to the decided standard
		 */

		//instantiate a subdescriptor of the document
		//		ODFSignedDoc theDoc = new ODFSignedDoc(m_xMCF, m_xCC);
		//		
		//		try {
		//			m_xDocumentStorage = xDocStorage.getDocumentStorage();
		////continue the signing			
		//
		////build the document description
		//			theDoc.verifyDocumentSignature(m_xDocumentStorage, null);
		//			
		//			
		//			
		//			
		//			
		//		} catch (com.sun.star.io.IOException e) {
		//			m_aLogger.log(e, true);
		//		} catch (Exception e) {
		//			m_aLogger.log(e, true);
		//		}
		return false;
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

			m_aLogger.log("cert label: " + aCert.getCertificateAttributes().getLabel());

			// get the device this was seen on
			XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(XOX_SSCDevice.class, aCert.getSSCDevice());

			m_sPkcs11CryptoLib = xSSCD.getCryptoLibraryUsed();

			m_aLogger.log("signDocument with: " + xSSCD.getDescription() + " cryptolib: " + m_sPkcs11CryptoLib);
			PKCS11TokenAttributes aTka = new PKCS11TokenAttributes(xSSCD.getManufacturer(), // from
					// device
					// description
					xSSCD.getDescription(), // from device description
					xSSCD.getTokenSerialNumber(), // from token
					xSSCD.getTokenMaximumPINLenght()); // from token

			try {
				SecurityManager sm = System.getSecurityManager();
				if (sm != null) {
					m_aLogger.info("SecurityManager: " + sm);
				} else {
					m_aLogger.info("no SecurityManager.");
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
								DialogQueryPIN aDialog1 = new DialogQueryPIN(xFrame, m_xCC, m_xMCF, aTka);
								int BiasX = 100;
								int BiasY = 30;
								aDialog1.initialize(BiasX, BiasY);
								aDialog1.executeDialog();
								char[] myPin = aDialog1.getPin();
								if (myPin != null && myPin.length > 0) {
									// user confirmed, check opening the session
									byte[] encDigestBytes = null;
									m_aLogger.log("sign!");
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
											m_aLogger.log("privateKeyHandle: " + privateKeyHandle);
											if (privateKeyHandle > 0) {
												encDigestBytes = m_aHelperPkcs11.signDataSinglePart(privateKeyHandle, baSha1);
											}
											//at list one certificate was signed
											bRetValue = true;
											bRetry = false;
										} catch (Throwable e) {
											//any exception thrown during signing process comes here
											//close the pending session
											m_aLogger.log("Throwable thrown! Closing session.");
											m_aHelperPkcs11.closeSession();
											throw (e);
										}
										m_aHelperPkcs11.closeSession();

									} catch (TokenException e) {
										// session can not be opened
										m_aLogger.warning("", ">TokenException", e);
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
					if (m_aHelperPkcs11 != null) {
						m_aHelperPkcs11.libFinalize();
						m_aHelperPkcs11 = null;
					}
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

	private boolean dummyCodeParking(XStorage xStorage, XFrame xFrame, XOX_X509Certificate[] _aCertArray) {
		// TODO Auto-generated method stub
		// for the time being only the first certificate is used
		XOX_X509Certificate aCert = _aCertArray[0];

		m_aLogger.log("cert label: " + aCert.getCertificateAttributes().getLabel());

		// get the device this was seen on
		XOX_SSCDevice xSSCD = (XOX_SSCDevice) UnoRuntime.queryInterface(XOX_SSCDevice.class, aCert.getSSCDevice());

		// to see if all is all right, examine the document structure
		/*
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
		 */

		String cryptolibrary = xSSCD.getCryptoLibraryUsed();

		m_aLogger.log("signDocument with: " + xSSCD.getDescription() + " cryptolib: " + cryptolibrary);
		// just for test, analyze the document package structure
		DigitalSignatureHelper dg = new DigitalSignatureHelper(m_xMCF, m_xCC);

		dg.verifyDocumentSignature(xStorage, null);

		PKCS11TokenAttributes aTk = new PKCS11TokenAttributes(xSSCD.getManufacturer(), //from device description
				xSSCD.getDescription(), // from device description
				xSSCD.getTokenSerialNumber(), //from token
				xSSCD.getTokenMaximumPINLenght()); //from token
		// try to get a pin from the user
		DialogQueryPIN aDialog1 = new DialogQueryPIN(xFrame, m_xCC, m_xMCF, aTk);
		// set the device description, can be used to display information on the
		// device the PIN is asked for

		try {
			// PosX e PosY devono essere ricavati dalla finestra genetrice (in
			// questo caso la frame)
			// get the parente window data
			// com.sun.star.awt.XWindow xCompWindow =
			// m_xFrame.getComponentWindow();
			// com.sun.star.awt.Rectangle xWinPosSize =
			// xCompWindow.getPosSize();
			int BiasX = 100;
			int BiasY = 30;
			// System.out.println("Width: "+xWinPosSize.Width+
			// " height: "+xWinPosSize.Height);
			// XWindow xWindow = m_xFrame.getContainerWindow();
			// XWindowPeer xPeer = xWindow.
			aDialog1.initialize(BiasX, BiasY);
			// center the dialog
			aDialog1.executeDialog();
			char[] myPin = aDialog1.getPin();
			if (myPin != null && myPin.length > 0) {
				m_aLogger.log("sign!");
				// convert certificate in Java format

				X509Certificate signatureCert = Helpers.getCertificate(aCert);

				PKCS11Driver helper;
				try {
					SecurityManager sm = System.getSecurityManager();
					if (sm != null) {
						m_aLogger.info("SecurityManager: " + sm);
					} else {
						m_aLogger.info("no SecurityManager.");
					}
					String Pkcs11WrapperLocal = Helpers.getPKCS11WrapperNativeLibraryPath(m_xCC);
					helper = new PKCS11Driver(m_aLogger, Pkcs11WrapperLocal, cryptolibrary);

					// these are 160 corresponding to a SHA1 hash (or digest)
					byte[] baSha1 = { 0x63, (byte) 0xAA, 0x4D, (byte) 0xD0, (byte) 0xF3, (byte) 0x8F, 0x62, (byte) 0xDC,
							(byte) 0xF7, (byte) 0x6F, (byte) 0xF2, (byte) 0x09, (byte) 0xA7, 0x5B, 0x01, 0x4E, 0x78, (byte) 0xF2,
							(byte) 0xF1, 0x31 };
					// try to sign something simple
					/*
					 * String sTest = "Y6pN0POPYtz3b/IJp1sBTnjy8TE="
					 */

					long[] nTokens = null;
					try {
						nTokens = helper.getTokenList();
						nTokens = helper.getTokens();
					} catch (PKCS11Exception ex3) {
						m_aLogger.severe("detectTokens, PKCS11Exception " + cryptolibrary, ex3);
					}

					if (nTokens != null) {
						// search in the available m_nTokens the one with the
						// certificate
						// selected
						for (int i = 0; i < nTokens.length; i++) {
							m_aLogger.log("token: " + nTokens[i]);
						}
						// helper.getModuleInfo(); info on pkcs#11 library

						helper.getMechanismInfo();
						// open session on this token
						helper.setTokenHandle(nTokens[0]);
						try {
							// helper.openSession(myPin);
							helper.openSession();

							// find private key and certificate in first token
							// only
							try {
								CK_TOKEN_INFO tokenInfo = helper.getTokenInfo(nTokens[0]);

								m_aLogger.log(tokenInfo.toString());

								// from the certificate get the mechanism needed
								// (the subject signature algor)
								// this will be the mechanism used to sign ??

								// from the certificate get the certificate
								// handle
								try {
									long certHandle = helper.findCertificate(signatureCert);

									// then the certificate handle get the
									// corresponding
									// private key handle
									// long sigKeyHandle =
									// helper.findSignatureKeyFromCertificateHandle(certHandle);
									// get key label as well
								} catch (PKCS11Exception e) {
									m_aLogger.severe(e);
								} catch (CertificateEncodingException e) {
									m_aLogger.severe(e);
								} catch (IOException e) {
									m_aLogger.severe(e);
								}
							} catch (Throwable e) {
								m_aLogger.severe(e);
							}
							// helper.logout();
							helper.closeSession();
						} catch (TokenException ex) {
							m_aLogger.log("Messaggio helper.openSession(): " + ex.getMessage());
							if (ex.toString().startsWith("iaik.pkcs.pkcs11.wrapper.PKCS11Exception: CKR_PIN_INCORRECT")
									|| ex.toString().startsWith("CKR_PIN_INCORRECT")) {
								// errorPresent = true;
								m_aLogger.log("PIN sbagliato.");
								// current = ERROR;

							}

							else if (ex.getMessage().startsWith("CKR_PIN_LOCKED")) {
								// errorPresent = true;
								m_aLogger.log("PIN bloccato.");
							} else if (ex.getMessage().startsWith("CKR_PIN_LEN_RANGE")) {
								// errorPresent = true;
								m_aLogger.log("PIN sbagliato: Lunghezza sbagliata.");
							} else if (ex.getMessage().startsWith("CKR_TOKEN_NOT_RECOGNIZED")) {
								// errorPresent = true;
								m_aLogger.log("CKR_TOKEN_NOT_RECOGNIZED.");
							} else if (ex.getMessage().startsWith("CKR_FUNCTION_FAILED")) {
								// errorPresent = true;
								m_aLogger.log("CKR_FUNCTION_FAILED.");
							}

							else if (ex.getMessage().startsWith("CKR_ARGUMENTS_BAD")) {
								// errorPresent = true;
								m_aLogger.log("CKR_ARGUMENTS_BAD.");
							} else {
								// inserisci tutte le TokenException!!!
								// errorPresent = true;
								// errorMsg =
								// "PKCS11Exception:\n"+ex.getMessage()+".";
							}
						}

					}
					helper.libFinalize();
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

				return true;
			}
		} catch (com.sun.star.uno.RuntimeException e) {
			m_aLogger.severe(e);
		} catch (BasicErrorException e) {
			m_aLogger.severe(e);
		} catch (Exception e) {
			m_aLogger.severe(e);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		//check the filtername
		PropertyValue[] aPVal = _xDocumentModel.getArgs();
		/////////////// for debug only
		for (int i = 0; i < aPVal.length; i++) {
			PropertyValue aVal = aPVal[i];
			m_aLogger.log(Utilities.showPropertyValue(aVal));
		}
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
		m_aLogger.log("document type ok !");

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

		//text sections? 

		//then the embedded object, should be all embedded, and if embedded
		//only the right type is allowed

		//the the forms, for now no forms are allowed in the text document.

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
		// decide if new or already saved
		XModifiable xMod = (XModifiable) UnoRuntime.queryInterface(XModifiable.class, _xDocumentModel);
		if ((xMod != null && xMod.isModified()) || (xStore != null && !xStore.hasLocation())) {
			MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
			aMex.executeDialogLocal(m_sErrorNotYetSaved);
			return false;
		}

		//check if the document is readonly: to sign we need to have the full control on it:
		if (xStore == null || xStore.isReadonly()) {
			MessageError aMex = new MessageError(_xFrame, m_xMCF, m_xCC);
			aMex.executeDialogLocal(m_sErroreIsReadOnly);
			return false;
		}
		//check the main document types interfaces
		XTextDocument xText = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, _xDocumentModel);
		if (xText != null) {
			if (!verifyTextDocumentBeforeSigning(_xFrame, _xDocumentModel)) {
				return false;
			}
		} else {
			Utilities.showInterfaces(_xDocumentModel, _xDocumentModel);
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
		m_xDocumentStorage = xDocStorage.getDocumentStorage();

		//		Utilities.showInterfaces(xDocStorage, m_xDocumentStorage);
		xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, m_xDocumentStorage);
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
				m_aLogger.log("Version is: " + sVersion); // this should be 1.2 or more
				if (sVersion.equalsIgnoreCase("1.2"))
					m_nTypeOfDocumentToBeSigned = IS_ODF12;
			} else {
				m_aLogger.log("Version is 1.0 or 1.1");
				m_nTypeOfDocumentToBeSigned = IS_ODF10_OR_11;
			}
			String sMediaType = "";
			try {
				sMediaType = (String) xPropSet.getPropertyValue("MediaType");
				m_aLogger.log("main storage media type: " + sMediaType);
			} catch (UnknownPropertyException e) {
				m_aLogger.warning("makeTheElementList", "Mediatype missing", e);
				//no problem if not existent
			} catch (WrappedTargetException e) {
				m_aLogger.warning("makeTheElementList", "Mediatype missing", e);
			}
		} else
			m_aLogger.log("Version does not exists! May be this is not a ODF package?");

		//verify if there is a Basic substorage holding the basic script
		String[] aElements = m_xDocumentStorage.getElementNames();
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

	//ROB: verification sample code
	public void verifySample(String fileToVerify) {
		DigiDocFactory digFac;
		try {
			digFac = ConfigManager.instance().getSignedDocFactory();

			ODFSignedDoc sdoc = (ODFSignedDoc) digFac.readSignedDoc(fileToVerify);
			// System.out.println("GOT: " + sdoc.toXML());

			// verify signature
			Signature sig = null;
			for (int i = 0; i < sdoc.countSignatures(); i++) {
				sig = sdoc.getSignature(i);
				System.out.println("Signature: " + sig.getId() + " - " + sig.getKeyInfo().getSubjectLastName() + ","
						+ sig.getKeyInfo().getSubjectFirstName() + "," + sig.getKeyInfo().getSubjectPersonalCode());
				ArrayList errs = sig.verify(sdoc, true, false);
				if (errs.size() == 0)
					System.out.println("Verification OK!");
				for (int j = 0; j < errs.size(); j++)
					System.out.println((SignedDocException) errs.get(i));
				System.out.println();
			}
		} catch (SignedDocException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
