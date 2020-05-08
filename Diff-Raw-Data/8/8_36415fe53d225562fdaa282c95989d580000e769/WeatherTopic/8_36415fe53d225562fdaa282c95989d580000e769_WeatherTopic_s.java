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
 package org.komusubi.feeder.aggregator.topic;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 import org.komusubi.feeder.aggregator.scraper.AbstractWeatherScraper;
 import org.komusubi.feeder.aggregator.scraper.HtmlScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherAnnouncementScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherContentScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherContentScraper.Content;
 import org.komusubi.feeder.aggregator.scraper.WeatherTitleScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherTitleScraper.Title;
 import org.komusubi.feeder.aggregator.site.WeatherTopicSite;
 import org.komusubi.feeder.model.Message;
 import org.komusubi.feeder.model.Message.Script;
 import org.komusubi.feeder.model.Tag;
 import org.komusubi.feeder.model.Tags;
 import org.komusubi.feeder.model.Topic;
 import org.komusubi.feeder.spi.FeederMessageProvider;
 
 /**
  * @author jun.ozeki
  */
 public class WeatherTopic implements Topic, Iterable<Script> {
 
     private static final long serialVersionUID = 1L;
     private Date created;
     private Message message;
     private WeatherAnnouncementScraper announceScraper;
     private WeatherTitleScraper titleScraper;
     private WeatherContentScraper contentScraper;
 
     /**
      * create new instance.
      */
     public WeatherTopic() {
         this(new WeatherTopicSite(), new HtmlScraper(), new FeederMessageProvider());
     }
     
     /**
      * create new instance.
      * @param site
      * @param provider
      */
     public WeatherTopic(WeatherTopicSite site, Provider<Message> provider) {
         this(site, new HtmlScraper(), provider);
     }
 
     /**
      * create new instance.
      * @param scraper
      * @param provider
      */
     public WeatherTopic(HtmlScraper scraper, Provider<Message> provider) {
         this(new WeatherTopicSite(), scraper, provider);
     }
 
     /**
      * create new instance.
      * @param site
      * @param scraper
      * @param provider
      */
     public WeatherTopic(WeatherTopicSite site, HtmlScraper scraper, Provider<Message> provider) {
         this(new WeatherContentScraper(site, scraper), 
              new WeatherTitleScraper(site, scraper), 
              new WeatherAnnouncementScraper(site, scraper),
              provider);
     }
 
     /**
      * create new instance.
      * @param topicScraper
      * @param titleScraper
      * @param announceScraper
      * @param provider
      */
     @Inject
     public WeatherTopic(WeatherContentScraper topicScraper, 
                         WeatherTitleScraper titleScraper,
                         WeatherAnnouncementScraper announceScraper, Provider<Message> provider) {
         this.contentScraper = topicScraper;
         this.titleScraper = titleScraper;
         this.announceScraper = announceScraper;
         this.created = new Date();
         this.message = provider.get();
     }
 
     // exclude any scraper, does NOT need to compare.
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         WeatherTopic other = (WeatherTopic) obj;
         if (created == null) {
             if (other.created != null)
                 return false;
         } else if (!created.equals(other.created))
             return false;
         if (message == null) {
             if (other.message != null)
                 return false;
         } else if (!message.equals(other.message))
             return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((created == null) ? 0 : created.hashCode());
         result = prime * result + ((message == null) ? 0 : message.hashCode());
         return result;
     }
 
     /**
     * append item.
     * @param script
     */
    public void add(Script script) {
        message.add(script);
    }

    /**
      * @see org.komusubi.feeder.model.Topic#message()
      */
     @Override
     public Message message() {
         for (Title title: titleScraper) {
             message.append(title)
                     .append("\n");
         }
         boolean topicFound = false;
         for (Content content: contentScraper) {
             message.add(content);
             topicFound = true;
         }
         if (topicFound)
             message.append("\n");
         message.addAll(announceScraper.scrape());
         
         // check duplicate tag
         Tags exists = new Tags();
         for (AbstractWeatherScraper scraper: Arrays.asList(announceScraper, titleScraper, contentScraper)) {
             Tags tags = scraper.site().tags();
             for (Iterator<Tag> it = tags.iterator(); it.hasNext(); ) {
                 Tag tag = it.next();
                 if (exists.contains(tag))
                     continue;
                 message.append(tag.label());
                 if (it.hasNext())
                     message.append(" ");
                 exists.add(tag);
             }
         }
         return message;
     }
   
     /**
      * 
      * @see java.lang.Iterable#iterator()
      */
     @Override
     public Iterator<Script> iterator() {
         return message.iterator();
     }
 
     /**
      * @see org.komusubi.feeder.model.Topic#createdAt()
      */
     @Override
     public Date createdAt() {
         return created;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("WeatherTopic [created=").append(created).append(", message=").append(message).append("]");
         return builder.toString();
     }
 
 }
