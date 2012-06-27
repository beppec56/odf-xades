/**
 * 
 */
package com.yacme.ext.oxsit.signature.dispatchers;

import com.sun.star.document.EventObject;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.dispatchers.threads.ImplDispatchAsynch;

/**
 * This class implements the dispatcher to create a temporary copy of the signed file, 
 * displaying the signatures inside it.<p>
 * Note that the temporary file will not have the signatures inside, it's meant
 * for printing and display purposes only.
 * 
 * @author Giuseppe Castagno
 *
 */
public class ImplDisplaySignedFileDispatch extends ImplDispatchAsynch implements
com.sun.star.document.XEventListener {

	/**
	 * Constructor called from {@link com.yacme.ext.oxsit.comp.DispatchIntercept#queryDispatch} 
	 *  
	 * @param xFrame
	 * @param xContext
	 * @param xMCF
	 * @param unoSaveSlaveDispatch
	 */
	public ImplDisplaySignedFileDispatch(XFrame xFrame, XComponentContext xContext, XMultiComponentFactory xMCF,
			XDispatch unoSaveSlaveDispatch) {
		super(xFrame, xContext, xMCF, unoSaveSlaveDispatch);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
	}

	/* (non-Javadoc)
	 * @see com.sun.star.document.XEventListener#notifyEvent(com.sun.star.document.EventObject)
	 */
	@Override
	public void notifyEvent(EventObject arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	@Override
	public void disposing(com.sun.star.lang.EventObject arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
