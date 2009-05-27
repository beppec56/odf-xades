/*************************************************************************
 * 
 *  This code is derived from
 *  it.trento.comune.j4sign.pcsc.PCSCHelper class in j4sign
 *  adapted to be used in OOo UNO environment
 *  Copyright (c) 2005 Francesco Cendron - Infocamere
 *
 *  For OOo UNO adaptation:
 *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
 *  Copyright 2009 by Roberto Resoli resoli@osor.eu
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

package it.plio.ext.oxsit.pcsc;

import it.plio.ext.oxsit.logging.DynamicLazyLogger;
import it.plio.ext.oxsit.logging.DynamicLogger;
import it.plio.ext.oxsit.logging.DynamicLoggerDialog;
import it.plio.ext.oxsit.logging.IDynamicLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.ibm.opencard.terminal.pcsc10.OCFPCSC1;
import com.ibm.opencard.terminal.pcsc10.Pcsc10Constants;
import com.ibm.opencard.terminal.pcsc10.PcscException;
import com.ibm.opencard.terminal.pcsc10.PcscReaderState;

/**
 * A java class for detecting SmartCard tokens and readers via PCSC.
 *
 * @author Roberto Resoli
 */
public class PCSCHelper {
    private Hashtable<String, CardInfo> m_CardInfos = new Hashtable<String, CardInfo>();
    
    private IDynamicLogger m_aLogger;

    /** The reference to the PCSC ResourceManager for this card terminal. */
    private OCFPCSC1 pcsc;
    
    private String[] readers = null;

	private String type = null;

    /** The context to the PCSC ResourceManager */
    private int context = 0;

    /** The state of this card terminal. */
    private boolean closed;

    /* states returned by SCardGetStatusChange */
    private static final int SCARD_STATE_MUTE = 0x200;

    private static final int SCARD_STATE_PRESENT = 0x020;

    /** The <tt>ATR</tt> of the presently inserted card. */
    private byte[] cachedATR;
    
    public PCSCHelper(boolean loadLib, String _PcscWrapperLib, IDynamicLogger aLogger) {
    	if(aLogger == null)
    		m_aLogger = new DynamicLazyLogger();
    	else {
    		if(aLogger instanceof DynamicLogger)
    			m_aLogger = (DynamicLogger)aLogger;
    		else if(aLogger instanceof DynamicLoggerDialog)
        			m_aLogger = (DynamicLoggerDialog)aLogger;
    	}

    	m_aLogger.enableLogging();

        try {
            m_aLogger.ctor("connect to PCSC 1.0 resource manager");

            // load native library
            if (loadLib) {
	            try {
	                OCFPCSC1.loadLib(_PcscWrapperLib);
	            } catch (NoSuchMethodError e) {
	            // this can happen if the library is not the one we think
	            	m_aLogger.info("Not found class OCFPCSC1 locally in jar file, trying the installed one from j4sign...");
	                OCFPCSC1.loadLib();
	            }
            }
            pcsc = new OCFPCSC1();

            readers = pcsc.SCardListReaders(null);

            this.type = "PCSC10";

            /* connect to the PCSC resource manager */
            context = pcsc.SCardEstablishContext(Pcsc10Constants.SCARD_SCOPE_USER);

            m_aLogger.info("Driver initialized");

            loadProperties();

        } catch (UnsatisfiedLinkError e) {
	        m_aLogger.severe("","Missing a library ? ",e);
        } catch (PcscException e) {
	        m_aLogger.severe(e);
        } catch (NoSuchMethodError e) {
	        m_aLogger.severe(e);
        } catch (NullPointerException e) {
	        m_aLogger.severe(e);
	    } catch (Throwable e) {
	        m_aLogger.severe(e);
	    }

        /* add one slot */
        //this.addSlots(1);
    }
    
    private void loadProperties() {

        m_aLogger.info("Loading properties...");

        Properties prop = new Properties();

        InputStream propertyStream=null;
        String scPropertiesFile = null;
        
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("win") > -1) {
            scPropertiesFile = "scWin.properties";
        }
        if (osName.toLowerCase().indexOf("linux") > -1) {
            scPropertiesFile = "scLinux.properties";
        }
        if (osName.toLowerCase().indexOf("mac") > -1) {
            scPropertiesFile = "scMac.properties";
        }
        if (scPropertiesFile!=null) {
            propertyStream = this.getClass().getResourceAsStream(scPropertiesFile);
        }

        if (propertyStream != null) {
            try {
                prop.load(propertyStream);

            } catch (IOException e2) {
            	m_aLogger.severe(e2);
            }
            //prop.list(System.out);
        }

        Iterator<Object> i = prop.keySet().iterator();

        String currKey = null;

        int index = 0;
        int pos = -1;
        String attribute = null;
        String value = null;

        //loading properties in a vector of CardInfo
        Vector<Object> v = new Vector<Object>();
        CardInfo ci = null;
        while (i.hasNext()) {
            currKey = (String) i.next();
            pos = currKey.indexOf(".");
            index = Integer.parseInt(currKey.substring(0, pos));
            attribute = currKey.substring(pos + 1);
            value = (String) prop.get(currKey);
            value = "atr".equals(attribute) ? value.toUpperCase() : value;

            while (index > v.size()) {
                ci = new CardInfo();
                v.addElement(ci);
            }
            ci = (CardInfo) v.get(index - 1);
            ci.addProperty(attribute, value);
        }

        //coverting vector to Hashtable (keyed by ATR)
        i = v.iterator();
        while (i.hasNext()) {
            ci = (CardInfo) i.next();
            this.m_CardInfos.put(ci.getProperty("atr"), ci);
            //cosa mette nella Hash Table?
            //System.out.println("ATR inserita nella Hash Table: "+ ci.getProperty("atr"));
        }

    }

    public List<CardInReaderInfo> findCardsAndReaders() {

        ArrayList<CardInReaderInfo> cardsAndReaders = new ArrayList<CardInReaderInfo>();

        try {
//            int numReaders = getReaders().length;

            //System.out.println("Found " + numReaders + " readers.");

            String currReader = null;
            CardInReaderInfo cIr = null;
            int indexToken = 0;
            for (int i = 0; i < getReaders().length; i++) {

                currReader = getReaders()[i];
                // System.out.println("\nChecking card in reader '"
                //                   + currReader + "'.");
                if (isCardPresent(currReader)) {
                    // System.out.println("Card is present in reader '"
                    //                    + currReader + "' , ATR String follows:");
                    // System.out.println("ATR: " + formatATR(cachedATR, " "));
                    CardInfo ci = new CardInfo();
                    // trova per ATR
                    try {
                    	ci = (CardInfo) getCardInfos().get(
                            formatATR(cachedATR, ""));
                    	if(ci == null) {
                    		String term =System.getProperty("line.separator"); 
                    		throw (new NullPointerException(term+term+
                    				"Card with ATR: "+formatATR(cachedATR, "")+" not found on internal properties"+term));
                    	}
	                    cIr = new CardInReaderInfo(currReader, ci);
	                    cIr.setIndexToken(indexToken);
	                    cIr.setSlotId(indexToken);
	                    cIr.setLib(ci.getProperty("lib"));
	                    indexToken++;                    	
                    } catch (NullPointerException e) {
                    	m_aLogger.severe(e);
	                    cIr = new CardInReaderInfo(currReader, null);
	                    cIr.setLib(null);
                    }
                } else {
                    //  System.out.println("No card in reader '" + currReader
                    //                     + "'!");
                    cIr = new CardInReaderInfo(currReader, null);
                    cIr.setLib(null);
                }
                cardsAndReaders.add(cIr);
            }
        } catch (Exception e) {
            m_aLogger.severe(e);
        }
        return cardsAndReaders;
    }

    public String formatATR(byte[] atr, String byteSeparator) {
        int n, x;
        String w = new String();
        String s = new String();

        for (n = 0; n < atr.length; n++) {
            x = (int) (0x000000FF & atr[n]);
            w = Integer.toHexString(x).toUpperCase();
            if (w.length() == 1) {
                w = "0" + w;
            }
            s = s + w + ((n + 1 == atr.length) ? "" : byteSeparator);
        } // for
        return s;
    }

   /**
     * Check whether there is a smart card present.
     *
     * @param name
     *            Name of the reader to check.
     * @return True if there is a smart card inserted in the card terminals
     *         slot.
     */
    public synchronized boolean isCardPresent(String name) {

        // check if terminal is already closed...
        if (!closed) {

            /* fill in the data structure for the state request */
            PcscReaderState[] rState = new PcscReaderState[1];
            rState[0] = new PcscReaderState();
            rState[0].CurrentState = Pcsc10Constants.SCARD_STATE_UNAWARE;
            rState[0].Reader = name;

            try {
                /* set the timeout to 1 second */
                pcsc.SCardGetStatusChange(context, 1, rState);

                // PTR 0219: check if a card is present but unresponsive
                if (((rState[0].EventState & SCARD_STATE_MUTE) != 0)
                    && ((rState[0].EventState & SCARD_STATE_PRESENT) != 0)) {

                	m_aLogger.info("Card present but unresponsive in reader "
                                     + name);
                }

            } catch (PcscException e) {
            	m_aLogger.severe("","Reader " + name + " is not responsive!",e);
            }

            cachedATR = rState[0].ATR;

            // check the length of the returned ATR. if ATR is empty / null, no
            // card is inserted
            if (cachedATR != null) {
                if (cachedATR.length > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        } else {
            return false;
        }
        // return "no card inserted", because terminal is already closed
    }


    /**
     * @return Returns the m_CardInfos.
     */
    public Hashtable<String, CardInfo> getCardInfos() {
        return m_CardInfos;
    }
    
    /**
     * @return Returns the readers.
     */
    public String[] getReaders() {
        return readers;
    }    
}
