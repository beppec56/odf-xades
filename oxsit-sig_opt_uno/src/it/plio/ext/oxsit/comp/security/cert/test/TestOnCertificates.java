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
import java.io.IOException;
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
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
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

		protected void printSubjectNameForNode() {
			X509Name aSubject = xc509.getSubject();
			//extract data from subject name following CNIPA recommendation
			/*
			 * first lookup for givenname and surname, if not existent
			 * lookup for commonName (cn), if not existent
			 * lookup for pseudonym ()
			 */

			//first, grab the OID in the subject name
			Vector<DERObjectIdentifier> oidv = aSubject.getOIDs();
			Vector values = aSubject.getValues();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			for(int i=0; i< oidv.size(); i++) {
//				m_aLogger.info(oidv.elementAt(i).getId()+" = "+values.elementAt(i)+" "+X509Name.DefaultSymbols.get(oidv.elementAt(i)));
				hm.put(oidv.elementAt(i), values.elementAt(i).toString());
			}
			//look for givename (=nome di battesimo)
			{
				//see BC source code for details about DefaultLookUp behaviour
				DERObjectIdentifier oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("givenname")); 
				if(hm.containsKey(oix)) {
					String nome = hm.get(oix).toString();
					oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("surname"));
					if(hm.containsKey(oix))
						m_aLogger.info(nome+" "+hm.get(oix).toString());				
				}
				else {
					//check for CN				
					//if still not, check for pseudodym

				}
			}			
		}
		
		protected void printSubject() {			
//print the subject
			//order of printing is as got in the CNIPA spec
			//first, grab the OID in the subject name
			Vector<DERObjectIdentifier> oidv =  xc509.getSubject().getOIDs();
			Vector values = xc509.getSubject().getValues();
			HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
			for(int i=0; i< oidv.size(); i++) {
				m_aLogger.info(X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
						" (OID: "+oidv.elementAt(i).toString()+")");
				hm.put(oidv.elementAt(i), values.elementAt(i).toString());
			}			
//			givenName
			
			DERObjectIdentifier oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("givenname"));
			if(hm.containsKey(oix))
				m_aLogger.info("givenName="+hm.get(oix).toString());
//			surname (OID: 2.5.4.42 e 2.5.4.4)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("surname"));
			 if(hm.containsKey(oix))
					m_aLogger.info("surname="+hm.get(oix).toString());			

//			countryName (OID: 2.5.4.6)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("c"));
			 if(hm.containsKey(oix))
					m_aLogger.info("countryCode="+hm.get(oix).toString());			
			
	//		organizationName (OID: 2.5.4.10)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("o"));
			 if(hm.containsKey(oix))
					m_aLogger.info("organizationName="+hm.get(oix).toString());			
			
		//	serialNumber (OID: 2.5.4.5)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("sn"));
			 if(hm.containsKey(oix))
					m_aLogger.info("serialNumber="+hm.get(oix).toString());			
			
			//pseudonym (OID: 2.5.4.65)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
			 if(hm.containsKey(oix))
					m_aLogger.info("pseudonym="+hm.get(oix).toString());

			//dnQualifier (OID: 2.5.4.46)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("dn"));
			 if(hm.containsKey(oix))
					m_aLogger.info("dnQualifier="+hm.get(oix).toString());

			//title (OID: 2.5.4.12)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("t"));
			 if(hm.containsKey(oix))
					m_aLogger.info("title="+hm.get(oix).toString());

			//localityName (OID: 2.5.4.7)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("l"));
			 if(hm.containsKey(oix))
					m_aLogger.info("localityName="+hm.get(oix).toString());

			//commonName (OID: 2.5.4.3)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn"));
			 if(hm.containsKey(oix))
					m_aLogger.info("commonName="+hm.get(oix).toString());

			//organizationalUnitName (OID: 2.5.4.11)
			 oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("ou"));
			 if(hm.containsKey(oix))
					m_aLogger.info("organizationalUnitName="+hm.get(oix).toString());			

			m_aLogger.info("Subject: "+xc509.getSubject().toString());

		}
		
		public String printCert() {
			
			printSubjectNameForNode();
			
			m_aLogger.info("Version: V"+c.getVersion());

			m_aLogger.info("Serial number: "+xc509.getSerialNumber().getValue().toString());
			m_aLogger.info("Issuer:  "+xc509.getIssuer().toString());

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

			printSubject();

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
