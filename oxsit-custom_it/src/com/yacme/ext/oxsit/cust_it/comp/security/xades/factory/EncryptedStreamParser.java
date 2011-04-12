/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.io.InputStream;
import java.io.OutputStream;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocumentException_IT;

/**
 * Interface for parsing large encrypted files
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface EncryptedStreamParser 
{
	/** 
	 * initializes the implementation class 
	 */
	public void init()
		throws SignedODFDocumentException_IT;

	/**
	 * Reads in a EncryptedData file (.cdoc)
	 * @param dencStream opened stream with EncrypyedData data
	 * The user must open and close it. 
	 * @param outs output stream for decrypted data
	 * @param token index of PKCS#11 token used
	 * @param pin pin code to decrypt transport key using PKCS#11
	 * @param recipientName Recipient atribute value of <EncryptedKey>
	 * used to locate the correct transport key to decrypt with
	 * @return number of bytes successfully decrypted
	 * @throws SignedODFDocumentException_IT for decryption errors
	 */
	public int decryptStreamUsingRecipientName(InputStream dencStream, 
			OutputStream outs, int token, String pin, String recipientName) 
		throws SignedODFDocumentException_IT;

	/**
	 * Reads in a EncryptedData file (.cdoc)
	 * @param dencStream opened stream with EncrypyedData data
	 * The user must open and close it. 
	 * @param outs output stream for decrypted data
	 * @param deckey decryption key
	 * @param recipientName Recipient atribute value of <EncryptedKey>
	 * used to locate the correct transport key to decrypt with
	 * @return number of bytes successfully decrypted
	 * @throws SignedODFDocumentException_IT for decryption errors
	 */
	public int decryptStreamUsingRecipientNameAndKey(InputStream dencStream, 
			OutputStream outs, byte[] deckey, String recipientName) 
		throws SignedODFDocumentException_IT;
}

