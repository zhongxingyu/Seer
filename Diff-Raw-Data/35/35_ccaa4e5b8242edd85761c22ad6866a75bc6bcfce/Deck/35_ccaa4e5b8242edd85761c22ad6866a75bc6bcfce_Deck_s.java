 /*
  * Copyright (C) 2012  David Hudson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.londonsburning.proxy;
 
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.TreeMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhudson
  * Date: 10/6/12
  * Time: 12:27 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Deck {
 
     /**
      *
      */
    private final static org.slf4j.Logger logger = LoggerFactory
             .getLogger(Deck.class);
 
     /**
      * @return String deckName
      */
     public final String getDeckName() {
         return deckName;
     }
 
     /**
      * @param deckNameParam DeckName
      */
     public final void setDeckName(final String deckNameParam) {
         this.deckName = deckNameParam;
     }
 
     /**
      * @return cardList
      */
     public final TreeMap<String, Integer> getCardList() {
         return cardList;
     }
 
     /**
      * @param cardListParam CardList
      */
     public final void setCardList(
             final TreeMap<String, Integer> cardListParam) {
         this.cardList = cardListParam;
     }
 
     /**
      *
      */
     private String deckName;
 
     /**
      *
      */
     private TreeMap<String, Integer> cardList = new TreeMap<String, Integer>();
 
     /**
      *
      */
     private File file;
 
     /**
      * @param fileParam File
      */
     public Deck(final File fileParam) {
         this.file = fileParam;
 
     }
 
     /**
      *
      */
     public final void parse() {
         try {
             // Open the file
             FileInputStream fstream = new FileInputStream(this.file);
 
             // Get the object of DataInputStream
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
             String strLine;
 
             while ((strLine = br.readLine()) != null) {
                 if (!strLine.startsWith("///")) {
                     if (strLine.startsWith("//")) {
                         this.deckName = strLine.substring(2).trim();
                     } else {
                         if (!strLine.trim().isEmpty()) {
                             String[] split = strLine.split(" ", 2);
                             this.cardList.put(split[1],
                                     Integer.parseInt(split[0]));
                         }
                     }
                 }
             }
             //Close the input stream
             in.close();
 
         } catch (FileNotFoundException e) {
             logger.debug("File not found");
         } catch (IOException e) {
             logger.debug("IO Error");
         }
     }
 }
