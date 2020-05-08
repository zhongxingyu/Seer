 /*
  * Copyright 2010 Lars Heuer (heuer[at]semagia.com). All rights reserved.
  *
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
 package org.isotopicmaps.sdsharetests.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URI;
 
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.isotopicmaps.sdsharetests.IConstants;
 import org.isotopicmaps.sdsharetests.MediaType;
 
 import static org.junit.Assert.*;
 
 import nu.xom.Document;
 import nu.xom.Node;
 import nu.xom.Nodes;
 
 /**
  * Abstract test case which provides some useful utility methods.
  * 
  * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
  * @version $Rev:$ - $Date:$
  */
 abstract class AbstractServerTestCase implements IConstants{
 
     private static final String _UNKNOWN_MEDIA_TYPE = "application/x-hello-iam+unknown";
 
     /**
      * Helper method to fetch an Atom feed.
      * 
      * The feed is not validated.
      *
      * @param uri
      * @return
      * @throws IOException
      */
     protected InputStream fetchAtomFeed(final URI uri) throws IOException {
         final HttpURLConnection conn = Utils.connect(uri, MEDIA_TYPE_ATOM_XML);
         assertEquals(HttpURLConnection.HTTP_OK, conn.getResponseCode());
         assertTrue(MediaType.ATOM_XML.isCompatible(MediaType.valueOf(conn.getContentType())));
         return conn.getInputStream();
     }
 
     /**
      * Executes an XPath query against the provided {@code node} using the default XPathContext.
      *
      * @node The context node.
      * @query The XPath expression.
      */
     protected static Nodes query(final Node node, final String query) {
         return node.query(query, Utils.getDefaultXPathContext());
     }
 
     /**
      * Tries to open a connection to the provided URI (which must be a valid URL) with
      * an unknown media type and expects a 
      *
      * @param uri The URI to test
      */
     protected void testWithUnknownMediaType(final URI uri) throws Exception {
         //TODO: Think about this
 //        final HttpURLConnection conn = Utils.connect(uri, _UNKNOWN_MEDIA_TYPE);
 //        assertEquals("Expected a Not Acceptable response for " + uri, HttpURLConnection.HTTP_NOT_ACCEPTABLE, conn.getResponseCode());
     }
 
     /**
      * Does some basic tests (i.e. if the atom:id is provided etc.) to validate
      * an Atom 1.0 feed.
      * @throws Exception 
      *
      * @doc The feed document
      */
     protected void testAtomFeedValidity(final Document doc) throws Exception {
         Nodes nodes = query(doc, "atom:feed");
         assertEquals("Expected exactly one atom:feed element", 1, nodes.size());
         final Node feed = nodes.get(0);
         validateEntryCommons(feed);
         // If the feed has no author, all entries must have an author
         nodes = query(feed, "atom:author");
         boolean feedAuthorProvided = false;
         if (nodes.size() > 0) {
             feedAuthorProvided = true;
             for (int i=0; i<nodes.size(); i++) {
                 validateAuthor(nodes.get(i));
             }
         }
         // Check each atom:entry for validity
         final Nodes entries = query(feed, "atom:entry");
         if (entries.size() == 0) {
             //TODO: Is this true?
             assertTrue("The feed has no entries, therefor the atom:feed must have an atom:author", feedAuthorProvided);
         }
         for (int i=0; i<entries.size(); i++) {
             Node entry = entries.get(i);
             validateEntryCommons(entry);
             final Nodes authors = query(entry, "atom:author");
             if (!feedAuthorProvided) {
                 // atom:feed has no author, all atom:entry children MUST have an author.
                 assertTrue("atom:feed has no atom:author, each atom:entry must have 1..n atom:author children", 
                             authors.size() > 0);
             }
             for (int j=0; j<authors.size(); j++) {
                validateAuthor(authors.get(j));
             }
             // Each entry must have a link rel="alternate" iff it has no content
             if (query(entry, "atom:content").size() == 0) {
                 assertTrue("The entry " + entry.toXML() + " has no atom:content and no atom:link[@rel='alternate'] child", 
                         query(entry, "atom:link[@rel='alternate']").size() > 0);
             }
         }
     }
 
     protected void validateEntryCommons(final Node node) throws Exception {
         Nodes nodes = query(node, "atom:id");
         assertEquals("No atom:id found", 1, nodes.size());
         final Node id = nodes.get(0);
         assertFalse("atom:id must be an IRI", id.getValue().isEmpty());
         nodes = query(node, "atom:title");
         assertEquals("No atom:title found", 1, nodes.size());
         final Node title = nodes.get(0);
         assertFalse("atom:title should not be empty", title.getValue().isEmpty());
         nodes = query(node, "atom:updated");
         assertEquals("No atom:updated found", 1, nodes.size());
         final String updated = nodes.get(0).getValue();
         final XMLGregorianCalendar dateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(updated);
         assertEquals(dateTime.toXMLFormat(), updated);
     }
 
     /**
      * Validates atom:author.
      *
      * @author The author to validate.
      */
     protected final void validateAuthor(final Node author) {
        assertNotNull("Internal test error, got an author node which is null", author);
         // atom:name is required
         Nodes nodes = query(author, "atom:name");
        assertEquals("atom:author must have 1 atom:name child", 1, nodes.size());
         assertFalse("atom:name should not be empty", nodes.get(0).getValue().isEmpty());
         // atom:email is optional
         nodes = query(author, "atom:email");
         if (nodes.size() != 0) {
             assertEquals("atom:author must have 0..1 atom:email children", 1, nodes.size());
             assertFalse("atom:email must be empty", nodes.get(0).getValue().isEmpty());
         }
         // atom:uri is optional
         nodes = query(author, "atom:uri");
         if (nodes.size() != 0) {
             assertEquals("atom:uri must have 0..1 atom:uri children", 1, nodes.size());
             assertFalse("atom:uri must be an IRI", nodes.get(0).getValue().isEmpty());
         }
     }
 
     /**
      * Returns the overview feed.
      *
      * The feed is validated.
      *
      * @return The overview feed as DOM.
      * @throws Exception In case of an error.
      */
     protected Document fetchOverviewFeed() throws Exception {
         return fetchAtomFeedAsDOM(Utils.getServerAddress());
     }
 
     /**
      * Returns the feed under the specified URI.
      *
      * The feed is validated.
      *
      * @param uri The URI to retrieve the feed from.
      * @return The feed as DOM.
      * @throws Exception In case of an error.
      */
     protected Document fetchAtomFeedAsDOM(final URI uri) throws Exception {
         final Document doc = Utils.makeDocument(fetchAtomFeed(uri), uri);
         testAtomFeedValidity(doc);
         return doc;
     }
 
     /**
      * Checks the returned media type and if the collection feed exists.
      *
      * @param uri The IRI to check.
      * @param mediaType The expected media type of the IRI.
      * @throws IOException In case of an error.
      */
     protected void testURIRetrieval(final URI uri, final String mediaType) throws IOException {
         final HttpURLConnection conn = Utils.connect(uri, mediaType);
         final MediaType requestMediaType = MediaType.valueOf(mediaType);
         final MediaType responseMediaType = MediaType.valueOf(conn.getContentType());
         assertEquals(HttpURLConnection.HTTP_OK, conn.getResponseCode());
         assertTrue(requestMediaType.isCompatible(responseMediaType));
     }
 
 }
