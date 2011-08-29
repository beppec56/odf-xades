/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.xades.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import com.yacme.ext.oxsit.cust_it.comp.security.xades.SignedDocException;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CRLFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.CanonicalizationFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.DigiDocFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.EncryptedDataParser;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.EncryptedStreamParser;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.NotaryFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.SignatureFactory;
import com.yacme.ext.oxsit.cust_it.comp.security.xades.factory.TimestampFactory;

/**
 * Configuration reader for JDigiDoc
 */
public class ConfigManager {
    /** Resource bundle */
    private static Properties m_props = null;
    /** singleton instance */
    private static ConfigManager m_instance = null;
    /** notary factory instance */
    private static NotaryFactory m_notFac = null;
    /** canonicalization factory instance */
    private static CanonicalizationFactory m_canFac = null;
    /** timestamp factory implementation */
    private static TimestampFactory m_tsFac = null;
    /** CRL factory instance */
    private static CRLFactory m_crlFac = null;
    /** XML-ENC parser factory instance */
    private static EncryptedDataParser m_dencFac = null;
    /** XML-ENC parses for large encrypted files */
    private static EncryptedStreamParser m_dstrFac = null;
    /** loh4j logger */
//    private Logger m_logger = null;
    
    /**
     * Singleton accessor
     */
    public static ConfigManager instance() {
        if(m_instance == null)
            m_instance = new ConfigManager();
        return m_instance;
    }
    
    /**
     * ConfigManager default constructor
     */
    private ConfigManager() {
    	// initialize logging
//    	if(getProperty("DIGIDOC_LOG4J_CONFIG") != null)
//    		PropertyConfigurator.configure(getProperty("DIGIDOC_LOG4J_CONFIG"));
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
            	ClassLoader cl = ConfigManager.class.getClassLoader();
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
        throws SignedDocException
    {
    	SignatureFactory sigFac = null;
        try {
        	sigFac = (SignatureFactory)Class.
                    forName(getProperty("DIGIDOC_SIGN_IMPL")).newInstance();
            sigFac.init();
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_INIT_SIG_FAC);
        }
        return sigFac;
    }
    
    /**
     * Returns the SignatureFactory instance
     * @param type type of signature factory
     * @return SignatureFactory implementation
     */
    public SignatureFactory getSignatureFactory(String type)
        throws SignedDocException
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
        		throw new SignedDocException(SignedDocException.ERR_INIT_SIG_FAC, "No signature factory of type: " + type, null);
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_INIT_SIG_FAC);
        }
        return sigFac;
    }

    /**
     * Returns the NotaryFactory instance
     * @return NotaryFactory implementation
     */
    public NotaryFactory getNotaryFactory()
        throws SignedDocException
    {
        try {
            if(m_notFac == null) {
                m_notFac = (NotaryFactory)Class.
                    forName(getProperty("DIGIDOC_NOTARY_IMPL")).newInstance();
                m_notFac.init();
            }
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_NOT_FAC_INIT);
        }
        return m_notFac;
    }

    /**
     * Returns the TimestampFactory instance
     * @return TimestampFactory implementation
     */
    public TimestampFactory getTimestampFactory()
        throws SignedDocException
    {
        try {
            if(m_tsFac == null) {
            	m_tsFac = (TimestampFactory)Class.
                    forName(getProperty("DIGIDOC_TIMESTAMP_IMPL")).newInstance();
            	m_tsFac.init();
            }
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_TIMESTAMP_FAC_INIT);
        }
        return m_tsFac;
    }

    /**
     * Returns the DigiDocFactory instance
     * @return DigiDocFactory implementation
     */
    public DigiDocFactory getSignedDocFactory()
        throws SignedDocException
    {
    	DigiDocFactory digFac = null;
        try {
        	//ROB
            digFac = (DigiDocFactory)Class.
                    forName(getProperty("SIGNEDDOC_FACTORY_IMPL")).newInstance();
            digFac.init();            
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_DIG_FAC_INIT);
        }
        return digFac;
    }
    
    /**
     * Returns the CanonicalizationFactory instance
     * @return CanonicalizationFactory implementation
     */
    public CanonicalizationFactory getCanonicalizationFactory()
        throws SignedDocException
    {
        try {
            if(m_canFac == null) {
                m_canFac = (CanonicalizationFactory)Class.
                    forName(getProperty("CANONICALIZATION_FACTORY_IMPL")).newInstance();
                //System.out.println("Config c14n: "+getProperty("CANONICALIZATION_FACTORY_IMPL"));
                m_canFac.init();
            }
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_CAN_FAC_INIT);
        }
        return m_canFac;
    }

	/**
	 * Returns the EncryptedDataParser instance
	 * @return EncryptedDataParser implementation
	 */
	public EncryptedDataParser getEncryptedDataParser()
		throws SignedDocException
	{
		try {
			if(m_dencFac == null)
				m_dencFac = (EncryptedDataParser)Class.
					forName(getProperty("ENCRYPTED_DATA_PARSER_IMPL")).newInstance();
			m_dencFac.init();            
		} catch(SignedDocException ex) {
			throw ex;
		} catch(Exception ex) {
			SignedDocException.handleException(ex, SignedDocException.ERR_DIG_FAC_INIT);
		}
		return m_dencFac;
	}

	/**
	 * Returns the EncryptedStreamParser instance
	 * @return EncryptedStreamParser implementation
	 */
	public EncryptedStreamParser getEncryptedStreamParser()
		throws SignedDocException
	{
		try {
			if(m_dstrFac == null)
				m_dstrFac = (EncryptedStreamParser)Class.
					forName(getProperty("ENCRYPTED_STREAM_PARSER_IMPL")).newInstance();
			m_dstrFac.init();            
		} catch(SignedDocException ex) {
			throw ex;
		} catch(Exception ex) {
			SignedDocException.handleException(ex, SignedDocException.ERR_DIG_FAC_INIT);
		}
		return m_dstrFac;
	}

    /**
     * Returns the CRLFactory instance
     * @return CRLFactory implementation
     */
    public CRLFactory getCRLFactory()
        throws SignedDocException
    {
        try {
            if(m_crlFac == null) {
                m_crlFac = (CRLFactory)Class.
                    forName(getProperty("CRL_FACTORY_IMPL")).newInstance();
                m_crlFac.init();
            }
        } catch(SignedDocException ex) {
            throw ex;
        } catch(Exception ex) {
            SignedDocException.handleException(ex, SignedDocException.ERR_INIT_CRL_FAC);
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
