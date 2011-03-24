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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/RegisterServices.java.
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

package com.yacme.ext.oxsit.cust_it;


import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.registry.XRegistryKey;
import com.yacme.ext.oxsit.cust_it.comp.security.AvailableSSCDs_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.DocumentSigner_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.SSCDevice_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.ca.CertificationPathCache_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.ca.CertificationPath_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.cert.CertificateComplianceCA_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.cert.CertificateCompliance_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.cert.CertificateRevocation_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.cert.X509CertDisplayCA_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.cert.X509CertDisplaySubject_IT;

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
	
/*		else if ( sImplementationName.equals( ManageSSCDOptions.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( ManageSSCDOptions.class, ManageSSCDOptions.m_sServiceNames );
//DEBUG			System.out.println("__getComponentFactory: "+ManageSSCDOptions.m_sImplementationName);
		}*/
		if ( sImplementationName.equals( DocumentSigner_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( DocumentSigner_IT.class, DocumentSigner_IT.m_sServiceNames );
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
		else if ( sImplementationName.equals( CertificationPathCache_IT.m_sImplementationName ) ) {
					xFactory = Factory.createComponentFactory( CertificationPathCache_IT.class, CertificationPathCache_IT.m_sServiceNames );
		//DEBUG		System.out.println("__getComponentFactory: "+CertificationPathCache_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( X509CertDisplayCA_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( X509CertDisplayCA_IT.class, X509CertDisplayCA_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+X509CertDisplayCA_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( X509CertDisplaySubject_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( X509CertDisplaySubject_IT.class, X509CertDisplaySubject_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+X509CertDisplaySubject_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( SSCDevice_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( SSCDevice_IT.class, SSCDevice_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+SSCDevice_IT.m_sImplementationName);
		}
		else if ( sImplementationName.equals( AvailableSSCDs_IT.m_sImplementationName ) ) {
			xFactory = Factory.createComponentFactory( AvailableSSCDs_IT.class, AvailableSSCDs_IT.m_sServiceNames );
//DEBUG		System.out.println("__getComponentFactory: "+AvailableSSCDs_IT.m_sImplementationName);
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
	 */
	public synchronized static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
//		System.out.println("__writeRegistryServiceInfo: "+ManageOptions.m_sImplementationName+" "+ManageOptions.m_sServiceNames[0] );
/* 		
		boolean retSSCDOpts = 
			Factory.writeRegistryServiceInfo( ManageSSCDOptions.m_sImplementationName, ManageSSCDOptions.m_sServiceNames, xRegistryKey );
*/
		boolean retDocumSigner = 
			Factory.writeRegistryServiceInfo( DocumentSigner_IT.m_sImplementationName, DocumentSigner_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifComplCA = 
			Factory.writeRegistryServiceInfo( CertificateComplianceCA_IT.m_sImplementationName, CertificateComplianceCA_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifCompl = 
			Factory.writeRegistryServiceInfo( CertificateCompliance_IT.m_sImplementationName, CertificateCompliance_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifRevoc = 
			Factory.writeRegistryServiceInfo( CertificateRevocation_IT.m_sImplementationName, CertificateRevocation_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifPath = 
			Factory.writeRegistryServiceInfo( CertificationPath_IT.m_sImplementationName, CertificationPath_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifPathCache = 
			Factory.writeRegistryServiceInfo( CertificationPathCache_IT.m_sImplementationName, CertificationPathCache_IT.m_sServiceNames, xRegistryKey );

		boolean retCertifDispIssIT = 
			Factory.writeRegistryServiceInfo( X509CertDisplayCA_IT.m_sImplementationName, X509CertDisplayCA_IT.m_sServiceNames, xRegistryKey );
		boolean retCertifDispSubjIT = 
			Factory.writeRegistryServiceInfo( X509CertDisplaySubject_IT.m_sImplementationName, X509CertDisplaySubject_IT.m_sServiceNames, xRegistryKey );

		boolean retSSCDevice = 
			Factory.writeRegistryServiceInfo( SSCDevice_IT.m_sImplementationName, SSCDevice_IT.m_sServiceNames, xRegistryKey );

		boolean retAvailSSCDs = 
			Factory.writeRegistryServiceInfo( AvailableSSCDs_IT.m_sImplementationName, AvailableSSCDs_IT.m_sServiceNames, xRegistryKey );

/*		if (!retSSCDOpts)
			System.out.println("__writeRegistryServiceInfo: "+ManageSSCDOptions.m_sImplementationName + "failed");		

*/		if (!retDocumSigner)
			System.out.println("__writeRegistryServiceInfo: "+DocumentSigner_IT.m_sImplementationName + "failed");		

		if (!retCertifComplCA)
			System.out.println("__writeRegistryServiceInfo: "+CertificateComplianceCA_IT.m_sImplementationName + "failed");

		if (!retCertifCompl)
			System.out.println("__writeRegistryServiceInfo: "+CertificateCompliance_IT.m_sImplementationName + "failed");
		if (!retCertifPath)
			System.out.println("__writeRegistryServiceInfo: "+CertificationPath_IT.m_sImplementationName + "failed");		
		if (!retCertifRevoc)
			System.out.println("__writeRegistryServiceInfo: "+CertificateRevocation_IT.m_sImplementationName + "failed");
		if (!retCertifPathCache)
			System.out.println("__writeRegistryServiceInfo: "+CertificationPathCache_IT.m_sImplementationName + "failed");		

		if (!retCertifDispIssIT)
			System.out.println("__writeRegistryServiceInfo: "+X509CertDisplayCA_IT.m_sImplementationName + "failed");
		if (!retCertifDispSubjIT)
			System.out.println("__writeRegistryServiceInfo: "+X509CertDisplaySubject_IT.m_sImplementationName + "failed");		

		if (!retSSCDevice)
			System.out.println("__writeRegistryServiceInfo: "+SSCDevice_IT.m_sImplementationName + "failed");		

		if (!retAvailSSCDs)
			System.out.println("__writeRegistryServiceInfo: "+AvailableSSCDs_IT.m_sImplementationName + "failed");		

		return (retDocumSigner && retSSCDevice && retAvailSSCDs &&
					retCertifCompl &&
					retCertifPath && retCertifPathCache &&
					retCertifRevoc && retCertifDispIssIT && retCertifDispSubjIT &&
					retCertifComplCA);
	}
}
