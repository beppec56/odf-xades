/*************************************************************************
 *
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *
 *  This code is adapted from www.openscdp.org
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

/*
 * Original file header retained
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2006 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 *
 *  This file is part of OpenSCDP.
 *
 *  OpenSCDP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  OpenSCDP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSCDP; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * original package was:
 * 
 * package de.cardcontact.scdp.ocsp
 * 
 * File imported from http://www.openscdp.org/scsh3/download.html
 * 
 */
package com.yacme.ext.oxsit.cust_it.security.crl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.ocsp.*;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 * How to use it it's here:
 * 
 * http://www.openscdp.org/scsh3/ocspquery.html
 * 
 * Perform a OCSP query for a list of certificates
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class OCSPQuery {
	public final static int GOOD = 0;
	public final static int UNKNOWN = 1;
	public final static int REVOKED = 100;
	public final static int KEYCOMPROMISE = 101;
	public final static int CACOMPROMISE = 102;
	public final static int AFFILIATIONCHANGED = 103;
	public final static int SUPERSEDED = 104;
	public final static int CESSATIONOFOPERATION = 105;
	public final static int CERTIFICATEHOLD = 106;
	public final static int REMOVEFROMCRL = 108;
	public final static int PRIVILEGEWITHDRAWN = 109;
	public final static int	AACOMPROMISE = 110;
	
	X509Certificate issuercert;
	X509Certificate rootcert;
	X509Certificate m_UserCert;
	HashMap map;			// Map of certificates to be included in query
	HashMap cs;				// Map of status responses for queried certificates
	byte[] ocspResponse;	// DER encoded response

	/**
	 * Create query object with issuer certificate and maximum number of
	 * certificates included in query
	 *
	 * @param rootcert Root certificate
	 * @param issuercert Certificate of issuer for certificate(s) in question
	 */	
	public OCSPQuery(X509Certificate rootcert, X509Certificate issuercert) {
		this.issuercert = issuercert;
		this.rootcert = rootcert;
		map = new HashMap();
		cs = null;
	}

    static {
        Security
                .addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

	/**
	 * Return reason message for reason code
	 * 
	 * @param reason code from certStatus()
	 * @return String message
	 */
	public static String reasonText(int reason) {
		String rt = "Invalid reason code";
		
		switch (reason) {
		case GOOD: 					rt = "Good"; break;
		case UNKNOWN: 				rt = "Unknown"; break;
		case REVOKED: 				rt = "Revoked(0)"; break;
		case KEYCOMPROMISE:			rt = "Key compromise(1)"; break;
		case CACOMPROMISE:			rt = "CA compromise(2)"; break;
		case AFFILIATIONCHANGED:	rt = "Affilitation changed(3)"; break;
		case SUPERSEDED:			rt = "Superseded(4)"; break;
		case CESSATIONOFOPERATION:	rt = "Cessation of operation(5)"; break;
		case CERTIFICATEHOLD:		rt = "Certificate hold(6)"; break;
		case REMOVEFROMCRL:			rt = "Remove from CRL(8)"; break;
		case PRIVILEGEWITHDRAWN:	rt = "Privilege withdrawn(9)"; break;
		case AACOMPROMISE:			rt = "AA compromise(10)"; break;
		}
		return rt;
	}

	
	
	/**
	 * Note that this will accept only one certificate at a time.
	 * Add certificate to query
	 * 
	 * @param cert Certificate to add to query
	 * @throws OCSPQueryException Certificate does not match with issuer certificate
	 */	
	public void addCertificate(X509Certificate cert) throws OCSPQueryException {
		
		try {
			cert.verify(issuercert.getPublicKey());
			CertificateID certid = new CertificateID(CertificateID.HASH_SHA1, issuercert, cert.getSerialNumber());
			m_UserCert = cert;
			map.put(certid, cert);
		}

		catch(Exception e) {
//			e.printStackTrace();
			throw new OCSPQueryException(e.toString());
		}
	}

	

	/**
	 * Get DER encoded request
	 * @return DER encoded request
	 * @throws OCSPQueryException
	 */
	public byte[] getRequest() throws OCSPQueryException {

		CertificateID certid;
		byte[] request = null;

		try	{
		    // Get request generator engine and add certificates
		    OCSPReqGenerator gen = new OCSPReqGenerator();
		    Iterator iter = map.keySet().iterator();
		
		    while (iter.hasNext()) {
		        certid = (CertificateID)iter.next();
		        gen.addRequest(certid);
		    }

		    // Get DER encoded request
		    request = gen.generate().getEncoded();
		}
		
		catch(Exception e) {
			throw new OCSPQueryException(e.toString());
		}
		
		return request; 
	}
	
	
	
	/**
	 * Return DER encoded response received from OCSP responder in execute() method
	 * 
	 * @return DER encoded response
	 * @throws OCSPQueryException
	 */
	public byte[] getResponse() throws OCSPQueryException {
	    return ocspResponse;
	}

	

	/**
	 * Post HTTP request to OCSP responder
	 * 
	 * @param urlstring URL of OCSP responder
	 * @param request DER encoded request
	 * @param header Array of custom header field which replace automatic values. Must
	 *               be in the format "field: value".
	 * @return DER encoded response
	 * @throws OCSPQueryException
	 */
	public byte[] post(String urlstring, byte[] request, String[] header) throws OCSPQueryException {
	    
	    try	{
	        URL url = new URL(urlstring);
	        HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
	        // Get TLV encoded request and transmitt using HTTP connection
	        con.setAllowUserInteraction(false);
	        con.setDoInput(true);
	        con.setDoOutput(true);
	        con.setUseCaches(false);
	        con.setInstanceFollowRedirects(false);
	        con.setRequestMethod("POST");
	        
	        if (header == null) {
	            con.setRequestProperty("Content-Length",Integer.toString(request.length));
	            con.setRequestProperty("Content-Type","application/ocsp-request");
	        } else {
	            for (int i = 0; i < header.length; i++) {
	                String field = header[i];
	                
	                int pos = field.indexOf(':');
	                if (pos != -1) {
	                    String key = field.substring(0, pos);
	                    String value = field.substring(pos + 2);
	                    con.setRequestProperty(key, value);
	                } else {
	                    throw new OCSPQueryException("HTTP header must be in format '<key>: <value>'");
	                }
	            }
	        }
		
	        con.connect();
	        OutputStream os = con.getOutputStream();
	        os.write(request);
	        os.close();

	        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
	            throw new OCSPQueryException("Server did not respond with HTTP_OK(200) but with " + con.getResponseCode());
	        }
		
	        if ((con.getContentType() == null) || !con.getContentType().equals("application/ocsp-response")) {
	            throw new OCSPQueryException("Response MIME type is not application/ocsp-response");
	        }

	        // Read response
	        InputStream reader = con.getInputStream();

	        int resplen = con.getContentLength();
	        ocspResponse = new byte[resplen];

	        int offset = 0;
	        int bread;
	        while ((resplen > 0) && (bread = reader.read(ocspResponse, offset, resplen))!=-1) {
	            offset += bread;
	            resplen -= bread;
	        }

	        reader.close();
	        con.disconnect();

	        if (resplen > 0) {
	            throw new OCSPQueryException("Could not read full response");
	        }
	    }
	    
	    catch(OCSPQueryException e) {
	        throw e;
	    }
	    
	    catch(Exception e) {
	        throw new OCSPQueryException("Error talking to OCSP responder: " + e.getMessage());
	    }
	   
	    cs = null;					// Decoding yet to be done;
	    return ocspResponse;
	}
	

	
	/**
	 * Post HTTP request to OCSP responder
	 * 
	 * @param urlstring URL of OCSP responder
	 * @param request DER encoded request
	 * @return DER encoded response
	 * @throws OCSPQueryException
	 */
	public byte[] post(String urlstring, byte[] request) throws OCSPQueryException {
	    return post(urlstring, request, null);
	}
	
	

	/**
	 * Decode OCSP response, verify signature and store certificate status internally
	 * 
	 * @param response
	 * @throws OCSPQueryException
	 */
	public void decodeResponse(byte[] response) throws OCSPQueryException {
	    
		int i;
	    try	{
	        ocspResponse = response;
			OCSPResp ocspresp = new OCSPResp(response);
			
			switch (ocspresp.getStatus()) {
				case OCSPRespStatus.SUCCESSFUL: break;
				case OCSPRespStatus.INTERNAL_ERROR: throw new OCSPQueryException ("Internal OCSP server error");
				case OCSPRespStatus.MALFORMED_REQUEST: throw new OCSPQueryException ("Malformed request");
				case OCSPRespStatus.SIGREQUIRED: throw new OCSPQueryException ("Signature required for request");
				case OCSPRespStatus.TRY_LATER: throw new OCSPQueryException ("The server was too busy to answer");
				case OCSPRespStatus.UNAUTHORIZED: throw new OCSPQueryException ("Not authorised to access server");
				default: throw new OCSPQueryException ("Unknown OCSPResponse status code");
			}

			// Decode BasicOCSP response (inner structure)
			BasicOCSPResp bresp = (BasicOCSPResp)ocspresp.getResponseObject();

			if (bresp == null) {
				throw new OCSPQueryException("No BasicOCSPResponse found in response");
			}

			// Obtain certificate chain from response
			X509Certificate[] ocspcerts = bresp.getCerts("BC");

			// Verify all except trusted anchor
			for (i = 0; i < ocspcerts.length - 1; i++) {
				ocspcerts[i].verify(ocspcerts[i + 1].getPublicKey());
			}
			if (rootcert != null) {
			    ocspcerts[i].verify(rootcert.getPublicKey());
			}

/* Die Validierung des Zertifikatspfad schl�gt fehl, da das OCSP Zertifikat eine
 * ExtendedKeyUsage spezifiziert, BouncyCastle die Extension aber nicht unterst�tzt.
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");

			CertPath cp = cf.generateCertPath(certlist);

			CertPathValidator cpv = CertPathValidator.getInstance("PKIX", "BC");
			PKIXParameters pkixparam = new PKIXParameters(rootcert);
			pkixparam.setRevocationEnabled(false);
			
			PKIXCertPathValidatorResult result =
				(PKIXCertPathValidatorResult)cpv.validate(cp, pkixparam);
*/
			
			if (!bresp.verify(ocspcerts[0].getPublicKey(), "BC")) {
				throw new OCSPQueryException("OCSP Signature verification failed");
			}
	
			// Store result in hashmap for later status queries
			SingleResp[] certstat = bresp.getResponses();

			cs = new HashMap(certstat.length);
			
			for (i = 0; i < certstat.length; i++) {
                CertificateID certID = certstat[i].getCertID(); 
				cs.put(certID, certstat[i]);
            }
	    }

	    catch (OCSPQueryException e) {
			throw e;
		}
		 
		catch (Exception e) {
			throw new OCSPQueryException("OCSP decode error: " + e);
		}
	    
	}
	
	
	/**
	 * Execute OCSP query using HTTP connection from issuer certificate
	 * and verify respone
	 * 
	 * @throws OCSPQueryException
	 */	
	public void execute() throws OCSPQueryException {
		
		try {
		    // Obtain URL from OCSP Responder Extension
//		    byte[] ocspext = issuercert.getExtensionValue("1.3.6.1.5.5.7.1.1");
		    byte[] ocspext = m_UserCert.getExtensionValue("1.3.6.1.5.5.7.1.1");
		    
		    ASN1Sequence asn1 = (ASN1Sequence)X509ExtensionUtil.fromExtensionValue(ocspext);
		    asn1 = (ASN1Sequence)asn1.getObjectAt(0);
		    ASN1TaggedObject tasn1 = (ASN1TaggedObject)asn1.getObjectAt(1);
		    DEROctetString ostr = (DEROctetString)tasn1.getObject();
		    String urlstr = new String(ostr.getOctets());
		    
		    ocspResponse = post(urlstr, getRequest());
		    
		    decodeResponse(ocspResponse);
		} catch (OCSPQueryException e) {
			throw e;
		} catch (NullPointerException e) {
			//means the we don'yt have OCSP or is malformed,
			//we'll fallover to CRL 
			throw e;
		} catch (Exception e) {
			throw new OCSPQueryException("OCSP.execute() error: " + e);
		}
	}



	/**
	 * Query status for a given certificate from previous query
	 * @param cert Certificate in question
	 * @return OCSPQuery.<constant> status value  
	 * @throws OCSPQueryException
	 */
	public int certStatus(X509Certificate cert) throws OCSPQueryException {
		
	    if (cs == null) {
	        decodeResponse(ocspResponse);
	    }
	    
		int rc = GOOD;
		
		try	{
            CertificateID certID = new CertificateID(CertificateID.HASH_SHA1, issuercert, cert.getSerialNumber());
			SingleResp sr = (SingleResp)cs.get(certID);

            if (sr == null) {
                throw new OCSPQueryException("Certificate not contained in query");
            }
            
			Object ro = sr.getCertStatus();
			
			if (ro != null) {
				if (ro instanceof RevokedStatus) {
					RevokedStatus ri = (RevokedStatus)ro;
					
					rc = REVOKED;
					if (ri.hasRevocationReason()) {
						rc = ri.getRevocationReason() + 100;
					}
				} else if (ro instanceof UnknownStatus) {
					rc = UNKNOWN;
				}
			}
		}
				
		catch (Exception e) {
			throw new OCSPQueryException("OCSP.certStatus() error: " + e);
		}
		
		return rc;
	}
	
	

	/**
	 * Return time of revocation if specified
	 * 
	 * @param cert Certificate to query
	 * @return Date and Time of revocation
	 * 
	 * @throws OCSPQueryException
	 */
	public Date getRevocationTime(X509Certificate cert) throws OCSPQueryException {

	    if (cs == null) {
	        decodeResponse(ocspResponse);
	    }
	    
	    Date rd = null;
	    
	    try	{
			SingleResp sr = (SingleResp)cs.get(new CertificateID(CertificateID.HASH_SHA1, issuercert, cert.getSerialNumber()));

			Object ro = sr.getCertStatus();
			
			if (ro != null) {
				if (ro instanceof RevokedStatus) {
					RevokedStatus ri = (RevokedStatus)ro;

					rd = ri.getRevocationTime();
				}
			}
		}
				
		catch (Exception e) {
			throw new OCSPQueryException("OCSP.certStatus() error: " + e);
		}

		return rd;
	}
}
