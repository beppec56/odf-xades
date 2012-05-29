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
public class MessageWrongCertificateSelected extends DialogGeneralMessage {

	public MessageWrongCertificateSelected(XFrame frame, XMultiComponentFactory _axmcf,
			XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
	}

	public short executeDialogLocal() {
		//read strings from resource
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
		String sTitle = "id_error_extension";
		String sCertifErr = "id_wrong_certificate_selected";
		try {
			sTitle = m_aRegAcc.getStringFromRegistry( sTitle );
			sCertifErr = m_aRegAcc.getStringFromRegistry( sCertifErr );			
		} catch (Exception e) {
			m_aLogger.severe(e);
		}			
		m_aRegAcc.dispose();

		return super.executeDialog(sTitle, sCertifErr,
				MessageBoxButtons.BUTTONS_OK , MessageBoxButtons.DEFAULT_BUTTON_OK, "errorbox");
	}
}
