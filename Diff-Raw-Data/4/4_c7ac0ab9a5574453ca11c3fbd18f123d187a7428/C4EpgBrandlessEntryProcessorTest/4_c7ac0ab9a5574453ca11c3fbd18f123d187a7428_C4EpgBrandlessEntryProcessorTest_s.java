 package org.atlasapi.remotesite.channel4.epg;
 
 import static com.google.common.collect.Iterables.getOnlyElement;
 import static com.metabroadcast.common.time.DateTimeZones.UTC;
 import static org.atlasapi.media.entity.Channel.CHANNEL_FOUR;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.StubContentResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.SystemOutAdapterLog;
 import org.atlasapi.remotesite.channel4.RecordingContentWriter;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 public class C4EpgBrandlessEntryProcessorTest extends TestCase {
 
     private final AdapterLog log = new SystemOutAdapterLog();
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public void testProcessNewItem() {
     	ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
     	
     	final RecordingContentWriter writer = new RecordingContentWriter();
     	
         C4EpgBrandlessEntryProcessor processor = new C4EpgBrandlessEntryProcessor(writer, resolver, log);
         processor.process(buildEntry().withLinks(ImmutableList.<String>of()), CHANNEL_FOUR);
         
         Brand brand = Iterables.getOnlyElement(writer.updatedBrands);
         
         assertThat(brand.getCanonicalUri(), is(equalTo("http://www.channel4.com/programmes/robin-williams-weapons-of-self")));
         assertThat(brand.getCurie(), is(equalTo("c4:robin-williams-weapons-of-self")));
         
         assertThat(writer.updatedSeries.size(), is(equalTo(0)));
 
         List<Episode> contents = (List) writer.updatedItems;
         assertThat(contents.size(), is(equalTo(1)));
         
         Episode episode = getOnlyElement(contents);
         assertThat(episode.getCanonicalUri(), is(equalTo("http://www.channel4.com/programmes/synthesized/robin-williams-weapons-of-self/606")));
         assertThat(episode.getTitle(), is(equalTo("Robin Williams: Weapons of Self...")));
         assertThat(episode.getEpisodeNumber(), is(equalTo(null)));
         assertThat(episode.getSeriesNumber(), is(equalTo(null)));
         
         Version version = getOnlyElement(episode.getVersions());
         assertThat(version.getDuration().longValue(), is(equalTo(Duration.standardMinutes(110).getStandardSeconds())));
         
         Broadcast broadcast = getOnlyElement(version.getBroadcasts());
         assertThat(broadcast.getId(), is(equalTo("c4:606")));
         assertThat(broadcast.getAliases().size(), is(1));
         assertThat(broadcast.getAliases(), hasItem("tag:www.channel4.com,2009:slot/C4606"));
         assertThat(broadcast.getTransmissionTime(), is(equalTo(new DateTime("2011-01-08T00:05:00.000Z"))));
         assertThat(broadcast.getTransmissionEndTime(), is(equalTo(new DateTime("2011-01-08T00:05:00.000Z").plus(Duration.standardMinutes(110)))));
         
         assertEquals(ImmutableSet.of(), version.getManifestedAs());
     }
     
   
 
     private C4EpgEntry buildEntry() {
         return new C4EpgEntry("tag:www.channel4.com,2009:slot/606")
             .withTitle("Robin Williams: Weapons of Self...")
             .withUpdated(new DateTime("2011-02-03T15:43:00.855Z"))
             .withSummary("...Destruction: Academy Award-winning actor, writer and comedian Robin Williams performs stand-up material at his sold-out US tour.")
             .withTxDate(new DateTime("2011-01-08T00:05:00.000Z"))
             .withTxChannel("C4")
             .withSubtitles(true)
             .withAudioDescription(false)
             .withDuration(Duration.standardMinutes(110));
     }
     
     public void testFindsRealItemWithBroadcastWithSameId() {
     	
     	final RecordingContentWriter writer = new RecordingContentWriter();
     	
     	ContentResolver resolver = new StubContentResolver().respondTo(realBrand()).respondTo(episode);
         
         C4EpgBrandlessEntryProcessor processor = new C4EpgBrandlessEntryProcessor(writer, resolver, log);
         
         processor.process(buildEntry().withLinks(ImmutableList.<String>of("http://api.channel4.com/programmes/gilmore-girls.atom")), CHANNEL_FOUR);
         
         Episode ep = Iterables.getOnlyElement(Iterables.filter(writer.updatedItems, Episode.class));
         Version v = Iterables.getOnlyElement(ep.getVersions());
         assertEquals(2, v.getBroadcasts().size());
         
        Broadcast b0 = Iterables.get(v.getBroadcasts(), 0);
        Broadcast b1 = Iterables.get(v.getBroadcasts(), 1);
         
         assertEquals("c4:616", b1.getId());
         assertEquals(new DateTime(0, UTC), b1.getTransmissionTime());
 
         assertEquals(new DateTime("2011-01-08T00:05:00.000Z"), b0.getTransmissionTime());
     }
     
     private final Episode episode = new Episode("ep1", "ep1", Publisher.C4);
     
     private Brand realBrand() {
         Brand brand = new Brand("http://www.channel4.com/programmes/gilmore-girls", "c4:gilmore-girls", Publisher.C4);
         episode.setContainer(brand);
         brand.setChildRefs(ImmutableList.of(episode.childRef()));
         Version version = new Version();
         Broadcast one = new Broadcast("telly1", new DateTime(0, UTC), new DateTime(0, UTC)).withId("c4:616");
         Broadcast two = new Broadcast("telly2", new DateTime(0, UTC), new DateTime(0, UTC)).withId("c4:606");
         version.setBroadcasts(ImmutableSet.of(one, two));
         episode.addVersion(version);
         return brand;
     }
 }
