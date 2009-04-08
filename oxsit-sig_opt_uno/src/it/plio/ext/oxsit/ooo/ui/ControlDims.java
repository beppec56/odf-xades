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

package it.plio.ext.oxsit.ooo.ui;

import it.plio.ext.oxsit.Utilities;

/**
 * costanti riprese da svtools/inc/controldims.hrc standard per GUI OOo
 * 
 * @author beppe
 * 
 */
public class ControlDims {
	// / costanti RSC_ riprese da svtools/inc/controldims.hrc
	public static final int	RSC_SP_DLG_INNERBORDER_BOTTOM	= 6;
	public static final int	RSC_CD_PUSHBUTTON_WIDTH			= 50;
	public static final int	RSC_CD_PUSHBUTTON_HEIGHT		= 14;

	public static final int	RSC_SP_CTRL_DESC_X				= 3;
	public static final int	RSC_SP_CTRL_X					= 6;					// controls that are unrelated
	public static final int	RSC_SP_CTRL_Y					= 7;
	public static final int	RSC_SP_CTRL_GROUP_X				= 3;					// related controls, or controls in a groupbox
	public static final int	RSC_SP_CTRL_GROUP_Y				= 4;
	public static final int	RSC_SP_CTRL_DESC_Y				= 3;
	public static final int	RSC_SP_DLG_INNERBORDER_RIGHT	= 6;
	public static final int	RSC_SP_DLG_INNERBORDER_LEFT		= 6;
	public static final int	RSC_BS_CHARHEIGHT				= 8;

	public static final int	RSC_SP_DLG_INNERBORDER_TOP		= 6;
	public static final int	RSC_SP_TBPG_INNERBORDER_TOP		= 3;
	public static final int	RSC_SP_TBPG_INNERBORDER_RIGHT	= 6;
	public static final int	RSC_SP_TBPG_INNERBORDER_BOTTOM	= 6;
	public static final int	RSC_CD_FIXEDTEXT_HEIGHT			= RSC_BS_CHARHEIGHT;
	public static final int	RSC_SP_GRP_SPACE_Y				= 6;
	
	public static final int	DLG_ABOUT_WIDTH			= 205;
	public static final int	DLG_ABOUT_HEIGH			= 140;
	
	public static final int	DLG_SHOW_LICENSE_WIDTH			= 255;
	public static final int	DLG_SHOW_LICENSE_HEIGH			= 170;
	
	public static final int	DLG_ABOUT_BACKG_COLOR			= Utilities.getRGBColor(255, 255, 234);
	public static final int	DLG_CERT_TREE_BACKG_COLOR		= Utilities.getRGBColor(255, 255, 245);

	public static int RSC_CD_FIXEDTEXT_HEIGHT() {
		return RSC_BS_CHARHEIGHT;
	}

	public static int RSC_CD_FIXEDLINE_HEIGHT() {
		return RSC_BS_CHARHEIGHT;
	}
}
