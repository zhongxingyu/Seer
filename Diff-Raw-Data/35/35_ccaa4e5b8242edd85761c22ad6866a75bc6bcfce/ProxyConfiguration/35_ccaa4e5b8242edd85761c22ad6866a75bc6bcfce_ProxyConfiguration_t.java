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
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashSet;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhudson
  * Date: 10/15/12
  * Time: 7:36 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ProxyConfiguration {
     /**
      * @param cardBorder          int
      * @param cardHeight          int
      * @param cardWidth           int
      * @param cardListWidthParam  int
      * @param outputTemplateParam String
      * @param basicLandNamesParam Strings
      * @param flagParserParam     Flag Parser
      */
     public ProxyConfiguration(final int cardBorder,
                               final int cardHeight,
                               final int cardWidth,
                               final int cardListWidthParam,
                               final String outputTemplateParam,
                               final HashSet<String> basicLandNamesParam,
                               final FlagParser flagParserParam
     ) {
         this.cardBorder = cardBorder;
         this.cardHeight = cardHeight;
         this.cardWidth = cardWidth;
         this.outputTemplate = outputTemplateParam;
         this.cardListWidth = cardListWidthParam;
         this.basicLandNames = basicLandNamesParam;
         this.flagParser = flagParserParam;
     }
 
     /**
      *
      */
    private final Logger logger = LoggerFactory
                 .getLogger(ProxyConfiguration.class);
 
     /**
      *
      */
     private int cardHeight;
 
     /**
      *
      */
     private int cardWidth;
 
     /**
      *
      */
     private int cardListWidth;
 
     /**
      *
      */
     private int cardBorder;
 
     /**
      *
      */
     private HashSet<String> basicLandNames = new HashSet<String>();
 
     /**
      *
      */
     private String outputTemplate = "";
 
     /**
      * @return FlagParser
      */
     public final FlagParser getFlagParser() {
         return this.flagParser;
     }
 
     /**
      * @param flagParser flag parser
      */
     public final void setFlagParser(final FlagParser flagParser) {
         this.flagParser = flagParser;
     }
 
     /**
      * @param skipBasicLands Skip basic lands
      */
     public final void setSkipBasicLands(final boolean skipBasicLands) {
         this.flagParser.setSkipBasicLands(skipBasicLands);
     }
 
     /**
      *
      */
     private FlagParser flagParser;
 
     /**
      * @return int card height
      */
     public final int getCardHeight() {
         return cardHeight;
     }
 
     /**
      * @param cardHeight card height
      */
     public final void setCardHeight(final int cardHeight) {
         this.cardHeight = cardHeight;
     }
 
     /**
      * @return int card width
      */
     public final int getCardWidth() {
         return cardWidth;
     }
 
     /**
      * @param cardWidth width of card
      */
     public final void setCardWidth(final int cardWidth) {
         this.cardWidth = cardWidth;
     }
 
     /**
      * @return int card list width
      */
     public final int getCardListWidth() {
         return cardListWidth;
     }
 
     /**
      * @param cardListWidth card list width
      */
     public final void setCardListWidth(final int cardListWidth) {
         this.cardListWidth = cardListWidth;
     }
 
     /**
      * @return int card border
      */
     public final int getCardBorder() {
         return cardBorder;
     }
 
     /**
      * @param cardBorderParam  card border size
      */
     public final void setCardBorder(final int cardBorderParam) {
         this.cardBorder = cardBorderParam;
     }
 
     /**
      * @return HashSet<String> Basic land names
      */
     public final HashSet<String> getBasicLandNames() {
         return basicLandNames;
     }
 
     /**
      * @param basicLandNames basic land names
      */
     public final void setBasicLandNames(final HashSet<String> basicLandNames) {
         this.basicLandNames = basicLandNames;
     }
 
     /**
      * @return String output template
      */
     public final String getOutputTemplate() {
         return outputTemplate;
     }
 
     /**
      * @param outputTemplate output template
      */
     public final void setOutputTemplate(final String outputTemplate) {
         this.outputTemplate = outputTemplate;
     }
 }
