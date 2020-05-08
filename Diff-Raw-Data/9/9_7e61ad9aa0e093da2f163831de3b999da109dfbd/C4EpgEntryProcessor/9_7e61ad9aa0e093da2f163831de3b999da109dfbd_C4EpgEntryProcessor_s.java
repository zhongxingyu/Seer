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
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.query.content.PerPublisherCurieExpander;
 import org.atlasapi.remotesite.channel4.C4AtomApi;
 import org.atlasapi.remotesite.channel4.C4EpisodesExtractor;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 public class C4EpgEntryProcessor {
 
     private static final String TAG_ALIAS_BASE = "tag:www.channel4.com,2009:/programmes/";
 
     private static final String C4_PROGRAMMES_BASE = "http://www.channel4.com/programmes/";
 
     private static final Pattern BRAND_ATOM_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+).atom.*");
     private static final Pattern C40D_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+)/4od#\\d*");
     private static final Pattern EPISODE_ATOM_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+)/episode-guide/series-\\d+/episode-\\d+.atom");
 
     private static final List<Pattern> WEB_SAFE_BRAND_PATTERNS = ImmutableList.of(BRAND_ATOM_PATTERN, C40D_PATTERN, EPISODE_ATOM_PATTERN);
 
     private static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(.*); end=(.*); scheme=W3C-DTF");
 
     private final ContentWriter contentWriter;
     private final ContentResolver contentStore;
 
     private final AdapterLog log;
 
     public C4EpgEntryProcessor(ContentWriter contentWriter, ContentResolver contentStore, AdapterLog log) {
         this.contentWriter = contentWriter;
         this.contentStore = contentStore;
         this.log = log;
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
 
 
         if (entry.media() != null && entry.media().thumbnail() != null) {
             C4AtomApi.addImages(episode, entry.media().thumbnail());
         }
 
         episode.setIsLongForm(true);
         episode.setMediaType(MediaType.VIDEO);
         episode.setSpecialization(Specialization.TV);
         episode.setLastUpdated(entry.updated());
 
         return episode;
     }
 
     private void updateVersion(Episode episode, C4EpgEntry entry, Channel channel) {
         Version version = Iterables.getFirst(episode.nativeVersions(), new Version());
         version.setDuration(entry.duration());
         
         updateBroadcasts(version.getBroadcasts(), entry, channel);
 
         Encoding encoding = Iterables.getFirst(version.getManifestedAs(), new Encoding());
 
         updateEncoding(version, encoding, entry);
 
         if (!version.getManifestedAs().contains(encoding)) {
             version.addManifestedAs(encoding);
         }
 
         if (!episode.getVersions().contains(version)) {
             episode.addVersion(version);
         }
     }
 
     private void updateBroadcasts(Set<Broadcast> broadcasts, C4EpgEntry entry, Channel channel) {
         for (Broadcast broadcast : broadcasts) {
             if (broadcast.getId() != null && broadcast.getId().equals(entry.slotId())){
                 broadcasts.remove(broadcast);
                 break;
             }
         }
         broadcasts.add(broadcastFrom(entry, channel));
     }
 
     private void updateEncoding(Version version, Encoding encoding, C4EpgEntry entry) {
 
         if (entry.media() != null && entry.media().player() != null) {
             
             //try to find and update Location with same uri.
             for (Location location : encoding.getAvailableAt()) {
                 if(entry.media().player().equals(location.getUri())) {
                     updateLocation(location, entry);
                     return;
                 }
             }
             
             //other wise create a new one.
             Location newLocation = new Location();
             updateLocation(newLocation, entry);
             encoding.addAvailableAt(newLocation);
         }
 
     }
 
     private void updateLocation(Location location, C4EpgEntry entry) {
 
         location.setUri(entry.media().player());
         location.setTransportType(TransportType.LINK);
         location.setPolicy(policyFrom(entry));
 
     }
 
     private Policy policyFrom(C4EpgEntry entry) {
         Policy policy = new Policy();
 
         policy.setAvailableCountries(entry.media().availableCountries());
         policy.setAvailabilityStart(entry.txDate());
 
         Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(entry.available());
         if (matcher.matches()) {
             policy.setAvailabilityEnd(new DateTime(matcher.group(2)));
         }
 
         return policy;
     }
 
     private Broadcast broadcastFrom(C4EpgEntry entry, Channel channel) {
         Broadcast broadcast = new Broadcast(channel.uri(), entry.txDate(), entry.duration());
         broadcast.addAlias(entry.id());
 
         broadcast.setLastUpdated(entry.updated());
         
         String id = entry.slotId();
         if (id != null) {
            broadcast.withId("c4:" + id);
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
