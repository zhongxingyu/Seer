 package org.atlasapi.persistence.content.schedule.mongo;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.ScheduleEntry;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.media.entity.ScheduleEntryTranslator;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.mongodb.DBCollection;
 
 public class MongoScheduleStore implements ScheduleResolver, ScheduleWriter {
 
 	private final static long BIN_MILLIS = Duration.standardHours(1).getMillis();
 	private final static Duration MAX_DURATION = Duration.standardDays(14);
 	private static final Duration MAX_BROADCAST_AGE = Duration.standardDays(365);
 
     private final DBCollection collection;
     private final ScheduleEntryTranslator translator = new ScheduleEntryTranslator();
 
     public MongoScheduleStore(DatabasedMongo db) {
         collection = db.collection("schedule");
         collection.ensureIndex(translator.toIndex(), MongoConstants.BACKGROUND);
     }
 
     public void writeScheduleFor(Iterable<? extends Item> items) {
         Map<String, ScheduleEntry> scheduleEntries = toScheduleEntries(items);
         Map<String, ScheduleEntry> existingEntries = Maps.uniqueIndex(translator.fromDbObjects(where().idIn(scheduleEntries.keySet()).find(collection)), ScheduleEntry.KEY);
         
         for (ScheduleEntry entry: scheduleEntries.values()) {
             ScheduleEntry updateEntry;
             ScheduleEntry existingEntry = existingEntries.get(entry.toKey());
             if (existingEntry == null) {
                 updateEntry = entry;
             } else {
 				updateEntry = existingEntry;
                 updateEntry.withItems(mergeItems(updateEntry.items(), entry.items()));
             }
 			collection.save(translator.toDb(updateEntry));
         }
     }
     
     private Iterable<Item> mergeItems(Iterable<Item> original, Iterable<Item> latest) {
     	final ImmutableSet<String> latestUris = ImmutableSet.copyOf(Iterables.transform(latest, Item.TO_URI));
     	
     	return ImmutableSortedSet.copyOf(ScheduleEntry.START_TIME_ITEM_COMPARATOR, Iterables.concat(latest, Iterables.filter(original, new Predicate<Item>() {
 			@Override
 			public boolean apply(Item input) {
 				return !latestUris.contains(input.getCanonicalUri());
 			}
 		})));
     }
     
     private Map<String, ScheduleEntry> toScheduleEntries(Iterable<? extends Item> items) {
         Map<String, ScheduleEntry> entries = Maps.newHashMap();
 
         for (Item item : items) {
             for (Version version : item.nativeVersions()) {
                 for (Broadcast broadcast : version.getBroadcasts()) {
                     Version entryVersion = version.copyWithBroadcasts(ImmutableSet.of(broadcast.copy()));
                    Item entryItem = item.copyWithVersions(ImmutableSet.of(entryVersion));
 
                     Channel channel = Channel.fromUri(broadcast.getBroadcastOn()).requireValue();
                     Publisher publisher = item.getPublisher();
                     
                     for (Interval interval: intervalsFor(broadcast.getTransmissionTime(), broadcast.getTransmissionEndTime())) {
                         String key = ScheduleEntry.toKey(interval, channel, publisher);
 
                         if (entries.containsKey(key)) {
                             ScheduleEntry entry = entries.get(key);
                             entry.withItems(ImmutableList.<Item> builder().addAll(entry.items()).add(entryItem).build());
                         } else {
                             ScheduleEntry entry = new ScheduleEntry(interval, channel, publisher, ImmutableList.of(entryItem));
                             entries.put(key, entry);
                         }
                     }
                 }
             }
         }
 
         return entries;
     }
 
     @Override
     public Schedule schedule(DateTime from, DateTime to, Iterable<Channel> channels, Iterable<Publisher> publishers) {
         Map<Channel, List<Item>> channelMap = createChannelMap(channels);
         Interval interval = new Interval(from, to);
         if (interval.toDuration().isLongerThan(MAX_DURATION)) {
             throw new IllegalArgumentException("You cannot request more than 2 weeks of schedule");
         }
         List<ScheduleEntry> entries = translator.fromDbObjects(where().idIn(keys(intervalsFor(from, to), channels, publishers)).find(collection));
         
         for (ScheduleEntry entry: entries) {
             channelMap.get(entry.channel()).addAll(entry.items());
         }
         
         ImmutableMap.Builder<Channel, List<Item>> processedChannelMap = ImmutableMap.builder();
         for (Entry<Channel, List<Item>> entry: channelMap.entrySet()) {
             processedChannelMap.put(entry.getKey(), processChannelItems(entry.getValue(), interval));
         }
         return Schedule.fromChannelMap(processedChannelMap.build(), interval);
     }
     
     private Map<Channel, List<Item>> createChannelMap(Iterable<Channel> channels) {
         ImmutableMap.Builder<Channel, List<Item>> channelMap = ImmutableMap.builder();
         for (Channel channel : channels) {
             channelMap.put(channel, Lists.<Item> newArrayList());
         }
         return channelMap.build();
     }
     
     private List<Item> processChannelItems(Iterable<Item> items, final Interval interval) {
         return filterLocations(orderItems(filterItems(items, interval)));
     }
     
     private List<Item> orderItems(Iterable<Item> items) {
         return Ordering.from(ScheduleEntry.START_TIME_ITEM_COMPARATOR).immutableSortedCopy(items);
     }
     
     private Iterable<Item> filterItems(Iterable<Item> items, final Interval interval) {
         final Predicate<Item> validBroadcast = new Predicate<Item>() {
             @Override
             public boolean apply(Item item) {
                 Broadcast broadcast = ScheduleEntry.BROADCAST.apply(item);
                 return interval.overlaps(new Interval(broadcast.getTransmissionTime(), broadcast.getTransmissionEndTime()));
             }
         };
         
         return Iterables.transform(ImmutableSet.copyOf(Iterables.transform(Iterables.filter(items, validBroadcast), ItemScheduleEntry.ITEM_SCHEDULE_ENTRY)), ItemScheduleEntry.ITEM);
     }
     
     private List<Item> filterLocations(Iterable<Item> items) {
         return ImmutableList.copyOf(Iterables.transform(items, new Function<Item, Item>() {
             @Override
             public Item apply(Item input) {
                 for (Version version: input.getVersions()) {
                     for (Encoding encoding: version.getManifestedAs()) {
                         if (! encoding.getAvailableAt().isEmpty()) {
                             encoding.setAvailableAt(ImmutableSet.copyOf(Iterables.filter(encoding.getAvailableAt(), Location.AVAILABLE_LOCATION)));
                         }
                     }
                 }
                 return input;
             }
         }));
     }
     
     private static List<String> keys(Iterable<Interval> intervals, Iterable<Channel> channels, Iterable<Publisher> publishers) {
         ImmutableList.Builder<String> keys = ImmutableList.builder();
         for (Interval interval: intervals) {
             for (Channel channel: channels) {
                 for (Publisher publisher: publishers) {
                     keys.add(ScheduleEntry.toKey(interval, channel, publisher));
                 }
             }
         }
         return keys.build();
     }
     
     private long millisBackToNearestBin(DateTime when) {
         long div = when.getMillis() / BIN_MILLIS;
         int asInt = (int) div;
         return asInt * BIN_MILLIS;
     }
     
     private List<Interval> intervalsFor(DateTime start, DateTime end) {
     	if (start.isBefore(new DateTime(DateTimeZones.UTC).minus(MAX_BROADCAST_AGE))) {
     		return ImmutableList.of();
     	}
     	
         long startMillis = millisBackToNearestBin(start);
         long endMillis = end.getMillis();
         ImmutableList.Builder<Interval> intervals = ImmutableList.builder();
 
         while (startMillis < endMillis) {
             Interval interval = new Interval(new DateTime(startMillis, DateTimeZones.UTC), new DateTime(startMillis + BIN_MILLIS - 1, DateTimeZones.UTC));
             intervals.add(interval);
             startMillis += (BIN_MILLIS);
         }
         
         return intervals.build();
     }
     
     private static class ItemScheduleEntry {
         
         private final Item item;
         
         public ItemScheduleEntry(Item item) {
             Preconditions.checkNotNull(item);
             this.item = item;
         }
         
         public Item item() {
             return item;
         }
         
         public DateTime startTime() {
             return ScheduleEntry.BROADCAST.apply(item).getTransmissionTime();
         }
         
         public String canonicalUri() {
             return item.getCanonicalUri();
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof ItemScheduleEntry) {
                 ItemScheduleEntry entry = (ItemScheduleEntry) obj;
                 return startTime().equals(entry.startTime()) && canonicalUri().equals(entry.canonicalUri());
             }
             return super.equals(obj);
         }
         
         @Override
         public int hashCode() {
             return Objects.hashCode(startTime(), canonicalUri());
         }
         
         final static Function<ItemScheduleEntry, Item> ITEM = new Function<ItemScheduleEntry, Item>() {
             @Override
             public Item apply(ItemScheduleEntry input) {
                 return input.item();
             }
         };
         
         final static Function<Item, ItemScheduleEntry> ITEM_SCHEDULE_ENTRY = new Function<Item, ItemScheduleEntry>() {
             @Override
             public ItemScheduleEntry apply(Item input) {
                 return new ItemScheduleEntry(input);
             }
         };
     }
 
 	void writeScheduleFrom(Item item1) {
 		writeScheduleFor(ImmutableList.of(item1));
 	}
 	
 	public static void main(String[] args) {
	    Interval interval = new Interval(new DateTime("2011-04-17T22:00:00.0000Z"), new DateTime("2011-04-18T00:00:00.000Z"));
	    List<String> keys = keys(ImmutableList.of(interval), ImmutableList.of(Channel.BBC_RADIO_ESSEX), ImmutableList.of(Publisher.BBC));
 	    System.out.println(keys);
 	}
 }
