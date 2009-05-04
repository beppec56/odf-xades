/**
 *	j4sign - an open, multi-platform digital signature solution
 *	Copyright (c) 2004 Roberto Resoli - Servizio Sistema Informativo - Comune di Trento.
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
 * $Header: /cvsroot/j4sign/j4sign/src/java/core/it/trento/comune/j4sign/cms/ExternalSignatureSignerInfoGenerator.java,v 1.1 2004/12/27 11:14:34 resoli Exp $
 * $Revision: 1.1 $
 * $Date: 2004/12/27 11:14:34 $
 */

package it.trento.comune.j4sign.cms;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.SignerIdentifier;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedDataGenerator;

/**
 * An <code>org.bouncycastle.asn1.cms.SignerInfo</code> generator, where encryption operations
 * are kept external.
 *<p>
 * The class is a reimplementation of the original nested <code>org.bouncycastle.cms.CMSSignedDataGenerator$SignerInf</code>
 * class.<br>
 * The key methods are {@link #getBytesToSign(DERObjectIdentifier, CMSProcessable, String)}, which
 * calculates the bytes to digest and encrypt externally, and {@link #setSignedBytes(byte[])} which
 * stores the result.
 * Actually the {@link #generate()} method (defined package private) is used only in 
 * {@link it.trento.comune.j4sign.cms.ExternalSignatureCMSSignedDataGenerator#generate(CMSProcessable, boolean)} .
 * <p>
 * For an usage example, see {@link it.trento.comune.j4sign.cms.ExternalSignatureCMSSignedDataGenerator} and {@link it.trento.comune.j4sign.examples.CLITest}.
 *
 * @author  Roberto Resoli
 * @version $Revision: 1.1 $ $Date: 2004/12/27 11:14:34 $
 */
public class ExternalSignatureSignerInfoGenerator {
    
    /**
     * The signer certificate, needed to extract <code>IssuerAndSerialNumber</code> CMS information.
     * This has to be set, along {@link #signedBytes}, before calling {@link #generate()}.
     */
    X509Certificate cert;

    /**
     * The (externally) encrypted digest of {@link #getBytesToSign(DERObjectIdentifier, CMSProcessable, String)}.
     * * This has to be set, along {@link #cert}, before calling {@link #generate()}.
     */
    byte[] signedBytes;

    /**
     * Digesting algorithm OID.
     */
    String digestOID;
    
    /**
     * Encryption algorithm OID.
     */    
    String encOID;

    /**
     * The externally set 'authenticated attributes' to be signed, other than contentType, messageDigest, signingTime;<br>
     * currently not used (no setter method).
     */
    AttributeTable sAttr = null;
    
    /**
     * The externally set attributes NOT to be signed;<br>
     * currently not used (no setter method).
     */
    AttributeTable unsAttr = null;
    
    /**
     * The set of authenticated attributes, calculated in {@link #getBytesToSign(DERObjectIdentifier, CMSProcessable, String)} method,<br>
     * that will be externally signed.
     */
    ASN1Set signedAttr = null;
    
    /**
     * The set of authenticated attributes, calculated in {@link #getBytesToSign(DERObjectIdentifier, CMSProcessable, String)} method,<br>
     * that will NOT be signed.
     */
    ASN1Set unsignedAttr = null;

    /**
     * Class wrapping a <code>MessageDigest</code> update in form of an
     * output stream.
     * Passed to <code>org.bouncycastle.cms.CMSProcessable.write(java.io.OutputStream)</code> method
     * to easily compute the digest of a <code>CMSProcessable</code>.
     */
    static class DigOutputStream extends OutputStream {
        MessageDigest dig;

        public DigOutputStream(MessageDigest dig) {
            this.dig = dig;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            dig.update(b, off, len);
        }

        public void write(int b) throws IOException {
            dig.update((byte) b);
        }
    }

    /**
     * Constructor.
     * @param digestOID the digesting algorithm OID
     * @param encOID the encryption algorithm OID
     */
    public ExternalSignatureSignerInfoGenerator(String digestOID, String encOID) {
        this.cert = null;
        this.digestOID = digestOID;
        this.encOID = encOID;
    }

    /**
     * Gets the signer certificate.
     * @return the signer certificate.
     */
    X509Certificate getCertificate() {
        return cert;
    }

    /**
     * Sets the signer certificate.
     * @param c the X509 certificate corresponding to the private key used to sign.
     */
    public void setCertificate(X509Certificate c) {
        cert = c;
    }

    /**
     * @return the digesting OID string.
     */
    String getDigestAlgOID() {
        return digestOID;
    }
    
    /**
     * @return the digesting algorithm parameters; currently returns null.
     */
    byte[] getDigestAlgParams() {
        return null;
    }

    /**
     * @return the encryption OID string.
     */

    String getEncryptionAlgOID() {
        return encOID;
    }

    /**
     * @return the externally set authenticated attributes; currently null.
     */

    AttributeTable getSignedAttributes() {
        return sAttr;
    }

    /**
     * @return the externally set not authenticated attributes; currently null.
     */

    AttributeTable getUnsignedAttributes() {
        return unsAttr;
    }

    /**
     * Return the digest algorithm using one of the standard JCA string
     * representations rather the the algorithm identifier (if possible).
     */
    String getDigestAlgName() {
        String digestAlgOID = this.getDigestAlgOID();

        if (CMSSignedDataGenerator.DIGEST_MD5.equals(digestAlgOID)) {
            return "MD5";
        } else if (CMSSignedDataGenerator.DIGEST_SHA1.equals(digestAlgOID)) {
            return "SHA1";
        } else if (CMSSignedDataGenerator.DIGEST_SHA224.equals(digestAlgOID)) {
            return "SHA224";
        } else {
            return digestAlgOID;
        }
    }

    /**
     * Return the digest encryption algorithm using one of the standard JCA
     * string representations rather the the algorithm identifier (if possible).
     */
    String getEncryptionAlgName() {
        String encryptionAlgOID = this.getEncryptionAlgOID();

        if (CMSSignedDataGenerator.ENCRYPTION_DSA.equals(encryptionAlgOID)) {
            return "DSA";
        } else if (CMSSignedDataGenerator.ENCRYPTION_RSA
                .equals(encryptionAlgOID)) {
            return "RSA";
        } else {
            return encryptionAlgOID;
        }
    }

    /**
     * Generates the SignerInfo CMS structure information for a single signer.
     * This method has to be called after setting {@link #cert} {@link #signedBytes}.
     * @return the <code>org.bouncycastle.asn1.cms.SignerInfo</code> object for
     * a signer.
     * @throws CertificateEncodingException
     * @throws IOException
     */
    SignerInfo generate() throws CertificateEncodingException, IOException {
        
        AlgorithmIdentifier digAlgId = null;
        AlgorithmIdentifier encAlgId = null;
        
        digAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(this
                .getDigestAlgOID()), new DERNull());

        if (this.getEncryptionAlgOID().equals(
                CMSSignedDataGenerator.ENCRYPTION_DSA)) {
            encAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(this
                    .getEncryptionAlgOID()));
        } else {
            encAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(this
                    .getEncryptionAlgOID()), new DERNull());
        }
        
        
        ASN1OctetString encDigest = new DEROctetString(this.signedBytes);

        X509Certificate cert = this.getCertificate();
        ByteArrayInputStream bIn = new ByteArrayInputStream(cert
                .getTBSCertificate());
        ASN1InputStream aIn = new ASN1InputStream(bIn);
        TBSCertificateStructure tbs = TBSCertificateStructure.getInstance(aIn
                .readObject());
        IssuerAndSerialNumber encSid = new IssuerAndSerialNumber(tbs
                .getIssuer(), cert.getSerialNumber());

        return new SignerInfo(new SignerIdentifier(encSid), digAlgId,
                signedAttr, encAlgId, encDigest, unsignedAttr);
    }

    /**
     * Calculates the bytes to be externally signed (digested and encrypted with signer
     * private key).<br>
     * The bytes are the DER encoding of authenticated attributes; the current
     * implementation includes this attributes:
     * <ul>
     * <li><b>content Type</b></li> of the provided content.
     * <li><b>message Digest</b></li> of the content, calculated in this method with the algorithm
     * specified in the class constructor.
     * <li><b>signing Time</b>. Note that time (internally stored as UTC) should be 
     * presented to the signer BEFORE applying the external signature procedure.<br>
     * This time has not to be confused with a thirdy part (Certification Authority) certified 
     * timestamp ("Marcatura Temporale" in italian terminology); for the italian
     * digital signature law this attribute is not mandatory and could be omitted.
     * Nevertheless, the italian law states also that the signature is valid if the certificate
     * is not expired nor suspended at the time of signature. So an indication of signing time
     * is (in my opinion) however useful.</li>
     * </ul>
     * 
     * 
     * @param contentType the <code>org.bouncycastle.asn1.DERObjectIdentifier</code> of the content.
     * @param content the content to be signed.
     * @param sigProvider the cryptographic provider to use for calculating the digest of the content.
     * @return a <code>byte[]</code> containing the raw bytes to be signed.
     * @throws IOException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException
     * @throws CMSException
     */
    public byte[] getBytesToSign(DERObjectIdentifier contentType,
            CMSProcessable content, String sigProvider) throws IOException,
            SignatureException, InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException, CertificateEncodingException,
            CMSException {


        MessageDigest dig = MessageDigest.getInstance(this.getDigestAlgOID(),
                sigProvider);
        
        content.write(new DigOutputStream(dig));

        byte[] hash = dig.digest();

        AttributeTable attr = this.getSignedAttributes();

        if (attr != null) {
            DEREncodableVector v = new DEREncodableVector();

            if (attr.get(CMSAttributes.contentType) == null) {
                v.add(new Attribute(CMSAttributes.contentType, new DERSet(
                        contentType)));
            } else {
                v.add(attr.get(CMSAttributes.contentType));
            }

            if (attr.get(CMSAttributes.signingTime) == null) {
                v.add(new Attribute(CMSAttributes.signingTime, new DERSet(
                        new Time(new Date()))));
            } else {
                v.add(attr.get(CMSAttributes.signingTime));
            }

            v.add(new Attribute(CMSAttributes.messageDigest, new DERSet(
                    new DEROctetString(hash))));

            Hashtable ats = attr.toHashtable();

            ats.remove(CMSAttributes.contentType);
            ats.remove(CMSAttributes.signingTime);
            ats.remove(CMSAttributes.messageDigest);

            Iterator it = ats.values().iterator();

            while (it.hasNext()) {
                v.add(Attribute.getInstance(it.next()));
            }

            signedAttr = new DERSet(v);
        } else {
            DEREncodableVector v = new DEREncodableVector();

            v.add(new Attribute(CMSAttributes.contentType, new DERSet(
                    contentType)));

            v.add(new Attribute(CMSAttributes.signingTime, new DERSet(
                    new DERUTCTime(new Date()))));

            v.add(new Attribute(CMSAttributes.messageDigest, new DERSet(
                    new DEROctetString(hash))));

            signedAttr = new DERSet(v);

        }

        attr = this.getUnsignedAttributes();

        if (attr != null) {
            Hashtable ats = attr.toHashtable();
            Iterator it = ats.values().iterator();
            DEREncodableVector v = new DEREncodableVector();

            while (it.hasNext()) {
                v.add(Attribute.getInstance(it.next()));
            }

            unsignedAttr = new DERSet(v);
        }

        //
        // sig must be composed from the DER encoding.
        //
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);

        dOut.writeObject(signedAttr);

        return bOut.toByteArray();

    }

    /**
     * @param signedBytes
     *            The signedBytes to set.
     */
    public void setSignedBytes(byte[] signedBytes) {
        this.signedBytes = signedBytes;
    }
}

