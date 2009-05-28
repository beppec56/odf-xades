/**
 * 
 */
package it.plio.ext.oxsit.test.ooo;

import java.util.Properties;

import it.plio.ext.oxsit.Utilities;
import it.plio.ext.oxsit.ooo.ConfigurationAccess;
import it.plio.ext.oxsit.ooo.GlobConstant;

import com.sun.star.beans.Property;
import com.sun.star.beans.XProperty;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XContainer;
import com.sun.star.container.XElementAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XNamed;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class SSCDsConfigurationAccess extends ConfigurationAccess {

	private Object m_oAllFramesConfView;

	/**
	 * @param context
	 */
	public SSCDsConfigurationAccess(XComponentContext context) {
		super(context);
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
}
