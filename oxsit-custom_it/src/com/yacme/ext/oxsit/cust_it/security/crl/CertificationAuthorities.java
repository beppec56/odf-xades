/*************************************************************************
 * 
 *  This code is partly derived from
 *  it.infocamere.freesigner.crl.CertificationAuthorities class in freesigner
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

package com.yacme.ext.oxsit.cust_it.security.crl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

import com.sun.star.task.XStatusIndicator;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.IDynamicLogger;

/**
 * @author beppe
 *
 */
public class CertificationAuthorities {
    private boolean debug;

    private boolean useproxy = false;

    private boolean alwaysCrlUpdate;

    private String auth = null;

    //these are the CNIPA trusted root Certification Autorities, e.g. the one
    //that usually are at the top of the trust chain in certification path
    private HashMap<X500Principal, X509Certificate> authorities;

    //these are the certification authority imported by the user
    //but not 
    private HashMap<X500Principal, X509Certificate> m_aOtherCertificationAuthorities;

    //private X509CertRL crls;

    private String message;

    private	IDynamicLogger	m_aLogger;

	private XComponentContext m_xCC;

	private XStatusIndicator m_xStatus;

    static {
        Security
                .addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * Instantiate the class with an empty list of CA. If you need to add a CA,
     * use<BR>
     * the addCertificateAuthority method.<BR>
     * <BR>
     * Istanzia la classe con una lista delle CA vuota. Occorre usare
     * esplicitamente il metodo addCertificateAuthority per inserire nella lista
     * le CA volute.
     */
    public CertificationAuthorities(XStatusIndicator _xStatus, XComponentContext _cc) {
        authorities = new HashMap<X500Principal, X509Certificate>();

        m_aOtherCertificationAuthorities = new HashMap<X500Principal, X509Certificate>();

        debug = false;
        // debug = true;
        alwaysCrlUpdate = false;
        m_xCC = _cc;
        m_aLogger = new DynamicLogger(this,m_xCC);
        m_aLogger.enableLogging();
        m_xStatus = _xStatus;
    }

    /**
     * This loads CA certificates from a ZIP file<BR>
     * <BR>
     * 
     * Carica i certificati delle CA da file ZIP
     * 
     * @param is
     *            stream relative to ZIP file containing "valid" CA
     * @param debug
     *            if true, it shows debug messages during ZIP file parsing
     * @throws GeneralSecurityException
     *             if no CA is loaded
     * @throws IOException
     *             any error during ZIP file reading
     */
    public CertificationAuthorities(XStatusIndicator _xStatus, XComponentContext _cc, InputStream is, boolean debug)
            throws GeneralSecurityException, IOException {
        this(_xStatus,_cc);
        this.setDebug(debug);
        byte[] bcer = new byte[4096];
        ZipEntry ze = null;
        ZipInputStream zis = null;
        ByteArrayOutputStream bais = null;
        try {
            zis = new ZipInputStream(is);
//            trace("Lettura ZIP stream");
            while ((ze = zis.getNextEntry()) != null) {
                // lettura singola entry dello zip
                trace("Lettura ZIP entry " + ze.getName());

                if (!ze.isDirectory()) {
                    bais = new ByteArrayOutputStream(4096);
                    int read;
                    while ((read = zis.read(bcer, 0, bcer.length)) > -1) {
                        bais.write(bcer, 0, read);
                    }
                    bais.flush();
                    try {
                        addCertificateAuthority(bais.toByteArray());
                    } catch (GeneralSecurityException ge) {
                        trace("Certificato CA non valido: " + ze.getName()
                                + " - "+ge.getMessage());
                    }
                    bais.close();
                }
            }
        } catch (IOException ie) {
        	//FIXME may be we can continue on loading the other CAs ?
            trace("Fallita lettura dello ZIP: " + ie.getMessage());
            throw ie;
        } finally {
            try {
                zis.close();
            } catch (IOException ie) {
            }
        }

        //Please note: if authorities is empty, that means that we are not bound
        //to Italian law wrt the user certificate validity
        if (authorities.isEmpty() && m_aOtherCertificationAuthorities.isEmpty()) {
            trace("No CA was loaded");
            throw new GeneralSecurityException("No CA was loaded");
        }
        trace("Inseriti " + authorities.size() + " certificati CA");
    }

    /**
     * This loads CA certificates from a ZIP file present at the specified URL.<BR>
     * No debug message is shown.<BR>
     * Carica i certificati delle CA da un file ZIP presente all'indirizzo
     * specificato. <br>
     * Non vengono visualizzati i messaggi di debug
     * @param statusIndicator 
     * 
     * @param url
     *            URL where you can fin ZIP file containg CA
     * @param b 
     * @throws GeneralSecurityException
     *             if no CA is loaded
     * @throws IOException
     *             any error during ZIP file reading
     * @throws CMSException 
     * 
     */
    public CertificationAuthorities(XStatusIndicator statusIndicator, XComponentContext _cc,URL url) throws GeneralSecurityException,
            IOException, CMSException {
        this(statusIndicator, _cc,url, false);
    }

    /**
     * This loads CA certificates from a ZIP file present at the specified URL.<BR>
     * No debug message is shown.<BR>
     * Carica i certificati delle CA da un file ZIP presente all'indirizzo
     * specificato
     * @param statusIndicator 
     * 
     * @param _CmsFileURL
     *            URL where you can fin ZIP file containg CA
     * @param debug
     *            if true, it shows debug messages during ZIP file downloading
     *            and parsing
     * @throws GeneralSecurityException
     *             if no CA is loaded
     * @throws IOException
     *             any error during ZIP file reading
     */
    public CertificationAuthorities(XStatusIndicator statusIndicator, XComponentContext _cc, URL _CmsFileURL, boolean debug)
            throws GeneralSecurityException, IOException, CMSException {
        // da testare!!
        // this(new ZipInputStream(url.openStream()), debug);

        this(statusIndicator, _cc,getCmsInputStream(_CmsFileURL), debug);
    }

    //ROB duplicato del metodo in VerifyTask, da fattorizzare
    private static InputStream getCmsInputStream(URL url) {

        ByteArrayInputStream bais = null;
        try {
            CMSSignedData cms = new CMSSignedData(url.openStream());

            cms.getSignedContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            cms.getSignedContent().write(baos);
            bais = new ByteArrayInputStream(baos.toByteArray());
        } catch (CMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bais;
    }

    /**
     * Add the specified CA certificate to CA list: certificate can be coded
     * base64 or DER.
     * 
     * Aggiunge alla lista delle CA riconosciute la CA specificata dal
     * certificato cert il certificato può essere in base64 o in formato DER
     * 
     * @param cert
     *            CA certificate
     * @throws GeneralSecurityException
     *             if any error occurs during certificate parsing or if
     *             certificate is not issued by a valid CA
     */
    public void addCertificateAuthority(byte[] cert)
            throws GeneralSecurityException {
        X509Certificate caCert = null;
        Security.removeProvider("BC");
        try { // Estrazione certificato da sequenza byte
            caCert = (X509Certificate) readCert(cert);

//            trace("Verifico " + caCert.getSubjectDN());
            if (authorities.containsKey(caCert.getIssuerX500Principal())) {
                trace("Gia' inserito nella lista delle CA: "+ caCert.getIssuerDN().getName());
                return;
            }

            int ext = caCert.getBasicConstraints();
            if (ext == -1) {
                throw new CertificateException("Flag CA uguale a false: "+
                		caCert.getSubjectX500Principal().getName());
            }
            
            try {
                caCert.checkValidity();
            } catch (CertificateExpiredException cee) {
//                throw new CertificateException("certificato CA scaduto: "+caCert.getSubjectX500Principal().getName());
            	//the expired state will be displayed to the user in the GUI
            }
            catch (CertificateNotYetValidException cnyve) {
//                throw new CertificateException("Certificato CA non ancora valido: "+caCert.getSubjectX500Principal().getName());
            	//the non yet valid state will be displayed to the user in the GUI
            }
                
            if (caCert.getIssuerDN().equals(caCert.getSubjectDN())) {
                caCert.verify(caCert.getPublicKey());
                authorities.put(caCert.getIssuerX500Principal(), caCert);
                trace("Inserita CA: " + caCert.getIssuerX500Principal().getName("CANONICAL")+ " "+caCert.getIssuerX500Principal());
                statusText(""+caCert.getIssuerX500Principal().getName("CANONICAL"));
            } else {
                throw new CertificateException("Non self-signed: "+caCert.getSubjectX500Principal().getName());
            }
        } catch (GeneralSecurityException ge) {
            //trace(ge);
            throw ge;
        }
    }

    /**
     * 
     * Reads and generates certificate from a sequence of bytes in DER or base64
     * 
     * Legge un certificato Certificate da una sequenza di bytes in DER o base64
     * e genera il certificato
     * 
     * @param certByte
     *            sequence of bytes
     * @throws GeneralSecurityException
     *             if any error occurs during certificate parsing
     * @return Certificate
     */
    public static Certificate readCert(byte[] certByte)
            throws GeneralSecurityException {
        Certificate cert = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(certByte);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (bis.available() > 0) {
                cert = cf.generateCertificate(bis);
            }
        } catch (GeneralSecurityException ge) {
            throw ge;
        }

        return cert;
    }

    /**
     * Verifies the the given certificate is issued by a trusted CA
     * 
     * Verifica se il certificato e' stato emesso da una delle CA
     * riconosciute
     * 
     * @param userCert
     *            certificate to verify
     * @return true if the given certificate is issued by a CA, false otherwise
     */
    public boolean isAccepted(X509Certificate userCert) {
        try {
            return authorities.containsKey((userCert).getIssuerX500Principal());
        } catch (Exception e) {
            trace("isAccepted: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Activate or deactivate debug messages
     * 
     * Attiva o disattiva i messaggi di debug
     * 
     * @param debug
     *            if true, it shows debug messages
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Return the CA certificate specified as caName
     * 
     * Restituisce il certificato della CA specificata da <CODE>caName</CODE>
     * se presente nelle CA di root.
     * 
     * @param caName
     *            Principal DN of CA
     * @return certificate CA X.509 , null if CA is not present
     * @throws GeneralSecurityException
     */
    public X509Certificate getCACertificate(Principal caName)
            throws GeneralSecurityException {

        if (authorities.containsKey(caName)) {
            return (X509Certificate) authorities.get(caName);
        } else {
            String errMsg = "CA non presente nella root: " + caName;
            trace(errMsg);
            throw new GeneralSecurityException(errMsg);
        }
    }

    public X509Certificate getIssuerCertificate(X509Certificate aCert)
    throws GeneralSecurityException {
    	return getCACertificate(aCert.getIssuerX500Principal()); 
    }
    

    /**
     * Returns the number of CA Restituisce il numero delle CA riconosciute
     * dall'applicazione
     * 
     * @return the number of CA
     */
    public int getCANumber() {
        return authorities.size();
    }

    /**
     * Returns the CA list as a Set of String Fornisce la lista delle CA
     * riconosciute sotto forma di Set di stringhe
     * 
     * @return the list of CA
     */
    public Set<X500Principal> getCANames() {
        return authorities.keySet();
    }

    /**
     * Returns a Collection of CA Fornisce una Collection delle CA riconosciute
     * 
     * @return Collection of CA
     */
    public Collection<X509Certificate> getCA() {
        return authorities.values();
    }
    
    /** ****************** PRIVATE PART****************************************** */

    private void trace(String s) {
        if (debug) {
            m_aLogger.log(s);
        }
    }

    private void trace(Throwable t) {
        if (debug && t != null) {
        	m_aLogger.severe(t);
        }
    }

    private void trace(String _mex, Throwable t) {
        if (debug && t != null) {
        	m_aLogger.severe(_mex,t);
        }
    }
    
    private void statusText(String _mex) {
    	if(m_xStatus != null)
    		m_xStatus.setText(_mex);
    }
}
