/**
 * 
 */
package com.yacme.ext.oxsit.logging;


import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.ControlDims;
import com.yacme.ext.oxsit.ooo.ui.DialogDisplayLog;
import com.yacme.ext.oxsit.ooo.ui.DialogShowLicense;

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
	 * @see it.plio.ext.logging.IDynamicLogger#severe(java.lang.String, java.lang.String)
	 */
	@Override
	public void severe(String _theMethod, String _message) {
		String _mex2 = "";
		if(_message.length() >0) {
			String term = System.getProperty("line.separator");
			_mex2 = term+term+_message+term; //
		}
		String theMex = m_sOwnerClassHashHex+" "+m_sOwnerClass+" "+_theMethod +" "+_mex2+" ";			
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
	 * @see it.plio.ext.logging.IDynamicLogger#severe(java.lang.Exception)
	 */
	public void severe(java.lang.Throwable ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, "", "", ex,true);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.logging.IDynamicLogger#severe(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String _theMethod, String _message, java.lang.Throwable ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, _message, ex,true);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.logging.IDynamicLogger#severe(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String _theMethod, java.lang.Throwable ex) {
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, _theMethod, "", ex,true);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.logging.IDynamicLogger#warning(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void warning(String method, String _message, java.lang.Throwable ex) {
		if(m_bLogEnabled && m_bWarningEnabled)
			log_exception(GlobConstant.m_nLOG_LEVEL_WARNING, method, _message, ex,false);		
	}
}
