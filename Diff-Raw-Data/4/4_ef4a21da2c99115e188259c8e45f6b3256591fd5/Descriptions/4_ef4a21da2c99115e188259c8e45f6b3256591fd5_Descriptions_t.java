 package org.atlasapi.media.util;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.simple.Description;
 import org.atlasapi.media.entity.simple.Item;
 import org.atlasapi.media.entity.simple.Playlist;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.ImmutableList.Builder;
 
 public class Descriptions {
     private Descriptions() {
     }
     
     public static List<Item> getItems(Iterable<? extends Description> descriptions) {
         return ImmutableList.copyOf(Iterables.transform(Iterables.filter(descriptions, IS_ITEM), TO_ITEM));
     }
     
     public static List<Playlist> getPlaylists(Iterable<? extends Description> descriptions) {
         return ImmutableList.copyOf(Iterables.transform(Iterables.filter(descriptions, IS_PLAYLIST), TO_PLAYLIST));
     }
     
     public static final Predicate<Description> IS_PLAYLIST = new Predicate<Description>() {
         @Override
         public boolean apply(Description input) {
             return input instanceof Playlist;
         }
     };
     
     public static final Predicate<Description> IS_ITEM = new Predicate<Description>() {
         @Override
         public boolean apply(Description input) {
             return input instanceof Item;
         }
     };
     
     public static final Function<Description, Playlist> TO_PLAYLIST = new Function<Description, Playlist>() {
         @Override
         public Playlist apply(Description input) {
             return (Playlist) input;
         }
     };
     
     public static final Function<Description, Item> TO_ITEM = new Function<Description, Item>() {
         @Override
         public Item apply(Description input) {
             return (Item) input;
         }
     };
     
     public static final Function<Playlist, Iterable<Description>> FLATTEN_PLAYLIST = new Function<Playlist, Iterable<Description>>() {
         @Override
         public Iterable<Description> apply(Playlist from) {
             Builder<Description> flattened = ImmutableList.builder();
             
             flattened.add(from);
            if (from.getContent() != null && ! from.getContent().isEmpty()) {
                flattened.addAll(from.getContent());
             }
             
             return flattened.build();
         }
     };
 }
