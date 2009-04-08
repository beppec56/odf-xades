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

package it.plio.ext.xades.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author beppe
 *
 */
public class LocalLogFormatter extends Formatter {

	/**
	 * 
	 */
	public LocalLogFormatter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	public String format(LogRecord _rec) {
		String sReturn;
		String sLevel;
		
		if(_rec.getLevel() == Level.SEVERE)
			sLevel = "S ";
		else if (_rec.getLevel() == Level.WARNING)
			sLevel = "W ";
		else
			sLevel = "i ";			

//		sReturn = getTimeMs(_rec) + _rec.getLoggerName() + "."+ _rec.getSourceMethodName()+ " "+ _rec.getMessage();	
/*		sReturn = sLevel+getTimeMs(_rec) + " " + _rec.getLoggerName() +" "+
						_rec.getSourceClassName()+" "+ _rec.getSourceMethodName()+" "+
						_rec.getMessage()+"\n";*/
		sReturn = sLevel+getTimeMs(_rec) + " "+
		_rec.getSourceClassName()+" "+ _rec.getSourceMethodName()+" "+
		_rec.getMessage()+"\n";
		return sReturn;
	}

	private String getTimeMs(LogRecord _rec) {
//		Date aDate = new Date();		
		String time = String.format( "%20d ", _rec.getMillis() );
		return time.substring( 14,17 ) +"."+ time.substring( 17 );		
	}
	
	public String getHead(Handler h) {
		return ""; // called when the formatter is instantianted
		
	}
	
	public String getTail(Handler h) {
		return "";
	}
	
	public String formatter(LogRecord rec) {
		return "";
	}
}
