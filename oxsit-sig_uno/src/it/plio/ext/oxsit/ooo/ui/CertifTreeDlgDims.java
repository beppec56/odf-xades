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

import it.plio.ext.oxsit.ooo.ui.ControlDims;

public class CertifTreeDlgDims {
	private static int m_nDsWidth = 300; //minimum value
	private static int m_nDsHeigh = 190; //minimum value
	private static int m_nDsTreeWidth; //minimum value

	public static final int TD_SP_INNERBORDER_LEFT = 3;
	public static final int TD_SP_INNERBORDER_RIGHT = 3;
	public static final int TD_SP_INNERBORDER_TOP = 3;
	public static final int TD_SP_INNERBORDER_BOTTOM = 3;
	public static final int SEP_FL_SPACE_Y = 6;

	public static void setDialogSize(int nDs_Width, int nDs_Heigh, int nDs_TreeWidth) {
//check for correct values
		/*
		 * 
		 */
		int minWith = ControlDims.RSC_SP_DLG_INNERBORDER_LEFT +
		ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT+
		4*ControlDims.RSC_SP_CTRL_X+
		5*dsBtnWidthCertTree();

		if(nDs_Width > m_nDsWidth)
			m_nDsWidth = nDs_Width;
			
		if(nDs_Heigh > m_nDsHeigh)
			m_nDsHeigh = nDs_Heigh;

		if(nDs_TreeWidth > m_nDsTreeWidth)
			m_nDsTreeWidth = nDs_TreeWidth;
	}
	
	public static int dsBtnWidthCertTree() {
		return ControlDims.RSC_CD_PUSHBUTTON_WIDTH; //55;
	}

	/** return 
	 * 		
	 */

	public static int dsWidth() {
		return m_nDsWidth;
/*		return( ControlDims.RSC_SP_DLG_INNERBORDER_LEFT +
				ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT+
				3*ControlDims.RSC_SP_CTRL_X+
				5*DS_BTNWIDTH_1()+DS_BTNWIDTH_1()/2);*/
	}

	public static int dsHeigh() {
		return m_nDsHeigh;	
//		return DS_WIDTH()*3/4;
//		System.out.print(DS_WIDTH()*2/3);
//		return DS_WIDTH()*2/3;
//		return DS_WIDTH()*610/967; // Fibonacci's sequence
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
		return (DS_ROW_4()-ControlDims.RSC_SP_CTRL_X/2);
	}

	public static int DS_ROW_4() {
		return (DS_ROW_5()-ControlDims.RSC_CD_PUSHBUTTON_HEIGHT);
	}
	
	public static int DS_ROW_5() {
		return DLGS_BOTTOM_LAST_CTRL_L(dsHeigh());
	}

	public static int DS_ROW_6() {
		return DLGS_BOTTOM_FL_Y(dsHeigh());
	}
	public static int DS_ROW_7() {
		return DLGS_BOTTOM_BTN_Y(dsHeigh());
	}
	public static int DS_ROW_8() {
		return DLGS_BOTTOM_BTN_L(dsHeigh());
	}	
	public static int DLGS_BOTTOM_FL_Y(int _nHeigh) {
		return ((DLGS_BOTTOM_BTN_Y(_nHeigh)-SEP_FL_SPACE_Y-5));
	}

	public static int DLGS_BOTTOM_LAST_CTRL_L(int _nHeigh) {
		return ((DLGS_BOTTOM_BTN_Y(_nHeigh)-
					2*SEP_FL_SPACE_Y-3));
	}

	public static int DLGS_BOTTOM_OK_X(int value) {

		return ( DLGS_BOTTOM_CANCEL_X( value ) - ControlDims.RSC_SP_CTRL_DESC_X
				- ControlDims.RSC_CD_PUSHBUTTON_WIDTH );
	}

	public static int DLGS_BOTTOM_CANCEL_X(int value) {
		return ((DLGS_BOTTOM_HELP_X( value )
				- ControlDims.RSC_SP_CTRL_X - ControlDims.RSC_CD_PUSHBUTTON_WIDTH));
	}

	public static int DLGS_BOTTOM_HELP_X(int value) {
		return (( value - ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT
				- ControlDims.RSC_CD_PUSHBUTTON_WIDTH));
	}

	public static int DLGS_BOTTOM_BTN_Y(int value) {
		return ((DLGS_BOTTOM_BTN_L( value )
				- ControlDims.RSC_CD_PUSHBUTTON_HEIGHT));
	}

	public static int DLGS_BOTTOM_BTN_L(int value) {
		return ((value-ControlDims.RSC_SP_DLG_INNERBORDER_BOTTOM));
	}

	public static int DS_COL_0() {
		return ControlDims.RSC_SP_DLG_INNERBORDER_LEFT;
	}
	public static int DS_COL_1() {
		return DS_COL_0();
	}

	public static int dsBtnSpacing() {
		return ControlDims.RSC_SP_CTRL_X;
	}

	public static int dsTextFieldSpacing() {
		return ControlDims.RSC_SP_CTRL_X;
	}

	public static int dsTreeControlWith() {
//		return ControlDims.RSC_SP_CTRL_X*2
		return dsTextFieldColumn()-dsTextFieldSpacing()-ControlDims.RSC_SP_DLG_INNERBORDER_LEFT;
	}

	public static int dsTextFieldWith() {
//		return m_nDsTreeWidth;
		return m_nDsWidth-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT-dsTextFieldColumn();
	}	

	public static int dsTextFieldColumn() {
		return (m_nDsWidth/2-
				dsTextFieldSpacing());
	}

	public static int DS_COL_PB1() {
		return DS_COL_PB5()-4*(dsBtnWidthCertTree()+dsBtnSpacing());
	}

	public static int DS_COL_PB2() {
		return DS_COL_PB5()-3*(dsBtnWidthCertTree()+dsBtnSpacing());
	}

	public static int DS_COL_PB3() {
		return DS_COL_PB5()-2*(dsBtnWidthCertTree()+dsBtnSpacing());
	}

	public static int DS_COL_PB4() {
		return DS_COL_PB5()-1*(dsBtnWidthCertTree()+dsBtnSpacing());
	}

	public static int DS_COL_PB5() {
		return m_nDsWidth-ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT-dsBtnWidthCertTree();
	}

	public static int TEXT_0X(){
		return dsTextFieldColumn()+ControlDims.RSC_SP_DLG_INNERBORDER_RIGHT;
	}

	public static int TEXT_L0Y(){
		return DS_ROW_1()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}

	public static int TEXT_L1Y(){
		return TEXT_L0Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L2Y(){
		return TEXT_L1Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L3Y(){
		return TEXT_L2Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L4Y(){
		return TEXT_L3Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L5Y(){
		return TEXT_L4Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L6Y(){
		return TEXT_L5Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L7Y(){
		return TEXT_L6Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L8Y(){
		return TEXT_L7Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L9Y(){
		return TEXT_L8Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}
	
	public static int TEXT_L10Y(){
		return TEXT_L9Y()+ControlDims.RSC_CD_FIXEDTEXT_HEIGHT;
	}	
}
