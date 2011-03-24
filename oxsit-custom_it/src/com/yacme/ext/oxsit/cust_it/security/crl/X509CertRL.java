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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/security/crl/X509CertRL.java.
 *
 * The Initial Developers of the Original Code are
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * Roberto Resoli resoli@osor.eu
 * 
 * Portions created by the Initial Developers are Copyright (C) 2009-2011
 * the Initial Developers. All Rights Reserved.
 * 
 * This code is partly derived from
 * it.infocamere.freesigner.crl.X509CertRL class in freesigner
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

import com.yacme.ext.oxsit.security.cert.CertificateState;
import com.yacme.ext.oxsit.security.cert.CertificateStateConditions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;

//import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.util.encoders.Base64;

import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.options.OptionsParametersAccess;

/**
 * @author beppec56
 *
 */
public class X509CertRL {

	private String CRLerror;
	private boolean debug;
	private CertificationAuthorities certAuths;
	private boolean useProxy;
	private HashMap<X500Principal, X509CRL>	crls;
	private XComponentContext m_xCC;
	private XMultiComponentFactory	m_xMCF;
	private XFrame	m_xFrame;
	private	XStatusIndicator	m_xStatusIndicator;

	private IDynamicLogger	m_aLogger;
	private IDynamicLogger	m_aLoggerDialog;
	private String reasonCode;
	private String auth;
	private CertificateState m_aCertificateState;
	private CertificateStateConditions m_aCertificateStateConditions;

	private	boolean	m_bOffLineOperation;
	private	boolean	m_bDisableCRLControl;
	private	boolean	m_bAlwaysDownloadCRL;

	protected OptionsParametersAccess m_xOptionsConfigAccess;
	private boolean m_bDisableOCSPControl;

	//FIXME some text inside the class needs localization
	
	/**
	 * 
	 * @param frame 
	 * @param _xcc 
	 * @param dbData
	 */
	public X509CertRL(XFrame frame, XComponentContext _xcc, CertificationAuthorities certAuths) {
		m_xCC = _xcc;
		m_xMCF = m_xCC.getServiceManager();
		m_xFrame = frame;
		m_aLogger = new DynamicLogger(this,m_xCC);

		setCertificateState(CertificateState.OK);
		setCertificateStateConditions(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED);
		//get configuration access, using standard registry functions
		getConfiguration();
		//
		m_aLogger.enableLogging();
		if(frame != null) {
			m_aLoggerDialog = new DynamicLoggerDialog(this,_xcc);
			m_aLoggerDialog.enableLogging();
		}

		crls = new HashMap();
        this.certAuths = certAuths;
        //debug = true;
        debug = true;
        useProxy = false;
        CRLerror = new String("");
	}

    /**
	 * 
	 */
	private void getConfiguration() {
		m_xOptionsConfigAccess = new OptionsParametersAccess(m_xCC);
		m_bOffLineOperation = m_xOptionsConfigAccess.getBoolean("OperationOffLine");
		m_bDisableOCSPControl = m_xOptionsConfigAccess.getBoolean("DisableOCSPControl");
		m_bDisableCRLControl = m_xOptionsConfigAccess.getBoolean("DisableCRLControl");
		m_bAlwaysDownloadCRL = m_xOptionsConfigAccess.getBoolean("ForceDownloadCRL");
		m_xOptionsConfigAccess.dispose();		
	}

    public void isNotRevokedOCSP(XStatusIndicator _aStatus, X509Certificate userCert, Date date) {
    	//first get the issuer certificate from the root CA data base
		getConfiguration();    	
    	Principal issuer = null;   	
		setCertificateStateConditions(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED);
		//Check if internet is enabled, if not enabled no control via OCSP or CRL
		//FIXME may be should drop to CRL which in torn will alert the user?
		if(m_bOffLineOperation) {
            setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROL_NOT_ENABLED);
    		setCertificateState(CertificateState.NOT_VERIFIABLE);
			return;
		}

		if(m_bDisableOCSPControl) {
			m_aLogger.log("OCSP control disabled, will try CRL");
			//simply return,
            setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROL_NOT_ENABLED);
    		setCertificateState(CertificateState.NOT_YET_VERIFIED);
    		//try the CRL
        	isNotRevokedCRL(_aStatus, userCert, date);
			return;
		}

        try {
        	issuer = (Principal) userCert.getIssuerX500Principal();
		} catch (Throwable e) {
//        	m_aLogger.severe(e);
        	//we drop here if the CA was not found, so, set error not verifiable
        	//and exit
    		setCertificateState(CertificateState.NOT_VERIFIABLE);
        	return;
		}

        try {
        	//FIXME: better exception subdivision
			X509Certificate issuerCert = certAuths.getCACertificate(issuer);
			OCSPQuery aQuery = new OCSPQuery(null, issuerCert);

			aQuery.addCertificate(userCert);
			
			m_aLogger.log("OCSP request sent for "+userCert.getSubjectDN().toString());
			aQuery.execute();

			int status = aQuery.certStatus(userCert);
			
			m_aLogger.log("OSCP query status returned: "+status+ " "+OCSPQuery.reasonText(status));

            setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
			switch (status) {
			case OCSPQuery.GOOD:
                setCertificateState(CertificateState.OK);
                break;
			default:
			case OCSPQuery.UNKNOWN:
                setCertificateState(CertificateState.NOT_YET_VERIFIED);
				break;
			case OCSPQuery.REVOKED:
	            setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.KEYCOMPROMISE:
                setCertificateState(CertificateState.REVOKED);
                break;
			case OCSPQuery.CACOMPROMISE:
                setCertificateState(CertificateState.REVOKED);
                break;
			case OCSPQuery.AFFILIATIONCHANGED:
                setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.SUPERSEDED:
                setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.CESSATIONOFOPERATION:
                setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.CERTIFICATEHOLD:
                setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.REMOVEFROMCRL:
                setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.PRIVILEGEWITHDRAWN:
                setCertificateState(CertificateState.REVOKED);
				break;
			case OCSPQuery.AACOMPROMISE:
                setCertificateState(CertificateState.REVOKED);
				break;
			}
        } catch (GeneralSecurityException e) {
        	m_aLogger.severe(e);
        	//got here if no OCSP or an error was found, try with CRL
        	isNotRevokedCRL(_aStatus, userCert, date);
        	return;
		} catch (OCSPQueryException e) {
        	m_aLogger.severe(e);
        	//got here if no OCSP or an error was found, try with CRL
        	isNotRevokedCRL(_aStatus, userCert, date);
        	return;
		} catch (NullPointerException e) {
//        	m_aLogger.severe(e);
			m_aLogger.log("OCSP info not found in certificate or error in trying, trying CRL...");
        	//got here if no OCSP or an error was found, try with CRL
        	isNotRevokedCRL(_aStatus, userCert, date);
        	return;
		} catch (Throwable e) {
        	m_aLogger.severe(e);
		}
    }

	/**
     *  Controls if the given certificate is revoked at the current date.<br><br>
     * Effettua il controllo di revoca sulla firma contenuta nel certificato userCert, rispetto alla data corrente
     * @param userCert certificate to verify
     * @return true if certificate is not revoked
     */
	public boolean isNotRevokedCRL(XStatusIndicator _aStatus, X509Certificate userCert) {
//reread the configuration parameters
        return isNotRevokedCRL(_aStatus,userCert, new Date());
	}

    /**
     * Controls if the given certificate is revoked at the specified date.
     * Effettua il controllo di revoca sulla firma contenuta nel certificato
     * userCert, rispetto alla data corrente<br><br>
     *
     * @param userCert certificate to verify
     * @param date Date
     * @return true if certificate is not revoked
     */
    public boolean isNotRevokedCRL(XStatusIndicator _aStatus, X509Certificate userCert, Date date) {

		setCertificateStateConditions(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED);

		X509CRL crl = null;
        //check if we have a status indicator
        m_xStatusIndicator = _aStatus;
		getConfiguration();        
        //check if CRL control is enabled
        if(m_bDisableCRLControl) {
            setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROL_NOT_ENABLED);
    		setCertificateState(CertificateState.NOT_VERIFIABLE);
        	return false;
        }

        try {
            // devo fare l'update per compatibilita' all'indietro!
            if (!update(userCert, date, m_bAlwaysDownloadCRL)) {

            	return false;
            } else {
                crl = (X509CRL) crls.get(userCert.getIssuerX500Principal());
            }
            X509CRLEntry entry = crl.getRevokedCertificate(userCert.
                    getSerialNumber());

            if (entry == null) {
                trace(
                        "Verifica di revoca del certificato effettuata correttamente" +
                        "\n***Fine Verifica CRL***");
                setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                setCertificateState(CertificateState.OK);
                return true;
            }

            if (crl.getVersion() >= 1) {
                // CRL versione 2 o superiore: prevede le extensions
                String reason = null;

                Date revDate = null;
                try {
                    revDate = entry.getRevocationDate();
                    byte[] extVal = entry.getExtensionValue("2.5.29.21");

                    if (extVal != null) {

                        trace("ReasonCode presente");

                        DERBitString dbs = new DERBitString(extVal);
                        reason = dbs.getString();

                        trace("ReasonCode trovato (DERBitString): " + reason);
                        if (reason.endsWith("0")) {
                            trace("unspecified(0)");
                            reasonCode = "in data "+revDate+" :\n unspecified(0)";
                        }
                        if (reason.endsWith("1")) {
                            trace("keyCompromise(1)");
                            reasonCode = "in data "+revDate+" :\n keyCompromise(1)";
                        }
                        if (reason.endsWith("2")) {
                            trace("cACompromise(2)");
                            reasonCode = "in data "+revDate+" :\n cACompromise(2)";
                        }
                        if (reason.endsWith("3")) {
                            trace("affiliationChanged(3)");
                            reasonCode = "in data "+revDate+" :\n affiliationChanged(3)";
                        }
                        if (reason.endsWith("4")) {
                            trace("superseded(4)");
                            reasonCode = "in data "+revDate+" :\n superseded(4)";
                        }
                        if (reason.endsWith("5")) {
                            trace("cessationOfOperation(5)");
                            reasonCode = "in data "+revDate+" :\n cessationOfOperation(5)";
                        }
                        if (reason.endsWith("8")) {
                            trace("removeFromCRL(8)");
                            reasonCode = "in data "+revDate+" :\n removeFromCRL(8)";
                        }
                        if (reason.endsWith("6")) { //ReasonFlags.CERTIFICATEHOLD
                            // il certificato e' sospeso ....
                            if (date.before(revDate)) {
                                trace(
                                        "Il certificato risulta sospeso alla data: " +
                                        revDate);
                                trace("data revoca " + revDate +
                                      " e data di controllo " + date);
                                reasonCode =
                                        "data revoca " + revDate +
                                        " e data di controllo " + date;
                                setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                                setCertificateState(CertificateState.SUSPENDED);
                                return true; // o false da decidere
                            } else {
                                trace(
                                        "Il certificato risulta sospeso in data: " +
                                        revDate);
                                reasonCode =
                                        "Il certificato risulta sospeso in data: " +
                                        revDate;
                                traceDialog(reasonCode);
                                setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                                setCertificateState(CertificateState.SUSPENDED);
                                return false;
                            }
                        }
                    }
                    // il certificato e' veramente revocato ....
                    if (date.before(revDate)) {
                        //non ancora revocato
                        trace("Il certificato risulta revocato dopo il " + date +
                              " (data di revoca: " + revDate);
                        reasonCode = "in futuro.\nIl certificato risulta revocato dopo il " +
                                     date +
                                     " (data di revoca: " + revDate;
                        traceDialog(reasonCode);
                        setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                        setCertificateState(CertificateState.REVOKED);
                        return true; // o false da decidere
                    } else {
                        trace("Il certificato risulta revocato in data: " +
                              revDate);
                        if (reasonCode==null){reasonCode =
                                "in data: " +
                                revDate;}
                        traceDialog(reasonCode);
                        setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                        setCertificateState(CertificateState.REVOKED);
                        return false;
                    }
                } catch (Throwable ex) {
                    trace(ex);
                    traceDialog(
                            "isNotRevoked - Errore nella lettura delle estensioni di revoca -> " +
                            ex.getMessage());

                    setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                    setCertificateState(CertificateState.NOT_YET_VERIFIED);
                    return false;
                }
                // la versione della CRL e' la uno e quindi non si può distinguere
                // la motivazione della revoca -> certificato revocato e basta.
            } else {
                trace("CRL V.1 : il certificato risulta revocato/sospeso");
//set state as revoked
                traceDialog("CRL V.1 : il certificato risulta revocato/sospeso");
                setCertificateStateConditions(CertificateStateConditions.REVOCATION_CONTROLLED_OK);
                setCertificateState(CertificateState.REVOKED);
                return false; // o false da decidere
            }
        } catch (Throwable e) {
            //trace(e);
            traceDialog("isNotRevoked - Errore generico nel metodo -> ", e);

            setCertificateStateConditions(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED);
            setCertificateState(CertificateState.NOT_YET_VERIFIED);
            return false;
        }
    }

    /**
     * Updates CRL if not present in cache or if present but expired or if download
     * is forced (flag forceUpdate set to true)<br><br>
     *
     * Aggiorna la CRL se non e' presente nella cache oppure se e' presente ma
     * e' scaduta oppure se e' stato impostato il download ad ogni verifica
     * tramite il flag forceUpdate.
     *
     * @param userCert certificate whose CRL is checked
     * @param date ckecks the validity of CRL according to this date
     * @param forceUpdate if true, it forces CRL download even if CRL in cache is not expired
     * @throws CertificateException if any error occurs during certificate parsing
     * @throws GeneralSecurityException
     * @return true if updating is successfully completed or if CRL in cache is
     * not expired and download is not forced
     */
    public boolean update(X509Certificate userCert, Date date,
                          boolean forceUpdate) throws CertificateException,
            GeneralSecurityException {
        X509CRL crl = null;
        trace("*** Inizio update CRL issuer: " + userCert.getIssuerDN() +
              ", forced: " + forceUpdate);
        Principal issuer = (Principal) userCert.getIssuerX500Principal();
        if (!forceUpdate) {
            if (crls.containsKey(userCert.getIssuerX500Principal())) {
                crl = (X509CRL) crls.get(userCert.getIssuerX500Principal());
                trace("CRL gia' scaricata, controllo nextUpdate: " +
                      crl.getNextUpdate());
                forceUpdate = (crl.getNextUpdate().before(date));
            } else {
                //FIXME: before attempting the download check if a CRL file with the name derived from
                //one of the the distribution point string is available in the user's file system cache.
                //if available, load it, recheck the dates and behave accordingly
            	crl = loadCRLFromPersistentStorage(userCert);
            	if(crl == null)
            		forceUpdate = true;
            	else {
            		//insert into the application internal cache and recheck the dates
                    forceUpdate = (crl.getNextUpdate().before(date));
                    if(!forceUpdate) {
	                    int verCode = check(crl, certAuths.getCACertificate(issuer), date);
	                    if (verCode != 0) {
	                       //CRL broken, so CRLerror reset:
	                        setCertificateStateConditions(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED);
	                    	forceUpdate = true;
	                    } else {//no error, load CRL in internal cache
	                        trace("Added to cache CRL of: " +
	                              userCert.getIssuerDN());
	                        crls.put(userCert.getIssuerX500Principal(), crl);
	                        trace("CRL was in persistent storage, check nextUpdate: " +
	                                crl.getNextUpdate());
	                    }
                    }
                    else
                        trace("The CRL loaded from persistent storage is stale, download forced");                    	
            	}
            }
        }

        if (forceUpdate) {
        	//first check if iINternet is enabled
        	if(m_bOffLineOperation) {
        		setCertificateStateConditions(CertificateStateConditions.INET_ACCESS_NOT_ENABLED);
        		setCertificateState(CertificateState.NOT_VERIFIABLE);
        		return false;
        	}
        	//check if the Issuer is in CARoot
        	try {
        		certAuths.getCACertificate(issuer);
        	} catch (GeneralSecurityException e) {
            	//if not present, do not download, simply set the right error and exits
        		setCertificateStateConditions(CertificateStateConditions.CRL_CANNOT_BE_ACCESSED);
        		m_aLogger.log("CA not found");
        		return false;
        	}
        	trace("(01) Inizio download CRL...");
            if ((crl = download(userCert)) == null) {
        		setCertificateStateConditions(CertificateStateConditions.INET_ACCESS_ERROR);
                return false;
            }
            //verifica CRL
            trace("(02) Inizio verifica della CRL...");
            //lancia GeneralSecurityEx se issuer non è presente in certAuths
            //cioè se la CA non è in root
            int verCode = check(crl, certAuths.getCACertificate(issuer), date);

            if (verCode != 0) {
               //CRLerror già settato in check()
                return false;
            } else {
                trace("Inserimento nella cache della CRL di: " +
                      userCert.getIssuerDN());
                crls.put(userCert.getIssuerX500Principal(), crl);
                return true;
            }
        } else {
            trace("CRL nella cache valida");
            return true;
        }
    }

    /**
     * Method look up if a CRL file with the name derived from
     * one of the the distribution point strings is available in the user's OOo file system
     * storage
     * if available, load it and returns
 	 * @param userCert
	 * @return the CRL loaded from persistent storage, or null if not existent
	 */
	private X509CRL loadCRLFromPersistentStorage(X509Certificate userCert) {
		try {
			String filesep = System.getProperty("file.separator");
			String aCRLCachePath = Helpers.getCRLCacheSystemPath(m_xCC);
			//first check if there is a storage cache
			File aStorDir = new File(aCRLCachePath);
			if(aStorDir.exists()) {
				//iterate through the distribution points
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				// ciclo sui distribution point presenti nel certificato utente
				X509CRL crl = null;
				//storage exists or was created, form the CRL file name of the CRL from the distribution point
				String aFileName = aCRLCachePath+filesep+getIssuerMD5Hash(userCert)+".crl";
				m_aLogger.log("crl storage: "+aFileName);
				//now save the CRL, if there is one, remove it first

				try {
					FileInputStream fos = new FileInputStream(aFileName);
					crl = (X509CRL) cf.generateCRL(fos);
					//crl loaded							
					return crl;
				} catch (FileNotFoundException e) {
					//expected exception, mean we don't have the file
				} catch (IOException e) {
					//something went wrong, continue to cicle
					m_aLogger.warning("", "Could not load a CRL from persistent storage, file non existent", e);
				} catch (CRLException e) {
					//something went wrong, continue to cicle
					m_aLogger.warning("", "Could not load a CRL from persistent storage", e);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			m_aLogger.severe(e);
		} catch (URISyntaxException e) {
			m_aLogger.severe(e);
		} catch (IOException e) {
			m_aLogger.severe(e);
		} catch (CertificateParsingException e) {
			m_aLogger.severe(e);
		} catch (CertificateException e) {
			m_aLogger.severe(e);
		} catch (Exception e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	/**
     * Checks validity of CRL of the specified CA at the specified date<br><br>
     *
     * Controlla la validita' di una CRL rispetto ad una specifica CA ed ad una data prefissata
     *
     * @param crl CRL to check
     * @param caCert CA certificate that should have signed CRL
     * @param date ckecks the validity of CRL according to this date
     * @throws CertificateException if any error occurs during DN parsing
     * @return int: 0 CRL is valid, else use getMessage() to check error message
     */
    public int check(X509CRL crl, X509Certificate caCert, Date date) throws
            CertificateException {
        // controllo che l'issuer della CRL corrisponda a quello utente
        // bisogna controllarlo tra classi omogenee SUN --- SUN oppure BALTIMORE --- BALTIMORE
        // controllato come X500Principal
        Principal caName = caCert.getIssuerX500Principal();

        Principal crlIssuer = crl.getIssuerX500Principal();

        trace("Controllo Issuers...");
        if (!crlIssuer.equals(caName)) {
            trace(
                    "isNotRevoked - CA emettitrice CRL diversa da quella dell'utente");
            trace("CRL Issuer: " + crlIssuer.getClass().getName() + " " +
                  crlIssuer);
            CRLerror="Errore: CA emettitrice CRL\n diversa da quella dell'utente";
            traceDialog(CRLerror);
            setCertificateStateConditions(CertificateStateConditions.CRL_CANNOT_BE_VERIFIED);            
            return 5;
        }
        trace("Controllo validita' temporale...");
        ///rfc2560
        // - thisUpdate: The time at which the status being indicated is known
        //          to be correct
        // - nextUpdate: The time at or before which newer information will be
        //          available about the status of the certificate
        //4.2.2.1  Time
        // The thisUpdate and nextUpdate fields define a recommended validity
        // interval. This interval corresponds to the {thisUpdate, nextUpdate}
        // interval in CRLs. Responses whose nextUpdate value is earlier than
        // the local system time value SHOULD be considered unreliable.
        // Responses whose thisUpdate time is later than the local system time
        // SHOULD be considered unreliable. Responses where the nextUpdate value
        // is not set are equivalent to a CRL with no time for nextUpdate (see
        // Section 2.4).

        if (crl.getNextUpdate().before(date)) {
            traceDialog("isNotRevoked - CRL con next update: " + crl.getNextUpdate() +
                  ", controllo alle: " + date);
            CRLerror = "Errore: CRL con next update:\n" +
                       crl.getNextUpdate() +
                       ",\ncontrollo alle:\n" + date;
            traceDialog(CRLerror);
            setCertificateStateConditions(CertificateStateConditions.CRL_CANNOT_BE_VERIFIED);
            return 3;
        }
        else
        	trace("CRL next update previsto dalla CA: "+crl.getNextUpdate());
        trace("Controllo validita' firma...");
        try {
            crl.verify(caCert.getPublicKey());
            return 0;
        } catch (GeneralSecurityException ge) {
            trace(ge);
            trace("isNotRevoked - Verifica della firma della CRL fallita -> " +
                  ge.getMessage());
            CRLerror = "Errore: Verifica della firma\ndella CRL fallita.";
            traceDialog(CRLerror);
            setCertificateStateConditions(CertificateStateConditions.CRL_CANNOT_BE_VERIFIED);
            return 6;
        }
        //FIXME: May be we need to add a check if the crl is signed by a known CA (from CA root array)
    }

	private static String decodeAGeneralName(GeneralName genName) throws IOException {
        switch (genName.getTagNo())
        {
            //only URI are used here, the other protocols are ignored
        case GeneralName.uniformResourceIdentifier:
        	return ((DERString)genName.getName()).getString();
        case GeneralName.ediPartyName:
        case GeneralName.x400Address:
        case GeneralName.otherName:
        case GeneralName.directoryName:
        case GeneralName.dNSName:
        case GeneralName.rfc822Name:            
        case GeneralName.registeredID:
        case GeneralName.iPAddress:
        	break;
        default:
        	throw new IOException("Bad tag number: " + genName.getTagNo());
        }
		return null;
	}
    
    public static String[] getCrlDistributionPoint(X509Certificate certificate) throws
    CertificateParsingException {
    	try {
    		//trova i DP (OID="2.5.29.31") nel certificato
    		DERObject obj = getExtensionValue(certificate, "2.5.29.31");

    		if (obj == null) {
    			//nessun DP presente
    			return null;
    		}
    		CRLDistPoint	crldp = CRLDistPoint.getInstance(obj);
    		DistributionPoint[] dp = crldp.getDistributionPoints();
    		String []urls=new String[5];

    		int p = 0;
    		for(int i = 0;i < dp.length;i++) {
    			DistributionPointName dpn = dp[i].getDistributionPoint();
    			//custom toString
    			if(dpn.getType() == DistributionPointName.FULL_NAME) {
    				//stx = stx+"fullName:" + term;
    			}
    			else {
    				//stx = stx+"nameRelativeToCRLIssuer:" + term;						
    			}

    			GeneralNames gnx = GeneralNames.getInstance(dpn.getName());
    			GeneralName[] gn = gnx.getNames();

    			for(int y=0; y <gn.length;y++) {
    				String aNm = decodeAGeneralName(gn[y]);
    				if(aNm != null) {
						urls[p++] = aNm;
    				}
    			}
    		}
    		return urls;
    	} catch (Throwable e) {
    		e.printStackTrace();
    		throw new CertificateParsingException(e.toString());
    	}
    }

    //FIXME: ROb, guarda se si può togliere, in un caso, con un certificato con protocollo
    //non stringa, non funzionava
    public static String[] getCrlDistributionPoint_Rob(X509Certificate certificate) throws
            CertificateParsingException {
        try {
            //trova i DP (OID="2.5.29.31") nel certificato
            DERObject obj = getExtensionValue(certificate, "2.5.29.31");

            if (obj == null) {
                //nessun DP presente
                return null;
            }
            ASN1Sequence distributionPoints = (ASN1Sequence) obj;

            String []urls=new String[5];
            String url;
            int p = 0;

            for (int i = 0; i < distributionPoints.size(); i++) {
                ASN1Sequence distrPoint = (ASN1Sequence) distributionPoints.
                                          getObjectAt(i);

                for (int j = 0; j < distrPoint.size(); j++) {
                    ASN1TaggedObject tagged = (ASN1TaggedObject) distrPoint.
                                              getObjectAt(j);
                    //0 � il tag per il DP
                    if (tagged.getTagNo() == 0) {
                        url = getStringFromGeneralNames(tagged.getObject());
                        if (url != null) {
                            urls[p++] = url;
                        }
                    }
                }
            }
            return urls;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new CertificateParsingException(e.toString());
        }
    }

    /**
     * Returns DERObject extension if the certificate corresponding to given OID<br><br>
     * Restituisce un estensione DERObject dal certificato, corrispoendente
     * all'OID
     *
     * @param cert certificate
     * @param oid String
     * @throws IOException
     * @return l'estensione
     */
    private static DERObject getExtensionValue(X509Certificate cert, String oid) throws
            IOException {
        byte[] bytes = cert.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(
                bytes));
        ASN1OctetString otteti = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(otteti.getOctets()));
        return aIn.readObject();
    }

    private static String getStringFromGeneralNames(DERObject names) {
        ASN1Sequence namesSequence = ASN1Sequence.getInstance((ASN1TaggedObject)
                names, false);
        if (namesSequence.size() == 0) {
            return null;
        }
        DERTaggedObject taggedObject
                = (DERTaggedObject) namesSequence.getObjectAt(0);
        return new String(ASN1OctetString.getInstance(taggedObject, false).
                          getOctets());

    }

    /**
     * Downloads CRL of the given certificate<br><br>
     * Scarica la CRL relativa al certificato in oggetto
     *
     * @param userCert certificate
     * @throws CertificateParsingException
     * @return la CRL relativa al certificato
     */
    public X509CRL download(X509Certificate userCert) throws
            CertificateParsingException {
        X509CRL crl = null;
        String sIssuer =Helpers.getIssuerName(userCert);
        String sMex = "Download CRL: "+ sIssuer;
        trace("Inizio download CRL per il cert: " + userCert.getSerialNumber() +
              ", emesso da: " + userCert.getIssuerDN());
        // URL[] dp = getCrlDistributionPointOLD(userCert);
        String[] dp = getCrlDistributionPoint(userCert);
        if (dp == null) {
            trace("Nessun punto distribuzione CRL disponibile");
            CRLerror = "Nessun punto distribuzione CRL disponibile.";
            return null;
        }
        // ciclo sui distribution point presenti nel certificato utente

        int p = 0;
        while (dp[p] != null) {
            p++;
        }

        trace(p + " distribution points found.");

        for (int i = 0; i < p; i++) {
            try {
                trace("Try to access the CRL Distribution Point = " +
                      dp[i]);
                statusText(sMex+" in Internet...");

                crl = download(dp[i], userCert.getIssuerDN());
                // il primo protocollo che dia esiti positivi interrompe il ciclo
                if (crl != null) {
                    trace("CRL downloaded correctly");
                    //so now, save the CRL into persistent cache storage
                    //first check if thereis already a storage, if not then create one
            		try {
            			String filesep = System.getProperty("file.separator");
            			String aCRLCachePath = Helpers.getCRLCacheSystemPath(m_xCC);
            			//first check if there is a storage cache
            			File aStorDir = new File(aCRLCachePath);
            			if(!aStorDir.exists()) {
            				//create the storage path
            				if(!aStorDir.mkdirs()) {
                				m_aLogger.warning("Path: "+aCRLCachePath+ " cannot be created!");
                				return null;
            				}
            			}

            			//storage exists or was created, form the CRL file name of the CRL from the distribution point
            			String aFileName = aCRLCachePath+filesep+getIssuerMD5Hash(userCert)+".crl";
            			m_aLogger.log("crl storage: "+aFileName);
            			//now save the CRL, if there is one, remove it first
                        FileOutputStream fos = new FileOutputStream(aFileName);
                        fos.write(crl.getEncoded());
                        fos.flush();
                        fos.close();
            		} catch (NoSuchAlgorithmException e) {
            			m_aLogger.severe(e);
            		} catch (URISyntaxException e) {
            			m_aLogger.severe(e);
            		} catch (IOException e) {
            			m_aLogger.severe(e);
            		} catch (Throwable e) {
            			m_aLogger.severe(e);
            		}
                    break;
                }
                else
                	setCertificateStateConditions(CertificateStateConditions.INET_ACCESS_ERROR);
            } catch (Throwable e) {
                trace(e);
                trace("isNotRevoked - Errore durante il " + i +
                      "-esimo accesso alle CRL " + e.getMessage());
            }
        }
        if (crl == null) {
            trace("isNotRevoked - CRL non raggiungibile");
            //si.setCertificateStatus(4);

            CRLerror = "CRL non raggiungibile"; //sia da http che da ldap

        }
        return crl;
    }

    private String getIssuerMD5Hash(X509Certificate userCert) throws NoSuchAlgorithmException {
		//get the issuer principal
		byte[] xp = userCert.getIssuerX500Principal().getEncoded();
		//form the MD5 hash of this array
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		byte[] md5hash = new byte[32];
		md.update(xp, 0, xp.length);
		md5hash = md.digest();
		return Helpers.convertToHex(md5hash).toUpperCase();
    }
    /**
     * Returns Common Name (string) of the given certificate <br><br>
     * Restituisce il CN del certificato in oggetto
     *
     * @param userCert X509Certificate
     * @return String
     */
    private static String getCommonName(X509Certificate userCert) {
        String DN = userCert.getIssuerDN().toString();
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

/**Downloads CRL issued by given CA from specified URL<br><br>
     * Scarica la CRL dall'URL specificato ed emessa dalla CA specificata
     * @param crlDP Distribution Point
     * @param issuer DN of the CRL signer, if LDAP protocol is used
     * @throws CertificateException error during certificate parsing
     * @return CRL the given certificate
     */
    public X509CRL download(String crlDP, Principal issuer) throws
            CertificateException, MalformedURLException {
        String protocol="";
        statusValue(5);
        protocol=crlDP.substring(0,crlDP.indexOf("://"));
        if (protocol.equalsIgnoreCase("ldap")) {
            return ricercaCrlByLDAP(crlDP, issuer);
        } else if (protocol.equalsIgnoreCase("http")) {
            return ricercaCrlByProxyHTTP(new URL(crlDP));
        } else if (protocol.equalsIgnoreCase("https")) {
            if (initHTTPS()) {
                return ricercaCrlByProxyHTTP(new URL(crlDP));
            } else {
                trace("Supporto al protoccolo HTTPS non disponibile");
                return null;
            }
        } else {
            trace(
                    "isNotRevoked - protocollo di accesso alla CRL non supportato: " +
                    protocol);
            return null;
        }
    }

    private X509CRL ricercaCrlByLDAP(String dp, Principal CADName) {
        trace("ricercaCrlByLDAP - Inizio Metodo");
        String ldapUrl = dp;
        //FIXME: add the communication exception exception, or something similar
        try {
            // ldapUrl = dp.toExternalForm();

            if (ldapUrl.toLowerCase().indexOf("?certificaterevocationlist") < 0) {
                ldapUrl = ldapUrl + "?certificaterevocationlist";
                // dp = new URL(ldapUrl);
                trace("Effettuata normalizzazione dell'url ldap");
            }
            trace("CRL Distribution Point: " + ldapUrl);

            statusValue(10);
         // Set up environment for creating initial context
/*            Hashtable env = new Hashtable(11);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//            env.put(Context.PROVIDER_URL, "ldap://localhost:389/o=JNDITutorial");
            env.put(Context.PROVIDER_URL, ldapUrl);

            // Specify timeout to be 5 seconds
            env.put("com.sun.jndi.ldap.connect.timeout", "15000");

            // Create initial context
            DirContext ctx = new InitialDirContext(env);*/
            DirContext ctx = new InitialDirContext();
            statusValue(20);

            //FIXME why was this doesn't run?
            // impostazione un timeout...
/*            int timeout = 5000; //5 s
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setTimeLimit(timeout); //

            NamingEnumeration ne = ctx.search(ldapUrl, "", ctls);*/
            NamingEnumeration ne = ctx.search(ldapUrl, "", null);
            if (!ne.hasMore()) {
                trace("CRL entry non trovata in base all'url ldap: " + ldapUrl);
                return null;
            }
            ctx.close();
            statusValue(40);

            Attributes attribs = ((SearchResult) ne.next()).getAttributes();
            Attribute a = null;
            statusValue(60);

            for (NamingEnumeration ae = attribs.getAll(); ae.hasMore(); ) {
                a = (Attribute) ae.next();
                trace("Attribute ID: " + a.getID() + ": " + a.size());
                if (a.getID() != null &&
                    a.getID().toLowerCase().indexOf("certificaterevocationlist") !=
                    -1) {
//                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                    return ((X509CRL)cf.generateCRL(new ByteArrayInputStream((byte[]) crlVector.get(0))));
                    return parse((byte[]) a.get(0));
                }
            }
            trace("CRL non trovata in base all'url ldap: " + ldapUrl);
            return null;
/*        } //catch (TimeLimitExceededException e) {
        //  System.out.println("time limit exceeded: "+e);
        //  return null;
        //  }
        catch (CommunicationException e) {
        	// set the error to the CRL control
        	
        	return null;*/
        }  catch (Throwable e) {
            trace(e);
            trace("ricercaCrlByLDAP -> " + e.toString());
            return null;
        }
    }

    private X509CRL ricercaCrlByProxyHTTP(URL dp) {
        // controllare throw delle exceptions
        trace("ricercaCrlByProxyHTTP - Inizio Metodo");
        int rd = 0;
        byte[] buff = new byte[1024];
        BufferedInputStream stream = null;
        try {
            URLConnection connection = dp.openConnection();
            if (auth != null) {
                connection.setRequestProperty("Proxy-Authorization", auth);
                trace("Impostati dati autenticazione Proxy");
            }
            connection.setDoInput(true);
            stream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream(102400);
            while ((rd = stream.read(buff)) != -1) {
                baos.write(buff, 0, rd);
            }
            baos.flush();
            trace("Scaricati " + baos.size() + " bytes");
            return parse(baos.toByteArray());
        } catch (Throwable e) {
            trace(e);
            trace("ricercaCrlByProxyHTTP -> " + e.toString());
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ie) {
                    ;
                }
            }
        }
    }

    private boolean initHTTPS() {
        String strVendor = System.getProperty("java.vendor");
        String strVersion = System.getProperty("java.version");
        Double dVersion = new Double(strVersion.substring(0, 3));
        if (1.2 <= dVersion.doubleValue()) {
            System.setProperty("java.protocol.handler.pkgs",
                               "com.sun.net.ssl.internal.www.protocol");
            try {
                Class clsFactory = Class.forName(
                        "com.sun.net.ssl.internal.ssl.Provider");
                if ((null != clsFactory) &&
                    (null == Security.getProvider("SunJSSE"))) {
                    Security.addProvider((Provider) clsFactory.newInstance());
                }
                return true;
            } catch (ClassNotFoundException cfe) {
                trace(
                        "Classi di JSSE SSL non trovate. Controllare il classpath: " +
                        cfe.toString());
                return false;
            } catch (Throwable e) {
                trace("Errore generico nel metodo initHTTPS --> " + e.toString());
                return false;
            }
        } else {
            trace("Versione del JRE non compatibile con il protocollo HTTPS");
            return false;
        }
    }

    /**
     * Activate or discactivate debug messages<br><br>
     *
     * Attiva o disattiva i messaggi di debug
     * @param debug if true, it shows debug messages
     */

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    private X509CRL parse(byte[] crlEnc) throws GeneralSecurityException {
        if (crlEnc == null) {
            return null;
        }

        byte[] crlData;
        try {
            // Quello di SUN non e' sempre affidabile!!!
            // crlData = new sun.misc.BASE64Decoder().decodeBuffer(new String(crlEnc));
            crlData = Base64.decode(crlEnc);
            trace("Decodifica base64 completata");
        } catch (Throwable e) {
            trace("La CRL non e' in formato base64");
            crlData = crlEnc;

        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        return (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlData));
    }

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
	private void traceDialog(String s) {
        if (m_aLoggerDialog != null) {
        	m_aLoggerDialog.log(s);
        }
    }

    private void traceDialog(Throwable t) {
    	if (m_aLoggerDialog != null) {
        	m_aLoggerDialog.severe(t);
        }
    }

    private void traceDialog(String _mex, Throwable t) {
        if (m_aLoggerDialog != null) {
        	m_aLoggerDialog.severe(_mex,t);
        }
    }

    private void statusText(String _mex) {
    	if(m_xStatusIndicator != null) {
    		m_xStatusIndicator.setText(_mex);
    	}
    }

    private void statusValue(int x) {
    	if(m_xStatusIndicator != null) {
    		m_xStatusIndicator.setValue(x);
    	}
    }

    /**
	 * @param m_aCertificateState the m_aCertificateState to set
	 */
	private void setCertificateState(CertificateState m_aCertificateState) {
		this.m_aCertificateState = m_aCertificateState;
	}

	/**
	 * @return the m_aCertificateState
	 */
	public CertificateState getCertificateState() {
		return m_aCertificateState;
	}

	/**
	 * @param m_aCertificateStateConditions the m_aCertificateStateConditions to set
	 */
	private void setCertificateStateConditions(
			CertificateStateConditions m_aCertificateStateConditions) {
		this.m_aCertificateStateConditions = m_aCertificateStateConditions;
	}

	/**
	 * @return the m_aCertificateStateConditions
	 */
	public CertificateStateConditions getCertificateStateConditions() {
		return m_aCertificateStateConditions;
	}

}
