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

import it.infocamere.freesigner.crl.*;
import it.trento.comune.j4sign.cms.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.cms.*;

/**
 * Object used to perform verification about certificate validity and signature integrity.
 * Methods get... perform action, methods is... just return value. It is obviously necessary
 * performing verification before returning the value
 *<br><br>
 * Oggetto che performa e restituisce le verifiche sul certificato e sull'integrità
 * della firma. I metodi get perfomano la verifica, i metodi is restituiscono solo
 * il risultato. E' ovviamente necessario prima performare la verifica e poi
 * restituire il risultato.
 *
 * @author Francesco Cendron
 */
class VerifyResult {
    public VerifyResult() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private boolean isHashCorrect;
    private boolean isPublicKeyCorrect;
    private boolean isPathValid;

    private boolean isRevoked;
    private boolean isExpired;
    private boolean isInUse; //contrario di NotYetValid

    private CertValidity cv;
    private String CRLerror = "";
    private SignerInformation signer;
    private CMSSignedData cms;
    private X509Certificate cert;
    private String encodedDigest;
    private boolean isDownloadCRLForced;

    private boolean isPassed;

    /**
     * Constructor
     *
     * @param c X509Certificate
     * @param cm CMSSignedData
     * @param C CertificationAuthorities
     * @param s SignerInformation
     */
    public VerifyResult(X509Certificate c, CMSSignedData cm,
                        CertificationAuthorities C,
                        SignerInformation s) {
        this(c, cm, C, s, false);

    }

    /**
     * Constructor
     *
     * @param c X509Certificate
     * @param cm CMSSignedData
     * @param C CertificationAuthorities
     * @param s SignerInformation
     * @param isDownloadCRLForced boolean
     */
    public VerifyResult(X509Certificate c, CMSSignedData cm,
                        CertificationAuthorities C,
                        SignerInformation s, boolean isDownloadCRLForced) {

        isHashCorrect = false;
        isPublicKeyCorrect = false;
        cert = c;
        signer = s;
        this.isDownloadCRLForced = isDownloadCRLForced;
        cv = new CertValidity(c, C, this.isDownloadCRLForced);

        cms = cm;
        encodedDigest = null;
        isPassed = false;

    }

    /**
     * Verifies the signature for the given buffer (hash) of bytes using the
     * public key.
     *
     * @param key PublicKey
     * @param buffer byte[]
     * @param signature byte[]
     * @return boolean
     */
    public static boolean verifySignature(PublicKey key, byte[] buffer,
                                          byte[] signature) {
        try {
            //System.out.println("is in");
            Signature sig = Signature.getInstance("RSA");
            // System.out.println("is in 2" + sig);
            sig.initVerify(key);
            //  System.out.println("is in 3");
            sig.update(buffer, 0, buffer.length);
            // System.out.println(" the thing inside is " + sig.verify(signature));
            return sig.verify(signature);
        } catch (SignatureException e) {
        } catch (InvalidKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        //System.out.println("vero nejke " );
        return false;
    }

    /**
     * Verified that dencrypted signature corresponds to hashed document
     *
     * @return boolean
     */
    public boolean getHashCorrect() {

        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1", "BC");
        } catch (NoSuchAlgorithmException ex1) {
        } catch (java.security.NoSuchProviderException ex) {
            System.out.println("NoSuchProviderException");
        }
        sha.update(getRawBytes());
        //questo � l'hash nel cms
        byte[] hash = sha.digest();

        //verifySignature(getPublicKey(), hash, firma)
        if (!(verifySignature(cert.getPublicKey(), hash, signer.getSignature()))) {
            System.out.println("The signature on the hash is verifyed");
            isHashCorrect = true;
        } else {
            System.out.println("the signature is not valid");
            isHashCorrect = false;
        }

        return isHashCorrect;
    }

    /**
     * Verify that public Key corresponds<br><br>
     * Performa la verifica che la chiave pubblica usata corrisponda
     *
     * @return boolean
     */
    public boolean getPublicKeyCorrect() {
        try {

            isPublicKeyCorrect = signer.verify(cert, "BC");
        } catch (CMSException ex) {
        } catch (CertificateNotYetValidException ex) {
        } catch (CertificateExpiredException ex) {
        } catch (NoSuchProviderException ex) {
        } catch (NoSuchAlgorithmException ex) {
        }
        System.out.println(signer.getSID().toString() + " " + cert.getSubjectDN() +
                           " " + isPublicKeyCorrect);
        return isPublicKeyCorrect;
    }

    /**
     * Perform the global verification and return the global result<br><br>
     * Metodo complessivo che esegue e restituisce la verifica
     *
     * @return boolean
     */
    public boolean getPassed() {

        isPassed = getPublicKeyCorrect() && getHashCorrect() && cv.getPassed();
        CRLerror = cv.getCRLerror();
        return isPassed;
    }

    /**
     * Return CRLerror (error during CRL download)<br><br>
     * Restituisce l'errore CRLerror (errore durante il download della CRL)
     *
     * @return String
     */
    public String getCRLerror() {

        return CRLerror;
    }


    /**
     * Checks certification path by IssuerX500Principal keyed in CAroot<br><br>
     * risale il certification path attraverso IssuerX500Principal chiave in
     * CAroot
     *
     * @return boolean
     */
    public boolean isPathValid() {

        return cv.isPathValid();
    }

    public boolean isHashCorrect() {

        return isHashCorrect;
    }

    public boolean isPublicKeyCorrect() {

        return isPublicKeyCorrect;
    }

    public boolean isRevoked() {

        return cv.isRevoked();
    }

    public boolean isExpired() {

        return cv.getExpired();
    }

    public boolean isInUse() {

        return cv.getInUse();
    }

    public boolean isPassed() {

        return isPassed;
    }

    public boolean isDownloadCRLForced() {

        return isDownloadCRLForced;
    }


    /**
     * Checks if CRL is already been checked<br><br>
     * True se la CRL � stato verificata
     *
     * @return boolean
     */
    public boolean isCRLChecked() {

        return cv.isCRLChecked();
    }


    public void setHashCorrect(boolean b) {
        isHashCorrect = b;
    }


    public void setPassed(boolean b) {
        isPassed = b;
    }

    public void setPublicKeyCorrect(boolean b) {
        isPublicKeyCorrect = b;
    }



    /**
     * Creates the base64 encoding of a byte array.
     *
     * @param bytes byte[]
     * @return java.lang.String
     */
    public String encodeFromBytes(byte[] bytes) {
        String encString = null;

        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        encString = encoder.encode(bytes);

        return encString;
    }

    /**
     * Return signd content ritorna il contenuto firmato (la firma)
     *
     * @return byte[]
     */
    byte[] getRawBytes() {
        return (byte[]) cms.getSignedContent().getContent();
    }

    private void jbInit() throws Exception {
    }


}
