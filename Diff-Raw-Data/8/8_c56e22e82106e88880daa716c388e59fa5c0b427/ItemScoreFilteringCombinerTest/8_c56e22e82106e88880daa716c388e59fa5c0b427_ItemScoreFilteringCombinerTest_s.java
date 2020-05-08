 package org.atlasapi.equiv.results.combining;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
 import org.atlasapi.equiv.results.scores.Score;
 import org.atlasapi.equiv.results.scores.ScoredEquivalents;
 import org.atlasapi.equiv.update.ContainerEquivalenceUpdater;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 public class ItemScoreFilteringCombinerTest extends TestCase {
 
     public final Item target = target("target", "Target", Publisher.BBC);
     public final Item equivalent1 = target("equivalent1", "Equivalent1", Publisher.BBC);
     public final Item equivalent2 = target("equivalent2", "Equivalent2", Publisher.BBC);
     public final Item equivalent3 = target("equivalent3", "Equivalent3", Publisher.C4);
     public final Item equivalent4 = target("equivalent4", "Equivalent4", Publisher.C4);
     
     private Item target(String name, String title, Publisher publisher) {
         Item target = new Item(name+"Uri", name+"Curie", publisher);
         target.setTitle("Test " + title);
         return target;
     }
     
     public void testCombine() {
 
         List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                 DefaultScoredEquivalents.<Item>fromSource(ContainerEquivalenceUpdater.NAME).addEquivalent(equivalent1, Score.valueOf(1.0)).addEquivalent(equivalent2, Score.NULL_SCORE).addEquivalent(equivalent3, Score.valueOf(0.0)).build(),
                 DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent4, Score.NULL_SCORE).addEquivalent(equivalent2, Score.valueOf(1.0)).addEquivalent(equivalent3, Score.valueOf(1.0)).addEquivalent(equivalent1, Score.valueOf(1.0)).build(),
                 DefaultScoredEquivalents.<Item>fromSource("source3").addEquivalent(equivalent4, Score.valueOf(1.0)).addEquivalent(equivalent1, Score.NULL_SCORE).build()
         );
     
         
         ItemScoreFilteringCombiner<Item> combiner = new ItemScoreFilteringCombiner<Item>(new NullScoreAwareAveragingCombiner<Item>(), ContainerEquivalenceUpdater.NAME);
         
         ScoredEquivalents<Item> combined = combiner.combine(scores);
         
         assertEquals(ImmutableMap.of(
                 equivalent1, Score.valueOf(1.0),
                equivalent2, Score.NULL_SCORE
        ),combined.equivalents().get(Publisher.BBC));
        
        assertEquals(ImmutableMap.of(
                 equivalent3, Score.NULL_SCORE,
                 equivalent4, Score.NULL_SCORE
        ),combined.equivalents().get(Publisher.C4));
         
     }
 
 }
