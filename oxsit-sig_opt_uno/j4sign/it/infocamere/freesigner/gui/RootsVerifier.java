package it.infocamere.freesigner.gui;

import it.infocamere.freesigner.crl.CertificationAuthorities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

public class RootsVerifier {
    
    private static RootsVerifier instance = null;
    
    private Configuration conf = null;

    private String CNIPADir = null;

    private String CAFilePath = null;

    private String CNIPACACertFilePath = null;

    private byte[] userApprovedFingerprint = null;

    private RootsVerifier() {
        conf = Configuration.getInstance();
        init();
        userApprovedFingerprint = getFingerprint();

    }
    
    public static RootsVerifier getInstance(){
        if(instance == null) {
            instance = new RootsVerifier();
         }
         return instance;
    }

    private void init() {

        File dir1 = new File(".");
        String curDir = null;
        try {
            curDir = dir1.getCanonicalPath();
        } catch (IOException ex1) {
            System.out.println("IOException in " + this.getClass().getName()
                    + "\n" + ex1);
        }

        this.CNIPADir = curDir + System.getProperty("file.separator") + "conf"
                + System.getProperty("file.separator") + conf.getCNIPA_dir()
                + System.getProperty("file.separator");

        this.CAFilePath = CNIPADir + conf.getCNIPA_roots();

        this.CNIPACACertFilePath = CNIPADir + conf.getCNIPA_ca();
    }

    private byte[] getFingerprint() {

        byte[] fingerprint = null;

        CertStore certs = null;
        CMSSignedData CNIPA_CMS = null;
        try {

            CNIPA_CMS = getCNIPA_CMS();

        } catch (FileNotFoundException ex) {
            System.out.println("Errore nella lettura del file delle RootCA: "
                    + ex);
        } catch (CMSException e) {
            // TODO Auto-generated catch block
            System.out.println("Errore nel CMS delle RootCA: " + e);
        }

        Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        if (Security.getProvider(p.getName()) == null)
            Security.addProvider(p);

        try {
            certs = CNIPA_CMS.getCertificatesAndCRLs("Collection", "BC");
        } catch (CMSException ex2) {
            System.out.println("Errore nel CMS delle RootCA");
        } catch (NoSuchProviderException ex2) {
            System.out.println("Non esiste il provider del servizio");
        } catch (NoSuchAlgorithmException ex2) {
            System.out.println("Errore nell'algoritmo");
        }

        if (certs == null)
            System.out.println("No certs for CNIPA signature!");
        else {
            SignerInformationStore signers = CNIPA_CMS.getSignerInfos();
            Collection c = signers.getSigners();
            if (c.size() != 1) {
                System.out.println("There is not exactly one signer!");
            } else {

                Iterator it = c.iterator();

                if (it.hasNext()) {
                    SignerInformation signer = (SignerInformation) it.next();
                    Collection certCollection = null;
                    try {
                        certCollection = certs.getCertificates(signer.getSID());

                        if (certCollection.size() == 1) {
                            fingerprint = getCertFingerprint((X509Certificate) certCollection
                                    .toArray()[0]);
                        } else
                            System.out
                                    .println("There is not exactly one certificate for this signer!");

                    } catch (CertStoreException ex1) {
                        System.out.println("Errore nel CertStore");
                    }
                }
            }
        }

        // ROB commentato per ora per evitare problemi di visualizzazione.

        if (JOptionPane.YES_OPTION == JOptionPane
                .showConfirmDialog(
                        null,
                        conf.getAcceptCAmsg()
                                + ((fingerprint == null) ? "impossibile calcolare l'impronta"
                                        : formatAsGUString(fingerprint)) + "\n",
                        "Impronta Certificato Presidente CNIPA",
                        JOptionPane.YES_NO_OPTION))
            return fingerprint;

        return null;
    }

    public String formatAsGUString(byte[] bytes) {
        int n, x;
        String w = new String();
        String s = new String();

        boolean separe = false;

        for (n = 0; n < bytes.length; n++) {
            x = (int) (0x000000FF & bytes[n]);
            w = Integer.toHexString(x).toUpperCase();
            if (w.length() == 1)
                w = "0" + w;
            // Group 2 consecutive bytes
            separe = (((n + 1) % 2) == 0) && (n + 1 != bytes.length);

            s = s + w + (separe ? " " : "");

        } // for
        return s;
    }

    private byte[] getBytesFromPath(String fileName) throws IOException {

        byte[] risultato = null;

        try {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead = 0;
            while ((bytesRead = fis.read(buffer, 0, buffer.length)) >= 0) {
                baos.write(buffer, 0, bytesRead);
            }
            fis.close();
            risultato = baos.toByteArray();

        } catch (IOException ioe) {

            throw ioe;
        }
        return risultato;
    }

    private CMSSignedData getCNIPA_CMS() throws CMSException,
            FileNotFoundException {

        FileInputStream is = null;

        is = new FileInputStream(CAFilePath);

        return new CMSSignedData(is);
    }

public CertificationAuthorities getRoots(AbstractTask task) throws GeneralSecurityException, IOException {

        CertificationAuthorities roots = null;
        boolean rootsOk = false;
        String error = null;

        try {

            CertificationAuthorities CNIPARoot = new CertificationAuthorities();
            try {
                CNIPARoot.addCertificateAuthority(CNIPARoot
                        .getBytesFromPath(this.CNIPACACertFilePath));
            } catch (GeneralSecurityException e) {
                log(task, "Errore nell'inizializzazione della CA CNIPA: "
                                + e);
            }

            X509Certificate cert = null;
            CertStore certs = null;

            CMSSignedData CNIPA_CMS = null;
            try {

                CNIPA_CMS = getCNIPA_CMS();

            } catch (FileNotFoundException ex) {
                log(task, "Errore nell'acquisizione del file: " + ex);
            }

            Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
            if (Security.getProvider(p.getName()) == null)
                Security.addProvider(p);

            try {
                certs = CNIPA_CMS.getCertificatesAndCRLs("Collection", "BC");
            } catch (CMSException ex2) {
                log(task, "Errore nel CMS delle RootCA");
            } catch (NoSuchProviderException ex2) {
                log(task, "Non esiste il provider del servizio");
            } catch (NoSuchAlgorithmException ex2) {
                log(task, "Errore nell'algoritmo");
            }

            if (certs != null) {
                SignerInformationStore signers = CNIPA_CMS.getSignerInfos();
                Collection c = signers.getSigners();

                System.out.println(c.size() + " signers found.");

                Iterator it = c.iterator();

                // ciclo tra tutti i firmatari
                int i = 0;
                while (it.hasNext()) {
                    SignerInformation signer = (SignerInformation) it.next();
                    Collection certCollection = null;
                    try {
                        certCollection = certs.getCertificates(signer.getSID());
                    } catch (CertStoreException ex1) {
                        log(task, "Errore nel CertStore");
                    }

                    if (certCollection.size() == 1) {

                        // task.setStatus(++current,
                        // "Verifica delle CA firmate dal CNIPA...");

                        byte[] signerFingerprint = getCertFingerprint((X509Certificate) certCollection
                                .toArray()[0]);

                        System.out.println("Signer fingerprint: "
                                + formatAsGUString(signerFingerprint));

                        if (Arrays.equals(signerFingerprint,
                                this.userApprovedFingerprint)) {

                            VerifyResult vr = new VerifyResult(
                                    (X509Certificate) certCollection.toArray()[0],
                                    CNIPA_CMS, CNIPARoot, signer, true);
                            rootsOk = vr.getPassed();
                            error = vr.getCRLerror();
                        } else
                            log(task, "Signer certs has wrong fingerprint!");
                    } else
                        log(task, "There is not exactly one certificate for this signer!");

                    i++;
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (rootsOk) {


                roots = new CertificationAuthorities(
                        getCmsInputStream(this.CAFilePath), true);

            

        } else {

            log(task, "Verifica del file CNIPA delle root CA fallita!");
            
        }

        return roots;

    }    

    private void log(AbstractTask task, String msg) {
        System.out.println("Verifica del file CNIPA delle root CA fallita!");
        if (task != null)
            task.setCanceled("Errore nella validazione delle root CA!");
    }

    private byte[] getCertFingerprint(X509Certificate cert) {
        MessageDigest md;
        byte[] fingerprint = null;
        try {

            md = MessageDigest.getInstance("SHA1");
            md.update(cert.getEncoded());

            fingerprint = md.digest();

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return fingerprint;
    }

    public byte[] getUserApprovedFingerprint() {
        return userApprovedFingerprint;
    }

    // ROB duplicato del metodo in VerifyTask ...
    private InputStream getCmsInputStream(String path) {

        FileInputStream is = null;
        try {
            is = new FileInputStream(path);
        } catch (FileNotFoundException ex) {
            System.out.println("Errore nell'acquisizione del file: " + ex);
        }
        ByteArrayInputStream bais = null;
        try {
            CMSSignedData cms = new CMSSignedData(is);

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

}
