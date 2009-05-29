/**
 * 
 */
package it.plio.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class MessageNoSSCDReaders extends DialogGeneralMessage {

	/**
	 * @param frame
	 * @param _axmcf
	 * @param _xcc
	 */
	public MessageNoSSCDReaders(XFrame frame, XMultiComponentFactory _axmcf,
			XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
		// TODO Auto-generated constructor stub
	}
	
	public short executeDialogLocal() {
		//read strings from resource
		MessageConfigurationAccess m_aRegAcc = null;
		m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
		String sTitle = "id_error_extension";
		String sFormatErr = "id_no_sscd_readers";
		try {
			sTitle = m_aRegAcc.getStringFromRegistry( sTitle );
			sFormatErr = m_aRegAcc.getStringFromRegistry( sFormatErr );			
		} catch (Exception e) {
			m_aLogger.severe(e);
		}			
		m_aRegAcc.dispose();

		return super.executeDialog(sTitle, 
				sFormatErr,
				MessageBoxButtons.BUTTONS_OK , MessageBoxButtons.DEFAULT_BUTTON_OK, "errorbox");
	}
}
