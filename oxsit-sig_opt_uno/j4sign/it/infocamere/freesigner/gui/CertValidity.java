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

import java.security.*;
import java.security.cert.*;

import it.infocamere.freesigner.crl.*;
import it.infocamere.freesigner.gui.Configuration;

/**
 * This class is constructed with a certificate X509Certificate c and  CertificationAuthorities C
 * <br>Checks the validity of the certificate.<br><br>
 *
 * Questa classe si costruisce con il certificato X509Certificate c e le CertificationAuthorities C
 * <br>Vengono effettuate tutte le verifiche necessarie per la validità del certificato
 *
 *
 * @author Francesco Cendron
 */
class CertValidity {
    //verifiche di validit� di un certificato
    private boolean isPathValid; //se la CA del cert � presente
    private Configuration conf;
    private String CRLerror = "";
    private boolean isRevoked;
    private boolean isDownloadCRLForced; //download forzato CRL (dal frame FreesignerCertFrame)
    private boolean isExpired;
    private boolean isInUse; //contrario di NotYetValid


    private boolean isPassed;
    private X509Certificate cert;
    private CertificationAuthorities CAroot;
    private X509CertRL CRL;
    



    /**Constructor of the class.<br><br>
     * Costrutture della classe: si richama la this.
     *
     * @param c certificate
     * @param C CertificationAuthorities
     */
    public CertValidity(X509Certificate c, CertificationAuthorities C) {
        this(c, C, false);

    }

    /**
     * Constructor of the class. It forces CRL download if flag isDownloadCRLForced
     * <br>is true. It is useful to avoid forcing CRL download (isDownloadCRLForced = false)
     * <br>when offline situations occurs. It is anyway necessary to complete
     * <br> certificate validity check by verifying CRL.
     *
     * <br><br> Methods is.... don't perform action, methos get.... do perform action.
     *
     *<br><br>
     * Costrutture della classe che forza il download della CRL nel
     * <br> caso il flag isDownloadCRLForced sia settato a true.
     * <br> E' utile non forzare il download della CRL (ponendo
     * isDownloadCRLForced = false)
     * <br> nei casi di mancanza di connessione alla rete. La verifica della
     * revoca del certificato
     * <br> � infatti comunque necessaria per la verifica della validit� del
     * certificato. N.B. I metodi is... a differenza dei get... non perfomano
     * l'azione ma restituiscono solo il valore
     *
     * @param c certificate4
     * @param C CertificationAuthorities
     * @param isDownloadCRLForced if true CRL download is forced
     */
    public CertValidity(X509Certificate c, CertificationAuthorities C,
                        boolean isDownloadCRLForced) {
        cert = c;
        CAroot = C;
        CRL = new X509CertRL(CAroot);
        conf = Configuration.getInstance();

        CRL.setUseproxy(conf.isUsingProxy(), conf.getUserName(),
                        conf.getPassWord(), conf.getHost(), conf.getPort());
        isPathValid = false;
        isRevoked = true;
        isExpired = false;
        isInUse = false;
        isPassed = false;
        this.isDownloadCRLForced = isDownloadCRLForced;

    }

    /**
     * Checks certification path by IssuerX500Principal keyed in CAroot<br><br>
     *  Risale il certification path attraverso IssuerX500Principal chiave in CAroot
     *
     *   @return true: if certification path is valid
     *
     */

    public boolean getPathValid() {
        isPathValid = true;
        X509Certificate certChild = cert;
        X509Certificate certParent = null;
        while (!certChild.getIssuerDN().equals(
                certChild.
                getSubjectDN())) {
            //finche' la CA non � autofirmata

            try {
                certParent = CAroot.getCACertificate(
                        certChild.getIssuerX500Principal());
            } catch (GeneralSecurityException ex) {
                //la CA non � presente nella root
                isPathValid = false;
                return isPathValid;
            }
            certChild = certParent;
        }
        ;

        return isPathValid;
    }

    /**
     * Checks if certificate is revoked<br><br>
     *  Verifica che il certificato non sia stato revocato.
     *
     * @return true: if certificate is revoked
     *
     *
     */
    public boolean getRevoked() {
        if (conf.getCRLupdate() || isDownloadCRLForced) {
            isRevoked = !CRL.isNotRevoked(cert);
        }
        return isRevoked;
    }

    public boolean isCRLChecked() {
        return conf.getCRLupdate() || isDownloadCRLForced;
    }

    /**
     *  Returns ReasonCode
     * CRLReason ::= ENUMERATED {
     unspecified(0), keyCompromise(1), cACompromise(2), affiliationChanged(3),
     superseded(4), cessationOfOperation(5), certificateHold(6), removeFromCRL(8)
     }
     * and possibly the date of revokation. see X509CertRL<br><br>
     *
     * @return String: reason code
     *
     *
     */
    public String getReasonCode() {
        return CRL.getReasonCode();
    }

    /**
     * Returns error message during CRL download. see X509CertRL
     * NB call this method always after getPassed(), that calls X509crl.isrevoked()<br><br>
     *
     *  Restituisce una stringa contenente un messaggio di errore nella fase
     * di verifica o download della CRL. vedi X509CertRL
     *
     * //chiamare questo metodo sempre dopo aver chiamata getpassed che chiama a sua volta
     *  //x509crl.isrevoked!
     *
     * @return String: error
     *
     *
     */


    public String getCRLerror() {

        return CRLerror;
    }

    /**
     * Return the general result<br><br>
     *  Restituisce il risultato di tutte le verifiche
     *
     * @return true: if certificate is valid
     *
     *
     */

    public boolean getPassed() {

        isPathValid = this.getPathValid();
        isExpired = this.getExpired();
        isInUse = this.getInUse();
        isRevoked = this.getRevoked();
        isPassed = isPathValid && !isRevoked && !isExpired && isInUse;
        System.out.println("************************Verifica: " +
                           cert.getSubjectDN() + "\n Risultato getPassed: " +
                           isPassed);
        CRLerror = CRL.getCRLerror();

        return isPassed;
    }

    /**Return true if certificate is expired<br><br>
     *  Restituisce true se il certificato � scaduto
     *

     *
     * @return true: if certificate is expired
     *
     *
     */

    public boolean getExpired() {
        try {
            cert.checkValidity();
            isInUse = true;
            isExpired = false;
        } catch (CertificateNotYetValidException ex) {
            isInUse = false;
        } catch (CertificateExpiredException ex) {
            isExpired = true;
        }

        return isExpired;
    }

    /**
     * Return true if the certificate is active<br><br>
     *  Restituisce true se il certificato � ancora attivo
     *

     *
     * @return true: if the certificate is active
     *
     *
     */
    public boolean getInUse() {
        try {
            cert.checkValidity();
            isInUse = true;
            isExpired = false;
        } catch (CertificateNotYetValidException ex) {
            isInUse = false;
        } catch (CertificateExpiredException ex) {
            isExpired = true;
        }

        return isInUse;
    }


    public boolean isPathValid() {

        return isPathValid;
    }

    public boolean isRevoked() {

        return isRevoked;
    }

    public boolean isPassed() {

        return isPassed;
    }

    public boolean isExpired() {

        return isExpired;
    }

    public boolean isInUse() {

        return isInUse;
    }

    public void setPathValid(boolean b) {
        isPathValid = b;
    }

    public void setRevoked(boolean b) {
        isRevoked = b;
    }

    public void setPassed(boolean b) {
        isPassed = b;
    }

    public void setExpired(boolean b) {
        isExpired = b;
    }

    public void setInUse(boolean b) {
        isInUse = b;
    }

    public void setisDownloadCRLForced(boolean b) {
        isDownloadCRLForced = b;
    }


}
