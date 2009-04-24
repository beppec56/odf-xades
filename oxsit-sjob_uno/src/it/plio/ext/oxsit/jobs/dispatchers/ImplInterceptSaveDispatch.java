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

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.comp.GlobConstantJobs;
import it.plio.ext.oxsit.dispatchers.ImplDispatchAsynch;
import it.plio.ext.oxsit.dispatchers.threads.IDispatchImplementer;
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

/** implement the intercepted Save dispatch
 * @author beppe
 *
 */
public class ImplInterceptSaveDispatch extends ImplDispatchAsynch implements XDispatch, IDispatchImplementer {

	public ImplInterceptSaveDispatch(XFrame xFrame, XComponentContext xContext,
			XMultiComponentFactory xMCF, XDispatch unoSaveSlaveDispatch) {

		super(xFrame, xContext, xMCF, unoSaveSlaveDispatch);
		m_logger.enableLogging();
	}

	public void impl_dispatch(URL aURL, PropertyValue[] lArguments) {

		m_logger.info("aURL "+aURL.Complete+" lArguments.length: "+lArguments.length);
		if(	lArguments.length > 0) {
//			m_logger.info(" lArguments.lenght: "+lArguments.length);
			for(int i = 0; i <lArguments.length; i++) {
				PropertyValue aValue = lArguments[i];
				
				m_logger.info("name: "+aValue.Name+" "+aValue.Value.toString());
			}
			m_logger.info("");
		}

		try {
//			check the slave one
			com.sun.star.util.URL[] aParseURL = new com.sun.star.util.URL[1];
			aParseURL[0] = new com.sun.star.util.URL();
			aParseURL[0].Complete = GlobConstant.m_sSIGN_PROTOCOL_BASE_URL+ GlobConstant.m_sBEFORE_SAVE_PATH;
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
			m_logger.info(aParseURL[0].Protocol+" "+aParseURL[0].Path);

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
						xNotifyingDispatcher.dispatchWithNotification(aParseURL[0], lProperties, null);
					else
						//trow exception: unimplemented interface !...
						xDispatcher.dispatch(aParseURL[0],lProperties);
//					then get from the Notify the value we need of the user answer.

				}
				else
					m_logger.info("No dispatcher for "+aParseURL[0]);
			}
			else
				m_logger.info("No provider for "+aParseURL[0]);

//			Dispatch the URL into the frame.
			super.impl_dispatch(aURL, lArguments);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
