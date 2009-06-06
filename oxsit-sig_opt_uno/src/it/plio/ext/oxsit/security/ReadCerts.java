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

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.CK_TOKEN_INFO;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
import it.plio.ext.oxsit.logging.DynamicLazyLogger;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.pcsc.CardInReaderInfo;
//import it.trento.comune.j4sign.pkcs11.PKCS11Signer;
import it.plio.ext.oxsit.pkcs11.CertificatePKCS11Attributes;
import it.plio.ext.oxsit.pkcs11.PKCS11Driver;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.star.task.XStatusIndicator;

/**
 * @author beppec56
 *
 */
public class ReadCerts {

    private boolean isDownloadCRLForced;
	private Hashtable<Integer, CertificatePKCS11Attributes> certsOnToken;
	private ArrayList signersList;
	private CardInReaderInfo cIr;
	private IDynamicLogger m_aLogger;
	private PKCS11Driver helper;
    private java.lang.String cryptokiLib = null;
    private long[] certs;
    private String cardDescription;
	private int current;
	private String statMessage;
    public static final int ERROR = -1;
    private int differentCerts;
    
    private	Hashtable<Long, PKCS11TokenAttributes>		m_oTokens = new Hashtable<Long, PKCS11TokenAttributes>();

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
        certsOnToken = new Hashtable<Integer, CertificatePKCS11Attributes>();
        signersList = new ArrayList();
        // pcsc = new PCSCHelper(true);
        setCryptokiLib(cIr.getLib());
        this.cIr = cIr;

        certs = null;
        helper = null;
        m_aLogger.info("Helper Class Loader: "
                + PKCS11Driver.class.getClassLoader());
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
            helper = new PKCS11Driver(m_aLogger, pkcs11WrapLib, cryptokiLib);

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
	 * Limited to a coupled: one Reader =-> one token
	 * 
	 * So:
	 * TODO add the possibility to have more than one token for a reader
	 * this is possible according to PKCS11 specification.
	 * 
	 * <br>
	 * Rileva i token con la libreria definita
	 * 
	 */
	public void detectTokens() {
		long[] tokens = null;
		try {
			tokens = helper.getTokens();
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
				readerFromPKCS11 = readerFromPKCS11.substring(0,
						readerFromPKCS11.length() - 1);
				// log.println(readerFromCiR + " = " + readerFromPKCS11 + "?");
				// log.println(readerFromCiR2 + " = " + readerFromPKCS112 +
				// "?");

				// riconoscimento lettore tramite name reader
				if ((readerFromPKCS11.startsWith(readerFromCiR))
						|| (readerFromCiR2.endsWith(readerFromPKCS112))) {

					PKCS11TokenAttributes aTk = new PKCS11TokenAttributes();

					CK_TOKEN_INFO aTkInfo = helper.getTokenInfo(tokens[i]);
					aTk.setTokenHandle(tokens[i]);
					String sString = new String(aTkInfo.label);
					aTk.setLabel(sString.trim());
					
					sString = new String(aTkInfo.manufacturerID);
					aTk.setManufacturerID(sString.trim());
					
					sString = new String(aTkInfo.model);
					aTk.setModel(sString.trim());

					sString = new String(aTkInfo.serialNumber);
					aTk.setSerialNumber(sString.trim());

					aTk.setMaxPinLen(aTkInfo.ulMaxPinLen);
					aTk.setMinPinLen(aTkInfo.ulMinPinLen);
//					m_aLogger.log("Token set: "+aTk.toString());

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
					m_oTokens.put(new Long(aTk.getTokenHandle()), aTk);
				}
			}
		} catch (PKCS11Exception ex3) {
			m_aLogger.severe("detectTokens, PKCS11Exception " + cryptokiLib,
					ex3);
		}
	}

    /**
     * Return a Collection of certificate present in token<br>
     * In the form of a 
     * <br>
     * restituisce una Collection dei certificati presenti nel token
     * attualmente selezionato nel driever (helper)
     * 
     * @return Collection
     */
    public Collection<CertificatePKCS11Attributes> getCertsOnToken() {
        byte[] certBytes = null;
        CertificatePKCS11Attributes oCertificate = null;

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
                	oCertificate = new CertificatePKCS11Attributes();
                	oCertificate.setCertificateValueDEREncoded(certBytes);
                	//get current token description
                	PKCS11TokenAttributes aTk = m_oTokens.get(new Long(helper.getTokenHandle()));
                	oCertificate.setToken(aTk);
                    bais = new java.io.ByteArrayInputStream(certBytes);
                    try {
                        javaCert = (java.security.cert.X509Certificate) cf
                                .generateCertificate(bais);
                        oCertificate.setCertificateValue(javaCert);
                    } catch (CertificateException ex1) {
                    }
                    //now get the additional certificate attribute from PKCS11 interface
                    oCertificate.setCertificateID(helper.getCertificateID(certs[i]));
                    oCertificate.setCertificateLabel(new String(helper.getCertificateLabel(certs[i])));
                    m_aLogger.info(javaCert.getSubjectDN().toString());
                } catch (PKCS11Exception ex2) {
                }
                if(oCertificate != null)
                	certsOnToken.put(new Integer(i), oCertificate);
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
