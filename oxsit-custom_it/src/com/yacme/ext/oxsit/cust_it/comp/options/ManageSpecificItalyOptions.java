/* ***** BEGIN LICENSE BLOCK ********************************************
 * Version: EUPL 1.1/GPL 3.0
 * 
 * The contents of this file are subject to the EUPL, Version 1.1 or 
 * - as soon they will be approved by the European Commission - 
 * subsequent versions of the EUPL (the "Licence");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/options/ManageSpecificItalyOptions.java.
 *
 * The Initial Developer of the Original Code is
 * Giuseppe Castagno giuseppe.castagno@acca-esse.it
 * 
 * Portions created by the Initial Developer are Copyright (C) 2009-2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 3 or later (the "GPL")
 * in which case the provisions of the GPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the GPL, and not to allow others to
 * use your version of this file under the terms of the EUPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EUPL, or the GPL.
 *
 * ***** END LICENSE BLOCK ******************************************** */

package com.yacme.ext.oxsit.cust_it.comp.options;


import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.comp.options.ManageOptions;
import com.yacme.ext.oxsit.ooo.ui.DialogAbout;
import com.yacme.ext.oxsit.options.SingleControlDescription;
import com.yacme.ext.oxsit.options.SingleControlDescription.ControlTypeCode;

/**
 * 
 * manages the General option page on Tools > Options...
 * 
 * @author beppe
 *
 */
public class ManageSpecificItalyOptions extends ManageOptions  {

	// needed for registration
	public static final String			m_sImplementationName	= ManageSpecificItalyOptions.class.getName();
	public static final String[]		m_sServiceNames			= { "com.yacme.ext.oxsit.comp.options.ManageSpecItalyOptions" };

    private int m_nAboutButton = 0;
    private int m_nBrowseInternetButton = 0;

    private static final int m_nNumberOfControls = 7;

    /**
     * 
     * @param xCompContext
     */
	public ManageSpecificItalyOptions(XComponentContext xCompContext) {
		super(xCompContext, m_nNumberOfControls, "leaf_general");
//		m_aLoggerDialog.enableLogging();// disabled in base class
		m_aLogger.ctor();
		//prepare the list of controls on the page

		int iter = 0;
		SingleControlDescription aControl = 
				new	SingleControlDescription("GeneralCNIPA_URL", ControlTypeCode.EDIT_TEXT, -1, "GeneralCNIPA_URL", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//checkbox
		aControl = 
			new SingleControlDescription("GeneralCheckBox1", ControlTypeCode.CHECK_BOX, -1, "TestBooleanValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
//the three radio buttons
		aControl = 
			new SingleControlDescription("OptionButton0", ControlTypeCode.RADIO_BUTTON, 0, "TestNumericValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("OptionButton1", ControlTypeCode.RADIO_BUTTON, 1, "TestNumericValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;
		aControl = 
			new SingleControlDescription("OptionButton2", ControlTypeCode.RADIO_BUTTON, 2, "TestNumericValue", 0, 0, true);
		ArrayOfControls[iter++] = aControl;//the single About push button
		aControl = 
			new SingleControlDescription("AboutButton", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nAboutButton = iter;
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;

		aControl = 
			new SingleControlDescription("BrowseInternet", ControlTypeCode.PUSH_BUTTON, -1, "", 0, 0, true);
		m_nBrowseInternetButton = iter; // set for further processing
		aControl.m_xAnActionListener = this;
		ArrayOfControls[iter++] = aControl;
	}

	public String getImplementationName() {
		return m_sImplementationName;
	}

	public String[] getSupportedServiceNames() {
		return m_sServiceNames;
	}

	public boolean supportsService(String _sService) {
		int len = m_sServiceNames.length;

		m_aLogger.info( "supportsService" );
		for (int i = 0; i < len; i++) {
			if (_sService.equals( m_sServiceNames[i] ))
				return true;
		}
		return false;
	}

	public void actionPerformed(ActionEvent rEvent) {
		m_aLogger.entering("actionPerformed");
	// TODO Auto-generated method stub
        try{
            // get the control that has fired the event,
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, rEvent.Source);
            XControlModel xControlModel = xControl.getModel();
            XPropertySet xPSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
            String sName = (String) xPSet.getPropertyValue("Name");
            // just in case the listener has been added to several controls,
            // we make sure we refer to the right one
            if (sName.equals(ArrayOfControls[m_nAboutButton].m_sControlName)) {
// activate the about dialog box
            	DialogAbout.showDialog(null, m_xComponentContext, m_xMultiComponentFactory);
            }
            else if (sName.equals(ArrayOfControls[m_nBrowseInternetButton].m_sControlName)) {
            	m_aLogger.info("browse the Internet for a URL");
                //...
            }
            else {
            	m_aLogger.info("Activated: "+sName);            	
            }
        }catch (com.sun.star.uno.Exception ex){
            /* perform individual exception handling here.
             * Possible exception types are:
             * com.sun.star.lang.WrappedTargetException,
             * com.sun.star.beans.UnknownPropertyException,
             * com.sun.star.uno.Exception
             */
        	m_aLogger.severe("", "", ex);
        }		
	}
}
