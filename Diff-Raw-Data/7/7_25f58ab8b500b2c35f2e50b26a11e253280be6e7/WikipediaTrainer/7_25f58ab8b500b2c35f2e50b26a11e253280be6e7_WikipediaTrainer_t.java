 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.aperigeek.jlad.trainer.wikipedia;
 
 import com.aperigeek.jlad.core.ngram.NGramsCollector;
 import com.aperigeek.jlad.core.token.WordTokenizer;
 import com.aperigeek.jlad.trainer.TrainerException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.util.Map;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.ext.DefaultHandler2;
 
 /**
  * Extracts the n-grams from a Wikipedia database dump and output the results
  * on the given stream.
  * 
  * This class is to be used during the training phase of the application.
  * 
  * @author Vivien Barousse
  */
 public class WikipediaTrainer {
 
     private NGramsCollector collector;
     private InputStream in;
 
     public WikipediaTrainer(InputStream in) {
         this.in = in;
         collector = new NGramsCollector();
     }
 
     public void train() throws TrainerException {
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser parser = factory.newSAXParser();
             parser.parse(in, new WikipediaTextHandler(), null);
         } catch (IOException ex) {
             throw new TrainerException("Error reading database file", ex);
         } catch (ParserConfigurationException ex) {
             throw new TrainerException("Invalid trainer configuration", ex);
         } catch (SAXException ex) {
             throw new TrainerException("Invalid Wikipedia database file", ex);
         }
     }
     
     public void dump(OutputStream out) {
         dump(new OutputStreamWriter(out));
     }
     
     public void dump(Writer out) {
         dump(new PrintWriter(out));
     }
     
     public void dump(PrintWriter out) {
         Map<String, Integer> ngrams = collector.getNgramsCount();
         for (Map.Entry<String, Integer> ngram : ngrams.entrySet()) {
             out.print(ngram.getKey());
             out.print(",");
             out.println(ngram.getValue());
         }
        out.flush();
     }
     
     private void collectText(String text) throws IOException {
         StringReader reader = new StringReader(text);
         WordTokenizer token = new WordTokenizer(reader);
 
         String word;
         while ((word = token.nextWord()) != null) {
             collector.collect(word.toLowerCase());
         }
     }
     
     private class WikipediaTextHandler extends DefaultHandler2 {
 
         private boolean collect;
 
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
             if (qName.equals("text")) {
                 collect = true;
             }
         }
 
         @Override
         public void characters(char[] ch, int start, int length) throws SAXException {
             try {
                 if (collect) {
                     collectText(new String(ch, start, length));
                 }
             } catch (IOException ex) {
                 throw new SAXException(ex);
             }
         }
 
         @Override
         public void endElement(String uri, String localName, String qName) throws SAXException {
             collect = false;
         }
         
     }
 }
