 /*
  * Copyright (c) 2010, Hamish Morgan.
  * All Rights Reserved.
  */
 package uk.ac.susx.mlcl.erl.linker;
 
 import static com.google.common.base.Preconditions.*;
 import com.google.common.base.Throwables;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.cache.Weigher;
 import com.google.common.collect.Sets;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import javax.annotation.Nonnull;
 import javax.annotation.concurrent.NotThreadSafe;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Instances of
  * <code>CachedCandidateGenerator</code> adapts another CachedCandidate implementation, and
  * implement a transparent memory cache over the methods.
  *
  * Repeated attempts to retrieve the same data will be returned from memory, rather than querying
  * the KB service. This particularly useful for web-based services such as Freebase, where
  * unnecessary calls can have a significant effect on performance.
  *
  * Memory usage is constrained to approximately 1 MiB per cache. There are currently two caches (one
  * for text and the other for search queries) so memory usage should not exceed 2MiB per instance.
  *
  * Not thread safe
  *
  * @author hamish
  */
 @Nonnull
 @NotThreadSafe
 public class CachedCandidateGenerator implements CandidateGenerator {
 
     private static final Logger LOG = LoggerFactory.getLogger(CachedCandidateGenerator.class);
     private final LoadingCache<String, Set<String>> searchCache;
 
     /**
      * Protected dependency injection constructor. Use 
      * {@link CachedCandidateGenerator#wrap(CandidateGenerator)} } instead.
      *
      * @param searchCache
      */
     protected CachedCandidateGenerator(final LoadingCache<String, Set<String>> searchCache) {
         this.searchCache = checkNotNull(searchCache, "searchCache");
     }
 
     @Override
     public Set<String> findCandidates(final String query) throws IOException {
         return get(searchCache, query);
     }
 
     @Override
     public Map<String, Set<String>> batchFindCandidates(Set<String> queries)
             throws IOException, ExecutionException {
         return searchCache.getAll(queries);
     }
 
     protected static <K, V> V get(final LoadingCache<K, V> cache, final K key)
             throws IOException {
         try {
             return cache.get(checkNotNull(key, "key"));
         } catch (final ExecutionException ex) {
             Throwables.propagateIfInstanceOf(ex.getCause(), IOException.class);
             Throwables.propagateIfPossible(ex.getCause());
             throw new RuntimeException("unexpected: ", ex);
         }
     }
 
     public static CandidateGenerator wrap(final CandidateGenerator inner) {
         if (checkNotNull(inner, "inner") instanceof CachedCandidateGenerator) {
             LOG.warn("Ignoring attempt to cache wrap a KnowledgeBase that was already cached.");
             return inner;
         }
 
         final LoadingCache<String, Set<String>> searchCache;
         {
            final Weigher<String, List<String>> searchWeighter =
                    new Weigher<String, List<String>>() {
                        public int weigh(String key, List<String> values) {
                             int sum = (4 * 2) + (2 * key.length());
                             for (String value : values) {
                                 sum += 4 + 2 * value.length();
                             }
                             return sum;
 
                         }
                     };
             final CacheLoader<String, Set<String>> searchLoader =
                     new CacheLoader<String, Set<String>>() {
                         @Override
                         public Set<String> load(String key) throws Exception {
                             return inner.findCandidates(key);
                         }
 
                         @Override
                         public Map<String, Set<String>> loadAll(Iterable<? extends String> keys)
                                 throws Exception {
                             return inner.batchFindCandidates(Sets.newHashSet(keys));
                         }
                     };
 
             searchCache = CacheBuilder
                     .newBuilder()
                     .weigher(searchWeighter)
                     .maximumWeight(1 << 20)
                     .build(searchLoader);
         }
 
         return new CachedCandidateGenerator(searchCache);
 
     }
 }
