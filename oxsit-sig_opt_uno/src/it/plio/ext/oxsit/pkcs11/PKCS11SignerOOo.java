/*************************************************************************
 * 
 *  This code is derived from
 *  it.trento.comune.j4sign.pcsc.PCSCHelper class in j4sign
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
import iaik.pkcs.pkcs11.wrapper.PKCS11;
import iaik.pkcs.pkcs11.wrapper.PKCS11Connector;
import iaik.pkcs.pkcs11.wrapper.PKCS11Constants;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLazyLogger;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;

import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * @author beppe
 *
 */
public class PKCS11SignerOOo {

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

	public PKCS11SignerOOo(IDynamicLogger aLogger, String pkcs11WrapLib, String cryptokiLib) throws
    IOException, TokenException {
		super(); // ????

		setLogger(aLogger);

		// log = out;
		cryptokiLibrary = cryptokiLib;

		m_aLogger.info("Initializing PKCS11SignerOOo...");

		m_aLogger.info("Trying to connect to PKCS#11 module: '"
				+ cryptokiLibrary + "' ...");

		pkcs11Module = PKCS11Connector.connectToPKCS11Module(cryptokiLibrary,
				pkcs11WrapLib);
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

        System.out.println("reading certificate bytes");

        byte[] certBytes = null;
        CK_ATTRIBUTE[] template = new CK_ATTRIBUTE[1];
        template[0] = new CK_ATTRIBUTE();
        template[0].type = PKCS11Constants.CKA_VALUE;
        pkcs11Module.C_GetAttributeValue(getSession(), certHandle, template);
        certBytes = (byte[]) template[0].pValue;

        return certBytes;
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
        m_aLogger.info("\nUser logged into session.");
    }
    
    /**
     * Opens a session on the token, logging in the user.
     *
     * @throws TokenException
     */
    public void openSession(char[] password) throws TokenException {
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
                            + Helpers.mechanismCodeToString(mechanismIDs[j])
                            + ": ");
                mechanismInfo = pkcs11Module.C_GetMechanismInfo(slotIDs[i],
                        mechanismIDs[j]);
                m_aLogger.info(mechanismInfo.toString());
            }
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
}
