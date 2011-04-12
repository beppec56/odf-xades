/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.utils.ConfigManager;

/**
 * Represents a DataFile instance, that either contains payload data or
 * references and external DataFile.
 * 
 * @author Veiko Sinivee
 * @version 1.0
 */
public class ExternalDataFile extends DataFile implements Serializable {

	private static int block_size = 2048;
	/** log4j logger */
//	private Logger m_logger = null;

	/**
	 * Creates new DataFile
	 * 
	 * @param id
	 *            id of the DataFile
	 * @param contenType
	 *            DataFile content type
	 * @param fileName
	 *            original file name (without path!)
	 * @param mimeType
	 *            contents mime type
	 * @param sdoc
	 *            parent object
	 * @throws SignedDocException
	 *             for validation errors
	 */
	public ExternalDataFile(InputStream entryStream, String id,
			String mimeType, String fileName, String contentType, SignedDoc sdoc)
			throws SignedDocException {
		super(id, contentType, fileName, mimeType, sdoc);

//		m_logger = Logger.getLogger(ExternalDataFile.class);

		calculateFileSizeAndDigest(entryStream);
	}

	/**
	 * Helper method to validate a digestValue
	 * 
	 * @param str
	 *            input data
	 * @return exception or null for ok
	 */
	private SignedDocException validateDigestValue(byte[] data) {
		SignedDocException ex = null;
		if (data != null && data.length != SignedDoc.SHA1_DIGEST_LENGTH)
			ex = new SignedDocException(
					SignedDocException.ERR_DATA_FILE_DIGEST_VALUE,
					"SHA1 digest value must be 20 bytes", null);
		return ex;
	}



	/**
	 * Helper method for using an optimization for base64 data's conversion and
	 * digest calculation. We use data blockwise to conserve memory
	 * 
	 * @param os
	 *            output stream to write data
	 * @param digest
	 *            existing sha1 digest to be updated
	 * @param b64leftover
	 *            leftover base64 data from previous block
	 * @param b64left
	 *            leftover data length
	 * @param data
	 *            new binary data
	 * @param dLen
	 *            number of used bytes in data
	 * @param bLastBlock
	 *            flag last block
	 * @return length of leftover bytes from this block
	 * @throws SignedDocException
	 */
	private int calculateAndWriteBase64Block(OutputStream os,
			MessageDigest digest, byte[] b64leftover, int b64left, byte[] data,
			int dLen, boolean bLastBlock) throws SignedDocException {
		byte[] b64input = null;
		int b64Used, nLeft = 0, nInLen = 0;
		StringBuffer b64data = new StringBuffer();
//
//		if (m_logger.isDebugEnabled())
//			m_logger.debug("os: " + ((os != null) ? "Y" : "N") + " b64left: "
//					+ b64left + " input: " + dLen + " last: "
//					+ (bLastBlock ? "Y" : "N"));
		try {
			// use data from the last block
			if (b64left > 0) {
				if (dLen > 0) {
					b64input = new byte[dLen + b64left];
					nInLen = b64input.length;
					System.arraycopy(b64leftover, 0, b64input, 0, b64left);
					System.arraycopy(data, 0, b64input, b64left, dLen);
//					if (m_logger.isDebugEnabled())
//						m_logger.debug("use left: " + b64left
//								+ " from 0 and add " + dLen);
				} else {
					b64input = b64leftover;
					nInLen = b64left;
//					if (m_logger.isDebugEnabled())
//						m_logger.debug("use left: " + b64left
//								+ " with no new data");
				}
			} else {
				b64input = data;
				nInLen = dLen;
//				if (m_logger.isDebugEnabled())
//					m_logger.debug("use: " + nInLen + " from 0");
			}
			// encode full rows
			b64Used = Base64Util.encodeToBlock(b64input, nInLen, b64data,
					bLastBlock);
			nLeft = nInLen - b64Used;
			// use the encoded data
			byte[] encdata = b64data.toString().getBytes();
			if (os != null)
				os.write(encdata);
			digest.update(encdata);
			// now copy not encoded data back to buffer
//			if (m_logger.isDebugEnabled())
//				m_logger.debug("Leaving: " + nLeft + " of: " + b64input.length);
			if (nLeft > 0)
				System.arraycopy(b64input, b64input.length - nLeft,
						b64leftover, 0, nLeft);
		} catch (Exception ex) {
			SignedDocException
					.handleException(ex, SignedDocException.ERR_READ_FILE);
		}
//		if (m_logger.isDebugEnabled())
//			m_logger.debug("left: " + nLeft + " bytes for the next run");
		return nLeft;
	}
/*
	public void calculateFileSizeAndDigest(OutputStream os)
			throws SignedDocException {
		if (m_logger.isDebugEnabled())
			m_logger.debug("No xml output for ODF XADES data file: "
					+ getFileName());
	}
*/
	
	/**
	 * Calculates the DataFiles size and digest Since it calculates the digest
	 * of the external file then this is only useful for detatched files
	 * 
	 * @throws SignedDocException
	 *             for all errors
	 */
	public void calculateFileSizeAndDigest(InputStream is)
			throws SignedDocException {
//		if (m_logger.isDebugEnabled())
//			m_logger.debug("calculateFileSizeAndDigest(" + getId() + ")");
		boolean bUse64ByteLines = true;
		String use64Flag = ConfigManager.instance().getProperty(
				"DATAFILE_USE_64BYTE_LINES");
		if (use64Flag != null && use64Flag.equalsIgnoreCase("FALSE"))
			bUse64ByteLines = false;
		try {

			if (getContentType().equals(CONTENT_ODF_PKG_BINARY_ENTRY)
					&& getDigestValue() == null) {
				setDigestType(DIGEST_TYPE_SHA1);
				setDigest(calculateDetachedFileDigest(is));
			}
			if (getContentType().equals(CONTENT_ODF_PKG_XML_ENTRY)
					&& getDigestValue() == null) {
				setDigestType(DIGEST_TYPE_SHA1);

				MessageDigest sha = MessageDigest.getInstance("SHA-1");
				sha.update(canonicalizeXml(inputStreamToByteArray(is)));
				setDigest(sha.digest());
			}

		} catch (Exception ex) {
			SignedDocException
					.handleException(ex, SignedDocException.ERR_READ_FILE);
		}
	}

	public byte[] calculateDetachedFileDigest(InputStream is)
			throws SignedDocException {
		byte[] digest = null;

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			setSize(is.available());

			if (getSize() != 0) {

				byte[] buf = new byte[block_size]; // use 2KB bytes to avoid
													// base64 problems
				int fRead = 0;
				while ((fRead = is.read(buf)) == block_size) {
					sha.update(buf);
				}
				byte[] buf2 = new byte[fRead];
				System.arraycopy(buf, 0, buf2, 0, fRead);
				sha.update(buf2);

				digest = sha.digest();
			} else
				digest = sha.digest();
			// System.out.println("DataFile: \'" + getId() +
			// "\' digest: " + Base64Util.encode(digest));
		} catch (Exception ex) {
			SignedDocException
					.handleException(ex, SignedDocException.ERR_READ_FILE);
		}
		return digest;

	}

	public static byte[] inputStreamToByteArray(InputStream in)
			throws IOException {
		byte[] buffer = new byte[block_size];
		int length = 0;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		while ((length = in.read(buffer)) >= 0) {
			baos.write(buffer, 0, length);
		}

		return baos.toByteArray();
	}
	


}
