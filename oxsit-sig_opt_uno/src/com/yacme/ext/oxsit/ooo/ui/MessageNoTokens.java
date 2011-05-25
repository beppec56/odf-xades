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
public class MessageNoTokens extends DialogGeneralMessage {

	/**
	 * @param frame
	 * @param _axmcf
	 * @param _xcc
	 * 
	 * 
	 */
	public MessageNoTokens(XFrame frame, XMultiComponentFactory _axmcf,
			XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
	}

	public short executeDialogLocal(String _nomeDispositivo) {
		//read strings from resource
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
		String sTitle = "id_error_extension";
		String sFormatErr = "id_no_sign_cert_avlb";
		try {
			sTitle = m_aRegAcc.getStringFromRegistry( sTitle );
			sFormatErr = m_aRegAcc.getStringFromRegistry( sFormatErr );			
		} catch (Exception e) {
			m_aLogger.severe(e);
		}			
		m_aRegAcc.dispose();

		return super.executeDialog(sTitle, 
				String.format(sFormatErr, _nomeDispositivo),
				MessageBoxButtons.BUTTONS_OK , MessageBoxButtons.DEFAULT_BUTTON_OK, "errorbox");
	}

}
