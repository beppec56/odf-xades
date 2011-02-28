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
public class DigSignDlgDims {
	private static int nDs_Width = 287;
	private static int nDs_Heigh = 287*2/3;

	private static int nTp_Width = 300;
	private static int nTp_Heigh = 185;
	
	public static int RIDDER_HEIGHT() {
		return (ControlDims.RSC_CD_PUSHBUTTON_HEIGHT);
	}

	public static final int TD_SP_INNERBORDER_LEFT = 3;
	public static final int TD_SP_INNERBORDER_RIGHT = 3;
	public static final int TD_SP_INNERBORDER_TOP = 3;
	public static final int TD_SP_INNERBORDER_BOTTOM = 3;
	public static final int SEP_FL_SPACE_Y = 6;

	public static int SEP_FL_ADJ_Y(int val) {
		return (val-3);
	}

	public static void setDsHeigh(int _nHeigh) {
		nDs_Heigh = _nHeigh;
	}

	public static void setDsWidth(int _nWidth) {
		nDs_Width = _nWidth;
	}

	public static int DLGS_WIDTH() {
		return nDs_Width;
	}

	public static int SIGTP_WIDTH() {
		return nSigTp_Width;
	}
	
	public static int SIGTP_HEIGH() {
		return nSigTp_Heigh;
	}

	public static int DS_HEIGHT() {
		return nDs_Heigh;	
	}

	/** le seguenti funzioni sono ricavate dalle macro in
	 *  xmlsecurity/source/dialogs/dialogs.hrc
	 * 
	 * @param value
	 * @return
	 */
	public static int DLGS_BOTTOM_OK_X(int value) {

		return ( DLGS_BOTTOM_CANCEL_X( value ) - ControlDims.RSC_SP_CTRL_DESC_X
				- ControlDims.RSC_CD_PUSHBUTTON_WIDTH );
	}

	public static int DLGS_BOTTOM_CANCEL_X(int value) {
		// TODO Auto-generated method stub
		return ((DLGS_BOTTOM_HELP_X( value )
				- ControlDims.RSC_SP_CTRL_X - ControlDims.RSC_CD_PUSHBUTTON_WIDTH));
	}

	public static int DLGS_BOTTOM_HELP_X(int value) {
		// TODO Auto-generated method stub
		return (( value - ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT
				- ControlDims.RSC_CD_PUSHBUTTON_WIDTH));
	}

	public static int DLGS_BOTTOM_BTN_Y(int value) {
		return ((DLGS_BOTTOM_BTN_L( value )
				- ControlDims.RSC_CD_PUSHBUTTON_HEIGHT));
	}

	public static int DLGS_BOTTOM_BTN_L(int value) {
		// TODO Auto-generated method stub
		return ((value-ControlDims.RSC_SP_DLG_INNERBORDER_BOTTOM));
	}

	public static int DS_COL_0() {
		return ControlDims.RSC_SP_DLG_INNERBORDER_LEFT;
	}

	public static int DS_COL_1() {
		return DS_COL_0();
	}

	public static int DS_COL_2() {
		return (DS_COL_1() + DS_BTNWIDTH_1() );
	}

	public static int DS_COL_3() {
		return (DS_COL_2() + DS_BTNSPACE_X());
	}

	public static int DS_COL_4() {
		return(DS_COL_3()+DS_BTNWIDTH_1());
	}

	public static int DS_COL_5() {
		return (DS_COL_4() + DS_BTNSPACE_X() );	
	}
	public static int DS_COL_6() {
		return DS_COL_7();
	}

	public static int DS_COL_7() {
		return (DS_WIDTH()-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT);
	}

	public static int DS_BTNWIDTH_1() {
		return 70;
	}

	public static int DS_BTNSPACE_X() {
		return ControlDims.RSC_SP_CTRL_X;
	}

	public static int DS_LB_WIDTH() {
		return((DS_COL_7()-DS_COL_0()));
	}
	
	public static int DS_WIDTH() {
		return( ControlDims.RSC_SP_DLG_INNERBORDER_LEFT +
				ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT+2*
				DS_BTNSPACE_X()+3*
				DS_BTNWIDTH_1());
	}

	public static int DS_ROW_0() {
		return ControlDims.RSC_SP_DLG_INNERBORDER_TOP;
	}
	public static int DS_ROW_1() {
		return (DS_ROW_0() +
				ControlDims.RSC_CD_FIXEDTEXT_HEIGHT()+
				ControlDims.RSC_SP_CTRL_DESC_X);
	}

	public static int DS_ROW_2() {
		return (DS_ROW_2A()-ControlDims.RSC_SP_CTRL_GROUP_Y);
	}

	public static int DS_ROW_2A() {
		return (DS_ROW_3()-ControlDims.RSC_CD_FIXEDTEXT_HEIGHT()-
				ControlDims.RSC_SP_CTRL_Y);
	}

	public static int DS_ROW_3() {
		return (DS_ROW_4()-ControlDims.RSC_CD_PUSHBUTTON_HEIGHT -
				ControlDims.RSC_SP_CTRL_X/2);
	}

	public static int DS_ROW_4() {
		return (DS_ROW_5()-ControlDims.RSC_CD_PUSHBUTTON_HEIGHT);
	}
	
	public static int DS_ROW_5() {
		return DLGS_BOTTOM_LAST_CTRL_L(nDs_Heigh);
	}

	public static int DS_ROW_6() {
		return DLGS_BOTTOM_FL_Y(nDs_Heigh);
	}
	public static int DS_ROW_7() {
		return DLGS_BOTTOM_BTN_Y(nDs_Heigh);
	}
/*	public static int DS_ROW_7() {
		return DLGS_BOTTOM_BTN_L(nHeigh);
	}
*/
	public static int DS_ROW_8() {
		return DLGS_BOTTOM_BTN_L(nDs_Heigh);
	}	
	public static int DLGS_BOTTOM_FL_Y(int _nHeigh) {
		return ((DLGS_BOTTOM_BTN_Y(_nHeigh)-SEP_FL_SPACE_Y-5));
	}

	public static int DLGS_BOTTOM_LAST_CTRL_L(int _nHeigh) {
		return ((DLGS_BOTTOM_BTN_Y(_nHeigh)-
					2*SEP_FL_SPACE_Y-3));
	}

	public static int TD_WIDTH() {
		return(TP_WIDTH()+
				ControlDims.RSC_SP_DLG_INNERBORDER_LEFT+
				ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT);
	}

	public static int TD_HEIGHT() {
		return (TP_HEIGHT()+TD_SP_INNERBORDER_TOP+
				2*TD_SP_INNERBORDER_BOTTOM+
				ControlDims.RSC_CD_PUSHBUTTON_HEIGHT+
				RIDDER_HEIGHT());
	}

	public static int TP_WIDTH() {
		return nTp_Width;
	}

	public static int TP_HEIGHT() {
		return nTp_Heigh;
	}

	//// --------- tab dialog Certificate viewer ---------
	public static int CV_COL_0A() {
		return TD_SP_INNERBORDER_LEFT;
	}

	public static int CV_COL_0() {
		return TD_SP_INNERBORDER_LEFT+
				ControlDims.RSC_CD_PUSHBUTTON_WIDTH+
				ControlDims.RSC_SP_CTRL_GROUP_X;
	}

	public static int CV_COL_0B() {
		return CV_COL_0()+DigSignDlgDims.CV_CONT_WIDTH()/3+TD_SP_INNERBORDER_LEFT;
	}
	
	public static int CV_COL_0C() {
		return CV_COL_0()+DigSignDlgDims.CV_CONT_WIDTH()/3+TD_SP_INNERBORDER_LEFT/2;
	}

	public static int CV_ROADMAP_WIDTH() {
		return ControlDims.RSC_CD_PUSHBUTTON_WIDTH;
	}

	public static int CV_COL_1() {
		return TP_WIDTH()+ControlDims.RSC_SP_TBPG_INNERBORDER_RIGHT;
	}

	public static int CV_CONT_WIDTH() {
		return (CV_COL_1()-CV_COL_0());
	}
	public static int CV_CONT_HEIGHT() {
		return (CV_ROW_3()-CV_ROW_0());
	}
/// riga per tabs	
	public static int CV_ROW_0() {
		return ControlDims.RSC_SP_TBPG_INNERBORDER_TOP;
	}
// riga inizio area lista valori certificato	
	public static int CV_ROW_0A() {
		return ControlDims.RSC_SP_TBPG_INNERBORDER_TOP+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT();
	}
	public static int CV_ROW_1() {
		return (CV_ROW_0A()+2*CV_CONT_HEIGHT()/3);
	}
	public static int CV_ROW_2() {
		return (CV_ROW_1()+ControlDims.RSC_SP_CTRL_GROUP_Y);
	}
	public static int CV_ROW_3() {
		return (TP_HEIGHT()-ControlDims.RSC_SP_TBPG_INNERBORDER_BOTTOM);
	}
	public static int CV_COL_D() {
		return (TD_WIDTH()-TD_SP_INNERBORDER_RIGHT);
	}
	public static int CV_COL_C() {
		return (CV_COL_D()-ControlDims.RSC_CD_PUSHBUTTON_WIDTH);
	}
	public static int CV_COL_B() {
		return (CV_COL_C()-ControlDims.RSC_SP_CTRL_X);
	}
	public static int CV_COL_A() {
		return (CV_COL_B()-ControlDims.RSC_CD_PUSHBUTTON_WIDTH);
	}

	public static int CV_ROW_B() {
		return (TD_HEIGHT()-TD_SP_INNERBORDER_BOTTOM);
	}
	public static int CV_ROW_A() {
		return (CV_ROW_B()-ControlDims.RSC_CD_PUSHBUTTON_HEIGHT);	
	}

	public static int CVP_ROW_0() {
		return ControlDims.RSC_SP_TBPG_INNERBORDER_TOP;
	}
	public static int CVP_ROW_1() {
		return (CVP_ROW_0()+
				ControlDims.RSC_CD_FIXEDTEXT_HEIGHT+
				ControlDims.RSC_SP_CTRL_DESC_Y);
	}
	public static int REST_HEIGHT() {
		return (TP_HEIGHT()-CVP_ROW_1()-
				2*ControlDims.RSC_SP_GRP_SPACE_Y-
				ControlDims.RSC_CD_FIXEDTEXT_HEIGHT-
				ControlDims.RSC_CD_PUSHBUTTON_HEIGHT);
	}
	public static int CVP_ROW_2() {
		return (CVP_ROW_1()+
				REST_HEIGHT()/3*2+
				ControlDims.RSC_SP_CTRL_DESC_Y);
	}
	public static int CVP_ROW_3() {
		return (CVP_ROW_2()+ControlDims.RSC_CD_PUSHBUTTON_HEIGHT);
	}
	public static int CVP_ROW_4() {
		return (CVP_ROW_3()+
					ControlDims.RSC_CD_FIXEDTEXT_HEIGHT+
					ControlDims.RSC_SP_CTRL_DESC_Y);
	}

	private static int nSigTp_Width = 140;
	private static int nSigTp_Heigh = 68;
		
	public static void setSIGTP_HEIGH(int i) {
		nSigTp_Heigh = i;
	}

	public static void setSIGTP_WIDTH(int i) {
		nSigTp_Width = i;
	}

	public static int getSIGTP_WIDTH() {
		return nSigTp_Width;
	}

	public static int SIGTP_PUSHBUTTON_WIDTH() {
		return 120;
	}

	public static int SIGTP_OOOSIG_PUSHBUTTON_POS() {
		return ControlDims.RSC_SP_DLG_INNERBORDER_TOP;
	}
	public static int SIGTP_CNIPASIG_PUSHBUTTON_POS() {
		return DigSignDlgDims.SIGTP_OOOSIG_PUSHBUTTON_POS()+
		ControlDims.RSC_CD_PUSHBUTTON_HEIGHT+
		ControlDims.RSC_SP_GRP_SPACE_Y;
	}
	public static int SIGTP_CANCEL_PUSHBUTTON_POS() {
		return SIGTP_CNIPASIG_PUSHBUTTON_POS()+
		ControlDims.RSC_CD_PUSHBUTTON_HEIGHT+
		ControlDims.RSC_SP_GRP_SPACE_Y;
	}
	
	public static int SIGTP_PUSHBUTTON_XPOS() {
		return (nSigTp_Width - SIGTP_PUSHBUTTON_WIDTH())/2;
	}
}
