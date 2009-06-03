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

package it.plio.ext.oxsit.cust_it;

import it.plio.ext.oxsit.cust_it.comp.security.ca.CertificationPathCache_IT;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
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
	
/*		if ( sImplementationName.equals( ManageGeneralOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageGeneralOptions.class, ManageGeneralOptions.m_sServiceNames );
//DEBUG			System.out.println("__getComponentFactory: "+ManageGeneralOptions.m_sImplementationName);
		}
		else if ( sImplementationName.equals( ManageLoggingOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageLoggingOptions.class, ManageLoggingOptions.m_sServiceNames );
//DEBUG			System.out.println("__getComponentFactory: "+ManageLoggingOptions.m_sImplementationName);
		}
		else if ( sImplementationName.equals( ManageSSCDOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageSSCDOptions.class, ManageSSCDOptions.m_sServiceNames );
//DEBUG			System.out.println("__getComponentFactory: "+ManageSSCDOptions.m_sImplementationName);
		}
		else if ( sImplementationName.equals( DocumentSignatures.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( DocumentSignatures.class, DocumentSignatures.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+DocumentSignatures.m_sImplementationName);
		}
		else if ( sImplementationName.equals( DocumentSigner.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( DocumentSigner.class, DocumentSigner.m_sServiceNames );
	//DEBUG		System.out.println("__getComponentFactory: "+DocumentSignatures.m_sImplementationName);
		}
		else if ( sImplementationName.equals( CertificateComplianceCA_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( CertificateComplianceCA_IT.class, CertificateComplianceCA_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+CertificateCompliance_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( CertificateCompliance_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( CertificateCompliance_IT.class, CertificateCompliance_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+CertificateCompliance_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( CertificateRevocation_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( CertificateRevocation_IT.class, CertificateRevocation_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+CertificateRevocation_IT.m_sImplementationName);
	}
		else if ( sImplementationName.equals( CertificationPath_IT.m_sImplementationName ) ) {
				xFactory = Factory.createComponentFactory( CertificationPath_IT.class, CertificationPath_IT.m_sServiceNames );
	//DEBUG		System.out.println("__getComponentFactory: "+CertificationPath_IT.m_sImplementationName);
		}
		else*/ if ( sImplementationName.equals( CertificationPathCache_IT.m_sImplementationName ) ) {
					xFactory = Factory.createComponentFactory( CertificationPathCache_IT.class, CertificationPathCache_IT.m_sServiceNames );
		//DEBUG		System.out.println("__getComponentFactory: "+CertificationPathCache_IT.m_sImplementationName);
		}
/*		else if ( sImplementationName.equals( CertificateExtension.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( CertificateExtension.class, CertificateExtension.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+X509Certificate.m_sImplementationName);
		}
		else if ( sImplementationName.equals( X509CertDisplayCA_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( X509CertDisplayCA_IT.class, X509CertDisplayCA_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+X509CertDisplayCA_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( X509CertDisplaySubject_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( X509CertDisplaySubject_IT.class, X509CertDisplaySubject_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+X509CertDisplaySubject_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( X509Certificate.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( X509Certificate.class, X509Certificate.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+X509Certificate.m_sImplementationName);
		}
		else if ( sImplementationName.equals( SSCDevice.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( SSCDevice.class, SSCDevice.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+SSCDevice.m_sImplementationName);
		}
		else if ( sImplementationName.equals( AvailableSSCDs.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( AvailableSSCDs.class, AvailableSSCDs.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+AvailableSSCDs.m_sImplementationName);
		}*/
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
	 */
	public synchronized static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
//		System.out.println("__writeRegistryServiceInfo: "+ManageOptions.m_sImplementationName+" "+ManageOptions.m_sServiceNames[0] );
/*		boolean retGeneral = 
			Factory.writeRegistryServiceInfo( ManageGeneralOptions.m_sImplementationName, ManageGeneralOptions.m_sServiceNames, xRegistryKey );
		boolean retLogging = 
			Factory.writeRegistryServiceInfo( ManageLoggingOptions.m_sImplementationName, ManageLoggingOptions.m_sServiceNames, xRegistryKey );
		
		boolean retSSCDOpts = 
			Factory.writeRegistryServiceInfo( ManageSSCDOptions.m_sImplementationName, ManageSSCDOptions.m_sServiceNames, xRegistryKey );

		boolean retDigitalSignatures = 
			Factory.writeRegistryServiceInfo( DocumentSignatures.m_sImplementationName, DocumentSignatures.m_sServiceNames, xRegistryKey );

		boolean retDocumSigner = 
			Factory.writeRegistryServiceInfo( DocumentSigner.m_sImplementationName, DocumentSigner.m_sServiceNames, xRegistryKey );

		boolean retCertifComplCA = 
			Factory.writeRegistryServiceInfo( CertificateComplianceCA_IT.m_sImplementationName, CertificateComplianceCA_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifCompl = 
			Factory.writeRegistryServiceInfo( CertificateCompliance_IT.m_sImplementationName, CertificateCompliance_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifRevoc = 
			Factory.writeRegistryServiceInfo( CertificateRevocation_IT.m_sImplementationName, CertificateRevocation_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifPath = 
			Factory.writeRegistryServiceInfo( CertificationPath_IT.m_sImplementationName, CertificationPath_IT.m_sServiceNames, xRegistryKey );
*/
		boolean retCertifPathCache = 
			Factory.writeRegistryServiceInfo( CertificationPathCache_IT.m_sImplementationName, CertificationPathCache_IT.m_sServiceNames, xRegistryKey );

/*		boolean retCertifExt = 
			Factory.writeRegistryServiceInfo( CertificateExtension.m_sImplementationName, CertificateExtension.m_sServiceNames, xRegistryKey );

		boolean retCertifDispIssIT = 
			Factory.writeRegistryServiceInfo( X509CertDisplayCA_IT.m_sImplementationName, X509CertDisplayCA_IT.m_sServiceNames, xRegistryKey );
		boolean retCertifDispSubjIT = 
			Factory.writeRegistryServiceInfo( X509CertDisplaySubject_IT.m_sImplementationName, X509CertDisplaySubject_IT.m_sServiceNames, xRegistryKey );

		boolean retQualCertif = 
			Factory.writeRegistryServiceInfo( X509Certificate.m_sImplementationName, X509Certificate.m_sServiceNames, xRegistryKey );

		boolean retSSCDevice = 
			Factory.writeRegistryServiceInfo( SSCDevice.m_sImplementationName, SSCDevice.m_sServiceNames, xRegistryKey );

		boolean retAvailSSCDs = 
			Factory.writeRegistryServiceInfo( AvailableSSCDs.m_sImplementationName, AvailableSSCDs.m_sServiceNames, xRegistryKey );

		if (!retGeneral)
			System.out.println("__writeRegistryServiceInfo: "+ManageGeneralOptions.m_sImplementationName + "failed");		

		if (!retLogging)
			System.out.println("__writeRegistryServiceInfo: "+ManageLoggingOptions.m_sImplementationName + "failed");		
		
		if (!retSSCDOpts)
			System.out.println("__writeRegistryServiceInfo: "+ManageSSCDOptions.m_sImplementationName + "failed");		
		
		if (!retDigitalSignatures)
			System.out.println("__writeRegistryServiceInfo: "+DocumentSignatures.m_sImplementationName + "failed");		

		if (!retDocumSigner)
			System.out.println("__writeRegistryServiceInfo: "+DocumentSigner.m_sImplementationName + "failed");		

		if (!retCertifComplCA)
			System.out.println("__writeRegistryServiceInfo: "+CertificateComplianceCA_IT.m_sImplementationName + "failed");
		
		if (!retCertifCompl)
			System.out.println("__writeRegistryServiceInfo: "+CertificateCompliance_IT.m_sImplementationName + "failed");
		if (!retCertifPath)
			System.out.println("__writeRegistryServiceInfo: "+CertificationPath_IT.m_sImplementationName + "failed");		
		if (!retCertifRevoc)
			System.out.println("__writeRegistryServiceInfo: "+CertificateRevocation_IT.m_sImplementationName + "failed");*/
		if (!retCertifPathCache)
			System.out.println("__writeRegistryServiceInfo: "+CertificationPathCache_IT.m_sImplementationName + "failed");		

/*		if (!retCertifExt)
			System.out.println("__writeRegistryServiceInfo: "+CertificateExtension.m_sImplementationName + "failed");		

		if (!retCertifDispIssIT)
			System.out.println("__writeRegistryServiceInfo: "+X509CertDisplayCA_IT.m_sImplementationName + "failed");		
		if (!retCertifDispSubjIT)
			System.out.println("__writeRegistryServiceInfo: "+X509CertDisplaySubject_IT.m_sImplementationName + "failed");		

		if (!retQualCertif)
			System.out.println("__writeRegistryServiceInfo: "+X509Certificate.m_sImplementationName + "failed");		

		if (!retSSCDevice)
			System.out.println("__writeRegistryServiceInfo: "+SSCDevice.m_sImplementationName + "failed");		

		if (!retAvailSSCDs)
			System.out.println("__writeRegistryServiceInfo: "+AvailableSSCDs.m_sImplementationName + "failed");		
*/
		return (/*retGeneral && retLogging && retDigitalSignatures &&
					retQualCertif && retSSCDevice && retAvailSSCDs &&
					retSSCDOpts && retCertifExt && retCertifCompl &&
					retCertifPath &&*/ retCertifPathCache /*&&
					retCertifRevoc && retCertifDispIssIT && retCertifDispSubjIT &&
					retCertifComplCA && retDocumSigner*/);
	}
}
