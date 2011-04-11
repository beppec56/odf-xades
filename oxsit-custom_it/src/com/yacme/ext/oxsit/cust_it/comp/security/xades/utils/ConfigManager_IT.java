/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedODFDocumentException_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CRLFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.DigiDocFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.NotaryFactory_IT;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.TimestampFactory_IT;

/**
 * Configuration reader for JDigiDoc
 */
public class ConfigManager_IT {
    /** Resource bundle */
    private static Properties m_props = null;
    /** singleton instance */
    private static ConfigManager_IT m_instance = null;
    /** notary factory instance */
    private static NotaryFactory_IT m_notFac = null;
    /** canonicalization factory instance */
    private static CanonicalizationFactory_IT m_canFac = null;
    /** timestamp factory implementation */
    private static TimestampFactory_IT m_tsFac = null;
    /** CRL factory instance */
    private static CRLFactory_IT m_crlFac = null;
    /** XML-ENC parser factory instance */
    private static EncryptedDataParser m_dencFac = null;
    /** XML-ENC parses for large encrypted files */
    private static EncryptedStreamParser m_dstrFac = null;
    /** loh4j logger */
//    private Logger m_logger = null;
    
    /**
     * Singleton accessor
     */
    public static ConfigManager_IT instance() {
        if(m_instance == null)
            m_instance = new ConfigManager_IT();
        return m_instance;
    }
    
    /**
     * ConfigManager default constructor
     */
    private ConfigManager_IT() {
    	// initialize logging
    	if(getProperty("DIGIDOC_LOG4J_CONFIG") != null)
    		PropertyConfigurator.configure(getProperty("DIGIDOC_LOG4J_CONFIG"));
//    	m_logger = Logger.getLogger(ConfigManager.class);
    }
    
    /**
     * Resets the configuration table
     */
    public void reset() {
    	m_props = new Properties();
    }
         
    /**
     * Init method for reading the config data
     * from a properties file. Note that this method
     * doesn't reset the configuration table held in
     * memory. Thus you can use it multpile times and
     * add constantly new configuration entries. Use the
     * reset() method to reset the configuration table.
     * @param cfgFileName config file anme or URL
     * @return success flag
     */
    public static boolean init(String cfgFileName) {
    	boolean bOk = false;
        try {
        	if(m_props == null)
        		m_props = new Properties();
            InputStream isCfg = null;
            URL url = null;
            if(cfgFileName.startsWith("http")) {
                url = new URL(cfgFileName);
                isCfg = url.openStream();
            } else if(cfgFileName.startsWith("jar://")) {
            	ClassLoader cl = ConfigManager_IT.class.getClassLoader();
                isCfg = cl.getResourceAsStream(cfgFileName.substring(6));
            } else {
                isCfg = new FileInputStream(cfgFileName);
            }
            m_props.load(isCfg);
            isCfg.close();
            url = null; 
			bOk = true;
        } catch (Exception ex) {            
            System.err.println("Cannot read config file: " + 
                cfgFileName + " Reason: " + ex.toString());
        }
        // initialize
        return bOk;
    }
         
    /**
     * Init method for settings the config data
     * from a any user defined source
     * @param hProps config data
     */
    public static void init(Hashtable hProps) {
    	m_props = new Properties();
      	m_props.putAll(hProps);
    }
    
    /**
     * Returns the SignatureFactory instance
     * @return SignatureFactory implementation
     */
    public SignatureFactory getSignatureFactory()
        throws SignedODFDocumentException_IT
    {
    	SignatureFactory sigFac = null;
        try {
        	sigFac = (SignatureFactory)Class.
                    forName(getProperty("DIGIDOC_SIGN_IMPL")).newInstance();
            sigFac.init();
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_INIT_SIG_FAC);
        }
        return sigFac;
    }
    
    /**
     * Returns the SignatureFactory instance
     * @param type type of signature factory
     * @return SignatureFactory implementation
     */
    public SignatureFactory getSignatureFactory(String type)
        throws SignedODFDocumentException_IT
    {
    	SignatureFactory sigFac = null;
        try {
        	String strClass = getProperty("DIGIDOC_SIGN_IMPL_" + type);
        	if(strClass != null) {
        		sigFac = (SignatureFactory)Class.
                    forName(strClass).newInstance();
                if(sigFac != null)
            		sigFac.init();
        	}
        	if(sigFac == null)
        		throw new SignedODFDocumentException_IT(SignedODFDocumentException_IT.ERR_INIT_SIG_FAC, "No signature factory of type: " + type, null);
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_INIT_SIG_FAC);
        }
        return sigFac;
    }

    /**
     * Returns the NotaryFactory instance
     * @return NotaryFactory implementation
     */
    public NotaryFactory_IT getNotaryFactory()
        throws SignedODFDocumentException_IT
    {
        try {
            if(m_notFac == null) {
                m_notFac = (NotaryFactory_IT)Class.
                    forName(getProperty("DIGIDOC_NOTARY_IMPL")).newInstance();
                m_notFac.init();
            }
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_NOT_FAC_INIT);
        }
        return m_notFac;
    }

    /**
     * Returns the TimestampFactory instance
     * @return TimestampFactory implementation
     */
    public TimestampFactory_IT getTimestampFactory()
        throws SignedODFDocumentException_IT
    {
        try {
            if(m_tsFac == null) {
            	m_tsFac = (TimestampFactory_IT)Class.
                    forName(getProperty("DIGIDOC_TIMESTAMP_IMPL")).newInstance();
            	m_tsFac.init();
            }
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_TIMESTAMP_FAC_INIT);
        }
        return m_tsFac;
    }

    /**
     * Returns the DigiDocFactory instance
     * @return DigiDocFactory implementation
     */
    public DigiDocFactory_IT getDigiDocFactory()
        throws SignedODFDocumentException_IT
    {
    	DigiDocFactory_IT digFac = null;
        try {
            digFac = (DigiDocFactory_IT)Class.
                    forName(getProperty("DIGIDOC_FACTORY_IMPL")).newInstance();
            digFac.init();            
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_DIG_FAC_INIT);
        }
        return digFac;
    }
    
    /**
     * Returns the CanonicalizationFactory instance
     * @return CanonicalizationFactory implementation
     */
    public CanonicalizationFactory_IT getCanonicalizationFactory()
        throws SignedODFDocumentException_IT
    {
        try {
            if(m_canFac == null) {
                m_canFac = (CanonicalizationFactory_IT)Class.
                    forName(getProperty("CANONICALIZATION_FACTORY_IMPL")).newInstance();
                m_canFac.init();
            }
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_CAN_FAC_INIT);
        }
        return m_canFac;
    }

	/**
	 * Returns the EncryptedDataParser instance
	 * @return EncryptedDataParser implementation
	 */
	public EncryptedDataParser getEncryptedDataParser()
		throws SignedODFDocumentException_IT
	{
		try {
			if(m_dencFac == null)
				m_dencFac = (EncryptedDataParser)Class.
					forName(getProperty("ENCRYPTED_DATA_PARSER_IMPL")).newInstance();
			m_dencFac.init();            
		} catch(SignedODFDocumentException_IT ex) {
			throw ex;
		} catch(Exception ex) {
			SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_DIG_FAC_INIT);
		}
		return m_dencFac;
	}

	/**
	 * Returns the EncryptedStreamParser instance
	 * @return EncryptedStreamParser implementation
	 */
	public EncryptedStreamParser getEncryptedStreamParser()
		throws SignedODFDocumentException_IT
	{
		try {
			if(m_dstrFac == null)
				m_dstrFac = (EncryptedStreamParser)Class.
					forName(getProperty("ENCRYPTED_STREAM_PARSER_IMPL")).newInstance();
			m_dstrFac.init();            
		} catch(SignedODFDocumentException_IT ex) {
			throw ex;
		} catch(Exception ex) {
			SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_DIG_FAC_INIT);
		}
		return m_dstrFac;
	}

    /**
     * Returns the CRLFactory instance
     * @return CRLFactory implementation
     */
    public CRLFactory_IT getCRLFactory()
        throws SignedODFDocumentException_IT
    {
        try {
            if(m_crlFac == null) {
                m_crlFac = (CRLFactory_IT)Class.
                    forName(getProperty("CRL_FACTORY_IMPL")).newInstance();
                m_crlFac.init();
            }
        } catch(SignedODFDocumentException_IT ex) {
            throw ex;
        } catch(Exception ex) {
            SignedODFDocumentException_IT.handleException(ex, SignedODFDocumentException_IT.ERR_INIT_CRL_FAC);
        }
        return m_crlFac;
    }
   
    /**
     * Retrieves the value for the spcified key
     * @param key property name
     */
    public String getProperty(String key) {
        return m_props.getProperty(key);        
    }
   
    /**
     * Retrieves a string value for the spcified key
     * @param key property name
     * @param def default value
     */
    public String getStringProperty(String key, String def) {
        return m_props.getProperty(key, def);        
    }
   
    /**
     * Retrieves an int value for the spcified key
     * @param key property name
     * @param def default value
     */
    public int getIntProperty(String key, int def) {
        int rc = def;
        try {
        	String s = m_props.getProperty(key);
        	if(s != null && s.trim().length() > 0)
        		rc = Integer.parseInt(s);    
        } catch(NumberFormatException ex) {
//            m_logger.error("Error parsing number: " + key, ex);
        }
        return rc;
    }

    /**
     * Retrieves a long value for the spcified key
     * @param key property name
     * @param def default value
     */
    public long getLongProperty(String key, long def) {
    	long rc = def;
        try {
        	String s = m_props.getProperty(key);
        	if(s != null && s.trim().length() > 0)
        		rc = Long.parseLong(s);    
        } catch(NumberFormatException ex) {
//            m_logger.error("Error parsing number: " + key, ex);
        }
        return rc;
    }
    
}
