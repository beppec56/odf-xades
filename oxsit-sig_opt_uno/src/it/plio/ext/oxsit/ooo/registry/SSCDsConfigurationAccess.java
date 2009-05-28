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

import java.util.Properties;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.IDynamicLogger;
import it.plio.ext.oxsit.ooo.ConfigurationAccess;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.pcsc.CardInfoOOo;

import com.sun.star.beans.Property;
import com.sun.star.beans.XProperty;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.configuration.XTemplateInstance;
import com.sun.star.container.XContainer;
import com.sun.star.container.XElementAccess;
import com.sun.star.container.XHierarchicalName;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
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
	private String m_sStartPath;
	private IDynamicLogger m_aLogger;
	private boolean isLinux;
	private boolean isWindows;
	private boolean isMac;
	  
	  /**
	 * @param context
	 */
	public SSCDsConfigurationAccess(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		super(_xContext);
		m_sStartPath = GlobConstant.m_sEXTENSION_CONF_SSCDS;
		// TODO Auto-generated constructor stub
		
		m_aLogger = new DynamicLogger(this,_xContext);
		m_aLogger.enableLogging();
		
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

	private interface ISSCDConfigurationProcessor {
      // process a value item
		public abstract void processValueElement(String sPath_, Object aValue_, CardInfoOOo _theFill);
  // process a structural item
	  	public abstract void processStructuralElement(String sPath_, XInterface xElement_);
	};

	private void exploreSSCDRecursively(XInterface _viewRoot, ISSCDConfigurationProcessor _aProcessor, CardInfoOOo _aCardInfo) 
	throws com.sun.star.uno.Exception {
		// First process this as an element (preorder traversal)
		XHierarchicalName xElementPath = (XHierarchicalName) UnoRuntime.queryInterface(
				XHierarchicalName.class, _viewRoot);

		String sPath = xElementPath.getHierarchicalName();

		//call configuration processor object
		_aProcessor.processStructuralElement(sPath, _viewRoot);

		// now process this as a container of named elements
		XNameAccess xChildAccess =
			(XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, _viewRoot);

		// get a list of child elements
		String[] aElementNames = xChildAccess.getElementNames();

		// and process them one by one
		for (int i=0; i< aElementNames.length; ++i) {
			Object aChild = xChildAccess.getByName(aElementNames[i]);

			// is it a structural element (object) ...
			if ( aChild instanceof XInterface ) {
				// then get an interface 
				XInterface xChildElement = (XInterface)aChild;
				// and continue processing child elements recursively
				exploreSSCDRecursively(xChildElement, _aProcessor,_aCardInfo);
			}
			// ... or is it a simple value
			else {
				// Build the path to it from the path of 
				// the element and the name of the child
				String sChildPath;
				sChildPath = xElementPath.composeHierarchicalName(aElementNames[i]);
				// and process the value
				_aProcessor.processValueElement(sChildPath, aChild,_aCardInfo);
			}
		}
	}
	
	public CardInfoOOo[] readSSCDConfiguration() {
//open the registry as readonly
	      try {
			XInterface xViewRoot = (XInterface)createConfigurationReadOnlyView(GlobConstant.m_sEXTENSION_CONF_SSCDS);
			
			//access the node, and see if there are elements
		      XHierarchicalName xElementPath = (XHierarchicalName) UnoRuntime.queryInterface(
			          XHierarchicalName.class, xViewRoot);
		      String sPath = xElementPath.getHierarchicalName();
		      
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
		    	  aCardInfo.m_sATRCode = aElementNames[i];
		          Object aChild = xChildAccess.getByName(aElementNames[i]);

		          // is it a structural element (object) ...
		          if ( aChild instanceof XInterface ) {
		              // then get an interface 
		              XInterface xSSCDElement = (XInterface)aChild;
		              // and continue processing child elements recursively
		              exploreSSCDRecursively(xSSCDElement,
		            		  new ISSCDConfigurationProcessor() {
								@Override
								public void processStructuralElement(String path_,
										XInterface element_) {
									// TODO Auto-generated method stub
									//simply print, for now
									  XTemplateInstance xInstance = 
										  ( XTemplateInstance )UnoRuntime.queryInterface( XTemplateInstance.class,element_);

										  XNamed xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class,element_);
//										  m_aLogger.info("== " + xNamed.getName() + " (" + path_ + ")");				
								}
								@Override
								public void processValueElement(String path_,
										Object value_, CardInfoOOo fill) {
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
										 fill.m_sOsLib = value_.toString();
/*									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/res1") )
										 fill.m_sOsRes1[CardInfoOOo.m_sOS_LINUX] = value_.toString();
									 else if ( path_.endsWith("]/OsData/OsType['OsLinux']/res2") )
										 fill.m_sOsRes2[CardInfoOOo.m_sOS_LINUX] = value_.toString();*/
									 else if ( path_.endsWith("]/OsData/OsType['OsWindows']/LibName") &&
											 isWindows)
										 fill.m_sOsLib = value_.toString();
/*									 else if (path_.endsWith("]/OsData/OsType['OsWindows']/res1") )
										 fill.m_sOsRes1[CardInfoOOo.m_sOS_WINDOWS] = value_.toString();
									 else if (path_.endsWith("]/OsData/OsType['OsWindows']/res2") )
										 fill.m_sOsRes2[CardInfoOOo.m_sOS_WINDOWS] = value_.toString();*/
									 else if (path_.endsWith("]/OsData/OsType['OsMac']/LibName") &&
											 isMac)
										 fill.m_sOsLib = value_.toString();
/*									 else if (path_.endsWith("]/OsData/OsType['OsMac']/res1") )
										 fill.m_sOsRes1[CardInfoOOo.m_sOS_MAC] = value_.toString();
									 else if (path_.endsWith("]/OsData/OsType['OsMac']/res2") )
										 fill.m_sOsRes2[CardInfoOOo.m_sOS_MAC] = value_.toString();*/
								}},
								aCardInfo);
		          }
		    	  
//browse recursively this element
		          retElements[i] = aCardInfo;  
		      }
			
		      ((XComponent) UnoRuntime.queryInterface(XComponent.class,xViewRoot)).dispose();			
		      return retElements;
		} catch (Throwable e) {
			m_aLogger.severe(e);
		}
		return null;
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
	 //////////// from dev guide
	// Interface to process information when browsing the configuration tree
    // these methods can be useful to show a tree in a configuration viewer
	  public interface IConfigurationProcessor {
	      // process a value item
	      public abstract void processValueElement(String sPath_, Object aValue_);
	      // process a structural item
	      public abstract void processStructuralElement(String sPath_, XInterface xElement_);
	  };

	    private class OurProcessor implements IConfigurationProcessor {

	    	public OurProcessor() {
	    		
	    	}

	    	/* (non-Javadoc)
			 * @see it.plio.ext.oxsit.test.ooo.SSCDsConfigurationAccess.IConfigurationProcessor#processStructuralElement(java.lang.String, com.sun.star.uno.XInterface)
			 */
			@Override
			public void processStructuralElement(String sPath_,
					XInterface xElement_) {
				// TODO Auto-generated method stub
				  // get template information, to detect instances of the 'Filter' template
				  XTemplateInstance xInstance = 
					  ( XTemplateInstance )UnoRuntime.queryInterface( XTemplateInstance.class,xElement_);

					  XNamed xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class,xElement_);
					  System.out.println("== " + xNamed.getName() + " (" + sPath_ + ")");				
			}

			/* (non-Javadoc)
			 * @see it.plio.ext.oxsit.test.ooo.SSCDsConfigurationAccess.IConfigurationProcessor#processValueElement(java.lang.String, java.lang.Object)
			 */
			@Override
			public void processValueElement(String sPath_, Object aValue_) {
				// TODO Auto-generated method stub
				  System.out.println("\tValue: " + sPath_ + " = " + aValue_);				
			}

		};

	  // Internal method to browse a structural element recursively in preorder
	  public void browseElementRecursively(XInterface xElement, IConfigurationProcessor aProcessor)
	          throws com.sun.star.uno.Exception {
	      // First process this as an element (preorder traversal)
	      XHierarchicalName xElementPath = (XHierarchicalName) UnoRuntime.queryInterface(
	          XHierarchicalName.class, xElement);

	      String sPath = xElementPath.getHierarchicalName();

	      //call configuration processor object
	      aProcessor.processStructuralElement(sPath, xElement);

	      // now process this as a container of named elements
	      XNameAccess xChildAccess =
	          (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xElement);

	      // get a list of child elements
	      String[] aElementNames = xChildAccess.getElementNames();
	 
	      // and process them one by one
	      for (int i=0; i< aElementNames.length; ++i) {
	          Object aChild = xChildAccess.getByName(aElementNames[i]);

	          // is it a structural element (object) ...
	          if ( aChild instanceof XInterface ) {
	              // then get an interface 
	              XInterface xChildElement = (XInterface)aChild;
	 
	              // and continue processing child elements recursively
	              browseElementRecursively(xChildElement, aProcessor);
	          }
	          // ... or is it a simple value
	          else {
	              // Build the path to it from the path of 
	              // the element and the name of the child
	              String sChildPath;
	              sChildPath = xElementPath.composeHierarchicalName(aElementNames[i]);
	 
	              // and process the value
	              aProcessor.processValueElement(sChildPath, aChild);
	          }
	      }
	  }	  

	  /** Method to browse the part rooted at sRootPath 
      of the configuration that the Provider provides.
 
      All nodes will be processed by the IConfigurationProcessor passed.
   */
  public void browseConfiguration(String sRootPath, IConfigurationProcessor aProcessor)
          throws com.sun.star.uno.Exception {
 
      // create the root element
      XInterface xViewRoot = (XInterface)super.createConfigurationReadOnlyView(sRootPath);
 
      // now do the processing
      browseElementRecursively(xViewRoot, aProcessor);
 
      // we are done with the view - dispose it 
      // This assumes that the processor 
      // does not keep a reference to the elements in processStructuralElement
 
      ((XComponent) UnoRuntime.queryInterface(XComponent.class,xViewRoot)).dispose();
      xViewRoot = null;
  }

  /** Method to browse the SSCDs.

  Information about installed SSCDs will be printed.
   */
  public void printRegisteredSSCDs() throws com.sun.star.uno.Exception {
	  final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
	  final String sFilterKey = "/it.plio.ext.oxsit.Configuration/SSCDs";
//	  final String sFilterKey = "/it.plio.ext.oxsit.Configuration/SignatureOptionsParameters";

	  // browse the configuration, dumping filter information
	  browseConfiguration(m_sStartPath, new OurProcessor() );
/*	  browseConfiguration( sFilterKey, 
			  new IConfigurationProcessor () { // anonymous implementation of our custom interface
		  // prints Path and Value of properties
		  public void processValueElement(String sPath_, Object aValue_) {
			  System.out.println("\tValue: " + sPath_ + " = " + aValue_);
		  }
		  // prints the Filter entries
		  public void processStructuralElement( String sPath_, XInterface xElement_) {
			  // get template information, to detect instances of the 'Filter' template
			  XTemplateInstance xInstance = 
				  ( XTemplateInstance )UnoRuntime.queryInterface( XTemplateInstance .class,xElement_);

				  XNamed xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class,xElement_);
				  System.out.println("== " + xNamed.getName() + " (" + sPath_ + ")");
		  }   
	  } 
	  );*/
  }  
  //////////// end from dev guide
}
