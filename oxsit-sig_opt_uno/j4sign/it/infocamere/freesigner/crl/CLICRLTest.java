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
import java.security.*;
import java.security.cert.*;
import java.util.*;

import org.bouncycastle.cms.*;
import org.bouncycastle.util.encoders.Base64;

/**
 * Command line client. It tests out CRL verifying<br><br>
 * Client a riga di comando per test verifica CRL
 * @author Francesco Cendron
 */
public class CLICRLTest {
    private static X509CertRL CRL;
    private static String filePath;

    /**
     * Constructor of CLICRLTest.<br>
     * Costruttore della classe CLICRLTest.<br>
     *
     * @param CAroot zip containg CAs
     * @param fileFirmato signed file to verify
     * @throws FileNotFoundException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public CLICRLTest(String CAroot, String fileFirmato) throws
            FileNotFoundException, IOException,
            GeneralSecurityException {
        //zip contenente le CA
        String CAfilePath = System.getProperty("user.home")
                            + System.getProperty("file.separator") +
                            CAroot;
        FileInputStream is = new FileInputStream(CAfilePath);

        CertificationAuthorities CA = new CertificationAuthorities(is, true);
        CRL = new X509CertRL(CA);

        //Settaggio proxy
        //if (CRL.setUseproxy(true, "utente", "pwd", "proxy",
        //                    "porta")) {
        //    System.out.println("Proxy settato");
        //}

        //File firmato
        filePath = System.getProperty("user.home")
                   + System.getProperty("file.separator") +
                   fileFirmato;

    }

    /**
     * It recognises all the signers of the CMS (coded base64 or DER) and verify if
     * it is revoked, if it is signed with the public key of a given CA and if it is
     * temporally valid<br><br>
     *
     * Fa un giro tra tutti gli i firmatari del file firmato codificato base64 o
     * DER e verifica revoca, integrità (+corrispondenza all'insieme delle CA
     * presenti in root) e scadenza dei rispettivi certificati
     *
     * @return true
     */
    public boolean verifica() {
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
                System.out.println("Il file firmato è in formato Base64");
            } catch (Exception e) {
                // il file non e' in formato base64
                //quindi � in DER (again italian law!)
                System.out.println("Il file firmato � in formato DER");
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
            System.out.println(c.size()+ " firmatari diversi trovati");
            System.out.println(certs.getCertificates(null).size()+ " firmatari diversi trovati");
             System.out.println(s.getSignerInfos().size()+ " firmatari diversi trovati");




            //non avrebbe senso che fossero uguali
            //quindi fa il ciclo tra i firmatari
            //PERO' PUO' CAPITARE CHE CI SIA UN FIRMATARIO CHE FIRMA DUE VOLTE
            // E IN QUESTO CASO DOVREBBE FARE IL GIRO SUI CERTIFICATI!!!
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
                    System.out.println(i + ") Verifiying signature from:\n"
                                       + cert.getSubjectDN());
                    /*
                     * System.out.println("Certificate follows:");
                     * System.out.println("====================================");
                     * System.out.println(cert);
                     * System.out.println("====================================");
                     */

                    //VERIFICA REVOCA
                    // Verifica Revoca e appartenza della CA
                    // NB verifica integrit� del doc e non-scadenza del cert sono fatte in CLITest
                    if (CRL.isNotRevoked(cert)) {
                        System.out.println("Certificato non revocato");
                    }

                    //VERIFICA VALIDITA' TEMPORALE
                    try {
                        cert.checkValidity();
                        System.out.println("Certificato valido fino a " +
                                           cert.getNotAfter());
                    } catch (CertificateExpiredException ex) {
                        System.out.println("Certificato scaduto il " +
                                           cert.getNotAfter());
                    } catch (CertificateNotYetValidException ex) {
                        System.out.println(
                                "Certificato non ancora valido. Valido da " +
                                cert.getNotBefore());
                    }

                    //VERIFICA INTEGRITA'
                    //verify that the given certificate succesfully handles
                    //and confirms the signature associated with this signer
                    //and, if a signingTime attribute is available, that the
                    //certificate was valid at the time the signature was
                    //generated.
                    if (signer.verify(cert, "BC")) {

                        System.out.println("Firma " + i + " integra.");
                    } else {
                        System.err.println("Firma " + i + " non integra!");
                    }

                } else {
                    System.out
                            .println(
                                    "There is not exactly one certificate for this signer!");
                }
                i++;
            }
        } catch (Exception ex) {
            System.err.println("eEXCEPTION:\n" + ex);
        }

        return true;
    }

    public static void main(String[] args) throws IOException, CMSException,
            CertificateException, GeneralSecurityException {

        CLICRLTest prova = new CLICRLTest("LISTACER_20090303.zip", "specifica-firma-XAdES-biblio.odb.nuovo-cert.p7m");
        prova.verifica();
    }
}
