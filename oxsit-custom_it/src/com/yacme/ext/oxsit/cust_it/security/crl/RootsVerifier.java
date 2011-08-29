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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/security/crl/RootsVerifier.java.
 *
 * The Initial Developers of the Original Code are
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * Roberto Resoli resoli@osor.eu
 * 
 * Portions created by the Initial Developers are Copyright (C) 2009-2011
 * the Initial Developers. All Rights Reserved.
 * 
 * This code is partly derived from
 * it.infocamere.freesigner.crl.CertificationAuthorities class in freesigner
 * adapted to be used in OOo UNO environment
 * Copyright (c) 2005 Francesco Cendron - Infocamere

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

package com.yacme.ext.oxsit.cust_it.security.crl;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import com.yacme.ext.oxsit.ooo.ui.ControlDims;
import com.yacme.ext.oxsit.ooo.ui.DialogRootVerify;

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

	private X509Certificate m_aRootSignatureCert;

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
							ConstantCustomIT.m_sCA_LIST_SIGNED_FILE;
			} catch (URISyntaxException e) {
				m_aLogger.severe(e);
			} catch (IOException e) {
				m_aLogger.severe(e);
			}
    }

    //FIXME, add certificate revocation check for CNIPA cert
    //
    private byte[] getFingerprint() {

        byte[] fingerprint = null;

        CertStore certs = null;
        CMSSignedData CNIPA_CMS = null;
        try {
            CNIPA_CMS = getCNIPA_CMS();
        } catch (FileNotFoundException ex) {
            m_aLogger.severe("getFingerprint","Errore nella lettura del file delle RootCA: ",ex);
        } catch (CMSException e) {
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
                        	m_aRootSignatureCert = (X509Certificate)certCollection
                            .toArray()[0];
                            fingerprint = getCertFingerprint(m_aRootSignatureCert);
                        } else {
                        	//FIXME print an error?
                        	m_aLogger.severe("getFingerprint","There is not exactly one certificate for this signer!");
                        }
                    } catch (CertStoreException ex1) {
                    	//FIXME print an error?
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
//		DialogRootVerify aDialog1 = new DialogRootVerify( null, m_xCC, m_xMCF,_mex );
		//PosX e PosY devono essere ricavati dalla finestra genetrice (in questo caso la frame)
		//get the parent window data
		//the problem is that we get the pixel, but we need the logical pixel, so for now it doesn't work...
		int BiasX = ControlDims.RSC_SP_DLG_INNERBORDER_LEFT;
		int BiasY = ControlDims.RSC_SP_DLG_INNERBORDER_TOP;
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

    private CMSSignedData getCNIPA_CMS() throws CMSException,
            FileNotFoundException {

        FileInputStream is = null;

        is = new FileInputStream(CAFilePath);

        return new CMSSignedData(is);
    }

    private byte[] getCertFingerprint(X509Certificate cert) {
        MessageDigest md;
        byte[] fingerprint = null;
        try {

            md = MessageDigest.getInstance("SHA1");
            md.update(cert.getEncoded());

            fingerprint = md.digest();

        } catch (NoSuchAlgorithmException e) {
        	m_aLogger.severe(e);
        } catch (CertificateException e) {
        	m_aLogger.severe(e);
        }

        return fingerprint;
    }

    public byte[] getUserApprovedFingerprint() {
        return userApprovedFingerprint;
    }

	/**
	 * @return the m_aRootSignatureCert
	 */
	public X509Certificate getRootSignatureCert() {
		return m_aRootSignatureCert;
	}
}
