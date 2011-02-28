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

package com.yacme.ext.oxsit.ooo.pack;

import com.sun.star.io.XActiveDataSink;
import com.sun.star.io.XInputStream;

/**
 *
 * @author beppe
 */
public class PackActiveDataSink implements XActiveDataSink {
   /**
      * Member properties
      */
     XInputStream m_aStream = null;
 
     /**
      * Constructor
      */        
     public PackActiveDataSink() {
         super();
     }
         
     /**
      *   Plugs the input stream.
      *
      *@param  XInputStream
      */
    
    public void setInputStream(XInputStream aStream) {
        m_aStream = aStream;
    }

    public XInputStream getInputStream() {
        return m_aStream;
    }
}
