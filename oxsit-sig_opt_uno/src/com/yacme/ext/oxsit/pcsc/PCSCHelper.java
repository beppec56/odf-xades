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

package com.yacme.ext.oxsit.pcsc;

import iaik.pkcs.pkcs11.wrapper.PKCS11Implementation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ibm.opencard.terminal.pcsc10.OCFPCSC1;
import com.ibm.opencard.terminal.pcsc10.Pcsc10Constants;
import com.ibm.opencard.terminal.pcsc10.PcscException;
import com.ibm.opencard.terminal.pcsc10.PcscReaderState;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.yacme.ext.oxsit.Helpers;
import com.yacme.ext.oxsit.logging.DynamicLazyLogger;
import com.yacme.ext.oxsit.logging.DynamicLogger;
import com.yacme.ext.oxsit.logging.DynamicLoggerDialog;
import com.yacme.ext.oxsit.logging.IDynamicLogger;
import com.yacme.ext.oxsit.ooo.registry.SSCDsConfigurationAccess;
import com.yacme.ext.oxsit.ooo.ui.MessageNoSSCD;
import com.yacme.ext.oxsit.ooo.ui.MessageNoSSCDReaders;
import com.yacme.ext.oxsit.options.OptionsParametersAccess;

/**
 * A java class for detecting SmartCard m_nTokens and readers via PCSC.
 *
 * @author Roberto Resoli
 */
public class PCSCHelper {

	private Hashtable<String, CardInfoOOo> m_CardInfosOOo = new Hashtable<String, CardInfoOOo>();
	
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

    
    private 	XComponentContext m_xCC;
    private		XMultiComponentFactory m_xMCF;

	private XFrame m_xFrame;

	private boolean m_bDoFeedBack;
    
    public PCSCHelper(XFrame _xFrame, XComponentContext _xContext, boolean loadLib, String _PcscWrapperLib, IDynamicLogger aLogger) {
    	m_xCC = _xContext;
    	m_xMCF = m_xCC.getServiceManager();
    	m_xFrame = _xFrame;
    	if(aLogger == null)
    		m_aLogger = new DynamicLazyLogger();
    	else {
    		if(aLogger instanceof DynamicLogger)
    			m_aLogger = (DynamicLogger)aLogger;
    		else if(aLogger instanceof DynamicLoggerDialog)
        			m_aLogger = (DynamicLoggerDialog)aLogger;
    	}

    	m_bDoFeedBack = false;

    	// grab the configuration information
		OptionsParametersAccess xOptionsConfigAccess = new OptionsParametersAccess(m_xCC);
		m_bDoFeedBack = xOptionsConfigAccess.getBoolean("EnableDebugLogging");
		xOptionsConfigAccess.dispose();
    	
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
	            catch (Throwable e) {
	            	m_aLogger.severe("PCSCHelper <ctor>","Error during init" , e);
	            }
            }
            pcsc = new OCFPCSC1();

            //FIXME it seems that sometimes it's returned an empty string
            //when there is no reader, happens in Ubuntu 8.04
            //may be the library needs checking?
            readers = pcsc.SCardListReaders(null);

            this.type = "PCSC10";

            /* connect to the PCSC resource manager */
            context = pcsc.SCardEstablishContext(Pcsc10Constants.SCARD_SCOPE_USER);

            m_aLogger.info("Driver initialized");

            loadOOoSSCDConfigurationData();

        } catch (UnsatisfiedLinkError e) {
	        m_aLogger.severe("","Missing a library ? ",e);
        } catch (PcscException e) {
        	//Give the user some feedback
        	MessageNoSSCDReaders aMex = new MessageNoSSCDReaders(m_xFrame,m_xMCF,m_xCC);      	
        	aMex.executeDialogLocal();
	        m_aLogger.log(e, false);
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
    
    private void loadOOoSSCDConfigurationData() {
    	//open the root config for SSCDs data
    	//see the file AddonConfiguration.xcu.xml on prj oxsit-ext_conf for details
    	//on how the configuration is written
    	SSCDsConfigurationAccess aConf = new SSCDsConfigurationAccess(m_xCC,m_xMCF);
        // create the root element to look for the SSCD configuration
    	CardInfoOOo[] aCardList = aConf.readSSCDConfiguration();
    	if(aCardList != null) {            
            for(int i =0; i< aCardList.length;i++)
            	m_CardInfosOOo.put(aCardList[i].getATRCode(), aCardList[i]);
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
                    CardInfoOOo ci = new CardInfoOOo();
                    // trova per ATR
                    try {
                    	ci = (CardInfoOOo) getCardInfosOOo().get(
                            formatATR(cachedATR, ""));
                    	if(ci == null) {
                    		String term =System.getProperty("line.separator"); 
                    		throw (new NullPointerException(term+term+
                    				"Card with ATR: "+formatATR(cachedATR, "")+" not found on internal properties"+term));
                    	}
	                    cIr = new CardInReaderInfo(currReader, ci);
	                    cIr.setIndexToken(indexToken);
	                    cIr.setSlotId(indexToken);

	                    cIr.setLib(ci.getDefaultLib());
	                    m_aLogger.log("For: "+cIr.getCard().getATRCode()+" library: "+cIr.getLib());
	                    indexToken++;
                    } catch (NullPointerException e) {
                    	m_aLogger.severe(e);
	                    cIr = new CardInReaderInfo(currReader, null);
	                    cIr.setLib(null);
                    }
                } else {
                    cIr = new CardInReaderInfo(currReader, null);
                    cIr.setLib(null);
                    //give the user some feedback
                    if(m_bDoFeedBack) {
                        MessageNoSSCD	aMex = new MessageNoSSCD(m_xFrame,m_xMCF,m_xCC);
                        aMex.executeDialogLocal(currReader);
                    }
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
            	if(m_bDoFeedBack)
            		m_aLogger.severe("","Reader " + name + " is not responsive!",e);
            	else
            		m_aLogger.warning("","Reader " + name + " is not responsive!",e);            		
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

    //function to substitute getCardInfos
    public Hashtable<String, CardInfoOOo> getCardInfosOOo() {
    	return m_CardInfosOOo;
    }

    /**
     * @return Returns the readers.
     */
    public String[] getReaders() {
        return readers;
    }    

 ////////////////////////// code used to test the XML conversion
    //ctor used to convert SSCD properties to XML structure
    public PCSCHelper() {

    }
}
