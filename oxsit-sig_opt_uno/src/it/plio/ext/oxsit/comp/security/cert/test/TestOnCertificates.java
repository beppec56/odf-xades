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
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.comp.security.cert.CertificateExtensionDisplayHelper;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.pcsc.CardInReaderInfo;
import it.plio.ext.oxsit.pcsc.CardInfoOOo;
import it.plio.ext.oxsit.pcsc.PCSCHelper;
import it.plio.ext.oxsit.security.ReadCerts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;

import com.sun.star.uno.Exception;
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

		private String term = System.getProperty("line.separator");

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
			Vector<?> values = _aName.getValues();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			for(int i=0; i< oidv.size(); i++) {
//				m_aLoggerDialog.info(oidv.elementAt(i).getId()+" = "+values.elementAt(i)+" "+X509Name.DefaultSymbols.get(oidv.elementAt(i)));
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

		protected void printX509Name(X509Name _aName) {			
//print the subject
			//order of printing is as got in the CNIPA spec
			//first, grab the OID in the subject name
			Vector<DERObjectIdentifier> oidv =  _aName.getOIDs();
			Vector<?> values = _aName.getValues();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			String sAname = "\r\n";
			for(int i=0; i< oidv.size(); i++) {
				sAname = sAname + X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
						" (OID: "+oidv.elementAt(i).toString()+") \r\n";
				hm.put(oidv.elementAt(i), values.elementAt(i).toString());
			}
			m_aLogger.info(sAname);
		}
		
		protected String printCertDate(Date _aTime) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(_aTime);	
	//string with time only
			String time = String.format("%1$tb %1$td %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)", calendar);
			return time;
		}

		public String printCert() {
			
			printSubjectNameForNode(xc509.getSubject());
			m_aLogger.info("Version: V"+c.getVersion());
			m_aLogger.info("Serial number: "+xc509.getSerialNumber().getValue());
			printX509Name(xc509.getIssuer());
			m_aLogger.info("Valid not before: "+printCertDate(xc509.getStartDate().getDate()));
			m_aLogger.info("Valid not after:  "+printCertDate(xc509.getEndDate().getDate()));
			printX509Name(xc509.getSubject());

			AlgorithmIdentifier aid = xc509.getSignatureAlgorithm();
			DERObjectIdentifier oi = aid.getObjectId();

			m_aLogger.info("Subject Public Signature Algorithm: "+((
					xc509.getSubjectPublicKeyInfo().getAlgorithmId().getObjectId().equals(X509CertificateStructure.rsaEncryption)) ?
							"pkcs-1 rsaEncryption" : oi.getId()
							));

			byte[] sbjkd = xc509.getSubjectPublicKeyInfo().getPublicKeyData().getBytes();

			m_aLogger.info("Subject Public Key Data:\n"+Helpers.printHexBytes(sbjkd));

			m_aLogger.info("Signature Algorithm: "+((
					xc509.getSignatureAlgorithm().getObjectId().equals(X509CertificateStructure.sha1WithRSAEncryption)) ? 
							"pkcs-1 sha1WithRSAEncryption" : oi.getId()));

/*			sbjkd = xc509.getSignature().getBytes();
			m_aLoggerDialog.info("Signature Data:\n"+Helpers.printHexBytes(sbjkd));*/

			//obtain a byte block of the entire certificate data
			ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
			DEROutputStream         dOut = new DEROutputStream(bOut);
			try {
				dOut.writeObject(xc509);
				byte[] certBlock = bOut.toByteArray();

				//now compute the certificate SHA1 & MD5 digest
				SHA1Digest digsha1 = new SHA1Digest();
				digsha1.update(certBlock, 0, certBlock.length);
				byte[] hashsha1 = new byte[digsha1.getDigestSize()];
				digsha1.doFinal(hashsha1, 0);
				m_aLogger.info("Certificate SHA1 Thumbprint: "+Helpers.printHexBytes(hashsha1));
				MD5Digest  digmd5 = new MD5Digest();
				digmd5.update(certBlock, 0, certBlock.length);
				byte[] hashmd5 = new byte[digmd5.getDigestSize()];
				digmd5.doFinal(hashmd5, 0);
				m_aLogger.info("Certificate MD5 Thumbprint: "+Helpers.printHexBytes(hashmd5));

			} catch (IOException e) {
				e.printStackTrace();
				m_aLogger.severe("certif data print!", e);
			}
			
			//print extensions
			X509Extensions xc509Ext = xc509.getTBSCertificate().getExtensions();
			
			Vector<DERObjectIdentifier> extoid = new Vector();
			for(Enumeration<DERObjectIdentifier> enume = xc509Ext.oids(); enume.hasMoreElements();) {
				extoid.add(enume.nextElement());
			}			
			CertificateExtensionDisplayHelper aHelper = new CertificateExtensionDisplayHelper(m_xCC,new Locale("it"), "", true,
					new DynamicLoggerDialog(this,m_xCC));
			MessageConfigurationAccess m_aRegAcc = null;
			m_aRegAcc = new MessageConfigurationAccess(m_xCC, m_xCC.getServiceManager() );
			
			//first the critical one
			m_aLogger.info("Critical Extensions:");
			for(int i=0; i<extoid.size();i++) {
				X509Extension aext = xc509Ext.getExtension(extoid.get(i));
				if(aext.isCritical())
					try {
						m_aLogger.log(extoid.get(i).getId()+ " "+m_aRegAcc.getStringFromRegistry(extoid.get(i).getId())+term+
								aHelper.examineExtension(aext, extoid.get(i), null));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			m_aLogger.info("Non Critical Extensions:");
			for(int i=0; i<extoid.size();i++) {
				X509Extension aext = xc509Ext.getExtension(extoid.get(i));
				if(!aext.isCritical())
					try {
						m_aLogger.log(extoid.get(i).getId()+ " "+m_aRegAcc.getStringFromRegistry(extoid.get(i).getId())+term+
								aHelper.examineExtension(aext, extoid.get(i), null));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
			m_aRegAcc.dispose();
			
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

		PCSCHelper pcsc = new PCSCHelper(null,true, null, m_aLogger);

		m_aLogger.log("After 'new PCSCHelper'");

		java.util.List<CardInReaderInfo> infos = pcsc.findCardsAndReaders();

		CardInfoOOo ci = null;
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
						+ ci.m_sDescription);
				m_aLogger.log("\tManufacturer:\t"
						+ ci.m_sManufacturer);
				m_aLogger.log("\tATR:\t\t" + ci.m_sATRCode);
				m_aLogger.log("\tCriptoki:\t" + ci.m_sOsLib);

				m_aLogger.log("\tLettura certificati");
				
				ReadCerts rt = new ReadCerts(null, m_aLogger, currReader, cIr);
				Collection<?> certsOnToken = rt.getCertsOnToken();
				if (certsOnToken != null) {
					Iterator<?> certIt = certsOnToken.iterator();
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
