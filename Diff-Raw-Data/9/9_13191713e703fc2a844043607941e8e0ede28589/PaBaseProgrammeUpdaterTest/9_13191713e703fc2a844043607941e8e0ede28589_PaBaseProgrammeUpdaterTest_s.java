 package org.atlasapi.remotesite.pa;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.LookupResolvingContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoContentWriter;
 import org.atlasapi.persistence.content.people.DummyItemsPeopleWriter;
 import org.atlasapi.persistence.content.schedule.mongo.MongoScheduleStore;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.SystemOutAdapterLog;
 import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
 import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
 import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
 import org.atlasapi.remotesite.pa.persistence.PaScheduleVersionStore;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.Interval;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.io.Resources;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.TimeMachine;
 
 @RunWith(JMock.class)
 public class PaBaseProgrammeUpdaterTest extends TestCase {
 
     private Mockery context = new Mockery();
     
     private PaProgDataProcessor programmeProcessor;
 
     private TimeMachine clock = new TimeMachine();
     private AdapterLog log = new SystemOutAdapterLog();
     private ContentResolver resolver;
     private ContentWriter contentWriter;
 	private MongoScheduleStore scheduleWriter;
 
 	private ChannelResolver channelResolver;
 
     @Override
     @Before
     public void setUp() throws Exception {
         super.setUp();
         DatabasedMongo db = MongoTestHelper.anEmptyTestDatabase();
         MongoLookupEntryStore lookupStore = new MongoLookupEntryStore(db);
         resolver = new LookupResolvingContentResolver(new MongoContentResolver(db), lookupStore);
         
         channelResolver = new DummyChannelResolver();
         contentWriter = new MongoContentWriter(db, lookupStore, clock);
         programmeProcessor = new PaProgrammeProcessor(contentWriter, resolver, channelResolver, new DummyItemsPeopleWriter(), log);
         scheduleWriter = new MongoScheduleStore(db, resolver, channelResolver);
     }
 
     @Test
     public void testShouldCreateCorrectPaData() throws Exception {
         final PaScheduleVersionStore scheduleVersionStore = context.mock(PaScheduleVersionStore.class);
         final Channel channel = channelResolver.fromUri("http://www.bbc.co.uk/bbcone").requireValue();
         final LocalDate scheduleDay = new LocalDate(2011, DateTimeConstants.JANUARY, 15);
         context.checking(new Expectations() {{
             oneOf(scheduleVersionStore).get(channel, scheduleDay);
             will(returnValue(Optional.<Long>absent()));
             oneOf(scheduleVersionStore).store(channel, scheduleDay, 1);
             oneOf(scheduleVersionStore).get(channel, scheduleDay);
             will(returnValue(Optional.<Long>of(1L)));
             oneOf(scheduleVersionStore).store(channel, scheduleDay, 201202251115L);
             oneOf(scheduleVersionStore).get(channel, scheduleDay);
             will(returnValue(Optional.<Long>of(201202251115L)));
             oneOf(scheduleVersionStore).get(channel, scheduleDay);
             will(returnValue(Optional.<Long>of(201202251115L)));
         }});
         
         TestPaProgrammeUpdater updater = new TestPaProgrammeUpdater(programmeProcessor, channelResolver, log, scheduleWriter, ImmutableList.of(new File(Resources.getResource("20110115_tvdata.xml").getFile()), new File(Resources.getResource("201202251115_20110115_tvdata.xml").getFile())), null, scheduleVersionStore);
         updater.run();
         Identified content = null;
 
         content = resolver.findByCanonicalUris(ImmutableList.of("http://pressassociation.com/brands/122139")).get("http://pressassociation.com/brands/122139").requireValue();
 
         assertNotNull(content);
         assertTrue(content instanceof Brand);
         Brand brand = (Brand) content;
         assertFalse(brand.getChildRefs().isEmpty());
         assertNotNull(brand.getImage());
 
         Item item = loadItemAtPosition(brand, 0);
         assertTrue(item.getCanonicalUri().contains("episodes"));
         assertNotNull(item.getImage());
         assertFalse(item.getVersions().isEmpty());
         assertEquals(MediaType.VIDEO, item.getMediaType());
         assertEquals(Specialization.TV, item.getSpecialization());
 
         assertEquals(17, item.people().size());
         assertEquals(14, item.actors().size());
 
         Version version = item.getVersions().iterator().next();
         assertFalse(version.getBroadcasts().isEmpty());
 
         Broadcast broadcast = version.getBroadcasts().iterator().next();
         assertEquals("pa:71118472", broadcast.getSourceId());
 
         updater.run();
         Thread.sleep(1000);
 
         content = resolver.findByCanonicalUris(ImmutableList.of("http://pressassociation.com/brands/122139")).get("http://pressassociation.com/brands/122139").requireValue();
         assertNotNull(content);
         assertTrue(content instanceof Brand);
         brand = (Brand) content;
         assertFalse(brand.getChildRefs().isEmpty());
 
         item =  loadItemAtPosition(brand, 0);
         assertFalse(item.getVersions().isEmpty());
 
         version = item.getVersions().iterator().next();
         assertFalse(version.getBroadcasts().isEmpty());
 
         broadcast = version.getBroadcasts().iterator().next();
         assertEquals("pa:71118472", broadcast.getSourceId());
 
 //        // Test people get created
 //        for (CrewMember crewMember : item.people()) {
 //            content = store.findByCanonicalUri(crewMember.getCanonicalUri());
 //            assertTrue(content instanceof Person);
 //            assertEquals(crewMember.name(), ((Person) content).name());
 //        }
     }
     
     @Test
     public void testBroadcastsTrimmerWindowNoTimesInFile() {
        final PaScheduleVersionStore scheduleVersionStore = context.mock(PaScheduleVersionStore.class);
         final BroadcastTrimmer trimmer = context.mock(BroadcastTrimmer.class);        
         final Interval firstFileInterval = new Interval(new DateTime(2011, DateTimeConstants.JANUARY, 15, 6, 0, 0, 0, DateTimeZones.LONDON), new DateTime(2011, DateTimeConstants.JANUARY, 16, 6, 0, 0, 0, DateTimeZones.LONDON));
          
         context.checking(new Expectations() {{
             oneOf (trimmer).trimBroadcasts(firstFileInterval, channelResolver.fromUri("http://www.bbc.co.uk/bbcone").requireValue(), ImmutableMap.of("pa:71118471", "http://pressassociation.com/episodes/1424497"));
         }});
         
        TestPaProgrammeUpdater updater = new TestPaProgrammeUpdater(programmeProcessor, channelResolver, log, scheduleWriter, ImmutableList.of(new File(Resources.getResource("20110115_tvdata.xml").getFile())), trimmer, scheduleVersionStore);
         updater.run();
     }
 
     @Test
     public void testBroadcastTrimmerWindowTimesInFile() {
        final PaScheduleVersionStore scheduleVersionStore = context.mock(PaScheduleVersionStore.class);
         final BroadcastTrimmer trimmer = context.mock(BroadcastTrimmer.class);
         final Interval fileInterval = new Interval(new DateTime(2011, DateTimeConstants.JANUARY, 15, 21, 40, 0, 0, DateTimeZones.LONDON), new DateTime(2011, DateTimeConstants.JANUARY, 15, 23, 30, 0, 0, DateTimeZones.LONDON));  
         
         context.checking(new Expectations() {{
             oneOf (trimmer).trimBroadcasts(fileInterval, channelResolver.fromUri("http://www.bbc.co.uk/bbcone").requireValue(), ImmutableMap.of("pa:71118472", "http://pressassociation.com/episodes/1424497"));
         }});
         
        TestPaProgrammeUpdater updater = new TestPaProgrammeUpdater(programmeProcessor, channelResolver, log, scheduleWriter, ImmutableList.of(new File(Resources.getResource("201202251115_20110115_tvdata.xml").getFile())), trimmer, scheduleVersionStore);
         updater.run();
     }    
 
     private Item loadItemAtPosition(Brand brand, int index) {
         return (Item) resolver.findByCanonicalUris(ImmutableList.of(brand.getChildRefs().get(index).getUri())).getFirstValue().requireValue();
     }
 
     static class TestPaProgrammeUpdater extends PaBaseProgrammeUpdater {
 
         private List<File> files;
 
         public TestPaProgrammeUpdater(PaProgDataProcessor processor, ChannelResolver channelResolver, AdapterLog log, MongoScheduleStore scheduleWriter, List<File> files, BroadcastTrimmer trimmer, PaScheduleVersionStore scheduleVersionStore) {
             super(MoreExecutors.sameThreadExecutor(), new PaChannelProcessor(processor, trimmer, scheduleWriter, scheduleVersionStore), new DefaultPaProgrammeDataStore("/data/pa", null), channelResolver, Optional.fromNullable(scheduleVersionStore));
             this.files = files;
         }
 
         @Override
         public void runTask() {
             this.processFiles(files);
         }
     }
     
     static class DummyChannelResolver implements ChannelResolver {
 
 		@Override
 		public Maybe<Channel> fromKey(String key) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Maybe<Channel> fromId(long id) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Maybe<Channel> fromUri(String uri) {
 			if("http://www.bbc.co.uk/bbcone".equals(uri)) {
 				return Maybe.just(new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", MediaType.VIDEO, "http://www.bbc.co.uk/bbcone"));
 			}
 			return Maybe.just(new Channel());
 		}
 
 		@Override
 		public Iterable<Channel> all() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Map<String, Channel> forAliases(String aliasPrefix) {
 			return ImmutableMap.of(
 					"http://pressassociation.com/channels/4", new Channel(Publisher.METABROADCAST, "BBC One", "bbcone", MediaType.VIDEO, "http://www.bbc.co.uk/bbcone"));
 		}
 
         @Override
         public Iterable<Channel> forIds(Iterable<Long> ids) {
             throw new UnsupportedOperationException();
         }
     	
     }
    
 }
