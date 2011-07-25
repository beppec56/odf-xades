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

package com.yacme.ext.oxsit.ooo.ui;


/**
 * macro DS_ riprese da	xmlsecurity/source/dialogs/dialogs.hrc
 * gestisce le dimensioni del dialogo 
 * @author beppe
 *
 */
public class PINDlgDims {
	private static int nDs_Width = ControlDims.RSC_SP_DLG_INNERBORDER_LEFT+
								(ControlDims.RSC_CD_PUSHBUTTON_WIDTH*2)+
								ControlDims.RSC_SP_CTRL_DESC_X+
								ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT+40;
	private static int nDs_Heigh = 140;

	public static int DLGS_WIDTH() {
		return nDs_Width;
	}

	public static int DS_HEIGHT() {
		return nDs_Heigh;	
	}

	public static int ED_WIDTH () {
		return ControlDims.RSC_CD_PUSHBUTTON_WIDTH*2;
//		return DLGS_WIDTH()-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT-ControlDims.RSC_SP_DLG_INNERBORDER_LEFT;
	}
}
