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

package it.plio.ext.oxsit.ooo.registry;

import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;
import it.plio.ext.oxsit.Helpers;
import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.ConfigurationAccess;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.IGeneralConfigurationProcessor;
import it.plio.ext.oxsit.pcsc.CardInfoOOo;

import com.sun.star.configuration.XTemplateInstance;
import com.sun.star.container.XElementAccess;
import com.sun.star.container.XHierarchicalName;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNamed;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

/**
 * @author beppe
 *
 */
public class SSCDsConfigurationAccess extends ConfigurationAccess {

	private Object m_oAllFramesConfView;
	private IDynamicLogger m_aLogger;
	private boolean isLinux;
	private boolean isWindows;
	private boolean isMac;
	  
	  /**
	 * @param context
	 */
	public SSCDsConfigurationAccess(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		super(_xContext);
		// TODO Auto-generated constructor stub
		
		m_aLogger = new DynamicLogger(this,_xContext);
//		m_aLogger.enableLogging();
		
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("win") > -1) {
            isWindows = true;
        }
        if (osName.toLowerCase().indexOf("linux") > -1) {
            isLinux = true;
        }
        if (osName.toLowerCase().indexOf("mac") > -1) {
            isMac = true;
        }
	}

	public CardInfoOOo[] readSSCDConfiguration() {
//open the registry as readonly
	      try {
			XInterface xViewRoot = (XInterface)createConfigurationReadOnlyView(GlobConstant.m_sEXTENSION_CONF_SSCDS);
			
			//access the node, and see if there are elements
			//the name here is used to display the path for debug, can be commented
/*		      XHierarchicalName xElementPath = (XHierarchicalName) UnoRuntime.queryInterface(
			          XHierarchicalName.class, xViewRoot);
		      String sPath = xElementPath.getHierarchicalName();*/
		      
		      XNameAccess xChildAccess =
		          (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xViewRoot);

		      // get a list of child elements
		      String[] aElementNames = xChildAccess.getElementNames();
		      if(aElementNames == null || aElementNames.length == 0) {
		    	  m_aLogger.info("readSSCDConfiguration","no elements");
		    	  return null;
		      }

		      CardInfoOOo[] retElements = new CardInfoOOo[aElementNames.length];

		      // and process them one by one
		      for (int i=0; i< aElementNames.length; ++i) {
		    	  m_aLogger.info(aElementNames[i]);
		    	  CardInfoOOo aCardInfo = new CardInfoOOo();
		    	  aCardInfo.setATRCode(aElementNames[i]);
		          Object aChild = xChildAccess.getByName(aElementNames[i]);

		          // is it a structural element (object) ...
		          if ( aChild instanceof XInterface ) {
		              // then get an interface 
		              XInterface xSSCDElement = (XInterface)aChild;
		              // and continue processing child elements recursively
		              exploreRegistryRecursively(xSSCDElement,
		            		  new IGeneralConfigurationProcessor() {
								@Override
								public void processStructuralElement(String path_,
										XInterface element_) {
									//simply print, for now
/*									  XTemplateInstance xInstance = 
										  ( XTemplateInstance )UnoRuntime.queryInterface( XTemplateInstance.class,element_);

										  XNamed xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class,element_);
										  m_aLogger.info("== " + xNamed.getName() + " (" + path_ + ")");*/				
								}
								@Override
								public void processValueElement(String path_,
										Object value_, Object _aObject) {
									CardInfoOOo fill = (CardInfoOOo)_aObject;
									// TODO Auto-generated method stub
									//simply print for now
									//the element should be added to the CardInfoOOo parameter
//									m_aLogger.info("\tValue: '" + path_ + "' = " + value_);
									 if ( path_.endsWith("]/Description") )
										 fill.m_sDescription = value_.toString();
									 else if ( path_.endsWith("]/Manufacturer") )
										 fill.m_sManufacturer = value_.toString();
									 else if ( path_.endsWith("]/CardType") )
										 fill.m_sCardType = value_.toString();
									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/LibName") &&
											 isLinux)
										 fill.setOsLib(value_.toString());
									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/LibNameAlt1") &&
											 isLinux)
										 fill.setOsLibAlt1(value_.toString());
									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/LibNameAlt2") &&
											 isLinux)
										 fill.setOsLibAlt2(value_.toString());
/*									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/res1") )
										 fill.m_sOsRes1[CardInfoOOo.m_sOS_LINUX] = value_.toString();
									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/res2") )
										 fill.m_sOsRes2[CardInfoOOo.m_sOS_LINUX] = value_.toString();*/
									 else if ( path_.endsWith("]/OsData/OsType['OsWindows']/LibName") &&
											 isWindows)
										 fill.setOsLib(value_.toString());
									 else if ( path_.endsWith("]/OsData/OsType['OsWindows']/LibNameAlt1") &&
											 isWindows)
										 fill.setOsLibAlt1(value_.toString());
									 else if ( path_.endsWith("]/OsData/OsType['OsWindows']/LibNameAlt2") &&
											 isWindows)
										 fill.setOsLibAlt2(value_.toString());
									 else if (path_.endsWith("]/OsData/OsType['OsMac']/LibName") &&
											 isMac)
										 fill.setOsLib(value_.toString());
									 else if (path_.endsWith("]/OsData/OsType['OsMac']/LibNameAlt1") &&
											 isMac)
										 fill.setOsLibAlt1(value_.toString());
									 else if (path_.endsWith("]/OsData/OsType['OsMac']/LibNameAlt2") &&
											 isMac)
										 fill.setOsLibAlt2(value_.toString());
								}},
								aCardInfo);
		          }

		          //check and set the library available on system
		          String Pkcs11WrapperLocal = Helpers.getPKCS11WrapperNativeLibraryPath(m_xContext);
		          try {
		        	  aCardInfo.detectDefaultLib(Pkcs11WrapperLocal);
					} catch (NoSuchMethodError e) {
						m_aLogger.severe(e);
					}
		          m_aLogger.log(aCardInfo.toString());
		          retElements[i] = aCardInfo;
		      }
			
		      ((XComponent) UnoRuntime.queryInterface(XComponent.class,xViewRoot)).dispose();			
		      return retElements;
		} catch (Throwable e) {
			m_aLogger.severe(e);
		}
		return null;
	}

	public void printRegisteredSSCDs() {
	      try {
				XInterface xViewRoot = (XInterface)createConfigurationReadOnlyView(GlobConstant.m_sEXTENSION_CONF_SSCDS);
				
				//access the node, and see if there are elements
			      XHierarchicalName xElementPath = (XHierarchicalName) UnoRuntime.queryInterface(
				          XHierarchicalName.class, xViewRoot);
			      String sPath = xElementPath.getHierarchicalName();
		    	  m_aLogger.info("Start: "+sPath);

		    	  XNameAccess xChildAccess =
			          (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xViewRoot);

			      // get a list of child elements
			      String[] aElementNames = xChildAccess.getElementNames();
			      if(aElementNames == null || aElementNames.length == 0) {
			    	  m_aLogger.info("printRegisteredSSCDs","no elements");
			      }

			      // and process them one by one
			      for (int i=0; i< aElementNames.length; ++i) {
			    	  m_aLogger.info(aElementNames[i]);
			          Object aChild = xChildAccess.getByName(aElementNames[i]);

			          // is it a structural element (object) ...
			          if ( aChild instanceof XInterface ) {
			              // then get an interface 
			              XInterface xSSCDElement = (XInterface)aChild;
			              // and continue processing child elements recursively
			              exploreRegistryRecursively(xSSCDElement,
			            		  new IGeneralConfigurationProcessor() {
									@Override
									public void processStructuralElement(String path_,
											XInterface element_) {
										//simply print, for now
										  XTemplateInstance xInstance = 
											  ( XTemplateInstance )UnoRuntime.queryInterface( XTemplateInstance.class,element_);
											  XNamed xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class,element_);
											  m_aLogger.info("== " + xNamed.getName() + " (" + path_ + ")");				
									}
									@Override
									public void processValueElement(String path_,
											Object value_, Object _aObject) {
										CardInfoOOo fill = (CardInfoOOo)_aObject;
										m_aLogger.info("\tValue: '" + path_ + "' = " + value_);
									}},
									null);
			          }
			      }
				
			      ((XComponent) UnoRuntime.queryInterface(XComponent.class,xViewRoot)).dispose();			
			} catch (Throwable e) {
				m_aLogger.severe(e);
			}
	}
	
	public void readConfig() {
	
		//try to open the configuration first

		try {
//			m_oAllFramesConfView = createConfigurationReadWriteView( GlobConstant.m_sEXTENSION_CONF_SSCDS );
			m_oAllFramesConfView = createConfigurationReadOnlyView(
					"it.plio.ext.oxsit.Configuration/SSCDs/aSSCD" );
//		"it.plio.ext.oxsit.Configuration/SSCDs/" );
			
			if(m_oAllFramesConfView != null) {
				
				Utilities.showInterfaces(this, m_oAllFramesConfView);
				
				//try to access the node container and see if it contains the requested frame
				XNameAccess xNAccess = (XNameAccess) UnoRuntime.queryInterface(
						XNameAccess.class, m_oAllFramesConfView );

				String[] sN = xNAccess.getElementNames();
				for(int i=0; i< sN.length;i++) {
					out(sN[i]);
				}
				XElementAccess xNC =  (XElementAccess) UnoRuntime.queryInterface(
						XElementAccess.class, m_oAllFramesConfView );
				
				out((xNC.hasElements())? "yes":"no");
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			out(e);
		}
		
	}

	public void dispose() {
		synchronized (this) {
			if (m_oAllFramesConfView != null) {
				( (XComponent) UnoRuntime.queryInterface( XComponent.class,
						m_oAllFramesConfView ) ).dispose();
				m_oAllFramesConfView = null;
			}
		}
	}	
    private void out(String line) {
    	System.out.println(line);	
    }	

    private void out(Throwable e) {
    	System.out.println(e);	
    }
}
