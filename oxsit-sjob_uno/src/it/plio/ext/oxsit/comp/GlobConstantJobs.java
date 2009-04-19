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

package it.plio.ext.oxsit.comp;

/** this class contains the global variables needed by all this implemenatation
 * 
 * @author beppe
 *
 */
public class GlobConstantJobs {
	
/** the followings are the dispatch code 'external' to this component (that is, this jar file)
 * 	public static final String m_sSignProtocolBaseUrl = "it.plio.ext.cnipa.comp.SignatureHandler:";
 * 
 */
	//intercepted url
	public static final String m_sUnoSignatureURLComplete = ".uno:Signature";
	public static final String m_sUnoSaveURLComplete = ".uno:Save";
	public static final String m_sUnoSaveAsURLComplete = ".uno:SaveAs";
	public static final String	m_sUnoSignatureURLProtocol	= ".uno:";
	public static final String	m_sUnoSignatureURLPath	= "Signature";

	// these come from sfx2/inc/sfx2/signaturestate.hxx
	public static final int		SIGNATURESTATE_UNKNOWN					= -1;
	public static final int		SIGNATURESTATE_NOSIGNATURES				= 0;
	public static final int		SIGNATURESTATE_SIGNATURES_OK			= 1;
	public static final int		SIGNATURESTATE_SIGNATURES_BROKEN		= 2;
	/** State was SIGNATURES_OK, but doc is modified now
	 * NOTE: this doesn't seem to be implemented in OOo as of 2.4. The
	 * behavior is different: when the document is modified, the signatures
	 * status disappears.
	 */
	public static final int		SIGNATURESTATE_SIGNATURES_INVALID		= 3;
	/**
	 *  signature is OK, but certificate could not be validated
	 */
	public static final int		SIGNATURESTATE_SIGNATURES_NOTVALIDATED	= 4;	
}
