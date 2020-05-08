 package org.atlasapi.remotesite;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.media.entity.ItemTranslator;
 import org.atlasapi.remotesite.ItemAvailabilityUpdater;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.TimeMachine;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class ItemAvailabilityUpdaterTest extends TestCase {
 	
 	DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
 	DBCollection itemsColl = mongo.collection("items");
 	
 	TimeMachine clock = new TimeMachine(); 
 	ItemAvailabilityUpdater updater = new ItemAvailabilityUpdater(mongo, new NullAdapterLog(), clock);
 	
 	ItemTranslator translator = new ItemTranslator();
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		Policy p = new Policy();
		p.setAvailabilityStart(new DateTime(10L));
		p.setAvailabilityEnd(new DateTime(20L));
 		
 		Location l = new Location();
 		l.setPolicy(p);
 		l.setAvailable(false);
 		
 		Encoding e = new Encoding();
 		e.addAvailableAt(l);
 		
 		Version v = new Version();
 		v.addManifestedAs(e);
 		
 		Item i = new Item("testUri", "testCurie", Publisher.BBC);
 		i.addVersion(v);
 		
 		itemsColl.insert(translator.toDBObject(new BasicDBObject(), i));
 	}
 	
 	protected void tearDown() throws Exception {
 		itemsColl.remove(new BasicDBObject());
 	}
 
 	public void testRun() {
 		clock.jumpTo(new DateTime(5L, DateTimeZones.UTC));
 		updater.run();
 		
 		List<Item> items = getItems();
 		assertThat(items.size(), is(equalTo(1)));
 		assertThat(items.get(0).isAvailable(), is(false));
 		
 		clock.jumpTo(new DateTime(15L, DateTimeZones.UTC));
 		updater.run();
 		
 		items = getItems();
 		assertThat(items.size(), is(equalTo(1)));
 		assertThat(items.get(0).isAvailable(), is(true));
 		
 		clock.jumpTo(new DateTime(25L, DateTimeZones.UTC));
 		updater.run();
 		
 		items = getItems();
 		assertThat(items.size(), is(equalTo(1)));
 		assertThat(items.get(0).isAvailable(), is(false));
 		
 		clock.jumpTo(new DateTime(15L, DateTimeZones.UTC));
 		updater.run();
 		
 		items = getItems();
 		assertThat(items.size(), is(equalTo(1)));
 		assertThat(items.get(0).isAvailable(), is(true));
 		
 		clock.jumpTo(new DateTime(5L, DateTimeZones.UTC));
 		updater.run();
 		
 		items = getItems();
 		assertThat(items.size(), is(equalTo(1)));
 		assertThat(items.get(0).isAvailable(), is(false));
 		
 	}
 
 	private List<Item> getItems() {
 		List<Item> items = ImmutableList.copyOf(Iterables.transform(itemsColl.find(), new Function<DBObject, Item>() {
 			@Override
 			public Item apply(DBObject from) {
 				return translator.fromDBObject(from, null);
 			}
 		}));
 		return items;
 	}
 
 }
