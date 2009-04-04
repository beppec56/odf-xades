/*************************************************************************
 * 
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  
 *  The Contents of this file are made available subject to
 *  the terms of European Union Public License (EUPL) as published
 *  by the European Community, either version 1.1 of the License,
 *  or any later version.
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

package it.plio.ext.xades.ooo.pack;

import java.util.Vector;

import com.sun.star.embed.ElementModes;
import com.sun.star.embed.StorageWrappedTargetException;
import com.sun.star.embed.XStorage;
import com.sun.star.io.IOException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 * to test is the package can be written to
 */
public class TestWriteDigitalSignature {
	
	public TestWriteDigitalSignature() {
		
	}
	
	public void testWriteSignatureStream(String aTheDocURL, XMultiComponentFactory _xMCF, XComponentContext _xCompCtx) {
		//from url the storage
		
		//try to upen the META-INF substorage for writing
		
		try {
			Object oObj = _xMCF.createInstanceWithContext("com.sun.star.embed.StorageFactory", _xCompCtx);
			if(oObj != null) {
				XSingleServiceFactory xStorageFactory =
					(XSingleServiceFactory)UnoRuntime.queryInterface(XSingleServiceFactory.class,oObj);
	            Object args[]=new Object[2];
	            args[0] = aTheDocURL;
	            args[1] = ElementModes.READ;
	            Object oMyStorage = xStorageFactory.createInstanceWithArguments(args); // the storage service
	            if(oMyStorage != null) {
		           	XStorage xThePackage = (XStorage) UnoRuntime.queryInterface( XStorage.class, oMyStorage );
		           	if(xThePackage != null) {
		           		//open the META-INF subpackage
		           		try {
		           			XStorage xSubStore = xThePackage.openStorageElement("META-INF", ElementModes.READWRITE);
		           		
		           			// note that the file manifest.xml is not presented 
		           			// we only get the other files
		           			String[] sNames = xSubStore.getElementNames();
		           			
		           			for(int i = 0; i < sNames.length;i++)
		           				System.out.println(sNames[i]);
		           			
		         	           			
		           			xSubStore.dispose();
		           		}
		        		catch (IOException e) {
		        			//no problem if not existent
		        			e.printStackTrace();
		        		} catch (StorageWrappedTargetException e) {
		        			// TODO Auto-generated catch block
		        			//no problem if not existent
		        			e.printStackTrace();
		        		} catch (IllegalArgumentException e) {
		        			// TODO Auto-generated catch block
		        			//no problem if not existent
		        			e.printStackTrace();
		        		}
		        		xThePackage.dispose();
		           	}

	            }
	            else
	            	System.out.println("No storage");

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
