 package org.atlasapi.remotesite.metabroadcast.picks;
 
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.transform;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.media.entity.Schedule.ScheduleChannel;
 import org.atlasapi.media.util.ItemAndBroadcast;
 import org.atlasapi.persistence.content.ContentGroupResolver;
 import org.atlasapi.persistence.content.ContentGroupWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.remotesite.bbc.nitro.ChannelDay;
 import org.atlasapi.remotesite.bbc.nitro.ChannelDayProcessor;
 import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.scheduling.UpdateProgress;
 
 
 public class PicksDayUpdater implements ChannelDayProcessor {
 
    private static final Logger log = LoggerFactory.getLogger(PicksDayUpdater.class);
    
     private static final String CONTENT_GROUP = "http://picks.metabroadcast.com/schedule-picks";
     private final ScheduleResolver scheduleResolver;
     private final ContentGroupResolver contentGroupResolver;
     private final ContentResolver contentResolver;
     private final ContentGroupWriter contentGroupWriter;
     private final PicksChannelsSupplier picksChannelsSupplier;
     
     public PicksDayUpdater(ScheduleResolver scheduleResolver, ContentGroupResolver contentGroupResolver,
             ContentResolver contentResolver, ContentGroupWriter contentGroupWriter, PicksChannelsSupplier picksChannelsSupplier) {
         this.scheduleResolver = scheduleResolver;
         this.contentGroupResolver = contentGroupResolver;
         this.contentGroupWriter = contentGroupWriter;
         this.contentResolver = contentResolver;
         this.picksChannelsSupplier = picksChannelsSupplier;
     }
     
     @Override
     public UpdateProgress process(ChannelDay channelDay) throws Exception {
         try {
             Iterable<Item> picks = concat(transform(scheduleResolver.unmergedSchedule(
                     channelDay.getDay().toDateTimeAtStartOfDay(DateTimeZone.UTC), 
                     channelDay.getDay().plusDays(1).toDateTimeAtStartOfDay(DateTimeZone.UTC), 
                     ImmutableSet.of(channelDay.getChannel()), 
                     ImmutableSet.of(Publisher.PA)).scheduleChannels(), TO_ITEMS));
             
             // We need to resolve the content to get all broadcasts
             final Map<String, Maybe<Identified>> resolved = contentResolver.findByCanonicalUris(Iterables.transform(picks, Identified.TO_URI)).asMap();
             
             Iterable<ItemAndBroadcast> itemsAndBroadcasts = Iterables.transform(picks, new Function<Item, ItemAndBroadcast>() {
 
                 @Override
                 public ItemAndBroadcast apply(Item item) {
                     return new ItemAndBroadcast((Item) resolved.get(item.getCanonicalUri()).requireValue(), 
                             Maybe.just(Iterables.getOnlyElement(Item.FLATTEN_BROADCASTS.apply(item))));
                 }
                 
             });
             
             addPicksToContentGroup(findPicks(itemsAndBroadcasts));
             
             return UpdateProgress.SUCCESS;
         } catch (Exception e) {
            log.error("Processing " + channelDay.getChannel().getCanonicalUri() 
                    + " [" + channelDay.getChannel().getTitle() + "] Day " 
                    + channelDay.getDay().toString(), e);
             return UpdateProgress.FAILURE;
         }
     }
 
     private Iterable<Item> findPicks(Iterable<ItemAndBroadcast> itemsAndBroadcasts) {
         return transform(filter(itemsAndBroadcasts, 
                 new PickPredicate(picksChannelsSupplier.get())), TO_ITEM);
     }
     
     private void addPicksToContentGroup(Iterable<Item> items) {
         ContentGroup contentGroup = resolveOrCreateContentGroup();
         Iterable<ChildRef> childRefs = transform(items, Item.TO_CHILD_REF);
         for (ChildRef childRef : childRefs) {
             if (!contentGroup.getContents().contains(childRef)) {
                 contentGroup.addContent(childRef);
             }
         }
         contentGroupWriter.createOrUpdate(contentGroup);
     }
     
     private ContentGroup resolveOrCreateContentGroup() {
         ResolvedContent contentGroup = contentGroupResolver.findByCanonicalUris(ImmutableSet.of(CONTENT_GROUP));
         Maybe<Identified> first = contentGroup.getFirstValue();
         
         if (first.hasValue()) {
             return (ContentGroup) first.requireValue();
         }
         
         return new ContentGroup(CONTENT_GROUP, Publisher.METABROADCAST_PICKS);
     }
     
     private static final Function<ScheduleChannel, List<Item>> TO_ITEMS = new Function<ScheduleChannel, List<Item>>() {
 
         @Override
         public List<Item> apply(ScheduleChannel scheduleChannel) {
             return scheduleChannel.items();
         }
     };
     
     private static final Function<ItemAndBroadcast, Item> TO_ITEM = new Function<ItemAndBroadcast, Item>() {
 
         @Override
         public Item apply(ItemAndBroadcast itemAndBroadcast) {
             return itemAndBroadcast.getItem();
         }
         
     };
 }
