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
public class MessageError extends DialogGeneralMessage {

	public static String	m_sTitle;
	
	/**
	 * @param frame
	 * @param _axmcf
	 * @param _xcc
	 */
	public MessageError(XFrame frame, XMultiComponentFactory _axmcf,
			XComponentContext _xcc) {
		super(frame, _axmcf, _xcc);
	}
	
	public short executeDialogLocal(String _theError) {
		//read strings from resource
		if(m_sTitle == null) {
			MessageConfigurationAccess m_aRegAcc = null;
			m_aRegAcc = new MessageConfigurationAccess( m_xCC, m_axMCF );		
			try {
				m_sTitle = m_aRegAcc.getStringFromRegistry( "id_error_extension" );
			} catch (Exception e) {
				m_aLogger.severe(e);
			}			
			m_aRegAcc.dispose();
		}

		return super.executeDialog((m_sTitle == null) ? "Error! ": m_sTitle, 
				_theError,
				MessageBoxButtons.BUTTONS_OK ,
				MessageBoxButtons.DEFAULT_BUTTON_OK,
				"errorbox");
	}
}
