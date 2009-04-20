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

package it.plio.ext.oxsit;

import it.plio.ext.oxsit.comp.GlobalLogger;
import it.plio.ext.oxsit.comp.SingletonGlobalVariables;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.InvalidRegistryException;
import com.sun.star.registry.XRegistryKey;

public class RegisterServices {
	/** Gives a factory for creating the service(s).
	 * This method is called by the <code>JavaLoader</code>
	 * <p>
	 * @return Returns a <code>XSingleServiceFactory</code> for creating the
	 * component.
	 * @see com.sun.star.comp.loader.JavaLoader#
	 * @param stringImplementationName The implementation name of the component.
	 * @param xmultiservicefactory The service manager, who gives access to every
	 * known service.
	 */
	public synchronized static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
		XSingleComponentFactory xFactory = null;
	
		if ( sImplementationName.equals( SingletonGlobalVariables.m_sImplementationName ) )
			xFactory = Factory.createComponentFactory( 
						SingletonGlobalVariables.class,
						SingletonGlobalVariables.m_sServiceNames );
		else if ( sImplementationName.equals( GlobalLogger.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( 
					GlobalLogger.class,
					GlobalLogger.m_sServiceNames );
//DEBUG	System.out.println("__getComponentFactory: "+GlobalLogger.m_sImplementationName);
		}

		return xFactory;
	}
	/** Writes the service information into the given registry key.
	 * This method is called by the <code>JavaLoader</code>.
	 * @return returns true if the operation succeeded
	 * @see com.sun.star.comp.loader.JavaLoader#
	 * @see com.sun.star.lib.uno.helper.Factory#
	 * @param xregistrykey Makes structural information (except regarding tree
	 * structures) of a single
	 * registry key accessible.
	 * The following console command shows the SINGLETONS:
./regview /opt/openoffice.org3/share/uno_packages/cache/registry/com.sun.star.comp.deployment.component.PackageRegistryBackend/common_.rdb / | grep -A5 -B7 SINGLETONS
	 */
	public synchronized static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
		boolean retSingvar = false; 
		//prepare the new key path
		try {
			XRegistryKey newKey = xRegistryKey.createKey(
					SingletonGlobalVariables.m_sImplementationName+ // the class implementing
					"/UNO/SINGLETONS/"+	//fixed key reference
					SingletonGlobalVariables.m_sServiceNames[0]); //

			newKey.setStringValue(SingletonGlobalVariables.m_sServiceNames[0]);
			retSingvar = Factory.writeRegistryServiceInfo( SingletonGlobalVariables.m_sImplementationName, 
					SingletonGlobalVariables.m_sServiceNames, xRegistryKey );
		} catch (InvalidRegistryException e) {
			System.out.println("__writeRegistryServiceInfo: "+SingletonGlobalVariables.m_sImplementationName + "failed");		
			e.printStackTrace();
		}

		boolean retGLogg = false; 
		//prepare the new key path
		try {
			XRegistryKey newKey = xRegistryKey.createKey(
					GlobalLogger.m_sImplementationName+ // the class implementing
					"/UNO/SINGLETONS/"+	//fixed key reference
					GlobalLogger.m_sServiceNames[0]); //

			newKey.setStringValue(GlobalLogger.m_sServiceNames[0]);
			retGLogg = Factory.writeRegistryServiceInfo( GlobalLogger.m_sImplementationName, 
					GlobalLogger.m_sServiceNames, xRegistryKey );
		} catch (InvalidRegistryException e) {
			System.out.println("__writeRegistryServiceInfo: "+GlobalLogger.m_sImplementationName + "failed");		
			e.printStackTrace();
		}
		return (retSingvar && retGLogg);
	}
}
