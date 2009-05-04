/**
 *	j4sign - an open, multi-platform digital signature solution
 *	Copyright (c) 2004 Roberto Resoli - Servizio Sistema Informativo - Comune di Trento.
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
/*
 * $Header: /cvsroot/j4sign/j4sign/src/java/core/it/trento/comune/j4sign/installer/Installer.java,v 1.3 2008/10/07 10:04:23 resoli Exp $
 * $Revision: 1.3 $
 * $Date: 2008/10/07 10:04:23 $
 */

package it.trento.comune.j4sign.installer;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;



/**
 * @author resolir
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Installer {

    public static void main(String[] args) {
        System.out.println("===== Smart Card Access Extension installation ====");
        String osName = System.getProperty("os.name");
        String extDirs = System.getProperty("java.ext.dirs");
        
        
        String extDir = extDirs;
        if (extDirs!=null){
            int separatorIndex = -1;
            if( osName.contains("Linux") && extDirs.contains(":") )
                separatorIndex = extDirs.indexOf(":");
            if( osName.contains("Windows") && extDirs.contains(";") )
                separatorIndex = extDirs.indexOf(";");
            if(separatorIndex != -1){
               extDir = extDirs.substring(0, separatorIndex);
               JOptionPane.showMessageDialog(null,
                    "L'installazione delle librerie verra' effettuata nella prima directory: '"+extDir+"'",
                    "Rilevata piu' di una directory per le estensioni",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
        Installer installer = new Installer();
        try {
            installer.installFile("SmartCardAccess-signed.jar", extDir);
            if(osName.indexOf("Windows") != -1){
                System.out.println("Windows OS detected.");
                installer.installFile("OCFPCSC1.dll", extDir);
                installer.installFile("pkcs11wrapper.dll", extDir);
            }else if(osName.indexOf("Linux") != -1){
                System.out.println("Linux OS detected.");
                installer.installFile("libOCFPCSC1.so", extDir);
                installer.installFile("libpkcs11wrapper.so", extDir);
            }else
                System.out.println("Native libraries for '"+osName+"' not available! Giving up.");
            
            System.out.println("==== Smart Card Access Extension installed. ====");
            JOptionPane.showMessageDialog(null,
                    "L'installazione e' stata completata\ncon successo!",
                    "Installation complete.",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.out.println("Error: "+e);
            System.out.println("==== Smart Card Access Extension NOT installed! ====");
            JOptionPane.showMessageDialog(null,
                    "L'installazione non si e' conclusa correttamente!",
                    "Installation complete.",
                    JOptionPane.ERROR_MESSAGE);
            
        }
        
        
    }

    private void installFile(String name, String destDir) throws IOException {
        File f = new File(destDir + System.getProperty("file.separator") + name);
        
        System.out.println("Installing '"+f.getAbsolutePath()+"'");
        
        boolean exists = f.isFile();
        
        InputStream in = getClass().getResourceAsStream(name);
        
        
        if (in!=null) {
            
            BufferedInputStream bufIn = new BufferedInputStream(in);
            try {
                OutputStream fout = new BufferedOutputStream(
                        new FileOutputStream(f));
                byte[] bytes = new byte[1024 * 10];
                for (int n = 0; n != -1; n = bufIn.read(bytes))
                    fout.write(bytes, 0, n);

                fout.close();
            } catch (IOException ioe) {
                // We might get an IOException trying to overwrite an existing
                // file if there is another process using the DLL.
                // If this happens, ignore errors.
                if (!exists)
                    throw ioe;
            }
        }else
            throw new IOException("Found no resource named: "+name);
        

    }
}