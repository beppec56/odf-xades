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

package it.plio.ext.xades.ooo.ui;

import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XItemListener;
import com.sun.star.lang.EventObject;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;


public class RmapItemStateChgListener implements XItemListener {
    protected com.sun.star.lang.XMultiServiceFactory m_xMSFDialogModel;
   
    public RmapItemStateChgListener(com.sun.star.lang.XMultiServiceFactory xMSFDialogModel) {
        m_xMSFDialogModel = xMSFDialogModel;
    }
    
    public void itemStateChanged(com.sun.star.awt.ItemEvent itemEvent) {
        try {
            // get the new ID of the roadmap that is supposed to refer to the new step of the dialogmodel
            int nNewID = itemEvent.ItemId;
            XPropertySet xDialogModelPropertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, m_xMSFDialogModel);
            int nOldStep = ((Integer) xDialogModelPropertySet.getPropertyValue("Step")).intValue();
            // in the following line "ID" and "Step" are mixed together.
            // In fact in this case they denot the same
            if (nNewID != nOldStep){
                xDialogModelPropertySet.setPropertyValue("Step", new Integer(nNewID));
            }
        } catch (com.sun.star.uno.Exception exception) {
            exception.printStackTrace(System.out);
        }
    }
    
    public void disposing(EventObject eventObject) {
    }
}
