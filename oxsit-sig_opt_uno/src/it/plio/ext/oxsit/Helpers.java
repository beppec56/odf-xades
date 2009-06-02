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

import iaik.pkcs.pkcs11.wrapper.PKCS11Constants;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;
import it.plio.ext.oxsit.security.XOX_DocumentSignaturesState;

import com.sun.star.beans.*;
import com.sun.star.container.*;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.frame.XModel;
import com.sun.star.lang.*;
import com.sun.star.lang.NoSuchMethodException;
import com.sun.star.ucb.ServiceNotFoundException;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import com.sun.star.util.XChangesListener;
import com.sun.star.beans.PropertyValue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/** Helper class composed of static methods.
 *  
 * @author beppe
 */
public class Helpers {
	  /**
	   * Maps mechanism codes as Long to their names as Strings.
	   */
	  protected static Hashtable mechansimNames_;

	  /**
	   * For converting numbers to their hex presentation.
	   */
	  protected static final char HEX_DIGITS[] = {
	      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	      'A', 'B', 'C', 'D', 'E', 'F' };

	  /**
	   * Converts a long value to a hexadecimal String of length 16. Includes
	   * leading zeros if necessary.
	   *
	   * @param value The long value to be converted.
	   * @return The hexadecimal string representation of the long value.
	   */
	  public static String toFullHexString(long value) {
	    long currentValue = value;
	    StringBuffer stringBuffer = new StringBuffer(16);
	    for(int j = 0; j < 16; j++) {
	      int currentDigit = (int) currentValue & 0xf;
	      stringBuffer.append(HEX_DIGITS[currentDigit]);
	      currentValue >>>= 4;
	    }

	    return stringBuffer.reverse().toString();
	  }


	
	protected Helpers() {
	}

	  /**
	   * Converts the long value code of a mechanism to a name.
	   *
	   * @param mechansimCode The code of the mechanism to be converted to a string.
	   * @return The string representation of the mechanism.
	   */
	  public static String mechanismCodeToString(long mechansimCode) {
	    if (mechansimNames_ == null) {
	      Hashtable mechansimNames = new Hashtable(160);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_PKCS_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_RSA_PKCS_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_PKCS), PKCS11Constants.NAME_CKM_RSA_PKCS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_9796), PKCS11Constants.NAME_CKM_RSA_9796);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_X_509), PKCS11Constants.NAME_CKM_RSA_X_509);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD2_RSA_PKCS), PKCS11Constants.NAME_CKM_MD2_RSA_PKCS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD5_RSA_PKCS), PKCS11Constants.NAME_CKM_MD5_RSA_PKCS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA1_RSA_PKCS), PKCS11Constants.NAME_CKM_SHA1_RSA_PKCS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD128_RSA_PKCS), PKCS11Constants.NAME_CKM_RIPEMD128_RSA_PKCS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD160_RSA_PKCS), PKCS11Constants.NAME_CKM_RIPEMD160_RSA_PKCS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_PKCS_OAEP), PKCS11Constants.NAME_CKM_RSA_PKCS_OAEP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_X9_31_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_RSA_X9_31_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_X9_31), PKCS11Constants.NAME_CKM_RSA_X9_31);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA1_RSA_X9_31), PKCS11Constants.NAME_CKM_SHA1_RSA_X9_31);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RSA_PKCS_PSS), PKCS11Constants.NAME_CKM_RSA_PKCS_PSS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA1_RSA_PKCS_PSS), PKCS11Constants.NAME_CKM_SHA1_RSA_PKCS_PSS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DSA_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_DSA_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DSA), PKCS11Constants.NAME_CKM_DSA);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DSA_SHA1), PKCS11Constants.NAME_CKM_DSA_SHA1);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DH_PKCS_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_DH_PKCS_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DH_PKCS_DERIVE), PKCS11Constants.NAME_CKM_DH_PKCS_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_X9_42_DH_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_X9_42_DH_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_X9_42_DH_DERIVE), PKCS11Constants.NAME_CKM_X9_42_DH_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_X9_42_DH_HYBRID_DERIVE), PKCS11Constants.NAME_CKM_X9_42_DH_HYBRID_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_X9_42_MQV_DERIVE), PKCS11Constants.NAME_CKM_X9_42_MQV_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC2_KEY_GEN), PKCS11Constants.NAME_CKM_RC2_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC2_ECB), PKCS11Constants.NAME_CKM_RC2_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC2_CBC), PKCS11Constants.NAME_CKM_RC2_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC2_MAC), PKCS11Constants.NAME_CKM_RC2_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC2_MAC_GENERAL), PKCS11Constants.NAME_CKM_RC2_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC2_CBC_PAD), PKCS11Constants.NAME_CKM_RC2_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC4_KEY_GEN), PKCS11Constants.NAME_CKM_RC4_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC4), PKCS11Constants.NAME_CKM_RC4);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES_KEY_GEN), PKCS11Constants.NAME_CKM_DES_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES_ECB), PKCS11Constants.NAME_CKM_DES_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES_CBC), PKCS11Constants.NAME_CKM_DES_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES_MAC), PKCS11Constants.NAME_CKM_DES_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES_MAC_GENERAL), PKCS11Constants.NAME_CKM_DES_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES_CBC_PAD), PKCS11Constants.NAME_CKM_DES_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES2_KEY_GEN), PKCS11Constants.NAME_CKM_DES2_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES3_KEY_GEN), PKCS11Constants.NAME_CKM_DES3_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES3_ECB), PKCS11Constants.NAME_CKM_DES3_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES3_CBC), PKCS11Constants.NAME_CKM_DES3_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES3_MAC), PKCS11Constants.NAME_CKM_DES3_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES3_MAC_GENERAL), PKCS11Constants.NAME_CKM_DES3_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DES3_CBC_PAD), PKCS11Constants.NAME_CKM_DES3_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CDMF_KEY_GEN), PKCS11Constants.NAME_CKM_CDMF_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CDMF_ECB), PKCS11Constants.NAME_CKM_CDMF_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CDMF_CBC), PKCS11Constants.NAME_CKM_CDMF_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CDMF_MAC), PKCS11Constants.NAME_CKM_CDMF_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CDMF_MAC_GENERAL), PKCS11Constants.NAME_CKM_CDMF_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CDMF_CBC_PAD), PKCS11Constants.NAME_CKM_CDMF_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD2), PKCS11Constants.NAME_CKM_MD2);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD2_HMAC), PKCS11Constants.NAME_CKM_MD2_HMAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD2_HMAC_GENERAL), PKCS11Constants.NAME_CKM_MD2_HMAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD5), PKCS11Constants.NAME_CKM_MD5);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD5_HMAC), PKCS11Constants.NAME_CKM_MD5_HMAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD5_HMAC_GENERAL), PKCS11Constants.NAME_CKM_MD5_HMAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA_1), PKCS11Constants.NAME_CKM_SHA_1);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA_1_HMAC), PKCS11Constants.NAME_CKM_SHA_1_HMAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA_1_HMAC_GENERAL), PKCS11Constants.NAME_CKM_SHA_1_HMAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD128), PKCS11Constants.NAME_CKM_RIPEMD128);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD128_HMAC), PKCS11Constants.NAME_CKM_RIPEMD128_HMAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD128_HMAC_GENERAL), PKCS11Constants.NAME_CKM_RIPEMD128_HMAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD160), PKCS11Constants.NAME_CKM_RIPEMD160);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD160_HMAC), PKCS11Constants.NAME_CKM_RIPEMD160_HMAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RIPEMD160_HMAC_GENERAL), PKCS11Constants.NAME_CKM_RIPEMD160_HMAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST_KEY_GEN), PKCS11Constants.NAME_CKM_CAST_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST_ECB), PKCS11Constants.NAME_CKM_CAST_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST_CBC), PKCS11Constants.NAME_CKM_CAST_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST_MAC), PKCS11Constants.NAME_CKM_CAST_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST_MAC_GENERAL), PKCS11Constants.NAME_CKM_CAST_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST_CBC_PAD), PKCS11Constants.NAME_CKM_CAST_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST3_KEY_GEN), PKCS11Constants.NAME_CKM_CAST3_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST3_ECB), PKCS11Constants.NAME_CKM_CAST3_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST3_CBC), PKCS11Constants.NAME_CKM_CAST3_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST3_MAC), PKCS11Constants.NAME_CKM_CAST3_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST3_MAC_GENERAL), PKCS11Constants.NAME_CKM_CAST3_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST3_CBC_PAD), PKCS11Constants.NAME_CKM_CAST3_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST5_KEY_GEN), PKCS11Constants.NAME_CKM_CAST5_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST128_KEY_GEN), PKCS11Constants.NAME_CKM_CAST128_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST5_ECB), PKCS11Constants.NAME_CKM_CAST5_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST128_ECB), PKCS11Constants.NAME_CKM_CAST128_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST5_CBC), PKCS11Constants.NAME_CKM_CAST5_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST128_CBC), PKCS11Constants.NAME_CKM_CAST128_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST5_MAC), PKCS11Constants.NAME_CKM_CAST5_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST128_MAC), PKCS11Constants.NAME_CKM_CAST128_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST5_MAC_GENERAL), PKCS11Constants.NAME_CKM_CAST5_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST128_MAC_GENERAL), PKCS11Constants.NAME_CKM_CAST128_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST5_CBC_PAD), PKCS11Constants.NAME_CKM_CAST5_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CAST128_CBC_PAD), PKCS11Constants.NAME_CKM_CAST128_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC5_KEY_GEN), PKCS11Constants.NAME_CKM_RC5_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC5_ECB), PKCS11Constants.NAME_CKM_RC5_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC5_CBC), PKCS11Constants.NAME_CKM_RC5_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC5_MAC), PKCS11Constants.NAME_CKM_RC5_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC5_MAC_GENERAL), PKCS11Constants.NAME_CKM_RC5_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_RC5_CBC_PAD), PKCS11Constants.NAME_CKM_RC5_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_IDEA_KEY_GEN), PKCS11Constants.NAME_CKM_IDEA_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_IDEA_ECB), PKCS11Constants.NAME_CKM_IDEA_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_IDEA_CBC), PKCS11Constants.NAME_CKM_IDEA_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_IDEA_MAC), PKCS11Constants.NAME_CKM_IDEA_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_IDEA_MAC_GENERAL), PKCS11Constants.NAME_CKM_IDEA_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_IDEA_CBC_PAD), PKCS11Constants.NAME_CKM_IDEA_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_GENERIC_SECRET_KEY_GEN), PKCS11Constants.NAME_CKM_GENERIC_SECRET_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CONCATENATE_BASE_AND_KEY), PKCS11Constants.NAME_CKM_CONCATENATE_BASE_AND_KEY);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CONCATENATE_BASE_AND_DATA), PKCS11Constants.NAME_CKM_CONCATENATE_BASE_AND_DATA);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_CONCATENATE_DATA_AND_BASE), PKCS11Constants.NAME_CKM_CONCATENATE_DATA_AND_BASE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_XOR_BASE_AND_DATA), PKCS11Constants.NAME_CKM_XOR_BASE_AND_DATA);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_EXTRACT_KEY_FROM_KEY), PKCS11Constants.NAME_CKM_EXTRACT_KEY_FROM_KEY);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SSL3_PRE_MASTER_KEY_GEN), PKCS11Constants.NAME_CKM_SSL3_PRE_MASTER_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SSL3_MASTER_KEY_DERIVE), PKCS11Constants.NAME_CKM_SSL3_MASTER_KEY_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SSL3_KEY_AND_MAC_DERIVE), PKCS11Constants.NAME_CKM_SSL3_KEY_AND_MAC_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SSL3_MASTER_KEY_DERIVE_DH), PKCS11Constants.NAME_CKM_SSL3_MASTER_KEY_DERIVE_DH);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_TLS_PRE_MASTER_KEY_GEN), PKCS11Constants.NAME_CKM_TLS_PRE_MASTER_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_TLS_MASTER_KEY_DERIVE), PKCS11Constants.NAME_CKM_TLS_MASTER_KEY_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_TLS_KEY_AND_MAC_DERIVE), PKCS11Constants.NAME_CKM_TLS_KEY_AND_MAC_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_TLS_MASTER_KEY_DERIVE_DH), PKCS11Constants.NAME_CKM_TLS_MASTER_KEY_DERIVE_DH);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SSL3_MD5_MAC), PKCS11Constants.NAME_CKM_SSL3_MD5_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SSL3_SHA1_MAC), PKCS11Constants.NAME_CKM_SSL3_SHA1_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD5_KEY_DERIVATION), PKCS11Constants.NAME_CKM_MD5_KEY_DERIVATION);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_MD2_KEY_DERIVATION), PKCS11Constants.NAME_CKM_MD2_KEY_DERIVATION);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SHA1_KEY_DERIVATION), PKCS11Constants.NAME_CKM_SHA1_KEY_DERIVATION);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_MD2_DES_CBC), PKCS11Constants.NAME_CKM_PBE_MD2_DES_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_MD5_DES_CBC), PKCS11Constants.NAME_CKM_PBE_MD5_DES_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_MD5_CAST_CBC), PKCS11Constants.NAME_CKM_PBE_MD5_CAST_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_MD5_CAST3_CBC), PKCS11Constants.NAME_CKM_PBE_MD5_CAST3_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_MD5_CAST5_CBC), PKCS11Constants.NAME_CKM_PBE_MD5_CAST5_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_MD5_CAST128_CBC), PKCS11Constants.NAME_CKM_PBE_MD5_CAST128_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_CAST5_CBC), PKCS11Constants.NAME_CKM_PBE_SHA1_CAST5_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_CAST128_CBC), PKCS11Constants.NAME_CKM_PBE_SHA1_CAST128_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_RC4_128), PKCS11Constants.NAME_CKM_PBE_SHA1_RC4_128);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_RC4_40), PKCS11Constants.NAME_CKM_PBE_SHA1_RC4_40);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_DES3_EDE_CBC), PKCS11Constants.NAME_CKM_PBE_SHA1_DES3_EDE_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_DES2_EDE_CBC), PKCS11Constants.NAME_CKM_PBE_SHA1_DES2_EDE_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_RC2_128_CBC), PKCS11Constants.NAME_CKM_PBE_SHA1_RC2_128_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBE_SHA1_RC2_40_CBC), PKCS11Constants.NAME_CKM_PBE_SHA1_RC2_40_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PKCS5_PBKD2), PKCS11Constants.NAME_CKM_PKCS5_PBKD2);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_PBA_SHA1_WITH_SHA1_HMAC), PKCS11Constants.NAME_CKM_PBA_SHA1_WITH_SHA1_HMAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_KEY_WRAP_LYNKS), PKCS11Constants.NAME_CKM_KEY_WRAP_LYNKS);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_KEY_WRAP_SET_OAEP), PKCS11Constants.NAME_CKM_KEY_WRAP_SET_OAEP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_KEY_GEN), PKCS11Constants.NAME_CKM_SKIPJACK_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_ECB64), PKCS11Constants.NAME_CKM_SKIPJACK_ECB64);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_CBC64), PKCS11Constants.NAME_CKM_SKIPJACK_CBC64);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_OFB64), PKCS11Constants.NAME_CKM_SKIPJACK_OFB64);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_CFB64), PKCS11Constants.NAME_CKM_SKIPJACK_CFB64);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_CFB32), PKCS11Constants.NAME_CKM_SKIPJACK_CFB32);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_CFB16), PKCS11Constants.NAME_CKM_SKIPJACK_CFB16);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_CFB8), PKCS11Constants.NAME_CKM_SKIPJACK_CFB8);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_WRAP), PKCS11Constants.NAME_CKM_SKIPJACK_WRAP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_PRIVATE_WRAP), PKCS11Constants.NAME_CKM_SKIPJACK_PRIVATE_WRAP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_SKIPJACK_RELAYX), PKCS11Constants.NAME_CKM_SKIPJACK_RELAYX);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_KEA_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_KEA_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_KEA_KEY_DERIVE), PKCS11Constants.NAME_CKM_KEA_KEY_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_FORTEZZA_TIMESTAMP), PKCS11Constants.NAME_CKM_FORTEZZA_TIMESTAMP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_KEY_GEN), PKCS11Constants.NAME_CKM_BATON_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_ECB128), PKCS11Constants.NAME_CKM_BATON_ECB128);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_ECB96), PKCS11Constants.NAME_CKM_BATON_ECB96);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_CBC128), PKCS11Constants.NAME_CKM_BATON_CBC128);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_COUNTER), PKCS11Constants.NAME_CKM_BATON_COUNTER);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_SHUFFLE), PKCS11Constants.NAME_CKM_BATON_SHUFFLE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_BATON_WRAP), PKCS11Constants.NAME_CKM_BATON_WRAP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_ECDSA_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_ECDSA_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_EC_KEY_PAIR_GEN), PKCS11Constants.NAME_CKM_EC_KEY_PAIR_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_ECDSA), PKCS11Constants.NAME_CKM_ECDSA);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_ECDSA_SHA1), PKCS11Constants.NAME_CKM_ECDSA_SHA1);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_ECDH1_DERIVE), PKCS11Constants.NAME_CKM_ECDH1_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_ECDH1_COFACTOR_DERIVE), PKCS11Constants.NAME_CKM_ECDH1_COFACTOR_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_ECMQV_DERIVE), PKCS11Constants.NAME_CKM_ECMQV_DERIVE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_JUNIPER_KEY_GEN), PKCS11Constants.NAME_CKM_JUNIPER_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_JUNIPER_ECB128), PKCS11Constants.NAME_CKM_JUNIPER_ECB128);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_JUNIPER_CBC128), PKCS11Constants.NAME_CKM_JUNIPER_CBC128);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_JUNIPER_COUNTER), PKCS11Constants.NAME_CKM_JUNIPER_COUNTER);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_JUNIPER_SHUFFLE), PKCS11Constants.NAME_CKM_JUNIPER_SHUFFLE);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_JUNIPER_WRAP), PKCS11Constants.NAME_CKM_JUNIPER_WRAP);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_FASTHASH), PKCS11Constants.NAME_CKM_FASTHASH);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_AES_KEY_GEN), PKCS11Constants.NAME_CKM_AES_KEY_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_AES_ECB), PKCS11Constants.NAME_CKM_AES_ECB);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_AES_CBC), PKCS11Constants.NAME_CKM_AES_CBC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_AES_MAC), PKCS11Constants.NAME_CKM_AES_MAC);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_AES_MAC_GENERAL), PKCS11Constants.NAME_CKM_AES_MAC_GENERAL);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_AES_CBC_PAD), PKCS11Constants.NAME_CKM_AES_CBC_PAD);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DSA_PARAMETER_GEN), PKCS11Constants.NAME_CKM_DSA_PARAMETER_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_DH_PKCS_PARAMETER_GEN), PKCS11Constants.NAME_CKM_DH_PKCS_PARAMETER_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_X9_42_DH_PARAMETER_GEN), PKCS11Constants.NAME_CKM_X9_42_DH_PARAMETER_GEN);
	      mechansimNames.put(new Long(PKCS11Constants.CKM_VENDOR_DEFINED), PKCS11Constants.NAME_CKM_VENDOR_DEFINED);
	      mechansimNames_ = mechansimNames;
	    }

	    Long mechansimCodeObject = new Long(mechansimCode);
	    Object entry = mechansimNames_.get(mechansimCodeObject);

	    String mechanismName = (entry != null)
	                           ? entry.toString()
	                           : "Unknwon mechanism with code: 0x" + toFullHexString(mechansimCode);

	    return mechanismName ;
	  }

	public static String getCRLCacheSystemPath(XComponentContext _xCC) throws Exception, URISyntaxException, IOException {
		String filesep = System.getProperty("file.separator");
		return Helpers.getExtensionStorageSystemPath(_xCC)+
								filesep+GlobConstant.m_sCRL_CACHE_PATH;
    }
    
	public static String getExtensionStorageSystemPath(XComponentContext _xCC) throws Exception, URISyntaxException, IOException {		
		String filesep = System.getProperty("file.separator");
		return getUserStorageSystemPath(_xCC)+filesep+"extdata"+filesep+GlobConstant.m_sEXTENSION_IDENTIFIER;
	}

	public static String getUserStorageSystemPath(XComponentContext _xCC) throws Exception, URISyntaxException, IOException {
		String aPath = getUserStoragePathURL(_xCC);
		return fromURLtoSystemPath(aPath);
	}

	public static String getUserStoragePathURL(XComponentContext _xCC) throws Exception {
		XMultiComponentFactory xMCF = _xCC.getServiceManager();
		Object oPathServ = xMCF.createInstanceWithContext("com.sun.star.util.PathSettings", _xCC);
		if(oPathServ != null){
			XPropertySet xPS = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, oPathServ);
			String aPath = (String)xPS.getPropertyValue("Storage");
			return aPath;
		}
		else
			throw (new Exception("The PathSetting service can not be retrieved") );
	}
	/**
	 * Returns the complete path to the native binary library in the root extension directory
	 * 
	 * @param _xContext
	 * @param _libName
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws Exception 
	 */
	public static String getLocalNativeLibraryPath(XComponentContext _xContext, String _libName)
					throws URISyntaxException, IOException, java.lang.NullPointerException
						{
		String sExtensionSystemPath = Helpers.getExtensionInstallationSystemPath(_xContext)+System.getProperty("file.separator");
		//now add the library name depending on os
        String osName = System.getProperty("os.name");
        if(osName.toLowerCase().indexOf("windows") != -1){
        	// Windows OS detected
        	return sExtensionSystemPath+_libName+".dll";
        } else if(osName.toLowerCase().indexOf("linux") != -1){
            // Linux OS detected
        	return sExtensionSystemPath+"lib"+_libName+".so";
        } else //something else...
        	throw(new java.lang.NullPointerException("Native libraries for '"+osName+"' not available! Giving up."));
	}

	public static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
        	int halfbyte = (data[i] >>> 4) & 0x0F;
        	int two_halfs = 0;
        	do {
	        	if ((0 <= halfbyte) && (halfbyte <= 9))
	                buf.append((char) ('0' + halfbyte));
	            else
	            	buf.append((char) ('a' + (halfbyte - 10)));
	        	halfbyte = data[i] & 0x0F;
        	} while(two_halfs++ < 1);
        }
        return buf.toString();
    }

	/** Return the hex hash code of an object.
	 * 
	 * @param _oObj the object to be examined
	 * @return the returned hash code, in hex
	 */
	public static String getHashHex(Object _oObj) {
		String ret;
		try {
//grab from the Object the has code and returns it
			if(_oObj == null)
				ret ="NULL";
			else
				ret = String.format( "%8H", _oObj );
		} catch (java.lang.Exception e) {
			e.printStackTrace(System.out);
			ret = "caught exception!";
		}
		return ret;
	}

	/**
	 * returns the global data object interface
	 * 
	 * @param xContext
	 * @return
	 * @throws ClassCastException internal Java error
	 * @throws ServiceNotFoundException if the global singleton data service is not available
	 * @throws NoSuchMethodException if the XOX_SingletonDataAccess interface is not available.
	 */
	public static XOX_SingletonDataAccess getSingletonDataAccess(XComponentContext xContext) 
		throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			Object							m_SingletonDataObject = null;
			XOX_SingletonDataAccess			m_xSingletonDataAccess = null;
			m_SingletonDataObject = xContext.getValueByName(GlobConstant.m_sSINGLETON_SERVICE_INSTANCE);
			if(m_SingletonDataObject != null) {
				m_xSingletonDataAccess = (XOX_SingletonDataAccess)UnoRuntime.queryInterface(XOX_SingletonDataAccess.class, m_SingletonDataObject);
				if(m_xSingletonDataAccess == null)
					throw (new NoSuchMethodException("XOX_SingletonDataAccess missing") ); 					
			}
			else
				throw (new ServiceNotFoundException("service "+GlobConstant.m_sSINGLETON_SERVICE+" not found") ); 

			return m_xSingletonDataAccess;
		}
	}

	public static XOX_DocumentSignaturesState initDocumentSignaturesData(XComponentContext xContext, XModel _xModel, XChangesListener _xChg) 
	throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			XOX_SingletonDataAccess		m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
			XOX_DocumentSignaturesState		m_xDocumentSignatures =
								m_xSingletonDataAccess.initDocumentAndListener(Helpers.getHashHex(_xModel), _xChg);
			if(m_xDocumentSignatures == null)
				throw (new NoSuchMethodException("XOX_DocumentSignaturesState missing") ); 									

			return m_xDocumentSignatures;
		}
	}

	public static XOX_DocumentSignaturesState getDocumentSignatures(XComponentContext xContext, XModel _xModel) 
	throws ClassCastException, ServiceNotFoundException, NoSuchMethodException {
		final Boolean	_staticLock = new Boolean(true);
		synchronized(_staticLock) {			
			XOX_SingletonDataAccess		m_xSingletonDataAccess = Helpers.getSingletonDataAccess(xContext);
			XOX_DocumentSignaturesState		m_xDocumentSignatures =
								m_xSingletonDataAccess.getDocumentSignatures(Helpers.getHashHex(_xModel));
			if(m_xDocumentSignatures == null)
				throw (new NoSuchMethodException("XOX_DocumentSignaturesState missing") ); 									

			return m_xDocumentSignatures;
		}
	}
	
	/**
	 * returns the string URL of the path where the extension is installed
	 * @param context
	 * @return
	 */
	public static String getExtensionInstallationPath(XComponentContext context) {
		XPackageInformationProvider xPkgInfo = PackageInformationProvider.get( context );
		if(xPkgInfo != null)
			return xPkgInfo.getPackageLocation( GlobConstant.m_sEXTENSION_IDENTIFIER );
		return null;
	}
	
	public static String fromURLtoSystemPath(String _aUrl) throws URISyntaxException, IOException {
		if(_aUrl != null) {
			URL aURL = new URL(_aUrl);
			URI aUri = new URI(aURL.toString());
			File aFile = new File(aUri);
			return aFile.getCanonicalPath();
		}
		else
			return "";
	}
	
	/**
	 * returns the string URL of the path where the extension is installed
	 * @param context
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static String getExtensionInstallationSystemPath(XComponentContext context) throws URISyntaxException, IOException {
		String aPath = getExtensionInstallationPath(context);
		return fromURLtoSystemPath(aPath);
	}

	/**
	 * @param abyte
	 */
	public static String getCompactHexStringFromString(String _String) {
		//FIXME see if this confersion is ok, what about the char set?
		byte[] ret = _String.getBytes();
		
		String rets = "";
		for(int i=0;i<ret.length;i++) {
			rets = rets+ String.format("%02X", ret[i] );
		}
		return rets;
	}

	public static String printHexBytes(byte[] _theBytes) {
		String _sRet ="";
		for(int i = 0; i < _theBytes.length;i++) {
			if(i !=  0 && i % 16 == 0)
				_sRet = _sRet + " \n";
			try {
				_sRet = _sRet + String.format(" %02X", ( _theBytes[i] & 0xff ) );
			} catch(IllegalFormatException e) {
				e.printStackTrace();
			}
		}
		return _sRet;
	}
	
	/**
	 * converts a list of Integer values included in an Integer vector to a list
	 * of int values
	 * 
	 * 
	 * @param _aIntegerVector
	 * @return
	 */
	/*
	 * public static int[] IntegerTointList(Vector _aIntegerVector){ try {
	 * Integer[] nIntegerValues = new Integer[_aIntegerVector.size()]; int[]
	 * nintValues = new int[_aIntegerVector.size()];
	 * _aIntegerVector.toArray(nIntegerValues); for (int i = 0; i <
	 * nIntegerValues.length; i++) nintValues[i] = nIntegerValues[i].intValue();
	 * return nintValues; } catch (RuntimeException e) {
	 * e.printStackTrace(System.out); return null; } }
	 */

	/**
	 * converts a list of Boolean values included in a Boolean vector to a list
	 * of boolean values
	 * 
	 * 
	 * @param _aBooleanVector
	 * @return
	 */
	/*
	 * public static boolean[] BooleanTobooleanList(Vector _aBooleanVector){ try {
	 * Boolean[] bBooleanValues = new Boolean[_aBooleanVector.size()]; boolean[]
	 * bbooleanValues = new boolean[_aBooleanVector.size()];
	 * _aBooleanVector.toArray(bBooleanValues); for (int i = 0; i <
	 * bBooleanValues.length; i++) bbooleanValues[i] =
	 * bBooleanValues[i].booleanValue(); return bbooleanValues; } catch
	 * (RuntimeException e) { e.printStackTrace(System.out); return null; }}
	 */

	public static String[] multiDimListToArray(String[][] multidimlist) {
		String[] retlist = new String[] {};
		retlist = new String[multidimlist.length];
		for (int i = 0; i < multidimlist.length; i++) {
			retlist[i] = multidimlist[i][0];
		}
		return retlist;
	}

	public static String getlongestArrayItem(String[] StringArray) {
		String sLongestItem = "";
		int FieldCount = StringArray.length;
		int iOldLength = 0;
		int iCurLength = 0;
		for (int i = 0; i < FieldCount; i++) {
			iCurLength = StringArray[i].length();
			if (iCurLength > iOldLength) {
				iOldLength = iCurLength;
				sLongestItem = StringArray[i];
			}
		}
		return sLongestItem;
	}

	public static String ArraytoString(String[] LocArray) {
		String ResultString = "";
		int iLen = LocArray.length;
		for (int i = 0; i < iLen; i++) {
			ResultString += LocArray[i];
			if (i < iLen - 1)
				ResultString += ";";
		}
		return ResultString;
	}

	/**
	 * @author bc93774
	 * @param SearchList
	 * @param SearchString
	 * @return the index of the field that contains the string 'SearchString' or
	 *         '-1' if not it is not contained within the array
	 */
	public static int FieldInList(String[] SearchList, String SearchString) {
		int FieldLen = SearchList.length;
		int retvalue = -1;
		for (int i = 0; i < FieldLen; i++) {
			if (SearchList[i].compareTo(SearchString) == 0) {
				retvalue = i;
				break;
			}
		}
		return retvalue;
	}

	public static int FieldInList(String[] SearchList, String SearchString, int StartIndex) {
		int FieldLen = SearchList.length;
		int retvalue = -1;
		if (StartIndex < FieldLen) {
			for (int i = StartIndex; i < FieldLen; i++) {
				if (SearchList[i].compareTo(SearchString) == 0) {
					retvalue = i;
					break;
				}
			}
		}
		return retvalue;
	}

	public static int FieldInTable(String[][] SearchList, String SearchString) {
		int retvalue;
		if (SearchList.length > 0) {
			int FieldLen = SearchList.length;
			retvalue = -1;
			for (int i = 0; i < FieldLen; i++) {
				if (SearchList[i][0] != null) {
					if (SearchList[i][0].compareTo(SearchString) == 0) {
						retvalue = i;
						break;
					}
				}
			}
		} else
			retvalue = -1;
		return retvalue;
	}

	public static int FieldInIntTable(int[][] SearchList, int SearchValue) {
		int retvalue = -1;
		for (int i = 0; i < SearchList.length; i++) {
			if (SearchList[i][0] == SearchValue) {
				retvalue = i;
				break;
			}
		}
		return retvalue;
	}

	public static int FieldInIntTable(int[] SearchList, int SearchValue, int _startindex) {
		int retvalue = -1;
		for (int i = _startindex; i < SearchList.length; i++) {
			if (SearchList[i] == SearchValue) {
				retvalue = i;
				break;
			}
		}
		return retvalue;
	}

	public static int FieldInIntTable(int[] SearchList, int SearchValue) {
		return FieldInIntTable(SearchList, SearchValue, 0);
	}

	public static int getArraylength(Object[] MyArray) {
		int FieldCount = 0;
		if (MyArray != null)
			FieldCount = MyArray.length;
		return FieldCount;
	}

	/**
	 * @author bc93774 This function bubble sorts an array of with 2 dimensions.
	 *         The default sorting order is the first dimension Only if
	 *         sort2ndValue is True the second dimension is the relevant for the
	 *         sorting order
	 */
	public static String[][] bubblesortList(String[][] SortList) {
		String DisplayDummy;
		int SortCount = SortList[0].length;
		int DimCount = SortList.length;
		for (int s = 0; s < SortCount; s++) {
			for (int t = 0; t < SortCount - s - 1; t++) {
				if (SortList[0][t].compareTo(SortList[0][t + 1]) > 0) {
					for (int k = 0; k < DimCount; k++) {
						DisplayDummy = SortList[k][t];
						SortList[k][t] = SortList[k][t + 1];
						SortList[k][t + 1] = DisplayDummy;
					}
				}
			}
		}
		return SortList;
	}

	/**
	 * @param MainString
	 * @param Token
	 * @return
	 */
	/*
	 * public static String[] ArrayoutofString(String MainString, String Token) {
	 * String[] StringArray; if (MainString.equals("") == false) { Vector
	 * StringVector = new Vector(); String LocString = null; int iIndex; do {
	 * iIndex = MainString.indexOf(Token); if (iIndex < 0)
	 * StringVector.addElement(MainString); else {
	 * StringVector.addElement(MainString.substring(0, iIndex)); MainString =
	 * MainString.substring(iIndex + 1, MainString.length()); } } while (iIndex >=
	 * 0); int FieldCount = StringVector.size(); StringArray = new
	 * String[FieldCount]; StringVector.copyInto(StringArray); } else
	 * StringArray = new String[0]; return StringArray; }
	 */
	public static String replaceSubString(String MainString, String NewSubString,
			String OldSubString) {
		try {
			int NewIndex = 0;
			int OldIndex = 0;
			int NewSubLen = NewSubString.length();
			int OldSubLen = OldSubString.length();
			while (NewIndex != -1) {
				NewIndex = MainString.indexOf(OldSubString, OldIndex);
				if (NewIndex != -1) {
					MainString = MainString.substring(0, NewIndex) + NewSubString
							+ MainString.substring(NewIndex + OldSubLen);
					OldIndex = NewIndex + NewSubLen;
				}
			}
			return MainString;
		} catch (java.lang.Exception exception) {
			exception.printStackTrace(System.out);
			return null;
		}
	}

	/*
	 * public static String getFilenameOutOfPath(String sPath){ String[]
	 * Hierarchy = ArrayoutofString(sPath, "/"); return
	 * Hierarchy[Hierarchy.length - 1]; }
	 * 
	 * 
	 * public static String getFileDescription(String sPath){ String sFilename =
	 * getFilenameOutOfPath(sPath); String[] FilenameList =
	 * ArrayoutofString(sFilename, "."); String FileDescription = ""; for (int i =
	 * 0; i < FilenameList.length - 1; i++) { FileDescription +=
	 * FilenameList[i]; } return FileDescription; }
	 */

	public static long getTimeInMillis(Calendar _calendar) {
		java.util.Date dDate = _calendar.getTime();
		return dDate.getTime();
	}

	public static void setTimeInMillis(Calendar _calendar, long _timemillis) {
		java.util.Date dDate = new java.util.Date();
		dDate.setTime(_timemillis);
		_calendar.setTime(dDate);
	}

	public static long getMillis(DateTime time) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.set(time.Year, time.Month, time.Day, time.Hours, time.Minutes, time.Seconds);
		return getTimeInMillis(cal);
	}

	/**
	 * searches a multidimensional array for duplicate fields. According to the
	 * following example SlaveFieldName1 ;SlaveFieldName2; SlaveFieldName3
	 * MasterFieldName1;MasterFieldName2;MasterFieldName3 The entries
	 * SlaveFieldNameX and MasterFieldNameX are grouped together and then the
	 * created groups are compared If a group is duplicate the entry of the
	 * second group is returned.
	 * 
	 * @param _scomplist
	 * @return
	 */
	public static int getDuplicateFieldIndex(String[][] _scomplist) {
		int retvalue = -1;
		if (_scomplist.length > 0) {
			int fieldcount = _scomplist[0].length;
			String[] sDescList = new String[fieldcount];
			for (int m = 0; m < fieldcount; m++) {
				for (int n = 0; n < _scomplist.length; n++) {
					if (n == 0)
						sDescList[m] = new String();
					sDescList[m] += _scomplist[n][m];
				}
			}
			return getDuplicateFieldIndex(sDescList);
		}
		return retvalue;
	}

	/**
	 * not tested!!!!!
	 * 
	 * @param scomplist
	 * @return
	 */
	public static int getDuplicateFieldIndex(String[] scomplist) {
		for (int n = 0; n < scomplist.length; n++) {
			String scurvalue = scomplist[n];
			for (int m = n; m < scomplist.length; m++) {
				if (m != n) {
					if (scurvalue.equals(scomplist[m]))
						return m;
				}
			}
		}
		return -1;
	}

	public static int getDuplicateFieldIndex(String[] _scomplist, String _fieldname) {
		int iduplicate = 0;
		for (int n = 0; n < _scomplist.length; n++) {
			if (_scomplist[n].equals(_fieldname)) {
				iduplicate++;
				if (iduplicate == 2) {
					return n;
				}
			}
		}
		return -1;
	}

	public static boolean isEqual(PropertyValue firstPropValue, PropertyValue secPropValue) {
		if (!firstPropValue.Name.equals(secPropValue.Name))
			return false;
		// TODO replace 'equals' with
		// AnyConverter.getType(firstpropValue).equals(secPropValue) to check
		// content and Type

		if (!firstPropValue.Value.equals(secPropValue.Value))
			return false;
		return (firstPropValue.Handle == secPropValue.Handle);
	}

	public static int[] getDuplicateFieldIndex(PropertyValue[][] ocomplist) {
		for (int n = 0; n < ocomplist.length; n++) {
			PropertyValue[] ocurValue = ocomplist[n];
			for (int m = n; m < ocurValue.length; m++) {
				PropertyValue odetValue = ocurValue[m];
				for (int s = 0; s < ocurValue.length; s++) {
					if (s != m) {
						if (isEqual(odetValue, ocurValue[s]))
							return new int[] { n, s };
					}
				}
			}
		}
		return new int[] { -1, -1 };
	}

	public static String getSuffixNumber(String _sbasestring) {
		int suffixcharcount = 0;
		for (int i = _sbasestring.length() - 1; i >= 0; i--) {
			char b = _sbasestring.charAt(i);
			if ((b >= '0') && (b <= '9'))
				suffixcharcount++;
			else
				break;
		}
		int istart = _sbasestring.length() - suffixcharcount;
		return _sbasestring.substring(istart, _sbasestring.length());
	}

	/**
	 * compares two strings. If one of them is empty and the other one is null
	 * it also returns true
	 * 
	 * @param sFirstString
	 * @param sSecondString
	 * @return
	 */
	public static boolean isSame(String sFirstString, String sSecondString) {
		boolean bissame = false;
		if (sFirstString == null) {
			if (sSecondString != null)
				bissame = sSecondString.equals("");
			else
				bissame = (sSecondString == null);
		} else {
			if (sFirstString.equals(""))
				bissame = (sSecondString == null);
			else if (sSecondString != null)
				bissame = sFirstString.equals(sSecondString);
		}
		return bissame;
	}
	
	/**
	 * return the com.sun.star.util.Color formed from the fundamental color
	 * the color is an object of typecom.sun.star.util.Color (a long)
	 * its bytes are: ignore, RGB:red,RGB:green,RGB:blue, hence grey will be:
	 *  127*256*256+127*256+127
	 * @param nRed
	 * @param nGreen
	 * @param nBlue
	 * @return
	 */
	public static int getRGBColor(int nRed, int nGreen, int nBlue) {
		return (nRed*256*256+nGreen*256+nBlue);
	}

	/*    *//**
			 * Embeds the given Image into a Textdocument at the given cursor
			 * position (Anchored as character)
			 * 
			 * @param grProps
			 *            OOo-style URL and width &amp; height of the graphic
			 * @param xMSF
			 *            the factory to create services from
			 * @param xCursor
			 *            the cursor where to insert the graphic
			 */
	/*
	 * private void embedGraphic(GraphicInfo grProps, XMultiServiceFactory xMSF,
	 * XTextCursor xCursor) {
	 * 
	 * XNameContainer xBitmapContainer = null; XText xText = xCursor.getText();
	 * XTextContent xImage = null; String internalURL = null;
	 * 
	 * try { xBitmapContainer = (XNameContainer) UnoRuntime.queryInterface(
	 * XNameContainer.class, xMSF.createInstance(
	 * "com.sun.star.drawing.BitmapTable")); xImage = (XTextContent)
	 * UnoRuntime.queryInterface( XTextContent.class, xMSF.createInstance(
	 * "com.sun.star.text.TextGraphicObject")); XPropertySet xProps =
	 * (XPropertySet) UnoRuntime.queryInterface( XPropertySet.class, xImage);
	 *  // helper-stuff to let OOo create an internal name of the graphic //
	 * that can be used later (internal name consists of various checksums)
	 * xBitmapContainer.insertByName("someID", grProps.unoURL); internalURL =
	 * AnyConverter.toString(xBitmapContainer .getByName("someID"));
	 * 
	 * xProps.setPropertyValue("AnchorType",
	 * com.sun.star.text.TextContentAnchorType.AS_CHARACTER);
	 * xProps.setPropertyValue("GraphicURL", internalURL);
	 * xProps.setPropertyValue("Width", (int) grProps.widthOfGraphic);
	 * xProps.setPropertyValue("Height", (int) grProps.heightOfGraphic);
	 *  // inser the graphic at the cursor position
	 * xText.insertTextContent(xCursor, xImage, false);
	 *  // remove the helper-entry xBitmapContainer.removeByName("someID"); }
	 * catch (Exception e) { System.out.println("Failed to insert Graphic"); } }
	 */

}
