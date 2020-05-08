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
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 /**
  * Generates the HTML for the proxies.
  */
 public class ProxyPrinter {
 
     /**
      *
      */
     private ProxyConfiguration proxyConfiguration;
 
     /**
      *
      */
    private final Logger logger = LoggerFactory
                 .getLogger(ProxyPrinterRunner.class);
 
     /**
      * @param proxyConfigurationParam ProxyConfiguration
      */
     public ProxyPrinter(final ProxyConfiguration proxyConfigurationParam) {
         this.proxyConfiguration = proxyConfigurationParam;
     }
 
     /**
      * <p>
      * writeDeck.
      * </p>
      *
      * @param deck    a String
      * @param file    a File
      * @param skipBasicLands Skip Basic Lands
      */
     private void generateHtml(final Deck deck, final File file,
                               final boolean skipBasicLands) {
         try {
             final BufferedWriter htmlOutput =
                     new BufferedWriter(new FileWriter(file));
 
             Template template;
 
             final Configuration cfg = new Configuration();
             cfg.setClassForTemplateLoading(deck.getClass(), "/");
             cfg.setObjectWrapper(new DefaultObjectWrapper());
             template = cfg.getTemplate(this.proxyConfiguration
                     .getOutputTemplate());
 
             final TreeMap<String, Object> root = new TreeMap<String, Object>();
             root.put("title", deck.getDeckName());
             final List<String> list = new ArrayList<String>();
             for (final String cardName : deck.getCardList().keySet()) {
                 for (int i = 0; i <= (deck.getCardList().get(cardName) - 1);
                      i++) {
                     if (!this.proxyConfiguration
                             .getBasicLandNames()
                             .contains(cardName) || !skipBasicLands) {
                         list.add(getURL(cardName));
                     }
                 }
             }
 
             final TreeMap<String, Integer> map =
                     deck.getCardList();
             root.put("urls", list);
             root.put("cardBorder", this.proxyConfiguration.getCardBorder());
             root.put("cardHeight", this.proxyConfiguration.getCardHeight());
             root.put("cardWidth", this.proxyConfiguration.getCardWidth());
             root.put("cardListWidth", this.proxyConfiguration.getCardWidth()
                     - this.proxyConfiguration.getCardListWidth());
             root.put("cardList", map);
 
             /* Merge data-model with template */
             template.process(root, htmlOutput);
             htmlOutput.flush();
             htmlOutput.close();
         } catch (final IOException e) {
             logger.debug(e.toString());
         } catch (final TemplateException e) {
             logger.debug(e.toString());
         } catch (final URISyntaxException e) {
             logger.debug(e.toString());
         } catch (final InterruptedException e) {
             logger.debug(e.toString());
         }
     }
 
     /**
      * @param cardName Name of Card
      * @return String Url
      * @throws URISyntaxException   Exception
      * @throws IOException          Exception
      * @throws InterruptedException Exception
      */
     private String getURL(final String cardName) throws
             URISyntaxException,
             IOException,
             InterruptedException {
         URI uri = new URI(
                 "http",
                 "magiccards.info",
                 "/query",
                 "q=!" + cardName,
                 null);
         String request = uri.toString();
 
         URL url = new URL(request);
         InputStream inputStream = url.openStream();
         BufferedReader bufferedReader =
                 new BufferedReader(
                         new InputStreamReader(inputStream, "UTF-8"));
         String returnString = "";
 
         while (bufferedReader.ready()) {
             String lineHtml = bufferedReader.readLine();
             Thread.sleep(1);
             if (lineHtml.contains("http://magiccards.info/scans/en")
                     && lineHtml.contains("jpg")) {
                 lineHtml = lineHtml.substring(lineHtml.indexOf("http"),
                         lineHtml.indexOf("jpg") + ".jpg".length());
                 return lineHtml;
             }
         }
         return returnString;
     }
 
     /**
      * @param flagParserParam flagParser
      */
     public final void printProxies(final FlagParser flagParserParam) {
         for (String arg : flagParserParam.getFlags()) {
             if (!arg.contentEquals(flagParserParam.getSkipBasicLandsFlag())) {
                 File file = new File(arg);
                 org.londonsburning.proxy.Deck deck =
                         new org.londonsburning.proxy.Deck(file);
                 deck.parse();
                 File htmlFile = new File(arg.replace(".dec", ".html"));
                 generateHtml(deck, htmlFile, flagParserParam
                         .isSkipBasicLands());
                 logger.info("Output file: " + htmlFile.getName());
             }
         }
     }
 }
