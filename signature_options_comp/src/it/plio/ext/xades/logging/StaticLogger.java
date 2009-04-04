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

package it.plio.ext.xades.logging;


import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * basic logger, static class
 * @author beppe
 *
 */
public class StaticLogger {
	private static ConsoleHandler	myHandl;
	private static LocalLogFormatter myformatter;
	private static FileHandler	myFileHandl;
	private static LocalLogFormatter myFileformatter;
	
	private static final StaticLogger alog = new StaticLogger(); 
	public static Logger m_logger;

	private StaticLogger() {
		m_logger = Logger.getLogger("it.plio");		
		m_logger.setUseParentHandlers(false);//disables the console output of the root logger

		myHandl = new ConsoleHandler();
		myformatter = new LocalLogFormatter();
		myHandl.setFormatter(myformatter);		
		m_logger.addHandler(myHandl);

		try {
			myFileHandl = new FileHandler("logger.log", true); //append to file
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		myFileformatter = new LocalLogFormatter();
		myFileHandl.setFormatter(myFileformatter);
		m_logger.addHandler(myFileHandl);

		System.out.println("Static Logger");
	}

	public Logger getLogger(String _loggername) {
		m_logger = Logger.getLogger(_loggername);
		m_logger.setUseParentHandlers(false);//disables the console output of the root logger
		return m_logger;
	}
	
	public static StaticLogger getInstance() {
		return alog;
	}
}
