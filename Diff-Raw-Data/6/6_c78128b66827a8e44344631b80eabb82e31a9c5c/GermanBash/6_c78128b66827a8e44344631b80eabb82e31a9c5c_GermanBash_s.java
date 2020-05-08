 /*
  * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
  * 
  * This file is part of the chatbot xpeter.
  * 
  * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter.parsers;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.Message;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.util.BotApi;
 
 public class GermanBash implements Parser, Observer<TextMessage> {
     
     @Override
     public void init(Bot bot) {
         bot.register(TextMessage.class, this);
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
     
     @Override
     public void inform(TextMessage msg) {
         String nick = msg.getBotNick();
         String text = msg.getText();
         
         if (!BotApi.addresses(text, nick)) {
             return;
         }
         
         text = BotApi.trimNick(text, nick).trim().toLowerCase();
         String query = null;
         
         if (text.matches("gb") || text.matches("gbo") || text.matches("german-bash.org")
                 || text.matches("german-bash") || text.matches("bash")) {
             query = "action/random";
         }
         
         if (text.startsWith("gb ")) {
             
             try {
                 query = "" + Integer.parseInt(text.substring("gb ".length()).trim());
             } catch (NumberFormatException e) {
                 query = null;
             }
         }
         
         if (text.startsWith("gbo ")) {
             
             try {
                 query = "" + Integer.parseInt(text.substring("gbo ".length()).trim());
             } catch (NumberFormatException e) {
                 query = null;
             }
         }
         
         if (text.startsWith("bash ")) {
             
             try {
                 query = "" + Integer.parseInt(text.substring("bash ".length()).trim());
             } catch (NumberFormatException e) {
                 query = null;
             }
         }
         
         if (text.startsWith("german-bash.org ")) {
             
             try {
                 query = "" + Integer.parseInt(text.substring("german-bash.org ".length()).trim());
             } catch (NumberFormatException e) {
                 query = null;
             }
         }
         
         if (text.startsWith("german-bash ")) {
             
             try {
                 query = "" + Integer.parseInt(text.substring("german-bash ".length()).trim());
             } catch (NumberFormatException e) {
                 query = null;
             }
         }
         
         Log.debug("Query is: " + query);
         
         if (query != null) {
             
             try {
                 String website = BotApi.getWebsite("german-bash.org", query);
                 
                 if (website.contains("Ein Zitat mit dieser id existiert leider nicht.")) {
                     msg.respond(new Message("Ein Zitat mit dieser Nummer gibt es bei "
                             + "german-bash.org nicht."));
                     return;
                 }
                 
                if (website.contains("Ein Zitat mit dieser id existiert leider nicht.")) {
                    msg.respond(new Message("Ein Zitat mit dieser Nummer gibt es bei "
                            + "german-bash.org nicht."));
                    return;
                }
                
                 String match = "[\\w\\W]*?<div class=\"zitat\">([\\w\\W]*?)</div>[\\w\\W]*";
                 String header = "[\\w\\W]*?<div class=\"quote_header\">([\\w\\W]*?)</div>[\\w\\W]*";
                 
                 if (website.matches(match) && website.matches(header)) {
                     String quote = website.replaceAll(match, "$1");
                     String number = website.replaceAll(header, "$1");
                     
                     number = number
                             .replaceAll("[\\w\\W]*?<span id=\"quote(\\d*)\">[\\w\\W]*", "$1");
                     
                     quote = quote.replaceAll(
                             "\\s*<span class=\"quote_zeile\">\\s*(.*?)</span>\\s*", "$1\n");
                     quote = quote.substring(0, quote.length() - 1);
                     quote = quote.replaceAll("&gt;", ">");
                     quote = quote.replaceAll("&lt;", "<");
                     String orig = quote;
                     
                     while (!(quote = quote.replaceAll(
                             "([\\w\\W]*?)&quot;([\\w\\W]*?)&quot;([\\w\\W]*)", "$1„$2“$3"))
                             .equals(orig)) {
                         orig = quote;
                     }
                     
                     while (quote.endsWith("\n")) {
                         quote = quote.substring(0, quote.length() - 1);
                     }
                     
                     msg
                             .respond(new Message("german-bash.org – Zitat Nr. " + number + ":\n"
                                     + quote));
                 } else {
                     msg.respond(new Message("Ich kann leider nicht verstehen, was german-bash.org "
                             + "zu mir sagt."));
                 }
                 
             } catch (UnknownHostException e) {
                 msg.respond(new DelayedMessage(
                         "Tut mir Leid, aber die IP-Adresse zu german-bash.org "
                                 + "konnte nicht gefunden werden (" + e.getMessage() + ").", 3000));
             } catch (IOException e) {
                 msg.respond(new DelayedMessage("Tut mir Leid, aber beim Datenaustausch mit "
                         + "german-bash.org ist ein Fehler aufgetreten (" + e.getMessage() + ").",
                         3000));
             }
         }
     }
 }
