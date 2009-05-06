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

package it.plio.ext.oxsit.comp.security.cert.test;

import it.infocamere.freesigner.gui.ReadCertsTask;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.trento.comune.j4sign.pcsc.CardInReaderInfo;
import it.trento.comune.j4sign.pcsc.CardInfo;
import it.trento.comune.j4sign.pcsc.PCSCHelper;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class TestOnCertificates {

	
	protected DynamicLogger m_aLogger;
	
	protected XComponentContext m_xCC;

	public TestOnCertificates(XComponentContext _CTX) {
		m_xCC = _CTX;
		m_aLogger = new DynamicLogger(this,_CTX);
		m_aLogger.enableLogging();
		m_aLogger.ctor();
	}

	private class CertInfo {
		public String certName;

		public X509Certificate c;

		/**
		 * Constructor (null)
		 * 
		 */

		public CertInfo() {
			certName = null;
			c = null;
		}

		/**
		 * Constructor
		 * 
		 * @param x
		 *            X509Certificate
		 */
		public CertInfo(X509Certificate x) {
			certName = toCNNames("" + x.getSubjectDN());
			c = x;
		}

		public String toString() {
			return certName;
		}

		public X509Certificate getCertificate() {
			return c;
		}

		public void setCertificate(X509Certificate x) {
			c = x;
		}

		public void setName(String s) {
			certName = s;
		}

	}

	private String toCNNames(String DN) {

		int offset = DN.indexOf("CN=");
		int end = DN.indexOf(",", offset);
		String CN;
		if (end != -1) {
			CN = DN.substring(offset + 3, end);
		} else {
			CN = DN.substring(offset + 3, DN.length());
		}
		CN = CN.substring(0, CN.length());
		return CN;

	}

	public void testMethod() {
		// TODO Auto-generated method stub
		m_aLogger.entering("testMethod");

		PCSCHelper pcsc = new PCSCHelper(true);

		m_aLogger.log("After 'new PCSCHelper'");

		java.util.List infos = pcsc.findCardsAndReaders();

		CardInfo ci = null;
		Iterator<CardInReaderInfo> it = infos.iterator();
		int indexToken = 0;
		int indexReader = 0;

		while (it.hasNext()) {
			m_aLogger.log("Reader " + indexReader + ")");

			CardInReaderInfo cIr = it.next();
			String currReader = cIr.getReader();

			ci = cIr.getCard();
			
			if (ci != null) {
				
				m_aLogger.log("Informations found for this card:");
				m_aLogger.log("\tDescription:\t"
						+ ci.getProperty("description"));
				m_aLogger.log("\tManufacturer:\t"
						+ ci.getProperty("manufacturer"));
				m_aLogger.log("\tATR:\t\t" + ci.getProperty("atr"));
				m_aLogger.log("\tCriptoki:\t" + ci.getProperty("lib"));
				
				m_aLogger.log("\n\tLettura certificati");
				
				ReadCertsTask rt = new ReadCertsTask(cIr);
				Collection certsOnToken = rt.getCertsOnToken();
				if (certsOnToken != null) {
					Iterator certIt = certsOnToken.iterator();
					if (certsOnToken.isEmpty()) {
						m_aLogger.log("\tcertsOnToken vuoto");
						CertInfo c = new CertInfo();
						c.setName("Carta presente ma vuota");

					}
					while (certIt.hasNext()) {

						m_aLogger.log("\n******************************************  &&  ****************  &&  *********************************************************");
						X509Certificate cert = (X509Certificate) certIt.next();
						CertInfo c = new CertInfo(cert);
						m_aLogger.log(cert.toString());
						m_aLogger.log("\n*******************************************************************************************************************");

					}

					rt.closeSession();
					rt.libFinalize();
					indexToken++;
				}

			} else {
				m_aLogger.log("No card in reader '" + currReader + "'!");

			}

			indexReader++;
		}		
	}

}
