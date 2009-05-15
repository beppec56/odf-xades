/**
 * 
 */
package it.plio.ext.oxsit.comp.security.cert;

import java.util.HashMap;
import java.util.Vector;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.asn1.x509.PrivateKeyUsagePeriod;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.qualified.Iso4217CurrencyCode;
import org.bouncycastle.asn1.x509.qualified.MonetaryValue;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.i18n.filter.TrustedInput;

import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/** a simple helper for certificate display, from certificate extension data
 * to human readabel form
 * @author beppe
 *
 */
public class CertificateExtensionDisplayHelper {
	static final String term = System.getProperty("line.separator");
	private boolean m_bDisplayOID;
	XComponentContext m_xCC;
	public CertificateExtensionDisplayHelper(XComponentContext _context, boolean _bDisplayOID) {
		m_bDisplayOID = _bDisplayOID;
		m_xCC = _context;
	}
	/**
	 * @param aext
	 * @param _aOID TODO
	 * @return
	 */
	public String examineExtension(X509Extension aext, DERObjectIdentifier _aOID) {
		try {
			if(_aOID.equals(X509Extensions.KeyUsage))
				return examineKeyUsage(aext);
			else if(_aOID.equals(X509Extensions.CertificatePolicies))
				return examineCertificatePolicies(aext);
			else if(_aOID.equals(X509Extensions.SubjectKeyIdentifier))
				return examineSubjectKeyIdentifier(aext);
			else if(_aOID.equals(X509Extensions.SubjectDirectoryAttributes))
				return examineSubjectDirectoryAttributes(aext);				
			else if(_aOID.equals(X509Extensions.PrivateKeyUsagePeriod))
				return examinePrivateKeyUsagePeriod(aext);				
			else if(_aOID.equals(X509Extensions.SubjectAlternativeName))
				return examineIssuerAlternativeName(aext);	
			else if(_aOID.equals(X509Extensions.IssuerAlternativeName))
				return examineIssuerAlternativeName(aext);	
			else if(_aOID.equals(X509Extensions.QCStatements))
				return examineQCStatements(aext);	
			else if(_aOID.equals(X509Extensions.AuthorityInfoAccess))
				return examineAuthorityInfoAccess(aext);
			else if(_aOID.equals(X509Extensions.AuthorityKeyIdentifier))
				return examineAuthorityKeyIdentifier(aext);
			else if(_aOID.equals(X509Extensions.CRLDistributionPoints))
				return examineCRLDistributionPoints(aext);
			else if(_aOID.equals(X509Extensions.ExtendedKeyUsage))
				return examineExtendedKeyUsage(aext);
			else {
				return Helpers.printHexBytes(aext.getValue().getOctets());
			}
		} catch (java.lang.Exception e) {
			return "Exception while processing OID: " + _aOID.getId() +":"+term+e.toString();
		}
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
	/**
	 * @param aext
	 * @return
	 */
	private String examineSubjectKeyIdentifier(X509Extension aext) {
		SubjectKeyIdentifier ski = SubjectKeyIdentifier.getInstance(aext);
		return Helpers.printHexBytes(ski.getKeyIdentifier());
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineSubjectDirectoryAttributes(X509Extension aext) {
		String stx = "";
		DERObject dbj = X509Extension.convertValueToObject(aext);
		SubjectDirectoryAttributes sda = SubjectDirectoryAttributes.getInstance(dbj);
		/*
		 *     SubjectDirectoryAttributes ::= Attributes
		 *     Attributes ::= SEQUENCE SIZE (1..MAX) OF Attribute
		 *     Attribute ::= SEQUENCE 
		 *     {
		 *       type AttributeType 
		 *       values SET OF AttributeValue 
		 *     }
		 *     
		 *     AttributeType ::= OBJECT IDENTIFIER
		 *     AttributeValue ::= ANY DEFINED BY AttributeType
		 */
		Vector<Attribute> attribv = sda.getAttributes();
		//FIXME: checked for Italy only.
		for(int i = 0; i < attribv.size();i++) {
			Attribute atrb = attribv.get(i);
			ASN1Set asns = atrb.getAttrValues();
			for(int y=0; y<asns.size();y++) {
				if(atrb.getAttrType().equals(X509Name.DATE_OF_BIRTH)) {
					DERGeneralizedTime dgt = DERGeneralizedTime.getInstance(asns.getObjectAt(y));
					stx = stx+"DateOfBirth (OID: "+atrb.getAttrType().getId()+")"+term+" "+dgt.getTime();
				}
				else
					stx= stx+(atrb.getAttrType().getId()+" "+asns.getObjectAt(y).toString());
			}
		}
		return stx;
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examinePrivateKeyUsagePeriod(X509Extension aext) {
		// TODO Auto-generated method stub
		PrivateKeyUsagePeriod pku = PrivateKeyUsagePeriod.getInstance(aext);
		return "to be written..."+Helpers.printHexBytes(aext.getValue().getOctets());
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineIssuerAlternativeName(X509Extension aext) {
		// TODO Auto-generated method stub
		return "to be written..."+Helpers.printHexBytes(aext.getValue().getOctets());
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineQCStatements(X509Extension aext) {
		String stx = "";
		//Prepare to retrieve the QCstatements in Localized language
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess(m_xCC, m_xCC.getServiceManager() );

        ASN1Sequence    dns = (ASN1Sequence)X509Extension.convertValueToObject(aext);
        for(int i= 0; i<dns.size();i++) {
            QCStatement qcs = QCStatement.getInstance(dns.getObjectAt(i));
            if (QCStatement.id_etsi_qcs_QcCompliance.equals(qcs.getStatementId()))  {
                // process statement - just write a notification that the certificate contains this statement
                stx = stx + "QcCompliance (OID: "+qcs.getStatementId()+")"+term;
                try {
					stx = stx + m_aRegAcc.getStringFromRegistry( qcs.getStatementId().getId() )+term;
				} catch (Exception e) {
					stx=stx+e.toString();
				}
            }
            else if(QCStatement.id_qcs_pkixQCSyntax_v1.equals(qcs.getStatementId())) {
                // process statement - just recognize the statement
            	stx=stx+(qcs.getStatementId()+" id_qcs_pkixQCSyntax_v1");
            }
            else if(QCStatement.id_etsi_qcs_QcSSCD.equals(qcs.getStatementId())) {
                // process statement - just write a notification that the certificate contains this statement
                stx = stx+"QcSSCD (OID: "+qcs.getStatementId()+")"+term;
                try {
					stx = stx + m_aRegAcc.getStringFromRegistry( qcs.getStatementId().getId() )+term;
				} catch (Exception e) {
					stx=stx+e.toString();
				}
            }
            else if(QCStatement.id_etsi_qcs_RetentionPeriod.equals(qcs.getStatementId())) {
            	String stxf = "QcEuRetentionPeriod (OID: "+qcs.getStatementId()+")"+term;
            	try {
					stxf = stxf + m_aRegAcc.getStringFromRegistry( qcs.getStatementId().getId() )+term;
				} catch (Exception e) {
					stxf=stxf+e.toString();
				}
            	stx = stx + String.format(stxf,qcs.getStatementInfo().toString());
            }
            else if(QCStatement.id_etsi_qcs_LimiteValue.equals(qcs.getStatementId())) {
            	//FIXME: needs to be tested !
                // process statement - write a notification containing the limit value
                MonetaryValue limit = MonetaryValue.getInstance(qcs.getStatementInfo());
                Iso4217CurrencyCode currency = limit.getCurrency();
                double value = limit.getAmount().doubleValue() * Math.pow(10,limit.getExponent().doubleValue());
                /*
                 * This statement is a statement by the issuer which impose a
                 * limitation on the value of transaction for which this certificate
                 * can be used to the specified amount (MonetaryValue), according to
                 * the Directive 1999/93/EC of the European Parliament and of the
                 * Council of 13 December 1999 on a Community framework for
                 * electronic signatures, as implemented in the law of the country
                 * specified in the issuer field of this certificate.
                 */
                if(currency.isAlphabetic()) {
                    stx = stx+ ("QcEuLimitValue (OID: "+qcs.getStatementId()+")"+
                            new Object[] {currency.getAlphabetic(),
                                          new TrustedInput(new Double(value)),
                                          limit});
                }
                else {
                    stx=stx+(" QcLimitValueNum" +
                            new Object[] {new Integer(currency.getNumeric()),
                                          new TrustedInput(new Double(value)),
                                          limit});
                }
            }
            else
            	stx=stx+(" QcUnknownStatement: "+qcs.getStatementId());
        }
		m_aRegAcc.dispose();
		return stx;
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineAuthorityInfoAccess(X509Extension aext) {
		// TODO Auto-generated method stub
		AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(aext);
		return "to be written..."+Helpers.printHexBytes(aext.getValue().getOctets());
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineAuthorityKeyIdentifier(X509Extension aext) {
		AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(aext);
		return Helpers.printHexBytes(aki.getKeyIdentifier());
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineCRLDistributionPoints(X509Extension aext)	{
		String stx = "";
		DERObject dbj = X509Extension.convertValueToObject(aext);
		CRLDistPoint	crldp = CRLDistPoint.getInstance(dbj);
		DistributionPoint[] dp = crldp.getDistributionPoints();

		for(int i = 0;i < dp.length;i++) {
			DistributionPointName dpn = dp[i].getDistributionPoint();
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

			//				m_aLogger.log(dpn.getName().toString());
			GeneralNames gns = dp[i].getCRLIssuer();
			if(gns != null) {
				gn = gns.getNames();
				for(int y=0; y <gn.length;y++) {
					stx = stx + gn[i].toString() + term;
				}
			}

			ReasonFlags rsf = dp[i].getReasons();
			if(rsf != null ){
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
		return stx;
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineExtendedKeyUsage(X509Extension aext) {
		String stx = "";
		//prepare a reverse lookup of keypurpose id
		//this can be static in another class
		ExtendedKeyUsage eku = ExtendedKeyUsage.getInstance(aext);
		HashMap<KeyPurposeId,String>		ReverseLookUp = new HashMap<KeyPurposeId, String>(22);

		ReverseLookUp.put(KeyPurposeId.anyExtendedKeyUsage,"anyExtendedKeyUsage");
		ReverseLookUp.put(KeyPurposeId.id_kp_serverAuth,"serverAuth");
		ReverseLookUp.put(KeyPurposeId.id_kp_clientAuth,"clientAuth");
		ReverseLookUp.put(KeyPurposeId.id_kp_codeSigning,"codeSigning");
		ReverseLookUp.put(KeyPurposeId.id_kp_emailProtection,"codeSigning");
		ReverseLookUp.put(KeyPurposeId.id_kp_ipsecEndSystem,"ipsecEndSystem");
		ReverseLookUp.put(KeyPurposeId.id_kp_ipsecTunnel,"ipsecTunnel");
		ReverseLookUp.put(KeyPurposeId.id_kp_ipsecUser,"ipsecUser");
		ReverseLookUp.put(KeyPurposeId.id_kp_timeStamping,"timeStamping");
		ReverseLookUp.put(KeyPurposeId.id_kp_OCSPSigning,"OCSPSigning");
		ReverseLookUp.put(KeyPurposeId.id_kp_dvcs,"dvcs");
		ReverseLookUp.put(KeyPurposeId.id_kp_sbgpCertAAServerAuth,"sbgpCertAAServerAuth");
		ReverseLookUp.put(KeyPurposeId.id_kp_scvp_responder,"scvp_responder");
		ReverseLookUp.put(KeyPurposeId.id_kp_eapOverPPP,"eapOverPPP");
		ReverseLookUp.put(KeyPurposeId.id_kp_eapOverLAN,"eapOverLAN");
		ReverseLookUp.put(KeyPurposeId.id_kp_scvpServer,"scvpServer");
		ReverseLookUp.put(KeyPurposeId.id_kp_scvpClient,"scvpClient");
		ReverseLookUp.put(KeyPurposeId.id_kp_ipsecIKE,"ipsecIKE");
		ReverseLookUp.put(KeyPurposeId.id_kp_capwapAC,"capwapAC");
		ReverseLookUp.put(KeyPurposeId.id_kp_capwapWTP,"capwapWTP");
		ReverseLookUp.put(KeyPurposeId.id_kp_smartcardlogon,"smartcardlogon");

		Vector<DERObjectIdentifier> usages = eku.getUsages();
		for(int i = 0; i < usages.size();i++) {
			stx = stx+" "+ReverseLookUp.get(usages.get(i))+
				((m_bDisplayOID) ? (" (OID: "+usages.get(i)+")"):"")+term;
		}
		return stx+term;
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineCertificatePolicies(X509Extension aext) {
		// TODO Auto-generated method stub
		//Italian specific OIDs:
		//1.3.76 == UNINFO
		String stx ="";
		ASN1Sequence cp = (ASN1Sequence)X509Extension.convertValueToObject(aext);
		if(cp != null) {
            for(int i = 0; i < cp.size();i++) {
                PolicyInformation pi = PolicyInformation.getInstance(cp.getObjectAt(i));
                DERObjectIdentifier oid = pi.getPolicyIdentifier();
                if(oid.equals(PolicyQualifierId.id_qt_cps)) {
                	stx = stx + "cps"+
                			((m_bDisplayOID) ? (" (OID: "+oid.getId()+")"):"") 
                			+term;
                }
                else if(oid.equals(PolicyQualifierId.id_qt_unotice)) {
                	stx = stx + "unotice"+
                			((m_bDisplayOID) ? (" (OID: "+oid.getId()+")"):"") 
                			+term;                        	
                }
                else
                	stx=stx+"OID: "+oid.getId()+term;

				ASN1Sequence pqs = (ASN1Sequence)pi.getPolicyQualifiers();
				if(pqs != null) {
					for(int y = 0; y < pqs.size();y++) {
						PolicyQualifierInfo pqi = PolicyQualifierInfo.getInstance(pqs.getObjectAt(y));
                        DERObjectIdentifier oidpqi = pqi.getPolicyQualifierId();
                        if(oidpqi.equals(PolicyQualifierId.id_qt_cps)) {
                        	stx = stx + "cps"+
		                    			((m_bDisplayOID) ? (" (OID: "+oid.getId()+")"):"") 
		                    			+term;
                        }
                        else if(oidpqi.equals(PolicyQualifierId.id_qt_unotice)) {
                        	stx = stx + "unotice"+
		                    			((m_bDisplayOID) ? (" (OID: "+oid.getId()+")"):"") 
		                    			+term;                        	
                        }
                        else
                        	stx=stx+"OID: "+oidpqi.getId();
                        stx = stx + " "+pqi.getQualifier().toString()+term;
					}
				}
            }
		}
		return stx;
	}

	/**
	 * @param aext the X509Extension to be examined
	 */
	private String examineKeyUsage(X509Extension aext) {
		String st = "";
		KeyUsage ku = new KeyUsage( KeyUsage.getInstance(aext) );
		if((ku.intValue() & KeyUsage.digitalSignature) != 0)
			st = st + " digitalSignature"+term;
		if((ku.intValue() & KeyUsage.nonRepudiation) != 0)
			st = st + " nonRepudiation"+term;
		if((ku.intValue() & KeyUsage.keyEncipherment) != 0)
			st = st + " keyEncipherment"+term;
		if((ku.intValue() & KeyUsage.dataEncipherment) != 0)
			st = st + " dataEncipherment"+term;
		if((ku.intValue() & KeyUsage.keyAgreement) != 0)
			st = st + " keyAgreement"+term;
		if((ku.intValue() & KeyUsage.keyCertSign) != 0)
			st = st + " keyCertSign"+term;
		if((ku.intValue() & KeyUsage.cRLSign) != 0)
			st = st + " cRLSign"+term;
		if((ku.intValue() & KeyUsage.encipherOnly) != 0)
			st = st + " encipherOnly"+term;
		if((ku.intValue() & KeyUsage.decipherOnly) != 0)
			st = st + " decipherOnly"+term;
		return st;
	}
}
