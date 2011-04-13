/**
 * 
 */
package com.yacme.ext.oxsit.cust_it.comp.security.odfdoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;



public class ODFDocument
{
    private Map<String, byte[]> jarContents = null;
    
    public ODFDocument(InputStream inputStream) throws IOException
    {
        JarInputStream jarInputStream = new JarInputStream(inputStream);
        
        jarContents = new HashMap<String, byte[]>();
        
        JarEntry entry = null;
    
        while ((entry = jarInputStream.getNextJarEntry()) != null)
        {
            if (!entry.isDirectory())
            {
                String entryName = entry.getName();
                byte[] data = inputStreamToByteArray(jarInputStream);
                
                jarContents.put(entryName, data);
            }
        }        
    }
    
    public byte[] getEntry(String entryName) throws IOException
    {        
        return jarContents.get(entryName);
    }

    public boolean hasEntry(String entryName) throws IOException
    {        
        return jarContents.containsKey(entryName);
    }
    
    public ArrayList<String> getFileList() throws IOException
    {        
        return new ArrayList<String>(jarContents.keySet());
    }
    
    public static byte[] inputStreamToByteArray(InputStream in) throws IOException
    {
        byte[] buffer = new byte[2048];
        int length = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while ((length = in.read(buffer)) >= 0)
        {
            baos.write(buffer, 0, length);
        }

        return baos.toByteArray();
    }
}
