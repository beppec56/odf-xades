/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
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

package it.plio.ext.oxsit.comp.security.cert.test;

import it.infocamere.freesigner.gui.ReadCertsTask;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.trento.comune.j4sign.pcsc.CardInReaderInfo;
import it.trento.comune.j4sign.pcsc.CardInfo;
import it.trento.comune.j4sign.pcsc.PCSCHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;

import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class TestOnCertificates {

	
	protected DynamicLogger m_aLogger;
	
	protected XComponentContext m_xCC;

	public TestOnCertificates(XComponentContext _CTX) {
		m_xCC = _CTX;
		m_aLogger = new DynamicLogger(this,_CTX);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
	}

	private class CertInfo {
		public String certName;

		public X509Certificate c;
		public X500Principal subject;
		public X500Principal issuer;

		private X509CertificateStructure xc509;


		/**
		 * Constructor (null)
		 * 
		 */

		public CertInfo() {
			certName = null;
			c = null;
		}

		/**
		 * Constructor
		 * 
		 * @param x
		 *            X509Certificate
		 */
		public CertInfo(X509Certificate x) {
			certName = toCNNames("" + x.getSubjectDN());
			c = x;
//			org.bouncycastle.asn1.ASN1Encodable ab;
//			ASN1Sequence aSeq =  ASN1Object.fromByteArray();
			try {
				byte[] derdata = c.getEncoded();//c.getTBSCertificate();
//
				ByteArrayInputStream as = new ByteArrayInputStream(derdata); 
				ASN1InputStream aderin = new ASN1InputStream(as);
				DERObject ado = aderin.readObject();
				xc509 = new X509CertificateStructure((ASN1Sequence) ado);

			} catch (CertificateEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			subject = c.getSubjectX500Principal();
			issuer = c.getIssuerX500Principal();
		}

		protected void printSubjectNameForNode(X509Name _aName) {
			//extract data from subject name following CNIPA recommendation
			/*
			 * first lookup for givenname and surname, if not existent
			 * lookup for commonName (cn), if not existent
			 * lookup for pseudonym ()
			 */

			//first, grab the OID in the subject name
			Vector<DERObjectIdentifier> oidv = _aName.getOIDs();
			Vector values = _aName.getValues();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			for(int i=0; i< oidv.size(); i++) {
//				m_aLogger.info(oidv.elementAt(i).getId()+" = "+values.elementAt(i)+" "+X509Name.DefaultSymbols.get(oidv.elementAt(i)));
				hm.put(oidv.elementAt(i), values.elementAt(i).toString());
			}
			//look for givename (=nome di battesimo)
			{
				String nome = "<no name>";
				//see BC source code for details about DefaultLookUp behaviour
				DERObjectIdentifier oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("givenname")); 
				if(hm.containsKey(oix)) {
					nome = hm.get(oix).toString();
					oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("surname"));
					if(hm.containsKey(oix))
						nome = nome +" "+hm.get(oix).toString();
				}
				else {
					//check for CN
					oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
					if(hm.containsKey(oix)) {
						nome = hm.get(oix).toString();
					}
					else {
						//if still not, check for pseudodym
						oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
						if(hm.containsKey(oix))
							nome = hm.get(oix).toString();						
					}
				}
				m_aLogger.info(nome);
			}			
		}

		protected void printSubject(X509Name _aName) {			
//print the subject
			//order of printing is as got in the CNIPA spec
			//first, grab the OID in the subject name
			Vector<DERObjectIdentifier> oidv =  _aName.getOIDs();
			Vector values = _aName.getValues();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			String sAname = "\r\n";
			for(int i=0; i< oidv.size(); i++) {
				sAname = sAname + X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
						" (OID: "+oidv.elementAt(i).toString()+") \r\n";
				hm.put(oidv.elementAt(i), values.elementAt(i).toString());
			}
			m_aLogger.info(sAname);
		}
		
		public String printCert() {
			
			printSubjectNameForNode(xc509.getSubject());
			
			m_aLogger.info("Version: V"+c.getVersion());
			
			m_aLogger.info("Serial number: "+xc509.getSerialNumber().getValue());
			
			printSubject(xc509.getIssuer());
			
//			m_aLogger.info("Issuer:  "+xc509.getIssuer().toString());

			Date notBefore = xc509.getStartDate().getDate();				
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(notBefore);	
	//string with time only
			String time = String.format("%1$tb %1$td %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)", calendar);
			m_aLogger.info("Valid not before: "+time);

			Date notAfter = xc509.getEndDate().getDate();
			calendar.setTime(notAfter);	
			//string with time only
			time = String.format("%1$tb %1$td %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)", calendar);
			m_aLogger.info("Valid not after:  "+time);

			printSubject(xc509.getSubject());

			AlgorithmIdentifier aid = xc509.getSignatureAlgorithm();
			DERObjectIdentifier oi = aid.getObjectId();

			m_aLogger.info("Subject Public Signature Algorithm: "+((
					xc509.getSubjectPublicKeyInfo().getAlgorithmId().getObjectId().equals(X509CertificateStructure.rsaEncryption)) ?
							"pkcs-1 rsaEncryption" : oi.getId()
							));
			
			byte[] sbjkd = xc509.getSubjectPublicKeyInfo().getPublicKeyData().getBytes();
			
			String keydatas = "";
			for(int i = 0; i < sbjkd.length;i++) {
				try {
					keydatas = keydatas + String.format(" %02X", (sbjkd[i] & 0xff) );
				} catch(IllegalFormatException e) {
					m_aLogger.severe("", e);
				}
				if(i !=  0 && (i+1) % 16 == 0)
					keydatas = keydatas + "\n";
			}
			m_aLogger.info("Subject Public Key Data:\n"+keydatas);

			
			m_aLogger.info("Signature Algorithm: "+((
					xc509.getSignatureAlgorithm().getObjectId().equals(X509CertificateStructure.sha1WithRSAEncryption)) ? 
							"pkcs-1 sha1WithRSAEncryption" : oi.getId()));

			sbjkd = xc509.getSignature().getBytes();
			keydatas = "";
			for(int i = 0; i < sbjkd.length;i++) {
				try {
					keydatas = keydatas + String.format(" %02X", (sbjkd[i] & 0xff) );
				} catch(IllegalFormatException e) {
					m_aLogger.severe("", e);
				}
				if(i !=  0 && (i+1) % 16 == 0)
					keydatas = keydatas + "\n";
			}
			m_aLogger.info("Signature Data:\n"+keydatas);
			
			//obtain a byte block of the certificate data
			TBSCertificateStructure tbsCert = xc509.getTBSCertificate();
			ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
			DEROutputStream         dOut = new DEROutputStream(bOut);
			try {
				dOut.writeObject(tbsCert);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] certBlock = bOut.toByteArray();

			//now compute the SHA1 & MD5

			
			// print thumbprint SHA1 & MD5 of certificate data
			/*
		logger.debug("Certificate structure generated, creating SHA1 digest");
		// attention: hard coded to be SHA1+RSA!
		SHA1Digest digester = new SHA1Digest();
		AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
		TBSCertificateStructure tbsCert = certGen.generateTBSCertificate();

		ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
		DEROutputStream         dOut = new DEROutputStream(bOut);
		dOut.writeObject(tbsCert);

		// and now sign
		byte[] signature;
		if (useBCAPI) {
			byte[] certBlock = bOut.toByteArray();
			// first create digest
			logger.debug("Block to sign is '" + new String(Hex.encodeHex(certBlock)) + "'");		
			digester.update(certBlock, 0, certBlock.length);
			byte[] hash = new byte[digester.getDigestSize()];
			digester.doFinal(hash, 0);
			// and sign that
			rsa.init(true, caPrivateKey);
			DigestInfo dInfo = new DigestInfo( new AlgorithmIdentifier(X509ObjectIdentifiers.id_SHA1, null), hash);
			byte[] digest = dInfo.getEncoded(ASN1Encodable.DER);
			signature = rsa.processBlock(digest, 0, digest.length);
		}
		else {
			// or the JCE way
	        PrivateKey caPrivKey = KeyFactory.getInstance("RSA").generatePrivate(
	        		new RSAPrivateCrtKeySpec(caPrivateKey.getModulus(), caPrivateKey.getPublicExponent(),
	        				caPrivateKey.getExponent(), caPrivateKey.getP(), caPrivateKey.getQ(), 
	        				caPrivateKey.getDP(), caPrivateKey.getDQ(), caPrivateKey.getQInv()));
			
	        Signature sig = Signature.getInstance(sigOID.getId());
	        sig.initSign(caPrivKey, sr);
	        sig.update(bOut.toByteArray());
	        signature = sig.sign();
		}
		logger.debug("SHA1/RSA signature of digest is '" + new String(Hex.encodeHex(signature)) + "'");

			 
			 */
			
			//print extensions
			
			
			//print the certificate path
			printSubjectNameForNode(xc509.getIssuer());
			return "";
		}

		public X509Certificate getCertificate() {
			return c;
		}

		public void setCertificate(X509Certificate x) {
			c = x;
		}

		public void setName(String s) {
			certName = s;
		}

	}

	private String toCNNames(String DN) {

		int offset = DN.indexOf("CN=");
		int end = DN.indexOf(",", offset);
		String CN;
		if (end != -1) {
			CN = DN.substring(offset + 3, end);
		} else {
			CN = DN.substring(offset + 3, DN.length());
		}
		CN = CN.substring(0, CN.length());
		return CN;

	}

	public void testMethod() {
		// TODO Auto-generated method stub
		m_aLogger.entering("testMethod");

		PCSCHelper pcsc = new PCSCHelper(true);

		m_aLogger.log("After 'new PCSCHelper'");

		java.util.List<CardInReaderInfo> infos = pcsc.findCardsAndReaders();

		CardInfo ci = null;
		Iterator<CardInReaderInfo> it = infos.iterator();
		int indexToken = 0;
		int indexReader = 0;

		while (it.hasNext()) {
			m_aLogger.log("Reader " + indexReader + ")");

			CardInReaderInfo cIr = it.next();
			String currReader = cIr.getReader();

			ci = cIr.getCard();
			
			if (ci != null) {
				
				m_aLogger.log("Informations found for this card:");
				m_aLogger.log("\tDescription:\t"
						+ ci.getProperty("description"));
				m_aLogger.log("\tManufacturer:\t"
						+ ci.getProperty("manufacturer"));
				m_aLogger.log("\tATR:\t\t" + ci.getProperty("atr"));
				m_aLogger.log("\tCriptoki:\t" + ci.getProperty("lib"));
				
				m_aLogger.log("\n\tLettura certificati");
				
				ReadCertsTask rt = new ReadCertsTask(cIr);
				Collection certsOnToken = rt.getCertsOnToken();
				if (certsOnToken != null) {
					Iterator certIt = certsOnToken.iterator();
					if (certsOnToken.isEmpty()) {
						m_aLogger.log("\tcertsOnToken vuoto");
						CertInfo c = new CertInfo();
						c.setName("Carta presente ma vuota");

					}
					while (certIt.hasNext()) {

						m_aLogger.log("******************************************  &&  ****************  &&  *********************************************************");
						X509Certificate cert = (X509Certificate) certIt.next();
						
						CertInfo c = new CertInfo(cert);
						m_aLogger.log(c.printCert());
						m_aLogger.log("*******************************************************************************************************************");

					}

					rt.closeSession();
					rt.libFinalize();
					indexToken++;
				}

			} else {
				m_aLogger.log("No card in reader '" + currReader + "'!");

			}

			indexReader++;
		}		
	}

}
