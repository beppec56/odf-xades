/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security;

import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier;
import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;

/** Verify a document signatures and the document
 * @author beppe
 *
 */
public class DocumentSignaturesVerifier_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
implements XServiceInfo, XComponent, XInitialization, XOX_DocumentSignaturesVerifier {

	protected IDynamicLogger m_aLogger;
	
	protected XComponentContext m_xCC;
	private XMultiComponentFactory m_xMCF;
	private XFrame m_xFrame;

	// the name of the class implementing this object
	public static final String m_sImplementationName = DocumentSignaturesVerifier_IT.class.getName();
	// the Object name, used to instantiate it inside the OOo API
	public static final String[] m_sServiceNames = { ConstantCustomIT.m_sDOCUMENT_VERIFIER_SERVICE };
	
	public DocumentSignaturesVerifier_IT (XComponentContext _ctx) {
		m_xCC = _ctx;
		m_xMCF = _ctx.getServiceManager();
		m_aLogger = new DynamicLogger(this, _ctx);
		m_aLogger = new DynamicLoggerDialog(this, _ctx);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
		
//		fillLocalizedStrings();
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getImplementationName()
	 */
	@Override
	public String getImplementationName() {
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;
		m_aLogger.info("supportsService", _sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals(m_sServiceNames[i]))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
	 */
	@Override
	public void initialize(Object[] _args) throws Exception {
		
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#getX509Certificates()
	 */
	@Override
	public XOX_X509Certificate[] getX509Certificates() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#removeDocumentSignature(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, int, java.lang.Object[])
	 */
	@Override
	public boolean removeDocumentSignature(XFrame _xFrame, 
					XModel _xDocumentModel, int _nCertificatePosition, Object[] args)
			throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.yacme.ext.oxsit.security.XOX_DocumentSignaturesVerifier#verifyDocumentSignatures(com.sun.star.frame.XFrame, com.sun.star.frame.XModel, java.lang.Object[])
	 */
	/*
	 * verifies the document signatures present.
	 * returns the document aggregated signature state
	 */
	@Override
	public int verifyDocumentSignatures(XFrame _xFrame, XModel _xDocumentModel, Object[] args) 
			throws IllegalArgumentException, Exception {
// FIXME should return the status of the signatures, may be the state of the aggregate document signatures should be implemented as uno type
		m_aLogger.log("verifyDocumentSignatures called");
		return 0;
	}
}
