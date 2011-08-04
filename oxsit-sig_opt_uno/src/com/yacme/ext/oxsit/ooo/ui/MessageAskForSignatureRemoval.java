/**
 * 
 */
package com.yacme.ext.oxsit.ooo.ui;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;

/**
 * @author beppe
 *
 */
public class MessageAskForSignatureRemoval extends DialogGeneralMessage {

	public MessageAskForSignatureRemoval(XFrame xFrame, XMultiComponentFactory axMCF, XComponentContext xCC) {
		super(xFrame, axMCF, xCC);
	}

	public short executeDialogLocal(String _signer, String _signatureDate) {
		//read strings from resource
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
		String sTitle = "id_message_signature_removal"; //Signature warning
		String sFormatErr = "id_the_signature_exists";	//A signature with the same certificate exists.
													//Overwrite former signature with the same certificate ?
													// Yes = owerwrite
													// No  = generate another signature
													// Cancel = do no sign and exit signature process
		try {
			sTitle = m_aRegAcc.getStringFromRegistry( sTitle );
			sFormatErr = m_aRegAcc.getStringFromRegistry( sFormatErr );			
		} catch (Exception e) {
			m_aLogger.severe(e);
		}			
		m_aRegAcc.dispose();

		return super.executeDialog(sTitle, 
				String.format(sFormatErr, _signer, _signatureDate),
				MessageBoxButtons.BUTTONS_YES_NO , MessageBoxButtons.DEFAULT_BUTTON_NO, "errorbox");
	}
}
