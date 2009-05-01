/**
 * 
 */
package it.plio.ext.oxsit.ooo.ui;

import com.sun.star.awt.XControl;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;

/**
 * @author beppe
 *
 */
public class FixedFontPitchTreeElement extends MultilineTreeElementBase {

	/**
	 * @param context
	 * @param _xmcf
	 */
	public FixedFontPitchTreeElement(XComponentContext context,
			XMultiComponentFactory _xmcf, String _sContentString, XControl _xTheTextControl) {
		super(context, _xmcf, _sContentString, _xTheTextControl);
		m_bSetFixedPitch = true;
	}
}
