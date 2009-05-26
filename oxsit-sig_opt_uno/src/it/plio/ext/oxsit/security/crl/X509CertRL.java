/*************************************************************************
 *
 *  This code is partly derived from
 *  it.infocamere.freesigner.crl.X509CertRL class in freesigner
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

package it.plio.ext.oxsit.security.crl;

import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.util.encoders.Base64;

import com.sun.jmx.snmp.daemon.CommunicationException;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppec56
 *
 */
public class X509CertRL {
	
	private String CRLerror;
	private boolean debug;
	private CertificationAuthorities certAuths;
	private boolean useProxy;
	private HashMap crls;
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
     *  Controls if the given certificate is revoked at the current date.<br><br>
     * Effettua il controllo di revoca sulla firma contenuta nel certificato userCert, rispetto alla data corrente
     * @param userCert certificate to verify
     * @return true if certificate is not revoked
     */
	public boolean isNotRevoked(XStatusIndicator _aStatus, X509Certificate userCert) {
        return isNotRevoked(_aStatus,userCert, new Date());
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
    public boolean isNotRevoked(XStatusIndicator _aStatus, X509Certificate userCert, Date date) {

        X509CRL crl = null;
        //check if we have a status indicator
        m_xStatusIndicator = _aStatus;
        
        try {
            // devo fare l'update per compatibilita' all'indietro!
            if (!update(userCert, date, false)) {

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
                } catch (Exception ex) {
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
        } catch (Exception e) {
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
                trace("CRL NON contenuta nella cache");
                forceUpdate = true;
            }
        }
        if (forceUpdate) {
        	//check if the Issuer is in CARoot
        	try {
        		certAuths.getCACertificate(issuer);
        	} catch (GeneralSecurityException e) {
            	//if not present, do not download, simply set the right error and exits
        		setCertificateStateConditions(CertificateStateConditions.CRL_CANNOT_BE_ACCESSED);
        		m_aLogger.log("CA not found");
        		return false;
        	}
        	trace("Inizio download CRL...");
            if ((crl = download(userCert)) == null) {
        		setCertificateStateConditions(CertificateStateConditions.INET_ACCESS_ERROR);
                return false;
            }
            //verifica CRL
            trace("Inizio verifica della CRL...");
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
        } catch (Exception e) {
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

        trace(p + " distribution points trovati.");

        for (int i = 0; i < p; i++) {
            try {
                trace("Tentativo di accesso al CRL Distribution Point = " +
                      dp[i]);
                statusText("Tentativo di accesso al CRL Distribution Point in Internet...");
                crl = download(dp[i], userCert.getIssuerDN());
                // il primo protocollo che dia esiti positivi interrompe il ciclo
                if (crl != null) {
                    trace("CRL scaricata correttamente");

                    if (debug) {
                        try {
                            File dir1 = new File (".");
							String curDir=dir1.getCanonicalPath();
							//zip contenente le CA
							//FIXME, change with OOo user temp path.
							String filePath =
                   //System.getProperty("user.home")
                                   curDir + System.getProperty("file.separator") +
                                   "crl"+ System.getProperty("file.separator") + getCommonName(userCert) +
                                              ".crl";

                            FileOutputStream fos = new FileOutputStream(
                                    filePath);
                            fos.write(crl.getEncoded());
                            fos.flush();
                            fos.close();
                        } catch (Exception e) {
                            System.out.println(e);;
                        }
                    }
                    break;
                }
                else
                	setCertificateStateConditions(CertificateStateConditions.INET_ACCESS_ERROR);
            } catch (Exception e) {
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
        } //catch (TimeLimitExceededException e) {
        //  System.out.println("time limit exceeded: "+e);
        //  return null;
        //  }
        catch (CommunicationException e) {
        	// set the error to the CRL control
        	
        	return null;
        }  catch (Exception e) {
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
        } catch (Exception e) {
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
            } catch (Exception e) {
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
        } catch (Exception e) {
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
