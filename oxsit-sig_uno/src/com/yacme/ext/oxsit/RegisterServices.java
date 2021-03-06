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

package com.yacme.ext.oxsit;


import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.XRegistryKey;
import com.yacme.ext.oxsit.comp.DispatchIntercept;
import com.yacme.ext.oxsit.comp.SignatureHandler;

public class RegisterServices {
	/**
	 * Gives a factory for creating the service(s). This method is called by the
	 * <code>JavaLoader</code>
	 * <p>
	 * 
	 * @return Returns a <code>XSingleServiceFactory</code> for creating the
	 *         component.
	 * @see com.sun.star.comp.loader.JavaLoader#
	 * @param stringImplementationName
	 *            The implementation name of the component.
	 * @param xmultiservicefactory
	 *            The service manager, who gives access to every known service.
	 */
	public synchronized static XSingleComponentFactory __getComponentFactory(
			String sImplementationName) {
		XSingleComponentFactory xFactory = null;
		if (sImplementationName.equals(	SignatureHandler.m_sImplementationName))
			xFactory = Factory.createComponentFactory(SignatureHandler.class,
					SignatureHandler.m_sServiceNames);
//DEBUG		System.out.println("__getComponentFactory: "+X509Certificate.m_sImplementationName);
		else if ( sImplementationName.equals( DispatchIntercept.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( 
					DispatchIntercept.class,
					DispatchIntercept.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+DispatchIntercept.m_sImplementationName);
		}

		return xFactory;
	}

	/**
	 * Writes the service information into the given registry key. This method
	 * is called by the <code>JavaLoader</code>.
	 * 
	 * @return returns true if the operation succeeded
	 * @see com.sun.star.comp.loader.JavaLoader#
	 * @see com.sun.star.lib.uno.helper.Factory#
	 * @param xregistrykey
	 *            Makes structural information (except regarding tree
	 *            structures) of a single registry key accessible.
	 */
	public synchronized static boolean __writeRegistryServiceInfo(
			XRegistryKey xRegistryKey) {
		boolean regSignature =  Factory.writeRegistryServiceInfo(
				SignatureHandler.m_sImplementationName,  // the class implementing the service
				SignatureHandler.m_sServiceNames, // the names of the implemented services
				xRegistryKey);
		
		boolean regSDispatcher =  Factory.writeRegistryServiceInfo(
				DispatchIntercept.m_sImplementationName,  // the class implementing the service
				DispatchIntercept.m_sServiceNames, // the names of the implemented services
				xRegistryKey);
		
		return regSignature && regSDispatcher;
	}
}
