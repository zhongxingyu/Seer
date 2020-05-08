 /*
 * Copyright 2006 Wyona
  *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.apache.lenya.search.crawler;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import websphinx.Crawler;
 import websphinx.DownloadParameters;
 import websphinx.EventLog;
 import websphinx.Link;
 import websphinx.LinkTransformer;
 import websphinx.Mirror;
 import websphinx.Page;
 
 /**
  * Crawler which creates a dump of a website.
  */
 public class DumpingCrawler extends Crawler {
 
     private String crawlScopeURL;
 
     private String dumpDir;
     private Mirror mirror;
     
     /**
      * Specify types of links which should be followed.
      * @see websphinx.StandardClassifier
      */
     private static final String[] LINK_TYPES = {"hyperlink", "image", "code", "header-link"};
 
     /**
      * Creates a new SimpleCrawler.
      * @param crawlStartURL 
     *          The URL where the crawl should start.
      * @param crawlScopeURL
      *          Limits the scope of the crawl, only links which match the scope url  
     *          will be followed. Must be a prefix of crawlStartURL.
     *          In most cases, the crawlScopeURL is equal to or the parent of the crawlStartURL.
      * @param dumpDir  
      *          The directory in the filesystem where the dumped files will be stored.
      *          Does not have to exist yet, it will be created by the crawler.
      */
     public DumpingCrawler(String crawlStartURL, String crawlScopeURL, String dumpDir) {
         try {
             this.setRoot(new Link(crawlStartURL));
         } catch (MalformedURLException e) {
             this.setRoot(null);
         }
         if (!crawlStartURL.startsWith(crawlScopeURL)) {
             throw new IllegalArgumentException("crawlScopeURL [" + crawlScopeURL + 
                     "] must be a prefix of crawlStartURL [" + crawlStartURL + "]");
         }
         this.crawlScopeURL = crawlScopeURL;
         this.dumpDir = dumpDir;
         this.setSynchronous(true);
         this.setDomain(Crawler.SERVER);
         this.setLinkType(LINK_TYPES);
         try {
             this.mirror = new Mirror(this.dumpDir, this.crawlScopeURL);
         } catch (IOException e) {
             throw new RuntimeException("Could not create mirror with directory: " + this.dumpDir 
                     + ": " + e, e);
         }
     }
 
     /**
      * @see websphinx.Crawler#visit(websphinx.Page)
      */
     public void visit(Page page) {
         try {
             mirror.writePage(page);
         } catch (IOException e) {
             throw new RuntimeException("Could not save page: url=" + page.getURL() + ": " + e, e);
         }
     }
     
 /*    public void visit(Page page) {
         String pageURL = page.getURL().toString();
         if (pageURL.startsWith(this.crawlScopeURL)) {
             File file = new File(this.dumpDir, pageURL.substring(this.crawlScopeURL.length()));
             //System.out.println("writing file: " + file.getAbsolutePath());
             try {
                 if (!file.exists()) {
                     file.getParentFile().mkdirs();
                     file.createNewFile();
                 }
                 LinkTransformer linkTransformer = new LinkTransformer(new FileOutputStream(file), 
                         page.getContentEncoding());
                 linkTransformer.setBase(page.getBase());
                 linkTransformer.writePage(page);
                 linkTransformer.flush();
             } catch (IOException e) {
                 throw new RuntimeException("Could not save page: url=" + page.getURL() + ", file=" 
                         + file + ": " + e, e);
             }
         }
     }
 */
     public boolean shouldVisit(Link link) {
         if (link.getURL().toString().startsWith(this.crawlScopeURL)) {
             return super.shouldVisit(link);
         } else {
             return false;
         }
     }
     
     public void close() {
         try {
             mirror.close();
         } catch (IOException e) {
             throw new RuntimeException("Could not close mirror: " + e, e);
         }
     }
     
     public static void main(String[] args) {
         String crawlStartURL = args[0];
         String crawlScopeURL = args[1];
         String dumpDir = args[2];
         
         DumpingCrawler crawler = new DumpingCrawler(crawlStartURL, crawlScopeURL, dumpDir);
         
         EventLog eventLog = new EventLog(System.out);
         crawler.addCrawlListener(eventLog);
         crawler.addLinkListener(eventLog);
 
         crawler.run();
         crawler.close();
     }
 }
