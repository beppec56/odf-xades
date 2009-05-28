/**
 * 
 */
package it.plio.ext.oxsit.test.ooo;

import it.plio.ext.oxsit.Utilities;

import com.sun.star.configuration.XTemplateInstance;
import com.sun.star.container.XElementAccess;
import com.sun.star.container.XHierarchicalName;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNamed;
import com.sun.star.lang.XComponent;
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
	  
	  /**
	 * @param context
	 */
	public SSCDsConfigurationAccess(XComponentContext context, String startpath_) {
		super(context);
		m_sStartPath = startpath_;
		// TODO Auto-generated constructor stub
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
