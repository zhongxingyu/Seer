 package org.atlasapi.remotesite.itv.whatson;
 
 import static org.mockito.Mockito.*;
 
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class ItvWhatsOnEntryProcessorTest {
     
     private final ChannelResolver channelResolver = mock(ChannelResolver.class); 
    private final ItvWhatsOnEntryExtractor extractor = new ItvWhatsOnEntryExtractor(new ItvWhatsonChannelMap(channelResolver));
     
     private String BRAND_URI = "http://itv.com/brand/1/7680";
     private String SERIES_URI = "http://itv.com/series/1/7680-02";
     private String ITEM_URI = "http://itv.com/1/7680/0029";
     private String MATCHED_URI = "http://itv.com/otheruri/huntik-secrets-and-seekers";
     
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
     
     private Content getMatchedItem() {
         Episode matchedItem = new Episode();
         matchedItem.setCanonicalUri(MATCHED_URI);
         matchedItem.setPublisher(Publisher.ITV);
         return matchedItem;
     }
     
     @Before
     public void setup() {
         when(channelResolver.fromUri(anyString())).then(new Answer<Maybe<Channel>>() {
             @Override
             public Maybe<Channel> answer(InvocationOnMock invocation) throws Throwable {
                 return Maybe.just(Channel.builder().withUri((String)invocation.getArguments()[0]).build());
             }
         });
     }
     
     
     @Test
     public void testNewItem() {
         ContentResolver contentResolver = mock(ContentResolver.class);
         ContentWriter contentWriter = mock(ContentWriter.class);
         ItvWhatsOnEntry entry = getTestItem();
         Optional<Brand> brand = extractor.toBrand(entry);
         Optional<Series> series = extractor.toSeries(entry);
         // Brand
         when(contentResolver.findByCanonicalUris(ImmutableList.of(BRAND_URI)))
             .thenReturn(ResolvedContent.builder().build());
         // Series
         when(contentResolver.findByCanonicalUris(ImmutableList.of(SERIES_URI)))
             .thenReturn(ResolvedContent.builder().build());   
         // Episode
         when(contentResolver.findByCanonicalUris(ImmutableList.of(ITEM_URI)))
             .thenReturn(ResolvedContent.builder().build());
         
         Episode episode = new Episode();
         episode.setContainer(brand.get());
         episode.setSeries(series.get());
         extractor.setCommonItemAttributes(episode, entry);
         ItvWhatsOnEntryProcessor processor = new ItvWhatsOnEntryProcessor(contentResolver, contentWriter, channelResolver);
         
         processor.createOrUpdateAtlasEntityFrom(entry);
         verify(contentResolver).findByCanonicalUris(ImmutableList.of(BRAND_URI));
         verify(contentResolver).findByCanonicalUris(ImmutableList.of(SERIES_URI));
         verify(contentResolver).findByCanonicalUris(ImmutableList.of(ITEM_URI));
         verify(contentWriter).createOrUpdate(brand.get());
         verify(contentWriter).createOrUpdate(series.get());
         verify(contentWriter).createOrUpdate(episode);
     }
     
     @Test
     public void testMergedItem() {
         ContentResolver contentResolver = mock(ContentResolver.class);
         ContentWriter contentWriter = mock(ContentWriter.class);
         ItvWhatsOnEntry entry = getTestItem();      
         // Brand
         when(contentResolver.findByCanonicalUris(ImmutableList.of(BRAND_URI))).thenReturn(ResolvedContent.builder().build());
         // Series
         when(contentResolver.findByCanonicalUris(ImmutableList.of(SERIES_URI))).thenReturn(ResolvedContent.builder().build());   
         // Episode
         when(contentResolver.findByCanonicalUris(ImmutableList.of(ITEM_URI)))
             .thenReturn(  ResolvedContent.builder().put(ITEM_URI, getMatchedItem()).build()  );
         
         ItvWhatsOnEntryProcessor processor = new ItvWhatsOnEntryProcessor(contentResolver, contentWriter, channelResolver);
         processor.createOrUpdateAtlasEntityFrom(entry);
         verify(contentWriter).createOrUpdate((Item) getMatchedItem());
     }
 
 }
