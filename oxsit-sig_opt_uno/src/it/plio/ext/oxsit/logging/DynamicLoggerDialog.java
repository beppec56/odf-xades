/**
 * 
 */
package it.plio.ext.oxsit.logging;

import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.ui.ControlDims;
import it.plio.ext.oxsit.ooo.ui.DialogDisplayLog;
import it.plio.ext.oxsit.ooo.ui.DialogShowLicense;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * this class is the same as Dynamic logger, except that it will display a severe
 * log on a dialog box, display what happened in details
 * 
 * It should be instantiatiated only from and object in OOo that run in a UI environment,
 * for example a dialog box or a context from where a dialog can be called.
 * @author beppec56
 *
 */
public class DynamicLoggerDialog extends DynamicLoggerBase {
	/**
	 * @param owner
	 * @param _ctx
	 */
	public DynamicLoggerDialog(Object _theOwner, XComponentContext _ctx) {
		super(_theOwner, _ctx);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String)
	 */
	@Override
	public void severe(String _theMethod, String _message) {
		String theMex = m_sOwnerClassHashHex+" "+m_sOwnerClass+" "+_theMethod +" "+_message+" ";			
		DialogDisplayLog dlg = new DialogDisplayLog(null,m_xCC,m_xMCF,theMex);
		try {
			dlg.initialize( 0, 0);
			dlg.executeDialog();
		} catch (Exception e) {
			e.printStackTrace();
			warning("error showing dialog");
		}
		m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_SEVERE,  m_sOwnerClassHashHex+" "+m_sOwnerClass, _theMethod, _message);
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.Exception)
	 */
	public void severe(java.lang.Exception ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, "", "", ex,true);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String _theMethod, String _message, java.lang.Exception ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, _message, ex,true);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String _theMethod, java.lang.Exception ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, "", ex,true);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void warning(String method, String _message, java.lang.Exception ex) {
		if(m_bLogEnabled && m_bWarningEnabled)
			log_exception(GlobConstant.m_nLOG_LEVEL_WARNING, method, _message, ex,false);		
	}
}
