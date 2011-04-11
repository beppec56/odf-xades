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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/SignedODFDocument_IT.java.
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

import com.yacme.ext.oxsit.cust_it.comp.security.xades.TimestampInfo_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CRLFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.DigiDocFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.NotaryFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.TimestampFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager_IT;

/**
 * Models an XML-DSIG/ETSI Signature. A signature
 * can contain references SignedInfoXADES_IT (truly signed data)
 * and signed and unsigned properties.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class SignatureXADES_IT implements Serializable {
    /** reference to the parent SignedODFDocument_IT object */
    private SignedODFDocument_IT m_sigDoc;
    /** signature id */
    private String m_id;
    /** SignedInfoXADES_IT object */
    private SignedInfoXADES_IT m_signedInfo;
    /** SignatureValue_IT object */
    private SignatureValue_IT m_signatureValue;
    /** KeyInfo_IT object */
    private KeyInfo_IT m_keyInfo;
    /** SignedPropertiesXADES_IT object */
    private SignedPropertiesXADES_IT m_sigProp;
    /** UnsignedProperties object */
    private UnsignedProperties_IT m_unsigProp;
    /** original bytes read from XML file  */
    private byte[] m_origContent;
	/** CertID_IT elements */
	private ArrayList m_certIds;    
    /** CertValue_IT elements */
	private ArrayList m_certValues;
    /** TimestampInfo_IT elements */
	private ArrayList m_timestamps;
    
    
    /** 
     * Creates new Signature 
     */
    public SignatureXADES_IT(SignedODFDocument_IT sigDoc) {
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
    public SignedODFDocument_IT getSignedDoc() {
        return m_sigDoc;
    }
    
    /**
     * Mutator for sigDoc attribute
     * @param sigDoc new value for sigDoc attribute
     */    
    public void setSignedDoc(SignedODFDocument_IT sigDoc) 
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
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setId(String str) 
        throws SignedODFDocumentException_IT
    {
        SignedODFDocumentException_IT ex = validateId(str);
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
    private SignedODFDocumentException_IT validateId(String str)
    {
        SignedODFDocumentException_IT ex = null;
        if(str == null)
            ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_SIGNATURE_ID, 
                "Id is a required attribute", null);
        return ex;
    }
    
    /**
     * Accessor for signedInfo attribute
     * @return value of signedInfo attribute
     */
    public SignedInfoXADES_IT getSignedInfo() {
        return m_signedInfo;
    }
    
    /**
     * Mutator for signedInfo attribute
     * @param str new value for signedInfo attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setSignedInfo(SignedInfoXADES_IT si) 
        throws SignedODFDocumentException_IT
    {
        //ArrayList errs = si.validate();
        //if(!errs.isEmpty())
        //    throw (SignedODFDocumentException_IT)errs.get(0);
        m_signedInfo = si;
    }

    /**
     * Calculates the SignedInfoXADES_IT digest
     * @return SignedInfoXADES_IT digest
     */
    public byte[] calculateSignedInfoDigest()
        throws SignedODFDocumentException_IT
    {
        return m_signedInfo.calculateDigest();
    }
    
    /**
     * Accessor for signatureValue attribute
     * @return value of signatureValue attribute
     */
    public SignatureValue_IT getSignatureValue() {
        return m_signatureValue;
    }
    
    /**
     * Mutator for signatureValue attribute
     * @param str new value for signatureValue attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setSignatureValue(SignatureValue_IT sv) 
        throws SignedODFDocumentException_IT
    {
        //ArrayList errs = sv.validate();
        //if(!errs.isEmpty())
        //    throw (SignedODFDocumentException_IT)errs.get(0);
        m_signatureValue = sv;
        // VS: bug fix on 14.05.2008
        m_origContent = null;
    }
    
    /**
     * Creates a new SignatureValue object
     * of this signature
     * @param sigv signatures byte data
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setSignatureValue(byte[] sigv) 
        throws SignedODFDocumentException_IT
    {
        SignatureValue_IT sv = new SignatureValue_IT(this, sigv);
        setSignatureValue(sv);
    }
    
    /**
     * Accessor for keyInfo attribute
     * @return value of keyInfo attribute
     */
    public KeyInfo_IT getKeyInfo() {
        return m_keyInfo;
    }
    
    /**
     * Mutator for keyInfo attribute
     * @param str new value for keyInfo attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setKeyInfo(KeyInfo_IT ki) 
        throws SignedODFDocumentException_IT
    {
        //ArrayList errs = ki.validate();
        //if(!errs.isEmpty())
        //    throw (SignedODFDocumentException_IT)errs.get(0);
        m_keyInfo = ki;
    }
    
    /**
     * Accessor for signedProperties attribute
     * @return value of SignedPropertiesXADES_IT attribute
     */
    public SignedPropertiesXADES_IT getSignedProperties() {
        return m_sigProp;
    }
    
    /**
     * Mutator for signedProperties attribute
     * @param str new value for signedProperties attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setSignedProperties(SignedPropertiesXADES_IT sp) 
        throws SignedODFDocumentException_IT
    {
        //ArrayList errs = sp.validate();
        //if(!errs.isEmpty())
        //    throw (SignedODFDocumentException_IT)errs.get(0);
        m_sigProp = sp;
    }
    
    /**
     * Accessor for unsignedProperties attribute
     * @return value of unsignedProperties attribute
     */
    public UnsignedProperties_IT getUnsignedProperties() {
        return m_unsigProp;
    }
    
    /**
     * Mutator for unsignedProperties attribute
     * @param str new value for unsignedProperties attribute
     * @throws SignedODFDocumentException_IT for validation errors
     */    
    public void setUnsignedProperties(UnsignedProperties_IT usp) 
        throws SignedODFDocumentException_IT
    {
        //ArrayList errs = usp.validate();
        //if(!errs.isEmpty())
        //    throw (SignedODFDocumentException_IT)errs.get(0);
        m_unsigProp = usp;
    }
    /**
     * return the count of CertID_IT objects
     * @return count of CertID_IT objects
     */
    public int countCertIDs()
    {
        return ((m_certIds == null) ? 0 : m_certIds.size());
    }
    
    /**
     * Adds a new CertID object
     * @param cid new object to be added
     */
    public void addCertID(CertID_IT cid)
    {
    	if(m_certIds == null)
    		m_certIds = new ArrayList();
    	cid.setSignature(this);
    	m_certIds.add(cid);
    }
    
    /**
     * Retrieves CertID element with the desired index
     * @param idx CertID index
     * @return CertID element or null if not found
     */
    public CertID_IT getCertID(int idx)
    {
    	if(m_certIds != null && idx < m_certIds.size()) {
    		return (CertID_IT)m_certIds.get(idx);
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves the last CertID element
     * @return CertID element or null if not found
     */
    public CertID_IT getLastCertId()
    {
    	if(m_certIds != null && m_certIds.size() > 0) {
    		return (CertID_IT)m_certIds.get(m_certIds.size()-1);
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves CertID_IT element with the desired type
     * @param type CertID_IT type
     * @return CertID_IT element or null if not found
     */
    public CertID_IT getCertIdOfType(int type)
    {
    	for(int i = 0; (m_certIds != null) && (i < m_certIds.size()); i++) {
    		CertID_IT cid = (CertID_IT)m_certIds.get(i);
    		if(cid.getType() == type)
    			return cid;
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves CertID_IT element with the desired type.
     * If not found creates a new one with this type.
     * @param type CertID_IT type
     * @return CertID_IT element
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CertID_IT getOrCreateCertIdOfType(int type)
    	throws SignedODFDocumentException_IT
    {
    	CertID_IT cid = getCertIdOfType(type);
    	if(cid == null) {
    		cid = new CertID_IT();
    		cid.setType(type);
    		addCertID(cid);
    	}
    	return cid; // not found
    }
    
    /**
     * return the count of CertValue_IT objects
     * @return count of CertValue_ITs objects
     */
    public int countCertValues()
    {
        return ((m_certValues == null) ? 0 : m_certValues.size());
    }
    
    /**
     * Adds a new CertValue_IT object
     * @param cval new object to be added
     */
    public void addCertValue(CertValue_IT cval)
    {
    	if(m_certValues == null)
    		m_certValues = new ArrayList();
    	cval.setSignature(this);
    	m_certValues.add(cval);
    }
    
    /**
     * Retrieves CertValue_IT element with the desired index
     * @param idx CertValue_IT index
     * @return CertValue_IT element or null if not found
     */
    public CertValue_IT getCertValue(int idx)
    {
    	if(m_certValues != null && idx < m_certValues.size()) {
    		return (CertValue_IT)m_certValues.get(idx);
    	} else
    	return null; // not found
    }
    
    /**
     * Retrieves the last CertValue_IT element 
     * @return CertValue_IT element or null if not found
     */
    public CertValue_IT getLastCertValue()
    {
    	if(m_certValues != null && m_certValues.size() > 0) {
    		return (CertValue_IT)m_certValues.get(m_certValues.size()-1);
    	} else
    		return null; // not found
    }
    
    /**
     * Retrieves CertValue_IT element with the desired type
     * @param type CertValue_IT type
     * @return CertValue_IT element or null if not found
     */
    public CertValue_IT getCertValueOfType(int type)
    {
    	for(int i = 0; (m_certValues != null) && (i < m_certValues.size()); i++) {
    		CertValue_IT cval = (CertValue_IT)m_certValues.get(i);
    		if(cval.getType() == type)
    			return cval;
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves CertValue_IT element with the desired type.
     * If not found creates a new one with this type.
     * @param type CertValue_IT type
     * @return CertValue_IT element
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public CertValue_IT getOrCreateCertValueOfType(int type)
    	throws SignedODFDocumentException_IT
    {
    	CertValue_IT cval = getCertValueOfType(type);
    	if(cval == null) {
    		cval = new CertValue_IT();
    		cval.setType(type);
    		addCertValue(cval);
    	}
    	return cval; // not found
    }
    
    /**
     * Returns the first CertValue_IT with the given serial
     * number that has been attached to this signature in
     * digidoc document. This could be either the signers 
     * cert, OCSP responders cert or one of the TSA certs.
     * @param serNo certificates serial number
     * @return found CertValue_IT or null
     */
    public CertValue_IT findCertValueWithSerial(BigInteger serNo)
    {
    	for(int i = 0; (m_certValues != null) && (i < m_certValues.size()); i++) {
    		CertValue_IT cval = (CertValue_IT)m_certValues.get(i);
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
    	CertValue_IT cval = getCertValueOfType(CertValue_IT.CERTVAL_TYPE_RESPONDER);
    	if(cval != null)
    		return cval.getCert();
    	else
    		return null;
    }
    
    /**
     * Retrieves TSA certificates
     * @return TSA certificates
     */
    public ArrayList findTSACerts()
    {
    	ArrayList vec = new ArrayList();
    	for(int i = 0; (m_certValues != null) && (i < m_certValues.size()); i++) {
    		CertValue_IT cval = (CertValue_IT)m_certValues.get(i);
    		if(cval.getType() == CertValue_IT.CERTVAL_TYPE_TSA)
    			vec.add(cval.getCert());
    	}
    	return vec;
    }
    
    /**
     * return the count of TimestampInfo_IT objects
     * @return count of TimestampInfo_IT objects
     */
    public int countTimestampInfos()
    {
        return ((m_timestamps == null) ? 0 : m_timestamps.size());
    }
    
    /**
     * Adds a new TimestampInfo_IT object
     * @param ts new object to be added
     */
    public void addTimestampInfo(TimestampInfo_IT ts)
    {
    	if(m_timestamps == null)
    		m_timestamps = new ArrayList();
    	ts.setSignature(this);
    	m_timestamps.add(ts);
    }
    
    /**
     * Retrieves TimestampInfo_IT element with the desired index
     * @param idx TimestampInfo_IT index
     * @return TimestampInfo_IT element or null if not found
     */
    public TimestampInfo_IT getTimestampInfo(int idx)
    {
    	if(m_timestamps != null && idx < m_timestamps.size()) {
    		return (TimestampInfo_IT)m_timestamps.get(idx);
    	} else
    		return null; // not found
    }
    
    /**
     * Retrieves the last TimestampInfo_IT element 
     * @return TimestampInfo_IT element or null if not found
     */
    public TimestampInfo_IT getLastTimestampInfo()
    {
    	if(m_timestamps != null && m_timestamps.size() > 0) {
    		return (TimestampInfo_IT)m_timestamps.get(m_timestamps.size()-1);
    	} else
    		return null; // not found
    }
    
    /**
     * Retrieves TimestampInfo_IT element with the desired type
     * @param type TimestampInfo_IT type
     * @return TimestampInfo_IT element or null if not found
     */
    public TimestampInfo_IT getTimestampInfoOfType(int type)
    {
    	for(int i = 0; (m_timestamps != null) && (i < m_timestamps.size()); i++) {
    		TimestampInfo_IT ts = (TimestampInfo_IT)m_timestamps.get(i);
    		if(ts.getType() == type)
    			return ts;
    	}
    	return null; // not found
    }
    
    /**
     * Retrieves TimestampInfo_IT element with the desired type.
     * If not found creates a new one with this type.
     * @param type TimestampInfo_IT type
     * @return TimestampInfo_IT element
     * @throws SignedODFDocumentException_IT for validation errors
     */
    public TimestampInfo_IT getOrCreateTimestampInfoOfType(int type)
    	throws SignedODFDocumentException_IT
    {
    	TimestampInfo_IT ts = getTimestampInfoOfType(type);
    	if(ts == null) {
    		ts = new TimestampInfo_IT();
    		ts.setType(type);
    		addTimestampInfo(ts);
    	}
    	return ts; // not found
    }
    
    /**
     * Gets confirmation and adds the corresponding
     * members that carry the returned info to
     * this signature
     * @throws SignedODFDocumentException_IT for all errors
     */
    public void getConfirmation()
        throws SignedODFDocumentException_IT
    {
        NotaryFactory_IT notFac = ConfigManager_IT.
	    instance().getNotaryFactory();
        
        
        
        X509Certificate cert = m_keyInfo.getSignersCertificate();
        DigiDocFactory_IT ddocFac = ConfigManager_IT.instance().getDigiDocFactory();
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
        CertValue_IT cval = new CertValue_IT();
        cval.setType(CertValue.CERTVAL_TYPE_RESPONDER);
        cval.setCert(rcert);
        addCertValue(cval);
        cval.setId(m_id + "-RESPONDER_CERT");
        CertID_IT cid = new CertID_IT(this, rcert, CertID_IT.CERTID_TYPE_RESPONDER);
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
		    SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_OCSP_GET_CONF);
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
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList verify(SignedODFDocument_IT sdoc, boolean checkDate, boolean demandConfirmation)
    {
    	Date do1 = null, dt1 = null, dt2 = null;
        ArrayList errs = new ArrayList();
        // check the DataFile_IT digests
        for(int i = 0; i < sdoc.countDataFiles(); i++) {
            DataFile_IT df = sdoc.getDataFile(i);
            //System.out.println("Check digest for DF: " + df.getId());
            ReferenceXADES_IT ref = m_signedInfo.getReferenceForDataFile(df);
            byte[] dfDig = null;
            try {
                dfDig = df.getDigest();
            } catch(SignedODFDocumentException_IT ex) {
                errs.add(ex);
            }
            if(ref != null) {
            	//System.out.println("Compare it to: " + Base64Util.encode(ref.getDigestValue(), 0));
                if(!SignedODFDocument_IT.compareDigests(ref.getDigestValue(), dfDig)) {
                   errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_DIGEST_COMPARE,
                    "Bad digest for DataFile_IT: " + df.getId(), null));
                    //System.out.println("BAD DIGEST");
                }
                //else System.out.println("GOOD DIGEST");
            } else {
            	//System.out.println("No ReferenceXADES_IT");
                errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_DATA_FILE_NOT_SIGNED,
                    "No ReferenceXADES_IT element for DataFile_IT: " + df.getId(), null));
            }
            // if this is a detatched file and the file
            // referred by this entry actually exists,
            // then go and check it's digest
            // If the datafile doesn't exist the
            // just trust whatever is in the XML
            if(df.getContentType().equals(DataFile_IT.CONTENT_DETATCHED)) {
                File fTest = new File(df.getFileName());
                if(fTest.canRead()) {
                    //System.out.println("Check detatched file: " + fTest.getAbsolutePath());
                    byte[] realDigest = null;
                    byte[] detDigest = null;
                    try {
                        realDigest = df.calculateDetatchedFileDigest();
                        detDigest = df.getDigestValue();
                    } catch(SignedODFDocumentException_IT ex) {
                        errs.add(ex);
                    }
                    if(!SignedODFDocument_IT.compareDigests(detDigest, realDigest)) {
                        errs.add(new SignedODFDocumentException_IT(
                            SignedODFDocumentException_IT.ERR_DIGEST_COMPARE,
                            "Bad digest for detatched file: " + df.getFileName(), null));
                    }
                }
                //else System.out.println("Cannot read detatched file: " + fTest.getAbsolutePath());
            }
        }
        // check signed properties digest
        ReferenceXADES_IT ref2 = m_signedInfo.getReferenceForSignedProperties(m_sigProp);
        if(ref2 != null) {
            byte[] spDig = null;
            try {
                spDig = m_sigProp.calculateDigest();
                //System.out.println("SignedProp real digest: " + Base64Util.encode(spDig, 0));
            } catch(SignedODFDocumentException_IT ex) {
                errs.add(ex);
            }
            //System.out.println("Compare it to: " + Base64Util.encode(ref2.getDigestValue(), 0));
            if(!SignedODFDocument_IT.compareDigests(ref2.getDigestValue(), spDig)) {
                   errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_DIGEST_COMPARE,
                    "Bad digest for SignedProperties: " + m_sigProp.getId(), null));
                    //System.out.println("BAD DIGEST");
            }
            //else System.out.println("GOOD DIGEST");
        } else {
            errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_SIG_PROP_NOT_SIGNED,
                    "No ReferenceXADES_IT element for SignedProperties: " + m_sigProp.getId(), null));
        }
        // verify signature value
        try {
            byte[] dig = m_signedInfo.calculateDigest();
            //System.out.println("SignedInfo real digest: " + Base64Util.encode(dig, 0) + " hex: " + SignedODFDocument_IT.bin2hex(dig));
            SignedODFDocument_IT.verify(dig, m_signatureValue.getValue(), m_keyInfo.getSignersCertificate());
            //System.out.println("GOOD DIGEST");
        } catch(SignedODFDocumentException_IT ex) {
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
            errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_CERT_EXPIRED,
                    "Signers certificate has expired!", null));
        }
        // check certificates CA
        try {
        	DigiDocFactory_IT digFac = ConfigManager_IT.instance().getDigiDocFactory();
        	digFac.verifyCertificate(m_keyInfo.getSignersCertificate());
        } catch(SignedODFDocumentException_IT ex) {
            errs.add(ex);
        }
        // if we check signatures using CRL
        String verifier = ConfigManager_IT.instance().
                getStringProperty("DIGIDOC_SIGNATURE_VERIFIER", "OCSP");
        if(verifier != null && verifier.equals("CRL")) {
        	try {
        		CRLFactory_IT crlFac = ConfigManager_IT.instance().getCRLFactory();
            	crlFac.checkCertificate(m_keyInfo.getSignersCertificate(), new Date());
            } catch(SignedODFDocumentException_IT ex) {
         	   errs.add(ex);
        	}
        }
        // check confirmation
        if(m_unsigProp != null) {
            ArrayList e = m_unsigProp.verify(sdoc);
            if(!e.isEmpty())
                errs.addAll(e);
            if(m_unsigProp.getNotary() != null)
            	do1 = m_unsigProp.getNotary().getProducedAt();
        } else { // not OCSP confirmation
            if(demandConfirmation)
                errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_NO_CONFIRMATION,
                        "Signature has no OCSP confirmation!", null));
        }
        // verify timestamps
        ArrayList tsaCerts = findTSACerts();
        if(m_timestamps != null && m_timestamps.size() > 0) {
        	TimestampFactory_IT tsFac = null;
        	try {
        		tsFac = ConfigManager_IT.instance().getTimestampFactory();
        	} catch(SignedODFDocumentException_IT ex) {
        		//m_logger.error("Failed to get TimestampFactory_IT: " + ex);
        		errs.add(ex);
        	}
        	ArrayList e = tsFac.verifySignaturesTimestamps(this);
        	if(!e.isEmpty())
                errs.addAll(e);
        	for(int i = 0; i < m_timestamps.size(); i++) {
        		TimestampInfo_IT ts = (TimestampInfo_IT)m_timestamps.get(i);
        		if(ts.getType() == TimestampInfo_IT.TIMESTAMP_TYPE_SIGNATURE)
        			dt1 = ts.getTime();
        		if(ts.getType() == TimestampInfo_IT.TIMESTAMP_TYPE_SIG_AND_REFS)
        			dt2 = ts.getTime();
        	}
        	//System.out.println("OCSP time: " + do1);
        	//System.out.println("SignatureTimeStamp time: " + dt1);
        	//System.out.println("SigAndRefsTimeStamp time: " + dt2);
        	int nMaxTsTimeErrSecs = ConfigManager_IT.instance().getIntProperty("MAX_TSA_TIME_ERR_SECS", 0);
        	dt1 = new Date(dt1.getTime() - (nMaxTsTimeErrSecs * 1000));
        	dt2 = new Date(dt2.getTime() + (nMaxTsTimeErrSecs * 1000));
        	//System.out.println("SignatureTimeStamp adj time: " + dt1);
        	//System.out.println("SigAndRefsTimeStamp adj time: " + dt2);
        	if(dt2.before(dt1))
        		errs.add(new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_VERIFY, "SignAndRefsTimeStamp is before SignatureTimeStamp", null));
        	if(do1.before(dt1) || do1.after(dt2))
        		errs.add(new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_VERIFY, "OCSP time is not between SignAndRefsTimeStamp and SignatureTimeStamp", null));
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
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList verifyOcspOrCrl(SignedODFDocument_IT sdoc, boolean checkDate, boolean bUseOcsp)
    {
    	Date do1 = null, dt1 = null, dt2 = null;
        ArrayList errs = new ArrayList();
        // check the DataFile_IT digests
        for(int i = 0; i < sdoc.countDataFiles(); i++) {
            DataFile_IT df = sdoc.getDataFile(i);
            //System.out.println("Check digest for DF: " + df.getId());
            ReferenceXADES_IT ref = m_signedInfo.getReferenceForDataFile(df);
            byte[] dfDig = null;
            try {
                dfDig = df.getDigest();
            } catch(SignedODFDocumentException_IT ex) {
                errs.add(ex);
            }
            if(ref != null) {
            	//System.out.println("Compare it to: " + Base64Util.encode(ref.getDigestValue(), 0));
                if(!SignedODFDocument_IT.compareDigests(ref.getDigestValue(), dfDig)) {
                   errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_DIGEST_COMPARE,
                    "Bad digest for DataFile_IT: " + df.getId(), null));
                    //System.out.println("BAD DIGEST");
                }
                //else System.out.println("GOOD DIGEST");
            } else {
            	//System.out.println("No ReferenceXADES_IT");
                errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_DATA_FILE_NOT_SIGNED,
                    "No ReferenceXADES_IT element for DataFile_IT: " + df.getId(), null));
            }
            // if this is a detatched file and the file
            // referred by this entry actually exists,
            // then go and check it's digest
            // If the datafile doesn't exist the
            // just trust whatever is in the XML
            if(df.getContentType().equals(DataFile_IT.CONTENT_DETATCHED)) {
                File fTest = new File(df.getFileName());
                if(fTest.canRead()) {
                    //System.out.println("Check detatched file: " + fTest.getAbsolutePath());
                    byte[] realDigest = null;
                    byte[] detDigest = null;
                    try {
                        realDigest = df.calculateDetatchedFileDigest();
                        detDigest = df.getDigestValue();
                    } catch(SignedODFDocumentException_IT ex) {
                        errs.add(ex);
                    }
                    if(!SignedODFDocument_IT.compareDigests(detDigest, realDigest)) {
                        errs.add(new SignedODFDocumentException_IT(
                            SignedODFDocumentException_IT.ERR_DIGEST_COMPARE,
                            "Bad digest for detatched file: " + df.getFileName(), null));
                    }
                }
                //else System.out.println("Cannot read detatched file: " + fTest.getAbsolutePath());
            }
        }
        // check signed properties digest
        ReferenceXADES_IT ref2 = m_signedInfo.getReferenceForSignedProperties(m_sigProp);
        if(ref2 != null) {
            byte[] spDig = null;
            try {
                spDig = m_sigProp.calculateDigest();
                //System.out.println("SignedProp real digest: " + Base64Util.encode(spDig, 0));
            } catch(SignedODFDocumentException_IT ex) {
                errs.add(ex);
            }
            //System.out.println("Compare it to: " + Base64Util.encode(ref2.getDigestValue(), 0));
            if(!SignedODFDocument_IT.compareDigests(ref2.getDigestValue(), spDig)) {
                   errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_DIGEST_COMPARE,
                    "Bad digest for SignedProperties: " + m_sigProp.getId(), null));
                    //System.out.println("BAD DIGEST");
            }
            //else System.out.println("GOOD DIGEST");
        } else {
            errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_SIG_PROP_NOT_SIGNED,
                    "No ReferenceXADES_IT element for SignedProperties: " + m_sigProp.getId(), null));
        }
        // verify signature value
        try {
            byte[] dig = m_signedInfo.calculateDigest();
            //System.out.println("SignedInfo real digest: " + Base64Util.encode(dig, 0) + " hex: " + SignedODFDocument_IT.bin2hex(dig));
            SignedODFDocument_IT.verify(dig, m_signatureValue.getValue(), m_keyInfo.getSignersCertificate());
            //System.out.println("GOOD DIGEST");
        } catch(SignedODFDocumentException_IT ex) {
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
            errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_CERT_EXPIRED,
                    "Signers certificate has expired!", null));
        }
        // check certificates CA
        try {
        	DigiDocFactory_IT digFac = ConfigManager_IT.instance().getDigiDocFactory();
        	digFac.verifyCertificate(m_keyInfo.getSignersCertificate());
        } catch(SignedODFDocumentException_IT ex) {
            errs.add(ex);
        }
        // switch OCSP or CRL verification
        if(bUseOcsp) { // use OCSP
        	// check confirmation
        	if(m_unsigProp != null) {
                ArrayList e = m_unsigProp.verify(sdoc);
                if(!e.isEmpty())
                    errs.addAll(e);
            } else { // not OCSP confirmation
                errs.add(new SignedODFDocumentException_IT(
                    SignedODFDocumentException_IT.ERR_NO_CONFIRMATION,
                            "Signature has no OCSP confirmation!", null));
            }        	
        	// verify timestamps
            ArrayList tsaCerts = findTSACerts();
            if(m_timestamps.size() > 0) {
            	TimestampFactory_IT tsFac = null;
            	try {
            		tsFac = ConfigManager_IT.instance().getTimestampFactory();
            	} catch(SignedODFDocumentException_IT ex) {
            		//m_logger.error("Failed to get TimestampFactory_IT: " + ex);
            		errs.add(ex);
            	}
            	ArrayList e = tsFac.verifySignaturesTimestamps(this);
            	if(!e.isEmpty())
                    errs.addAll(e);
            	for(int i = 0; i < m_timestamps.size(); i++) {
            		TimestampInfo_IT ts = (TimestampInfo_IT)m_timestamps.get(i);
            		if(ts.getType() == TimestampInfo_IT.TIMESTAMP_TYPE_SIGNATURE)
            			dt1 = ts.getTime();
            		if(ts.getType() == TimestampInfo_IT.TIMESTAMP_TYPE_SIG_AND_REFS)
            			dt2 = ts.getTime();
            	}
            	//System.out.println("OCSP time: " + do1);
            	//System.out.println("SignatureTimeStamp time: " + dt1);
            	//System.out.println("SigAndRefsTimeStamp time: " + dt2);
            	int nMaxTsTimeErrSecs = ConfigManager_IT.instance().getIntProperty("MAX_TSA_TIME_ERR_SECS", 0);
            	dt1 = new Date(dt1.getTime() - (nMaxTsTimeErrSecs * 1000));
            	dt2 = new Date(dt2.getTime() + (nMaxTsTimeErrSecs * 1000));
            	//System.out.println("SignatureTimeStamp adj time: " + dt1);
            	//System.out.println("SigAndRefsTimeStamp adj time: " + dt2);
            	if(dt2.before(dt1))
            		errs.add(new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_VERIFY, "SignAndRefsTimeStamp is before SignatureTimeStamp", null));
            	if(do1.before(dt1) || do1.after(dt2))
            		errs.add(new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_TIMESTAMP_VERIFY, "OCSP time is not between SignAndRefsTimeStamp and SignatureTimeStamp", null));
            }
        } else {
        	try {
        		CRLFactory_IT crlFac = ConfigManager_IT.instance().getCRLFactory();
            	crlFac.checkCertificate(m_keyInfo.getSignersCertificate(), new Date());
            } catch(SignedODFDocumentException_IT ex) {
         	   errs.add(ex);
        	}
        }
        return errs;
    }
    
    /**
     * Helper method to validate the whole
     * Signature object
     * @return a possibly empty list of SignedODFDocumentException_IT objects
     */
    public ArrayList validate()
    {
        ArrayList errs = new ArrayList();
        SignedODFDocumentException_IT ex = validateId(m_id);
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
        throws SignedODFDocumentException_IT
    {
    	if(m_origContent == null) {
        	ByteArrayOutputStream bos = 
            	new ByteArrayOutputStream();
        	try {
            	bos.write(ConvertUtils.str2data("<Signature Id=\""));
            	bos.write(ConvertUtils.str2data(m_id));
            	bos.write(ConvertUtils.str2data("\" xmlns=\"" + SignedODFDocument_IT.xmlns_xmldsig + "\">\n"));
            	bos.write(m_signedInfo.toXML());
            	bos.write(ConvertUtils.str2data("\n"));
            	// VS: 2.2.24 - fix to allowe Signature without SignatureValue - incomplete sig
				if(m_signatureValue != null)
            		bos.write(m_signatureValue.toXML());
            	bos.write(ConvertUtils.str2data("\n"));
            	bos.write(m_keyInfo.toXML());
            	// In version 1.3 we use xmlns atributes like specified in XAdES 
            	if(m_sigDoc.getVersion().equals(SignedODFDocument_IT.VERSION_1_3)) {
            		bos.write(ConvertUtils.str2data("\n<Object><QualifyingProperties xmlns=\""));
            		bos.write(ConvertUtils.str2data(SignedODFDocument_IT.xmlns_etsi));
            		bos.write(ConvertUtils.str2data("\" Target=\"#"));
            		bos.write(ConvertUtils.str2data(m_id));
            		bos.write(ConvertUtils.str2data("\">"));
            	} else // in versions prior to 1.3 we used atributes in wrong places
            		bos.write(ConvertUtils.str2data("\n<Object><QualifyingProperties>"));
            	if(m_sigProp != null)
                	bos.write(m_sigProp.toXML());
            	if(m_unsigProp != null)
                	bos.write(m_unsigProp.toXML());
            	bos.write(ConvertUtils.str2data("</QualifyingProperties></Object>\n"));
            	bos.write(ConvertUtils.str2data("</Signature>"));
        	} catch(IOException ex) {
            	SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_XML_CONVERT);
        	}
        	return bos.toByteArray();
    	}
    	else
    		return m_origContent;
    }

    /**
     * Returns the stringified form of Signature
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
