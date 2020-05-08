 package org.atlasapi.remotesite.lovefilm;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.SetMultimap;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 
 /**
  * Extracts LoveFilmDataRow, merges with existing Content and writes.
  * 
  * Rows are not required to be passed to this in the order they have to be
  * written. To achieve this a set of 'seen' content IDs is maintained along with
  * a Multimap of content ID to cached Content. When Content is extracted, if its
  * parent(s) have not yet been written it is placed in the cache mapped against
  * the missing parent. When a possible parent is written its ID is added to
  * 'seen' and any Content cached against that ID is written and removed from the
  * cache.
  * 
  */
 public class DefaultLoveFilmDataRowHandler implements LoveFilmDataRowHandler {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private final ContentResolver resolver;
     private final ContentWriter writer;
     private final ContentExtractor<LoveFilmDataRow, Optional<Content>> extractor;
 
     private final HashSet<String> seen = Sets.newHashSet();
     private final SetMultimap<String, Content> cached = HashMultimap.create();
 
     public DefaultLoveFilmDataRowHandler(ContentResolver resolver, ContentWriter writer) {
         this(resolver, writer, new LoveFilmDataRowContentExtractor());
     }
 
     public DefaultLoveFilmDataRowHandler(ContentResolver resolver, ContentWriter writer, 
             ContentExtractor<LoveFilmDataRow, Optional<Content>> extractor) {
         this.resolver = resolver;
         this.writer = writer;
         this.extractor = extractor;
     }
 
     @Override
     public void prepare() {
     }
 
     @Override
     public void handle(LoveFilmDataRow row) {
         Optional<Content> possibleContent = extract(row);
         if (!possibleContent.isPresent()) {
             return;
         }
         Content content = possibleContent.get();
         Maybe<Identified> existing = resolve(content.getCanonicalUri());
         if (existing.isNothing()) {
             write(content);
         } else {
             Identified identified = existing.requireValue();
             if (content instanceof Item) {
                 write(merge(asItem(identified), (Item) content));
             } else if (content instanceof Container) {
                 write(merge(asContainer(identified), (Container) content));
             }
         }
     }
 
     @Override
     public void finish() {
         if (cached.values().size() > 0) {
             log.warn("{} extracted but unwritten", cached.values().size());
             for (Entry<String, Collection<Content>> mapping : cached.asMap().entrySet()) {
                 log.warn(mapping.toString());
             }
 
         }
         seen.clear();
         cached.clear();
     }
 
     private Optional<Content> extract(LoveFilmDataRow row) {
         return extractor.extract(row);
     }
 
     //TODO: extract merging to somewhere common.
     private Item merge(Item current, Item extracted) {
         current = mergeContents(current, extracted);
         current.setParentRef(extracted.getContainer());
         if (current instanceof Episode && extracted instanceof Episode) {
             Episode currentEp = (Episode) current;
             Episode extractedEp = (Episode) extracted;
             currentEp.setEpisodeNumber(extractedEp.getEpisodeNumber());
             currentEp.setSeriesRef(extractedEp.getSeriesRef());
         }
         return current;
     }
 
     private Container merge(Container current, Container extracted) {
         current = mergeContents(current, extracted);
         if (current instanceof Series && extracted instanceof Series) {
             ((Series) current).setParentRef(((Series) extracted).getParent());
         }
         return current;
     }
 
     private <C extends Content> C mergeContents(C current, C extracted) {
         current.setTitle(extracted.getTitle());
         current.setImage(extracted.getImage());
         current.setYear(extracted.getYear());
         current.setGenres(extracted.getGenres());
         current.setPeople(extracted.people());
         current.setLanguages(extracted.getLanguages());
         current.setCertificates(extracted.getCertificates());
         return current;
     }
 
     private Maybe<Identified> resolve(String uri) {
         ImmutableSet<String> uris = ImmutableSet.of(uri);
         return resolver.findByCanonicalUris(uris).get(uri);
     }
 
     private void write(Content content) {
         if (content instanceof Container) {
             if (content instanceof Series) {
                 cacheOrWriteSeriesAndSubContents((Series) content);
             } else if (content instanceof Brand) {
                 writeBrandAndCachedSubContents((Brand) content);
             } else {
                 writer.createOrUpdate((Container) content);
             }
         } else if (content instanceof Item) {
             if (content instanceof Episode) {
                 cacheOrWriteEpisode((Episode) content);
             } else {
                 cacheOrWriteItem(content);
             }
         }
     }
 
     private void writeBrandAndCachedSubContents(Brand brand) {
         writer.createOrUpdate(brand);
         String brandUri = brand.getCanonicalUri();
         seen.add(brandUri);
         for (Content subContent : cached.removeAll(brandUri)) {
             write(subContent);
         }
     }
 
     private void cacheOrWriteSeriesAndSubContents(Series series) {
         ParentRef parent = series.getParent();
         if (parent != null && !seen.contains(parent.getUri())) {
             cached.put(parent.getUri(), series);
         } else {
             String seriesUri = series.getCanonicalUri();
             writer.createOrUpdate(series);
             seen.add(seriesUri);
             for (Content episode : cached.removeAll(seriesUri)) {
                 write(episode);
             }
         }
     }
 
     protected void cacheOrWriteItem(Content content) {
         Item item = (Item) content;
         ParentRef parent = item.getContainer();
         if (parent != null && !seen.contains(parent.getUri())) {
             cached.put(parent.getUri(), item);
         }
        writer.createOrUpdate((Item) content);
     }
 
     private void cacheOrWriteEpisode(Episode episode) {
         String brandUri = episode.getContainer().getUri();
         String seriesUri = episode.getSeriesRef().getUri();
         if (!seen.contains(brandUri)) {
             cached.put(brandUri, episode);
         } else if (!seen.contains(seriesUri)) {
             cached.put(seriesUri, episode);
         } else {
             writer.createOrUpdate(episode);
         }
     }
 
     private Container asContainer(Identified identified) {
         return castTo(identified, Container.class);
     }
 
     private Item asItem(Identified identified) {
         return castTo(identified, Item.class);
     }
 
     private <T> T castTo(Identified identified, Class<T> cls) {
         try {
             return cls.cast(identified);
         } catch (ClassCastException e) {
             throw new ClassCastException(String.format("%s: expected %s got %s", 
                 identified.getCanonicalUri(), 
                 cls.getSimpleName(), 
                 identified.getClass().getSimpleName()));
         }
     }
 
 }
