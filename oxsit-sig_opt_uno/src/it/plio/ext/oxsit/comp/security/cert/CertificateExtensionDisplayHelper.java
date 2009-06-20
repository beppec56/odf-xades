/**
 * 
 */
package it.plio.ext.oxsit.comp.security.cert;

import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.cert.XOX_X509CertificateDisplay;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
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

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/** a simple helper for certificate display, from certificate extension data
 * to human readabel form
 * @author beppe
 *
 */
public class CertificateExtensionDisplayHelper {
	static final String term = System.getProperty("line.separator");
	
	static final Hashtable		m_aKeyPurposeIdReverseLookUp = new Hashtable();

	static {
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.anyExtendedKeyUsage,"anyExtendedKeyUsage");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_serverAuth,"serverAuth");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_clientAuth,"clientAuth");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_codeSigning,"codeSigning");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_emailProtection,"codeSigning");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_ipsecEndSystem,"ipsecEndSystem");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_ipsecTunnel,"ipsecTunnel");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_ipsecUser,"ipsecUser");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_timeStamping,"timeStamping");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_OCSPSigning,"OCSPSigning");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_dvcs,"dvcs");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_sbgpCertAAServerAuth,"sbgpCertAAServerAuth");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_scvp_responder,"scvp_responder");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_eapOverPPP,"eapOverPPP");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_eapOverLAN,"eapOverLAN");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_scvpServer,"scvpServer");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_scvpClient,"scvpClient");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_ipsecIKE,"ipsecIKE");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_capwapAC,"capwapAC");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_capwapWTP,"capwapWTP");
		m_aKeyPurposeIdReverseLookUp.put(KeyPurposeId.id_kp_smartcardlogon,"smartcardlogon");
	};

	protected IDynamicLogger m_aLogger; 
//	protected DynamicLoggerDialog m_aLogger; 

	private boolean m_bDisplayOID;
	XComponentContext m_xCC;

	private Locale m_lTheLocale;

	private String m_sTimeLocaleString;

	private String m_sLocaleDateOfBirth;

	public CertificateExtensionDisplayHelper(XComponentContext _context, Locale _aLocale, 
						String _timeloc, String _date_of_birth_locale, boolean _bDisplayOID, IDynamicLogger _aLogger ) {
		m_lTheLocale = _aLocale;
		m_sTimeLocaleString = _timeloc;
		m_sLocaleDateOfBirth = _date_of_birth_locale;
		m_bDisplayOID = _bDisplayOID;
		m_xCC = _context;
		if(_aLogger != null)
			m_aLogger = _aLogger;
		else
			m_aLogger = new DynamicLogger(this,_context);
//		m_aLogger.enableLogging();
	}

	public CertificateExtensionDisplayHelper(XComponentContext _context, Locale _aLocale, 
						String _timeloc,  boolean _bDisplayOID) {
		m_sTimeLocaleString = _timeloc;
		m_lTheLocale = _aLocale;
		m_bDisplayOID = _bDisplayOID;
		m_xCC = _context;
		m_aLogger = new DynamicLogger(this,_context);
//		m_aLogger.enableLogging();
	}

	/**
	 * @param aext
	 * @param _aOID the extension OID
	 * @param _xCert the certificate containing the extension
	 * @return
	 */
	public String examineExtension(X509Extension aext, DERObjectIdentifier _aOID, XOX_X509CertificateDisplay _xCert) {
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
				return examineAlternativeName(aext);	
			else if(_aOID.equals(X509Extensions.IssuerAlternativeName))
				return examineAlternativeName(aext);	
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
			else if(_aOID.equals(X509Extensions.BasicConstraints))
				return examineBasicConstraints(aext);
			else if(_aOID.equals(X509Extensions.PolicyConstraints))
				return examinePolicyConstraints(aext);
			else {
				throw (new java.lang.NoSuchMethodException(term+"While processing OID: " + _aOID.getId() +":"+term+
						Helpers.printHexBytes(aext.getValue().getOctets())));
			}
		} catch (java.lang.Exception e) {
			String ret = "Exception while processing OID: " + _aOID.getId();
			_xCert.setCertificateExtensionCommentString(_aOID.getId(), "This extension is NOT recognized.\nIssuer specific extension?");
			m_aLogger.severe(e);
			return ret;
		}
	}

	/*
still to be implemented: 1.2.840.113533.7.65.0 - entrust version extension

OID description:
certificate extension for entrust version

entrustVersInfo EXTENSION ::= {
	SYNTAX EntrustVersInfoSyntax
	IDENTIFIED BY { id-nsn-ext 0}
}

EntrustVersInfoSyntax ::= OCTET STRING
 
Superior references

    * 1.2.840.113533.7.65 - Secure Networks Certificate Extensions
    * 1.2.840.113533.7 - Entrust Technologies
    * 1.2.840.113533 - Nortel Networks
    * 1.2.840 - USA
    * 1.2 - ISO member body
    * 1 - ISO assigned OIDs 

    public static final DERObjectIdentifier CRLNumber = new DERObjectIdentifier("2.5.29.20");
    public static final DERObjectIdentifier ReasonCode = new DERObjectIdentifier("2.5.29.21");
    public static final DERObjectIdentifier InstructionCode = new DERObjectIdentifier("2.5.29.23");
    public static final DERObjectIdentifier InvalidityDate = new DERObjectIdentifier("2.5.29.24");
    public static final DERObjectIdentifier DeltaCRLIndicator = new DERObjectIdentifier("2.5.29.27");
    public static final DERObjectIdentifier IssuingDistributionPoint = new DERObjectIdentifier("2.5.29.28");
    public static final DERObjectIdentifier CertificateIssuer = new DERObjectIdentifier("2.5.29.29");
    public static final DERObjectIdentifier NameConstraints = new DERObjectIdentifier("2.5.29.30");
    public static final DERObjectIdentifier PolicyMappings = new DERObjectIdentifier("2.5.29.33");
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
	@SuppressWarnings("unchecked")
	private String examinePolicyConstraints(X509Extension aext) {
		String stx = "";
		try {
			ASN1Sequence pc = (ASN1Sequence) 
					X509Extension.convertValueToObject(aext);
			if (pc != null) {
				Enumeration policyConstraints = pc.getObjects();
				while (policyConstraints.hasMoreElements()) {
					ASN1TaggedObject constraint = (ASN1TaggedObject)
							policyConstraints.nextElement();
					int tmpInt;

					switch (constraint.getTagNo()) {
					case 0:
						tmpInt = DERInteger.getInstance(constraint).getValue()
								.intValue();
						stx = stx + " requireExplicitPolicy: " + tmpInt;
						break;
					case 1:
						tmpInt = DERInteger.getInstance(constraint).getValue()
								.intValue();
						stx = stx + " inhibitPolicyMapping: " + tmpInt;
						break;
					}
				}
			}
		} catch (Throwable ae) {
			m_aLogger.severe(ae);
		}
		return stx;
	}

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
		Vector<?> attribv = sda.getAttributes();
		//FIXME: checked for Italy only.
		for(int i = 0; i < attribv.size();i++) {
			Attribute atrb = (Attribute) attribv.get(i);
			ASN1Set asns = atrb.getAttrValues();
			for(int y=0; y<asns.size();y++) {
				if(atrb.getAttrType().equals(X509Name.DATE_OF_BIRTH)) {
					DERGeneralizedTime dgt = DERGeneralizedTime.getInstance(asns.getObjectAt(y));
					// as per CNIPA print the date of birth in localized mode
					TimeZone gmt = TimeZone.getTimeZone("UTC");
					GregorianCalendar calendar = new GregorianCalendar(gmt,m_lTheLocale);
					try {
						calendar.setTime(dgt.getDate());
						//string with time only
						//the locale should be the one of the extension not the Java one.
						String time = String.format(m_lTheLocale,m_sLocaleDateOfBirth, calendar);
						stx = stx +time;
/*						stx = term+stx+"Original value"+
							((m_bDisplayOID) ? " (OID: "+atrb.getAttrType().getId()+")":"")+
							term+" "+dgt.getTime();*/
					} catch (ParseException e) {
						m_aLogger.severe(e);
						stx = stx + term +e.toString();
					}	
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
		PrivateKeyUsagePeriod pku = PrivateKeyUsagePeriod.getInstance(aext);
		DERGeneralizedTime from = pku.getNotBefore();
		DERGeneralizedTime to = pku.getNotAfter();
		String stx = "";

		try {
			stx = stx+" Not Before: "+getDateStringHelper(from.getDate())+term;
			stx = stx+" Not After: "+getDateStringHelper(to.getDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stx;
	}

	private String getDateStringHelper(Date _aTime) {
		//force UTC time
		TimeZone gmt = TimeZone.getTimeZone("UTC");
		GregorianCalendar calendar = new GregorianCalendar(gmt,m_lTheLocale);
		calendar.setTime(_aTime);	
//string with time only
//the locale should be the one of the extension not the Java one.
		String time = String.format(m_lTheLocale,m_sTimeLocaleString, calendar);
		return time;
	}	

	
	private String decodeAGeneralName(GeneralName genName) throws IOException {
		String stx ="";
        switch (genName.getTagNo())
        {
        case GeneralName.ediPartyName:
            stx = stx + "ediPartyName: "+ genName.getName().getDERObject();
            break;
        case GeneralName.x400Address:
            stx = stx + "x400Address: "+ genName.getName().getDERObject();
            break;
        case GeneralName.otherName:
            stx = stx + "otherName: "+ genName.getName().getDERObject();
            break;
        case GeneralName.directoryName:
            stx = stx +"directoryName: "+X509Name.getInstance(genName.getName()).toString();
            break;
        case GeneralName.dNSName:
        	stx = stx+"dNSName: "+((DERString)genName.getName()).getString();
            break;
        case GeneralName.rfc822Name:
        	stx = stx+"e-mail: "+((DERString)genName.getName()).getString();
            break;
        case GeneralName.uniformResourceIdentifier:
        	stx = stx+"URI: "+((DERString)genName.getName()).getString();
            break;
        case GeneralName.registeredID:
            stx = stx + "registeredID: "+DERObjectIdentifier.getInstance(genName.getName()).getId();
            break;
        case GeneralName.iPAddress:
            stx = stx + "iPAddress: "+DEROctetString.getInstance(genName.getName()).getOctets();
            break;
        default:
        	throw new IOException("Bad tag number: " + genName.getTagNo());
        }
		return stx;
	}

	/**
	 * @param aext
	 * @return
	 * @throws IOException 
	 */
	private String examineAlternativeName(X509Extension aext) throws IOException {
		String stx="";
        byte[] extnValue = aext.getValue().getOctets();
        Enumeration<?> it = DERSequence.getInstance( ASN1Object.fromByteArray(extnValue)).getObjects();
        while (it.hasMoreElements()) {
            GeneralName genName = GeneralName.getInstance(it.nextElement());
            stx = stx + decodeAGeneralName(genName); 
        }		
		return stx;
	}

	/**
	 * @param aext
	 * @return
	 */
	private String examineBasicConstraints(X509Extension aext) {
		BasicConstraints bc = BasicConstraints.getInstance(aext);
		String stx = " cA = ";
		if(bc.isCA())
			stx = stx + "true";
		else
			stx = stx + "false";
		stx = stx + term+ " pathLenConstraints: ";
		BigInteger pathLen = bc.getPathLenConstraint();
		if(pathLen != null) {
			stx = stx + pathLen;
		}
		else
			stx = stx +"'no limit'";

		return stx;
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
		AccessDescription[] aAccess = aia.getAccessDescriptions();
		String stx = "";
		for(int i=0;i< aAccess.length;i++) {
			if(aAccess[i].getAccessMethod().equals(AccessDescription.id_ad_caIssuers))
				stx = stx+"caIssuers";
			else if(aAccess[i].getAccessMethod().equals(AccessDescription.id_ad_ocsp))
					stx = stx+"ocsp";
			stx = stx + ": "+term+"  ";
			GeneralName aName = aAccess[i].getAccessLocation();
			try {
				stx = stx + decodeAGeneralName(aName)+" "+term;
			} catch (IOException e) {
				m_aLogger.severe(e);
			}
		}
		return stx;
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
	 * @throws IOException 
	 */
	private String examineCRLDistributionPoints(X509Extension aext) throws IOException	{
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
				stx = stx + decodeAGeneralName(gn[y]) + term;
			}
			stx = stx + term;

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

		Vector<?> usages = eku.getUsages();
		for(int i = 0; i < usages.size();i++) {
			stx = stx+" "+m_aKeyPurposeIdReverseLookUp.get(usages.get(i))+
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
