 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.komusubi.feeder.sns.twitter.strategy;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.doNothing;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.time.DateUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 import org.komusubi.common.util.Resolver;
 import org.komusubi.feeder.model.FeederMessage;
 import org.komusubi.feeder.model.Message;
 import org.komusubi.feeder.model.Page;
 import org.komusubi.feeder.model.Topic;
 import org.komusubi.feeder.model.Topics;
 import org.komusubi.feeder.sns.History;
 import org.komusubi.feeder.sns.twitter.TweetMessage;
 import org.komusubi.feeder.sns.twitter.TweetTopic;
 import org.komusubi.feeder.sns.twitter.TweetTopics;
 import org.komusubi.feeder.sns.twitter.Twitter4j;
 import org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.FilePageCache;
 import org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.PageCache;
 import org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.TimelinePageCache;
 import org.komusubi.feeder.utils.ResolverUtils.DateResolver;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import twitter4j.Status;
 
 /**
  * @author jun.ozeki
  */
 @RunWith(Enclosed.class)
 public class SleepStrategyTest {
 
     /**
      * PageCache Test class. 
      * @author jun.ozeki
      */
     public static class TimelinePageCacheTest {
         @Mock private Twitter4j mockTwitter4j;
         @Mock private Resolver<Date> mockResolver;
         @Mock private History mockHistory;
         @Mock private Page mockPage;
         @Mock private Topics<? extends Topic> mockTopics;
         @Mock private TweetTopics tweetTopics;
         @Mock private Status mockStatus;
 
         @Before
         public void before() {
             MockitoAnnotations.initMocks(TimelinePageCacheTest.this);
         }
 
         @Test
         public void overOutdated() throws Exception {
             // setup
             Date date = DateUtils.parseDate("2013/08/01 10:11:35", new String[] { "yyyy/MM/dd HH:mm:ss" });
             when(mockResolver.resolve()).thenReturn(date);
             when(mockTwitter4j.history()).thenReturn(mockHistory);
             when(mockHistory.next()).thenReturn(mockPage);
             when(mockPage.topics()).thenReturn(null);
 
             // exercise
 //            SleepStrategy parent = new SleepStrategy(mockTwitter4j, 1L);
             TimelinePageCache cache = new TimelinePageCache(mockTwitter4j, mockResolver, 36000L);
 
             // verify
             assertFalse(cache.outdated());
             verify(mockResolver, times(2)).resolve();
         }
 
         @Test
         public void upToDate() throws Exception {
            // setup 
 
            when(mockResolver.resolve()).thenAnswer(new Answer<Date>() {
 
                private int count = 0;
                @Override
                public Date answer(InvocationOnMock invocation) throws Throwable {
                    Date date;
                    if (count++ == 0)
                        date = DateUtils.parseDate("2013/07/31 19:18:20", new String[]{"yyyy/MM/dd HH:mm:ss"});
                    else 
                        date = DateUtils.parseDate("2013/07/31 20:18:21", new String[]{"yyyy/MM/dd HH:mm:ss"});
                    return date;
                }
                
            });
            when(mockTwitter4j.history()).thenReturn(mockHistory);
            when(mockHistory.next()).thenReturn(mockPage);
            when(mockPage.topics()).thenReturn(null);
            
            // exercise
            TimelinePageCache target = new TimelinePageCache(mockTwitter4j, mockResolver, 36000L);
            
            // verify
            assertTrue(target.outdated());
            verify(mockResolver, times(2)).resolve();
         }
 
         @Test
         public void existsMessage() {
             // setup
             String msg = "sample test message";
             // new another instance does not work, jdk give a same object id.
 //            when(mockStatus.getText()).thenReturn(msg.substring(0, msg.length())); 
             when(mockStatus.getText()).thenReturn(msg);
 
             Topics<? extends Topic> topics = new Topics<Topic>();
             TweetTopic topic = new TweetTopic(mockStatus);
             topics.add(topic);
 
             when(mockTwitter4j.history()).thenReturn(mockHistory);
             when(mockHistory.next()).thenReturn(mockPage);
             doReturn(topics).when(mockPage).topics();
 
             TweetMessage message = new TweetMessage();
 //            message.append(msg.substring(0, msg.length()));
             message.append(msg);
 
             // exercise
             TimelinePageCache cache = new TimelinePageCache(mockTwitter4j, new DateResolver(), 36000L);
            
             // verify
             assertTrue(cache.exists(message));
             verify(mockPage).topics();
         }
     }
     
     public static class FilePageCacheTest {
         
         private File file = new File(System.getProperty("java.io.tmpdir") + "/unit-feeder.txt");
 
         @Test
         public void replaceRefresh() {
             // setup
             FilePageCache cache = new FilePageCache(file);
             Message message = new TweetMessage();
            for (int i = 0; i < 50; i++) {
                 message.append("text message message  message  message  message  message  message  message  message  message  message  message  message  message : " + i);
             }
             cache.store(message);
 
             // exercise
             cache.refresh();
             
             // verify
             List<String> items = cache.cache();
            assertThat(items.size(), is(40));
         }
         
         @Test
         public void stripSchema() {
             // setup
             FilePageCache cache = new FilePageCache(file);
             String stripped = cache.strip("message test http://localhost/test abcdefg");
             assertThat(stripped, is("message test abcdefg"));
         }
 
         @Test
         public void existsAllMessages() {
             // setup
             final String[] scripts = new String[]{"script no 1.", "script no 2.", "script no 3."};
             
             FilePageCache cache = new FilePageCache(file) {
                 @Override
                 public List<String> cache() {
                     return Arrays.asList(scripts[0], scripts[1], scripts[2]);
                 }
             };
             // use FeederMessage, because TweetMessage adjust text length by append method.
             Message message = new FeederMessage();
             message.append(scripts[0])
                     .append(scripts[1])
                     .append(scripts[2]);
             
             // exercise & verify
             assertTrue(cache.exists(message));
         }
         
         @Test
         public void existsMessages() {
             // setup
             final String[] scripts = new String[]{"tweet message already no 1.", "tweet message already no 2.", "tweet message already no 3."};
             FilePageCache cache = new FilePageCache(file) {
                 @Override
                 public List<String> cache() {
                     return Arrays.asList(scripts[0], scripts[1], scripts[2]);
                 }
             };
             // use FeederMessage, because TweetMessage adjust text length by append method.
             Message message = new FeederMessage();
             message.append(scripts[2])
                     .append(scripts[0])
                     .append(scripts[1]);
 
             // exercise
             assertTrue(cache.exists(message));
         }
     }
 
     /**
      * SleepStrategy Test class.
      * @author jun.ozeki
      */
     public static class SleepStrategyParentTest {
         
         @Mock private PageCache mockCache;
     
         @Before
         public void before() {
             MockitoAnnotations.initMocks(this);
         }
 
         @Test
         public void postAvailable() {
             // setup
             Message message = new TweetMessage();
             doNothing().when(mockCache).refresh();
             when(mockCache.exists(message)).thenReturn(Boolean.FALSE);
             
             // exercise
             SleepStrategy target = new SleepStrategy(1L, mockCache);
             
             // verify
             assertTrue(target.available(message));
             verify(mockCache).exists(message);
         }
         
         @Test
         public void postNotAvailable() {
             // setup
             Message message = new TweetMessage();
             doNothing().when(mockCache).refresh();
             when(mockCache.exists(message)).thenReturn(Boolean.TRUE);
             
             // exercise
             SleepStrategy target = new SleepStrategy(1L, mockCache);
             
             // verify 
             assertFalse(target.available(message));
             verify(mockCache).exists(message);
         }
     }
 
 }
