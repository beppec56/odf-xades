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

package it.plio.ext.oxsit.options;

import it.plio.ext.oxsit.logging.XDynamicLogger;
import it.plio.ext.oxsit.ooo.ConfigurationAccess;
import it.plio.ext.oxsit.ooo.GlobConstant;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Access to configurations options (Tools > Options...)
 * @author beppe
 *
 */
public class OptionsParametersAccess extends ConfigurationAccess  implements XComponent {
	private Object	m_oOptionsParametersRegKey	= null;

	/**
	 * @param _Context
	 */
	public OptionsParametersAccess(XComponentContext _Context) {
		super(_Context);
		try {
			m_oOptionsParametersRegKey = createConfigurationReadWriteView( GlobConstant.m_sEXTENSION_CONF_OPTIONS );
		} catch (Exception e) {
			m_logger.severe("ctor", "", e);
		} finally {
		}
	}

	public String getText(String sTheName) {
		String retVal = null;
		if(m_oOptionsParametersRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oOptionsParametersRegKey );
/*				String[] elements = xNAccess.getElementNames();
				System.out.print( "Available property names: " );

				for (int i = 0; i < elements.length; i++) {
					System.out.print( elements[i] + ", " );
				}
				System.out.print( "\n" );*/

				if (xNAccess.hasByName( sTheName )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oOptionsParametersRegKey );
					retVal = AnyConverter.toString( xPS.getPropertyValue( sTheName ) );
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}

	public void setText(String sTheName, String sStr) {
		if(m_oOptionsParametersRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oOptionsParametersRegKey );
				if (xNAccess.hasByName( sTheName )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oOptionsParametersRegKey );
					xPS.setPropertyValue(sTheName, sStr);
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean getBoolean(String sTheName) {
		boolean retVal = false;
		if(m_oOptionsParametersRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oOptionsParametersRegKey );

				if (xNAccess.hasByName( sTheName )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oOptionsParametersRegKey );
					retVal = AnyConverter.toBoolean( xPS.getPropertyValue( sTheName ) );
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}

	public void setBoolean(String sTheName, boolean bBool) {
		if(m_oOptionsParametersRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oOptionsParametersRegKey );

				if (xNAccess.hasByName( sTheName )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oOptionsParametersRegKey );
					xPS.setPropertyValue(sTheName, new Boolean(bBool));
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//			System.out.println("getText, sTheName: "+sTheName);
			catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getNumber(String sTheName) {
		int retVal = 0;
		if(m_oOptionsParametersRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oOptionsParametersRegKey );
				if (xNAccess.hasByName( sTheName )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oOptionsParametersRegKey );
					retVal = AnyConverter.toInt( xPS.getPropertyValue( sTheName ) );
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}

	public void setNumber(String sTheName, int nNum) {
		if(m_oOptionsParametersRegKey != null) {
			try {
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oOptionsParametersRegKey );

				if (xNAccess.hasByName( sTheName )) {
					XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(
							XPropertySet.class, m_oOptionsParametersRegKey );
					xPS.setPropertyValue(sTheName, new Integer(nNum));
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WrappedTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//			System.out.println("getText, sTheName: "+sTheName);
			catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		synchronized (this) {
			if(m_oOptionsParametersRegKey != null) {
		        // now clean up
		        ((XComponent) UnoRuntime.queryInterface(XComponent.class, m_oOptionsParametersRegKey)).dispose();
		        m_oOptionsParametersRegKey = null;
			}
		}
	}

	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
		System.out.println(this.getClass().getName()+" removeEventListener");		
	}
	public void commit() {
		commitChanges(m_oOptionsParametersRegKey);
	}
}
