 package org.atlasapi.remotesite.bbc.ion;
 
 import static org.atlasapi.media.entity.Publisher.BBC;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;
 
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
 import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
 import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
 import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
 import org.atlasapi.remotesite.bbc.ion.model.IonFeed;
 import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
 import org.atlasapi.remotesite.bbc.ion.model.IonVersion;
 
 import com.google.common.base.Strings;
 import com.google.common.primitives.Ints;
 import com.google.gson.reflect.TypeToken;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.time.SystemClock;
 
 public class BbcIonEpisodeDetailItemFetcherClient implements BbcItemFetcherClient {
 
     private static final String CURIE_BASE = "bbc:";
     private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
     private static final String EPISODE_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/episodedetail/episode/%s/include_broadcasts/1/clips/include/next_broadcasts/1/allow_unavailable/1/format/json";
 
     private final BbcIonDeserializer<IonFeed<IonEpisodeDetail>> ionDeserialiser = BbcIonDeserializers.deserializerForType(new TypeToken<IonFeed<IonEpisodeDetail>>(){});
     private final BbcProgrammeEncodingAndLocationCreator enodingCreator = new BbcProgrammeEncodingAndLocationCreator(new SystemClock());
     private final AdapterLog log;
     
     public BbcIonEpisodeDetailItemFetcherClient(AdapterLog log) {
         this.log = log;
     }
     
     private IonEpisodeDetail getEpisodeDetail(String pid) {
         try {
             return ionDeserialiser.deserialise(HttpClients.webserviceClient().getContentsOf(String.format(EPISODE_DETAIL_PATTERN, pid))).getBlocklist().get(0);
         } catch (HttpException e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Could get episode detail for " + pid));
             return null;
         }
     }
     
     @Override
     public Item createItem(String episodeId) {
         IonEpisodeDetail episodeDetail = getEpisodeDetail(episodeId);
         if(episodeDetail != null) {
             return createItemFrom(episodeDetail);
         }
         return null;
     }
 
     private Item createItemFrom(IonEpisodeDetail episodeDetail) {
         Item item = null;
         if (!Strings.isNullOrEmpty(episodeDetail.getBrandId()) || !Strings.isNullOrEmpty(episodeDetail.getSeriesId())) {
             item = new Episode(SLASH_PROGRAMMES_ROOT+episodeDetail.getId(), CURIE_BASE+episodeDetail.getId(), BBC);
             updateEpisodeDetails((Episode)item, episodeDetail);
         } else {
             item = new Item(SLASH_PROGRAMMES_ROOT+episodeDetail.getId(), CURIE_BASE+episodeDetail.getId(), BBC);
         }
         return updateItemDetails(item, episodeDetail);
     }
 
     private void updateEpisodeDetails(Episode item, IonEpisodeDetail episodeDetail) {
         if(episodeDetail.getSeriesId() != null) {
             item.setSeriesUri(SLASH_PROGRAMMES_ROOT + episodeDetail.getSeriesId());
         }
         if(Strings.isNullOrEmpty(episodeDetail.getSubseriesId()) && episodeDetail.getPosition() != null) {
             item.setSeriesNumber(Ints.saturatedCast(episodeDetail.getPosition()));
         }
     }
 
     private Item updateItemDetails(Item item, IonEpisodeDetail episode) {
         
         item.setTitle(episode.getTitle());
         item.setDescription(episode.getSynopsis());
         item.setThumbnail(episode.getMyImageBaseUrl() + episode.getId() + "_150_84.jpg");
         item.setImage(episode.getMyImageBaseUrl() + episode.getId() + "_640_360.jpg");
 
         if(episode.getVersions() != null) {
             for (IonVersion ionVersion : episode.getVersions()) {
                item.addVersion(versionFrom(ionVersion));
             }
         }
 
         return item;
     }
 
     private Broadcast broadcastFrom(IonBroadcast ionBroadcast) {
         String serviceUri = BbcIonServices.get(ionBroadcast.getService());
         if(serviceUri == null) {
             log.record(new AdapterLogEntry(WARN).withDescription("Couldn't find service URI for Ion Service " + ionBroadcast.getService()).withSource(getClass()));
             return null;
         } else {
             Broadcast broadcast = new Broadcast(serviceUri, ionBroadcast.getStart(), ionBroadcast.getEnd());
             broadcast.withId(CURIE_BASE + ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
             broadcast.setLastUpdated(ionBroadcast.getUpdated());
             return broadcast;
         }
     }
     
    private Version versionFrom(IonVersion ionVersion) {
         Version version = new Version();
         version.setCanonicalUri(SLASH_PROGRAMMES_ROOT + ionVersion.getId());
         version.setPublishedDuration(ionVersion.getDuration() != null ? Ints.saturatedCast(ionVersion.getDuration()) : null);
         version.setProvider(BBC);
         if(ionVersion.getBroadcasts() != null) {
             for (IonBroadcast ionBroadcast : ionVersion.getBroadcasts()) {
                 Broadcast broadcast = broadcastFrom(ionBroadcast);
                 if(broadcast != null) {
                     version.addBroadcast(broadcast);
                 }
             }
         }
         if(ionVersion.getOndemands() != null) {
             for (IonOndemandChange ondemand : ionVersion.getOndemands()) {
                Maybe<Encoding> possibleEncoding = enodingCreator.createEncoding(ondemand);
                 if(possibleEncoding.hasValue()) {
                     version.addManifestedAs(possibleEncoding.requireValue());
                 }
             }
         }
         return version;
     }
 }
