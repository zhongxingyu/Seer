 /*
  * Copyright 1999-2004 The Apache Software Foundation.
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
  */
 
 package org.apache.cocoon.generation;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.avalon.framework.parameters.ParameterException;
 import org.apache.avalon.framework.parameters.Parameters;
 import org.apache.cocoon.ProcessingException;
 import org.apache.cocoon.components.source.SourceUtil;
 import org.apache.cocoon.environment.ObjectModelHelper;
 import org.apache.cocoon.environment.Request;
 import org.apache.cocoon.environment.SourceResolver;
 import org.apache.excalibur.source.Source;
 import org.apache.excalibur.source.SourceException;
 import org.apache.lenya.cms.publication.Document;
 import org.apache.lenya.cms.publication.DocumentIdentityMap;
 import org.apache.lenya.cms.publication.Publication;
 import org.apache.lenya.cms.publication.PublicationUtil;
 import org.apache.lenya.cms.repository.RepositoryUtil;
 import org.apache.lenya.cms.repository.Session;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import org.wyona.wiki.ParseException;
 import org.wyona.wiki.SimpleCharStream;
 import org.wyona.wiki.SimpleNode;
 import org.wyona.wiki.Tree2XML;
 import org.wyona.wiki.WikiParser;
 import org.wyona.wiki.WikiParserTokenManager;
 
 /**
  * Blog entry generator
  */
 public class WikiGenerator extends ServiceableGenerator {
 
     protected Source inputSource = null;
 
     /**
      * Set the request parameters. Must be called before the generate method.
      * 
      * @param resolver
      *            the SourceResolver object
      * @param objectModel
      *            a <code>Map</code> containing model object
      * @param src
      *            the source URI (ignored)
      * @param par
      *            configuration parameters
      */
     public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException,
             SAXException, IOException {
         super.setup(resolver, objectModel, src, par);
         try {
             this.inputSource = super.resolver.resolveURI(src);
         } catch (SourceException se) {
             throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
         }
     }
 
     /**
      * Generate XML data.
      * 
      * @throws SAXException
      *             if an error occurs while outputting the document
      */
     public void generate() throws SAXException, ProcessingException {
         try {    
             InputStream wikiIs = inputSource.getInputStream();
             WikiParser wikiParser = new WikiParser(new WikiParserTokenManager(new SimpleCharStream(
                     new InputStreamReader(wikiIs, "UTF-8"))));
             SimpleNode root = wikiParser.WikiBody();
            Tree2XML wikiTree = new Tree2XML(this.contentHandler);            
             wikiTree.traverseJJTree(root);            
         } catch (SAXException e) {
             throw e;
         } catch (Exception e) {
             throw new ProcessingException(e);
         }
     }
 }
