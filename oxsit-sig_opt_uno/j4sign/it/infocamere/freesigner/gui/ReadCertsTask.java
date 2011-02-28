/**
 *	Freesigner - a j4sign-based open, multi-platform digital signature client
 *	Copyright (c) 2005 Francesco Cendron - Infocamere
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either version 2
 *	of the License, or (at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package it.infocamere.freesigner.gui;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import iaik.pkcs.pkcs11.*;
import iaik.pkcs.pkcs11.wrapper.*;
import it.infocamere.freesigner.crl.*;
import it.trento.comune.j4sign.examples.SwingWorker;
import it.trento.comune.j4sign.pcsc.*;
import it.trento.comune.j4sign.pkcs11.*;
import org.bouncycastle.cms.*;

import com.yacme.ext.oxsit.logging.DynamicLazyLogger;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;

/**
 * This task reads certificates from token Task di lettura dei certifati da
 * token
 * 
 * @author Francesco Cendron
 */

public class ReadCertsTask extends AbstractTask {

    private String filepath;

    private String cardDescription;

    private boolean isDownloadCRLForced = false;

    private int current;

    private String statMessage;

    private String CRLerror = "";

    private CertificationAuthorities CAroot = null;

    private boolean done;

    private boolean canceled;

    private boolean passed;

    private int differentCerts;

    private ArrayList signersList;

    private CMSSignedData cms;

    private Hashtable risultati;

    private PKCS11Signer helper;

    private long[] certs;

    private Hashtable<Integer, X509Certificate> certsOnToken;

    private PCSCHelper pcsc;

    private java.util.List cards;

    private CardInReaderInfo cIr;

    private java.lang.String cryptokiLib = null;

//    private java.io.PrintStream log = null;
    
    private IDynamicLogger m_aLogger;

    public static final int SIGN_MAXIMUM = 4;

    public static final int SIGN_INIT_SESSION = 0;

    public static final int SIGN_CERTIFICATE_INITDATA = 3;

    public static final int SIGN_ENCRYPT_DIGEST = 2;

    public static final int SIGN_DONE = 4;

    public static final int VERIFY_MAXIMUM = 2;

    public static final int VERIFY_INIT = 1;

    public static final int VERIFY_DONE = 2;

    public static final int RESET = 0;

    public static final int ERROR = -1;

    private RootsVerifier rootsVerifier = null;

    /**
     * Constructor<br>
     * <br>
     * Costruttore
     * 
     */

    public ReadCertsTask() {
        this(0);
    }

    public ReadCertsTask(int indexToken) {
    	this(null, "", indexToken);
    }

    /**
     * Constructor<br>
     * <br>
     * Costruttore
     * 
     * @param indexToken
     *            int
     */
    public ReadCertsTask( IDynamicLogger aLogger, String pkcs11WrapLib, int indexToken) {

    	setLogger(aLogger);
 
        certsOnToken = new Hashtable();
        signersList = new ArrayList();
        pcsc = new PCSCHelper(true);
        setLibForToken(indexToken);

        m_aLogger.info("Accesso alla carta con indexToken " + indexToken
                + " con lib " + cryptokiLib);

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
            // setStatus(SIGN_INIT_SESSION, "Accesso alla carta2...\n ");
            helper = new PKCS11Signer(m_aLogger, pkcs11WrapLib, cryptokiLib);

            long[] tokens = helper.getTokens();
            m_aLogger.info(tokens.length + " token rilevati con la lib "
                    + cryptokiLib);
            for (int i = 0; i < tokens.length; i++) {
            	m_aLogger.info("indice " + i + " ha tokenHandle " + tokens[i]
                        + " su " + tokens.length + "token trovati");
            	m_aLogger.info(helper.getSlotDescription((long) tokens[i]));
            }

            // int indexToken = 0;
            // if (m_nTokens.length > 1) {
            // BufferedReader in = new BufferedReader(new InputStreamReader(
            // System.in));
            // System.out.print("Numero di token da usare
            // [0-"+(m_nTokens.length-1)+"] : ");
            // indexToken = Integer.parseInt(in.readLine());
            // }
            helper.setTokenHandle(tokens[indexToken]);

            helper.openSession();

            certs = helper.findCertificates();
            differentCerts = certs.length;

        } catch (TokenException te) {
            // setStatus(ERROR, PKCS11Helper.decodeError(te.getCode()));
            // log.println(PKCS11Helper.decodeError(te.getCode()));
            // setStatus(ERROR, PKCS11Helper.decodeError(-1));
            setStatus(ERROR, "Errore");
            m_aLogger.severe(te);

            /*
             * catch (UnsatisfiedLinkError ule) { setStatus(ERROR, "Occorre
             * chiudere il browser\nprima di firmare nuovamente");
             * log.println(ule);
             */
        } catch (Exception e) {
            setStatus(ERROR, "Eccezione: " + e);
            m_aLogger.severe(e);
        }

    }

    /**
     * Constructor
     * 
     * @param cIr :
     *            Object containing information about card in reader
     */

    public ReadCertsTask(IDynamicLogger aLogger, String pkcs11WrapLib, CardInReaderInfo cIr) {
        this(aLogger, pkcs11WrapLib, cIr, false);
        detectTokens();
    }

    public ReadCertsTask(CardInReaderInfo cIr) {
        this(cIr, false);
        detectTokens();
    }

    public ReadCertsTask(CardInReaderInfo cIr, boolean isDownloadCRLForced,
            RootsVerifier rv) {
        this(cIr, isDownloadCRLForced);
        this.rootsVerifier = rv;
    }

    public ReadCertsTask(CardInReaderInfo cIr, boolean isDownloadCRLForced) {
    	this(null,"",cIr,isDownloadCRLForced);
    }
    
    /**
     * Constructor
     * 
     * @param cIr :
     *            Object containing information about card in reader
     * @param isDownloadCRLForced
     *            true if CRL is forced
     */
    public ReadCertsTask(IDynamicLogger aLogger, String pkcs11WrapLib, CardInReaderInfo cIr, boolean isDownloadCRLForced) {

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
            // if (m_nTokens.length > 1) {
            // BufferedReader in = new BufferedReader(new InputStreamReader(
            // System.in));
            // System.out.print("Numero di token da usare
            // [0-"+(m_nTokens.length-1)+"] : ");
            // indexToken = Integer.parseInt(in.readLine());
            // }

            cardDescription = cIr.getCard().getProperty("description");

        } catch (TokenException te) {
            // setStatus(ERROR, PKCS11Helper.decodeError(te.getCode()));
            // log.println(PKCS11Helper.decodeError(te.getCode()));
            // setStatus(ERROR, PKCS11Helper.decodeError(-1));
            setStatus(ERROR, "Errore");
            m_aLogger.severe(te);

            /*
             * catch (UnsatisfiedLinkError ule) { setStatus(ERROR, "Occorre
             * chiudere il browser\nprima di firmare nuovamente");
             * log.println(ule);
             */
        } catch (Exception e) {
            setStatus(ERROR, "Eccezione: " + e);
            m_aLogger.severe(e);
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
     * Set library for token<br>
     * <br>
     * Setta la libreria per il token
     * 
     * @param indexToken :
     * 
     */

    private void setLibForToken(int indexToken) {

        String s = pcsc.findLibForIndexToken(indexToken);
        setCryptokiLib(s);
        m_aLogger.info("Setting library " + s + " for token with indexToken: "
                + indexToken + "\n");

    }

    /**
     * This triggers the PCSC wrapper stuff; a {@link PCSCHelper}class is used
     * to detect reader and token presence, trying also to provide a candidate
     * PKCS#11 cryptoki for it.
     * 
     * @return true if a token with corresponding candidate cryptoki was
     *         detected.
     * @throws IOException
     * @param indexToken
     *            int
     */
    private boolean detectCardAndCriptoki(int indexToken) throws IOException {
        CardInfo ci = null;
        boolean cardPresent = false;
        m_aLogger.info("\n\n========= DETECTING CARD ===========");

        m_aLogger.info("Resetting cryptoki name");
        setCryptokiLib(null);

        m_aLogger.info("Trying to detect card via PCSC ...");
        // JNIUtils jni = new JNIUtils();
        // jni.loadLibrary("OCFPCSC1");
        // jni.loadLibrary("pkcs11wrapper");

        pcsc = new PCSCHelper(true);
        cards = pcsc.findCards();
        cardPresent = !cards.isEmpty();
        if (cardPresent) {
            ci = (CardInfo) cards.get(indexToken);
            m_aLogger.info("\n\n" + ci.getProperty("description"));
            m_aLogger.info("Setting library for token with indexToken: "
                    + indexToken + "\n");
            setCryptokiLib(ci.getProperty("lib"));

        } else {
        	m_aLogger.info("Sorry, no card detected!");
        }

        m_aLogger.info("=================================");

        return (getCryptokiLib() != null);
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

    /**
     * Returns the cryptoki library name.
     * 
     * @return java.lang.String
     */
    public java.lang.String getCryptokiLib() {
        return cryptokiLib;
    }

    /**
     * Returns the PKCS11 helper.
     * 
     * @return PKCS11Signer
     */
    public PKCS11Signer getPKCS11Signer() {
        return helper;
    }

    /**
     * Returns certificate handle with index i<br>
     * <br>
     * Ritorna l'handle del certificato con indice i
     * 
     * @param i :
     *            index of chosen certificate( it is chosen in a table)
     * @return long: cert handle
     */

    public long getCertHandle(int i) {
        return certs[i];

    }

    /**
     * Return certifate from handle<br>
     * <br>
     * Ritorna il certificato dall'handle
     * 
     * @param handle :
     * @return X509Certificate
     */
    public X509Certificate getCertFromHandle(long handle) {

        // get Certificate
        CertificateFactory cf = null;
        try {
            cf = java.security.cert.CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
        }
        ByteArrayInputStream bais = null;
        try {
            bais = new java.io.ByteArrayInputStream(helper
                    .getDEREncodedCertificate(handle));
        } catch (PKCS11Exception ex1) {
        }
        X509Certificate javaCert = null;
        try {
            javaCert = (java.security.cert.X509Certificate) cf
                    .generateCertificate(bais);
        } catch (CertificateException ex2) {
        }

        return javaCert;
    }

    boolean isDone() {
        return done;
    }

    boolean isCanceled() {
        return canceled;
    }

    boolean isPassed() {
        return passed;
    }

    /**
     * Return the number of different certificates present in token<br>
     * <br>
     * Ritorna il numero del differenti certificati presenti nel token
     * 
     * @return int
     */
    int getDifferentCerts() {
        return differentCerts;
    }

    /**
     * Returns String array of signers (used in combobox)<br>
     * <br>
     * restituisce un array di stringhe contenenti i firmatari (utilizzato nel
     * combo box)
     * 
     * @return String[]
     */
    String[] getSigners() {
        String[] s = new String[15];
        SignerInformationStore signers = cms.getSignerInfos();
        Collection c = signers.getSigners();

        Iterator it = signersList.listIterator();

        int i = 0;
        while (i != signersList.size()) {
            s[i] = signersList.get(i).toString();
            i++;
        }

        return s;
    }

    String getFilePath() {
        return filepath;
    }

    CertificationAuthorities getCAroot() {
        return CAroot;
    }

    String getCurrentSigner() {
        // ritorna solo l'ultimo
        return statMessage;
    }

    /**
     * Executes all verifications on certificate<br>
     * <br>
     * Esegue le verifiche sul certificato
     * 
     */

    void verify() {
        Security
                .addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        X509Certificate cert = null;

        passed = false;

        if (certsOnToken != null) {
            Collection certCollection = certsOnToken.values();
            Iterator it = certCollection.iterator();

            while (it.hasNext()) {
                // Iterator certIt = certCollection.iterator();
                // X509Certificate cert = (X509Certificate)
                // certIt.next();

                cert = (X509Certificate) it.next();
                System.out.println(" Verifiying signature from:\n"
                        + cert.getSubjectDN());
                CertValidity cv = new CertValidity(cert, CAroot,
                        isDownloadCRLForced);

                // inserisce in una lista i DN dei firmatari
                signersList.add(cert.getSubjectDN());

                passed = cv.getPassed();
                CRLerror = cv.getCRLerror();

                risultati.put(cert.getSubjectDN(), cv);
            }
            done = true;
        }

    }

    /**
     * Return Common name of given certificate<br>
     * <br>
     * Restituisce il CN del subjct certificato in oggetto
     * 
     * @param userCert
     *            certificate
     * @return CN
     */
    private static String getCommonName(X509Certificate userCert) {
        String DN = userCert.getSubjectDN().toString();
        int offset = DN.indexOf("CN=");
        int end = DN.indexOf(",", offset);
        String CN;
        if (end != -1) {
            CN = DN.substring(offset + 3, end);
        } else {
            CN = DN.substring(offset + 3, DN.length());
        }
        return CN;
    }

    /**
     * Detect m_nTokens with defined library<br>
     * <br>
     * Rileva i token con la libreria definita
     * 
     */

    public void detectTokens() {
        long[] tokens = null;
        try {
            tokens = helper.getTokens();
        } catch (PKCS11Exception ex3) {
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

    public void go() {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                detectTokens();

                current = 1;
                done = false;
                canceled = false;

                initCARoots();

                if (CAroot == null)
                    return null;

                risultati = new Hashtable();

                return new CertsFinder();
            }

        };
        worker.start();
    }

    // ROB caricamento CA dal file firmato CNIPA.
    /**
     * Inizializes CRL with CA file
     * 
     */

    public void initCARoots() {
        try {
            CAroot = this.rootsVerifier.getRoots(this);
        } catch (Exception ex) {
            setCanceled("Errore nell'inizializzazione delle CA: " + ex);
        }
    }

    /**
     * The actual long running task. This runs in a SwingWorker thread. The goal
     * of this task is finding all certificates on token
     */
    class CertsFinder {
        CertsFinder() {
        	m_aLogger.info("CertsFinder running...");
            byte[] certBytes = null;

            java.security.cert.X509Certificate javaCert = null;
            CertificateFactory cf = null;
            try {
                cf = java.security.cert.CertificateFactory.getInstance("X.509");
            } catch (CertificateException ex) {
            }
            java.io.ByteArrayInputStream bais = null;
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
                setStatus(i + 1, "Lettura certificato\n"
                        + getCommonName(javaCert));
                m_aLogger.info(javaCert.getSubjectDN().toString());

                certsOnToken.put(new Integer(i), javaCert);

            }

            verify();

        }

        protected void encryptDigestAndGetCertificate(String signerLabel,
                PKCS11Signer helper) throws CertificateException {

            byte[] encrypted_digest = null;

            setStatus(SIGN_ENCRYPT_DIGEST, "Generazione della firma ...");
            try {

                // helper.openSession(password);
                // helper.login(String.valueOf(password));
            	m_aLogger.info("User logged in.");

                long privateKeyHandle = -1L;
                long certHandle = -1;

                byte[] encDigestBytes = null;
                byte[] certBytes = null;

                m_aLogger.info("Searching objects from certificate key usage ...");

                certHandle = helper.findCertificateWithNonRepudiationCritical();

                if (certHandle > 0) {
                    privateKeyHandle = helper
                            .findSignatureKeyFromCertificateHandle(certHandle);
                    if (privateKeyHandle > 0) {
                        // if (getDigest() != null)
                        // encDigestBytes = helper.signDataSinglePart(
                        // privateKeyHandle, getDigest());
                        // else
                        // encDigestBytes = helper.signDataMultiplePart(
                        // privateKeyHandle, getDataStream());

                        certBytes = helper.getDEREncodedCertificate(certHandle);
                    }

                    // log.println("\nEncrypted digest:\n" +
                    // formatAsHexString(encDigestBytes));

                    // log.println("\nDER encoded Certificate:\n" +
                    // formatAsHexString(certBytes));

                    // setEncryptedDigest(encDigestBytes);
                    // setCertificate(certBytes);

                } else {
                	m_aLogger.info("\nNo private key corrisponding to certificate found on token!");
                }

            } catch (TokenException e) {
            	m_aLogger.info("sign() Error: " + e);
                // log.println(PKCS11Helper.decodeError(e.getCode()));
                // log.println(PKCS11Helper.decodeError(e.getCode()));

                // } catch (IOException ioe) {
                // log.println(ioe);
            } catch (UnsatisfiedLinkError ule) {
            	m_aLogger.severe(ule);
            } finally {
                if (helper != null) {
                    try {
                        helper.closeSession();
                        m_aLogger.info("Sign session Closed.");
                    } catch (PKCS11Exception e2) {
                    	m_aLogger.severe("","Error closing session: " + e2);
                    }

                    try {
                        helper.libFinalize();
                        m_aLogger.info("Lib finalized.");
                    } catch (Throwable e1) {
                        // TODO Auto-generated catch block
                    	m_aLogger.severe("","Error finalizing criptoki: ",e1);
                    }

                }
                helper = null;
                System.gc();
            }
        }
    }

    void setStatus(int status, String message) {
        this.current = status;
        this.statMessage = message;
    }

    void setMessage(String message) {

        this.statMessage = message;
    }

    void setCanceled(String message) {
        this.statMessage = message;
        this.canceled = true;
    }

    String getMessage() {
        return statMessage;
    }

    int getStatus() {
        return current;
    }

    X509Certificate getCAcert(X509Certificate c)
            throws java.security.GeneralSecurityException {

        return CAroot.getCACertificate(c.getIssuerX500Principal());
    }

    // restituisce il certificato corrispondente al subject DN passato

    X509Certificate getCert(String DN) throws CMSException,
            java.security.NoSuchProviderException,
            java.security.NoSuchAlgorithmException, CertStoreException {
        Collection certCollection = certsOnToken.values();
        Iterator it = certCollection.iterator();
        int i = 0;
        while (it.hasNext()) {
            X509Certificate c = (X509Certificate) it.next();
            if ((c).getSubjectDN().toString().equals(DN)) {
                return c;
            }

            i++;
        }
        return null;
    }

    // ROB
    /**
     * Finds the index associated to a certificate in this task.
     * 
     * Trova l'indice associato ad un certificato trovato in questo task.
     * 
     * 
     * @author Roberto Resoli
     */

    public int getCertIndex(String DN) {

        Iterator it = certsOnToken.entrySet().iterator();
        Map.Entry currEntry = null;
        while (it.hasNext()) {
            currEntry = (Map.Entry) it.next();

            if (((X509Certificate) currEntry.getValue()).getSubjectDN()
                    .toString().equals(DN)) {
                return ((Integer) currEntry.getKey()).intValue();
            }
        }

        return -1;
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
     * Return bytearray of file to sign<br>
     * <br>
     * Restituisce il bytearray del file da firmare
     * 
     * @return byte[]
     */
    byte[] getFile() {
        return (byte[]) cms.getSignedContent().getContent();
    }

    /**
     * Return hashtable with results<br>
     * <br>
     * Restituisce l'hashtable dei risultati
     * 
     * @return Hashtable
     */
    Hashtable getRisultati() {
        return risultati;
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
     * Return error message if any error occured during CRL download<br>
     * <br>
     * Restituisce il messaggio di errore CRLerror che capita durante il
     * download della CRL
     * 
     * @return String
     */
    public String getCRLerror() {

        return CRLerror;
    }

    /**
     * Return description of read card Restituisce la descrizione della carta
     * letta
     * 
     * @return String
     */
    public String getCardDescription() {

        return cardDescription;
    }

}
