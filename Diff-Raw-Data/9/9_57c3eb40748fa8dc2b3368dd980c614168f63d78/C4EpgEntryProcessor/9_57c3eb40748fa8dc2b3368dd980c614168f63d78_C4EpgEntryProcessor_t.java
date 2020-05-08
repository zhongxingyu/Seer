 package org.atlasapi.remotesite.channel4.epg;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.atlasapi.media.entity.Publisher.C4;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;
 
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.query.content.PerPublisherCurieExpander;
 import org.atlasapi.remotesite.channel4.C4AtomApi;
 import org.atlasapi.remotesite.channel4.C4EpisodesExtractor;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class C4EpgEntryProcessor {
 
     private static final String TAG_ALIAS_BASE = "tag:www.channel4.com,2009:/programmes/";
 
     private static final String C4_PROGRAMMES_BASE = "http://www.channel4.com/programmes/";
 
     private static final Pattern BRAND_ATOM_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+).atom.*");
     private static final Pattern C40D_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+)/4od#\\d*");
     private static final Pattern EPISODE_ATOM_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+)/episode-guide/series-\\d+/episode-\\d+.atom");
 
     private static final List<Pattern> WEB_SAFE_BRAND_PATTERNS = ImmutableList.of(BRAND_ATOM_PATTERN, C40D_PATTERN, EPISODE_ATOM_PATTERN);
 
     public static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(.*); end=(.*); scheme=W3C-DTF");
 
     private final ContentWriter contentWriter;
     private final ContentResolver contentStore;
 
     private final AdapterLog log;
     
     private final C4SynthesizedItemUpdater c4SynthesizedItemUpdater;
 
     public C4EpgEntryProcessor(ContentWriter contentWriter, ContentResolver contentStore, AdapterLog log) {
         this.contentWriter = contentWriter;
         this.contentStore = contentStore;
         this.log = log;
         this.c4SynthesizedItemUpdater = new C4SynthesizedItemUpdater(contentStore, contentWriter);
     }
 
     public void process(C4EpgEntry entry, Channel channel) {
         try {
 
             String webSafeBrandName = webSafeBrandName(entry);
 
             if (webSafeBrandName == null) {
                 throw new IllegalStateException("Couldn't get web-safe brand name for " + entry.id());
             }
 
             String itemUri = uriFrom(entry, webSafeBrandName);
 
             Episode episode = (Episode) contentStore.findByCanonicalUri(itemUri);
             if (episode == null) {
                 episode = new Episode(itemUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri), Publisher.C4);
                 episode.addAlias(String.format(TAG_ALIAS_BASE+"%s/episode-guide/series-%s/episode-%s", webSafeBrandName, entry.seriesNumber(), entry.episodeNumber()));
                 
             }
             //look for a synthesized equivalent of this item and copy any broadcast/locations and remove its versions.
             updateFromPossibleSynthesized(webSafeBrandName, entry, episode);
 
             updateEpisodeDetails(episode, entry, channel);
             
             if(episode.getSeriesNumber() != null) {
                 updateSeries(C4AtomApi.seriesUriFor(webSafeBrandName, entry.seriesNumber()), webSafeBrandName, episode);
             }
             Brand brand = updateBrand(webSafeBrandName, episode, entry);
 
             contentWriter.createOrUpdate(brand, true);
 
         } catch (Exception e) {
             log.record(new AdapterLogEntry(WARN).withCause(e).withSource(getClass()).withDescription("Exception processing entry: " + entry.id()));
         }
 
     }
 
     private void updateFromPossibleSynthesized(String webSafeBrandName, C4EpgEntry entry, Episode episode) {
         c4SynthesizedItemUpdater.findAndUpdatePossibleSynthesized("c4:"+entry.slotId(), episode, C4_PROGRAMMES_BASE+webSafeBrandName);
     }
 
     private Brand updateBrand(String brandName, Episode episode, C4EpgEntry entry) {
         String brandUri = C4_PROGRAMMES_BASE + brandName;
         Brand brand = (Brand) contentStore.findByCanonicalUri(brandUri);
         if (brand != null) {
             addOrReplaceItemInPlaylist(episode, brand);
         } else {
             brand = new Brand(brandUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(brandUri), C4);
             brand.addContents(episode);
             brand.setTitle(entry.brandTitle());
             brand.addAlias(TAG_ALIAS_BASE+brandName);
         }
         return brand;
     }
 
     private Series updateSeries(String seriesUri, String brandName, Episode episode) {
         Series series = (Series) contentStore.findByCanonicalUri(seriesUri);
         if (series != null) {
             addOrReplaceItemInPlaylist(episode, series);
         } else {
             series = new Series(seriesUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(seriesUri), C4);
             series.addContents(episode);
             series.addAlias(String.format(TAG_ALIAS_BASE+"%s/episode-guide/series-%s", brandName, episode.getSeriesNumber()));
             series.withSeriesNumber(episode.getSeriesNumber());
         }
         return series;
     }
 
     @SuppressWarnings("unchecked")
     private <T extends Item> void addOrReplaceItemInPlaylist(Item item, Container<T> playlist) {
         int itemIndex = playlist.getContents().indexOf(item);
         if (itemIndex >= 0) {
             List<T> items = Lists.newArrayList(playlist.getContents());
             items.set(itemIndex, (T) item);
             playlist.setContents(items);
         } else {
             playlist.addContents((T) item);
         }
     }
 
     private Episode updateEpisodeDetails(Episode episode, C4EpgEntry entry, Channel channel) {
         if (entry.title().equals(entry.brandTitle())) {
             episode.setTitle(String.format(C4EpisodesExtractor.EPISODE_TITLE_TEMPLATE, entry.seriesNumber(), entry.episodeNumber()));
         } else {
             episode.setTitle(entry.title());
         }
 
         episode.setSeriesNumber(entry.seriesNumber());
         episode.setEpisodeNumber(entry.episodeNumber());
 
         episode.setDescription(entry.summary());
 
         updateVersion(episode, entry, channel);
 
         if (entry.media() != null && !Strings.isNullOrEmpty(entry.media().thumbnail())) {
             C4AtomApi.addImages(episode, entry.media().thumbnail());
         }
 
         episode.setIsLongForm(true);
         episode.setMediaType(MediaType.VIDEO);
         episode.setLastUpdated(entry.updated());
 
         return episode;
     }
 
     public static void updateVersion(Episode episode, C4EpgEntry entry, Channel channel) {
         Version version = Iterables.getFirst(episode.nativeVersions(), new Version());
         version.setDuration(entry.duration());
         
         version.setBroadcasts(updateBroadcasts(version.getBroadcasts(), entry, channel));
 
         // Don't add/update locations unless this is the first time we've seen the item because
         // we cannot determine the availability start without reading the /4od feed.
         if (version.getManifestedAs().isEmpty()) {
         	Encoding encoding = Iterables.getFirst(version.getManifestedAs(), new Encoding());
         	updateEncoding(version, encoding, entry);
        	if (!encoding.getAvailableAt().isEmpty() && !version.getManifestedAs().contains(encoding)) {
         		version.addManifestedAs(encoding);
         	}
         } else {
         	for (Encoding encoding : version.getManifestedAs()) {
         		for (Location location : encoding.getAvailableAt()) {
         			location.setLastUpdated(entry.updated());
         		}
         	}
         }
         
         if (!episode.getVersions().contains(version)) {
             episode.addVersion(version);
         }
     }
 
     private static Set<Broadcast> updateBroadcasts(Set<Broadcast> currentBroadcasts, C4EpgEntry entry, Channel channel) {
         Broadcast entryBroadcast = broadcastFrom(entry, channel);
         
         Set<Broadcast> broadcasts = Sets.newHashSet(entryBroadcast);
         for (Broadcast broadcast : currentBroadcasts) {
             if (!entryBroadcast.getId().equals(broadcast.getId())){
                 broadcasts.add(broadcast);
             }
         }
         
         return broadcasts;
     }
 
     private static void updateEncoding(Version version, Encoding encoding, C4EpgEntry entry) {
 
         if (entry.media() != null && entry.media().player() != null) {
             
             //try to find and update Location with same uri.
             for (Location location : encoding.getAvailableAt()) {
                 if(entry.media().player().equals(location.getUri())) {
                     updateLocation(location, entry);
                     return;
                 }
             }
             
             //otherwise create a new one.
             Location newLocation = new Location();
             updateLocation(newLocation, entry);
             encoding.addAvailableAt(newLocation);
         }
 
     }
 
     static void updateLocation(Location location, C4EpgEntry entry) {
 
         location.setUri(entry.media().player());
         location.setTransportType(TransportType.LINK);
         location.setPolicy(policyFrom(entry));
 
     }
 
     static Policy policyFrom(C4EpgEntry entry) {
         Policy policy = new Policy();
         policy.setLastUpdated(entry.updated());
 
         policy.setAvailableCountries(entry.media().availableCountries());
 
         Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(entry.available());
         if (matcher.matches()) {
         	policy.setAvailabilityStart(new DateTime(matcher.group(1)));
             policy.setAvailabilityEnd(new DateTime(matcher.group(2)));
         }
 
         return policy;
     }
 
     private static Broadcast broadcastFrom(C4EpgEntry entry, Channel channel) {
         Broadcast broadcast = new Broadcast(channel.uri(), entry.txDate(), entry.duration());
         broadcast.addAlias(entry.id());
 
         broadcast.setLastUpdated(entry.updated() != null ? entry.updated() : new DateTime(DateTimeZones.UTC));
         broadcast.setIsActivelyPublished(true);
         
         String id = entry.slotId();
         if (id != null) {
             broadcast.withId("c4:"+id);
         }
 
         return broadcast;
     }
 
     private String uriFrom(C4EpgEntry entry, String brandName) {
         checkNotNull(brandName);
         checkNotNull(entry.seriesNumber());
         checkNotNull(entry.episodeNumber());
         return String.format("%s%s/episode-guide/series-%s/episode-%s", C4_PROGRAMMES_BASE, brandName, entry.seriesNumber(), entry.episodeNumber());
     }
 
     public static String webSafeBrandName(C4EpgEntry entry) {
         for (String link : entry.links()) {
             for (Pattern pattern : WEB_SAFE_BRAND_PATTERNS) {
                 Matcher matcher = pattern.matcher(link);
                 if (matcher.matches()) {
                     return matcher.group(1);
                 }
             }
         }
         return null;
     }
 
 }
