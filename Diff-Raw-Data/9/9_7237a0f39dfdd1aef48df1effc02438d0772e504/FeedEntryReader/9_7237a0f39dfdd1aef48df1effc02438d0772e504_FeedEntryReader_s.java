 /*
  * This is a utility project for wide range of applications
  *
 * Copyright (C) 8  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.atom;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource.Builder;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.activation.MimeTypeParseException;
 import javax.ws.rs.core.MediaType;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.model.Link;
 
 /**
  * Its purpose is to read a collection of entries from feed recursively. It will go upto depth 'n' as specified by its
  * configuration provided during initialization.
  *
  */
 public class FeedEntryReader<T> {
 
   private final Client client;
   private final List<Map.Entry<String, String>> linkSpecs;
   private final Class<? extends T> clazz;
   private final StreamBasedEntityDeserializer<T> deserializer;
   private final boolean entryAsContent;
 
   /**
    * Construct a reader able to read from any feed upto depth 'n' as specified by linkSpecs.
    * @param client A Jersey Client which can be used to fetch HTTP resource to get the main entry. Can not be null.
    * @param linkSpecs Specs on what type of {@link Link} to read from entries of root feed and subsequent feeds to reach
    *                  to a point where a non-feed resource is GET. Can not be null or empty.
    * @param clazz The {@link Class} to use to de-serialize the main entity. Can not be null
    * @throws IllegalArgumentException If any parameter is null or link spec is empty.
    */
   public FeedEntryReader(Client client, List<Entry<String, String>> linkSpecs, Class<? extends T> clazz) throws
       IllegalArgumentException {
     if (client == null || linkSpecs == null || clazz == null) {
       throw new IllegalArgumentException("No argument in the constructor be null!");
     }
     if (linkSpecs.isEmpty()) {
       throw new IllegalArgumentException("At least one link spec must be provided!");
     }
     this.client = client;
     this.linkSpecs = linkSpecs;
     this.clazz = clazz;
     this.deserializer = null;
     this.entryAsContent = false;
   }
 
   /**
    * Create entry reader to read feed entries and deserialize on the basis of the content data in the respective entry.
    * @param deserializer The deserializer know-how-to
    * @throws IllegalArgumentException If deserializer is null
    */
   public FeedEntryReader(StreamBasedEntityDeserializer<T> deserializer) throws IllegalArgumentException{
     if(deserializer == null) {
       throw new IllegalArgumentException("Deserializer can not be null!");
     }
     this.deserializer = deserializer;
     this.client = null;
     this.linkSpecs = null;
     this.clazz = null;
     this.entryAsContent = true;
 
   }
 
   /**
    * De-serialize the entries from the feed provided.
    * @param rootFeed The feed to fetch the entries for
    * @return Return the object instances for the entries of the feed.
    */
   public List<T> readEntriesFromRooFeed(Feed rootFeed) {
     if (rootFeed == null || rootFeed.getEntries() == null || rootFeed.getEntries().isEmpty()) {
       return Collections.emptyList();
     }
     List<org.apache.abdera.model.Entry> entries = rootFeed.getEntries();
     ArrayList<T> result = new ArrayList<T>(entries.size());
     if (entryAsContent) {
       readEntriesFromTheirContent(entries, result);
     }
     else {
       readEntriesFromTheirLink(entries, result);
     }
     return result;
   }
 
   protected void readEntriesFromTheirContent(List<org.apache.abdera.model.Entry> entries, List<T> entities) {
     for (org.apache.abdera.model.Entry entry : entries) {
       try {
         entities.add(deserializer.deserialze(entry.getContentStream(),
             entry.getContentSrc().toURI(), entry.getContentMimeType().toString()));
       }
       catch (Exception ex) {
         ex.printStackTrace();
       }
     }
   }
 
   protected void readEntriesFromTheirLink(List<org.apache.abdera.model.Entry> entries, List<T> entities) {
     for (org.apache.abdera.model.Entry entry : entries) {
       Map.Entry<String, String> linkSpec = getLinkSpec(0);
       List<Link> links = entry.getLinks(linkSpec.getKey());
       Link mainLink = getWantedLink(links, linkSpec);
       if (mainLink != null) {
         entities.add(fetchObject(mainLink, 1));
       }
       else {
         entities.add(null);
       }
     }
   }
 
   protected T fetchObject(Link fetchLink, int depth) {
     try {
       URI rsrcUri = fetchLink.getHref().toURI();
       Builder webRsrc = client.resource(rsrcUri).accept(fetchLink.getMimeType().toString());
       if (fetchLink.getMimeType().match(MediaType.APPLICATION_ATOM_XML)) {
         Feed nestedFeed = webRsrc.get(Feed.class);
         Map.Entry<String, String> linkSpec = getLinkSpec(depth);
         List<Link> links = nestedFeed.getLinks(linkSpec.getKey());
         Link mainLink = getWantedLink(links, linkSpec);
         if (mainLink != null) {
           return fetchObject(mainLink, depth++);
         }
         else {
           return null;
         }
       }
       else {
         return webRsrc.get(clazz);
       }
     }
     catch (Exception ex) {
       ex.printStackTrace();
     }
     return null;
   }
 
   protected Link getWantedLink(List<Link> links,
                                Entry<String, String> linkSpec) {
     Link mainLink = null;
     for (Link link : links) {
       try {
         if (link.getMimeType().match(linkSpec.getValue())) {
           mainLink = link;
         }
       }
       catch (MimeTypeParseException ex) {
         ex.printStackTrace();
       }
     }
     return mainLink;
   }
 
   protected Entry<String, String> getLinkSpec(int depth) {
     if (depth <= 0) {
       return linkSpecs.get(0);
     }
     else if (depth >= linkSpecs.size()) {
       return getLinkSpec(--depth);
     }
     else {
       return linkSpecs.get(depth);
     }
   }
 }
