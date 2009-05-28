/**
 * 
 */
package it.plio.ext.oxsit.test.ooo;

import it.plio.ext.oxsit.ooo.ConfigurationAccess;
import it.plio.ext.oxsit.security.crl.RootsVerifier;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XContent;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class TestSSCDConfiguration {

	public OOoServerInfo SvrInfo = new OOoServerInfo();
    private static TestRootVerifierOOo theInstance = null;
    public XComponent xComponent = null;
    private XContent xContent = null;
    private Object XActiveDataSink;
    private XMultiComponentFactory xMCF = null;
    private XComponentContext xCC = null;

	public TestSSCDConfiguration() {
		
	}
	
    private void run() {
        try {
            if(SvrInfo.InitConnection()) {
                System.out.println("Connection successful !");
                xCC = SvrInfo.getCompCtx();
                xMCF = xCC.getServiceManager();
            //try to get a document
//                Object desktop = SvrInfo.getFactory().createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
            // get the remote service manager
            // query its XDesktop interface, we need the current component

//init the configuration manager
                String root = "/it.plio.ext.oxsit.Configuration/SSCDCollection";
//                root = "/org.openoffice.Office.TypeDetection/Filters";
//                root = "/org.openoffice.Office.Calc/Grid";
//                	root ="/org.openoffice.Office.Addons";
                SSCDsConfigurationAccess aSSCD = new SSCDsConfigurationAccess(xCC,root);
                
                aSSCD.printRegisteredSSCDs();

            }
        }
        catch(Exception e) {
            System.out.println("WARNING: exception thrown !\nJob aborted:\n"+e.toString());
        } catch (java.lang.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestSSCDConfiguration t = new TestSSCDConfiguration();		
		t.run();
		System.exit(0);
	}	
}