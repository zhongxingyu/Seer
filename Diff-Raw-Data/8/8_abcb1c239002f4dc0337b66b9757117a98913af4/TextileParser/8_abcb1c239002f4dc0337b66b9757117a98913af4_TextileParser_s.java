 /*
  * Project: doxia-module-textile
  * Package: org.apache.maven.doxia.module.textile
  * File   : TextileParser.java
  * Created: Jul 3, 2011 - 6:32:27 PM
  *
  *
  * Copyright 2011 viadee IT Unternehmensberatung GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package de.viadee.maven.doxia.modules.textile;
 
 import java.io.IOException;
 import java.io.Reader;
 
 import org.apache.maven.doxia.parser.AbstractTextParser;
 import org.apache.maven.doxia.parser.ParseException;
 import org.apache.maven.doxia.sink.Sink;
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
 
 import com.google.common.io.CharStreams;
 
 /**
  * <p>
  * Doxia parser for Textile documents.
  * </p>
  * 
  * @author 				Sebastian Hoß (sebastian.hoss@viadee.de)
  * @since               1.0.0
  * @plexus.component 	role="org.apache.maven.doxia.parser.Parser" role-hint="textile"
  */
 public class TextileParser extends AbstractTextParser {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void parse(final Reader reader, final Sink sink) throws ParseException {
         this.getLog().info("Parsing Textile document.."); //$NON-NLS-1$
 
        // Creating parser for Textile language
        final MarkupParser markupParser = new MarkupParser();
        markupParser.setMarkupLanguage(new TextileLanguage());

         // Reading content of given markup
         String markupContent;
 
         try {
             markupContent = CharStreams.toString(reader);
             this.getLog().info("Textile content is: " + markupContent); //$NON-NLS-1$
         } catch (final IOException exception) {
             throw new ParseException("Cannot read input", exception); //$NON-NLS-1$
         }
 
         // Parse given markup to HTML
         if (markupContent != null && !markupContent.isEmpty()) {
             final String html = markupParser.parseToHtml(markupContent);
             this.getLog().info("HTML content is: " + html); //$NON-NLS-1$
 
             sink.rawText(html);
 
             sink.flush();
         }
 
         // Finally close the sink.
         sink.close();
     }
 
 }
