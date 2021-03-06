/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents and XML-DSIG reference block that referrs to a particular piece of
 * signed XML data and contains it's hash code.
 * 
 * @author Veiko Sinivee, modified by G. Castagno
 * @version 1.0
 */
public class Reference implements Serializable {
	/** reference to parent SignedInfo object */
	private SignedInfo m_sigInfo;
	/** URI to signed XML data */
	private String m_uri;
	/** selected digest algorithm */
	private String m_digestAlgorithm;
	/** digest data */
	private byte[] m_digestValue;
	/** transform algorithm */
	private String m_transformAlgorithm;

	/**
	 * Creates new Reference. Initializes everything to null
	 * 
	 * @param sigInfo
	 *            reference to parent SignedInfo object
	 */
	public Reference(SignedInfo sigInfo) {
		m_sigInfo = sigInfo;
		m_uri = null;
		m_digestAlgorithm = null;
		m_digestValue = null;
		m_transformAlgorithm = null;
	}

	/**
	 * Creates new Reference
	 * 
	 * @param sigInfo
	 *            reference to parent SignedInfo object
	 * @param uri
	 *            reference uri pointing to signed XML data
	 * @param algorithm
	 *            sigest algorithm identifier
	 * @param digest
	 *            message digest data
	 * @param transform
	 *            transform algorithm
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public Reference(SignedInfo sigInfo, String uri, String algorithm,
			byte[] digest, String transform) throws SignedDocException {
		m_sigInfo = sigInfo;
		setUri(uri);
		setDigestAlgorithm(algorithm);
		setDigestValue(digest);
		setTransformAlgorithm(transform);
	}

	/**
	 * Creates new Reference and initializes it with default values from the
	 * DataFile
	 * 
	 * @param sigInfo
	 *            reference to parent SignedInfo object
	 * @param df
	 *            DataFile object
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public Reference(SignedInfo sigInfo, DataFile df)
			throws SignedDocException {
		

		m_sigInfo = sigInfo;
		setUri("#" + df.getId());
		//ROB
		setDigestAlgorithm(SignedDoc.SHA256_DIGEST_ALGORITHM);
		setDigestValue(df.getDigest());
		setTransformAlgorithm(df.getContentType().equals(
				DataFile.CONTENT_DETATCHED) ? SignedDoc.DIGIDOC_DETATCHED_TRANSFORM
				: null);
		//ROB
		setTransformAlgorithm(df.getContentType().equals(
				DataFile.CONTENT_ODF_PKG_XML_ENTRY) ? SignedDoc.CANONICALIZATION_METHOD_20010315
				: null);
	}

	/**
	 * Accessor for sigInfo attribute
	 * 
	 * @return value of sigInfo attribute
	 */
	public SignedInfo getSignedInfo() {
		return m_sigInfo;
	}

	/**
	 * Mutator for sigInfo attribute
	 * 
	 * @param sigInfo
	 *            new value for sigInfo attribute
	 */
	public void setSignedInfo(SignedInfo sigInfo) {
		m_sigInfo = sigInfo;
	}

	/**
	 * Creates new Reference and initializes it with default values from the
	 * SignedProperties
	 * 
	 * @param sigInfo
	 *            reference to parent SignedInfo object
	 * @param sp
	 *            SignedProperties object
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public Reference(SignedInfo sigInfo, SignedProperties sp)
			throws SignedDocException {
		m_sigInfo = sigInfo;
		setUri(sp.getTarget() + "-SignedProperties");
		//ROB
		setDigestAlgorithm(SignedDoc.SHA256_DIGEST_ALGORITHM);
		setDigestValue(sp.calculateDigest());
		setTransformAlgorithm(null);
	}

	/**
	 * Accessor for uri attribute
	 * 
	 * @return value of uri attribute
	 */
	public String getUri() {
		return m_uri;
	}

	/**
	 * Mutator for uri attribute
	 * 
	 * @param str
	 *            new value for uri attribute
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public void setUri(String str) throws SignedDocException {
		SignedDocException ex = validateUri(str);
		if (ex != null)
			throw ex;
		m_uri = str;
	}

	/**
	 * Helper method to validate a uri
	 * 
	 * @param str
	 *            input data
	 * @return exception or null for ok
	 */
	private SignedDocException validateUri(String str) {
		SignedDocException ex = null;
		if (str == null) // check the uri somehow ???
			ex = new SignedDocException(SignedDocException.ERR_REFERENCE_URI,
					"URI has to be in format #<DataFile-ID>", null);
		return ex;
	}

	/**
	 * Accessor for digestAlgorithm attribute
	 * 
	 * @return value of digestAlgorithm attribute
	 */
	public String getDigestAlgorithm() {
		return m_digestAlgorithm;
	}

	/**
	 * Mutator for digestAlgorithm attribute
	 * 
	 * @param str
	 *            new value for digestAlgorithm attribute
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public void setDigestAlgorithm(String str) throws SignedDocException {
		SignedDocException ex = validateDigestAlgorithm(str);
		if (ex != null)
			throw ex;
		m_digestAlgorithm = str;
	}

	/**
	 * Helper method to validate a digest algorithm
	 * 
	 * @param str
	 *            input data
	 * @return exception or null for ok
	 */
	private SignedDocException validateDigestAlgorithm(String str) {
		SignedDocException ex = null;
		//ROB
		if (str == null || !str.equals(SignedDoc.SHA256_DIGEST_ALGORITHM))
			ex = new SignedDocException(SignedDocException.ERR_DIGEST_ALGORITHM,
					"Currently supports only SHA256 digest algorithm", null);
		return ex;
	}

	/**
	 * Accessor for digestValue attribute
	 * 
	 * @return value of digestValue attribute
	 */
	public byte[] getDigestValue() {
		return m_digestValue;
	}

	/**
	 * Mutator for digestValue attribute
	 * 
	 * @param data
	 *            new value for digestValue attribute
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public void setDigestValue(byte[] data) throws SignedDocException {
		SignedDocException ex = validateDigestValue(data);
		if (ex != null)
			throw ex;
		m_digestValue = data;
	}

	/**
	 * Helper method to validate a digest value
	 * 
	 * @param data
	 *            input data
	 * @return exception or null for ok
	 */
	private SignedDocException validateDigestValue(byte[] data) {
		SignedDocException ex = null;
		if(data == null || data.length != SignedDoc.SHA256_DIGEST_LENGTH)
            ex = new SignedDocException(SignedDocException.ERR_DIGEST_LENGTH, 
                "SHA256 digest data is always "+ SignedDoc.SHA256_DIGEST_LENGTH +" bytes of length", null);
        return ex;
	}

	/**
	 * Accessor for transformAlgorithm attribute
	 * 
	 * @return value of transformAlgorithm attribute
	 */
	public String getTransformAlgorithm() {
		return m_transformAlgorithm;
	}

	/**
	 * Mutator for transformAlgorithm attribute. Currently supports only one
	 * transform which has to be digidoc detatched document transform or none at
	 * all.
	 * 
	 * @param str
	 *            new value for transformAlgorithm attribute
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public void setTransformAlgorithm(String str) throws SignedDocException {
		SignedDocException ex = validateTransformAlgorithm(str);
		if (ex != null)
			throw ex;
		m_transformAlgorithm = str;
	}

	/**
	 * Helper method to validate a transform algorithm
	 * 
	 * @param str
	 *            input data
	 * @return exception or null for ok
	 */
	private SignedDocException validateTransformAlgorithm(String str) {
		SignedDocException ex = null;
		/*
		if (str != null && !str.equals(SignedDoc.DIGIDOC_DETATCHED_TRANSFORM))
			ex = new SignedDocException(
					SignedDocException.ERR_TRANSFORM_ALGORITHM,
					"Currently supports either no transforms or one detatched document transform",
					null);
		*/
		//ROB
		if (str != null && !str.equals(SignedDoc.CANONICALIZATION_METHOD_20010315))
			ex = new SignedDocException(
					SignedDocException.ERR_TRANSFORM_ALGORITHM,
					"Currently supports either no transforms or one canonicalization transform",
					null);
		return ex;
	}

	/**
	 * Helper method to validate the whole Reference object
	 * 
	 * @return a possibly empty list of SignedDocException objects
	 */
	public ArrayList validate() {
		ArrayList errs = new ArrayList();
		SignedDocException ex = validateUri(m_uri);
		if (ex != null)
			errs.add(ex);
		ex = validateDigestAlgorithm(m_digestAlgorithm);
		if (ex != null)
			errs.add(ex);
		ex = validateDigestValue(m_digestValue);
		if (ex != null)
			errs.add(ex);
		ex = validateTransformAlgorithm(m_transformAlgorithm);
		if (ex != null)
			errs.add(ex);
		return errs;
	}

	/**
	 * Converts the Reference to XML form
	 * 
	 * @return XML representation of Reference
	 */
	public byte[] toXML() throws SignedDocException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			/*
			 * bos.write(ConvertUtils.str2data("<Reference URI=\""));
			 * bos.write(ConvertUtils.str2data(m_uri));
			 * if(m_sigInfo.getSignature().getSignedDoc().
			 * getVersion().equals(SignedDoc.VERSION_1_2)) {
			 * bos.write(ConvertUtils.str2data(
			 * "\" Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties"));
			 * }
			 */
			// VS: rc11_02 bug fix on Type attribute
			bos.write(ConvertUtils.str2data("<Reference"));
			if ((m_sigInfo.getSignature().getSignedDoc().getVersion().equals(
					SignedDoc.VERSION_1_2) || m_sigInfo.getSignature()
					.getSignedDoc().getVersion().equals(SignedDoc.VERSION_1_3))
					&& m_uri.indexOf("SignedProperties") != -1) {
				//ROB
				bos
						.write(ConvertUtils
								.str2data(" Type=\"http://uri.etsi.org/01903#SignedProperties\""));
			}
			bos.write(ConvertUtils.str2data(" URI=\""));
			bos.write(ConvertUtils.str2data(m_uri));
			bos.write(ConvertUtils.str2data("\">\n"));
			if (m_transformAlgorithm != null) {
				bos.write(ConvertUtils
						.str2data("<Transforms><Transform Algorithm=\""));
				bos.write(ConvertUtils.str2data(m_transformAlgorithm));
				bos.write(ConvertUtils
						.str2data("\"></Transform></Transforms>\n"));
			}
			bos.write(ConvertUtils.str2data("<DigestMethod Algorithm=\""));
			bos.write(ConvertUtils.str2data(m_digestAlgorithm));
			bos.write(ConvertUtils.str2data("\">\n</DigestMethod>\n"));
			bos.write(ConvertUtils.str2data("<DigestValue>"));
			bos.write(ConvertUtils
					.str2data(Base64Util.encode(m_digestValue, 0)));
			bos.write(ConvertUtils.str2data("</DigestValue>\n"));
			bos.write(ConvertUtils.str2data("</Reference>"));
		} catch (IOException ex) {
			SignedDocException.handleException(ex,
					SignedDocException.ERR_XML_CONVERT);
		}
		return bos.toByteArray();
	}

	/**
	 * Returns the stringified form of Reference
	 * 
	 * @return References string representation
	 */
	public String toString() {
		String str = null;
		try {
			str = new String(toXML());
		} catch (Exception ex) {
		}
		return str;
	}
}
