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

package it.plio.ext.oxsit;

import it.plio.ext.oxsit.ooo.GlobConstant;
import it.plio.ext.oxsit.ooo.ui.test.DialogCertificateTree;
import it.plio.ext.oxsit.utilities.OOoServerInfo;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.script.BasicErrorException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

public class MainStandAloneDebug {

	protected XComponentContext		m_xContext;
	protected XMultiComponentFactory	m_xMCF;
	
	public MainStandAloneDebug(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		m_xContext = _xContext;
		m_xMCF = _xMCF;
	}

	public void run(OOoServerInfo aOOo) {
		
		// set the global flag telling the rest of the
		// program we are running as debug stand alone version
		GlobConstant.m_bCalledOutsideExtention = true;

		// test the access to the toobar configuration elements
/*		ToolbarButtonTitleRegistryAccess aTba = new ToolbarButtonTitleRegistryAccess(m_xContext, m_xMCF, "button1" );
		
		println("Title: "+aTba.getString() );
		
		aTba.setString( "A new title was set..." );
*/
		
//		FrameStatusConfigurationAccess aFrm = new FrameStatusConfigurationAccess(m_xContext, "test%20frame%20url");
//		aFrm.setSignatureStatus( 23 );
//		FrameStatusConfigurationAccess aFrm = new FrameStatusConfigurationAccess(m_xContext, "test frame url");
//		aFrm.setSignatureStatus( 2 );
//		FrameStatusConfigurationAccess aFrm = new FrameStatusConfigurationAccess(m_xContext, "FeFAFcf56abf0178");
//		aFrm.setSignatureStatus( 232 );
//		FrameStatusConfigurationAccess aFrm = new FrameStatusConfigurationAccess(m_xContext, m_xMCF, "FeFAFcf56abf0177");
//		aFrm.setSignatureStatus( 32 );

//		FrameStatusConfigurationAccess aFrm = new FrameStatusConfigurationAccess(m_xContext, m_xMCF, "test.frame.url");
//		aFrm.setSignatureStatus( 3 );
//		FrameStatusConfigurationAccess aFrm = new FrameStatusConfigurationAccess(m_xContext, m_xMCF, "dummy");
//		aFrm.setSignatureStatus( 1 );

//		println( "status is :"+ aFrm.getSignatureStatus());

//		aFrm.dispose();
//		aFrm.removeFrameData();
		
//		aFrm.removeAllElements();
		
/*		MessageRegistryAccess aMex = new MessageRegistryAccess(m_xContext, m_xMCF);
		
		try {
			println("string: "+aMex.getStringFromRegistry("id_cancel"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		// create the first dialog (display signature certificate present in
		// the package)

		// quello racchiuso tra i commenti corrisponde al richiamo del menu
		// factory e componentcontext sono da adattare all'ambiente
		// di richiamo
		// XComponentContext xCtx = aOOo.getCompCtx();

		// //////////////////////////////////////

		/*
		 * test dialog per firma
		 */
		
		DialogCertificateTree aDialog1 = new DialogCertificateTree( null, aOOo.getCompCtx(),
				aOOo.getFactory() );
		try {
			aDialog1.initialize( 10, 10 );
		} catch (BasicErrorException e) {
			e.printStackTrace();
		}
		try {
			aDialog1.executeDialog();
		} catch (BasicErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
		
		/*
		 * DialogAbout aDialog1 = new DialogAbout( null, aOOo.getCompCtx(), aOOo
		 * .getFactory() ); try { // PosX e PosY devono essere ricavati dalla
		 * finestra genetrice // (in questo caso la frame) aDialog1.initialize(
		 * 80, 30 ); } catch (BasicErrorException e) { e.printStackTrace(); }
		 * aDialog1.executeDialog(); // //////////////////////////////////////
		 *  // //////// test the DispatchIntercept XComponentContext xCtx =
		 * aOOo.getCompCtx(); // try to get a document Object desktop =
		 * aOOo.getFactory().createInstanceWithContext(
		 * "com.sun.star.frame.Desktop", xCtx ); // get the remote service
		 * manager // query its XDesktop interface, we need the current
		 * component XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(
		 * XDesktop.class, desktop );
		 * 
		 * XFrame xFrame = xDesktop.getCurrentFrame();
		 * 
		 *//*
			 * TestDispatchInterceptor maInterceptor = new
			 * TestDispatchInterceptor(xFrame,aOOo.getCompCtx(),aOOo.getFactory());
			 * 
			 * maInterceptor.startListening();
			 * 
			 * //wait until Interceptor exits because of OOo quitting try {
			 * while(!maInterceptor.isIdle()) { Thread.sleep(2000); }
			 * Thread.sleep(5000); } catch (InterruptedException e) {
			 */
	}

	public static void main(String args[]) throws Exception {

		OOoServerInfo aOOo = new OOoServerInfo();
		// connect to Office
		if (aOOo.InitConnection()) {
			MainStandAloneDebug aMain = new MainStandAloneDebug(aOOo.getCompCtx(), aOOo.getFactory());

			aMain.run(aOOo);
			aOOo.CloseConnection();
		} else {
			System.out.println( "Non trovato OpenOffice.org funzionante" );
		}
		System.out.println( "Programma terminato" );
	}

	// debug methods
	// ////////////////debug methods
	protected String getHashHex() {
		return String.format( "%8H", hashCode() );
	}

	protected void printlnName(String _sMex) {
		System.out
				.println( getHashHex() + " " + this.getClass().getName() + ": " + _sMex );
	}

	protected void printName(String _sMex) {
		System.out.print( getHashHex() + " " + this.getClass().getName() + ": " + _sMex );
	}

	protected void print(String _sMex) {
		System.out.print( " " + _sMex );
	}

	protected void println(String _sMex) {
		System.out.println( getHashHex() + " " + _sMex );
	}

	protected void passert(String a_message, Object _tocheck) {
		println( a_message + ": " + ( _tocheck != null ) );
	}
}
