/**
 * 
 */
package it.plio.ext.oxsit.ooo.cert;

import com.sun.star.uno.XInterface;

/**
 * @author beppe
 *
 */
public interface XOXDocumentSignatures extends XInterface {
	
	public String DocumentURL = null;

	public abstract String getDocumentURL();

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.cert.XOXDocumentSignatures#setDocumentURL(java.lang.String)
	 */
	public abstract void setDocumentURL(String arg0);
}
