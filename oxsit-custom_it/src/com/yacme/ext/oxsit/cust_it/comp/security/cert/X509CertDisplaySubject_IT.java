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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/cert/X509CertDisplaySubject_IT.java.
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

package com.yacme.ext.oxsit.cust_it.comp.security.cert;


import java.util.HashMap;
import java.util.Vector;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;

import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.cust_it.ConstantCustomIT;

/**
 *  This service implements the X509Certificate service.
 * receives the doc information from the task  
 *  
 * This objects has properties, they are set by the calling UNO objects.
 * 
 * The service is initialized with URL and XStorage of the document under test
 * Information about the certificates, number of certificates, status of every signature
 * ca be retrieved through properties 
 * 
 * @author beppec56
 *
 */
public class X509CertDisplaySubject_IT extends X509CertDisplayBase_IT //help class, implements XTypeProvider, XInterface, XWeak
			implements 
			XServiceInfo
			 {

	// the name of the class implementing this object
	public static final String			m_sImplementationName	= X509CertDisplaySubject_IT.class.getName();

	// the Object name, used to instantiate it inside the OOo API
	public static final String[]		m_sServiceNames			= { ConstantCustomIT.m_sX509_CERTIFICATE_DISPLAY_SERVICE_SUBJ_IT };

	/**
	 * 
	 * 
	 * @param _ctx
	 */
	public X509CertDisplaySubject_IT(XComponentContext _ctx) {
		super(_ctx);
	}

	@Override
	public String getImplementationName() {
//		m_aLoggerDialog.entering("getImplementationName");
		return m_sImplementationName;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#getSupportedServiceNames()
	 */
	@Override
	public String[] getSupportedServiceNames() {
//		m_aLoggerDialog.info("getSupportedServiceNames");
		return m_sServiceNames;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XServiceInfo#supportsService(java.lang.String)
	 */
	@Override
	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.info("supportsService",_sService);
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	protected void initSubjectName() {
		m_sSubjectName = "";
		//print the subject
		//order of printing is as got in the CNIPA spec
		//first, grab the OID in the subject name
		X509Name aName = m_aX509.getSubject();
		Vector<DERObjectIdentifier> oidv =  aName.getOIDs();
		Vector<?> values = aName.getValues();
		HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
		for(int i=0; i< oidv.size(); i++) {
			m_sSubjectName = m_sSubjectName + X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
					((m_bDisplayOID) ? (" (OID: "+oidv.elementAt(i).toString()+")" ): "") +" \n";
			hm.put(oidv.elementAt(i), values.elementAt(i).toString());
		}
		//extract data from subject name following CNIPA recommendation
		/*
		 * first lookup for givenname and surname, if not existent
		 * lookup for commonName (cn), if not existent
		 * lookup for pseudonym ()
		 */

		//look for givename (=nome di battesimo)
			m_sSubjectDisplayName = "";			
			//see BC source code for details about DefaultLookUp behaviour
			DERObjectIdentifier oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("givenname")); 
			if(hm.containsKey(oix)) {
				String tmpName = hm.get(oix).toString();
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("surname"));
				if(hm.containsKey(oix))
					m_sSubjectDisplayName = tmpName +" "+hm.get(oix).toString();
			}
			if(m_sSubjectDisplayName.length() == 0) {
				//check for CN
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
				if(hm.containsKey(oix)) {
					m_sSubjectDisplayName = hm.get(oix).toString();
				}
			}
			if(m_sSubjectDisplayName.length() == 0) {
				//if still not, check for pseudodym
				oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
				if(hm.containsKey(oix))
					m_sSubjectDisplayName = hm.get(oix).toString();						
			}
			if(m_sSubjectDisplayName.length() == 0)
				m_sSubjectDisplayName = m_sSubjectName;
	}

	protected void initIssuerName() {
		m_sIssuerName = "";
		X509Name aName = m_aX509.getIssuer();
		Vector<DERObjectIdentifier> oidv =  aName.getOIDs();
		HashMap<DERObjectIdentifier, String> hm = new HashMap<DERObjectIdentifier, String>(20);
		Vector<?> values = aName.getValues();
		for(int i=0; i< oidv.size(); i++) {
			m_sIssuerName = m_sIssuerName + X509Name.DefaultSymbols.get(oidv.elementAt(i))+"="+values.elementAt(i).toString()+
			((m_bDisplayOID) ? (" (OID: "+oidv.elementAt(i).toString()+")" ): "") +" \n";
			hm.put(oidv.elementAt(i), values.elementAt(i).toString());
		}
		//look for givename (=nome di battesimo)
		m_sIssuerDisplayName = "";			
		//see BC source code for details about DefaultLookUp behaviour
		DERObjectIdentifier oix; 
		if(m_sIssuerDisplayName.length() == 0) {
			//check for O
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("o")); 
			if(hm.containsKey(oix)) {
				m_sIssuerDisplayName = hm.get(oix).toString();
			}
		}
		if(m_sIssuerDisplayName.length() == 0) {
			//check for CN
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
			if(hm.containsKey(oix)) {
				m_sIssuerDisplayName = hm.get(oix).toString();
			}
		}
		if(m_sIssuerDisplayName.length() == 0) {
			//if still not, check for pseudodym
			oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("pseudonym"));
			if(hm.containsKey(oix))
				m_sIssuerDisplayName = hm.get(oix).toString();						
		}
		if(m_sIssuerDisplayName.length() == 0)
			m_sIssuerDisplayName = m_sIssuerName;
		//check for CN
		oix = (DERObjectIdentifier)(X509Name.DefaultLookUp.get("cn")); 
		if(hm.containsKey(oix)) {
			m_sIssuerCommonName = hm.get(oix).toString();
		}
	}
}
