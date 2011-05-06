/*
 * SAXSignedDocException.java
 * DESCRIPTION: Digi Doc functions for creating
 *	and reading signed documents. 
 * AUTHOR:  Veiko Sinivee, S|E|B IT Partner Estonia
 *==================================================
 * Copyright (C) AS Sertifitseerimiskeskus
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * GNU Lesser General Public Licence is available at
 * http://www.gnu.org/copyleft/lesser.html
 *==================================================
 */

package com.yacme.ext.oxsit.cust_it.comp.security.xades.factory;

import org.xml.sax.SAXException;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;

import java.io.IOException;

/**
 * SAXExcepton subclass, that
 * has the same data as SignedDocException
 * @author  Veiko Sinivee
 * @modified  Roberto Resoli
 * @version 1.0
 */
public class SAXSignedDocException extends SAXException 
{
    private int m_code;
    private Throwable m_detail;
    
    /** Creates new SAXSignedDocException */
    public SAXSignedDocException(int code, String msg) 
    {
        super(msg);
        m_code = code;  
        m_detail = null;
    }

    /**
     * Accessor for error code
     * @return error code
     */
    public int getCode() {
        return m_code;
    }
    
    /**
     * Accessor for nested exception
     * @return nested exception
     */
    public Throwable getNestedException() {
        return m_detail;
    }
    
    /**
     * Mutator for nested exception
     * @param detail nested exception
     */
    public void setNestedException(Throwable t) {
        m_detail = t;
    }

    /**
     * Factory method to handle exceptions
     * @param ex Exception object to use
     * @param code error code
     */
    public static void handleException(SignedDocException ex)
        throws SAXSignedDocException 
    {        
        SAXSignedDocException ex1 = 
            new SAXSignedDocException(ex.getCode(), ex.getMessage());
        if(ex.getNestedException() != null)
            ex1.setNestedException(ex.getNestedException());
        throw ex1;
    }
    
    /**
     * Factory method to handle excetions
     * @param ex Exception object to use
     * @param code error code
     */
    public static void handleException(IOException ex)
        throws SAXSignedDocException 
    {        
        SAXSignedDocException ex1 = 
            new SAXSignedDocException(SignedDocException.ERR_WRITE_FILE, ex.getMessage());
        ex1.setNestedException(ex);
        throw ex1;
    }
    
    /**
     * Converts this exception to an equivalent 
     * SignedDocException
     * @return SignedDocException
     */
    public SignedDocException getSignedDocException()
    {
        return new SignedDocException(m_code, getMessage(), m_detail);
    }
}
