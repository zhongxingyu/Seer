 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.channel4;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.ContentType;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Country;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.query.content.PerPublisherCurieExpander;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
import org.joda.time.LocalDate;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Feed;
 import com.sun.syndication.feed.atom.Link;
 
 public class C4EpisodesExtractor implements ContentExtractor<Feed, List<Episode>> {
     
 	public static final String DC_GUIDANCE = "dc:relation.Guidance";
 	public static final String DC_TERMS_AVAILABLE = "dcterms:available";
 	public static final String DC_TX_DATE = "dc:date.TXDate";
 	public static final String DC_START_TIME = "dc:relation.TXStartTime";
 	public static final String DC_TX_CHANNEL = "dc:relation.TXChannel";
 	public static final String EPISODE_TITLE_TEMPLATE = "Series %s Episode %s";
 	public static final String DC_EPISODE_NUMBER = "dc:relation.EpisodeNumber";
 	public static final String DC_SERIES_NUMBER = "dc:relation.SeriesNumber";
 
	private static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(\\d{4}-\\d{2}-\\d{2}); end=(\\d{4}-\\d{2}-\\d{2}); scheme=W3C-DTF");
 
 	public static Map<String, String> CHANNEL_LOOKUP = channelLookup();
 	private final AdapterLog log;
 	
 	public C4EpisodesExtractor(AdapterLog log) {
         this.log = log;
 	}
 	
 	private static Map<String, String> channelLookup() {
 		Map<String, String> channelLookup = Maps.newHashMap();
 		channelLookup.put("C4", "http://www.channel4.com");
 		channelLookup.put("M4", "http://www.channel4.com/more4");
 		channelLookup.put("E4", "http://www.e4.com");
 		return channelLookup;
 	}
 	
 	private boolean include4odInfo = false;
 	private boolean inlcudeBroadcasts = false;
 	
 	public C4EpisodesExtractor includeOnDemands() {
 		this.include4odInfo = true;
 		return this;
 	}
 	
 	public C4EpisodesExtractor includeBroadcasts() {
 		this.inlcudeBroadcasts = true;
 		return this;
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Episode> extract(Feed source) {
 		return (List) extractItems(source, true);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Clip> extractClips(Feed source) {
 		return (List) extractItems(source, false);
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<Item> extractItems(Feed source, boolean isEpisode) {
 		List<Item> items = Lists.newArrayList();
 		
 		for (Entry entry : (List<Entry>) source.getEntries()) {
 			Map<String, String> lookup = C4AtomApi.foreignElementLookup(entry);
 
 			
 			Item episode = isEpisode ? createEpisode(source, entry, lookup) : createClip(source, entry, lookup);
 			
 			if (episode == null) {
 				continue;
 			}
 			
 			episode.setLastUpdated(new DateTime(source.getUpdated(), DateTimeZones.UTC));
 			
 			
 			episode.setDescription(description(entry));
 			
 			Element mediaGroup = C4AtomApi.mediaGroup(entry);
 
 			
 			if (mediaGroup != null) {
 				Element thumbnail = mediaGroup.getChild("thumbnail", C4AtomApi.NS_MEDIA_RSS);
 				if (thumbnail != null) {
 					Attribute thumbnailUri = thumbnail.getAttribute("url");
 					C4AtomApi.addImages(episode, thumbnailUri.getValue());
 				}
 			}
 			
 			Version version = versionFor(entry, episode, mediaGroup, lookup);
 		
 			if (version != null) {
 				episode.addVersion(version);
 			}
 			items.add(episode);
 		}
 		return items;
 	}
 	
 	
 	private Clip createClip(Feed source, Entry entry, Map<String, String> lookup) {
 
 		String clipUri = C4AtomApi.clipUri(entry);
 		
 		if (clipUri == null) {
 		    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to derive URI for C4 clip with id: " + entry.getId()).withSource(this.getClass()));
 		    return null;
 		}
 		
 		Clip episode = new Clip(clipUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(clipUri), Publisher.C4);
 
 		String fourOdUri = C4AtomApi.fourOdUri(entry);
 		if (fourOdUri != null) {
 			episode.addAlias(fourOdUri);
 		}
 
 		episode.setTitle(title(entry));
 		episode.setContentType(ContentType.VIDEO);
 		episode.setIsLongForm(true);
 		return episode;
 	}
 
 	private Item createEpisode(Feed source, Entry entry, Map<String, String> lookup) {
 		
 		String feedTitle = Strings.nullToEmpty(source.getTitle());
 
 		Integer seriesNumber = C4AtomApi.readAsNumber(lookup, DC_SERIES_NUMBER);
 		Integer episodeNumber = C4AtomApi.readAsNumber(lookup, DC_EPISODE_NUMBER);
 		
 		String itemUri = C4AtomApi.canonicalUri(entry);
 		
 		if (itemUri == null) {
 			// fall back to hacking the uri out of the feed
 			itemUri = extarctUriFromLink(source, entry);
 		}
 		if (itemUri == null) {
 		    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to derive URI for c4 episode with id: " + entry.getId()).withSource(this.getClass()));
 		    return null;
 		}
 		
 		Episode episode = new Episode(itemUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri), Publisher.C4);
 
 		String fourOdUri = C4AtomApi.fourOdUri(entry);
 		if (fourOdUri != null) {
 			episode.addAlias(fourOdUri);
 		}
 		
 		
 		if (C4AtomApi.isAnEpisodeId(entry.getId())) {
 			episode.addAlias(entry.getId());
 		}
 
 		episode.setTitle(title(entry));
 		
 		if ((Strings.isNullOrEmpty(episode.getTitle()) || feedTitle.startsWith(episode.getTitle())) && episodeNumber != null && seriesNumber != null) {
 			episode.setTitle(String.format(EPISODE_TITLE_TEMPLATE , seriesNumber, episodeNumber));
 		}
 		
 		episode.setEpisodeNumber(episodeNumber);
 		episode.setSeriesNumber(seriesNumber);
 		episode.setIsLongForm(true);
 		episode.setContentType(ContentType.VIDEO); 
 		return episode;
 	}
 
 	public Version versionFor(Entry entry, Item episode, Element mediaGroup, Map<String, String> lookup) {
 		Set<Country> availableCountries = null;
 		if (mediaGroup != null) {
 			Element restriction = mediaGroup.getChild("restriction", C4AtomApi.NS_MEDIA_RSS);
 			if (restriction != null && restriction.getValue() != null) {
 				availableCountries = Countries.fromDelimtedList(restriction.getValue());
 			}
 		}
 		
 		
 		String uri = C4AtomApi.fourOdUri(entry);
 		if (uri == null) {
 			uri = C4AtomApi.clipUri(entry);
 		}
 		return version(uri, entry.getId(), lookup, availableCountries, new DateTime(entry.getUpdated(), DateTimeZones.UTC));
 	}
 
 	@SuppressWarnings("unchecked")
 	private String extarctUriFromLink(Feed source, Entry entry) {
 		for (Link link : (List<Link>) entry.getOtherLinks()) {
 			Matcher matcher = C4AtomApi.SERIES_AND_EPISODE_NUMBER_IN_ANY_URI.matcher(link.getHref());
 			if (matcher.find()) {
 				return C4AtomApi.episodeUri(C4AtomApi.webSafeNameFromAnyFeedId(source.getId()), Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));	
 			}
 		}
 		return null;
 	}
 
 	
 	
 	private String description(Entry entry) {
 		com.sun.syndication.feed.atom.Content description = entry.getSummary();
 		if (description == null) {
 			return null;
 		}
 		return description.getValue();
 	}
 	
 	private String title(Entry entry) {
         com.sun.syndication.feed.atom.Content title = entry.getTitleEx();
         if (title == null) {
             return null;
         }
         return title.getValue();
     }
 	
 	private Location location(String uri, String locationId, Map<String, String> lookup, Set<Country> availableCountries, DateTime lastUpdated) {
 		Location location = new Location();
 		location.setUri(uri);
 		location.addAlias(locationId);
 		location.setTransportType(TransportType.LINK);
 		location.setLastUpdated(lastUpdated);
 		
 		// The feed only contains available content
 		location.setAvailable(true);
 		
 		String availability = lookup.get(DC_TERMS_AVAILABLE);
 		
 		if (availability != null) {
 			Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(availability);
			if (matcher.matches()) {
				Policy policy = new Policy()
					.withAvailabilityStart(new DateTime(matcher.group(1)))
					.withAvailabilityEnd(new LocalDate(matcher.group(2)).plusDays(1).toDateTimeAtStartOfDay());
					
				if (availableCountries != null) {
					policy.setAvailableCountries(availableCountries);
				}
				location.setPolicy(policy);
 			}
 		}
 		return location;
 	}
 
 
 	private Version version(String uri, String locationId, Map<String, String> lookup, Set<Country> availableCountries, DateTime lastUpdated) {
 		Version version = new Version();
 		Duration duration = C4AtomApi.durationFrom(lookup);
 		
 		if (duration != null) {
 			version.setDuration(duration);
 		}
 		
 		String guidance = lookup.get(DC_GUIDANCE);
 		if (guidance != null) {
 			version.setRating("http://ref.atlasapi.org/ratings/simple/adult");
 			version.setRatingText(guidance);
 		} else {
 			version.setRating("http://ref.atlasapi.org/ratings/simple/nonadult");
 		} 
 		
 		if (inlcudeBroadcasts) {
 			String txChannel = CHANNEL_LOOKUP.get(lookup.get(DC_TX_CHANNEL));
 			String txDate = lookup.get(DC_TX_DATE);
 			if (txChannel != null) {
 				DateTime txStart = new DateTime(txDate);
 				Broadcast broadcast = new Broadcast(txChannel, txStart, duration);
 				broadcast.setLastUpdated(lastUpdated);
 				version.addBroadcast(broadcast);
 			}
 		}
 		
 		if (include4odInfo) {
 			Encoding encoding = new Encoding();
 			encoding.addAvailableAt(location(uri, locationId, lookup, availableCountries, lastUpdated));
 			version.addManifestedAs(encoding);
 		}
 				
 		return version.getBroadcasts().isEmpty() && version.getManifestedAs().isEmpty() ? null : version;
 	}
 	
 }
