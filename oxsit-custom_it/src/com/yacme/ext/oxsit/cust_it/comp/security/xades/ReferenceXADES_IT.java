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
public class ReferenceXADES_IT implements Serializable {
	/** reference to parent SignedInfoXADES_IT object */
	private SignedInfoXADES_IT m_sigInfo;
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
	 *            reference to parent SignedInfoXADES_IT object
	 */
	public ReferenceXADES_IT(SignedInfoXADES_IT sigInfo) {
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
	 *            reference to parent SignedInfoXADES_IT object
	 * @param uri
	 *            reference uri pointing to signed XML data
	 * @param algorithm
	 *            sigest algorithm identifier
	 * @param digest
	 *            message digest data
	 * @param transform
	 *            transform algorithm
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public ReferenceXADES_IT(SignedInfoXADES_IT sigInfo, String uri, String algorithm,
			byte[] digest, String transform) throws SignedODFDocumentException_IT {
		m_sigInfo = sigInfo;
		setUri(uri);
		setDigestAlgorithm(algorithm);
		setDigestValue(digest);
		setTransformAlgorithm(transform);
	}

	/**
	 * Creates new Reference and initializes it with default values from the
	 * DataFile_IT
	 * 
	 * @param sigInfo
	 *            reference to parent SignedInfoXADES_IT object
	 * @param df
	 *            DataFile_IT object
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public ReferenceXADES_IT(SignedInfoXADES_IT sigInfo, DataFile_IT df)
			throws SignedODFDocumentException_IT {
		

		m_sigInfo = sigInfo;
		setUri("#" + df.getId());
		setDigestAlgorithm(SignedODFDocument_IT.SHA1_DIGEST_ALGORITHM);
		setDigestValue(df.getDigest());
		setTransformAlgorithm(df.getContentType().equals(
				DataFile_IT.CONTENT_DETATCHED) ? SignedODFDocument_IT.DIGIDOC_DETATCHED_TRANSFORM
				: null);
	}

	/**
	 * Accessor for sigInfo attribute
	 * 
	 * @return value of sigInfo attribute
	 */
	public SignedInfoXADES_IT getSignedInfo() {
		return m_sigInfo;
	}

	/**
	 * Mutator for sigInfo attribute
	 * 
	 * @param sigInfo
	 *            new value for sigInfo attribute
	 */
	public void setSignedInfo(SignedInfoXADES_IT sigInfo) {
		m_sigInfo = sigInfo;
	}

	/**
	 * Creates new Reference and initializes it with default values from the
	 * SignedProperties
	 * 
	 * @param sigInfo
	 *            reference to parent SignedInfoXADES_IT object
	 * @param sp
	 *            SignedPropertiesXADES_IT object
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public ReferenceXADES_IT(SignedInfoXADES_IT sigInfo, SignedPropertiesXADES_IT sp)
			throws SignedODFDocumentException_IT {
		m_sigInfo = sigInfo;
		setUri(sp.getTarget() + "-SignedProperties");
		setDigestAlgorithm(SignedODFDocument_IT.SHA1_DIGEST_ALGORITHM);
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
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public void setUri(String str) throws SignedODFDocumentException_IT {
		SignedODFDocumentException_IT ex = validateUri(str);
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
	private SignedODFDocumentException_IT validateUri(String str) {
		SignedODFDocumentException_IT ex = null;
		if (str == null) // check the uri somehow ???
			ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_REFERENCE_URI,
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
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public void setDigestAlgorithm(String str) throws SignedODFDocumentException_IT {
		SignedODFDocumentException_IT ex = validateDigestAlgorithm(str);
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
	private SignedODFDocumentException_IT validateDigestAlgorithm(String str) {
		SignedODFDocumentException_IT ex = null;
		if (str == null || !str.equals(SignedODFDocument_IT.SHA1_DIGEST_ALGORITHM))
			ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_DIGEST_ALGORITHM,
					"Currently supports only SHA1 digest algorithm", null);
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
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public void setDigestValue(byte[] data) throws SignedODFDocumentException_IT {
		SignedODFDocumentException_IT ex = validateDigestValue(data);
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
	private SignedODFDocumentException_IT validateDigestValue(byte[] data) {
		SignedODFDocumentException_IT ex = null;
		if (data == null || data.length != SignedODFDocument_IT.SHA1_DIGEST_LENGTH)
			ex = new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_DIGEST_LENGTH,
					"SHA1 digest data is allways 20 bytes of length", null);
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
	 * @throws SignedODFDocumentException_IT
	 *             for validation errors
	 */
	public void setTransformAlgorithm(String str) throws SignedODFDocumentException_IT {
		SignedODFDocumentException_IT ex = validateTransformAlgorithm(str);
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
	private SignedODFDocumentException_IT validateTransformAlgorithm(String str) {
		SignedODFDocumentException_IT ex = null;
		if (str != null && !str.equals(SignedODFDocument_IT.DIGIDOC_DETATCHED_TRANSFORM))
			ex = new SignedODFDocumentException_IT(
					SignedODFDocumentException_IT.ERR_TRANSFORM_ALGORITHM,
					"Currently supports either no transforms or one detatched document transform",
					null);
		return ex;
	}

	/**
	 * Helper method to validate the whole Reference object
	 * 
	 * @return a possibly empty list of SignedODFDocumentException_IT objects
	 */
	public ArrayList validate() {
		ArrayList errs = new ArrayList();
		SignedODFDocumentException_IT ex = validateUri(m_uri);
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
	public byte[] toXML() throws SignedODFDocumentException_IT {
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
					SignedODFDocument_IT.VERSION_1_2) || m_sigInfo.getSignature()
					.getSignedDoc().getVersion().equals(SignedODFDocument_IT.VERSION_1_3))
					&& m_uri.indexOf("SignedProperties") != -1) {
				bos
						.write(ConvertUtils
								.str2data(" Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\""));
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
			SignedODFDocumentException_IT.handleException(ex,
					SignedODFDocumentException_IT.ERR_XML_CONVERT);
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
