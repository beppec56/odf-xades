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
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/SignatureProductionPlace.
 *
 * The Initial Developer of the Original Code is
 * AUTHOR:  Veiko Sinivee, S|E|B IT Partner Estonia
 * Copyright (C) AS Sertifitseerimiskeskus
 * from which ideas and part of the code are derived
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * FIXME add Rob e-mail
 * Roberto Resoli 
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

package com.yacme.ext.oxsit.cust_it.comp.security.xades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Models the SignatureProductionPlace element of
 * an XML-DSIG/ETSI Signature.
 * @author  Veiko Sinivee
 * @version 1.0
 */
public class SignatureProductionPlace implements Serializable {
    /** city name */
    private String m_city;
    /** state name */
    private String m_state;
    /** county name */
    private String m_country;
    /** postal code */
    private String m_zip;
    
    /** 
     * Creates new SignatureProductionPlace 
     * Initializes everything to null
     */
    public SignatureProductionPlace() {
        m_city = null;
        m_state = null;
        m_country = null;
        m_zip = null;
    }
    
    /** 
     * Creates new SignatureProductionPlace 
     * @param city city name
     * @param state state or province name
     * @param country country name
     * @param zip postal code
     */
    public SignatureProductionPlace(String city, String state, 
        String country, String zip) 
    {
        m_city = city;
        m_state = state;
        m_country = country;
        m_zip = zip;
    }

    /**
     * Accessor for city attribute
     * @return value of city attribute
     */
    public String getCity() {
        return m_city;
    }
    
    /**
     * Mutator for city attribute
     * @param str new value for city attribute
     */    
    public void setCity(String str) 
    {
        m_city = str;
    }
    
    /**
     * Accessor for stateOrProvince attribute
     * @return value of stateOrProvince attribute
     */
    public String getStateOrProvince() {
        return m_state;
    }
    
    /**
     * Mutator for stateOrProvince attribute
     * @param str new value for stateOrProvince attribute
     */    
    public void setStateOrProvince(String str) 
    {
        m_state = str;
    }

    /**
     * Accessor for countryName attribute
     * @return value of countryName attribute
     */
    public String getCountryName() {
        return m_country;
    }
    
    /**
     * Mutator for countryName attribute
     * @param str new value for countryName attribute
     */    
    public void setCountryName(String str) 
    {
        m_country = str;
    }

    /**
     * Accessor for postalCode attribute
     * @return value of postalCode attribute
     */
    public String getPostalCode() {
        return m_zip;
    }
    
    /**
     * Mutator for postalCode attribute
     * @param str new value for postalCode attribute
     */    
    public void setPostalCode(String str) 
    {
        m_zip = str;
    }

    /**
     * Converts the SignatureProductionPlace to XML form
     * @return XML representation of SignatureProductionPlace
     */
    public byte[] toXML()
        throws SignedDocException
    {
        ByteArrayOutputStream bos = 
            new ByteArrayOutputStream();        
        // just in case ...
        // make sure we only output if there is any data
        try {
        if(m_city != null || m_state != null || 
           m_zip != null || m_country != null) {
               bos.write(ConvertUtils.str2data("<SignatureProductionPlace>\n"));
            if(m_city != null) {
                bos.write(ConvertUtils.str2data("<City>"));
                bos.write(ConvertUtils.str2data(m_city));
                bos.write(ConvertUtils.str2data("</City>\n"));
            }
            if(m_state != null) {
                bos.write(ConvertUtils.str2data("<StateOrProvince>"));
                bos.write(ConvertUtils.str2data(m_state));
                bos.write(ConvertUtils.str2data("</StateOrProvince>\n"));
            }
            if(m_zip != null) {
                bos.write(ConvertUtils.str2data("<PostalCode>"));
                bos.write(ConvertUtils.str2data(m_zip));
                bos.write(ConvertUtils.str2data("</PostalCode>\n"));
            }
            if(m_country != null) {
                bos.write(ConvertUtils.str2data("<CountryName>"));
                bos.write(ConvertUtils.str2data(m_country));
                bos.write(ConvertUtils.str2data("</CountryName>\n"));
            }
            bos.write(ConvertUtils.str2data("</SignatureProductionPlace>"));
        }
        } catch(IOException ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_XML_CONVERT);
        }
        return bos.toByteArray();
    }

    /**
     * Returns the stringified form of SignatureProductionPlace
     * @return SignatureProductionPlace string representation
     */
    public String toString() {
        String str = null;
        try {
            str = new String(toXML());
        } catch(Exception ex) {}
        return str;
    }    
}
