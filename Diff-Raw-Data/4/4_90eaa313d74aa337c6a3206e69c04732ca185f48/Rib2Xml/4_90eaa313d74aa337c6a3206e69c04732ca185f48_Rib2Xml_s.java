 /* 
    +----------------------------------------------------------------------+
    | RendermanXML                                                         |
    +----------------------------------------------------------------------+
    | Copyright (c) 2004 Cyberroadie                                       |
    +----------------------------------------------------------------------+
    | This source file is subject to version 2 of the GNU General Public   |
    | License, that is bundled with this package in the file LICENSE.      |
    |                                                                      |
    | This program is free software; you can redistribute it and/or modify |
    | it under the terms of the GNU General Public License as published by |
    | the Free Software Foundation; either version 2 of the License, or    |
    | (at your option) any later version.                                  |
    |                                                                      |
    | This program is distributed in the hope that it will be useful,      |
    | but WITHOUT ANY WARRANTY; without even the implied warranty of       |
    | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        |
    | GNU General Public License for more details.                         |
    |                                                                      |
    | You should have received a copy of the GNU General Public License    |
    | along with this program; if not, write to the Free Software          |
    | Foundation, Inc., 59 Temple Place, Suite 330, Boston,                |
    | MA  02111-1307  USA                                                  |
    |                                                                      |
    +----------------------------------------------------------------------+
    | Author: Olivier Van Acker <cyberroadie@yahoo.co.uk>                  |
    +----------------------------------------------------------------------+
 */
 package net.transformatorhuis.cgi.conversion;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.Level;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import java.io.FileReader;
 import java.io.StreamTokenizer;
 import java.io.IOException;
 
 import java.net.URL;
 
 import net.transformatorhuis.cgi.utils.Config;
 
 import java.util.Vector;
 
 class Rib2Xml {
 
     static Logger logger = Logger.getLogger(Rib2Xml.class);
         
     public static void main(String[] argv) {
               
         // is there anything to do?
         if (argv.length == 0) {
             printUsage();
             System.exit(1);
         }
 
         try {
             new Rib2Xml(new FileReader(argv[0]));
             logger.info("Input file: " + argv[0]);
         } catch (IOException ioe) {
             logger.info(ioe.toString());
             System.exit(1);
         }
         
     }
     
     public Rib2Xml(FileReader ribFileReader) {
         
         /* Configure log4j, read conf out of jar file */
         Class clazz = getClass();
         URL url = clazz.getResource("/conf/log4j.xml");
         DOMConfigurator.configure(url);
 
         /* Create the ribfactory which deal with all the rib elements */
         Config config = Config.instance();
         RibFactory ribFac = new RibFactory(config);
         
         Vector ribNames = config.getNames();
         
         StreamTokenizer thTokens = new StreamTokenizer(ribFileReader);
         //thTokens.resetSyntax();
         thTokens.commentChar('#');
         thTokens.eolIsSignificant(false);
         thTokens.parseNumbers();
         thTokens.ordinaryChar('[');
         thTokens.ordinaryChar(']');
         thTokens.quoteChar('"');
         int count = 0;
  
         try {
             String factoryInput = new String();
              
             while(thTokens.nextToken() != StreamTokenizer.TT_EOF) {
                 logger.debug(thTokens.lineno() + ": " + thTokens.sval + ": ttype: " + thTokens.ttype);
                 if(thTokens.ttype == StreamTokenizer.TT_NUMBER) {
                     logger.debug(thTokens.lineno() + ": " + thTokens.nval);
                     factoryInput += " " + String.valueOf(thTokens.nval);
                     count++;
                 } else if (thTokens.ttype == StreamTokenizer.TT_WORD) {
                     if(ribNames.contains(thTokens.sval)) {
                         logger.debug(factoryInput);
                         
                         // Rib Factory called to add an element to xml document
                         logger.debug("Elements: " + count + ": " + factoryInput);
                         ribFac.processRibElement(factoryInput);
   
                         factoryInput = thTokens.sval;
                     } else {
                         factoryInput += " " + thTokens.sval;
                     }
                     logger.debug(thTokens.lineno() + ": " + thTokens.sval);
                     count++;
                 } else {
                     if(thTokens.ttype != '"') {
                         logger.debug(thTokens.lineno() + ": " + (char) thTokens.ttype);
                         factoryInput += " " + (char) thTokens.ttype;
                         count++;
                     } else if(thTokens.sval != null ) {
                         logger.debug(thTokens.lineno() + ": " + (char) thTokens.ttype + thTokens.sval + (char) thTokens.ttype);
                         factoryInput += " " + (char) thTokens.ttype + thTokens.sval + (char) thTokens.ttype;
                         count++;
                     }
                 }
                 
             }
             
         } catch (Exception ex) {
             logger.error("Error: " + ex.toString());
         }
  
         logger.info("Tokens: " + count);
         
         RibDocument ribDoc = RibDocument.newInstance();
         ribDoc.toFile();
  
     }
 
     private static void printUsage() {
             System.err.println("Usage: java -jar ribxml.jar [rib file]");
     }
     
 }
