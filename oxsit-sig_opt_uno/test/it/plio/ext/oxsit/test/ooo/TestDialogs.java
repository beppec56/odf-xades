/**
 * 
 */
package it.plio.ext.oxsit.test.ooo;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.ui.DialogQueryPIN;
import it.plio.ext.oxsit.pcsc.CardInfoOOo;

import com.sun.star.awt.Size;
import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.style.BreakType;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.WrapTextMode;
import com.sun.star.text.XPagePrintable;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.ucb.XContent;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class TestDialogs {

	public OOoServerInfo SvrInfo = new OOoServerInfo();
    private static TestRootVerifierOOo theInstance = null;
    public XComponent xComponent = null;
    private XContent xContent = null;
    private Object XActiveDataSink;
    private XMultiComponentFactory xMCF = null;
    private XComponentContext xCC = null;

	public TestDialogs() {

	}

	private void trace(String s) {
		System.out.println(s);
	}
	
	private void trace(Throwable s) {
		s.printStackTrace();
	}

	private void run() {
        try {
            if(SvrInfo.InitConnection()) {
                System.out.println("Connection successful !");
                xCC = SvrInfo.getCompCtx();
                xMCF = xCC.getServiceManager();
            //try to get a document
//                Object desktop = SvrInfo.getFactory().createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
                
        		DialogQueryPIN aDialog1 =
        			new DialogQueryPIN( null, xCC, xMCF );
        		try {
        			//PosX e PosY devono essere ricavati dalla finestra genetrice (in questo caso la frame)
        			//get the parente window data
//        			com.sun.star.awt.XWindow xCompWindow = m_xFrame.getComponentWindow();
//        			com.sun.star.awt.Rectangle xWinPosSize = xCompWindow.getPosSize();
        			int BiasX = 100;
        			int BiasY = 30;
//        			System.out.println("Width: "+xWinPosSize.Width+ " height: "+xWinPosSize.Height);
//        			XWindow xWindow = m_xFrame.getContainerWindow();
//        			XWindowPeer xPeer = xWindow.
        			aDialog1.initialize(BiasX,BiasY);
        //center the dialog
        			short test = aDialog1.executeDialog();
        			trace("return: "+test+ " "+aDialog1.getThePin());
        		}
        		catch (com.sun.star.uno.RuntimeException e) {
        			e.printStackTrace();
        		} catch (BasicErrorException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (Exception e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
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
		TestDialogs t = new TestDialogs();		
		t.run();
		System.exit(0);
	}	
}

