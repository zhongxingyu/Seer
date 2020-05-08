 package org.atlasapi.remotesite.itv.whatson;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Policy.RevenueContract;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.intl.Country;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class ItvWhatsOnTranslatorTest {
     private final ItvWhatsOnEntryExtractor extractor = new ItvWhatsOnEntryExtractor();
     
     private DateTime getDateTimeFromMillis(long millis) {
         return new DateTime(DateTimeZones.UTC).withMillis(millis);
     }
     
     private ItvWhatsOnEntry getTestItem() {
         ItvWhatsOnEntry entry = new ItvWhatsOnEntry();
         entry.setChannel("CITV");
         entry.setBroadcastDate(getDateTimeFromMillis(1374644400000L));
         ItvWhatsOnEntryDuration duration = new ItvWhatsOnEntryDuration();
         duration.setTicks(18000000000L);
         duration.setDays(0);
         duration.setHours(0);
         duration.setMilliseconds(0);
         duration.setMinutes(30);
         duration.setSeconds(0);
         duration.setTotalDays(0.020833333333333332);
         duration.setTotalHours(0.5);
         duration.setTotalMilliseconds(1800000);
         duration.setTotalMinutes(30);
         duration.setTotalSeconds(1800);
         entry.setDuration(duration);
         entry.setProgrammeTitle("Huntik - Secrets and Seekers");
         entry.setEpisodeTitle("Cave of the Casterwills");
         entry.setSynopsis("Animated adventure series. Dante Vale and his band of "
                 + "Seekers explore exotic locations while on missions to save humanity"
                 + " from evil forces ");
         entry.setImageUri("http://example.com/DotCom/episode/318524/image.jpg");
         entry.setVodcrid("318524");
         entry.setAvailabilityStart(getDateTimeFromMillis(1374646200000L));
         entry.setAvailabilityEnd(getDateTimeFromMillis(1377298740000L));
         entry.setRepeat(false);
         entry.setComingSoon(true);
         entry.setProductionId("1/7680/0029#001");
         entry.setProgrammeId("1/7680");
         entry.setSeriesId("1/7680-02");
         entry.setEpisodeId("1/7680/0029");
         return entry;
     }
     
     @Test
     public void testBrandTranslation() {
         Optional<Brand> brand = extractor.toBrand(getTestItem());
         assertEquals(brand.get().getCanonicalUri(), "http://itv.com/brand/1/7680");
         assertEquals(brand.get().getTitle(), "Huntik - Secrets and Seekers");
     }
     
     @Test
     public void testSeriesTranslation() {
         Optional<Series> series = extractor.toSeries(getTestItem());
         assertEquals(series.get().getCanonicalUri(), "http://itv.com/series/1/7680-02");
     }
     
     private Episode getEpisode(ItvWhatsOnEntry entry) {
         Optional<Brand> brand = extractor.toBrand(entry);
         Optional<Series> series = extractor.toSeries(entry);
         Episode episode = new Episode();
         episode.setContainer(brand.get());
         episode.setSeries(series.get());
         extractor.setCommonItemAttributes(episode, entry);
         return episode;
     }
     
     @Test
     public void testItemTranslation() {
         Item item = getEpisode(getTestItem());
         assertEquals(item.getCanonicalUri(), "http://itv.com/1/7680/0029");
         assertEquals(item.getTitle(), "Cave of the Casterwills");
         assertEquals(item.getDescription().length(), 139);
         assertEquals(item.getImage(), "http://example.com/DotCom/episode/318524/image.jpg");
         String aliasUrl = Iterables.getOnlyElement(item.getAliasUrls());
         assertEquals(aliasUrl, "http://itv.com/vodcrid/318524");
     }
     
     @Test
     public void testEpisodeSynthesizedTranslation() {
         ItvWhatsOnEntry entry = getTestItem();
         entry.setEpisodeId("");        
         Episode episode = getEpisode(entry);
         assertEquals(episode.getCanonicalUri(), "http://itv.com/synthesized/1/7680/0029#001");
     }
     
     @Test
     public void testVersion() {
         Episode episode = getEpisode(getTestItem());
         Version version = Iterables.getOnlyElement(episode.getVersions());
         assertEquals(version.getCanonicalUri(), "http://itv.com/version/1/7680/0029#001");
         assertEquals(version.getDuration().intValue(), 1800);
     }
     
     @Test
     public void testBroadcast() {
         Episode episode = getEpisode(getTestItem());
         DateTime expectedTransmissionStart = getDateTimeFromMillis(1374644400000L);
         DateTime expectedTransmissionEnd = expectedTransmissionStart.plusSeconds(1800);
         Version version = Iterables.getOnlyElement(episode.getVersions());
         Broadcast broadcast = Iterables.getOnlyElement(version.getBroadcasts());
         assertEquals(broadcast.getBroadcastOn(), "http://www.itv.com/channels/citv");
         assertEquals(broadcast.getTransmissionTime(), expectedTransmissionStart);
         assertEquals(broadcast.getTransmissionEndTime(), expectedTransmissionEnd);
         assertEquals(broadcast.getBroadcastDuration().intValue(), 1800);
         assertFalse(broadcast.getRepeat().booleanValue());
     }
     
     @Test
     public void testLocation() {
         Episode episode = getEpisode(getTestItem());
         Version version = Iterables.getOnlyElement(episode.getVersions());
         Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
         Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
        assertEquals("https://www.itv.com/itvplayer/video/?filter=1%2F7680%2F0029%23001", location.getUri());
        assertEquals(TransportType.LINK, location.getTransportType());
         Policy policy = location.getPolicy();
         checkPolicy(policy);
     }
     
     private void checkPolicy(Policy policy) {
         assertEquals(policy.getRevenueContract(), RevenueContract.FREE_TO_VIEW);
         assertEquals(policy.getAvailableCountries().size(), 1);
         assertEquals(policy.getAvailabilityStart(), getDateTimeFromMillis(1374646200000L));
         assertEquals(policy.getAvailabilityEnd(), getDateTimeFromMillis(1377298740000L));
         Country country = Iterables.getOnlyElement(policy.getAvailableCountries());
         assertEquals(country, Countries.GB);
     }
 }
