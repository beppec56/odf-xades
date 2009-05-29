/**
 * 
 */
package it.plio.ext.oxsit.logging;

/**
 * A logger helper class to be used when you want to disable the logging in a class
 * that receive the object from the parent.
 * 
 * @author beppec56
 *
 */
public class DynamicLazyLogger implements IDynamicLogger {

	/**
	 * 
	 */
	public DynamicLazyLogger() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#ctor()
	 */
	@Override
	public void ctor() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#ctor(java.lang.String)
	 */
	@Override
	public void ctor(String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#disableInfo()
	 */
	@Override
	public void disableInfo() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#disableLogging()
	 */
	@Override
	public void disableLogging() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#disableWarning()
	 */
	@Override
	public void disableWarning() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#enableInfo()
	 */
	@Override
	public void enableInfo() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#enableLogging()
	 */
	@Override
	public void enableLogging() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#enableWarning()
	 */
	@Override
	public void enableWarning() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#entering(java.lang.String)
	 */
	@Override
	public void entering(String method) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#entering(java.lang.String, java.lang.String)
	 */
	@Override
	public void entering(String method, String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#exiting(java.lang.String, java.lang.String)
	 */
	@Override
	public void exiting(String method, String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#info(java.lang.String)
	 */
	@Override
	public void info(String method) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#info(java.lang.String, java.lang.String)
	 */
	@Override
	public void info(String method, String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#log(java.lang.String)
	 */
	@Override
	public void log(String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#log(java.lang.String, java.lang.String)
	 */
	@Override
	public void log(String method, String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String)
	 */
	@Override
	public void severe(String method, String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.Exception)
	 */
	@Override
	public void severe(Throwable ex) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String method, String _message, Throwable ex) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#severe(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void severe(String method, Throwable ex) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#stopLogging()
	 */
	@Override
	public void stopLogging() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String)
	 */
	@Override
	public void warning(String method) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String)
	 */
	@Override
	public void warning(String method, String _message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#warning(java.lang.String, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void warning(String method, String _message, Throwable ex) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#log_exception(int, java.lang.String, java.lang.String, java.lang.Throwable, boolean)
	 */
	@Override
	public void log_exception(int theLevel, String method, String _message,
			Throwable ex, boolean useDialog) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.logging.IDynamicLogger#log(java.lang.Throwable, boolean)
	 */
	@Override
	public void log(Throwable e, boolean dialog) {
		// TODO Auto-generated method stub
		
	}

}
