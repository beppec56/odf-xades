/*************************************************************************
 * 
 *  This code is derived from
 *  it.infocamere.freesigner.gui.ReadCertstask class in freesigner
 *  adapted to be used in OOo UNO environment
 *  Copyright (c) 2005 Francesco Cendron - Infocamere
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

package it.plio.ext.oxsit.security;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import com.sun.star.task.XStatusIndicator;

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
import it.plio.ext.oxsit.logging.DynamicLazyLogger;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.pcsc.CardInReaderInfo;
import it.trento.comune.j4sign.pkcs11.PKCS11Signer;

/**
 * @author beppec56
 *
 */
public class ReadCerts {

    private boolean isDownloadCRLForced;
	private Hashtable certsOnToken;
	private ArrayList signersList;
	private CardInReaderInfo cIr;
	private IDynamicLogger m_aLogger;
	private PKCS11Signer helper;
    private java.lang.String cryptokiLib = null;
    private long[] certs;
    private String cardDescription;
	private int current;
	private String statMessage;
    public static final int ERROR = -1;
    private int differentCerts;

    private XStatusIndicator m_aStatus;
    
	/**
     * Constructor
     * 
     * @param cIr :
     *            Object containing information about card in reader
     */

    public ReadCerts(XStatusIndicator xStatus, IDynamicLogger aLogger, String pkcs11WrapLib, CardInReaderInfo cIr) {
        this(xStatus, aLogger, pkcs11WrapLib, cIr, false);
        detectTokens();
    }

    /**
     * Constructor
     * 
     * @param cIr :
     *            Object containing information about card in reader
     * @param isDownloadCRLForced
     *            true if CRL is forced
     */
    public ReadCerts(XStatusIndicator xStatus, IDynamicLogger aLogger, String pkcs11WrapLib, CardInReaderInfo cIr, boolean isDownloadCRLForced) {

    	setLogger(aLogger);
        this.isDownloadCRLForced = isDownloadCRLForced;
        certsOnToken = new Hashtable();
        signersList = new ArrayList();
        // pcsc = new PCSCHelper(true);
        setCryptokiLib(cIr.getLib());
        this.cIr = cIr;

        certs = null;
        helper = null;
        m_aLogger.info("Helper Class Loader: "
                + PKCS11Signer.class.getClassLoader());
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
            	m_aLogger.info("SecurityManager: " + sm);
            } else {
            	m_aLogger.info("no SecurityManager.");
            }
            // setStatus(SIGN_INIT_SESSION, "Accesso alla
            // carta...\n"+SIGN_INIT_SESSION+" "+
            // differentCerts);
            helper = new PKCS11Signer(m_aLogger, pkcs11WrapLib, cryptokiLib);

            // int indexToken = 0;
            // if (tokens.length > 1) {
            // BufferedReader in = new BufferedReader(new InputStreamReader(
            // System.in));
            // System.out.print("Numero di token da usare
            // [0-"+(tokens.length-1)+"] : ");
            // indexToken = Integer.parseInt(in.readLine());
            // }

            cardDescription = cIr.getCard().m_sDescription;

        } catch (TokenException te) {
            // setStatus(ERROR, PKCS11Helper.decodeError(te.getCode()));
            // log.println(PKCS11Helper.decodeError(te.getCode()));
            // setStatus(ERROR, PKCS11Helper.decodeError(-1));
            setStatus(ERROR, "Errore");
            m_aLogger.severe("TokenException",te);

            /*
             * catch (UnsatisfiedLinkError ule) { setStatus(ERROR, "Occorre
             * chiudere il browser\nprima di firmare nuovamente");
             * log.println(ule);
             */
        } catch (Exception e) {
            setStatus(ERROR, "Eccezione: " + e);
            m_aLogger.severe("Exception",e);
        }
    }

    /**
     * Detect tokens with defined library<br>
     * <br>
     * Rileva i token con la libreria definita
     * 
     */

    public void detectTokens() {
        long[] tokens = null;
        try {
            tokens = helper.getTokens();
        } catch (PKCS11Exception ex3) {
        	m_aLogger.severe("detectTokens, PKCS11Exception "+cryptokiLib,ex3);
        }
        m_aLogger.info(tokens.length + " token rilevati con la lib "
                        + cryptokiLib);
        // confronto tra la stringa reader di pcsc e quelle rilevate con la
        // libreria da helper
        for (int i = 0; i < tokens.length; i++) {
            String readerFromCiR = cIr.getReader();
            String readerFromPKCS11 = helper
                    .getSlotDescription((long) tokens[i]);
            String readerFromPKCS112 = readerFromPKCS11.replaceAll(" ", "");
            String readerFromCiR2 = readerFromCiR.replaceAll(" ", "");
            readerFromPKCS11 = readerFromPKCS11.substring(0, readerFromPKCS11
                    .length() - 1);
            // log.println(readerFromCiR + " = " + readerFromPKCS11 + "?");
            // log.println(readerFromCiR2 + " = " + readerFromPKCS112 + "?");

            // riconoscimento lettore tramite name reader
            if ((readerFromPKCS11.startsWith(readerFromCiR))
                    || (readerFromCiR2.endsWith(readerFromPKCS112))) {

            	m_aLogger.info("Settato token "
                        + helper.getSlotDescription((long) tokens[i]));
            	
            	statusText("Settato token "
                        + helper.getSlotDescription((long) tokens[i]));

            	helper.setTokenHandle(tokens[i]);
                try {
                    helper.openSession();
                } catch (TokenException ex1) {
                }

                try {
                    certs = helper.findCertificates();
                } catch (CertificateException ex2) {
                } catch (TokenException ex2) {
                }
                differentCerts = certs.length;

            }
        }
    }

    /**
     * Return a Collection of certificates present in token<br>
     * <br>
     * restituisce una Collection dei certificato presenti nel token
     * 
     * @return Collection
     */
    public Collection<X509Certificate> getCertsOnToken() {
        byte[] certBytes = null;

        java.security.cert.X509Certificate javaCert = null;
        CertificateFactory cf = null;

        m_aLogger.info("getCertsOnToken running ...");
        try {
            cf = java.security.cert.CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
        }
        java.io.ByteArrayInputStream bais = null;
        if (certs != null) {
            for (int i = 0; (i < certs.length); i++) {

            	m_aLogger.info("Generating certificate with handle: " + i + ") "
                        + certs[i]);

                try {
                    certBytes = helper.getDEREncodedCertificate(certs[i]);
                } catch (PKCS11Exception ex2) {
                }
                bais = new java.io.ByteArrayInputStream(certBytes);
                try {
                    javaCert = (java.security.cert.X509Certificate) cf
                            .generateCertificate(bais);
                } catch (CertificateException ex1) {
                }
                m_aLogger.info(javaCert.getSubjectDN().toString());

                certsOnToken.put(new Integer(i), javaCert);

            }
        } else {
            return null;
        }

        return certsOnToken.values();
    }

    /**
     * Close sessione of helper<br>
     * <br>
     * Chiude la sessione dell'helper
     * 
     */

    public void closeSession() {
        try {
            helper.closeSession();
        } catch (PKCS11Exception ex) {
        }
    }
    
    /**
     * Finalize helper
     * 
     */

    public void libFinalize() {
        try {
            helper.libFinalize();
            m_aLogger.info("Lib finalized.");
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
        	m_aLogger.info("Error finalizing criptoki: " + e1);
        }
        helper = null;
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
     * Sets the cryptoki library name.
     * 
     * @param newCryptokiLib
     *            String
     */
    private void setCryptokiLib(java.lang.String newCryptokiLib) {
        cryptokiLib = newCryptokiLib;
    }

    void setStatus(int status, String message) {
        this.current = status;
        this.statMessage = message;
    }

    void statusValue(int x) {
    	if ( m_aStatus != null)
    		m_aStatus.setValue(x);
    }
    
    void statusText(String s) {
    	if ( m_aStatus != null)
    		m_aStatus.setText(s);    	
    }
}
