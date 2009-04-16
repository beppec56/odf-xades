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

import it.plio.ext.oxsit.singleton.SingletonVariables;

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
	
//		System.out.println("__getComponentFactory: "+ManageOptions.m_sImplementationName);
		if ( sImplementationName.equals( SingletonVariables.m_sImplementationName ) )
			xFactory = Factory.createComponentFactory( 
						SingletonVariables.class,
						SingletonVariables.m_sServiceNames );
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
//		System.out.println("__writeRegistryServiceInfo: "+ManageOptions.m_sImplementationName+" "+ManageOptions.m_sServiceNames[0] );
		
		//prepare thew key path
		try {
			XRegistryKey newKey = xRegistryKey.createKey(
					SingletonVariables.m_sImplementationName+ // the class implementing
					"/UNO/SINGLETONS/"+	//fixed key reference
					SingletonVariables.m_sServiceNames[0]); //

			newKey.setStringValue(SingletonVariables.m_sServiceNames[0]);
		} catch (InvalidRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Factory.writeRegistryServiceInfo( SingletonVariables.m_sImplementationName, SingletonVariables.m_sServiceNames, xRegistryKey );
	}
}
