/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  Copyright 2009 by Roberto Resoli resoli@osor.eu
 *  This code in partly derived from
 *  it.infocamere.freesigner.gui.RootsVerifier class in freesigner
 *  adapted to be unsed in OOo UNO environment
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

package it.plio.ext.oxsit.comp.security.ca;

import it.infocamere.freesigner.crl.CertificationAuthorities;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.ooo.ui.DialogQuery;
import it.plio.ext.oxsit.ooo.ui.DialogRootVerify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

public class RootsVerifier {
    
    private static RootsVerifier instance = null;
    
    private String CNIPADir = null;

    private String CAFilePath = null;

    private String CNIPACACertFilePath = null;

    private byte[] userApprovedFingerprint = null;
    private DynamicLoggerDialog	m_aLogger;

    private XComponentContext	m_xCC;
    private XFrame				m_xFrame;
    private XMultiComponentFactory m_xMCF;

    public RootsVerifier(XFrame _xFrame, XComponentContext _xContext) {
//        conf = Configuration.getInstance();
    	m_xCC = _xContext;
    	m_xFrame = _xFrame;
    	m_xMCF = m_xCC.getServiceManager();
    	m_aLogger = new DynamicLoggerDialog(this,_xContext);
//
    	m_aLogger.enableLogging();
        init();
        userApprovedFingerprint = getFingerprint();
    }
    
/*    public static RootsVerifier getInstance(){
        if(instance == null) {
            instance = new RootsVerifier();
         }
         return instance;
    }*/

    //beppec56:
    //FIXME needs some adjusting on the  location where the
    //file will be, or automatic determination of the file present there.
    private void init() {
        //grab the extension internal path
    	//the file is in a special directory
    	// when checking for update, the file will be copied in a local user directory
    	//we need to know what to do when the file doesn't exist or we need to update it from Internet
        //for the type being is absolute, will be given from configuration ? Or
        //do we devise a method to get it automatically?
        
        String _originPath;
			try {
				_originPath = Helpers.getExtensionInstallationSystemPath(m_xCC);
		        this.CAFilePath = _originPath+System.getProperty("file.separator") + 
							"ca-list-signed-p7m-it"+ //fixed path, the directory containing the current root zip file
							System.getProperty("file.separator")+
							"LISTACER_20090303.zip.p7m";
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				m_aLogger.severe(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				m_aLogger.severe(e);
			}
    }

    private byte[] getFingerprint() {

        byte[] fingerprint = null;

        CertStore certs = null;
        CMSSignedData CNIPA_CMS = null;
        try {
            CNIPA_CMS = getCNIPA_CMS();
        } catch (FileNotFoundException ex) {
            m_aLogger.severe("getFingerprint","Errore nella lettura del file delle RootCA: ",ex);
        } catch (CMSException e) {
            // TODO Auto-generated catch block
        	m_aLogger.severe("getFingerprint","Errore nel CMS delle RootCA: ",e);
        }

        Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        if (Security.getProvider(p.getName()) == null)
            Security.addProvider(p);

        try {
            certs = CNIPA_CMS.getCertificatesAndCRLs("Collection", "BC");
        } catch (CMSException ex2) {
        	m_aLogger.severe("getFingerprint","Errore nel CMS delle RootCA",ex2);
        } catch (NoSuchProviderException ex2) {
        	m_aLogger.severe("getFingerprint","Non esiste il provider del servizio",ex2);
        } catch (NoSuchAlgorithmException ex2) {
        	m_aLogger.severe("getFingerprint","Errore nell'algoritmo",ex2);
        }

        if (certs == null)
        	m_aLogger.severe("getFingerprint","No certs for CNIPA signature!");
        else {
            SignerInformationStore signers = CNIPA_CMS.getSignerInfos();
            Collection c = signers.getSigners();
            if (c.size() != 1) {
            	m_aLogger.severe("getFingerprint","There is not exactly one signer!");
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
                        	m_aLogger.severe("getFingerprint","There is not exactly one certificate for this signer!");

                    } catch (CertStoreException ex1) {
                    	m_aLogger.severe("Errore nel CertStore",ex1);
                    }
                }
            }
        }

        //grab the localized text to display
        String _format = "id_root_verify_message";
        String _title = "id_root_verify_message_title";
        String _no_fp = "id_root_verify_message_ko";
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xCC, m_xMCF);

		try {
			_title = m_aRegAcc.getStringFromRegistry( _title );
			_format = m_aRegAcc.getStringFromRegistry( _format );
			_no_fp = m_aRegAcc.getStringFromRegistry( _no_fp );
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		m_aRegAcc.dispose();

        String theFingerprint = ((fingerprint == null) ? _no_fp : formatAsGUString(fingerprint));
        String _mex = String.format(_format, theFingerprint);
        
		DialogRootVerify aDialog1 = new DialogRootVerify( m_xFrame, m_xCC, m_xMCF,_mex );
		//PosX e PosY devono essere ricavati dalla finestra genetrice (in questo caso la frame)
		//get the parent window data
		//the problem is that we get the pixel, but we need the logical pixel, so for now it doesn't work...
//			com.sun.star.awt.XWindow xCompWindow = m_xFrame.getComponentWindow();
//			com.sun.star.awt.Rectangle xWinPosSize = xCompWindow.getPosSize();
		int BiasX = 100;
		int BiasY = 30;
//			System.out.println("Width: "+xWinPosSize.Width+ " height: "+xWinPosSize.Height);
//			XWindow xWindow = m_xFrame.getContainerWindow();
//			XWindowPeer xPeer = xWindow.
//center the dialog

        short ret;
		try {
			aDialog1.initialize(BiasX,BiasY);
			ret = aDialog1.executeDialog();
	        // ret = 0: NO
	        // ret = 1: Yes
	        if (ret == 1) {
	        	return fingerprint;
	        }
		} catch (BasicErrorException e) {
			m_aLogger.severe(e);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
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

/*    private byte[] getBytesFromPath(String fileName) throws IOException {

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
    }*/

    private CMSSignedData getCNIPA_CMS() throws CMSException,
            FileNotFoundException {

        FileInputStream is = null;

        is = new FileInputStream(CAFilePath);

        return new CMSSignedData(is);
    }

/*public CertificationAuthorities getRoots(AbstractTask task) throws GeneralSecurityException, IOException {

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
    }*/

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
