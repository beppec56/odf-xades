/*************************************************************************
 * 
 *  This code is derived from
 *  it.trento.comune.j4sign.pcsc.PKCS11Signer class in j4sign
 *  adapted to be used in OOo UNO environment
 *	Copyright (c) 2004 Roberto Resoli - Servizio Sistema Informativo - Comune di Trento.
 *
 *  For OOo UNO adaptation:
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  Copyright 2009 by Roberto Resoli resoli@osor.eu
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

package it.plio.ext.oxsit.pkcs11;

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.CK_ATTRIBUTE;
import iaik.pkcs.pkcs11.wrapper.CK_INFO;
import iaik.pkcs.pkcs11.wrapper.CK_MECHANISM;
import iaik.pkcs.pkcs11.wrapper.CK_MECHANISM_INFO;
import iaik.pkcs.pkcs11.wrapper.CK_SLOT_INFO;
import iaik.pkcs.pkcs11.wrapper.CK_TOKEN_INFO;
import iaik.pkcs.pkcs11.wrapper.Functions;
import iaik.pkcs.pkcs11.wrapper.PKCS11;
import iaik.pkcs.pkcs11.wrapper.PKCS11Connector;
import iaik.pkcs.pkcs11.wrapper.PKCS11Constants;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLazyLogger;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x509.X509CertificateStructure;

/**
 * @author beppe
 *
 */
public class PKCS11Driver {

    private IDynamicLogger m_aLogger;

    /**
     * The PKCS#11 session identifier returned when a session is opened. Value
     * is -1 if no session is open.
     */
    private long sessionHandle = -1L;

    /**
     * The <code>cryptokiLibrary</code> is the native library implementing the
     * <code>PKCS#11</code> specification.
     */
    private java.lang.String cryptokiLibrary = null;

    /**
     * The java object wrapping criptoki library functionalities.
     */
    private PKCS11 pkcs11Module = null;

    /**
     * PKCS#11 identifier for the signature algorithm.
     */
    private CK_MECHANISM signatureMechanism = null;

    /**
     * The PKCS#11 token identifier. Value is -1 if there is no current token.
     */
    private long tokenHandle = -1L;

	public PKCS11Driver(IDynamicLogger aLogger, String pkcs11WrapLib, String cryptokiLib) throws
    IOException, TokenException {
		super(); // ????

		setLogger(aLogger);

		// log = out;
		cryptokiLibrary = cryptokiLib;

		m_aLogger.info("Initializing PKCS11Driver...");

		m_aLogger.info("Trying to connect to PKCS#11 module: '"
				+ cryptokiLibrary + "' ...");
		//this strange way of calling
		//depends from the value returned
		if(pkcs11WrapLib != null && pkcs11WrapLib.length() > 0)
			pkcs11Module = PKCS11Connector.connectToPKCS11Module(cryptokiLibrary,pkcs11WrapLib);
		else
			pkcs11Module = PKCS11Connector.connectToPKCS11Module(cryptokiLibrary);							

		m_aLogger.info("connected");

		initializeLibrary();
	}

    /**
     * Returns the DER encoded certificate identified by the given handle, as
     * read from the token.
     *
     * @param certHandle
     *            the handleof the certificate on the token.
     * @return the DER encoded certificate, as a byte array.
     * @throws UnsupportedEncodingException
     * @throws TokenException
     */
    public byte[] getDEREncodedCertificate(long certHandle) throws
            PKCS11Exception {

        m_aLogger.log("reading certificate bytes");

        byte[] certBytes = null;
        CK_ATTRIBUTE[] template = new CK_ATTRIBUTE[1];
        template[0] = new CK_ATTRIBUTE();
        template[0].type = PKCS11Constants.CKA_VALUE;
        pkcs11Module.C_GetAttributeValue(getSession(), certHandle, template);
        certBytes = (byte[]) template[0].pValue;

        return certBytes;
    }

    /**
     * 
     * @param certHandle
     * @return the certificare ID as returned by CKA_ID attribute query
     * @throws PKCS11Exception
     */
    public byte[] getCertificateID(long certHandle) throws PKCS11Exception {

    	m_aLogger.log("reading certificate ID (CKA_ID)");

		byte[] certID = null;
		CK_ATTRIBUTE[] template = new CK_ATTRIBUTE[1];
		template[0] = new CK_ATTRIBUTE();
		template[0].type = PKCS11Constants.CKA_ID;
		pkcs11Module.C_GetAttributeValue(getSession(), certHandle, template);
		if(template[0].pValue != null)
			certID = (byte[]) template[0].pValue;

		return certID;
	}

    public char[] getCertificateLabel(long certHandle) throws PKCS11Exception {

    	m_aLogger.log("reading certificate Label (CKA_LABEL)");

		char[] certLabel = null;
		CK_ATTRIBUTE[] template = new CK_ATTRIBUTE[1];
		template[0] = new CK_ATTRIBUTE();
		template[0].type = PKCS11Constants.CKA_LABEL;
		pkcs11Module.C_GetAttributeValue(getSession(), certHandle, template);
		if(template[0].pValue != null)
			certLabel = (char[]) template[0].pValue;

		return certLabel;
	}
    /**
     * Trova un'array di certHandle di tutti i certificati presenti sulla carta
     * senza che la sessione sia aperta (no password). La length dell'array corrisponde
     * al numero dei certificati
     *
     * @return the handle of the required certificate, if found; -1 otherwise.
     * @throws TokenException
     * @throws CertificateException
     */
    public long[] findCertificates() throws TokenException,
            CertificateException {

        long certKeyHandle = -1L;

        m_aLogger.info("finding all certificates on token ...");

        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[1];

        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_CLASS;
        attributeTemplateList[0].pValue = new Long(
                PKCS11Constants.CKO_CERTIFICATE);

        pkcs11Module.C_FindObjectsInit(getSession(), attributeTemplateList);

        long[] availableCertificates = pkcs11Module.C_FindObjects(getSession(),
                100);
        //maximum of 100 at once

        pkcs11Module.C_FindObjectsFinal(getSession());

        if (availableCertificates == null) {
            m_aLogger.info("null returned - no certificate key found");
        } else {
            m_aLogger.info("found " + availableCertificates.length
                        + " certificates");

        }

        return availableCertificates;
    }

    /**
     * Finds a certificate matching the one passed as parameter.
     *
     * @param _aCertificate
     * @return the handle of the certificate, or -1 if not found.
     * @throws PKCS11Exception
     * @throws CertificateEncodingException 
     * @throws IOException 
     */
    public long findCertificate(X509Certificate _aCertificate) throws PKCS11Exception, CertificateEncodingException, IOException {

        long sessionHandle = getSession();
        long certificateHandle = -1L;

        if (sessionHandle < 0 || _aCertificate == null) {
            return -1L;
        }

        m_aLogger.info("find certificate.");
		ByteArrayInputStream as = new ByteArrayInputStream(_aCertificate.getEncoded()); 
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado;
		ado = aderin.readObject();
		X509CertificateStructure m_aX509 = new X509CertificateStructure((ASN1Sequence) ado);

        // now get the certificate with the same ID as the signature key
        int idx = 0;
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[4];

        attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_CLASS;
        attributeTemplateList[idx++].pValue = new Long(
                PKCS11Constants.CKO_CERTIFICATE);

        attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_SUBJECT;
        attributeTemplateList[idx++].pValue =  m_aX509.getTBSCertificate().getSubject().getDEREncoded();
        
        attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_ISSUER;
        attributeTemplateList[idx++].pValue =  m_aX509.getTBSCertificate().getIssuer().getDEREncoded();
        
        byte[] ar = m_aX509.getTBSCertificate().getSerialNumber().getDEREncoded();
        byte[] sn = new byte[3];
        
        sn[0] = ar[2];
        sn[1] = ar[3];
        sn[2] = ar[4];
        
        ar = m_aX509.getTBSCertificate().getSerialNumber().getEncoded();

        ar = m_aX509.getTBSCertificate().getSerialNumber().getEncoded("BER");

        attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_SERIAL_NUMBER;
        attributeTemplateList[idx++].pValue =  sn;

/*        attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_SUBJECT;
        attributeTemplateList[idx++].pValue =  _aCertificate.getSubjectX500Principal().getEncoded();*/
        
/*        attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_ISSUER;
        attributeTemplateList[idx++].pValue =  _aCertificate.getIssuerX500Principal().getEncoded();

        //now we need to get the serial number of the certificate, we need the DER
        // version
		ByteArrayInputStream as = new ByteArrayInputStream(_aCertificate.getEncoded()); 
		ASN1InputStream aderin = new ASN1InputStream(as);
		DERObject ado;
		ado = aderin.readObject();
		X509CertificateStructure m_aX509 = new X509CertificateStructure((ASN1Sequence) ado);

		attributeTemplateList[idx] = new CK_ATTRIBUTE();
        attributeTemplateList[idx].type = PKCS11Constants.CKA_SERIAL_NUMBER;
        attributeTemplateList[idx++].pValue =  m_aX509.getTBSCertificate().toASN1Object().getObjectAT(1);//getSerialNumber().getDERObject().getDEREncoded();// getEncoded(); //getDEREncoded(); no
        
*/        
        pkcs11Module.C_FindObjectsInit(getSession(), attributeTemplateList);

        long[] availableCertificates = pkcs11Module.C_FindObjects(getSession(),
                100);
        //maximum of 100 at once
        if (availableCertificates == null || availableCertificates.length == 0) {
        	m_aLogger.info("null returned - no certificate found");
        } else {
        	m_aLogger.info("found " + availableCertificates.length
                        + " certificates with matching attributes.");
            for (int i = 0; i < availableCertificates.length; i++) {
                if (i == 0) { // the first we find, we take as our certificate
                    certificateHandle = availableCertificates[i];
                    if(certificateHandle > 0L) {
                        // now get the certificate with the same ID as the signature key
                        CK_ATTRIBUTE[] attributeTemplateListR = new CK_ATTRIBUTE[3];

                        attributeTemplateListR[0] = new CK_ATTRIBUTE();
                        attributeTemplateListR[0].type = PKCS11Constants.CKA_SERIAL_NUMBER;
                    	
                        attributeTemplateListR[1] = new CK_ATTRIBUTE();
                        attributeTemplateListR[1].type = PKCS11Constants.CKA_LABEL;

                        attributeTemplateListR[2] = new CK_ATTRIBUTE();
                        attributeTemplateListR[2].type = PKCS11Constants.CKA_ID;

                        pkcs11Module.C_GetAttributeValue(getSession(), certificateHandle,
                                attributeTemplateListR);
                        byte[] certificateSN = null;
                        if(attributeTemplateListR[0].pValue != null) {
	                        certificateSN = (byte[]) attributeTemplateListR[0].pValue;
	                        if(certificateSN != null) {
	                        	m_aLogger.log("CKA_SERIAL_NUMBER "+Helpers.printHexBytes(certificateSN));
	                        }
                        }
                        if(attributeTemplateListR[1].pValue != null) {
                        	
                        	attributeTemplateListR[1].pValue.toString();
                        	String aLabel = new String((char[]) attributeTemplateListR[1].pValue);
	                        	m_aLogger.log("CKA_LABEL '"+aLabel+"'");
                        }
                        if(attributeTemplateListR[2].pValue != null) {
	                        certificateSN = (byte[]) attributeTemplateListR[2].pValue;
	                        if(certificateSN != null) {
	                        	m_aLogger.log("CKA_ID "+Helpers.printHexBytes(certificateSN));
	                        }
                        }
                    }
                }
                m_aLogger.info("certificate " + i);
            }
        }
        pkcs11Module.C_FindObjectsFinal(getSession());
//get serial number of this certificate
        
        
        
        return certificateHandle;
    }

    /**
     * Finds a certificate matching the given byte[] id.
     *
     * @param id
     * @return the handle of the certificate, or -1 if not found.
     * @throws PKCS11Exception
     */
    public long findCertificateFromID(byte[] id) throws PKCS11Exception {

        long sessionHandle = getSession();
        long certificateHandle = -1L;

        if (sessionHandle < 0 || id == null) {
            return -1L;
        }

        m_aLogger.info("find certificate from id.");

        // now get the certificate with the same ID as the signature key
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[2];

        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_CLASS;
        attributeTemplateList[0].pValue = new Long(
                PKCS11Constants.CKO_CERTIFICATE);
        attributeTemplateList[1] = new CK_ATTRIBUTE();
        attributeTemplateList[1].type = PKCS11Constants.CKA_ID;
        attributeTemplateList[1].pValue = id;

        pkcs11Module.C_FindObjectsInit(getSession(), attributeTemplateList);
        long[] availableCertificates = pkcs11Module.C_FindObjects(getSession(),
                100);
        //maximum of 100 at once
        if (availableCertificates == null) {
        	m_aLogger.info("null returned - no certificate found");
        } else {
        	m_aLogger.info("found " + availableCertificates.length
                        + " certificates with matching ID");
            for (int i = 0; i < availableCertificates.length; i++) {
                if (i == 0) { // the first we find, we take as our certificate
                    certificateHandle = availableCertificates[i];
                    System.out.print("for verification we use ");
                    
                    
                    
                }
                m_aLogger.info("certificate " + i);
            }
        }
        pkcs11Module.C_FindObjectsFinal(getSession());
        return certificateHandle;
    }

    /**
     * Finds a certificate matching the given textual label.
     *
     * @param label
     * @return the handle of the certificate, or -1 if not found.
     * @throws PKCS11Exception
     */
    public long findCertificateFromLabel(char[] label) throws PKCS11Exception {

        long sessionHandle = getSession();
        long certificateHandle = -1L;

        if (sessionHandle < 0 || label == null) {
            return -1L;
        }

        m_aLogger.log("find certificate from label.");

        // now get the certificate with the same ID as the signature key
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[2];

        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_CLASS;
        attributeTemplateList[0].pValue = new Long(
                PKCS11Constants.CKO_CERTIFICATE);
        attributeTemplateList[1] = new CK_ATTRIBUTE();
        attributeTemplateList[1].type = PKCS11Constants.CKA_LABEL;
        attributeTemplateList[1].pValue = label;

        pkcs11Module.C_FindObjectsInit(getSession(), attributeTemplateList);
        long[] availableCertificates = pkcs11Module.C_FindObjects(getSession(),
                100);
        //maximum of 100 at once
        if (availableCertificates == null) {
        	m_aLogger.log("null returned - no certificate found");
        } else {
        	m_aLogger.log("found " + availableCertificates.length
                        + " certificates with matching ID");
            for (int i = 0; i < availableCertificates.length; i++) {
                if (i == 0) { // the first we find, we take as our certificate
                    certificateHandle = availableCertificates[i];
                    System.out.print("for verification we use ");
                }
                m_aLogger.log("certificate " + i);
            }
        }
        pkcs11Module.C_FindObjectsFinal(getSession());

        return certificateHandle;
    }

    /**
     * Searches the certificate corresponding to the private key identified by
     * the given handle; this method assumes that corresponding certificates and
     * private keys are sharing the same byte[] IDs.
     *
     * @param signatureKeyHandle
     *            the handle of a private key.
     * @return the handle of the certificate corrisponding to the given key.
     * @throws PKCS11Exception
     */
    public long findCertificateFromSignatureKeyHandle(long signatureKeyHandle) throws
            PKCS11Exception {

        long sessionHandle = getSession();
        long certificateHandle = -1L;

        if (sessionHandle < 0) {
            return -1L;
        }

        m_aLogger.log("\nFind certificate from signature key handle: "
                    + signatureKeyHandle);

        // first get the ID of the signature key
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[1];
        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_ID;

        pkcs11Module.C_GetAttributeValue(getSession(), signatureKeyHandle,
                                         attributeTemplateList);

        byte[] keyAndCertificateID = (byte[]) attributeTemplateList[0].pValue;
        m_aLogger.log("ID of signature key: "
                    + Functions.toHexString(keyAndCertificateID));

        return findCertificateFromID(keyAndCertificateID);
    }


    /**
     * Searches the private key corresponding to the certificate identified by
     * the given handle; this method assumes that corresponding certificates and
     * private keys are sharing the same byte[] IDs.
     *
     * @param certHandle
     *            the handle of a certificate.
     * @return the handle of the private key corrisponding to the given
     *         certificate.
     * @throws PKCS11Exception
     */
    public long findSignatureKeyFromCertificateHandle(long certHandle) throws
            PKCS11Exception {

        long sessionHandle = getSession();
        long keyHandle = -1L;

        if (sessionHandle < 0) {
            return -1L;
        }

        m_aLogger.log("Find signature key from certificate with handle: "
                    + certHandle);

        // first get the ID of the signature key
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[1];
        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_ID;

        pkcs11Module.C_GetAttributeValue(getSession(), certHandle,
                                         attributeTemplateList);

        byte[] keyAndCertificateID = (byte[]) attributeTemplateList[0].pValue;

        m_aLogger.log("ID of cert: "
                         + Functions.toHexString(keyAndCertificateID));

        return findSignatureKeyFromID(keyAndCertificateID);
    }

    // look for a RSA key and encrypt ...
    public byte[] encryptDigest(String label, byte[] digest) throws
            PKCS11Exception, IOException {

        byte[] encryptedDigest = null;

        long sessionHandle = getSession();
        if (sessionHandle < 0) {
            return null;
        }

        long signatureKeyHandle = findSignatureKeyFromLabel(label);

        if (signatureKeyHandle > 0) {
            m_aLogger.log("\nStarting digest encryption...");
            encryptedDigest = signDataSinglePart(signatureKeyHandle, digest);
        } else {
            //         we have not found a suitable key, we cannot contiue
        }

        return encryptedDigest;
    }


    /**
     * Returns the private key handle, on current token, corresponding to the
     * given textual label.
     *
     * @param label
     *            the string label to search.
     * @return the integer identifier of the private key, or -1 if no key was
     *         found.
     * @throws PKCS11Exception
     */
    public long findSignatureKeyFromLabel(String label) throws PKCS11Exception {

        long signatureKeyHandle = -1L;

        if (getSession() < 0) {
            return -1L;
        }

        m_aLogger.log("finding signature key with label: '" + label + "'");
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[2];
        //CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[1];

        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_CLASS;
        attributeTemplateList[0].pValue = new Long(
                PKCS11Constants.CKO_PRIVATE_KEY);

        attributeTemplateList[1] = new CK_ATTRIBUTE();

        attributeTemplateList[1].type = PKCS11Constants.CKA_LABEL;
        attributeTemplateList[1].pValue = label.toCharArray();

        pkcs11Module.C_FindObjectsInit(getSession(), attributeTemplateList);
        long[] availableSignatureKeys = pkcs11Module.C_FindObjects(
                getSession(), 100);
        //maximum of 100 at once

        if (availableSignatureKeys == null) {
        	m_aLogger.log("null returned - no signature key found");
        } else {
        	m_aLogger.log("found " + availableSignatureKeys.length
                        + " signature keys, picking first.");
            for (int i = 0; i < availableSignatureKeys.length; i++) {
                if (i == 0) { // the first we find, we take as our signature key
                    signatureKeyHandle = availableSignatureKeys[i];
                    m_aLogger.log(
                                    "for signing we use signature key with handle: "
                                    + signatureKeyHandle);
                }
            }
        }
        pkcs11Module.C_FindObjectsFinal(getSession());

        return signatureKeyHandle;
    }
    
    /**
     * Returns the private key handle, on current token, corresponding to the
     * given byte[]. ID is often the byte[] version of the label.
     *
     * @param id
     *            the byte[] id to search.
     * @return the integer identifier of the private key, or -1 if no key was
     *         found.
     * @throws PKCS11Exception
     * @see PKCS11Signer#findSignatureKeyFromLabel(String)
     */
    public long findSignatureKeyFromID(byte[] id) throws PKCS11Exception {

        long signatureKeyHandle = -1L;

        if (getSession() < 0) {
            return -1L;
        }

        m_aLogger.log("finding signature key from id.");
        CK_ATTRIBUTE[] attributeTemplateList = new CK_ATTRIBUTE[2];

        attributeTemplateList[0] = new CK_ATTRIBUTE();
        attributeTemplateList[0].type = PKCS11Constants.CKA_CLASS;
        attributeTemplateList[0].pValue = new Long(
                PKCS11Constants.CKO_PRIVATE_KEY);

        attributeTemplateList[1] = new CK_ATTRIBUTE();

        attributeTemplateList[1].type = PKCS11Constants.CKA_ID;
        attributeTemplateList[1].pValue = id;

        pkcs11Module.C_FindObjectsInit(getSession(), attributeTemplateList);
        long[] availableSignatureKeys = pkcs11Module.C_FindObjects(
                getSession(), 100);
        //maximum of 100 at once

        if (availableSignatureKeys == null) {
            m_aLogger.log(
                            "null returned - no signature key found with matching ID");
        } else {
            m_aLogger.log("found " + availableSignatureKeys.length
                        + " signature keys, picking first.");
            for (int i = 0; i < availableSignatureKeys.length; i++) {
                if (i == 0) { // the first we find, we take as our signature key
                    signatureKeyHandle = availableSignatureKeys[i];
                    m_aLogger.log("returning signature key with handle: "
                                + signatureKeyHandle);
                }

            }
        }
        pkcs11Module.C_FindObjectsFinal(getSession());

        return signatureKeyHandle;
    }

    /**
     * Closes the default PKCS#11 session.
     *
     * @throws PKCS11Exception
     */
    public void closeSession() throws PKCS11Exception {

        if (getSession() == -1L) {
            return;
        }
        m_aLogger.info("Closing session ...");
        pkcs11Module.C_CloseSession(getSession());
        setSession( -1L);

    }

    /**
     * Finalizes PKCS#11 operations; note this NOT actually unloads the native
     * library.
     *
     * @throws Throwable
     */
    public void libFinalize() throws Throwable {
        m_aLogger.info("finalizing PKCS11 module...");
        if(getSession() != -1L) {
        	closeSession();
        }
       // getPkcs11().finalize();
        pkcs11Module.C_Finalize(null);
        m_aLogger.info("finalized.");
    }

    /**
     * Logs in to the current session; login is usually necessary to see and use
     * private key objects on the token.
     *
     * @param pwd
     *            password as a char[].
     * @throws PKCS11Exception
     */
    public void login(char[] pwd) throws PKCS11Exception {
        if (getSession() < 0) {
            return;
        }
        // log in as the normal user...

        pkcs11Module.C_Login(getSession(), PKCS11Constants.CKU_USER, pwd);
        m_aLogger.log("User logged into session.");
    }

    /**
     * Logs out the current user.
     *
     * @throws PKCS11Exception
     */
    public void logout() throws PKCS11Exception {
        if (getSession() < 0) {
            return;
        }
        // log in as the normal user...
        pkcs11Module.C_Logout(getSession());
        m_aLogger.info("User logged out.");
    }
    
    /**
     * Opens a session on the token, logging in the user.
     *
     * @throws TokenException
     */
    public void openSession(char[] password) throws TokenException, PKCS11Exception {
        openSession();
        login(password);
    }

    /**
     * Gets the current session handle.
     *
     * @return the <code>long</code> identifying the current session.
     */
    private long getSession() {
        return sessionHandle;
    }

    /**
     * Sets the session handle.
     *
     * @param newSession
     */
    private void setSession(long newSession) {
        this.sessionHandle = newSession;
    }

    /**
     * Opens a session on the default token.
     *
     * @throws TokenException
     */
    public void openSession() throws TokenException {
        long sessionHandle = -1L;
        if (getTokenHandle() >= 0) {
            sessionHandle = pkcs11Module.C_OpenSession(getTokenHandle(),
                    PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            setSession(sessionHandle);
            m_aLogger.info("Session opened.");

        } else {
            m_aLogger.info("No token found!");
        }
        //for testing wrong login
        //0x000000A0 = CKR_PIN_INCORRECT
        //0x00000007 = CKR_ARGUMENTS_BAD
        //0x000000A4 = CKR_PIN_LOCKED                        

//        throw	(new PKCS11Exception(0x000000A4));
    }

    public CK_TOKEN_INFO getTokenInfo(long _lTheToken) throws PKCS11Exception {
    	CK_TOKEN_INFO ret = null;
    	ret = pkcs11Module.C_GetTokenInfo(_lTheToken);
    	return ret;
    }
    
    /**
     * Lists currently inserted tokens and relative infos.
     *
     * @throws PKCS11Exception
     */

    public long[] getTokenList() {
        m_aLogger.info("\ngetting token list");
        long[] tokenIDs = null;
        //get only slots with a token present
        try {
            tokenIDs = pkcs11Module.C_GetSlotList(true);
        } catch (PKCS11Exception ex) {
            m_aLogger.severe(ex);
        }
        CK_TOKEN_INFO tokenInfo;
        m_aLogger.info(tokenIDs.length + " tokens found.");
        for (int i = 0; i < tokenIDs.length; i++) {
            m_aLogger.info(i + ") Info for token with handle: " + tokenIDs[i]);
            tokenInfo = null;
            try {
                tokenInfo = pkcs11Module.C_GetTokenInfo(tokenIDs[i]);
            } catch (PKCS11Exception ex1) {
                m_aLogger.severe(ex1);
            }
            m_aLogger.info(tokenInfo.toString());
        }

        return tokenIDs;
    }
    
    /**
     * Sign (here means encrypting with private key) the provided data with a
     * single operation. This is the only modality supported by the (currently
     * fixed) RSA_PKCS mechanism.
     *
     * @param signatureKeyHandle
     *            handle of the private key to use for signing.
     * @param data
     *            the data to sign.
     * @return a byte[] containing signed data.
     * @throws IOException
     * @throws PKCS11Exception
     */
    public byte[] signDataSinglePart(long signatureKeyHandle, byte[] data) throws
            IOException, PKCS11Exception {

        byte[] signature = null;
        if (getSession() < 0) {
            return null;
        }

        m_aLogger.log("Start single part sign operation...");
        pkcs11Module.C_SignInit(getSession(), this.signatureMechanism,
                                signatureKeyHandle);

        if ((data.length > 0) && (data.length < 1024)) {
        	m_aLogger.log("Signing ...");
            signature = pkcs11Module.C_Sign(getSession(), data);
            m_aLogger.log("FINISHED.");
        } else {
        	m_aLogger.log("Error in data length!");
        }

        return signature;

    }
    
    public String getSlotDescription(long slotID) {
        try {
            CK_SLOT_INFO slotInfo = pkcs11Module.C_GetSlotInfo(slotID);
            String s = new String(slotInfo.slotDescription);
            return s;
        } catch (PKCS11Exception ex) {
            return null;
        }
    }

    /**
     * Gets the current token.
     *
     * @return Returns the token handle
     */
    public long getTokenHandle() {
        return tokenHandle;
    }

    /**
     * Sets the current token handle.
     *
     * @param token
     *            the token handle to set.
     */
    public void setTokenHandle(long token) {
        this.tokenHandle = token;
    }
    
    /**
     * Lists currently inserted tokens.
     * Questo metodo è public e utilizzato in ReadCertsTask
     *
     * @throws PKCS11Exception
     */

    public long[] getTokens() throws PKCS11Exception {

        long[] tokenIDs = null;
        //get only slots with a token present
        tokenIDs = pkcs11Module.C_GetSlotList(true);

        //m_aLogger.info(tokenIDs.length + " tokens found.");

        return tokenIDs;
    }

    /**
     * Initializes cryptoki library operations.
     *
     * @throws PKCS11Exception
     */
    private void initializeLibrary() throws PKCS11Exception {
        m_aLogger.info("Initializing module ... ");
        pkcs11Module.C_Initialize(null);
        m_aLogger.info("initialized.");
    }
	
    /**
     * Gets currently loaded cryptoky description.
     *
     * @throws PKCS11Exception
     */
    public void getModuleInfo() throws PKCS11Exception {
        m_aLogger.info("getting PKCS#11 module info");
        CK_INFO moduleInfo = pkcs11Module.C_GetInfo();
        m_aLogger.info(moduleInfo.toString());
    }


    public void setMechanism(long mechanism, Object pParameter) {
        this.signatureMechanism = new CK_MECHANISM();

        this.signatureMechanism.mechanism = mechanism;
        this.signatureMechanism.pParameter = pParameter;

    }

    public void setMechanism(long mechanism) {
        this.setMechanism(mechanism, null);

    }

    /**
     * Gets informations on cryptographic operations supported by the tokens.
     *
     * @throws PKCS11Exception
     */
    public void getMechanismInfo() throws PKCS11Exception {
        CK_MECHANISM_INFO mechanismInfo;

        m_aLogger.info("\ngetting mechanism list...");
        long[] slotIDs = getTokenList();
        for (int i = 0; i < slotIDs.length; i++) {
            m_aLogger.info("getting mechanism list for slot " + slotIDs[i]);
            long[] mechanismIDs = pkcs11Module.C_GetMechanismList(slotIDs[i]);
            for (int j = 0; j < mechanismIDs.length; j++) {
                m_aLogger.info("mechanism info for mechanism id "
                            + mechanismIDs[j] + "->"
                            + Functions.mechanismCodeToString(mechanismIDs[j])
                            + ": ");
                mechanismInfo = pkcs11Module.C_GetMechanismInfo(slotIDs[i],
                        mechanismIDs[j]);
                m_aLogger.info(mechanismInfo.toString());
            }
        }

    }    

	/**
	 * @param tokenHandle2
	 */
	public void getMechanismInfo(long tokenHandle2) throws PKCS11Exception {
		// TODO Auto-generated method stub
		CK_MECHANISM_INFO mechanismInfo;

		m_aLogger.info("Getting mechanism list...");
		m_aLogger.info("getting mechanism list for token " + tokenHandle2);
		long[] mechanismIDs = pkcs11Module.C_GetMechanismList(tokenHandle2);
		for (int j = 0; j < mechanismIDs.length; j++) {
			m_aLogger.info("mechanism info for mechanism id " + mechanismIDs[j] + "->"
					+ Functions.mechanismCodeToString(mechanismIDs[j]) + ": ");
			mechanismInfo = pkcs11Module.C_GetMechanismInfo(tokenHandle2, mechanismIDs[j]);
			m_aLogger.info(mechanismInfo.toString());
		}
	}	

	/**
	 * @param logger
	 */
	private void setLogger(IDynamicLogger aLogger) {
    	if(aLogger == null)
    		m_aLogger = new DynamicLazyLogger();
    	else {
    		if(aLogger instanceof DynamicLogger)
    			m_aLogger = (DynamicLogger)aLogger;
    		else if(aLogger instanceof DynamicLoggerDialog)
        			m_aLogger = (DynamicLoggerDialog)aLogger;
    	}
    	m_aLogger.enableLogging();
	}

	/**
	 * @param manufacturer
	 * @param description
	 * @param tokenSerialNumber
	 * @return TODO
	 */
	public boolean isTokenPresent(String manufacturer, String description,
			String tokenSerialNumber) {
		// TODO Auto-generated method stub
		//scan the token present
		
		return false;
	}

}
