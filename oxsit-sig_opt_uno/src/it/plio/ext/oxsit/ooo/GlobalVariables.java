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

package it.plio.ext.oxsit.ooo;

import it.plio.ext.oxsit.logging.DynamicLogger;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XChangesListener;

/**
 * this is a Java class singleton, see
 * http://www.javacoffeebreak.com/articles/designpatterns/index.html
 * http://www.javaworld.com/javaworld/jw-04-2003/jw-0425-designpatterns.html
 * 
 * It holds the global variable, e.g. the variables common
 * in an OOo session.
 * 
 * Only one of this exists in a JVM
 * 
 * Luckily OOo instantiates only one JVM.
 * 
 * @author beppe
 *
 */
public class GlobalVariables {

	private static GlobalVariables INSTANCE = null;

	private class DocumentDescriptor {
		public String	documentURLHash; // the hash computed from the URL name class
		public String	aTheURL;   //the corresponding URL
		public int		signatureXAdESState; // state of the signature(s) in this frame
		public HashMap<XChangesListener,XChangesListener> listeners;
	};

	private HashMap<String, DocumentDescriptor>	theDocumentList = new HashMap<String, DocumentDescriptor>(10);

	private GlobalVariables() {
	}

	 public synchronized static GlobalVariables getInstance()
	  {
		 if(INSTANCE == null) {
			 synchronized (GlobalVariables.class) {
				 INSTANCE = new GlobalVariables();					
			}
		 }
		 return INSTANCE;
	  }

	 @Override
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
	}

	public void logSomething(String message) {
//		m_logger.info(message);
	}
	
	public synchronized boolean addDocumentURL(String aURL) {
		// this assumes that to a unique URL corresponds a unique frame
//		m_logger.log("addDocumentURL");
		String aURLHash = getDocumentURLHash(aURL);
		if(!theDocumentList.containsKey(aURLHash)) {
			DocumentDescriptor docuDescrip = new DocumentDescriptor();
			docuDescrip.aTheURL = aURL;
			docuDescrip.documentURLHash = aURLHash;
			docuDescrip.signatureXAdESState = GlobConstant.m_nSIGNATURESTATE_UNKNOWN;
			docuDescrip.listeners = new HashMap<XChangesListener, XChangesListener>(10);
			theDocumentList.put(docuDescrip.documentURLHash, docuDescrip);
		}
		return true;
	}

	public synchronized void changeDocumentURL(String oldURL, String newURL) {
//		m_logger.log("changeDocumentURL");
		String newURLHash = getDocumentURLHash(newURL);
		String oldURLHash = getDocumentURLHash(oldURL);

//fetch the old URL
		if(theDocumentList.containsKey(oldURLHash)) {
			//remove from HashMap
			DocumentDescriptor docuDescrip = theDocumentList.remove(oldURLHash);
			//changes URL, the rest remains as is
			docuDescrip.aTheURL = newURL; //update
			docuDescrip.documentURLHash = newURLHash; //update
			//put it back in Hash, with new key
			theDocumentList.put(docuDescrip.documentURLHash, docuDescrip);
		}
	}
	
	public synchronized boolean existDocumentURL(String aURL) {
//		m_logger.log("existDocumentURL");
		String aURLHash = getDocumentURLHash(aURL);
		return theDocumentList.containsKey(aURLHash);
	}

	public synchronized void addListenerToADocument(String aURL, XChangesListener aChangeListener) {
		//find the document,
//		m_logger.log("addListenerToADocument");
		String aURLHash = getDocumentURLHash(aURL);

//if there is no document record, then add it		
		if(!theDocumentList.containsKey(aURLHash))
			addDocumentURL(aURL);

		if(theDocumentList.containsKey(aURLHash)) {
			DocumentDescriptor docuDescrip = theDocumentList.remove(aURLHash);
			docuDescrip.aTheURL = aURL;
			//add the listener to the HashMap		
			docuDescrip.documentURLHash = aURLHash;
			docuDescrip.signatureXAdESState = GlobConstant.m_nSIGNATURESTATE_UNKNOWN;
			docuDescrip.listeners.put(aChangeListener, aChangeListener);
			theDocumentList.put(docuDescrip.documentURLHash, docuDescrip);
		}
	}

	public synchronized void removeListenerFromADocument(String aURL, XChangesListener aChangeListener) {
		//find the document,
		
		//remove the listener from the HashMap		

	}

	public synchronized void removeDocumentURL(String aURL) {
//		m_logger.log("removeDocumentURL");
		String aURLHash = getDocumentURLHash(aURL);
		if(theDocumentList.containsKey(aURLHash)) {
			DocumentDescriptor docuDescrip = theDocumentList.remove(aURLHash);
			docuDescrip.listeners.clear();
		}
	}

	public synchronized void removeAllDocumentURL() {
//convert the HasMap to a collection, then remove each item
//		m_logger.log("removeAllDocumentURL");
		Collection<DocumentDescriptor> cDocuDescriptors = theDocumentList.values();
		
		if(!cDocuDescriptors.isEmpty()) {
			Iterator<DocumentDescriptor> aIter = cDocuDescriptors.iterator();
			while (aIter.hasNext()) {
				DocumentDescriptor aDocDesc = aIter.next();
				aDocDesc.listeners.clear();
			}			
		}
		theDocumentList.clear();
	}

	public synchronized void setDocumentData(String aURL, int signatureState) {
//		m_logger.log("setDocumentData");
		String aURLHash = getDocumentURLHash(aURL);		
		if(theDocumentList.containsKey(aURLHash)) {
			DocumentDescriptor theDocu = theDocumentList.remove(aURLHash);			
			theDocu.aTheURL = aURL;
			theDocu.signatureXAdESState = signatureState;
			theDocumentList.put(theDocu.documentURLHash, theDocu);
//notify all the changes m_aListeners in the list
			notifyListeners(theDocu);
		}		
	}

	public synchronized boolean getDocumentData(String aURL) {
//		m_logger.log("getDocumentData");
		return theDocumentList.containsKey(aURL);
	}

	public synchronized boolean setDocumentSignatureState(String aURL, int signatureState) {
//		m_logger.log("setDocumentSignatureState");
		String aURLHash = getDocumentURLHash(aURL);		
		if(theDocumentList.containsKey(aURLHash)) {
			DocumentDescriptor theDocu = theDocumentList.remove(aURLHash);			
			theDocu.aTheURL = aURL;
			theDocu.signatureXAdESState = signatureState;
			theDocumentList.put(theDocu.documentURLHash, theDocu);
//notify all the change m_aListeners in the list
			notifyListeners(theDocu);
		}		
		return true;
	}

	private void notifyListeners(DocumentDescriptor theDocu) {
		Collection<XChangesListener> cChangesListener = theDocu.listeners.values();			
		if(!cChangesListener.isEmpty()) {
			Iterator<XChangesListener> aIter = cChangesListener.iterator();
			while (aIter.hasNext()) {
				XChangesListener aChangesListener = aIter.next();
				aChangesListener.changesOccurred(null);
			}			
		}		
	}
	
	public synchronized int getSignatureState(String aURL) {
//		m_logger.log("getSignatureState");
		String aURLHash = getDocumentURLHash(aURL);		
		if(theDocumentList.containsKey(aURLHash)) {
			DocumentDescriptor theDocu = theDocumentList.get(aURLHash);			
			return theDocu.signatureXAdESState;
		}
		return GlobConstant.m_nSIGNATURESTATE_UNKNOWN;
	}

	private synchronized String getDocumentURLHash(String aURL) {
		return GlobConstant.m_sEXTENSION_CONF_FRAME_ID + Integer.toHexString( aURL.hashCode() );
	}
}
