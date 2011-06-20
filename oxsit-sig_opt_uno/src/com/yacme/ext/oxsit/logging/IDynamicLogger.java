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

package com.yacme.ext.oxsit.logging;


import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.ooo.GlobConstant;

/**
 * interface for class to wrap the UNO logger. Characterizes the class owner
 * Can be used only if the UNO context is known
 * All the logging rigamarole is carried out in an UNO singleton object.
 * 
 * Main logging switch turned on/off on a owner basis (eg the parent class).
 * 
 * 
 * @author beppec56
 *
 */
public interface IDynamicLogger {

	abstract void ctor();

	abstract void ctor(String _message);

	abstract void debug(String _message);

	abstract void log(String _message);

	abstract void log(Throwable e, boolean _useDialog);

	abstract void entering(String _theMethod);

	abstract void entering(String _theMethod, String _message);

	abstract void exiting(String _theMethod, String _message);

	abstract void log(String _theMethod, String _message);

	abstract void info(String _theMethod);

	abstract void info(String _theMethod, String _message);

	abstract void warning(String _theMethod);

	abstract void warning(String _theMethod, String _message);

	abstract void warning(String _theMethod, String _message, Throwable ex);

	abstract void severe(String _theMethod, String _message);

	abstract void severe(Throwable ex);

	abstract void severe(String _theMethod, String _message, Throwable ex);

	abstract void severe(String _theMethod, Throwable ex);
	
	abstract void disableLogging();
	
	abstract void enableLogging();
	
	abstract void enableInfo();

	abstract void disableInfo();

	abstract void enableWarning();

	abstract void disableWarning();

	abstract void stopLogging();
	
	abstract void log_exception(int n_TheLevel, String _theMethod, String _message, java.lang.Throwable ex, boolean useDialog);
}
