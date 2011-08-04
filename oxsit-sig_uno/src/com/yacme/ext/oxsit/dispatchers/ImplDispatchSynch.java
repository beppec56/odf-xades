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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/AvailableSSCDs_IT.java.
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

package com.yacme.ext.oxsit.dispatchers;



import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XStatusListener;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.yacme.ext.oxsit.logging.DynamicLogger;

/** superclass of all dispatchers
 * @author beppe
 *
 */
public class ImplDispatchSynch implements IDispatchBaseObject //XDispatch, XComponent 
{

	protected XFrame m_xFrame;
	protected XMultiComponentFactory m_axMCF;
	protected XComponentContext m_xCC;
	protected XDispatch m_aUnoSlaveDispatch = null;
	
	protected DynamicLogger		m_aLogger;

	public ImplDispatchSynch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		m_xFrame = xFrame;
		m_axMCF = xMCF;
		m_xCC = xContext;
		m_aUnoSlaveDispatch = unoSaveSlaveDispatch;
		m_aLogger = new DynamicLogger(this,xContext);
		m_aLogger.ctor();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#addStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void addStatusListener(XStatusListener aListener, URL aURL) {
		m_aLogger.debug("addStatusListener",aURL.Complete);		
		if(m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.addStatusListener(aListener, aURL);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#dispatch(com.sun.star.util.URL, com.sun.star.beans.PropertyValue[])
	 * 
	 */
	/** important: the derived class should implement itself the XNotifyingDispatch behavior 
	 * 
	 */
	public void dispatch(URL aURL, PropertyValue[] lArguments) {
		m_aLogger.debug("dispatch (ImplDispatchSynch)  "+aURL.Complete);		
		if(m_aUnoSlaveDispatch!=null)
			m_aUnoSlaveDispatch.dispatch(aURL,lArguments);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.frame.XDispatch#removeStatusListener(com.sun.star.frame.XStatusListener, com.sun.star.util.URL)
	 */
	public void removeStatusListener(XStatusListener aListener, URL aURL) {
//		m_aLoggerDialog.log("removeStatusListener",aURL.Complete);		
		if(m_aUnoSlaveDispatch != null)
			m_aUnoSlaveDispatch.removeStatusListener(aListener, aURL);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {		
		m_aLogger.severe("addEventListener", "implements it in subclass!");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		m_aLogger.severe("dispose", "implements it in subclass!");
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		m_aLogger.severe("removeEventListener", "implements it in subclass!");		
	}	
}
