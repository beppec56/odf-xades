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
 * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/DocumentSigner_IT.java.
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

package com.yacme.ext.oxsit.logging;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	public String format(LogRecord _rec) {
		String sReturn;
		String sLevel;
		
		if(_rec.getLevel() == Level.FINER)
			sLevel = "l ";
		else if(_rec.getLevel() == Level.CONFIG)
			sLevel = "c ";
		else if(_rec.getLevel() == Level.SEVERE)
			sLevel = "S ";
		else if (_rec.getLevel() == Level.WARNING)
			sLevel = "W ";
		else if (_rec.getLevel() == Level.FINE)
			sLevel = "d ";
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
		Date aDate = new Date(_rec.getMillis());
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(aDate);	
		
//string with date and time
//		String time = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL", calendar);
//string with time only
		String time = String.format("%1$tH:%1$tM:%1$tS.%1$tL", calendar);
		return time;
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
