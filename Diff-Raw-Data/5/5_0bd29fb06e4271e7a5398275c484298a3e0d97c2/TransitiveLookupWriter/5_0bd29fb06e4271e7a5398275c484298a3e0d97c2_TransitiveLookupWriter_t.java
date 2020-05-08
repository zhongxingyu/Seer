 package org.atlasapi.persistence.lookup;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Predicates.in;
 import static com.google.common.base.Predicates.not;
 import static com.google.common.base.Strings.emptyToNull;
 import static com.google.common.collect.Iterables.transform;
 import static org.atlasapi.media.entity.LookupRef.TO_ID;
 
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import org.atlasapi.equiv.ContentRef;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.MorePredicates;
 
 public class TransitiveLookupWriter implements LookupWriter {
     
     private static final Logger log = LoggerFactory.getLogger(TransitiveLookupWriter.class);
     private static final int maxSetSize = 150;
     
     private final LookupEntryStore entryStore;
     private final boolean explicit;
     
     public static TransitiveLookupWriter explicitTransitiveLookupWriter(LookupEntryStore entryStore) {
         return new TransitiveLookupWriter(entryStore, true);
     }
     
     public static TransitiveLookupWriter generatedTransitiveLookupWriter(LookupEntryStore entryStore) {
         return new TransitiveLookupWriter(entryStore, false);
     }
 
     private TransitiveLookupWriter(LookupEntryStore entryStore, boolean explicit) {
         this.entryStore = entryStore;
         this.explicit = explicit;
     }
     
     private static final Function<ContentRef, String> TO_URI = new Function<ContentRef, String>() {
         @Override
         public String apply(@Nullable ContentRef input) {
             return input.getCanonicalUri();
         }
     };
 
     @Override
     public void writeLookup(ContentRef subject, Iterable<ContentRef> equivalents, Set<Publisher> publishers) {
         writeLookup(subject.getCanonicalUri(), ImmutableSet.copyOf(Iterables.transform(filterContentPublishers(equivalents, publishers), TO_URI)), publishers);
     }
     
     public void writeLookup(final String subjectUri, Iterable<String> equivalentUris, final Set<Publisher> publishers) {
         Preconditions.checkNotNull(emptyToNull(subjectUri), "null subject");
         Set<String> newNeighbourUris = ImmutableSet.copyOf(equivalentUris);
         
         LookupEntry subject = entryFor(subjectUri);
 
         if(noChangeInNeighbours(subject, newNeighbourUris, publishers)) {
             return;
         }
         
         Set<LookupEntry> neighbours = entriesFor(newNeighbourUris);
 
         Map<LookupRef, LookupEntry> transitiveSet;
         try {
             transitiveSet = transitiveClosure(subject, neighbours);
         } catch (IllegalArgumentException iae) {
             log.info("Transitive set too large: " + subjectUri, iae);
             return;
         }
         
         final ImmutableSet<LookupRef> subjectRef = ImmutableSet.of(subject.lookupRef());
         for (LookupEntry equivalent : transitiveSet.values()) {
             Iterable<LookupRef> updatedNeighbours = null;
             if (equivalent.equals(subject)) {
                 updatedNeighbours
                     = updateSubjectNeighbours(subject, neighbours, publishers);
             } else if (publishers.contains(equivalent.lookupRef().publisher())) {
                 updatedNeighbours
                     = updateEquivalentsNeighbours(equivalent, neighbours, subjectRef);
             } else {
                 updatedNeighbours = relevantNeighbours(equivalent);
             }
             transitiveSet.put(equivalent.lookupRef(), updateRelevantNeighbours(equivalent, updatedNeighbours));
         }
 
         Set<LookupEntry> newLookups = recomputeTransitiveClosures(transitiveSet);
 
         for (LookupEntry entry : newLookups) {
             entryStore.store(entry);
         }
 
     }
 
     private Iterable<LookupRef> updateEquivalentsNeighbours(LookupEntry entry,
             Set<LookupEntry> neighbours, ImmutableSet<LookupRef> subjectRef) {
         Iterable<LookupRef> updatedNeighbours;
         if (neighbours.contains(entry)) {
             updatedNeighbours = Sets.union(relevantNeighbours(entry), subjectRef);
         } else {
             updatedNeighbours = Sets.difference(relevantNeighbours(entry), subjectRef);
         }
         return updatedNeighbours;
     }
 
     private boolean noChangeInNeighbours(LookupEntry subjectEntry, Set<String> newNeighbours,
             final Set<Publisher> publishers) {
         final Set<String> newNeighbourUris = ImmutableSet.<String>builder()
             .add(subjectEntry.uri()).addAll(newNeighbours).build();
         Set<LookupRef> currentNeighbours = 
             Sets.filter(relevantNeighbours(subjectEntry), 
                 MorePredicates.transformingPredicate(LookupRef.TO_SOURCE, Predicates.in(publishers)));
         Set<String> currentNeighbourUris = ImmutableSet.copyOf(transform(currentNeighbours,TO_ID));
         boolean noChange = currentNeighbourUris.equals(newNeighbourUris);
         if (!noChange) {
             log.trace("Equivalence change: {} -> {}", currentNeighbourUris, newNeighbourUris);
         }
         return noChange;
     }
 
     private LookupEntry updateRelevantNeighbours(LookupEntry entry,
             Iterable<LookupRef> neighbours) {
         return explicit ? entry.copyWithExplicitEquivalents(neighbours)
                         : entry.copyWithDirectEquivalents(neighbours);
     }
 
     private Iterable<LookupRef> updateSubjectNeighbours(LookupEntry subject,
             Set<LookupEntry> neighbours, final Set<Publisher> publishers) {
         Predicate<LookupRef> sourceFilter = MorePredicates.transformingPredicate(LookupRef.TO_SOURCE, not(in(publishers)));
         return Iterables.concat(
             Sets.filter(relevantNeighbours(subject), sourceFilter), 
             Iterables.transform(neighbours, LookupEntry.TO_SELF)
         );
     }
 
     private Set<LookupRef> relevantNeighbours(LookupEntry subjectEntry) {
         return explicit ? subjectEntry.explicitEquivalents() : subjectEntry.directEquivalents();
     }
 
     private Iterable<ContentRef> filterContentPublishers(Iterable<ContentRef> content, final Set<Publisher> publishers) {
         return Iterables.filter(content, new Predicate<ContentRef>() {
             @Override
             public boolean apply(ContentRef input) {
                 return publishers.contains(input.getPublisher());
             }
         });
     }
 
     private Set<LookupEntry> recomputeTransitiveClosures(Map<LookupRef, LookupEntry> lookups) {
         
         Set<LookupEntry> newLookups = Sets.newHashSet();
         for (LookupEntry entry : lookups.values()) {
             if (!newLookups.contains(entry)) {
                 Set<LookupRef> transitiveSet = Sets.newHashSet();
                 
                 Queue<LookupRef> direct = Lists.newLinkedList(neighbours(entry));
                 //Traverse equivalence graph breadth-first
                 while(!direct.isEmpty()) {
                     LookupRef current = direct.poll();
                     transitiveSet.add(current);
                     LookupEntry currentEntry = checkNotNull(lookups.get(current), "No lookup entry for " + current);
                     Iterables.addAll(direct, Iterables.filter(neighbours(currentEntry), not(in(transitiveSet))));
                 }
                 
                 for (LookupRef lookupRef : transitiveSet) {
                     newLookups.add(lookups.get(lookupRef).copyWithEquivalents(transitiveSet));
                 }
             }
         }
         return newLookups;
     }
 
     private Iterable<LookupRef> neighbours(LookupEntry current) {
         return Iterables.concat(current.directEquivalents(), current.explicitEquivalents());
     }
     
     private Set<LookupEntry> entriesFor(Iterable<String> equivalents) {
         return ImmutableSet.copyOf(entryStore.entriesForCanonicalUris(equivalents));
     }
 
     private LookupEntry entryFor(String subject) {
         return Iterables.getOnlyElement(entryStore.entriesForCanonicalUris(ImmutableList.of(subject)), null);
     }
 
     // Uses a work queue to pull out and map the transitive closures rooted at each entry in entries.
     private Map<LookupRef, LookupEntry> transitiveClosure(LookupEntry subjectEntry, Set<LookupEntry> entries) {
         
         Map<LookupRef, LookupEntry> transitiveClosure = Maps.newHashMap();
         Set<LookupRef> refs = Sets.newHashSet();
         
        for (LookupEntry entry : Iterables.concat(ImmutableList.of(subjectEntry), entries)) {
             transitiveClosure.put(entry.lookupRef(), entry);
             if (refs.size() + entry.equivalents().size() > maxSetSize) {
                 throw new IllegalArgumentException(size(subjectEntry, entries));
             }
             refs.addAll(entry.equivalents());
         }
         
         for (LookupEntry entry : entryStore.entriesForCanonicalUris(Iterables.transform(refs, LookupRef.TO_ID))) {
             transitiveClosure.put(entry.lookupRef(), entry);
         }
         
         return transitiveClosure;
     }
 
     private String size(LookupEntry subjectEntry, Set<LookupEntry> entries) {
         int size = subjectEntry.equivalents().size();
         for (LookupEntry lookupEntry : entries) {
             size += lookupEntry.equivalents().size();
         }
         return String.valueOf(size);
     }
 
 }
