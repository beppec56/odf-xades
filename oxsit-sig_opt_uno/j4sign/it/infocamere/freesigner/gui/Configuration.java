/**
 *	Freesigner - a j4sign-based open, multi-platform digital signature client
 *	Copyright (c) 2005 Francesco Cendron
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either version 2
 *	of the License, or (at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package it.infocamere.freesigner.gui;

import java.io.*;
import java.util.*;




/**
 * Manager of configuration paramaters:<br>
 * <br>
 * defaultReader: Name of default reader <br>
 * lib: required reader library <br>
 * PKCS7format: format of PKCS7 file (base64 or DER?) <br>
 * useProxy: true if proxy is used <br>
 * host: proxy host <br>
 * port: proxy port <br>
 * userName: user Name <br>
 * passWord: password <br>
 * CRLupdate: true if CRL is always checked<br>
 * <br>
 * 
 * Gestisce i vari parametri di configurazione dell'applicativo:<br>
 * <br>
 * defaultReader: Nome del lettore predefinito <br>
 * lib: libreria associata al lettore <br>
 * PKCS7format: formato di salvataggio dei file firmati <br>
 * useProxy: true se si utilizza il proxy <br>
 * host: nome dell'host per la connessione proxy <br>
 * port: nome della porta per la connessione proxy <br>
 * userName: nome utente <br>
 * passWord: password <br>
 * CRLupdate: true se la verifica della CRL avviene sempre
 * 
 * 
 * @author Francesco Cendron
 * 
 */

public class Configuration {
    private static Configuration instance;

    private Properties properties;

    private String defaultReader;

    private String lib;

    private String PKCS7format;

    private boolean useProxy;

    private String host;

    private String port;

    private String userName = null;

    private String passWord = null;

    private boolean CRLupdate;

    private String confPath;

    // ROB
    private String CNIPA_dir;

    private String CNIPA_roots;

    private String CNIPA_ca;

    private boolean proxyUsingUserPassword;
    
    private String acceptCAmsg;

    public String getAcceptCAmsg() {
		return acceptCAmsg;
	}

	private Configuration() {
        load();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * Load configuration from file "conf.properties"<br>
     * <br>
     * Carica la configurazione dell'applicativo dal file "conf.properties"
     */

    private void load() {

        System.out.println("Loading configuration...");

        properties = new Properties();
        FileInputStream propertyStream;
        File dir1 = new File(".");
        String curDir = null;
        try {
            curDir = dir1.getCanonicalPath();
        } catch (IOException ex1) {
        }

        confPath =
        // System.getProperty("user.home")
        curDir + System.getProperty("file.separator") + "conf"
                + System.getProperty("file.separator") + "conf.properties";

        propertyStream = null;
        try {
            propertyStream = new FileInputStream(confPath);
        } catch (FileNotFoundException ex) {
        }

        if (propertyStream != null) {
            try {
                properties.load(propertyStream);
            } catch (IOException e2) {
                System.out.println(e2);
            }
            // prop.list(System.out);
        }

        defaultReader = properties.getProperty("defaultReader");
        lib = properties.getProperty("lib");
        PKCS7format = properties.getProperty("PKCS7format");
        useProxy = properties.getProperty("useProxy").equals("true");

        host = properties.getProperty("host");
        port = properties.getProperty("port");
        if ("".equals(host.trim()) || "".equals(port.trim()))
            host = port = null;

        // userName = properties.getProperty("userName");
        // passWord = properties.getProperty("passWord");

        CRLupdate = properties.getProperty("CRLupdate").equals("true");
        CNIPA_dir = properties.getProperty("CNIPA_dir");
        CNIPA_roots = properties.getProperty("CNIPA_roots");
        CNIPA_ca = properties.getProperty("CNIPA_ca");
        proxyUsingUserPassword = properties.getProperty(
                "proxyUsingUserPassword").equals("true");

        if (useProxy
                && (proxyUsingUserPassword && ((userName == null) || (passWord == null)))) {
            UserPassDialog d = new UserPassDialog();
            d.show();
            String pc = d.getCredentials();
            if (pc != null) {
                userName = pc.substring(0, pc.indexOf(":"));
                passWord = pc.substring(pc.indexOf(":") + 1);
            }
        }
        
        acceptCAmsg = properties.getProperty("AcceptCAmsg");

    }

    /**
     * Save configuration to file "conf.properties"<br>
     * <br>
     * Salva la configurazione dell'applicativo nel file "conf.properties"
     */

    public void save() {
        System.out.println("Saving configuration...");

        properties.setProperty("defaultReader", defaultReader);
        properties.setProperty("lib", ""+lib);
        properties.setProperty("PKCS7format", PKCS7format);
        properties.setProperty("useProxy", "" + useProxy);
        properties.setProperty("host", ""+host);
        properties.setProperty("port", ""+port);

        // properties.setProperty("userName", userName);
        // properties.setProperty("passWord", passWord);

        properties.setProperty("CRLupdate", "" + CRLupdate);
        properties.setProperty("proxyUsingUserPassword", ""
                + proxyUsingUserPassword);

        try {
            properties.store(new FileOutputStream(confPath), null);
        } catch (IOException e) {

        }
        load();

    }

    public void setReader(String attribute) {
        defaultReader = attribute;
    }

    public String getReader() {
        return defaultReader;
    }

    public void setLib(String attribute) {
        lib = attribute;
    }

    public String getLib() {
        return lib;
    }

    public void setPKCS7format(String attribute) {
        PKCS7format = attribute;
    }

    public String getPKCS7format() {
        return PKCS7format;
    }

    public void setHost(String attribute) {
        host = attribute;
    }

    public String getHost() {
        return host;
    }

    public String getConfPath() {
        return confPath;
    }

    public void setPort(String attribute) {
        port = attribute;
    }

    public String getPort() {
        return port;
    }

    public void setUserName(String attribute) {
        userName = attribute;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassWord(String attribute) {
        passWord = attribute;
    }

    public String getPassWord() {
        // String s = "";
        // if (passWord.length() != 0) {
        // PasswordCrypting k = new PasswordCrypting();
        // s = k.decrypt(passWord);
        // System.out.println(passWord);
        // System.out.println(s);
        // }

        return passWord;
    }

    public void setUsingProxy(boolean attribute) {
        useProxy = attribute;
    }

    public boolean isUsingProxy() {
        return useProxy;
    }

    public boolean getCRLupdate() {
        return CRLupdate;
    }

    public void setCRLupdate(boolean attribute) {
        CRLupdate = attribute;
    }

    public String toString() {
        return defaultReader;
    }

    public String getCNIPA_dir() {
        return CNIPA_dir;
    }

    public String getCNIPA_roots() {
        return CNIPA_roots;
    }

    public String getCNIPA_ca() {
        return CNIPA_ca;
    }

    public boolean isProxyUsingUserPassword() {
        return proxyUsingUserPassword;
    }

    public void setProxyUsingUserPassword(boolean proxyUsingUserPasword) {
        this.proxyUsingUserPassword = proxyUsingUserPasword;
    }

}
