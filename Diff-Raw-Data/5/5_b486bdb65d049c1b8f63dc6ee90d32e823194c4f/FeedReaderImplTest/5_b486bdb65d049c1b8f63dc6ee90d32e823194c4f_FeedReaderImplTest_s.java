 /*
  * Copyright 2009 OW2 Chameleon
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.ow2.chameleon.syndication.rome.test;
 
 import java.net.URL;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.ow2.chameleon.syndication.FeedEntry;
 import org.ow2.chameleon.syndication.rome.FeedReaderImpl;
 
 
 public class FeedReaderImplTest {
 
     @Test
     public void testFeedReaderImpl() throws Exception {
         URL ak = new URL("http://blog.akquinet.de/feed/");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 1000, 5);
         Assert.assertEquals(ak.toExternalForm(), reader.getURL());
         Assert.assertEquals("akquinet-blog", reader.getTitle());
         Thread.sleep(2000);
         reader.stop();
     }
 
     @Test
     public void testReadAtom() throws Exception {
         URL ak = new URL("file:src/test/resources/atom-v1.xml");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 10000, 5);
         Assert.assertEquals(ak.toExternalForm(), reader.getURL());
         Assert.assertEquals("Example Feed", reader.getTitle());
         Assert.assertEquals(1, reader.getEntries().size());
         Assert.assertEquals(1, reader.getRecentEntries().size());
         Assert.assertEquals("Atom-Powered Robots Run Amok", reader
                 .getLastEntry().title());
         Assert.assertEquals("Some text.", reader.getLastEntry().content());
         Assert.assertEquals(0, reader.getLastEntry().categories().size());
         Assert.assertEquals("", reader.getLastEntry().author());
         Assert.assertEquals("http://example.org/2003/12/13/atom03", reader
                 .getLastEntry().url());
         Assert.assertNotNull(reader.getLastEntry().publicationDate());
         reader.stop();
     }
 
     @Test
     public void testEmptyAtom() throws Exception {
         URL ak = new URL("file:src/test/resources/atom-empty.xml");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 10000, 5);
         Assert.assertEquals(ak.toExternalForm(), reader.getURL());
         Assert.assertEquals("Example Feed", reader.getTitle());
         Assert.assertEquals(0, reader.getEntries().size());
         Assert.assertEquals(0, reader.getRecentEntries().size());
         Assert.assertNull(reader.getLastEntry());
         reader.stop();
     }
 
     @Test
     public void testReadRSS() throws Exception {
         URL ak = new URL("file:src/test/resources/rss-v2.xml");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 10000, 5);
         Assert.assertEquals(ak.toExternalForm(), reader.getURL());
         Assert.assertEquals("RSS Example", reader.getTitle());
         Assert.assertEquals(1, reader.getEntries().size());
         Assert.assertEquals(1, reader.getRecentEntries().size());
         Assert.assertEquals("Item Example", reader.getLastEntry().title());
         Assert.assertEquals("This is an example of an Item", reader
                 .getLastEntry().content());
         Assert.assertEquals(0, reader.getLastEntry().categories().size());
         Assert.assertEquals("", reader.getLastEntry().author());
         Assert.assertEquals("http://www.domain.com/link.htm", reader
                 .getLastEntry().url());
         Assert.assertNotNull(reader.getLastEntry().publicationDate());
         reader.stop();
     }
 
     @Test
     public void testEmptyRSS() throws Exception {
         URL ak = new URL("file:src/test/resources/rss-empty.xml");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 10000, 5);
         Assert.assertEquals(ak.toExternalForm(), reader.getURL());
         Assert.assertEquals("RSS Example", reader.getTitle());
         Assert.assertEquals(0, reader.getEntries().size());
         Assert.assertEquals(0, reader.getRecentEntries().size());
         Assert.assertNull(reader.getLastEntry());
         reader.stop();
     }
 
     @Test(expected = Exception.class)
     public void testCorruptedFeed() throws Exception {
         URL ak = new URL("file:src/test/resources/rss-corrupted.xml");
         new FeedReaderImpl(ak, 10000, 5);
     }
 
     @Test
     public void testGetEntries() throws Exception {
         URL ak = new URL("http://blog.akquinet.de/feed/");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 1000, 5);
         List<FeedEntry> list = reader.getEntries();
         for (FeedEntry entry : list) {
             Assert.assertNotNull(entry.title());
             Assert.assertNotNull(entry.author());
             Assert.assertNotNull(entry.publicationDate());
             System.out.println(entry.title() + " - " + entry.categories());
         }
     }
 
     @Test
     public void testGetLastEntry() throws Exception {
         URL ak = new URL(
                "http://blog.akquinet.de/category/all/osgi-and-mobile-solutions/feed/");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 1000, 5);
         FeedEntry entry = reader.getLastEntry();
         Assert.assertNotNull(entry.title());
         Assert.assertNotNull(entry.author());
         Assert.assertNotNull(entry.publicationDate());
         URL link = new URL(entry.url());
         Assert.assertNotNull(link.openConnection());
     }
 
     @Test
     public void testGetRecentEntries() throws Exception {
         URL ak = new URL(
                "http://blog.akquinet.de/category/all/osgi-and-mobile-solutions/feed/");
         FeedReaderImpl reader = new FeedReaderImpl(ak, 1000, 5);
         List<FeedEntry> list = reader.getRecentEntries();
         for (FeedEntry e : list) {
             System.out.println(e.title());
         }
         Assert.assertEquals(5, list.size());
         for (FeedEntry entry : list) {
             Assert.assertNotNull(entry.title());
             Assert.assertNotNull(entry.author());
             Assert.assertNotNull(entry.publicationDate());
         }
     }
 
 }
