 /*******************************************************************************
  * Copyright (C) 2009, 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  * 
  * These coded instructions, statements, and computer programs contain
  * proprietary information of Hoccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Java-API".
  * 
  * Linccer Java-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Java-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Java-API. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.hoccer.api;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.junit.Test;
 
 import com.hoccer.data.GenericStreamableContent;
 import com.hoccer.data.StreamableContent;
 import com.hoccer.data.StreamableString;
 import com.hoccer.http.AsyncHttpPut;
 import com.hoccer.http.AsyncHttpRequest;
 import com.hoccer.http.ResponseHandlerForTesting;
 import com.hoccer.tools.HttpHelper;
 import com.hoccer.tools.TestHelper;
 
 public class TestFileCache {
 
     @Test
     public void storeTextInFileCache() throws Exception {
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
 
         String locationUri = filecache.store(new StreamableString("hello world"), 10);
         assertThat(locationUri, containsString("https://filecache"));
         assertThat(locationUri, containsString(".hoccer.com"));
 
         StreamableContent data = new StreamableString();
         filecache.fetch(locationUri, data);
 
         assertThat(data.toString(), is(equalTo("hello world")));
 
         Thread.sleep(2000);
         filecache.fetch(locationUri, data);
     }
 
     @Test
     public void storeBinaryDataInFileCache() throws Exception {
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
 
         GenericStreamableContent content = new GenericStreamableContent();
         content.setFilename("data.png");
         content.setContentType("image/png");
         byte[] data = { 23, 42, 23 };
         content.openOutputStream().write(data);
 
         String locationUri = filecache.store(content, 2);
         assertThat(locationUri, containsString("https://filecache"));
         assertThat(locationUri, containsString(".hoccer.com"));
     }
 
     @Test
     public void plainAsyncStoringOfText() throws Exception {
 
         String uri = ClientConfig.getFileCacheBaseUri() + "/" + UUID.randomUUID()
                 + "?expires_in=10000";
 
         AsyncHttpPut storeRequest = new AsyncHttpPut(ApiSigningTools.sign(uri,
                 TestApiKeySigning.demoKey, TestApiKeySigning.demoSecret));
         storeRequest.setBody(new StreamableString("hello world"));
         storeRequest.start();
 
         blockUntilRequestIsDone(storeRequest);
 
        assertThat(storeRequest.getStatusCode(), is(equalTo(200)));
         assertThat(storeRequest.getBodyAsString(), containsString("https://filecache"));
         assertThat(storeRequest.getBodyAsString(), containsString(".hoccer.com"));
     }
 
     protected void blockUntilRequestIsDone(final AsyncHttpRequest pRequest) throws Exception {
         TestHelper.blockUntilTrue("request should have been performed by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return pRequest.isTaskCompleted();
                     }
                 });
     }
 
     @Test
     public void asyncStoringOfTextViaFileCache() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting();
         String uri = filecache.asyncStore(new StreamableString("hello world 12 11"), 10, handler);
 
         TestHelper.blockUntilTrue("request should have been successful by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.wasSuccessful;
                     }
                 });
 
         assertThat(handler.body.toString(), containsString("https://filecache"));
         assertThat(HttpHelper.getAsString(uri), is(equalTo("hello world 12 11")));
     }
 
     @Test
     public void asyncFetchingOfTextViaFileCache() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
         final String uri = filecache.store(new StreamableString("hello file cache"), 10);
 
         final GenericStreamableContent sink = new GenericStreamableContent();
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting() {
             @Override
             public void onHeaderAvailable(HashMap<String, String> headers) {
                 AsyncHttpRequest.setTypeAndFilename(sink, headers, uri);
             }
         };
         filecache.asyncFetch(uri, sink, handler);
 
         TestHelper.blockUntilTrue("request should have been successful by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.wasSuccessful;
                     }
                 });
         assertThat(uri, containsString("https://filecache"));
        assertThat(sink.getContentType(), is(equalTo("text/plain;charset=utf-8")));
         assertThat(handler.body.toString(), is(equalTo("hello file cache")));
 
     }
 }
