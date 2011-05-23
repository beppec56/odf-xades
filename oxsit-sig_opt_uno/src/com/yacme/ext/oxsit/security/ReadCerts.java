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

package com.yacme.ext.oxsit.security;

import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.wrapper.CK_TOKEN_INFO;
import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;
//import it.trento.comune.j4sign.pkcs11.PKCS11Signer;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.sun.star.task.XStatusIndicator;
import com.yacme.ext.oxsit.logging.DynamicLazyLogger;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.pcsc.CardInReaderInfo;
import com.yacme.ext.oxsit.pkcs11.CertificatePKCS11Attributes;
import com.yacme.ext.oxsit.pkcs11.PKCS11Driver;

/**
 * @author beppec56
 *
 */
public class ReadCerts {

    private boolean isDownloadCRLForced;
	private Hashtable<Integer,Collection<CertificatePKCS11Attributes>> certs;
	private ArrayList signersList;
	private CardInReaderInfo cIr;
	private IDynamicLogger m_aLogger;
	private PKCS11Driver helper;
    private java.lang.String cryptokiLib = null;
    private String cardDescription;
	private int current;
	private String statMessage;
    public static final int ERROR = -1;
	long[] m_nTokens;
    
    
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
        
        signersList = new ArrayList();
        // pcsc = new PCSCHelper(true);
        setCryptokiLib(cIr.getLib());
        this.cIr = cIr;

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
            // if (m_nTokens.length > 1) {
            // BufferedReader in = new BufferedReader(new InputStreamReader(
            // System.in));
            // System.out.print("Numero di token da usare
            // [0-"+(m_nTokens.length-1)+"] : ");
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
	 * Detect m_nTokens with defined library<br>
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
		m_nTokens = null;
		try {
			m_nTokens = helper.getTokens();
			certs = new Hashtable<Integer,Collection<CertificatePKCS11Attributes>>();
			
			m_aLogger.info(m_nTokens.length + " token rilevati con la lib "
					+ cryptokiLib);
			// confronto tra la stringa reader di pcsc e quelle rilevate con la
			// libreria da helper,
			for (int i = 0; i < m_nTokens.length; i++) {
				String readerFromCiR = cIr.getReader();
				String readerFromPKCS11 = helper.getSlotDescription((long) m_nTokens[i]);
				String readerFromPKCS112 = readerFromPKCS11.replaceAll(" ", "");
				String readerFromCiR2 = readerFromCiR.replaceAll(" ", "");
				readerFromPKCS11 = readerFromPKCS11.substring(0, readerFromPKCS11.length() - 1);
				// log.println(readerFromCiR + " = " + readerFromPKCS11 + "?");
				// log.println(readerFromCiR2 + " = " + readerFromPKCS112 +
				// "?");

//FIXME, set the current reader name to the slot name
				m_aLogger.info("Slot description:"+ readerFromPKCS11);				
					//FIXME the exceptions here need reworking
					helper.setTokenHandle(m_nTokens[i]);
					try {
						m_aLogger.info(i+") Opening session on token with handle "+m_nTokens[i]);
						helper.openSession();
						long[] certHandles = helper.findCertificates();
						m_aLogger.info("\tExtracting certificates...");
						Collection certsOnToken = getCertsFromHandles(certHandles);
						certs.put(new Integer(i), certsOnToken);
						m_aLogger.info(i+") Closing session on token with handle "+m_nTokens[i]);
						helper.closeSession();
						
					} catch (CertificateException ex2) {
					} catch (TokenException ex2) {
					}
			}
		} catch (PKCS11Exception ex3) {
			m_aLogger.severe("detectTokens, PKCS11Exception " + cryptokiLib,
					ex3);
		}
	}

	public PKCS11TokenAttributes getTokenAttributes(long _nToken) {
		PKCS11TokenAttributes aTk = new PKCS11TokenAttributes();

		CK_TOKEN_INFO aTkInfo;
		try {
			aTkInfo = helper.getTokenInfo(_nToken);
			aTk.setTokenHandle(_nToken);
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

		} catch (PKCS11Exception e) {
			m_aLogger.severe(e);
		}
		return aTk;
	}
	public void setTokenHandle(long aHandle) {
		helper.setTokenHandle(aHandle);
	}

	public long[] getTokens() {
		return m_nTokens;
	}

	/**
	 * @return the helper
	 */
	public PKCS11Driver getHelper() {
		return helper;
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
    public Collection<CertificatePKCS11Attributes> getCertsFromHandles(long[] certHandles) {
        byte[] certBytes = null;
        CertificatePKCS11Attributes oCertificate = null;

        java.security.cert.X509Certificate javaCert = null;
        CertificateFactory cf = null;

        m_aLogger.info("getCertsFromHandles running ...");
        try {
            cf = java.security.cert.CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
        	m_aLogger.info("getCertsFromHandles CertificateException:"+ex);
        }
        java.io.ByteArrayInputStream bais = null;
        if (certHandles != null) {
        	Hashtable<Integer, CertificatePKCS11Attributes> certsOnToken = new Hashtable<Integer, CertificatePKCS11Attributes>();
            for (int i = 0; (i < certHandles.length); i++) {

            	m_aLogger.info("Generating certificate " + i + ") with handle: "
                        + certHandles [i]);

                try {
                    certBytes = helper.getDEREncodedCertificate(certHandles[i]);
                    m_aLogger.info("Got " + certBytes.length +" bytes.");
                	oCertificate = new CertificatePKCS11Attributes();
                	oCertificate.setCertificateValueDEREncoded(certBytes);
                    bais = new java.io.ByteArrayInputStream(certBytes);
                    try {
                        javaCert = (java.security.cert.X509Certificate) cf
                                .generateCertificate(bais);
                        oCertificate.setCertificateValue(javaCert);
                    } catch (CertificateException ex1) {
                    	m_aLogger.info("Certificate Exception ex1:" + ex1);
                    }
                    //now get the additional certificate attribute from PKCS11 interface
                    oCertificate.setCertificateID(helper.getCertificateID(certHandles[i]));
                    oCertificate.setCertificateLabel(new String(helper.getCertificateLabel(certHandles[i])));
                    m_aLogger.info(javaCert.getSubjectDN().toString());
                } catch (PKCS11Exception ex2) {
                	m_aLogger.info("Certificate Exception ex2:" + ex2);
                }
                if(oCertificate != null)
                	certsOnToken.put(new Integer(i), oCertificate);
            }
            return certsOnToken.values();
        } else {
            return null;
        }
    }
    
    public Collection<CertificatePKCS11Attributes> getCertsOnToken(int t) {	
    	return certs.get(new Integer(t));
    }
    /**
     * Close sessione of helper<br>
     * <br>
     * Chiude la sessione dell'helper
     * 
     */

    public void openSession() {
        try {
            helper.openSession();
        } catch (TokenException ex) {
        }
    }
    
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
