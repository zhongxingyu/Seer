 package org.atlasapi.equiv.scorers;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.atlasapi.equiv.generators.ItemResultContainerResolver;
 import org.atlasapi.equiv.results.EquivalenceResult;
 import org.atlasapi.equiv.results.description.ResultDescription;
 import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
 import org.atlasapi.equiv.results.scores.ScaledScoredEquivalents;
 import org.atlasapi.equiv.results.scores.Score;
 import org.atlasapi.equiv.results.scores.ScoredEquivalents;
 import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.logging.AdapterLog;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSet.Builder;
 import com.google.common.collect.Iterables;
 
 public class ContainerChildEquivalenceScorer implements ContentEquivalenceScorer<Container> {
 
     private static final int ITEM_SCORE_SCALER = 20;
     public static final String NAME = "Items";
     
     private final ContentEquivalenceUpdater<Item> itemUpdater;
     private final LiveEquivalenceResultStore childResultStore;
     private final ContentResolver resolver;
     private final ItemResultContainerResolver itemResultContainerResolver;
 
     public ContainerChildEquivalenceScorer(ContentEquivalenceUpdater<Item> itemUpdater, LiveEquivalenceResultStore childResultStore, ContentResolver resolver, AdapterLog log) {
         this.itemUpdater = itemUpdater;
         this.childResultStore = childResultStore;
         this.resolver = resolver;
         this.itemResultContainerResolver = new ItemResultContainerResolver(resolver, NAME, log);
     }
     
     @Override
     public ScoredEquivalents<Container> score(Container subject, Iterable<Container> suggestions, ResultDescription desc) {
         List<Item> childrenOfSuggestedContainers = childrenOf(suggestions);
         List<Item> childrenOfSubject = childrenOf(ImmutableList.of(subject));
         
         Builder<EquivalenceResult<Item>> childResults = ImmutableSet.builder();
         for (Item item : childrenOfSubject) {
             childResults.add(childResultStore.store(itemUpdater.updateEquivalences(item, Optional.of(childrenOfSuggestedContainers))));
         }
         return extractContainersFrom(childResults.build(), desc);
     }
     
     /* Calculates equivalence scores for the containers of items that are strongly equivalent to the items of the subject container.
      * Scores are normalized by the number of items in the container. 
      */
     private ScoredEquivalents<Container> extractContainersFrom(Set<EquivalenceResult<Item>> childResults, ResultDescription desc) {
 
         desc.startStage("Extracting containers from child results");
         
         ScoredEquivalents<Container> containerScores = itemResultContainerResolver.extractContainersFrom(childResults);
         
         ScaledScoredEquivalents<Container> scaled = ScaledScoredEquivalents.scale(containerScores, new Function<Double, Double>() {
             @Override
             public Double apply(Double input) {
                 return Math.min(1, input * ITEM_SCORE_SCALER);
             }
         });
         
         for (Entry<Container, Score> result : scaled.equivalents().entrySet()) {
             desc.appendText("%s (%s) scored %s", result.getKey().getTitle(), result.getKey().getCanonicalUri(), result.getValue());
         }
         desc.finishStage();
         
         return scaled;
     }
 
     public ImmutableList<Item> childrenOf(Iterable<Container> suggestions) {
        Iterable<String> childUris = Iterables.transform(Iterables.concat(Iterables.transform(suggestions, new Function<Container, Iterable<ChildRef>>() {
             @Override
             public Iterable<ChildRef> apply(Container input) {
                 return input.getChildRefs();
             }
        })), ChildRef.TO_URI);
         return ImmutableList.copyOf(Iterables.filter(resolver.findByCanonicalUris(childUris).getAllResolvedResults(), Item.class));
     }
 
     @Override
     public String toString() {
         return "Container Child scorer";
     }
     
 }
