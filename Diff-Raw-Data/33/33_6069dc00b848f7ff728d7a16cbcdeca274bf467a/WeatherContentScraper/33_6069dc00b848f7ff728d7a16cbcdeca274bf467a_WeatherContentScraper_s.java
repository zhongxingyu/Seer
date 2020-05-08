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
 package org.komusubi.feeder.aggregator.scraper;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.htmlparser.Node;
 import org.htmlparser.NodeFilter;
 import org.htmlparser.filters.AndFilter;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.filters.NodeClassFilter;
 import org.htmlparser.tags.Div;
 import org.htmlparser.tags.TableColumn;
 import org.htmlparser.tags.TableHeader;
 import org.htmlparser.util.NodeList;
 import org.komusubi.feeder.aggregator.scraper.WeatherContentScraper.Content;
 import org.komusubi.feeder.aggregator.site.WeatherTopicSite;
 import org.komusubi.feeder.model.Message.Script;
 import org.komusubi.feeder.model.Region;
 
 /**
  * @author jun.ozeki
  */
 public class WeatherContentScraper extends AbstractWeatherScraper implements Iterable<Content> {
     /**
      * weather status.
      * @author jun.ozeki
      */
     public static class Status implements Serializable {
         private static final long serialVersionUID = 1L;
         private String status;
 
         /**
          * @param status
          */
         public Status(String status) {
             this.status = status;
         }
 
         /**
          * @see java.lang.Object#equals(java.lang.Object)
          */
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             Status other = (Status) obj;
             if (status == null) {
                 if (other.status != null)
                     return false;
             } else if (!status.equals(other.status))
                 return false;
             return true;
         }
 
         /**
          * @see java.lang.Object#hashCode()
          */
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + ((status == null) ? 0 : status.hashCode());
             return result;
         }
 
         /**
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() {
             StringBuilder builder = new StringBuilder();
             builder.append("WeatherStatus [status=").append(status).append("]");
             return builder.toString();
         }
 
         /**
          * 
          * @return
          */
         public String value() {
             return status;
         }
     }
 
     /**
      * @author jun.ozeki
      */
     public static class Content implements Script {
 
         private static final long serialVersionUID = 1L;
         private Region region;
         private Status status;
 
         /**
          * create new instance.
          * @param region
          * @param status
          */
         public Content(Region region, Status status) {
             this.region = region;
             this.status = status;
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#line()
          */
         @Override
         public String line() {
             if (region == null || status == null)
                 return "";
             return region.name() + ": " + status.value();
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointCount()
          */
         @Override
         public int codePointCount() {
             if (line() == null)
                 return 0;
             return line().codePointCount(0, line().length());
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointSubstring(int, int)
          */
         @Override
         public String codePointSubstring(int begin, int end) {
             // FIXME code point substring.
             if (line() == null)
                 return null;
             return line().substring(begin, end);
         }
 
         /**
          * @see org.komusubi.feeder.model.Message.Script#codePointSubstring(int)
          */
         @Override
         public String codePointSubstring(int begin) {
             if (line() == null)
                 return null;
             return codePointSubstring(begin, line().length());
         }
 
         @Override
         public String toString() {
             StringBuilder builder = new StringBuilder();
             builder.append("Content [region=").append(region).append(", status=").append(status).append("]");
             return builder.toString();
         }
 
     }
 
    private static final String ATTR_VALUE = "weather_info_txtBox mgt20";
    private static final String ATTR_NAME = "class";

     /**
      * create new instance.
      */
     public WeatherContentScraper() {
 
     }
 
     /**
      * create new instance.
      * @param site
      */
     public WeatherContentScraper(WeatherTopicSite site) {
         this(site, new HtmlScraper());
     }
 
     /**
      * create new instance.
      * @param scraper
      */
     public WeatherContentScraper(HtmlScraper scraper) {
         this(new WeatherTopicSite(), scraper);
     }
 
     /**
      * create new instance.
      * @param site
      * @param scraper
      */
     @Inject
     public WeatherContentScraper(WeatherTopicSite site, HtmlScraper scraper) {
         super(site, scraper);
     }
 
     /**
      * scrape for Region.
      * @return
      */
     public List<Region> scrapeRegion() {
         NodeList nodes = scrape(Region.class);
         List<Region> regions = new ArrayList<Region>();
         for (int i = 0; i < nodes.size(); i++) {
             Node node = nodes.elementAt(i);
             regions.add(new Region(node.toPlainTextString()));
         }
         return regions;
     }
 
     /**
      * scrape for WeatherStatus.
      * @return
      */
     public List<Status> scrapeWeatherStatus() {
         NodeList nodes = scrape(Status.class);
         List<Status> statuses = new ArrayList<Status>();
         for (int i = 0; i < nodes.size(); i++) {
             Node node = nodes.elementAt(i);
             String[] values = node.toPlainTextString().split("\n");
             StringBuilder builder = new StringBuilder();
             for (int j = 0; j < values.length; j++) {
                 builder.append(values[j]);
                 if (values.length > j + 1)
                     builder.append(" ");
             }
             statuses.add(new Status(builder.toString()));
         }
         return statuses;
     }
 
     /**
      * scrape html.
      * @return
      */
     public NodeList scrape(Class<?> clazz) {
         if (!clazz.isAssignableFrom(Region.class) && !clazz.isAssignableFrom(Status.class))
             throw new IllegalArgumentException("argument MUST be Region.class or WeatherStatus.class");
 
         Class<?> filterTagClass = null;
         if (clazz.isAssignableFrom(Region.class)) {
             filterTagClass = TableHeader.class;
         } else if (clazz.isAssignableFrom(Status.class)) {
             filterTagClass = TableColumn.class;
         }
         return scraper().scrapeMatchNodes(site().url(), filter(), filterTagClass);
     }
 
     /**
      * WeatherTopics filter for html scraper.
      * @return
      */
     protected NodeFilter filter() {
         return new AndFilter(
                         new NodeClassFilter(Div.class),
                        new HasAttributeFilter(ATTR_NAME, ATTR_VALUE));
     }
 
     /**
      * topic count size.
      * @return
      */
     public int size() {
         int size = scrapeRegion().size();
         if (size != scrapeWeatherStatus().size())
             throw new IllegalStateException("not match size: Region:" + scrapeRegion().size()
                             + ", WeatherStatus:" + scrapeWeatherStatus().size());
         return size;
     }
 
     /**
      * @see java.lang.Iterable#iterator()
      */
     @Override
     public Iterator<Content> iterator() {
         List<Content> list = new ArrayList<Content>();
         for (int i = 0; i < size(); i++) {
             Region region = scrapeRegion().get(i);
             Status status = scrapeWeatherStatus().get(i);
             list.add(new Content(region, status));
         }
         return list.iterator();
     }
 
 }
