 package org.atlasapi.remotesite.itv;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Country;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class ItvMercuryEpisodeExtractor implements ContentExtractor<Map<String, Object>, Episode> {
 
     private static final Pattern DATE = Pattern.compile("/Date\\((\\d+)\\)/");
 
     @SuppressWarnings("unchecked")
     @Override
     public Episode extract(Map<String, Object> source) {
         String title = null;
         String desc = null;
         Set<String> genres = Sets.newHashSet();
         String id = null;
 
         if (source.containsKey("Result")) {
             List<Map<String, Object>> results = (List<Map<String, Object>>) source.get("Result");
             for (Map<String, Object> result : results) {
                 if (result.containsKey("Details")) {
                     List<Map<String, Object>> programmes = (List<Map<String, Object>>) result.get("Details");
                     for (Map<String, Object> programme : programmes) {
                         if (programme.containsKey("Episode")) {
                             List<Map<String, Object>> episodes = (List<Map<String, Object>>) programme.get("Episodes");
                             for (Map<String, Object> episode : episodes) {
                                 source = episode;
                             }
                         }
                     }
                 }
             }
         }
 
         if (source.containsKey("Episode")) {
             Map<String, Object> episodeInfo = (Map<String, Object>) source.get("Episode");
 
             title = (String) episodeInfo.get("Title");
             desc = (String) episodeInfo.get("ShortSynopsis");
             genres = ItvMercuryBrandExtractor.genres((String) episodeInfo.get("Genres"));
         }
 
         if (source.containsKey("Vodcrid")) {
             Map<String, Object> crid = (Map<String, Object>) source.get("Vodcrid");
             id = (String) crid.get("Id");
         }
 
         if (id == null) {
             return null;
         }
 
         Episode episode = new Episode(ItvMercuryBrandAdapter.BASE_URL + id, "itv:" + id, Publisher.ITV);
         episode.setTitle(title);
         episode.setDescription(desc);
         episode.setGenres(genres);
         episode.setImage((String) source.get("PosterFrameUri"));
 
         Version version = new Version();
         episode.addVersion(version);
 
         Integer duration = (Integer) source.get("Duration");
         DateTime startTime = broadcast((String) source.get("LastBroadcast"));
         String broadcastOn = channel(source);
 
         if (startTime != null && duration != null && broadcastOn != null) {
             DateTime endTime = startTime.plusMinutes(duration);
             Broadcast broadcast = new Broadcast(broadcastOn, startTime, endTime);
             broadcast.setLastUpdated(new DateTime(DateTimeZones.UTC));
             version.addBroadcast(broadcast);
         }
 
         Encoding encoding = new Encoding();
         version.addManifestedAs(encoding);
 
         Integer daysRemaining = (Integer) source.get("DaysRemaining");
         if (daysRemaining != null && startTime != null) {
             Location location = new Location();
             encoding.addAvailableAt(location);
 
             location.setAvailable(true);
             location.setLastUpdated(new DateTime(DateTimeZones.UTC));
             location.setUri(ItvMercuryBrandAdapter.BASE_URL + id);
             location.setTransportType(TransportType.LINK);
 
             Policy policy = new Policy();
             policy.setAvailableCountries(ImmutableSet.<Country> of(Countries.GB));
             policy.setAvailabilityStart(startTime);
             policy.setAvailabilityEnd(startTime.plusDays(daysRemaining));
             location.setPolicy(policy);
         }
 
         return episode;
     }
 
     private DateTime broadcast(String lastBroadcast) {
         if (lastBroadcast != null) {
             Matcher matcher = DATE.matcher(lastBroadcast);
             if (matcher.matches()) {
                 Long millis = Long.valueOf(matcher.group(1));
                 return new DateTime(millis, DateTimeZones.UTC);
             }
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     private String channel(Map<String, Object> source) {
         if (source.containsKey("Channel")) {
             Map<String, Object> channel = (Map<String, Object>) source.get("Channel");
             String channelName = (String) channel.get("Name");
             if (channelName != null) {
                 return "http://www.itv.com/channels/" + channelName.toLowerCase();
             }
         }
 
         return null;
     }
 }
