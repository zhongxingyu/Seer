 package org.atlasapi.remotesite.channel4.epg;
 
 import static com.google.common.collect.Iterables.getOnlyElement;
 import static com.metabroadcast.common.time.DateTimeZones.UTC;
 import static org.atlasapi.media.entity.Channel.CHANNEL_FOUR;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.endsWith;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.SystemOutAdapterLog;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 public class C4EpgBrandlessEntryProcessorTest extends TestCase {
 
     private final AdapterLog log = new SystemOutAdapterLog();
     
     private final Mockery context = new Mockery();
     private final ContentResolver resolver = context.mock(ContentResolver.class);
     private final ContentWriter writer = context.mock(ContentWriter.class);
     
     public void testProcessNewItem() {
         
         context.checking(new Expectations(){{
             allowing(resolver).findByCanonicalUri(with(any(String.class))); will(returnValue(null));
             one(writer).createOrUpdate(with(synthesizedBrand()), with(true));
         }});
         
         C4EpgBrandlessEntryProcessor processor = new C4EpgBrandlessEntryProcessor(writer, resolver, log);
         
         processor.process(buildEntry().withLinks(ImmutableList.<String>of()), CHANNEL_FOUR);
         
     }
     
     private Matcher<Brand> synthesizedBrand() {
         return new TypeSafeMatcher<Brand>() {
 
             @Override
             public void describeTo(Description desc) {
                 desc.appendText("synthesized brand");
             }
 
             @Override
             public boolean matchesSafely(Brand brand) {
                 assertThat(brand.getCanonicalUri(), is(equalTo("http://www.channel4.com/programmes/robin-williams-weapons-of-self")));
                 assertThat(brand.getCurie(), is(equalTo("c4:robin-williams-weapons-of-self")));
                 
                 assertThat(brand.getSeries().size(), is(equalTo(0)));
 
                 ImmutableList<Episode> contents = brand.getContents();
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
                 assertThat(broadcast.getTransmissionTime(), is(equalTo(new DateTime("2011-01-08T00:05:00.000Z"))));
                 assertThat(broadcast.getTransmissionEndTime(), is(equalTo(new DateTime("2011-01-08T00:05:00.000Z").plus(Duration.standardMinutes(110)))));
                 
                assertThat(version.getManifestedAs().isEmpty(), is(true));
                 return true;
             }
         };
     }
 
     private C4EpgEntry buildEntry() {
         return new C4EpgEntry("tag:int.channel4.com,2009:slot/606")
             .withTitle("Robin Williams: Weapons of Self...")
             .withUpdated(new DateTime("2011-02-03T15:43:00.855Z"))
             .withSummary("...Destruction: Academy Award-winning actor, writer and comedian Robin Williams performs stand-up material at his sold-out US tour.")
             .withTxDate(new DateTime("2011-01-08T00:05:00.000Z"))
             .withTxChannel("C4")
             .withSubtitles(true)
             .withAudioDescription(false)
             .withDuration(Duration.standardMinutes(110));
     }
     
     @SuppressWarnings("unchecked")
     public void testFindsRealItemWithBroadcastWithSameId() {
         
         context.checking(new Expectations(){{
             allowing(resolver).findByCanonicalUri(with(allOf(endsWith("gilmore-girls"),not(containsString("synthesized"))))); will(returnValue(realBrand()));
             one(writer).createOrUpdate(with(updatedRealBrand()), with(true));
         }});
         
         C4EpgBrandlessEntryProcessor processor = new C4EpgBrandlessEntryProcessor(writer, resolver, log);
         
         processor.process(buildEntry().withLinks(ImmutableList.<String>of("http://api.channel4.com/programmes/gilmore-girls.atom")), CHANNEL_FOUR);
         
     }
     
     private Brand realBrand() {
         Brand brand = new Brand();
         Episode episode = new Episode();
         Version version = new Version();
         Broadcast one = new Broadcast("telly1", new DateTime(0, UTC), new DateTime(0, UTC)).withId("c4:616");
         Broadcast two = new Broadcast("telly2", new DateTime(0, UTC), new DateTime(0, UTC)).withId("c4:606");
         version.setBroadcasts(ImmutableSet.of(one, two));
         episode.addVersion(version);
         brand.addContents(episode);
         return brand;
     }
     
     private Matcher<Brand> updatedRealBrand() {
         return new TypeSafeMatcher<Brand>() {
             @Override
             public void describeTo(Description desc) {
                 desc.appendText("brand with updated broadcasts for episode");
             }
 
             @Override
             public boolean matchesSafely(Brand brand) {
                 
                 Episode ep = Iterables.getOnlyElement(brand.getContents());
                 Version v = Iterables.getOnlyElement(ep.getVersions());
                 if(v.getBroadcasts().size() != 2) {
                     return false;
                 }
                 for (Broadcast b : v.getBroadcasts()) {
                     if(b.getId().equals("c4:616")) {
                         if(!b.getTransmissionTime().equals(new DateTime(0, UTC))) {
                             return false;
                         }
                     } else {
                         if(!b.getTransmissionTime().equals(new DateTime("2011-01-08T00:05:00.000Z"))) {
                             return false;
                         }
                     }
                 }
                 return true;
             }
         };
     }
 }
