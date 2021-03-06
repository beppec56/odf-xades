/**
 * 
 */
package com.yacme.ext.oxsit.uno_types;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author beppe
 * This class simply log this jar version
 *
 */
public class LogJarVersion {
	
	final String m_aJarFileName = "oxsit-uno_types.uno.jar";
	
	URI executivePath ;

	private String m_aJarVersion;
	public LogJarVersion() {		
        CodeSource aCs = LogJarVersion.class.getProtectionDomain().getCodeSource();
        if(aCs != null) {
            try {
                URL aURL = aCs.getLocation(); // where this class is 'seen' by the java runtime
                //System.out.println(aURL.toString()+" "+aURL.getPath());
                String thisFile = aURL.getFile();
                int pos = aURL.toString().indexOf(m_aJarFileName); //FIXME: _00 modificare in modo che il nome del jar sia quello giusto
                if(pos == -1) {
                    //non esiste, l'URL è il path
                    executivePath = new URI(aURL.toString());
                }
                else {
                    //esiste, elimina
                    executivePath = new URI(aURL.toString().substring(0, pos-1));
                }
            } catch (URISyntaxException e) {
//                m_aLogger.log(e, false);
            }
        }

      //legge la versione dal manifest
        try {
            URI aNew = new URI(executivePath.getScheme(),
                    executivePath.getUserInfo(), executivePath.getHost(), executivePath.getPort(),
                    executivePath.getPath()+"/"+m_aJarFileName,
                    executivePath.getQuery(),
                    executivePath.getFragment());
//            info(aNew.getPath());
            JarFile jarFile = new JarFile(aNew.getPath());
            Manifest mf = jarFile.getManifest();
            Attributes mfAttr = mf.getMainAttributes();
            //qui estrae la versione e la stampa.
            String aVers = mfAttr.getValue("Implementation-Version");
            if(aVers != null) {
                String jarVersion = "version: ";
                jarVersion += aVers;
                m_aJarVersion = jarVersion; 
            }
            //else
                //jarVersion += "not found in jar file !";
        } catch (IOException e3) {
//            m_aLogger.log(e3, false);
        } catch (URISyntaxException e) {
//            m_aLogger.log(e, false);
        }		
	}
	
	public String getVersion() {
		return m_aJarFileName+":    "+m_aJarVersion;
	}
}
