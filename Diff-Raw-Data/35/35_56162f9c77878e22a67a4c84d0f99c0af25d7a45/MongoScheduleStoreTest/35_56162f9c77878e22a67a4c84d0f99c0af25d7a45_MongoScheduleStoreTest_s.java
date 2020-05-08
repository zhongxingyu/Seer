 package org.atlasapi.persistence.content.schedule.mongo;
 
import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.media.channel.Channel;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.Schedule.ScheduleChannel;
 import org.atlasapi.media.entity.ScheduleEntry;
 import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.output.Annotation;
 import org.atlasapi.persistence.channels.DummyChannelResolver;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.EquivalentContent;
 import org.atlasapi.persistence.content.EquivalentContentResolver;
 import org.atlasapi.persistence.testing.StubContentResolver;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 
 @RunWith(MockitoJUnitRunner.class)
 public class MongoScheduleStoreTest {
 
     private MongoScheduleStore store;
     
     private final Channel BBC_ONE = new Channel(Publisher.BBC, "BBC One", "bbcone", false, MediaType.VIDEO, "http://www.bbc.co.uk/bbcone");
     private final Channel BBC_TWO = new Channel(Publisher.BBC, "BBC Two", "bbctwo", false, MediaType.VIDEO, "http://www.bbc.co.uk/bbctwo");
     private final Channel Channel_4_HD = new Channel(Publisher.C4, "Channel 4", "channel4", false, MediaType.VIDEO, "http://www.channel4.com");
     private final Channel AL_JAZEERA_ENGLISH = new Channel(Publisher.C4, "Al Jazeera English", "aljazeera", false, MediaType.VIDEO, "http://www.aljazeera.com/");
     
     private final DateTime now = new DateTime(DateTimeZones.UTC);
     private final Broadcast broadcast1 = new Broadcast(BBC_ONE.uri(), now.minusHours(4), now.minusHours(2));
     private final Broadcast broadcast2 = new Broadcast(BBC_TWO.uri(), now.minusHours(4), now.minusHours(1));
     private final Broadcast broadcast3 = new Broadcast(BBC_ONE.uri(), now.minusHours(2), now.minusHours(1));
     private final Broadcast broadcast4 = new Broadcast(BBC_TWO.uri(), now.minusHours(1), now);
 
     private final Broadcast veryOldBroadcast = new Broadcast(BBC_TWO.uri(), now.minusYears(1).minusHours(1), now.minusYears(1));
     
     private final Version version1 = new Version();
     private final Version version2 = new Version();
     private final Version version3 = new Version();
     
     private final Item item1 = new Item("item1", "item1", Publisher.BBC);
     private final Item item2 = new Episode("item2", "item2", Publisher.BBC);
     private final Item item3 = new Episode("item3", "item3", Publisher.BBC);
     private final Brand brand1 = new Brand("brand1", "brand1", Publisher.BBC);
     
     private final Location availableLocation = new Location();
     private final Location unavailableLocation = new Location();
     private final Encoding encoding = new Encoding();
     private long when = System.currentTimeMillis();
     
     private DatabasedMongo database;
     
     private StubContentResolver contentResolver;
     
     @Before
     public void setUp() throws Exception {
         database = MongoTestHelper.anEmptyTestDatabase();
         
         availableLocation.setAvailable(true);
         unavailableLocation.setAvailable(false);
         encoding.addAvailableAt(availableLocation);
         encoding.addAvailableAt(unavailableLocation);
         
         version1.addBroadcast(broadcast1);
         version1.addManifestedAs(encoding);
         version2.addBroadcast(broadcast2);
         version2.addManifestedAs(encoding);
         version3.addBroadcast(broadcast3);
         version3.addManifestedAs(encoding);
         version3.addBroadcast(broadcast4);
         
         item1.addVersion(version1);
         item1.setContainer(brand1);
         item2.addVersion(version2);
         item2.addVersion(version3);
         
         when = System.currentTimeMillis();
 
         contentResolver = new StubContentResolver().respondTo(item1).respondTo(item2).respondTo(item3);
         
         ChannelResolver channelResolver = new DummyChannelResolver(ImmutableList.of(BBC_ONE, BBC_TWO, Channel_4_HD, AL_JAZEERA_ENGLISH));
         store = new MongoScheduleStore(database, contentResolver, channelResolver, mock(EquivalentContentResolver.class));
     }
     
     @After
     public void tearDown() {
         System.out.println("Completed in "+(System.currentTimeMillis()-when)+" millis");
     }
     
     @Test
     public void shouldSaveItemsAndRetrieveSchedule() throws Exception {
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(BBC_ONE, BBC_TWO), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
         assertSchedule(schedule);
     }
 
     @Test
     public void testShouldIgnoreBroadcastsOverAYearOld() throws Exception {
   
     	Item item = itemWithBroadcast("id", new Broadcast(BBC_ONE.uri(), now.withHourOfDay(1).withMinuteOfHour(10), now.withHourOfDay(1).withMinuteOfHour(20)));
     	store.writeScheduleFrom(item);
 
         assertEquals(1, Iterables.size(database.collection("schedule").find()));
     }
 
 	private Item itemWithBroadcast(String id, Broadcast broadcast) {
 		Version version = new Version();
     	version.addBroadcast(veryOldBroadcast);
     	version.addBroadcast(broadcast);
     	
     	Item item = new Item(id, "", Publisher.BBC);
     	
 		item.addVersion(version);
 		return item;
 	}
     
     @Test
     public void shouldSaveItemsAndRetrieveScheduleWithLoadsOfPublishers() throws Exception {
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(BBC_ONE, BBC_TWO), ImmutableSet.of(Publisher.BBC, Publisher.C4, Publisher.ITV), Optional.<ApplicationConfiguration>absent());
         assertSchedule(schedule);
     }
     
     @Test
     public void shouldReplaceScheduleBlock() throws Exception {
     	DateTime broadcast1Start = now.withMinuteOfHour(20);
     	DateTime broadcast1End = broadcast1Start.plusMinutes(30);
     	Broadcast b1 = new Broadcast(Channel_4_HD.uri(), broadcast1Start, broadcast1End);
     	
     	DateTime broadcast2End = broadcast1End.plusMinutes(45);
     	Broadcast b2 = new Broadcast(Channel_4_HD.uri(), broadcast1End, broadcast2End);
     	
     	DateTime broadcast3End = broadcast2End.plusMinutes(60);
     	Broadcast b3 = new Broadcast(Channel_4_HD.uri(), broadcast2End, broadcast3End);
     	  
     	Version v1 = new Version();
     	Version v2 = new Version();
     	Version v3 = new Version();
     	
     	v1.addBroadcast(b1);
     	v2.addBroadcast(b2);
     	v3.addBroadcast(b3);
     	
     	item1.addVersion(v1);
     	item2.addVersion(v2);
     	item3.addVersion(v3);
     	
     	List<ItemRefAndBroadcast> itemsAndBroadcasts = Lists.newArrayList();
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item1, b1));
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item2, b2));
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item3, b3));
     												  
     	store.replaceScheduleBlock(Publisher.BBC, Channel_4_HD, itemsAndBroadcasts);
     	Schedule schedule = store.schedule(broadcast1Start, broadcast3End.plusMinutes(10), ImmutableSet.of(Channel_4_HD), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
     	
     	assertEquals(1, schedule.scheduleChannels().size());
         ScheduleChannel channel = Iterables.getOnlyElement(schedule.scheduleChannels());
         
         assertEquals(3, channel.items().size());
         
         assertEquals(item1.getCanonicalUri(), channel.items().get(0).getCanonicalUri());
         assertEquals(item2.getCanonicalUri(), channel.items().get(1).getCanonicalUri());
         assertEquals(item3.getCanonicalUri(), channel.items().get(2).getCanonicalUri());
         
         // Now replace the item being broadcast in the b2 slot
         
         Item item4 = itemWithBroadcast("item4", b2);
         contentResolver = contentResolver.respondTo(item4);
         List<ItemRefAndBroadcast> replacementItemAndBcast = Lists.newArrayList();
         replacementItemAndBcast.add(new ItemRefAndBroadcast(item4, b2));
         
     	store.replaceScheduleBlock(Publisher.BBC, Channel_4_HD, replacementItemAndBcast);
         Schedule updatedSchedule = store.schedule(broadcast1Start, broadcast3End.plusMinutes(10), 
                 ImmutableSet.of(Channel_4_HD), ImmutableSet.of(Publisher.BBC), 
                 Optional.<ApplicationConfiguration>absent());
     	assertEquals(1, updatedSchedule.scheduleChannels().size());
         ScheduleChannel replacementChannel = Iterables.getOnlyElement(updatedSchedule.scheduleChannels());
         
         assertEquals(3, replacementChannel.items().size());
         
         assertEquals(item1.getCanonicalUri(), replacementChannel.items().get(0).getCanonicalUri());
         assertEquals(item4.getCanonicalUri(), replacementChannel.items().get(1).getCanonicalUri());
         assertEquals(item3.getCanonicalUri(), replacementChannel.items().get(2).getCanonicalUri());
           	
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void overlappingScheduleShouldError() throws Exception {
     	Broadcast wrongBroadcast = new Broadcast(BBC_ONE.uri(), now.minusHours(3), now.minusHours(1));
     	version3.setBroadcasts(ImmutableSet.of(wrongBroadcast));
     	List<ItemRefAndBroadcast> itemsAndBroadcasts = Lists.newArrayList();
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item1, broadcast1));
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item3, wrongBroadcast));
     	store.replaceScheduleBlock(Publisher.BBC, BBC_ONE, itemsAndBroadcasts);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void multipleChannelsInScheduleReplaceShouldError() throws Exception {
     
     	List<ItemRefAndBroadcast> itemsAndBroadcasts = Lists.newArrayList();
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item1, broadcast1));
     	itemsAndBroadcasts.add(new ItemRefAndBroadcast(item2, broadcast2));
     	store.replaceScheduleBlock(Publisher.BBC, BBC_ONE, itemsAndBroadcasts);
     }
     
     @Test
     public void wrongChannelShouldBeFiltered() throws Exception {
         Broadcast broadcast = new Broadcast(AL_JAZEERA_ENGLISH.uri(), now.minusHours(2), now.minusHours(3));
         version1.addBroadcast(broadcast);
         
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(BBC_ONE, BBC_TWO), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
         assertSchedule(schedule);
     }
     
     @Test
     public void wrongIntervalShouldBeFiltered() throws Exception {
         Broadcast broadcast = new Broadcast(BBC_ONE.uri(), now.minusHours(6), now.minusHours(5));
         version1.addBroadcast(broadcast);
         
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(BBC_ONE, BBC_TWO), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
         assertSchedule(schedule);
         
         schedule = store.schedule(now.minusHours(6), now.minusHours(5), ImmutableSet.of(BBC_ONE), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
         
         ScheduleChannel channel = Iterables.getOnlyElement(schedule.scheduleChannels());
         Item item1 = Iterables.getOnlyElement(channel.items());
         Broadcast broadcast1 = ScheduleEntry.BROADCAST.apply(item1);
         assertEquals(now.minusHours(6), broadcast1.getTransmissionTime());
     }
     
     @Test
     public void wrongPublisherShouldBeFiltered() throws Exception {
         Item copy = (Item) item1.copy();
         copy.setPublisher(Publisher.BLIP);
         
         store.writeScheduleFrom(copy);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(BBC_ONE), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
         ScheduleChannel channel = Iterables.getOnlyElement(schedule.scheduleChannels());
         Item item1 = Iterables.getOnlyElement(channel.items());
         Broadcast broadcast1 = ScheduleEntry.BROADCAST.apply(item1);
         assertEquals(now.minusHours(2), broadcast1.getTransmissionTime());
     }
     
     private void assertSchedule(Schedule schedule) {
         assertEquals(2, schedule.scheduleChannels().size());
         
         for (ScheduleChannel channel: schedule.scheduleChannels()) {
             assertEquals(2, channel.items().size());
             
             Item item1 = channel.items().get(0);
             
             assertFalse(Iterables.isEmpty(item1.flattenLocations()));
             
             for (Location location : item1.flattenLocations()) {
                 assertTrue(location.getAvailable());
             }
             
             Broadcast broadcast1 = ScheduleEntry.BROADCAST.apply(item1);
             if (item1.getCanonicalUri().equals(this.item1.getCanonicalUri())) {
                 assertNotNull(item1.getContainer());
             }
             
             Item item2 = channel.items().get(1);
             assertTrue(item2 instanceof Episode);
             Broadcast broadcast2 = ScheduleEntry.BROADCAST.apply(item2);
             
             if (channel.channel() == BBC_ONE) {
                 assertEquals(now.minusHours(4), broadcast1.getTransmissionTime());
                 assertEquals(now.minusHours(2), broadcast2.getTransmissionTime());
             } else {
                 assertEquals(now.minusHours(4), broadcast1.getTransmissionTime());
                 assertEquals(now.minusHours(1), broadcast2.getTransmissionTime());
             }
         }
     }
     
     @Test
     public void testResolvingScheduleByCount() {
         
         Item item1 = itemWithBroadcast(BBC_ONE, now, now.plusHours(30));
         Item item2 = itemWithBroadcast(BBC_ONE, now.plusHours(30), now.plusHours(35));
         Item item3 = itemWithBroadcast(BBC_TWO, now, now.plusHours(5));
         Item item4 = itemWithBroadcast(BBC_TWO, now.plusHours(5), now.plusHours(10));
         
         ContentResolver contentResolver = new StubContentResolver()
             .respondTo(item1)
             .respondTo(item2)
             .respondTo(item3)
             .respondTo(item4);
         
         ImmutableSet<Channel> channels = ImmutableSet.of(BBC_ONE, BBC_TWO);
         ChannelResolver channelResolver = new DummyChannelResolver(channels);
         MongoScheduleStore store = new MongoScheduleStore(database, contentResolver, channelResolver, mock(EquivalentContentResolver.class));
 
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         store.writeScheduleFrom(item3);
         store.writeScheduleFrom(item4);
         
         checkCount(now, store, channels, 1, 1);
         checkCount(now, store, channels, 2, 2);
 
     }
 
     @Test
     public void testResolvesMaxItemsWhenCountHigherThanMax() {
         
         Item item1 = itemWithBroadcast(BBC_ONE, now, now.plusHours(30));
         Item item2 = itemWithBroadcast(BBC_ONE, now.plusHours(30), now.plusHours(35));
         
         ContentResolver contentResolver = new StubContentResolver()
             .respondTo(item1)
             .respondTo(item2);
         
         ImmutableSet<Channel> channel = ImmutableSet.of(BBC_ONE);
         ChannelResolver channelResolver = new DummyChannelResolver(channel);
         MongoScheduleStore store = new MongoScheduleStore(database, contentResolver, channelResolver, mock(EquivalentContentResolver.class));
         
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         checkCount(now, store, channel, 3, 2);
         
     }
 
     @Test
     public void testIgnoresRefsInEntryBeforeFromParam() {
         
         Item item1 = itemWithBroadcast(BBC_ONE, now, now.plusMinutes(30));
         Item item2 = itemWithBroadcast(BBC_ONE, now.plusMinutes(30), now.plusMinutes(60));
         
         ContentResolver contentResolver = new StubContentResolver()
             .respondTo(item1)
             .respondTo(item2);
         
         ImmutableSet<Channel> channel = ImmutableSet.of(BBC_ONE);
         ChannelResolver channelResolver = new DummyChannelResolver(channel);
         MongoScheduleStore store = new MongoScheduleStore(database, contentResolver, channelResolver, mock(EquivalentContentResolver.class));
         
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         checkCount(now.plusMinutes(45), store, channel, 1, 1);
         
     }
     
     @Test
     public void testResolvesEquivalentItemsForApplicationConfigurationWithPrecendenceEnabled() {
         
         Item item1 = itemWithBroadcast(BBC_ONE, now, now.plusHours(30));
         Item item2 = itemWithBroadcast(BBC_ONE, now.plusHours(30), now.plusHours(35));
         Item item3 = itemWithBroadcast(BBC_TWO, now, now.plusHours(5));
         Item item4 = itemWithBroadcast(BBC_TWO, now.plusHours(5), now.plusHours(10));
         
         Collection<String> uris = Collections2.transform(ImmutableList.of(item1,item2,item3,item4), Identified.TO_URI);
         
         ChannelResolver channelResolver = new DummyChannelResolver(ImmutableList.of(BBC_ONE, BBC_TWO, Channel_4_HD, AL_JAZEERA_ENGLISH));
         ContentResolver contentResolver = mock(ContentResolver.class);
         EquivalentContentResolver equivalentContentResolver = mock(EquivalentContentResolver.class);
         MongoScheduleStore store = new MongoScheduleStore(database, contentResolver, channelResolver, equivalentContentResolver);
 
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         store.writeScheduleFrom(item3);
         store.writeScheduleFrom(item4);
         
         Optional<ApplicationConfiguration> appConfig = Optional.of(
             ApplicationConfiguration.defaultConfiguration()
                 .copyWithPrecedence(ImmutableList.<Publisher>of()));
         
         when(equivalentContentResolver.resolveUris(
                argThat(hasItem(isIn(uris))), 
                 argThat(is(appConfig.get().getEnabledSources())), 
                 argThat(is(Annotation.defaultAnnotations())), 
                 eq(false)))
             .thenReturn(EquivalentContent.builder().build());
         
         store.schedule(now, now.plusHours(48), ImmutableSet.of(BBC_ONE, BBC_TWO), ImmutableSet.of(Publisher.BBC), appConfig);
         
         verify(contentResolver, never()).findByCanonicalUris(any(Iterable.class));
         for (String uri : uris) {
            verify(equivalentContentResolver).resolveUris(argThat(hasItem(uri)), 
                     argThat(is(appConfig.get().getEnabledSources())), 
                     argThat(is(Annotation.defaultAnnotations())), 
                     eq(false));
         }
     }
 
     private void checkCount(DateTime from, MongoScheduleStore store, Set<Channel> channels, int requestedCount, int expectedCount) {
         Schedule schedule = store.schedule(from, requestedCount, 
                 ImmutableSet.copyOf(channels), ImmutableSet.of(Publisher.BBC), Optional.<ApplicationConfiguration>absent());
         for (ScheduleChannel sc : schedule.scheduleChannels()) {
             assertThat(String.format("Schedule for %s should have %s items", sc.channel(), expectedCount),
                     sc.items().size(), is(expectedCount));
         }
     }
 
     private Item itemWithBroadcast(Channel channel, DateTime start, DateTime end) {
         Item item = new Item();
         item.setCanonicalUri(String.valueOf(item.hashCode()));
         item.setPublisher(Publisher.BBC);
         
         Version version = new Version();
         version.addBroadcast(new Broadcast(channel.getCanonicalUri(), start, end));
         
         item.addVersion(version);
         return item;
     }
 }
