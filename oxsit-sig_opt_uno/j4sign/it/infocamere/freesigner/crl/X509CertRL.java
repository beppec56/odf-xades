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
/*
 */
package it.infocamere.freesigner.crl;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import javax.naming.*;
import javax.naming.ldap.*;
import javax.naming.directory.*;

import org.bouncycastle.asn1.*;
import org.bouncycastle.cms.*;
import org.bouncycastle.util.encoders.*;

/**
 * Represents the set of CRL downloaded in the system.
 * It allows to control revokation in CRL<br><br>
 *
 * Rappresenta il set di CRL caricate nel sistema.
 * Fornisce i metodi per il controllo di revoca dei certificati
 *
 * @author Francesco Cendron
 */
public class X509CertRL {
    private X509Certificate userCert;
    private String CRLerror = "";
    private boolean debug;
    private HashMap crls;
    private boolean useProxy = false;
    private CertificationAuthorities certAuths;
    private String auth;
    private String message;
    private String reasonCode = null;

    /**
     * Constructor used in CertificationAuthorities<br>
     * Costruttore utilizzato dalla classe CertificationAuthorities
     *
     * @param certAuths reference to CertificationAuthorities used to verify signature
     * in CRL
     */
    public X509CertRL(CertificationAuthorities certAuths) {
        crls = new HashMap();
        this.certAuths = certAuths;
        //debug = true;
        debug = true;
        useProxy = false;
        CRLerror = new String("");
    }

    /**
     * Controls if the signed file at the specified filePath is revoked at the current date.
     *<br><br>
     * Effettua il controllo di revoca sulla firma contenuta nel file in filePath, rispetto alla data corrente
     * @param filePath filePath
     * @return true if certificate is not revoked
     */

    public boolean isNotRevoked(String filePath) {
        return isNotRevoked(getCertificatesFromFile(filePath), new Date());
    }

    /**
     *  Controls if the given certificate is revoked at the current date.<br><br>
     * Effettua il controllo di revoca sulla firma contenuta nel certificato userCert, rispetto alla data corrente
     * @param userCert certificate to verify
     * @return true if certificate is not revoked
     */
    public boolean isNotRevoked(X509Certificate userCert) {
        return isNotRevoked(userCert, new Date());
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
    public boolean isNotRevoked(X509Certificate userCert, Date date) {

        X509CRL crl = null;
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

                                return true; // o false da decidere
                            } else {
                                trace(
                                        "Il certificato risulta sospeso in data: " +
                                        revDate);
                                reasonCode =
                                        "Il certificato risulta sospeso in data: " +
                                        revDate;
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
                        return true; // o false da decidere
                    } else {
                        trace("Il certificato risulta revocato in data: " +
                              revDate);
                        if (reasonCode==null){reasonCode =
                                "in data: " +
                                revDate;}
                        return false;
                    }
                } catch (Exception ex) {
                    trace(ex);
                    trace(
                            "isNotRevoked - Errore nella lettura delle estensioni di revoca -> " +
                            ex.getMessage());

                    return false;
                }
                // la versione della CRL e' la uno e quindi non si può distinguere
                // la motivazione della revoca -> certificato revocato e basta.
            } else {
                trace("CRL V.1 : il certificato risulta revocato/sospeso");

                return false; // o false da decidere
            }
        } catch (Exception e) {
            //trace(e);
            trace("isNotRevoked - Errore generico nel metodo -> " +
                  e.getMessage());

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
            trace("Inizio download CRL...");
            if ((crl = download(userCert)) == null) {
                return false;
            }
            //verifica CRL
            trace("Inizio verifica della CRL...");
            //lancia GeneralSecurityEx se issuer non � presente in certAuths
            //cio� se la CA non � in root
            int verCode = check(crl, certAuths.getCACertificate(issuer), date);

            if (verCode != 0) {
               //CRLerror gi� settato in check()
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
            trace("Crt Issuer: " + caName.getClass().getName() + " " + caName);
            CRLerror="Errore: CA emettitrice CRL\n diversa da quella dell'utente";
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
            trace("isNotRevoked - CRL con next update: " + crl.getNextUpdate() +
                  ", controllo alle: " + date);
            CRLerror = "Errore: CRL con next update:\n" +
                       crl.getNextUpdate() +
                       ",\ncontrollo alle:\n" + date;
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
     * Return CRL Distribution Points (DP) of the specified cert in an array of URL
     * Restituisce i CRL DP del certificato specificato in un array di URL<br><br>
     *
     * @param certificate extracts DP from this certificate
     * @throws CertificateParsingException
     * @return URL []: URL array
     */
    public static URL[] getCrlDistributionPointOLD(X509Certificate certificate) throws
            CertificateParsingException {
        try {
            //trova i DP (OID="2.5.29.31") nel certificato
            DERObject obj = getExtensionValue(certificate, "2.5.29.31");

            if (obj == null) {
                //nessun DP presente
                return null;
            }
            ASN1Sequence distributionPoints = (ASN1Sequence) obj;

            URL[] urlArray = new URL[5];
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
                            urlArray[p++] = new URL(url);
                        }
                    }
                }

            }
            return urlArray;
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
                crl = download(dp[i], userCert.getIssuerDN());
                // il primo protocollo che dia esiti positivi interrompe il ciclo
                if (crl != null) {
                    trace("CRL scaricata correttamente");

                    if (debug) {
                        try {
                            File dir1 = new File (".");
           String curDir=dir1.getCanonicalPath();
           //zip contenente le CA
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

            DirContext ctx = new InitialDirContext();
            // impostazione un timeout...
            // int timeout = 5000; //5 s
            //SearchControls ctls = new SearchControls();
            // ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            //ctls.setTimeLimit(timeout); //

            NamingEnumeration ne = ctx.search(ldapUrl, "", null);
            if (!ne.hasMore()) {
                trace("CRL entry non trovata in base all'url ldap: " + ldapUrl);
                return null;
            }
            ctx.close();
            Attributes attribs = ((SearchResult) ne.next()).getAttributes();
            Attribute a = null;

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

        catch (Exception e) {
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

    /**
     * Set proxy connection parameters to download CRL<br><br>
     * Imposta i parametri di connessione con il proxy verso Internet per lo scarico delle CRL
     * @param proxy true is proxy is used
     * @param user proxy authenticated user
     * @param password password
     * @param proxyHost proxy
     * @param proxyPort proxy port
     * @return true se l'impostazione del collegamento col proxy e' terminata correttamente
     */


    public boolean setUseproxy(boolean proxy, String user, String password,
                               String proxyHost, String proxyPort) {
        if (proxy) {
            if (proxyHost != null && proxyPort != null) {
                this.useProxy = proxy;
                if (user != null && password != null) {
                    String authString = user + ":" + password;
                    auth = "Basic " +
                           new sun.misc.BASE64Encoder().encode(authString.
                            getBytes());
                }
                System.getProperties().put("proxySet", "true");
                System.getProperties().put("proxyHost", proxyHost);
                System.getProperties().put("proxyPort", proxyPort);
                System.setProperty("https.proxyHost", proxyHost);
                System.setProperty("https.proxyPort", proxyPort);
                trace("setUseproxy - Abilitato l'uso del proxy (anche per https)");
                return true;
            } else {
                this.useProxy = proxy;
                trace("setUseproxy - host o port nulli: disabilitato l'uso del proxy");
                return false;
            }
        } else {
            this.useProxy = proxy;
            trace("setUseproxy - Settato il collegamento senza proxy");
            return false;
        }
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
            System.out.println(s);
        }
    }

    private void trace(Throwable t) {
        if (debug && t != null) {
            t.printStackTrace();
        }
    }

    /**
     * Return the possible error message of the last CRL verification<br><br>

     * @return description of the last CRL verification error
     */

    public String getMessage() {
        return message;
    }

    /**
     * Returns certificate present in a file at the given filePath.<br>
     * This can be coded base64 or DER<br><br>
     * Restituisce il certificato contenuto nel file specificato nel filePath.
     * Distingue tra codifica base64 e DER.
     *
     * @return certificate
     * @param filePath String
     */
    public static X509Certificate getCertificatesFromFile(String filePath) {
        X509Certificate cert = null;
        try {

            byte[] buffer = new byte[1024];
            FileInputStream is = new FileInputStream(filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (is.read(buffer) > 0) {
                baos.write(buffer);
            }
            byte[] risultato = baos.toByteArray();

            //codifica file Base64 o DER?
            byte[] certData;
            try {
                //se Base64, decodifica (italian law!)
                certData = Base64.decode(risultato);
                //Decodifica base64 completata
                System.out.println("Il file � in formato Base64");
            } catch (Exception e) {
                // il file non e' in formato base64
                //quindi � in DER (again italian law!)
                System.out.println("Il file � in formato DER");
                certData = risultato;

            }
            //Estrazione del certificato dal file (ora codificato DER)
            CMSSignedData s = new CMSSignedData(certData);
            Security.addProvider(new org.bouncycastle.jce.provider.
                                 BouncyCastleProvider());
            //recupero dal CMS la lista dei certificati

            CertStore certs = s.getCertificatesAndCRLs("Collection", "BC");

            //Recupero i firmatari.
            SignerInformationStore signers = s.getSignerInfos();
            Collection c = signers.getSigners();

            Iterator it = c.iterator();

            //ciclo tra tutti i firmatari
            int i = 0;
            while (it.hasNext()) {
                SignerInformation signer = (SignerInformation) it.next();
                Collection certCollection = certs.getCertificates(signer
                        .getSID());

                if (certCollection.size() == 1) {
                    //Iterator certIt = certCollection.iterator();
                    //X509Certificate cert = (X509Certificate)
                    // certIt.next();

                    cert = (X509Certificate) certCollection
                           .toArray()[0];

                } else {
                    System.out
                            .println(
                                    "There is not exactly one certificate for this signer!");
                }
                i++;
            }

        } catch (Exception ex) {
            System.err.println("EXCEPTION:\n" + ex);
        }

        return cert;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getCRLerror() {

        return CRLerror;
    }

    public void resetCRLerror() {

        CRLerror = "";
    }
}   
