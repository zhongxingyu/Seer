 package org.eclipse.virgo.build.p2tools.instructions;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * 
  * Parses a feature in search for the specific 'autostart' attribute of the element plugin. The attribute is case sensitive so if's written incorrectly it'll be ignored.
  * <p />
  *
  * <strong>Concurrent Semantics</strong><br />
  * Not thread-safe.
  */
 public class FeatureParser extends DefaultHandler {
 
     private ArrayList<String> result;
 
     private final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 
     private SAXParser parser;
 
     public FeatureParser() {
         try {
             this.parser = this.parserFactory.newSAXParser();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         if ("plugin".equals(qName)) {
             processAutostart(attributes);
         }
     }
 
     public ArrayList<String> parse(File featureXml) throws IOException {
         this.result = new ArrayList<String>();
         if (featureXml.exists()) {
             InputStream input = null;
             try {
                 input = new BufferedInputStream(new FileInputStream(featureXml));
                 this.parser.parse(input, this);
             } catch (Exception e) {
             } finally {
                 input.close();
             }
             return this.result;
         }
         return null;
     }
 
     private void processAutostart(Attributes attributes) {
        if (attributes.getValue("autostart").equals("true")) {
             this.result.add(attributes.getValue("id"));
         }
     }
 
 }
