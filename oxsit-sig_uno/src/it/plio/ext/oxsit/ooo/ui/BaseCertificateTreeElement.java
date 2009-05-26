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

import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.ooo.registry.MessageConfigurationAccess;
import it.plio.ext.oxsit.security.cert.CertificateState;
import it.plio.ext.oxsit.security.cert.CertificateStateConditions;
import it.plio.ext.oxsit.security.cert.CertificationAuthorityState;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/** This class describes the node representing a certificate obtained from
 * an SSCD.
 * This element shows on the right side of the dialog a series of text lines, with the status of
 * the certificate.
 * 
 * @author beppec56
 *
 */
public class BaseCertificateTreeElement extends TreeElement {

	// describes the field for certificate/signature status common to both
	protected  int m_nFIELD_OWNER_NAME 					= 0;
	protected  int m_nFIELD_CERTIFICATE_STATE 			= 1;
	protected  int m_nFIELD_CERTIFICATE_VERF_CONDITIONS	= 2;
	protected  int m_nFIELD_TITLE_ISSUER	 			= 7;
	protected  int m_nFIELD_ISSUER 						= 8;
	protected  int m_nFIELD_ISSUER_CN					= 9;
	protected  int m_nFIELD_ISSUER_VERF_CONDITIONS		= 10;

	/**
	 * constants for signature and document verification state conditions
	 * 
	 */
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_ENABLED = 0;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_NO_SIG_OPT = 1;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_NO_DOC_OPT = 2;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_DISAB = 3;
	public static final int m_nDOCUMENT_VERIF_STATE_CONDT_NO_INET = 4;
	/**
	 * the corresponding strings identifier, to retrieve the string from resources.
	 */
	public static final String[]  m_sDOCUMENT_VERIF_STATE_CONDT =  { 
							"err_txt_docum_verf_condt_ok",
							"err_txt_docum_verf_condt_nosig",
							"err_txt_docum_verf_condt_nodocu",
							"err_txt_docum_verf_condt_disb",
							"err_txt_verf_condt_no_inet"
						};

	//hash table to convert the enum of the certificate state to the id string in resources
	/* the mapping from strings to emun state is:
	 * 1) the enum is edited/changed in the IDL file
	 * 2) the corresponding string plus id is added to the localization file
	 * 3) change this hashmap to connect the enum to the id of the string
	 * 
	 *   the code will take care of the rest
	 */
	public static Hashtable<CertificateState,String>	m_aCERTIFICATE_STATE = new Hashtable<CertificateState, String>(15);	
	static {
		m_aCERTIFICATE_STATE.put(CertificateState.NOT_VERIFIABLE, "err_txt_cert_nover");
		m_aCERTIFICATE_STATE.put(CertificateState.NOT_YET_VERIFIED, "err_txt_cert_noyver");
		m_aCERTIFICATE_STATE.put(CertificateState.OK, "err_txt_cert_ok");
		m_aCERTIFICATE_STATE.put(CertificateState.EXPIRED, "err_txt_cert_exp");
		m_aCERTIFICATE_STATE.put(CertificateState.REVOKED, "err_txt_cert_rev");
		m_aCERTIFICATE_STATE.put(CertificateState.SUSPENDED, "err_txt_cert_suspen");
		m_aCERTIFICATE_STATE.put(CertificateState.NOT_ACTIVE, "err_txt_cert_noact");
		m_aCERTIFICATE_STATE.put(CertificateState.NOT_COMPLIANT, "err_txt_cert_noconf");
		m_aCERTIFICATE_STATE.put(CertificateState.ERROR_IN_EXTENSION, "err_txt_cert_ko_extension");
		m_aCERTIFICATE_STATE.put(CertificateState.MISSING_EXTENSION, "err_txt_cert_miss_ext");
		m_aCERTIFICATE_STATE.put(CertificateState.CORE_CERTIFICATE_ELEMENT_INVALID, "err_txt_cert_ko_core");
		m_aCERTIFICATE_STATE.put(CertificateState.MALFORMED_CERTIFICATE, "err_txt_cert_no_read");
	};
	//hash table to convert the enum of the certificate state to the actual strings in resources
	public static Hashtable<CertificateState,String>	m_aCERTIFICATE_STATE_STRINGS;	

	//hash table to convert the enum of the certificate state conditions to the id string in resources
	public static Hashtable<CertificateStateConditions,String>	m_aCERTIFICATE_STATE_CONDITIONS = new Hashtable<CertificateStateConditions, String>(15);
	
	static {		
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED,"revocation_not_yet_controlled");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.REVOCATION_CONTROLLED_OK,"revocation_controlled_ok");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.REVOCATION_CONTROL_NOT_ENABLED,"revocation_control_not_enabled");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.CRL_CANNOT_BE_ACCESSED,"crl_cannot_be_accessed");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.CRL_CANNOT_BE_VERIFIED,"crl_cannot_be_verified");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.OCSP_CANNOT_BE_ACCESSED,"ocsp_cannot_be_accessed");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.INET_ACCESS_NOT_ENABLED,"inet_access_not_enabled");
		m_aCERTIFICATE_STATE_CONDITIONS.put(CertificateStateConditions.INET_ACCESS_ERROR,"inet_access_error");
	};
	//hash table to convert the enum of the certificate state conditions to the actual strings in resources
	public static Hashtable<CertificateStateConditions,String>	m_aCERTIFICATE_STATE_CONDITIONS_STRINGS;	
	
	//hash table to convert the enum of the certification authority state to the id string in resources
	public static Hashtable<CertificationAuthorityState,String>	m_aCA_STATE_CONDITIONS = new Hashtable<CertificationAuthorityState, String>(15);	
	static {		
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.NOT_YET_CHECKED,"ca_not_yet_checked");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.CANNOT_BE_CHECKED,"ca_cannot_be_checked");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.NOT_TRUSTED,"ca_not_trusted");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.TRUSTED,"ca_trusted");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.TRUSTED_WITH_WARNING,"ca_trusted_with_warning");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.CA_LIST_DB_THUMBP_NOT_CHECKED,"ca_list_db_thumbp_not_checked");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.NOT_COMPLIANT,"ca_not_compliant");
		m_aCA_STATE_CONDITIONS.put(CertificationAuthorityState.CA_LIST_DB_MISSING,"ca_list_db_missing");
	};
	//hash table to convert the enum of the certification authority state to the actual strings in resources
	public static Hashtable<CertificationAuthorityState,String>	m_aCA_STATE_CONDITIONS_STRINGS;	

	/**
	 * the string list showed on the left, allocated, according to global constants.
	 */
	public String[]	m_sStringList;
	/** the control to print
	 *  the above strings
	 */
	private Object[] m_xControlLines;

	/**
	 * 
	 */
	private XControl m_xBackgroundControl;


	protected MessageConfigurationAccess m_aRegAcc = null; 

	public BaseCertificateTreeElement(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
		setNodeType(TreeNodeType.CERTIFICATE);
		setLogger(new DynamicLogger(this,_xContext));
//		getLogger().enableLogging();
		setMultiComponentFactory(_xMCF);
		setComponentContext(_xContext);
		setCertificateState(CertificateState.NOT_YET_VERIFIED_value);
		setCertificateStateConditions(CertificateStateConditions.REVOCATION_NOT_YET_CONTROLLED_value);
		setCertificationAutorityState(CertificationAuthorityState.NOT_YET_CHECKED_value);
		setNodeGraphic(null);
		m_sStringList = new String[CertifTreeDlgDims.m_nMAXIMUM_FIELDS];
		m_xControlLines = new Object[CertifTreeDlgDims.m_nMAXIMUM_FIELDS];
		initStaticCertificateStrings();
		initStaticCertificateConditionStrings();
		initStaticCertificationAuthorityStrings();
		if(m_aRegAcc != null) {
			m_aRegAcc.dispose();
			m_aRegAcc = null;
		}
	}

	/**
	 * 
	 */
	private void initStaticCertificationAuthorityStrings() {
		if(m_aCA_STATE_CONDITIONS_STRINGS == null) {
			//init it once per element
			m_aCA_STATE_CONDITIONS_STRINGS = new Hashtable<CertificationAuthorityState, String>(10);
			if(m_aRegAcc == null)
				m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
			if(m_aRegAcc != null) {
				Set<CertificationAuthorityState> aKeySet = m_aCA_STATE_CONDITIONS.keySet();
				Iterator<CertificationAuthorityState> it = aKeySet.iterator();
		        while (it.hasNext()) {
					try {
						CertificationAuthorityState cs = it.next();
						m_aCA_STATE_CONDITIONS_STRINGS.put(cs, 
								m_aRegAcc.getStringFromRegistry(
										m_aCA_STATE_CONDITIONS.get(cs
																)
																)
														);
					} catch (com.sun.star.uno.Exception e) {
						getLogger().severe(e);
					}
		        }
			}
		}		
	}

	/**
	 * 
	 */
	private void initStaticCertificateConditionStrings() {
		if(m_aCERTIFICATE_STATE_CONDITIONS_STRINGS == null) {
			//init it once per element
			m_aCERTIFICATE_STATE_CONDITIONS_STRINGS = new Hashtable<CertificateStateConditions, String>(10);
			if(m_aRegAcc == null)
				m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
			if(m_aRegAcc != null) {
				Set<CertificateStateConditions> aKeySet = m_aCERTIFICATE_STATE_CONDITIONS.keySet();
				Iterator<CertificateStateConditions> it = aKeySet.iterator();
		        while (it.hasNext()) {
					try {
						CertificateStateConditions cs = it.next();
						m_aCERTIFICATE_STATE_CONDITIONS_STRINGS.put(cs, 
								m_aRegAcc.getStringFromRegistry(
										m_aCERTIFICATE_STATE_CONDITIONS.get(cs
																)
																)
														);
					} catch (com.sun.star.uno.Exception e) {
						getLogger().severe(e);
					}
		        }
			}
		}		
	}

	/**
	 * 
	 */
	private void initStaticCertificateStrings() {
		if(m_aCERTIFICATE_STATE_STRINGS == null) {
			//init it once per element
			m_aCERTIFICATE_STATE_STRINGS = new Hashtable<CertificateState, String>(10);
			if(m_aRegAcc == null)
				m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
			if(m_aRegAcc != null) {
				Set<CertificateState> aKeySet = m_aCERTIFICATE_STATE.keySet();
				Iterator<CertificateState> it = aKeySet.iterator();
		        while (it.hasNext()) {
					try {
						CertificateState cs = it.next();
						m_aCERTIFICATE_STATE_STRINGS.put(cs, 
								m_aRegAcc.getStringFromRegistry(
										m_aCERTIFICATE_STATE.get(cs
																)
																)
														);
					} catch (com.sun.star.uno.Exception e) {
						getLogger().severe(e);
					}
		        }
			}
		}		
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#initialize()
	 * 
	 * initializes the element common to both the signature and certificate
	 */
	@Override
	public void initialize() {
//inizialize string grabber
		m_aRegAcc = new MessageConfigurationAccess(getComponentContext(), getMultiComponentFactory());
		// allocate string needed for display
		// these are the string common to both the signature and the 
		// certificate
		try {
			//initializes fixed string (titles)
			m_sStringList[m_nFIELD_TITLE_ISSUER] = m_aRegAcc.getStringFromRegistry("cert_title_issuer" );

			//grab the string for certificate status
			//FIXME: a better solution would be to substitute the id in the Hashmap with the
			// corresponding string, to be done once when element is instantiated
			try {
			m_sStringList[m_nFIELD_CERTIFICATE_STATE] =
							m_aCERTIFICATE_STATE_STRINGS.get(
									CertificateState.fromInt(getCertificateState())
											);
			m_sStringList[m_nFIELD_CERTIFICATE_VERF_CONDITIONS] =
						m_aCERTIFICATE_STATE_CONDITIONS_STRINGS.get(
								CertificateStateConditions.fromInt(getCertificateStateConditions())
										);

			m_sStringList[m_nFIELD_ISSUER_VERF_CONDITIONS] =
						m_aCA_STATE_CONDITIONS_STRINGS.get(
								CertificationAuthorityState.fromInt(getCertificationAutorityState())
										);
			} catch (java.lang.Exception e) {
				getLogger().severe("initialize", e);
			}
		} catch (java.lang.Exception e) {
			getLogger().severe("initialize", e);
		}
		m_aRegAcc.dispose();
	}

	/* (non-Javadoc)
	 * @see it.plio.ext.oxsit.ooo.ui.TreeElement#EnableDisplay(boolean)
	 * this methods simply enabled/disable the display of the controls
	 */
	@Override
	void EnableDisplay(boolean bWhat) {
//		getLogger().log("EnableDisplay "+bWhat);
		XWindow xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, getBackgroundControl() );
		if(xaWNode != null )					
			xaWNode.setVisible(bWhat);

		if(bWhat == false) {	
			for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
				XControl xTFControl = (XControl)getControlLine()[i];
				xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
				if(xaWNode != null )					
					xaWNode.setVisible(bWhat);
			}
		}
		else {
			/*ouput them
			 * for every string meant for the multi fixed text display
			 *  devise a method to change the character font strike
			 *  the first character in the string marks the stroke type
			 *  (see function: it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.EnableDisplay(boolean) for
			 *  for information on how they are interpreted):
			 *   b	the string will be in bold
			 *   B
			 *   i	the string will be output in italic single underlined 
			 *   s  the string will striken out regular
			 *   S  the string will striken out bold
			 *   w	text background will be in ControlDims.DLG_CERT_TREE_STATE_WARNING_COLOR
			 *   e  text background will be in ControlDims.DLG_CERT_TREE_STATE_ERROR_COLOR;
			 */
			for(int i = 0; i < CertifTreeDlgDims.m_nMAXIMUM_FIELDS; i++) {
				XControl xTFControl = (XControl)getControlLine()[i];
				xaWNode = (XWindow)UnoRuntime.queryInterface( XWindow.class, xTFControl );
				if(xaWNode != null ) {
					//check the interface, should be the one for fixed text
					XFixedText aFText =  (XFixedText)UnoRuntime.queryInterface( XFixedText.class, xTFControl );
					if(aFText != null) {
						String sStr = m_sStringList[i];
						if(sStr != null && sStr.length() > 0 ) {
							//grab the font width property
							//								Utilities.showInterfaces(xTFControl);
							XPropertySet xTFModelPSet = (XPropertySet) UnoRuntime.queryInterface(
									XPropertySet.class, xTFControl.getModel());
							//DEBUG: print properties:
							/*									Utilities.showProperties(this, xTFModelPSet);*/
							//detect the first char
							String sFirst = m_sStringList[i].substring(0,1);
							// set the width accordingly 
							try {
								// back is the back color of the control, not the font
								int nBackCol =  ControlDims.DLG_CERT_TREE_BACKG_COLOR;
								// get the font descriptor props
								FontDescriptor xf = (FontDescriptor)xTFModelPSet.getPropertyValue("FontDescriptor");
								xf.Slant  = com.sun.star.awt.FontSlant.NONE;
								xf.Underline = com.sun.star.awt.FontUnderline.NONE;
								xf.Weight = FontWeight.NORMAL;
								if(sFirst.equalsIgnoreCase("b")) {
									xf.Weight = FontWeight.BOLD;
								}
								else if(sFirst.equalsIgnoreCase("w")) {
									xf.Weight = FontWeight.BOLD;
									//set backgound color for 'e' errors, bold+
									nBackCol = ControlDims.DLG_CERT_TREE_STATE_WARNING_COLOR;
								}
								else if(sFirst.equalsIgnoreCase("e")) {
									xf.Weight = FontWeight.BOLD;
									//set backgound color for 'e' errors, bold+
									nBackCol = ControlDims.DLG_CERT_TREE_STATE_ERROR_COLOR;
								}
								else if(sFirst.equalsIgnoreCase("i")) {
									// italic, underline single
									xf.Slant  = com.sun.star.awt.FontSlant.ITALIC;
									xf.Underline = com.sun.star.awt.FontUnderline.SINGLE;
								}
								xTFModelPSet.setPropertyValue("BackgroundColor", new Integer( nBackCol ));
								xTFModelPSet.setPropertyValue("FontDescriptor", xf);
							} catch (UnknownPropertyException e) {
								getLogger().severe("EnableDisplay", e);
							} catch (PropertyVetoException e) {
								getLogger().severe("EnableDisplay", e);
							} catch (IllegalArgumentException e) {
								getLogger().severe("EnableDisplay", e);
							} catch (WrappedTargetException e) {
								getLogger().severe("EnableDisplay", e);
							}
							// set the string
							aFText.setText(sStr.substring(1));
						}
						else
							aFText.setText("<found null string> "+i);//debug, remove after test
						xaWNode.setVisible(bWhat);
					}
				}
				else {
					getLogger().log("missing XFixedText interface");
/*					XTextComponent aText =  (XTextComponent)UnoRuntime.queryInterface( XTextComponent.class, xTFControl );
					if( aText != null) // then it's the background  control in background color
						xaWNode.setVisible(true); //match if edit control
					else
						xaWNode.setVisible(false);*/
				}
			}
		}
	}

	/**
	 * @param m_xControlLines the m_xControlLines to set
	 */
	public void setAControlLine(Object _xControlLine, int _nIndex) {
		this.m_xControlLines[_nIndex] = _xControlLine;
	}

	/**
	 * @return the m_xControlLines
	 */
	public Object[] getControlLine() {
		return m_xControlLines;
	}

	/**
	 * @param m_xBackgroundControl the m_xBackgroundControl to set
	 */
	public void setBackgroundControl(XControl m_xBackgroundControl) {
		this.m_xBackgroundControl = m_xBackgroundControl;
	}

	/**
	 * @return the m_xBackgroundControl
	 */
	public XControl getBackgroundControl() {
		return m_xBackgroundControl;
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void addEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
	 */
	@Override
	public void removeEventListener(XEventListener arg0) {
		// TODO Auto-generated method stub
	}
}
