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
import com.yacme.ext.oxsit.comp.options.ManageGeneralOptions;
import com.yacme.ext.oxsit.comp.options.ManageLoggingOptions;
import com.yacme.ext.oxsit.comp.options.ManageSSCDOptions;
import com.yacme.ext.oxsit.comp.security.DocumentSignatures;
import com.yacme.ext.oxsit.comp.security.cert.CertificateExtension;
import com.yacme.ext.oxsit.comp.security.cert.X509Certificate;

public class RegisterServices {
	/** Gives a factory for creating the service(s).
	 * This method is called by the <code>JavaLoader</code>
	 * <p>
	 * @return Returns a <code>XSingleServiceFactory</code> for creating the
	 * component.
	 * @see com.sun.star.comp.loader.JavaLoader
	 * @param stringImplementationName The implementation name of the component.
	 * @param xmultiservicefactory The service manager, who gives access to every
	 * known service.
	 */
	public synchronized static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
		XSingleComponentFactory xFactory = null;
	
		if ( sImplementationName.equals( ManageGeneralOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageGeneralOptions.class, ManageGeneralOptions.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+ManageGeneralOptions.m_sImplementationName);
		}
		else if ( sImplementationName.equals( ManageLoggingOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageLoggingOptions.class, ManageLoggingOptions.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+ManageLoggingOptions.m_sImplementationName);
		}
		else if ( sImplementationName.equals( ManageSSCDOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageSSCDOptions.class, ManageSSCDOptions.m_sServiceNames );
//DEBUG	System.out.println("__getComponentFactory: "+ManageSSCDOptions.m_sImplementationName);
		}
		else if ( sImplementationName.equals( DocumentSignatures.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( DocumentSignatures.class, DocumentSignatures.m_sServiceNames );
//DEBUG	System.out.println("__getComponentFactory: "+DocumentSignatures.m_sImplementationName);
		}
		else if ( sImplementationName.equals( CertificateExtension.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( CertificateExtension.class, CertificateExtension.m_sServiceNames );
//DEBUG	System.out.println("__getComponentFactory: "+CertificateExtension.m_sImplementationName);
		}
		else if ( sImplementationName.equals( X509Certificate.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( X509Certificate.class, X509Certificate.m_sServiceNames );
//DEBUG	System.out.println("__getComponentFactory: "+X509Certificate.m_sImplementationName);
		}
		return xFactory;
	}

	/** Writes the service information into the given registry key.
	 * This method is called by the <code>JavaLoader</code>.
	 * @return returns true if the operation succeeded
	 * @see com.sun.star.comp.loader.JavaLoader
	 * @see com.sun.star.lib.uno.helper.Factory
	 * @param xregistrykey Makes structural information (except regarding tree
	 * structures) of a single
	 * registry key accessible.
	 */
	public synchronized static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
		boolean retGeneral = 
			Factory.writeRegistryServiceInfo( ManageGeneralOptions.m_sImplementationName, ManageGeneralOptions.m_sServiceNames, xRegistryKey );

//		System.out.println("__writeRegistryServiceInfo: "+ManageGeneralOptions.m_sImplementationName);

		boolean retLogging = 
			Factory.writeRegistryServiceInfo( ManageLoggingOptions.m_sImplementationName, ManageLoggingOptions.m_sServiceNames, xRegistryKey );

//		System.out.println("__writeRegistryServiceInfo: "+ManageLoggingOptions.m_sImplementationName);
		
		boolean retSSCDOpts = 
			Factory.writeRegistryServiceInfo( ManageSSCDOptions.m_sImplementationName, ManageSSCDOptions.m_sServiceNames, xRegistryKey );

		boolean retDocumSignatures = 
			Factory.writeRegistryServiceInfo( DocumentSignatures.m_sImplementationName, DocumentSignatures.m_sServiceNames, xRegistryKey );

		boolean retCertifExt = 
			Factory.writeRegistryServiceInfo( CertificateExtension.m_sImplementationName, CertificateExtension.m_sServiceNames, xRegistryKey );

		boolean retX509Certif = 
			Factory.writeRegistryServiceInfo( X509Certificate.m_sImplementationName, X509Certificate.m_sServiceNames, xRegistryKey );

		if (!retGeneral)
			System.out.println("__writeRegistryServiceInfo: "+ManageGeneralOptions.m_sImplementationName + "failed");		

		if (!retLogging)
			System.out.println("__writeRegistryServiceInfo: "+ManageLoggingOptions.m_sImplementationName + "failed");		
		
		if (!retSSCDOpts)
			System.out.println("__writeRegistryServiceInfo: "+ManageSSCDOptions.m_sImplementationName + "failed");		
		
		if (!retDocumSignatures)
			System.out.println("__writeRegistryServiceInfo: "+DocumentSignatures.m_sImplementationName + "failed");		

		if (!retCertifExt)
			System.out.println("__writeRegistryServiceInfo: "+CertificateExtension.m_sImplementationName + "failed");			

		if (!retX509Certif)
			System.out.println("__writeRegistryServiceInfo: "+X509Certificate.m_sImplementationName + "failed");		

		return (retGeneral && retLogging && retSSCDOpts && retDocumSignatures &&
				retCertifExt && retX509Certif );
	}
}
