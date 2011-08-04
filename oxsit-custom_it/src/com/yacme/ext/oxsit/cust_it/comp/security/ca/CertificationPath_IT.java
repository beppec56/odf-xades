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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/ca/CertificationPath_IT.java.
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

package com.yacme.ext.oxsit.cust_it.comp.security.ca;

import com.yacme.ext.oxsit.XOX_SingletonDataAccess;
import com.yacme.ext.oxsit.security.cert.CertificateState;
import com.yacme.ext.oxsit.security.cert.CertificationAuthorityState;
import com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.IDynamicLogger;

/**
 *  This service implements the CertificationPath_IT service, used to check the
 *  certificate for compliance to Italian law.
 *  
 *  
 * @author beppec56
 *
 */
public class CertificationPath_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo,
			XInitialization,
			XOX_CertificationPathProcedure
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= CertificationPath_IT.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { ConstantCustomIT.m_sCERTIFICATION_PATH_SERVICE_IT };
	
	private XComponentContext	m_xCC;
	private XMultiComponentFactory m_xMCF;

	private IDynamicLogger m_aLogger;

	private XOX_X509Certificate m_xQc;
	
	private CertificateState m_aCertificateState;
    private java.security.cert.X509Certificate m_JavaCert = null;

    private XOX_CertificationPathProcedure m_axoxChildProc;

	private XFrame m_xFrame;

	private CertificationAuthorityState m_aLastCAState;
	private	XComponent		m_xaCertificationAutorities;

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public CertificationPath_IT(XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = m_xCC.getServiceManager();
		m_aLogger = new DynamicLogger(this, m_xCC);
//		m_aLogger = new DynamicLazyLogger();
//		m_aLogger.enableLogging();
    	m_aLogger.ctor();
	}

	@Override
	public String getImplementationName() {
		m_aLogger.entering("getImplementationName");
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		m_aLogger.debug("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.debug("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	/** Function used internally when instantiating the object
	 * 
	 * Called using:
	 *
	 *		Object[] aArguments = new Object[4];
	 *
	 *		aArguments[0] = new String(OID);
	 *
	 *		aArguments[1] = new String("TheName"));
	 *		aArguments[2] = new String("The Value");
	 *		aArguments[3] = new Boolean(true or false);
	 * 
	 * Object aExt = m_xMCF.createInstanceWithArgumentsAndContext(
	 *				"com.yacme.ext.oxsit.security.cert.CertificateExtension", aArguments, m_xContext);
	 *
	 * @param _eValue array of ? object:
	 * <p>_eValue[0] string ??</p>
	 * 
	 * (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 *  
	 */
	@Override
	public void initialize(Object[] _eValue) throws Exception {
		//the eValue is the byte stream of the extension
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		super.addEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		m_aLogger.entering("dispose");
//		super.dispose();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		super.removeEventListener(arg0);
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure#configureOptions(com.sun.star.frame.XFrame, com.sun.star.uno.XComponentContext)
	 */
	@Override
	public void configureOptions(XFrame arg0, XComponentContext arg1) {
		// not used in this component
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure#initializeProcedure(com.sun.star.frame.XFrame)
	 */
	@Override
	public void initializeProcedure(XFrame arg0) {
		// not used in this component		
	}
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure#getCertificationAuthorityState()
	 */
	@Override
	public CertificationAuthorityState getCertificationAuthorityState() {
		//check if already done
		try {
			if(checkSubComponent())
				if(m_axoxChildProc != null) {
					return m_axoxChildProc.getCertificationAuthorityState();
				}
				else
					m_aLogger.debug("CANNOT execute child");
		} catch (ClassCastException e) {
			m_aLogger.severe(e);
		} catch (Throwable e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	private boolean checkSubComponent() throws 
			ClassCastException, Exception {
		try {
			m_axoxChildProc = null;
			XOX_SingletonDataAccess xSingletonDataAccess = Helpers.getSingletonDataAccess(m_xCC);

			try {
				XComponent xComp = xSingletonDataAccess.getUNOComponent(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT);					
				//yes, grab it and set our component internally
				m_aLogger.debug("Cache found!");
				m_axoxChildProc = 
					(XOX_CertificationPathProcedure)
					UnoRuntime.queryInterface(
							XOX_CertificationPathProcedure.class, xComp);
				return true;
			} catch (NoSuchElementException ex ) {
				//no, instantiate it and add to the singleton 
				m_aLogger.debug("Cache NOT found!");
				//create the object
				Object oCertPath = m_xMCF.createInstanceWithContext(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT, m_xCC);
				//add it the singleton
				//now use it
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, oCertPath); 
				if(xComp != null) {
					xSingletonDataAccess.addUNOComponent(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT, xComp);
					m_axoxChildProc = (XOX_CertificationPathProcedure)UnoRuntime.queryInterface(XOX_CertificationPathProcedure.class, xComp);
					return true;
				}
				else
					throw (new IllegalArgumentException());
			} catch (IllegalArgumentException ex ) {
//				m_aLogger.severe(ex);
				throw (ex);
			}
		} catch (ClassCastException ex) {
//			m_aLogger.severe(ex);
			throw (ex);
		} catch (ServiceNotFoundException ex) {
//			m_aLogger.severe(ex);
			throw (ex);
		} catch (NoSuchMethodException ex) {
//			m_aLogger.severe(ex);
			throw (ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure#verifyCertificationPath(com.sun.star.lang.XComponent)
	 */
	@Override
	public CertificationAuthorityState verifyCertificationPath(XFrame _xFrame, Object arg0)
			throws IllegalArgumentException, Exception {
		m_aLogger.debug("verifyCertificationPath");
		//see if our singleton has the object
		// grab the certificate and save it
		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, arg0);
		if(m_xQc == null)
			throw (new IllegalArgumentException("XOX_CertificateComplianceControlProcedure#verifyCertificateCertificateCompliance wrong argument"));

		try {
			m_axoxChildProc = null;
			XOX_SingletonDataAccess xSingletonDataAccess = Helpers.getSingletonDataAccess(m_xCC);

			try {
				XComponent xComp = xSingletonDataAccess.getUNOComponent(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT);					
				//yes, grab it and set our component internally
				m_aLogger.debug("Cache found!");
				m_axoxChildProc = 
					(XOX_CertificationPathProcedure)
					UnoRuntime.queryInterface(
							XOX_CertificationPathProcedure.class, xComp);
			} catch (NoSuchElementException ex ) {
				//no, instantiate it and add to the singleton 
				m_aLogger.debug("Cache NOT found!");
				//create the object
				Object oCertPath = m_xMCF.createInstanceWithContext(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT, m_xCC);
				//add it the singleton
				//now use it
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, oCertPath); 
				if(xComp != null) {
					xSingletonDataAccess.addUNOComponent(ConstantCustomIT.m_sCERTIFICATION_PATH_CACHE_SERVICE_IT, xComp);
					m_axoxChildProc = (XOX_CertificationPathProcedure)UnoRuntime.queryInterface(XOX_CertificationPathProcedure.class, xComp);
				}
				else
					throw (new IllegalArgumentException());

			} catch (IllegalArgumentException ex ) {
				m_aLogger.severe(ex);
			} catch (Throwable ex ) { 
				m_aLogger.severe(ex);
			}

			if(m_axoxChildProc != null) {
				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, m_xQc); 
				if(xComp != null) {
					m_axoxChildProc.verifyCertificationPath(_xFrame,xComp);
					m_aLastCAState = m_axoxChildProc.getCertificationAuthorityState();
				}
				 return m_aLastCAState;
			}
			else
				m_aLogger.debug("CANNOT execute child");
			//instantiate the cache, init it and add it
		} catch (ClassCastException ex) {
			m_aLogger.severe(ex);
		} catch (ServiceNotFoundException ex) {
			m_aLogger.severe(ex);
		} catch (NoSuchMethodException ex) {
			m_aLogger.severe(ex);
		} catch (Throwable ex ) { 
			m_aLogger.severe(ex);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure#getCertificationAutorities()
	 */
	@Override
	public int getCertificationAuthoritiesNumber() {
		try {
			if(checkSubComponent())
				if(m_axoxChildProc != null) {
					return m_axoxChildProc.getCertificationAuthoritiesNumber();
				}
				else
					m_aLogger.debug("CANNOT execute child");
		} catch (ClassCastException e) {
			m_aLogger.severe(e);
		} catch (Throwable e) {
			m_aLogger.severe(e);
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.cert.XOX_CertificationPathProcedure#getCertificationAuthorities()
	 */
	@Override
	public XComponent[] getCertificationAuthorities(XFrame _aFrame) {
		//check if already done
		try {
			if(checkSubComponent())
				if(m_axoxChildProc != null) {
					return m_axoxChildProc.getCertificationAuthorities(_aFrame);
				}
				else
					m_aLogger.warning("CANNOT execute child");
		} catch (ClassCastException e) {
			m_aLogger.severe(e);
		} catch (Throwable e) {
			m_aLogger.severe(e);
		}
		return null;
	}
}
