/**
 * 
 */
package it.plio.ext.oxsit.dispatchers;

import com.sun.star.frame.XDispatch;
import com.sun.star.lang.XComponent;

/**
 * This interface is needed in order to use both the XDispatch and XComponent
 * interfaces in dispatch implementors.
 * XDispatch is used by UNO, while XComponent is used as a convenience in
 * the extension
 * 
 * @author beppec56
 *
 */
public interface IDispatchBaseObject extends XDispatch, XComponent {

}
