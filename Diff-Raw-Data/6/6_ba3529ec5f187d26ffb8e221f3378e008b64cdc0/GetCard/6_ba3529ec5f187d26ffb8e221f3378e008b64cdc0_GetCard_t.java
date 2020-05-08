 /*
  *  GetCard.java
  * 
  *  Created on Oct 25, 2009, 11:51:35 AM
  * 
  *  Copyright (c) 2009 Hippos Development Team. All rights reserved.
  * 
  *  This file is part of Karma.
  * 
  *  Karma is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Karma is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with Karma.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.taksmind.karma.functions;
 
 import com.taksmind.karma.Main;
 import com.taksmind.karma.tarot.Deck;
 import com.taksmind.karma.tarot.Card;
 
 /**
  *
  * @author tak <tak@taksmind.com>
  */
 public class GetCard extends Function {
 
     private String message;
     private String channel;
     private String define;
     private Deck deck = new Deck();
 
     @Override
     public void run() {
         /*if there is a message store and check it*/
         if (bot.hasMessage()) {
             message = bot.getMessage();
             channel = bot.getChannel();
         }
 
         if (message.startsWith("~card")) {
            tokenize(false, 6, message);
            define = parameters;
             Card definition = deck.getByName(define);
            
             if(definition != null) {
             	Main.bot.sendMessage(channel, definition.toString());
             }
             else {
             	Main.bot.sendMessage(channel, "Could not locate card in Major Arcana");
             }
         }
     }
 }
