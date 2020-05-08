 package org.atlasapi.remotesite.tvblob;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.EventFiringContentWriter;
 import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.content.mongo.MongoScheduleStore;
import org.atlasapi.persistence.content.mongo.ScheduleUpdatingContentListener;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.io.Resources;
 import com.google.inject.internal.Lists;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 
 public class TVBlobDayPopulatorTest extends TestCase {
 
     private MongoDbBackedContentStore store;
     private MongoScheduleStore scheduleStore;
     private TVBlobDayPopulator extractor;
     private EventFiringContentWriter writer;
     private final DateTime now = new DateTime("2010-07-20T00:00:00+00:00");
     private final Channel channel = new Channel("raiuno", "http://tvblob.com/channel/raiuno", "raiuno");
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         DatabasedMongo db = MongoTestHelper.anEmptyTestDatabase();
         this.scheduleStore = new MongoScheduleStore(db);
         this.store = new MongoDbBackedContentStore(db);
         this.writer = new EventFiringContentWriter(store, new ScheduleUpdatingContentListener(scheduleStore));
         extractor = new TVBlobDayPopulator(writer, store, "raiuno");
     }
     
     public void testShouldRetrievePlaylistAndItems() throws Exception {
         InputStream is = Resources.getResource(getClass(), "today.json").openStream();
         
         extractor.populate(is);
         
         Schedule schedule = scheduleStore.schedule(now, now.plusDays(5), ImmutableSet.of(channel), ImmutableSet.of(Publisher.TVBLOB));
         List<Item> items = Iterables.getOnlyElement(schedule.scheduleChannels()).items();
         
         boolean foundBrandWithMoreThanOneEpisode = false;
         List<String> brandUris = Lists.newArrayList();
         
         Map<String, Integer> itemUriCount = Maps.newHashMap();
         for (Item episode: items) {
             if (episode.getContainer() != null) {
                 assertNotNull(episode.getContainer().getCanonicalUri());
                 if (brandUris.contains(episode.getContainer().getCanonicalUri())) {
                     foundBrandWithMoreThanOneEpisode = true;
                 } else {
                     brandUris.add(episode.getContainer().getCanonicalUri());
                 }
             }
             assertNotNull(episode.getCanonicalUri());
             assertFalse(episode.getVersions().isEmpty());
             
             Integer count = 0;
             if (itemUriCount.containsKey(episode.getCanonicalUri())) {
                 count = itemUriCount.get(episode.getCanonicalUri());
             }
             itemUriCount.put(episode.getCanonicalUri(), count+1);
             
             Version version = episode.getVersions().iterator().next();
             if (version.getBroadcasts().size() > 1) {
                 assertEquals(2, version.getBroadcasts().size());
             }
             
             for (Broadcast broadcast: version.getBroadcasts()) {
                 assertEquals("http://tvblob.com/channel/raiuno", broadcast.getBroadcastOn());
                 assertNotNull(broadcast.getTransmissionTime());
             }
         }
         
         boolean foundItemWithMoreTHanOneBroadcast = false;
         for (Integer count: itemUriCount.values()) {
             if (count > 1) {
                 foundItemWithMoreTHanOneBroadcast = true;
             }
         }
         assertTrue(foundItemWithMoreTHanOneBroadcast);
         assertTrue(foundBrandWithMoreThanOneEpisode);
         
         List<Identified> brands = store.findByCanonicalUri(ImmutableList.of("http://tvblob.com/brand/269"));
         
         assertFalse(brands.isEmpty());
         assertEquals(1, brands.size());
         Brand brand = (Brand) brands.get(0);
         assertEquals(2, brand.getContents().size());
     }
 }
