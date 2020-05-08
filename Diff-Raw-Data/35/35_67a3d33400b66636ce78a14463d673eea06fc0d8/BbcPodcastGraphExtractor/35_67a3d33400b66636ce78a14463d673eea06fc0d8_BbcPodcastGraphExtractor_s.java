 /* Copyright 2009 British Broadcasting Corporation
    Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.bbc;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.ContentType;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.remotesite.synd.GenericPodcastGraphExtractor;
 import org.atlasapi.remotesite.synd.SyndicationSource;
 import org.jdom.Element;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 /**
  * {@link BeanGraphExtractor} that processes the entries of a BBC Podcast RSS/Atom feed, 
  * and constructs a {@link Representation}, which may later be handled
  * by the {@link BeanGraphFactory}.
  *
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class BbcPodcastGraphExtractor extends GenericPodcastGraphExtractor {
 
 	private String removeExtensionFrom(String url) {
 		int index = url.lastIndexOf('.');
 		if (index < 0) {
 			return url;
 		} else {
 			return url.substring(0, index);
 		}
 	}
 
 	@Override
 	protected Publisher publisher() {
 		return Publisher.BBC;
 	}
 	
 	@Override
 	public Playlist extract(SyndicationSource source) {
 		Playlist playlist = super.extract(source);
 		
 		ContentType cType = contentTypeFor(source);
 		if (cType != null) {
 			playlist.setContentType(cType);
 			for (Item item : playlist.getItems()) {
 				item.setContentType(cType);
 			}
 		}
 
 //		Don't include a /programmes link this creates duplicate aliases
 //		TODO: Reinstate this code
 //		String slashProgrammesLink = slashProgrammesLinkFor(source.getFeed());
 //		if (slashProgrammesLink != null) {
 //			representation.addAliasFor(source.getUri(), slashProgrammesLink);
 //		}
 		return playlist;
 	}
 	
 	private ContentType contentTypeFor(SyndicationSource source) {
 		SyndFeed feed = source.getFeed();
 		if (feed == null) {
 			return null;
 		}
 		List<Element> foreignMarkup = (List<Element>) feed.getForeignMarkup();
 		
 		for (Element element : foreignMarkup) {
 			if (element.getName().equals("network") &&
 				element.getNamespacePrefix().equals("ppg") &&
 				element.getAttributeValue("id") != null) {
 				
 				String service = element.getAttributeValue("id");
 				
 				return BbcMasterbrandContentTypeMap.lookupService(service).valueOrNull();
 			}
 		}
 		return null;
 	}
 
 	@SuppressWarnings({ "unchecked", "unused" })
 	private String slashProgrammesLinkFor(SyndFeed feed) {
 		
 		List<Element> foreignMarkup = (List<Element>) feed.getForeignMarkup();
 		
 		for (Element element : foreignMarkup) {
 			if (element.getName().equals("systemRef") &&
 				element.getNamespacePrefix().equals("ppg") &&
 				element.getAttributeValue("systemId").equals("pid.brand")) {
 				
 				String pid = element.getAttributeValue("key");
 				
 				return String.format("http://www.bbc.co.uk/programmes/%s", pid);
 			}
 		}
 	
 		return null;
 	}
 
 	@Override
 	protected String itemUri(SyndEntry entry) {
 		return removeExtensionFrom(entry.getLink());
 	}
 }
