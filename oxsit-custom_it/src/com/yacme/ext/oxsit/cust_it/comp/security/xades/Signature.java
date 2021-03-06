/* ***** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/SignedDoc.java.
 *
 * The Initial Developer of the Original Code is
 * AUTHOR:  Veiko Sinivee, S|E|B IT Partner Estonia
 * Copyright (C) AS Sertifitseerimiskeskus
 * from which ideas and part of the code are derived
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * FIXME add Rob e-mail
 * Roberto Resoli 
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.TimestampInfo;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CRLFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.DigiDocFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.NotaryFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.TimestampFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;

/**
 * Models an XML-DSIG/ETSI Signature. A signature
 * can contain references SignedInfo (truly signed data)
 * and signed and unsigned properties.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class Signature implements Serializable {
    /** reference to the parent SignedDoc object */
    private SignedDoc m_sigDoc;
    /** signature id */
    private String m_id;
    /** SignedInfo object */
    private SignedInfo m_signedInfo;
    /** SignatureValue object */
    private SignatureValue m_signatureValue;
    /** KeyInfo object */
    private KeyInfo m_keyInfo;
    /** SignedProperties object */
    private SignedProperties m_sigProp;
    /** UnsignedProperties object */
    private UnsignedProperties m_unsigProp;
    /** original bytes read from XML file  */
    private byte[] m_origContent;
	/** CertID elements */
	private ArrayList<CertID> m_certIds;    
    /** CertValue elements */
	private ArrayList<CertValue> m_certValues;
    /** TimestampInfo elements */
	private ArrayList<TimestampInfo> m_timestamps;
    
    
    /** 
     * Creates new Signature 
     */
    public Signature(SignedDoc sigDoc) {
        m_sigDoc = sigDoc;
        m_id = null;
        m_signedInfo = null;
        m_signatureValue = null;
        m_keyInfo = null;
        m_sigProp = null;
        m_unsigProp = null;
        m_origContent = null;
        m_certIds = null;
        m_certValues = null;
        m_timestamps = null;
    }
    
    /**
     * Accessor for sigDoc attribute
     * @return value of sigDoc attribute
     */
    public SignedDoc getSignedDoc() {
        return m_sigDoc;
    }
    
    /**
     * Mutator for sigDoc attribute
     * @param sigDoc new value for sigDoc attribute
     */    
    public void setSignedDoc(SignedDoc sigDoc) 
    {
        m_sigDoc = sigDoc;
    }
    
    /**
     * Accessor for id attribute
     * @return value of id attribute
     */
    public String getId() {
        return m_id;
    }
    
    /**
     * Mutator for id attribute
     * @param str new value for id attribute
     * @throws SignedDocException for validation errors
     */    
    public void setId(String str) 
        throws SignedDocException
    {
        SignedDocException ex = validateId(str);
        if(ex != null)
            throw ex;
        m_id = str;
    }
    
    /**
     * Accessor for origContent attribute
     * @return value of origContent attribute
     */
    public byte[] getOrigContent() {
        return m_origContent;
    }
    
    /**
     * Mutator for origContent attribute
     * @param str new value for origContent attribute
     */    
    public void setOrigContent(byte[] data) 
    {
        m_origContent = data;
    }
    
    /**
     * Helper method to validate an id
     * @param str input data
     * @return exception or null for ok
     */
    private SignedDocException validateId(String str)
    {
        SignedDocException ex = null;
        if(str == null)
            ex = new SignedDocException(SignedDocException.ERR_SIGNATURE_ID, 
                "Id is a required attribute", null);
        return ex;
    }
    
    /**
     * Accessor for signedInfo attribute
     * @return value of signedInfo attribute
     */
    public SignedInfo getSignedInfo() {
        return m_signedInfo;
    }
    
    /**
     * Mutator for signedInfo attribute
     * @param str new value for signedInfo attribute
     * @throws SignedDocException for validation errors
     */    
    public void setSignedInfo(SignedInfo si) 
        throws SignedDocException
    {
        //ArrayList errs = si.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_signedInfo = si;
    }

    /**
     * Calculates the SignedInfo digest
     * @return SignedInfo digest
     */
    public byte[] calculateSignedInfoDigest()
        throws SignedDocException
    {
        return m_signedInfo.calculateDigest();
    }
    
    /**
     * Accessor for signatureValue attribute
     * @return value of signatureValue attribute
     */
    public SignatureValue getSignatureValue() {
        return m_signatureValue;
    }
    
    /**
     * Mutator for signatureValue attribute
     * @param str new value for signatureValue attribute
     * @throws SignedDocException for validation errors
     */    
    public void setSignatureValue(SignatureValue sv) 
        throws SignedDocException
    {
        //ArrayList errs = sv.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_signatureValue = sv;
        // VS: bug fix on 14.05.2008
        m_origContent = null;
    }
    
    /**
     * Creates a new SignatureValue object
     * of this signature
     * @param sigv signatures byte data
     * @throws SignedDocException for validation errors
     */    
    public void setSignatureValue(byte[] sigv) 
        throws SignedDocException
    {
        SignatureValue sv = new SignatureValue(this, sigv);
        setSignatureValue(sv);
    }
    
    /**
     * Accessor for keyInfo attribute
     * @return value of keyInfo attribute
     */
    public KeyInfo getKeyInfo() {
        return m_keyInfo;
    }
    
    /**
     * Mutator for keyInfo attribute
     * @param str new value for keyInfo attribute
     * @throws SignedDocException for validation errors
     */    
    public void setKeyInfo(KeyInfo ki) 
        throws SignedDocException
    {
        //ArrayList errs = ki.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_keyInfo = ki;
    }
    
    /**
     * Accessor for signedProperties attribute
     * @return value of SignedProperties attribute
     */
    public SignedProperties getSignedProperties() {
        return m_sigProp;
    }
    
    /**
     * Mutator for signedProperties attribute
     * @param str new value for signedProperties attribute
     * @throws SignedDocException for validation errors
     */    
    public void setSignedProperties(SignedProperties sp) 
        throws SignedDocException
    {
        //ArrayList errs = sp.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_sigProp = sp;
    }
    
    /**
     * Accessor for unsignedProperties attribute
     * @return value of unsignedProperties attribute
     */
    public UnsignedProperties getUnsignedProperties() {
        return m_unsigProp;
    }
    
    /**
     * Mutator for unsignedProperties attribute
     * @param str new value for unsignedProperties attribute
     * @throws SignedDocException for validation errors
     */    
    public void setUnsignedProperties(UnsignedProperties usp) 
        throws SignedDocException
    {
        //ArrayList errs = usp.validate();
        //if(!errs.isEmpty())
        //    throw (SignedDocException)errs.get(0);
        m_unsigProp = usp;
    }
    /**
     * return the count of CertID objects
     * @return count of CertID objects
     */
    public int countCertIDs()
    {
        return ((m_certIds == null) ? 0 : m_certIds.size());
    }
    
    /**
     * Adds a new CertID object
     * @param cid new object to be added
     */
    public void addCertID(CertID cid)
    {
    	if(m_certIds == null)
    		m_certIds = new ArrayList<CertID>();
    	cid.setSignature(this);
    	m_certIds.add(cid);
    }
    
    /**
     * Retrieves CertID element with the desired index
     * @param idx CertID index
     * @return CertID element or null if not found
     */
    public CertID getCertID(int idx)
    {
    	if(m_certIds != null && idx < m_certIds.size()) {
    		return (CertID)m_certIds.get(idx);
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves the last CertID element
     * @return CertID element or null if not found
     */
    public CertID getLastCertId()
    {
    	if(m_certIds != null && m_certIds.size() > 0) {
    		return (CertID)m_certIds.get(m_certIds.size()-1);
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves CertID element with the desired type
     * @param type CertID type
     * @return CertID element or null if not found
     */
    public CertID getCertIdOfType(int type)
    {
    	for(int i = 0; (m_certIds != null) && (i < m_certIds.size()); i++) {
    		CertID cid = (CertID)m_certIds.get(i);
    		if(cid.getType() == type)
    			return cid;
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves CertID element with the desired type.
     * If not found creates a new one with this type.
     * @param type CertID type
     * @return CertID element
     * @throws SignedDocException for validation errors
     */
    public CertID getOrCreateCertIdOfType(int type)
    	throws SignedDocException
    {
    	CertID cid = getCertIdOfType(type);
    	if(cid == null) {
    		cid = new CertID();
    		cid.setType(type);
    		addCertID(cid);
    	}
    	return cid; // not found
    }
    
    /**
     * return the count of CertValue objects
     * @return count of CertValue_ITs objects
     */
    public int countCertValues()
    {
        return ((m_certValues == null) ? 0 : m_certValues.size());
    }
    
    /**
     * Adds a new CertValue object
     * @param cval new object to be added
     */
    public void addCertValue(CertValue cval)
    {
    	if(m_certValues == null)
    		m_certValues = new ArrayList<CertValue>();
    	cval.setSignature(this);
    	m_certValues.add(cval);
    }
    
    /**
     * Retrieves CertValue element with the desired index
     * @param idx CertValue index
     * @return CertValue element or null if not found
     */
    public CertValue getCertValue(int idx)
    {
    	if(m_certValues != null && idx < m_certValues.size()) {
    		return (CertValue)m_certValues.get(idx);
    	} else
    	return null; // not found
    }
    
    /**
     * Retrieves the last CertValue element 
     * @return CertValue element or null if not found
     */
    public CertValue getLastCertValue()
    {
    	if(m_certValues != null && m_certValues.size() > 0) {
    		return (CertValue)m_certValues.get(m_certValues.size()-1);
    	} else
    		return null; // not found
    }
    
    /**
     * Retrieves CertValue element with the desired type
     * @param type CertValue type
     * @return CertValue element or null if not found
     */
    public CertValue getCertValueOfType(int type)
    {
    	for(int i = 0; (m_certValues != null) && (i < m_certValues.size()); i++) {
    		CertValue cval = (CertValue)m_certValues.get(i);
    		if(cval.getType() == type)
    			return cval;
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves CertValue element with the desired type.
     * If not found creates a new one with this type.
     * @param type CertValue type
     * @return CertValue element
     * @throws SignedDocException for validation errors
     */
    public CertValue getOrCreateCertValueOfType(int type)
    	throws SignedDocException
    {
    	CertValue cval = getCertValueOfType(type);
    	if(cval == null) {
    		cval = new CertValue();
    		cval.setType(type);
    		addCertValue(cval);
    	}
    	return cval; // not found
    }
    
    /**
     * Returns the first CertValue with the given serial
     * number that has been attached to this signature in
     * digidoc document. This could be either the signers 
     * cert, OCSP responders cert or one of the TSA certs.
     * @param serNo certificates serial number
     * @return found CertValue or null
     */
    public CertValue findCertValueWithSerial(BigInteger serNo)
    {
    	for(int i = 0; (m_certValues != null) && (i < m_certValues.size()); i++) {
    		CertValue cval = (CertValue)m_certValues.get(i);
    		//System.out.println("Serach cert: " + serNo + " found: " + cval.getCert().getSerialNumber());
    		if(cval.getCert().getSerialNumber().equals(serNo))
    			return cval;
    	}
    	return null;
    }
	
    /**
     * Retrieves OCSP respoinders certificate
     * @return OCSP respoinders certificate
     */
    public X509Certificate findResponderCert()
    {
    	CertValue cval = getCertValueOfType(CertValue.CERTVAL_TYPE_RESPONDER);
    	if(cval != null)
    		return cval.getCert();
    	else
    		return null;
    }
    
    /**
     * Retrieves TSA certificates
     * @return TSA certificates
     */
    public ArrayList<X509Certificate> findTSACerts()
    {
    	ArrayList<X509Certificate> vec = new ArrayList<X509Certificate>();
    	for(int i = 0; (m_certValues != null) && (i < m_certValues.size()); i++) {
    		CertValue cval = (CertValue)m_certValues.get(i);
    		if(cval.getType() == CertValue.CERTVAL_TYPE_TSA)
    			vec.add(cval.getCert());
    	}
    	return vec;
    }
    
    /**
     * return the count of TimestampInfo objects
     * @return count of TimestampInfo objects
     */
    public int countTimestampInfos()
    {
        return ((m_timestamps == null) ? 0 : m_timestamps.size());
    }
    
    /**
     * Adds a new TimestampInfo object
     * @param ts new object to be added
     */
    public void addTimestampInfo(TimestampInfo ts)
    {
    	if(m_timestamps == null)
    		m_timestamps = new ArrayList<TimestampInfo>();
    	ts.setSignature(this);
    	m_timestamps.add(ts);
    }
    
    /**
     * Retrieves TimestampInfo element with the desired index
     * @param idx TimestampInfo index
     * @return TimestampInfo element or null if not found
     */
    public TimestampInfo getTimestampInfo(int idx)
    {
    	if(m_timestamps != null && idx < m_timestamps.size()) {
    		return (TimestampInfo)m_timestamps.get(idx);
    	} else
    		return null; // not found
    }
    
    /**
     * Retrieves the last TimestampInfo element 
     * @return TimestampInfo element or null if not found
     */
    public TimestampInfo getLastTimestampInfo()
    {
    	if(m_timestamps != null && m_timestamps.size() > 0) {
    		return (TimestampInfo)m_timestamps.get(m_timestamps.size()-1);
    	} else
    		return null; // not found
    }
    
    /**
     * Retrieves TimestampInfo element with the desired type
     * @param type TimestampInfo type
     * @return TimestampInfo element or null if not found
     */
    public TimestampInfo getTimestampInfoOfType(int type)
    {
    	for(int i = 0; (m_timestamps != null) && (i < m_timestamps.size()); i++) {
    		TimestampInfo ts = (TimestampInfo)m_timestamps.get(i);
    		if(ts.getType() == type)
    			return ts;
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves TimestampInfo element with the desired type.
     * If not found creates a new one with this type.
     * @param type TimestampInfo type
     * @return TimestampInfo element
     * @throws SignedDocException for validation errors
     */
    public TimestampInfo getOrCreateTimestampInfoOfType(int type)
    	throws SignedDocException
    {
    	TimestampInfo ts = getTimestampInfoOfType(type);
    	if(ts == null) {
    		ts = new TimestampInfo();
    		ts.setType(type);
    		addTimestampInfo(ts);
    	}
    	return ts; // not found
    }
    
    /**
     * Gets confirmation and adds the corresponding
     * members that carry the returned info to
     * this signature
     * @throws SignedDocException for all errors
     */
    public void getConfirmation()
        throws SignedDocException
    {
        NotaryFactory notFac = ConfigManager.
	    instance().getNotaryFactory();
        
        
        
        X509Certificate cert = m_keyInfo.getSignersCertificate();
        //ROB
        //DigiDocFactory ddocFac = ConfigManager.instance().getDigiDocFactory();
        DigiDocFactory ddocFac = ConfigManager.instance().getSignedDocFactory();
        X509Certificate caCert = ddocFac.findCAforCertificate(cert);
        //ROB
        notFac.checkCertificateOcspOrCrl(cert, false);
        
        //ROB confirmation not required in Italy
        /*
        Notary not = notFac.getConfirmation(this, cert, caCert);
        CompleteRevocationRefs rrefs = 
            new CompleteRevocationRefs(not);    
        // modified in ver 2.1.0 - find responder certs that succeded in verification
        X509Certificate rcert = notFac.getNotaryCert(rrefs.getResponderCommonName(), not.getCertNr());
        // if the request was successful then
        // create new data memebers
        CertValue cval = new CertValue();
        cval.setType(CertValue.CERTVAL_TYPE_RESPONDER);
        cval.setCert(rcert);
        addCertValue(cval);
        cval.setId(m_id + "-RESPONDER_CERT");
        CertID cid = new CertID(this, rcert, CertID.CERTID_TYPE_RESPONDER);
        addCertID(cid);        
        CompleteCertificateRefs crefs = 
            new CompleteCertificateRefs();
        UnsignedProperties usp = new UnsignedProperties(this, 
            crefs, rrefs, rcert, not);
        rrefs.setUnsignedProperties(usp);
        crefs.setUnsignedProperties(usp);
        setUnsignedProperties(usp);
        // reset original content since we just added to confirmation
        if(m_origContent != null) {
	    String str = new String(m_origContent);
	    int idx1 = str.indexOf("</SignedProperties>");
	    if(idx1 != -1) {
		try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    bos.write(m_origContent, 0, idx1);
		    bos.write("</SignedProperties>".getBytes());
		    bos.write(usp.toXML());
		    bos.write("</QualifyingProperties></Object></Signature>".getBytes());
		    m_origContent = bos.toByteArray();
		} catch(java.io.IOException ex) {
		    SignedDocException.handleException(ex, SignedDocException.ERR_OCSP_GET_CONF);
		}
	    }   
        }
        */
    }
    
    /** 
     * Verifies this signature
     * @param sdoc parent doc object
     * @param checkDate Date on which to check the signature validity
     * @param demandConfirmation true if you demand OCSP confirmation from
     *every signature
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList<SignedDocException> verify(SignedDoc sdoc, boolean checkDate, boolean demandConfirmation)
    {
    	Date do1 = null, dt1 = null, dt2 = null;
        ArrayList<SignedDocException> errs = new ArrayList<SignedDocException>();
        // check the DataFile digests
        for(int i = 0; i < sdoc.countDataFiles(); i++) {
            DataFile df = sdoc.getDataFile(i);
            //System.out.println("Check digest for DF: " + df.getId());
            Reference ref = m_signedInfo.getReferenceForDataFile(df);
            byte[] dfDig = null;
            try {
                dfDig = df.getDigest();
            } catch(SignedDocException ex) {
                errs.add(ex);
            }
            if(ref != null) {
            	//System.out.println("Compare it to: " + Base64Util.encode(ref.getDigestValue(), 0));
                if(!SignedDoc.compareDigests(ref.getDigestValue(), dfDig)) {
                   errs.add(new SignedDocException(
                    SignedDocException.ERR_DIGEST_COMPARE,
                    "Bad digest for DataFile: " + df.getId(), null));
                    //System.out.println("BAD DIGEST");
                }
                //else System.out.println("GOOD DIGEST");
            } else {
            	//System.out.println("No Reference");
                errs.add(new SignedDocException(
                    SignedDocException.ERR_DATA_FILE_NOT_SIGNED,
                    "No Reference element for DataFile: " + df.getId(), null));
            }
            // if this is a detatched file and the file
            // referred by this entry actually exists,
            // then go and check it's digest
            // If the datafile doesn't exist the
            // just trust whatever is in the XML
            if(df.getContentType().equals(DataFile.CONTENT_DETATCHED) /*|| df.getContentType().equals(DataFile.CONTENT_ODF_PKG_XML_ENTRY) ||
            		df.getContentType().equals(DataFile.CONTENT_ODF_PKG_BINARY_ENTRY)*/) {
                File fTest = new File(df.getFileName());
                if(fTest.canRead()) {
                    //System.out.println("Check detatched file: " + fTest.getAbsolutePath());
                    byte[] realDigest = null;
                    byte[] detDigest = null;
                    try {
                        realDigest = df.calculateDetatchedFileDigest();
                        detDigest = df.getDigestValue();
                    } catch(SignedDocException ex) {
                        errs.add(ex);
                    }
                    if(!SignedDoc.compareDigests(detDigest, realDigest)) {
                        errs.add(new SignedDocException(
                            SignedDocException.ERR_DIGEST_COMPARE,
                            "Bad digest for detatched file: " + df.getFileName(), null));
                    }
                }
                //else System.out.println("Cannot read detatched file: " + fTest.getAbsolutePath());
            }
        }
        // check signed properties digest
        Reference ref2 = m_signedInfo.getReferenceForSignedProperties(m_sigProp);
        if(ref2 != null) {
            byte[] spDig = null;
            try {
                spDig = m_sigProp.calculateDigest();
                //System.out.println("SignedProp real digest: " + Base64Util.encode(spDig, 0));
            } catch(SignedDocException ex) {
                errs.add(ex);
            }
            //System.out.println("Compare it to: " + Base64Util.encode(ref2.getDigestValue(), 0));
            if(!SignedDoc.compareDigests(ref2.getDigestValue(), spDig)) {
                   errs.add(new SignedDocException(
                    SignedDocException.ERR_DIGEST_COMPARE,
                    "Bad digest for SignedProperties: " + m_sigProp.getId(), null));
                    //System.out.println("BAD DIGEST");
            }
            //else System.out.println("GOOD DIGEST");
        } else {
            errs.add(new SignedDocException(
                    SignedDocException.ERR_SIG_PROP_NOT_SIGNED,
                    "No Reference element for SignedProperties: " + m_sigProp.getId(), null));
        }
        // verify signature value
        try {
            byte[] dig = m_signedInfo.calculateDigest();
            //System.out.println("SignedInfo real digest: " + Base64Util.encode(dig, 0) + " hex: " + SignedDoc.bin2hex(dig));
            SignedDoc.verify(dig, m_signatureValue.getValue(), m_keyInfo.getSignersCertificate());
            //System.out.println("GOOD DIGEST");
        } catch(SignedDocException ex) {
                errs.add(ex);
                System.out.println("BAD DIGEST");
        }
        // verify signers cert...
        // check the certs validity dates
        try {
            if(checkDate)
            	m_keyInfo.getSignersCertificate().
                	checkValidity(m_sigProp.getSigningTime()); 
        } catch(Exception ex) {
            errs.add(new SignedDocException(
                    SignedDocException.ERR_CERT_EXPIRED,
                    "Signers certificate has expired!", null));
        }

        // check certificates CA
//        try {
//        	//ROB
//        	//DigiDocFactory digFac = ConfigManager.instance().getDigiDocFactory();
//        	DigiDocFactory digFac = ConfigManager.instance().getSignedDocFactory();
//        	digFac.verifyCertificate(m_keyInfo.getSignersCertificate());
//        } catch(SignedDocException ex) {
//            errs.add(ex);
//        }
        // if we check signatures using CRL
//        String verifier = ConfigManager.instance().
//                getStringProperty("DIGIDOC_SIGNATURE_VERIFIER", "OCSP");
//        if(verifier != null && verifier.equals("CRL")) {
//        	try {
//        		CRLFactory crlFac = ConfigManager.instance().getCRLFactory();
//            	crlFac.checkCertificate(m_keyInfo.getSignersCertificate(), new Date());
//            } catch(SignedDocException ex) {
//         	   errs.add(ex);
//        	}
//        }
        // check confirmation
//        if(m_unsigProp != null) {
//            ArrayList<SignedDocException> e = m_unsigProp.verify(sdoc);
//            if(!e.isEmpty())
//                errs.addAll(e);
//            if(m_unsigProp.getNotary() != null)
//            	do1 = m_unsigProp.getNotary().getProducedAt();
//        } else { // not OCSP confirmation
//            if(demandConfirmation)
//                errs.add(new SignedDocException(
//                    SignedDocException.ERR_NO_CONFIRMATION,
//                        "Signature has no OCSP confirmation!", null));
//        }
        // verify timestamps
        ArrayList<X509Certificate> tsaCerts = findTSACerts();
        if(m_timestamps != null && m_timestamps.size() > 0) {
        	TimestampFactory tsFac = null;
        	try {
        		tsFac = ConfigManager.instance().getTimestampFactory();
        	} catch(SignedDocException ex) {
        		//m_logger.error("Failed to get TimestampFactory: " + ex);
        		errs.add(ex);
        	}
        	ArrayList<SignedDocException> e = tsFac.verifySignaturesTimestamps(this);
        	if(!e.isEmpty())
                errs.addAll(e);
        	for(int i = 0; i < m_timestamps.size(); i++) {
        		TimestampInfo ts = (TimestampInfo)m_timestamps.get(i);
        		if(ts.getType() == TimestampInfo.TIMESTAMP_TYPE_SIGNATURE)
        			dt1 = ts.getTime();
        		if(ts.getType() == TimestampInfo.TIMESTAMP_TYPE_SIG_AND_REFS)
        			dt2 = ts.getTime();
        	}
        	//System.out.println("OCSP time: " + do1);
        	//System.out.println("SignatureTimeStamp time: " + dt1);
        	//System.out.println("SigAndRefsTimeStamp time: " + dt2);
        	int nMaxTsTimeErrSecs = ConfigManager.instance().getIntProperty("MAX_TSA_TIME_ERR_SECS", 0);
        	dt1 = new Date(dt1.getTime() - (nMaxTsTimeErrSecs * 1000));
        	dt2 = new Date(dt2.getTime() + (nMaxTsTimeErrSecs * 1000));
        	//System.out.println("SignatureTimeStamp adj time: " + dt1);
        	//System.out.println("SigAndRefsTimeStamp adj time: " + dt2);
        	if(dt2.before(dt1))
        		errs.add(new SignedDocException(SignedDocException.ERR_TIMESTAMP_VERIFY, "SignAndRefsTimeStamp is before SignatureTimeStamp", null));
        	if(do1.before(dt1) || do1.after(dt2))
        		errs.add(new SignedDocException(SignedDocException.ERR_TIMESTAMP_VERIFY, "OCSP time is not between SignAndRefsTimeStamp and SignatureTimeStamp", null));
        }
        return errs;
    }

    /** 
     * Verifies this signature. Demands either OCSP confirmation
     * or uses CRL to check signature validity. 
     * @param sdoc parent doc object
     * @param checkDate Date on which to check the signature validity
     * @param bUseOcsp true if you demand OCSP confirmation from
     * every signature. False if you want to check against CRL.
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList<SignedDocException> verifyOcspOrCrl(SignedDoc sdoc, boolean checkDate, boolean bUseOcsp)
    {
    	Date do1 = null, dt1 = null, dt2 = null;
        ArrayList<SignedDocException> errs = new ArrayList<SignedDocException>();
        // check the DataFile digests
        for(int i = 0; i < sdoc.countDataFiles(); i++) {
            DataFile df = sdoc.getDataFile(i);
            //System.out.println("Check digest for DF: " + df.getId());
            Reference ref = m_signedInfo.getReferenceForDataFile(df);
            byte[] dfDig = null;
            try {
                dfDig = df.getDigest();
            } catch(SignedDocException ex) {
                errs.add(ex);
            }
            if(ref != null) {
            	//System.out.println("Compare it to: " + Base64Util.encode(ref.getDigestValue(), 0));
                if(!SignedDoc.compareDigests(ref.getDigestValue(), dfDig)) {
                   errs.add(new SignedDocException(
                    SignedDocException.ERR_DIGEST_COMPARE,
                    "Bad digest for DataFile: " + df.getId(), null));
                    //System.out.println("BAD DIGEST");
                }
                //else System.out.println("GOOD DIGEST");
            } else {
            	//System.out.println("No Reference");
                errs.add(new SignedDocException(
                    SignedDocException.ERR_DATA_FILE_NOT_SIGNED,
                    "No Reference element for DataFile: " + df.getId(), null));
            }
            // if this is a detatched file and the file
            // referred by this entry actually exists,
            // then go and check it's digest
            // If the datafile doesn't exist the
            // just trust whatever is in the XML
            if(df.getContentType().equals(DataFile.CONTENT_DETATCHED)) {
                File fTest = new File(df.getFileName());
                if(fTest.canRead()) {
                    //System.out.println("Check detatched file: " + fTest.getAbsolutePath());
                    byte[] realDigest = null;
                    byte[] detDigest = null;
                    try {
                        realDigest = df.calculateDetatchedFileDigest();
                        detDigest = df.getDigestValue();
                    } catch(SignedDocException ex) {
                        errs.add(ex);
                    }
                    if(!SignedDoc.compareDigests(detDigest, realDigest)) {
                        errs.add(new SignedDocException(
                            SignedDocException.ERR_DIGEST_COMPARE,
                            "Bad digest for detatched file: " + df.getFileName(), null));
                    }
                }
                //else System.out.println("Cannot read detatched file: " + fTest.getAbsolutePath());
            }
        }
        // check signed properties digest
        Reference ref2 = m_signedInfo.getReferenceForSignedProperties(m_sigProp);
        if(ref2 != null) {
            byte[] spDig = null;
            try {
                spDig = m_sigProp.calculateDigest();
                //System.out.println("SignedProp real digest: " + Base64Util.encode(spDig, 0));
            } catch(SignedDocException ex) {
                errs.add(ex);
            }
            //System.out.println("Compare it to: " + Base64Util.encode(ref2.getDigestValue(), 0));
            if(!SignedDoc.compareDigests(ref2.getDigestValue(), spDig)) {
                   errs.add(new SignedDocException(
                    SignedDocException.ERR_DIGEST_COMPARE,
                    "Bad digest for SignedProperties: " + m_sigProp.getId(), null));
                    //System.out.println("BAD DIGEST");
            }
            //else System.out.println("GOOD DIGEST");
        } else {
            errs.add(new SignedDocException(
                    SignedDocException.ERR_SIG_PROP_NOT_SIGNED,
                    "No Reference element for SignedProperties: " + m_sigProp.getId(), null));
        }
        // verify signature value
        try {
            byte[] dig = m_signedInfo.calculateDigest();
            //System.out.println("SignedInfo real digest: " + Base64Util.encode(dig, 0) + " hex: " + SignedDoc.bin2hex(dig));
            SignedDoc.verify(dig, m_signatureValue.getValue(), m_keyInfo.getSignersCertificate());
            //System.out.println("GOOD DIGEST");
        } catch(SignedDocException ex) {
                errs.add(ex);
                System.out.println("BAD DIGEST");
        }
        // verify signers cert...
        // check the certs validity dates
        try {
            if(checkDate)
            	m_keyInfo.getSignersCertificate().
                	checkValidity(m_sigProp.getSigningTime()); 
        } catch(Exception ex) {
            errs.add(new SignedDocException(
                    SignedDocException.ERR_CERT_EXPIRED,
                    "Signers certificate has expired!", null));
        }
        // check certificates CA
        try {
        	//ROB
        	DigiDocFactory digFac = ConfigManager.instance().getSignedDocFactory();
        	digFac.verifyCertificate(m_keyInfo.getSignersCertificate());
        } catch(SignedDocException ex) {
            errs.add(ex);
        }
        // switch OCSP or CRL verification
        if(bUseOcsp) { // use OCSP
        	// check confirmation
        	if(m_unsigProp != null) {
                ArrayList<SignedDocException> e = m_unsigProp.verify(sdoc);
                if(!e.isEmpty())
                    errs.addAll(e);
            } else { // not OCSP confirmation
                errs.add(new SignedDocException(
                    SignedDocException.ERR_NO_CONFIRMATION,
                            "Signature has no OCSP confirmation!", null));
            }        	
        	// verify timestamps
            ArrayList<X509Certificate> tsaCerts = findTSACerts();
            if(m_timestamps.size() > 0) {
            	TimestampFactory tsFac = null;
            	try {
            		tsFac = ConfigManager.instance().getTimestampFactory();
            	} catch(SignedDocException ex) {
            		//m_logger.error("Failed to get TimestampFactory: " + ex);
            		errs.add(ex);
            	}
            	ArrayList<SignedDocException> e = tsFac.verifySignaturesTimestamps(this);
            	if(!e.isEmpty())
                    errs.addAll(e);
            	for(int i = 0; i < m_timestamps.size(); i++) {
            		TimestampInfo ts = (TimestampInfo)m_timestamps.get(i);
            		if(ts.getType() == TimestampInfo.TIMESTAMP_TYPE_SIGNATURE)
            			dt1 = ts.getTime();
            		if(ts.getType() == TimestampInfo.TIMESTAMP_TYPE_SIG_AND_REFS)
            			dt2 = ts.getTime();
            	}
            	//System.out.println("OCSP time: " + do1);
            	//System.out.println("SignatureTimeStamp time: " + dt1);
            	//System.out.println("SigAndRefsTimeStamp time: " + dt2);
            	int nMaxTsTimeErrSecs = ConfigManager.instance().getIntProperty("MAX_TSA_TIME_ERR_SECS", 0);
            	dt1 = new Date(dt1.getTime() - (nMaxTsTimeErrSecs * 1000));
            	dt2 = new Date(dt2.getTime() + (nMaxTsTimeErrSecs * 1000));
            	//System.out.println("SignatureTimeStamp adj time: " + dt1);
            	//System.out.println("SigAndRefsTimeStamp adj time: " + dt2);
            	if(dt2.before(dt1))
            		errs.add(new SignedDocException(SignedDocException.ERR_TIMESTAMP_VERIFY, "SignAndRefsTimeStamp is before SignatureTimeStamp", null));
            	if(do1.before(dt1) || do1.after(dt2))
            		errs.add(new SignedDocException(SignedDocException.ERR_TIMESTAMP_VERIFY, "OCSP time is not between SignAndRefsTimeStamp and SignatureTimeStamp", null));
            }
        } else {
        	try {
        		CRLFactory crlFac = ConfigManager.instance().getCRLFactory();
            	crlFac.checkCertificate(m_keyInfo.getSignersCertificate(), new Date());
            } catch(SignedDocException ex) {
         	   errs.add(ex);
        	}
        }
        return errs;
    }
    
    /**
     * Helper method to validate the whole
     * Signature object
     * @return a possibly empty list of SignedDocException objects
     */
    public ArrayList<SignedDocException> validate()
    {
        ArrayList errs = new ArrayList();
        SignedDocException ex = validateId(m_id);
        if(ex != null)
            errs.add(ex);
        ArrayList e = m_signedInfo.validate();
        if(!e.isEmpty())
            errs.addAll(e);
        // VS: 2.2.24 - fix to allowe Signature without SignatureValue - incomplete sig
        //if(m_signatureValue != null)
        e = m_signatureValue.validate();
        if(!e.isEmpty())
            errs.addAll(e);
        e = m_keyInfo.validate();
        if(!e.isEmpty())
            errs.addAll(e);
        e = m_sigProp.validate();
        if(!e.isEmpty())
            errs.addAll(e);
        if(m_unsigProp != null) {
            e = m_unsigProp.validate();
            if(!e.isEmpty())
                errs.addAll(e);
        }
        return errs;
    }
    
    /**
     * Converts the Signature to XML form
     * @return XML representation of Signature
     */
    public byte[] toXML()
        throws SignedDocException
    {
    	if(m_origContent == null) {
        	ByteArrayOutputStream bos = 
            	new ByteArrayOutputStream();
        	try {
            	bos.write(ConvertUtils.str2data("<Signature Id=\""));
            	bos.write(ConvertUtils.str2data(m_id));
            	bos.write(ConvertUtils.str2data("\" xmlns=\"" + SignedDoc.xmlns_xmldsig + "\">\n"));
            	bos.write(m_signedInfo.toXML());
            	bos.write(ConvertUtils.str2data("\n"));
            	// VS: 2.2.24 - fix to allowe Signature without SignatureValue - incomplete sig
				if(m_signatureValue != null)
            		bos.write(m_signatureValue.toXML());
            	bos.write(ConvertUtils.str2data("\n"));
            	bos.write(m_keyInfo.toXML());
            	// In version 1.3 we use xmlns atributes like specified in XAdES 
            	if(m_sigDoc.getVersion().equals(SignedDoc.VERSION_1_3)) {
            		bos.write(ConvertUtils.str2data("\n<Object><QualifyingProperties xmlns=\""));
            		bos.write(ConvertUtils.str2data(SignedDoc.xmlns_etsi));
            		bos.write(ConvertUtils.str2data("\" Target=\"#"));
            		bos.write(ConvertUtils.str2data(m_id));
            		bos.write(ConvertUtils.str2data("\">"));
            	} else // in versions prior to 1.3 we used attributes in wrong places
            		bos.write(ConvertUtils.str2data("\n<Object><QualifyingProperties>"));
            	if(m_sigProp != null)
                	bos.write(m_sigProp.toXML());
            	if(m_unsigProp != null)
                	bos.write(m_unsigProp.toXML());
            	bos.write(ConvertUtils.str2data("</QualifyingProperties></Object>\n"));
            	bos.write(ConvertUtils.str2data("</Signature>"));
        	} catch(IOException ex) {
            	SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        	}
        	return bos.toByteArray();
    	}
    	else
    		return m_origContent;
    }

    /**
     * Returns the string form of Signature
     * @return Signature string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML(), "UTF-8");
        } catch(Exception ex) {}
        return str;
    }    
}
