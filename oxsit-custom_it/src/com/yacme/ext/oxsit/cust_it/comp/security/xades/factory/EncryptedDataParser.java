/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import java.io.InputStream;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.EncryptedData;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;

/**
 * Interface for reading encrypted files
 * @author  Veiko Sinivee
 * @version 1.0
 */
public interface EncryptedDataParser 
{
	/** 
	 * initializes the implementation class 
	 */
	public void init()
		throws SignedDocException;

	/**
	 * Reads in a EncryptedData file
	 * @param fileName file name
	 * @return EncryptedData document object if successfully parsed
	 */
	public EncryptedData readEncryptedData(String fileName) 
		throws SignedDocException;

	/**
	 * Reads in a EncryptedData file (.cdoc)
	 * @param dencStream opened stream with EncrypyedData data
	 * The user must open and close it. 
	 * @return EncryptedData object if successfully parsed
	 */
	public EncryptedData readEncryptedData(InputStream dencStream) 
		throws SignedDocException;

}
