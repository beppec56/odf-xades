/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) version 1.1
 *  as published by the European Community.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the EUPL.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  EUPL for more details.
 *
 *  You should have received a copy of the EUPL along with this
 *  program.  If not, see:
 *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
 *
 ************************************************************************/

package it.plio.ext.oxsit.jobs.dispatchers;

import it.plio.ext.oxsit.comp.GlobConstantJobs;
import it.plio.ext.oxsit.dispatchers.threads.IDispatchImplementer;
import it.plio.ext.oxsit.dispatchers.threads.ImplDispatchAsynch;
import it.plio.ext.oxsit.ooo.GlobConstant;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XURLTransformer;

/**
 * @author beppe
 *
 */
public class ImplInterceptSaveAsDispatch extends ImplDispatchAsynch implements
		XDispatch, IDispatchImplementer {
	public ImplInterceptSaveAsDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super( xFrame, xContext, xMCF, unoSaveSlaveDispatch);
	}

	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {
		// TODO Auto-generated method stub
//		we need to first call our internal method and then call the original one
		m_aLogger.info(" lArguments.length: "+lArguments.length);
		
/** check if a CNIPA signature is present, if yes, call a message box to
 * ask the user confirmation before saving (need to check for a better method, actually
 */
		
		try {
//			check the slave one
			com.sun.star.util.URL[] aParseURL = new com.sun.star.util.URL[1];
			aParseURL[0] = new com.sun.star.util.URL();
			aParseURL[0].Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL+
													GlobConstant.m_sBEFORE_SAVE_AS_PATH;
			com.sun.star.beans.PropertyValue[] lProperties = new com.sun.star.beans.PropertyValue[1];

			com.sun.star.frame.XDispatchProvider xProvider =
				(com.sun.star.frame.XDispatchProvider)UnoRuntime.queryInterface(
						com.sun.star.frame.XDispatchProvider.class, m_xFrame);
//			need an URLTransformer
			Object obj;
			obj = m_axMCF.createInstanceWithContext("com.sun.star.util.URLTransformer", m_xCC);
			XURLTransformer xTransformer = (XURLTransformer)UnoRuntime.queryInterface(
					XURLTransformer.class, obj);
			xTransformer.parseStrict( aParseURL );
			m_aLogger.info(aParseURL[0].Protocol+" "+aParseURL[0].Path);

//			Ask it for right dispatch object for our URL.
//			Force given frame as target for following dispatch by using "",
//			it's the same as "_self".
			if( xProvider != null ) {
				com.sun.star.frame.XDispatch xDispatcher = null;
				xDispatcher = xProvider.queryDispatch(aParseURL[0],"",0);

				// Dispatch the URL into the frame.
				if(xDispatcher != null) {
					com.sun.star.frame.XNotifyingDispatch xNotifyingDispatcher = 
						(com.sun.star.frame.XNotifyingDispatch)UnoRuntime.queryInterface(
								com.sun.star.frame.XNotifyingDispatch.class,xDispatcher);
					if( xNotifyingDispatcher != null )
						try {
							xNotifyingDispatcher.dispatchWithNotification(aParseURL[0], lProperties, null);
						} catch (com.sun.star.uno.RuntimeException e) {
							//trow exception: unimplemented interface !...
							xDispatcher.dispatch(aParseURL[0],lProperties);
							//					then get from the Notify the value we need of the user answer.
						}
						else
							xDispatcher.dispatch(aParseURL[0],lProperties);					
				}
				else
					m_aLogger.info("NO dispatcher for"+aParseURL[0].Complete);
			}
			else
				m_aLogger.info("NO provider for "+aParseURL[0].Complete);

//			Dispatch the URL into the frame.
			super.impl_dispatch(aURL, lArguments);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
