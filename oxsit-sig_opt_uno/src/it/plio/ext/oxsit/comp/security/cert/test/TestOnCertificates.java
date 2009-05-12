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
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.trento.comune.j4sign.pcsc.CardInReaderInfo;
import it.trento.comune.j4sign.pcsc.CardInfo;
import it.trento.comune.j4sign.pcsc.PCSCHelper;

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
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;

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
			Vector<?> values = _aName.getValues();
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
			m_aLogger.info("Signature Data:\n"+Helpers.printHexBytes(sbjkd));*/

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
			
			//first the critical one
			m_aLogger.info("Critical Extensions:");
			for(int i=0; i<extoid.size();i++) {
				X509Extension aext = xc509Ext.getExtension(extoid.get(i));
				if(aext.isCritical())
					examineExtension(extoid.get(i));
			}
			
			m_aLogger.info("Non Critical Extensions:");
			for(int i=0; i<extoid.size();i++) {
				X509Extension aext = xc509Ext.getExtension(extoid.get(i));
				if(!aext.isCritical())
					examineExtension(extoid.get(i));
			}
			//then the not critical
			
			
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
		
		protected void examineExtension(DERObjectIdentifier _oid) {
			X509Extension aext = xc509.getTBSCertificate().getExtensions().getExtension(_oid);
			m_aLogger.log(""+_oid.toString());
			if(_oid.equals(X509Extensions.KeyUsage)) {
				KeyUsage ku = new KeyUsage( KeyUsage.getInstance(aext) );
				examineKeyUsage(ku);
			}
			else if(_oid.equals(X509Extensions.CertificatePolicies)) {
				//CertificatePolicies cp = CertificatePolicies.getInstance(aext);
				m_aLogger.log("CertificatePolicies");
			}
			else if(_oid.equals(X509Extensions.SubjectKeyIdentifier)) {
				//SubjectKeyIdentifier ski = SubjectKeyIdentifier.getInstance(aext);
				m_aLogger.log("SubjectKeyIdentifier");
			}
			else if(_oid.equals(X509Extensions.SubjectDirectoryAttributes)) {
				m_aLogger.log("SubjectDirectoryAttributes");
			}
			else if(_oid.equals(X509Extensions.PrivateKeyUsagePeriod)) {
				m_aLogger.log("PrivateKeyUsagePeriod");
			}
			else if(_oid.equals(X509Extensions.SubjectAlternativeName)) {
				m_aLogger.log("SubjectAlternativeName");
			}
			else if(_oid.equals(X509Extensions.IssuerAlternativeName)) {
				m_aLogger.log("IssuerAlternativeName");
			}
			else if(_oid.equals(X509Extensions.QCStatements)) {
				m_aLogger.log("QCStatements");
			}
			else if(_oid.equals(X509Extensions.AuthorityInfoAccess)) {
				m_aLogger.log("AuthorityInfoAccess");
			}
			else if(_oid.equals(X509Extensions.AuthorityKeyIdentifier)) {
				m_aLogger.log("AuthorityKeyIdentifier");
			}
			else if(_oid.equals(X509Extensions.CRLDistributionPoints)) {
				try { 
					DERObject dbj = X509Extension.convertValueToObject(aext);
					CRLDistPoint	crldp = CRLDistPoint.getInstance(dbj);
					examineCRLDistributionPoints(crldp);
				} catch (IllegalArgumentException e) {
					m_aLogger.severe("CRLDistributionPoints",e);
				}
			}
			else if(_oid.equals(X509Extensions.ExtendedKeyUsage)) {
				ExtendedKeyUsage eku = ExtendedKeyUsage.getInstance(aext);
				examineExtendedKeyUsage(eku);
			}
/*
    public static final DERObjectIdentifier BasicConstraints = new DERObjectIdentifier("2.5.29.19");
    public static final DERObjectIdentifier CRLNumber = new DERObjectIdentifier("2.5.29.20");
    public static final DERObjectIdentifier ReasonCode = new DERObjectIdentifier("2.5.29.21");
    public static final DERObjectIdentifier InstructionCode = new DERObjectIdentifier("2.5.29.23");
    public static final DERObjectIdentifier InvalidityDate = new DERObjectIdentifier("2.5.29.24");
    public static final DERObjectIdentifier DeltaCRLIndicator = new DERObjectIdentifier("2.5.29.27");
    public static final DERObjectIdentifier IssuingDistributionPoint = new DERObjectIdentifier("2.5.29.28");
    public static final DERObjectIdentifier CertificateIssuer = new DERObjectIdentifier("2.5.29.29");
    public static final DERObjectIdentifier NameConstraints = new DERObjectIdentifier("2.5.29.30");
    public static final DERObjectIdentifier PolicyMappings = new DERObjectIdentifier("2.5.29.33");
    public static final DERObjectIdentifier PolicyConstraints = new DERObjectIdentifier("2.5.29.36");
    public static final DERObjectIdentifier FreshestCRL = new DERObjectIdentifier("2.5.29.46");
    public static final DERObjectIdentifier InhibitAnyPolicy = new DERObjectIdentifier("2.5.29.54");
    public static final DERObjectIdentifier SubjectInfoAccess = new DERObjectIdentifier("1.3.6.1.5.5.7.1.11");
    public static final DERObjectIdentifier LogoType = new DERObjectIdentifier("1.3.6.1.5.5.7.1.12");
    public static final DERObjectIdentifier BiometricInfo = new DERObjectIdentifier("1.3.6.1.5.5.7.1.2");
    public static final DERObjectIdentifier AuditIdentity = new DERObjectIdentifier("1.3.6.1.5.5.7.1.4");
    public static final DERObjectIdentifier NoRevAvail = new DERObjectIdentifier("2.5.29.56");
    public static final DERObjectIdentifier TargetInformation = new DERObjectIdentifier("2.5.29.55");
 */
		}
		
		/**
		 * @param ku
		 */
		private void examineKeyUsage(KeyUsage ku) {
			m_aLogger.log("Key Usage:");
			String st = "";
			if((ku.intValue() & KeyUsage.digitalSignature) != 0)
				st = st + " digitalSignature";
			if((ku.intValue() & KeyUsage.nonRepudiation) != 0)
				st = st + " nonRepudiation";
			if((ku.intValue() & KeyUsage.keyEncipherment) != 0)
				st = st + " keyEncipherment";
			if((ku.intValue() & KeyUsage.dataEncipherment) != 0)
				st = st + " dataEncipherment";
			if((ku.intValue() & KeyUsage.keyAgreement) != 0)
				st = st + " keyAgreement";
			if((ku.intValue() & KeyUsage.keyCertSign) != 0)
				st = st + " keyCertSign";
			if((ku.intValue() & KeyUsage.cRLSign) != 0)
				st = st + " cRLSign";
			if((ku.intValue() & KeyUsage.encipherOnly) != 0)
				st = st + " encipherOnly";
			if((ku.intValue() & KeyUsage.decipherOnly) != 0)
				st = st + " decipherOnly";
			m_aLogger.log(st);			
		}

		/**
		 * @param crldp 
		 * 
		 */
		private void examineCRLDistributionPoints(CRLDistPoint crldp) {
			// TODO Auto-generated method stub

			String term = System.getProperty("line.separator");
			String stx = "CRLDistributionPoints:"+term;
			try {
				DistributionPoint[] dp = crldp.getDistributionPoints();

				for(int i = 0;i < dp.length;i++) {
					DistributionPointName dpn = dp[i].getDistributionPoint();

					//				m_aLogger.log(dpn.toString());
					{
						//custom toString
						if(dpn.getType() == DistributionPointName.FULL_NAME) {
							stx = stx+"fullName:" + term;
						}
						else {
							stx = stx+"nameRelativeToCRLIssuer:" + term;						
						}
						GeneralNames gnx = GeneralNames.getInstance(dpn.getName());
						GeneralName[] gn = gnx.getNames();
						for(int y=0; y <gn.length;y++) {
//							stx = stx + gn[y].toString() + term;
							//set type
							switch(gn[y].getTagNo())
							{
							case GeneralName.otherName:
								stx = stx +"otherName: ";
								break;
							case GeneralName.rfc822Name:
								stx = stx +"rfc822Name: ";
								break;
							case GeneralName.dNSName:
								stx = stx +"dNSName: ";
								break;
							case GeneralName.x400Address:
								stx = stx +"x400Address: ";
								break;
							case GeneralName.directoryName:
								stx = stx +"directoryName: ";
								break;
							case GeneralName.ediPartyName:
								stx = stx +"ediPartyName: ";
								break;
							case GeneralName.uniformResourceIdentifier:
								stx = stx +"uniformResourceIdentifier: ";
								break;
							case GeneralName.iPAddress:
								stx = stx +"iPAddress: ";
								break;
							case GeneralName.registeredID:
								stx = stx +"registeredID: ";
								break;							
							}
					        switch (gn[y].getTagNo())
					        {
					        case GeneralName.rfc822Name:
					        case GeneralName.dNSName:
					        case GeneralName.uniformResourceIdentifier:
					        	stx = stx + DERIA5String.getInstance(gn[y].getName()).getString();
					            break;
					        case GeneralName.directoryName:
					        	stx = stx + X509Name.getInstance(gn[y].getName()).toString();
					            break;
					        default:
					        	stx = stx + gn[y].toString();
					        }
						}
						stx = stx + term;
					}

					//				m_aLogger.log(dpn.getName().toString());
					GeneralNames gns = dp[i].getCRLIssuer();
					if(gns != null) {
						GeneralName[] gn = gns.getNames();
						for(int y=0; y <gn.length;y++) {
							stx = stx + gn[i].toString() + term;
						}
					}

					ReasonFlags rsf = dp[i].getReasons();
					if(rsf != null ){
						m_aLogger.log("Reason flags:");
						if((rsf.intValue() & ReasonFlags.unused) != 0)
							stx = stx + " unused";			    
						if((rsf.intValue() & ReasonFlags.keyCompromise) != 0)
							stx = stx + " keyCompromise";
						if((rsf.intValue() & ReasonFlags.cACompromise) != 0)
							stx = stx + " cACompromise";
						if((rsf.intValue() & ReasonFlags.affiliationChanged) != 0)
							stx = stx + " affiliationChanged";
						if((rsf.intValue() & ReasonFlags.superseded) != 0)
							stx = stx + " superseded";
						if((rsf.intValue() & ReasonFlags.cessationOfOperation) != 0)
							stx = stx + " cessationOfOperation";
						if((rsf.intValue() & ReasonFlags.certificateHold) != 0)
							stx = stx + " certificateHold";
						if((rsf.intValue() & ReasonFlags.privilegeWithdrawn) != 0)
							stx = stx + " privilegeWithdrawn";
						if((rsf.intValue() & ReasonFlags.aACompromise) != 0)
							stx = stx + " aACompromise";
						stx = stx + term;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			m_aLogger.log(stx);
		}

		protected void examineExtendedKeyUsage(ExtendedKeyUsage eku) {
			//prepare a reverse lookup of keypurpose id
			//this can be static in another class
			HashMap<KeyPurposeId,String>		ReverseLookUp = new HashMap<KeyPurposeId, String>(22);
			
			ReverseLookUp.put(KeyPurposeId.anyExtendedKeyUsage,"anyExtendedKeyUsage");
			ReverseLookUp.put(KeyPurposeId.id_kp_serverAuth,"id-kp-serverAuth");
			ReverseLookUp.put(KeyPurposeId.id_kp_clientAuth,"id-kp-clientAuth");
			ReverseLookUp.put(KeyPurposeId.id_kp_codeSigning,"id-kp-codeSigning");
			ReverseLookUp.put(KeyPurposeId.id_kp_emailProtection,"id-kp-codeSigning");
			ReverseLookUp.put(KeyPurposeId.id_kp_ipsecEndSystem,"id-kp-ipsecEndSystem");
			ReverseLookUp.put(KeyPurposeId.id_kp_ipsecTunnel,"id-kp-ipsecTunnel");
			ReverseLookUp.put(KeyPurposeId.id_kp_ipsecUser,"id-kp-ipsecUser");
			ReverseLookUp.put(KeyPurposeId.id_kp_timeStamping,"id-kp-timeStamping");
			ReverseLookUp.put(KeyPurposeId.id_kp_OCSPSigning,"id-kp-OCSPSigning");
			ReverseLookUp.put(KeyPurposeId.id_kp_dvcs,"id-kp-dvcs");
			ReverseLookUp.put(KeyPurposeId.id_kp_sbgpCertAAServerAuth,"id-kp-sbgpCertAAServerAuth");
			ReverseLookUp.put(KeyPurposeId.id_kp_scvp_responder,"id-kp-scvp_responder");
			ReverseLookUp.put(KeyPurposeId.id_kp_eapOverPPP,"id-kp-eapOverPPP");
			ReverseLookUp.put(KeyPurposeId.id_kp_eapOverLAN,"id-kp-eapOverLAN");
			ReverseLookUp.put(KeyPurposeId.id_kp_scvpServer,"id-kp-scvpServer");
			ReverseLookUp.put(KeyPurposeId.id_kp_scvpClient,"id-kp-scvpClient");
			ReverseLookUp.put(KeyPurposeId.id_kp_ipsecIKE,"id-kp-ipsecIKE");
			ReverseLookUp.put(KeyPurposeId.id_kp_capwapAC,"id-kp-capwapAC");
			ReverseLookUp.put(KeyPurposeId.id_kp_capwapWTP,"id-kp-capwapWTP");
			ReverseLookUp.put(KeyPurposeId.id_kp_smartcardlogon,"id-kp-smartcardlogon");

			m_aLogger.log("ExtendedKeyUsage");
			Vector<DERObjectIdentifier> usages = eku.getUsages();
			for(int i = 0; i < usages.size();i++)
				m_aLogger.log("  "+usages.get(i).getId()+": "+ReverseLookUp.get(usages.get(i)));			
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
