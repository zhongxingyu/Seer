 package org.atlasapi.persistence.content.schedule.mongo;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.ScheduleEntry;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.media.entity.Schedule.ScheduleChannel;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.ids.UUIDGenerator;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class MongoScheduleStoreTest {
 
     private MongoScheduleStore store;
     
     private final DateTime now = new DateTime(DateTimeZones.UTC);
     private final Broadcast broadcast1 = new Broadcast(Channel.BBC_ONE.uri(), now.minusHours(4), now.minusHours(2));
     private final Broadcast broadcast2 = new Broadcast(Channel.BBC_TWO.uri(), now.minusHours(4), now.minusHours(1));
     private final Broadcast broadcast3 = new Broadcast(Channel.BBC_ONE.uri(), now.minusHours(2), now.minusHours(1));
     private final Broadcast broadcast4 = new Broadcast(Channel.BBC_TWO.uri(), now.minusHours(1), now);
 
     private final Broadcast veryOldBroadcast = new Broadcast(Channel.BBC_TWO.uri(), now.minusYears(1).minusHours(1), now.minusYears(1));
     
     private final Version version1 = new Version();
     private final Version version2 = new Version();
     private final Version version3 = new Version();
     
     private final Item item1 = new Item("item1", "item1", Publisher.BBC);
    private final Item item2 = new Episode("item2", "item2", Publisher.BBC);
     private final Brand brand1 = new Brand("brand1", "brand1", Publisher.BBC);
     
     private final Location availableLocation = new Location();
     private final Location unavailableLocation = new Location();
     private final Encoding encoding = new Encoding();
     private long when = System.currentTimeMillis();
     
     private DatabasedMongo database;
     
     @Before
     public void setUp() throws Exception {
         database = MongoTestHelper.anEmptyTestDatabase();
 		store = new MongoScheduleStore(database);
         
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
     }
     
     @After
     public void tearDown() {
         System.out.println("Completed in "+(System.currentTimeMillis()-when)+" millis");
     }
     
     @Test
     public void shouldSaveItemsAndRetrieveSchedule() throws Exception {
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(Channel.BBC_ONE, Channel.BBC_TWO), ImmutableSet.of(Publisher.BBC));
         assertSchedule(schedule);
     }
 
     @Test
     public void testShouldIgnoreBroadcastsOverAYearOld() throws Exception {
   
     	Item item = itemWithBroadcast(new Broadcast(Channel.BBC_ONE.uri(), now.withHourOfDay(1).withMinuteOfHour(10), now.withHourOfDay(1).withMinuteOfHour(20)));
     	store.writeScheduleFrom(item);
 
         assertEquals(1, Iterables.size(database.collection("schedule").find()));
     }
     
     @Test
     public void testUpdatingABroadcast() throws Exception {
     	DateTime someHour = now.withHourOfDay(1).withMinuteOfHour(0);
 		Broadcast broadcast = new Broadcast(Channel.BBC_ONE.uri(), someHour.withMinuteOfHour(10), someHour.withMinuteOfHour(20));
 
 		broadcast.withId("v1");
     	store.writeScheduleFrom(itemWithBroadcast(broadcast));
     	
     	Schedule schedule = store.schedule(someHour, someHour.plusHours(1), ImmutableList.of(Channel.BBC_ONE), ImmutableList.of(Publisher.BBC));
     	assertEquals("v1", extractBroadcastFrom(schedule).getId());
     	
 		broadcast.withId("v2");
     	store.writeScheduleFrom(itemWithBroadcast(broadcast));
     	
     	schedule = store.schedule(someHour, someHour.plusHours(1), ImmutableList.of(Channel.BBC_ONE), ImmutableList.of(Publisher.BBC));
     	assertEquals("v2", extractBroadcastFrom(schedule).getId());
 	}
 
 	private Broadcast extractBroadcastFrom(Schedule schedule) {
 		return Iterables.getOnlyElement(Iterables.getOnlyElement(Iterables.getOnlyElement(Iterables.getOnlyElement(schedule.scheduleChannels()).items()).getVersions()).getBroadcasts());
 	}
 
 	private Item itemWithBroadcast(Broadcast broadcast) {
 		Version version = new Version();
     	version.addBroadcast(veryOldBroadcast);
     	version.addBroadcast(broadcast);
     	
     	Item item = new Item(new UUIDGenerator().generate(), "", Publisher.BBC);
     	
 		item.addVersion(version);
 		return item;
 	}
     
     @Test
     public void shouldSaveItemsAndRetrieveScheduleWithLoadsOfPublishers() throws Exception {
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(Channel.BBC_ONE, Channel.BBC_TWO), ImmutableSet.of(Publisher.BBC, Publisher.C4, Publisher.ITV));
         assertSchedule(schedule);
     }
     
     @Test
     public void wrongChannelShouldBeFiltered() throws Exception {
         Broadcast broadcast = new Broadcast(Channel.AL_JAZEERA_ENGLISH.uri(), now.minusHours(2), now.minusHours(3));
         version1.addBroadcast(broadcast);
         
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(Channel.BBC_ONE, Channel.BBC_TWO), ImmutableSet.of(Publisher.BBC));
         assertSchedule(schedule);
     }
     
     @Test
     public void wrongIntervalShouldBeFiltered() throws Exception {
         Broadcast broadcast = new Broadcast(Channel.BBC_ONE.uri(), now.minusHours(6), now.minusHours(5));
         version1.addBroadcast(broadcast);
         
         store.writeScheduleFrom(item1);
         store.writeScheduleFrom(item2);
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(Channel.BBC_ONE, Channel.BBC_TWO), ImmutableSet.of(Publisher.BBC));
         assertSchedule(schedule);
         
         schedule = store.schedule(now.minusHours(6), now.minusHours(5), ImmutableSet.of(Channel.BBC_ONE), ImmutableSet.of(Publisher.BBC));
         
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
         
         Schedule schedule = store.schedule(now.minusHours(4), now, ImmutableSet.of(Channel.BBC_ONE), ImmutableSet.of(Publisher.BBC));
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
             assertTrue(Iterables.getOnlyElement(Iterables.getOnlyElement(Iterables.getOnlyElement(item1.getVersions()).getManifestedAs()).getAvailableAt()).getAvailable());
             Broadcast broadcast1 = ScheduleEntry.BROADCAST.apply(item1);
             if (item1.getCanonicalUri().equals(this.item1.getCanonicalUri())) {
                 assertNotNull(item1.getContainer());
             }
             
             Item item2 = channel.items().get(1);
            assertTrue(item2 instanceof Episode);
             Broadcast broadcast2 = ScheduleEntry.BROADCAST.apply(item2);
             
             
             if (channel.channel() == Channel.BBC_ONE) {
                 assertEquals(now.minusHours(4), broadcast1.getTransmissionTime());
                 assertEquals(now.minusHours(2), broadcast2.getTransmissionTime());
             } else {
                 assertEquals(now.minusHours(4), broadcast1.getTransmissionTime());
                 assertEquals(now.minusHours(1), broadcast2.getTransmissionTime());
             }
         }
     }
 }
