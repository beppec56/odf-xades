/* ***** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/DocumentSigner_IT.java.
 *
 * The Initial Developer of the Original Code is
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.logging;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.GlobConstant;
import com.yacme.ext.oxsit.ooo.ui.DialogDisplayLog;

/**
 * class to wrap the UNO logger. Characterizes the class owner
 * Can be used only if the UNO context is known
 * All the logging rigamarole is carried out in an UNO singleton object.
 * The drawback is that the
 * 
 * Main logging switch turned on/off on a owner basis (eg the parent class).
 * 
 * 
 * @author beppec56
 *
 */
abstract class DynamicLoggerBase implements IDynamicLogger {
	protected String m_sOwnerClass;
	protected String m_sOwnerClassHashHex;
	protected XOX_Logger m_xLogger;
	protected boolean	m_bLogEnabled = false;
	protected boolean 	m_bInfoEnabled = true;
	protected boolean 	m_bWarningEnabled = true;
	
	private boolean 	m_bDebugEnabled = true;

	protected XComponentContext m_xCC;
	protected XMultiComponentFactory m_xMCF;

	/**
	 * Class for logger.
	 * The link point between the user class (e.g. the one that uses a logger) and
	 * the UNO singleton which exposes it.
	 * 
	 * Default starts disabled.
	 * Must be enabled when needed!
	 * @param _theOwner parent object
	 * @param _ctx the UNO context
	 */
	public DynamicLoggerBase(Object _theOwner, XComponentContext _ctx) {
		m_xCC = _ctx;
		if(_ctx != null ) {
			m_xMCF = _ctx.getServiceManager();
			m_xLogger = (XOX_Logger)UnoRuntime.queryInterface(XOX_Logger.class, 
					_ctx.getValueByName(GlobConstant.m_sSINGLETON_LOGGER_SERVICE_INSTANCE));
			if(m_xLogger == null) {
				System.out.println("no main logger!");
				//FIXME prepare a default local logger, using the standard one of Java, no file, only console
				//use it instead of the singleton one
			}
		}
		m_sOwnerClassHashHex = String.format( "%8H", _theOwner );
		m_sOwnerClass =  _theOwner.getClass().getName();
		//FIXME enable levels according to configuration ?
	}

	/**
	 * outputs a log with the class hex hash+class full name + "<init>"
	 * @param _theMethod
	 */
	public void ctor() {
		if(m_bLogEnabled /*&& m_bDebugEnabled */)
			m_xLogger.logp(GlobConstant.m_nLOG_CONFIG, m_sOwnerClassHashHex, m_sOwnerClass, "<init>");
	}

	/**
	 * outputs a log with the class hex hash+class full name + "<init>" + a message
	 * @param _theMethod
	 */
	public void ctor(String _message) {
		if(m_bLogEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_CONFIG, m_sOwnerClassHashHex, m_sOwnerClass, "<init> "+_message);
	}

	/**
	 * outputs a log with the class hex hash + one message
	 * @param the message to output
	 */
	public void debug(String _message) {
		if(m_bLogEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_DEBUG, m_sOwnerClassHashHex, "", _message);
	}

	/**
	 * outputs a log with the class hex hash + one message
	 * @param _theMethod
	 */
	public void log(String _message) {
		if(m_bLogEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_ALWAYS, m_sOwnerClassHashHex, "", _message);
	}

	public void log(Throwable e, boolean _useDialog) {
		log_exception(GlobConstant.m_nLOG_ALWAYS, "", "", e, _useDialog);
	}

	public void entering(String _theMethod) {
		if(m_bLogEnabled && m_bDebugEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_DEBUG, m_sOwnerClassHashHex, "entering "+_theMethod, "");
	}

	public void entering(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bDebugEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_DEBUG, m_sOwnerClassHashHex, "entering "+_theMethod, _message);
	}

	public void exiting(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bDebugEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_DEBUG, m_sOwnerClassHashHex, "exiting " + _theMethod, _message);
	}

	/**
	 * outputs a log with the class hex hash + two message
	 * @param _theMethod
	 */
	public void debug(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bDebugEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_DEBUG, m_sOwnerClassHashHex, _theMethod, _message);
	}

	/**
	 * outputs a log with the class hex hash + two message
	 * @param _theMethod
	 */
	public void log(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, _theMethod, _message);
	}

	/**
	 * outputs a log with the class hex hash + one message
	 * @param _theMethod
	 */
	public void info(String _theMethod) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex, _theMethod, "");
	}

	/**
	 * outputs a log with the class hex hash+class full name + two messages
	 * @param _theMethod
	 */
	public void info(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bInfoEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_INFO, m_sOwnerClassHashHex+" "+ m_sOwnerClass, _theMethod, _message);
	}

	public void warning(String _theMethod) {
		if(m_bLogEnabled && m_bWarningEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_WARNING,  m_sOwnerClassHashHex, _theMethod, "");
	}

	public void warning(String _theMethod, String _message) {
		if(m_bLogEnabled && m_bWarningEnabled)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_WARNING, m_sOwnerClassHashHex+" "+ m_sOwnerClass, _theMethod, _message);
	}

	public abstract void warning(String _theMethod, String _message, java.lang.Throwable ex);

	public abstract void severe(String _theMethod, String _message);

	public abstract void severe(java.lang.Throwable ex);

	public abstract void severe(String _theMethod, String _message, java.lang.Throwable ex);

	public abstract void severe(String _theMethod, java.lang.Throwable ex);

	public static String getStackFromException(java.lang.Throwable ex) {
		String term = System.getProperty("line.separator");
		String stack = term+ex.toString()+" ";

		StackTraceElement[] ste = ex.getStackTrace();
		if(ste != null)
			for(int i = 0; i < ste.length; i++)
				stack = stack+term+"\t"+ste[i].toString()+" ";
		return stack;
	}

	public void log_exception(int n_TheLevel, String _theMethod, String _message, java.lang.Throwable ex, boolean usedialog) {
		if(m_xLogger != null)
			m_xLogger.logp(GlobConstant.m_nLOG_LEVEL_SEVERE, m_sOwnerClassHashHex+" "+m_sOwnerClass,
					_theMethod +" "+_message,
					DynamicLoggerBase.getStackFromException(ex));

		if(m_xCC != null && usedialog && n_TheLevel == GlobConstant.m_nLOG_LEVEL_SEVERE) {
			try {
			//Use the dialog
				String _mex2 = "";
				if(_message.length() >0) {
					String term = System.getProperty("line.separator");
					_mex2 = term+term+_message+term; //
				}
				
				String theMex = m_sOwnerClassHashHex+" "+m_sOwnerClass+" "+_theMethod +
									" "+_mex2+
									DynamicLoggerBase.getStackFromException(ex);			
				DialogDisplayLog dlg = new DialogDisplayLog(null,m_xCC,m_xMCF,theMex);
				dlg.initialize( 0, 0);
				dlg.executeDialog();
			} catch (java.lang.Exception e) {
				e.printStackTrace();
				warning("error showing dialog");
			}
		}
	}

//enable/disable, set level
	/**
	 * disable logging completely, severe level as well
	 */
	public void disableLogging() {
		m_bLogEnabled = false;
	}
	
	/**
	 * enable all logging
	 */
	public void enableLogging() {
		if(m_xCC != null && m_xLogger != null)
			m_bLogEnabled = true;
	}
	
	/**
	 * enable INFO level only
	 */
	public void enableInfo() {
		m_bInfoEnabled = true;
	}

	/**
	 * disable INFO level only
	 */
	public void disableInfo() {
		m_bInfoEnabled = false;		
	}

	/**
	 * enable WARNING level only
	 */
	public void enableWarning() {
		m_bWarningEnabled = true;
	}

	/**
	 * disable WARNING level only
	 */
	public void disableWarning() {
		m_bWarningEnabled = false;
		
	}

	/**
	 * @param m_bDebugEnabled the m_bDebugEnabled to set
	 */
	public void enableDebug(boolean _bEnable) {
		this.m_bDebugEnabled = _bEnable;
	}

	/**
	 * @return the m_bDebugEnabled
	 */
	public boolean isDebugEnabled() {
		return m_bDebugEnabled;
	}

	/**
	 * this method is modal: it disables the logging
	 * for ALL the entire extension.
	 */
	public	void stopLogging() {
		m_xLogger.stopLogging();
	}
}
