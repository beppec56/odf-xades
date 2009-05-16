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
public class DynamicLoggerDialog /*extends DynamicLogger*/ implements IDynamicLogger {
	protected String m_sOwnerClass;
	protected String m_sOwnerClassHashHex;
	protected XOX_Logger m_xLogger;
	protected boolean	m_bLogEnabled = false;
	protected boolean 	m_bInfoEnabled = true;
	protected boolean 	m_bWarningEnabled = true;
	
	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_xMCF;

	/**
	 * @param owner
	 * @param _ctx
	 */
	public DynamicLoggerDialog(Object _theOwner, XComponentContext _ctx) {
//		super(owner, _ctx);
		//compute the parent class ID hex hash
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_xLogger = (XOX_Logger)UnoRuntime.queryInterface(XOX_Logger.class, 
				_ctx.getValueByName(GlobConstant.m_sSINGLETON_LOGGER_SERVICE_INSTANCE));
		if(m_xLogger == null)
			System.out.println("no main logger!");
		m_sOwnerClassHashHex = String.format( "%8H", _theOwner );
		m_sOwnerClass =  _theOwner.getClass().getName();		
	}

	@Override
	public void log_exception(int n_TheLevel, String _theMethod, String _message, java.lang.Exception ex) {
		String stack = "\n"+ex.toString();

		StackTraceElement[] ste = ex.getStackTrace();
		if(ste != null)
			for(int i = 0; i < ste.length; i++)
				stack = stack+"\n\t"+ste[i].toString();

		String theMex = m_sOwnerClassHashHex+" "+m_sOwnerClass+" "+_theMethod +" "+_message+" "+ex.getLocalizedMessage()+DynamicLogger.getStackFromException(ex);
		if(n_TheLevel == GlobConstant.m_nLOG_LEVEL_SEVERE) {
			//Use the dialog
			info("using dialog: "+theMex);
			DialogDisplayLog dlg = new DialogDisplayLog(/*m_xParentFrame*/ null,m_xCC,m_xMCF,theMex);
			int BiasX = (ControlDims.DLG_ABOUT_WIDTH-ControlDims.DLG_SHOW_LICENSE_WIDTH)/2;
			int BiasY = ControlDims.RSC_CD_PUSHBUTTON_HEIGHT;
			try {
//				dlg.initialize( BiasX, BiasY);
				dlg.initialize( 0, 0);
				dlg.executeDialog();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				warning("error showing dialog");
			}
		}
		m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_SEVERE, m_sOwnerClassHashHex+" "+m_sOwnerClass,
					_theMethod +" "+_message, 
					ex.getLocalizedMessage()+DynamicLogger.getStackFromException(ex));
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#ctor()
	 */
	@Override
	public void ctor() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#ctor(java.lang.String)
	 */
	@Override
	public void ctor(String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#disableInfo()
	 */
	@Override
	public void disableInfo() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#disableLogging()
	 */
	@Override
	public void disableLogging() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#disableWarning()
	 */
	@Override
	public void disableWarning() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#enableInfo()
	 */
	@Override
	public void enableInfo() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#enableLogging()
	 */
	@Override
	public void enableLogging() {
		// TODO Auto-generated method stub
		m_bLogEnabled = true;		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#enableWarning()
	 */
	@Override
	public void enableWarning() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#entering(java.lang.String)
	 */
	@Override
	public void entering(String method) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#entering(java.lang.String, java.lang.String)
	 */
	@Override
	public void entering(String method, String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#exiting(java.lang.String, java.lang.String)
	 */
	@Override
	public void exiting(String method, String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#info(java.lang.String)
	 */
	@Override
	public void info(String method) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#info(java.lang.String, java.lang.String)
	 */
	@Override
	public void info(String method, String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#log(java.lang.String)
	 */
	@Override
	public void log(String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#log(java.lang.String, java.lang.String)
	 */
	@Override
	public void log(String method, String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String)
	 */
	@Override
	public void severe(String method, String _message) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.Exception)
	 */
	public void severe(java.lang.Exception ex) {
		// TODO Auto-generated method stub
		log_exception(GlobConstant.m_nLOG_LEVEL_SEVERE, "", "", ex);		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String method, String _message, java.lang.Exception ex) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String method, java.lang.Exception ex) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#stopLogging()
	 */
	@Override
	public void stopLogging() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String)
	 */
	@Override
	public void warning(String method) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String)
	 */
	@Override
	public void warning(String method, String _message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void warning(String method, String _message, java.lang.Exception ex) {
		// TODO Auto-generated method stub
		
	}
}
