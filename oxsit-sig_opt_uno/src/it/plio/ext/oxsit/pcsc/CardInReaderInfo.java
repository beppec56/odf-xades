/*************************************************************************
 * 
 *  This code comes from
 *  it.trento.comune.j4sign.pcsc.CardInReadedInfo class in j4sign
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

/**
 *	j4sign - an open, multi-platform digital signature solution
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
/*
 * $Header: /cvsroot/j4sign/j4sign/src/java/core/it/trento/comune/j4sign/pcsc/CardInReaderInfo.java,v 1.1 2005/12/28 15:38:51 resoli Exp $
 * $Revision: 1.1 $
 * $Date: 2005/12/28 15:38:51 $
 */
package it.plio.ext.oxsit.pcsc;


/**
 * Stores informations about a card and its reader.
 *
 * @author Francesco Cendron
 *
 */

public class CardInReaderInfo {
    private String reader;
    private long slotID=0;
    private int indexToken = 0;
    private CardInfo card;
    private String lib;

    public CardInReaderInfo() {

    }

    public CardInReaderInfo(String attribute1, CardInfo attribute2) {
        reader = attribute1;
        card = attribute2;
    }

    public void setReader(String attribute) {
        reader = attribute;
    }

    public void setCard(CardInfo attribute) {
        card = attribute;
    }

    public void setIndexToken(int attribute) {
        indexToken = attribute;
    }

    public int getIndexToken() {
        return indexToken;
    }
    public void setSlotId(int attribute) {
        slotID = attribute;
    }

    public long getSlotId() {
        return slotID;
    }

    public void setLib(String attribute) {
       lib = attribute;
   }

   public String getLib() {
       return lib;
   }



    public String getReader() {
        return reader;
    }

    public CardInfo getCard() {
        return card;
    }

    public String toString() {
        return reader;
    }


}
