 /**
  * Copyright 2013 Brandon Inman
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package io.github.brinman2002.pipeline;
 
 import static io.github.brinman2002.Helper.attribute;
 import static io.github.brinman2002.Helper.outcome;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 import io.github.brinman2002.data.model.Attribute;
 import io.github.brinman2002.data.model.Outcome;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.crunch.PTable;
 import org.apache.crunch.Pair;
 import org.apache.crunch.impl.mem.MemPipeline;
 import org.apache.crunch.types.PTableType;
 import org.apache.crunch.types.avro.Avros;
 import org.junit.Test;
 
 /**
  * Test of the NBC. FIXME this need more extensive tests.
  * 
  * @author brandon
  * 
  */
 public class NaiveBayesianClassifierTest {
 
     private static final PTableType<Long, Attribute> TABLE_OF_ATTRIBUTES = Avros.tableOf(Avros.longs(), Avros.containers(Attribute.class));
     private static final PTableType<Long, Outcome> TABLE_OF_OUTCOMES = Avros.tableOf(Avros.longs(), Avros.containers(Outcome.class));
     private static final Long MILLIS_IN_DAY = 1000L * 60 * 60 * 24;
 
     @Test
     public void train() {
         final PTable<Long, Attribute> attributes = MemPipeline.typedTableOf(TABLE_OF_ATTRIBUTES, attributes());
         final PTable<Long, Outcome> outcomes = MemPipeline.typedTableOf(TABLE_OF_OUTCOMES, outcomes());
 
         final Pair<PTable<Pair<Outcome, Attribute>, Double>, PTable<Outcome, Double>> trained = NaiveBayesianClassifier.train(attributes, outcomes);
         System.out.println(trained.first());
         System.out.println(trained.second());
         // TODO assert expected values

        final Collection<Pair<Outcome, Double>> outcomeProbabilities = trained.second().asCollection().getValue();

        assertEquals(4, outcomeProbabilities.size());
        assertTrue(outcomeProbabilities.contains(Pair.of(outcome("a"), Double.valueOf(0.5))));
        assertTrue(outcomeProbabilities.contains(Pair.of(outcome("b"), Double.valueOf(0.25))));
        assertTrue(outcomeProbabilities.contains(Pair.of(outcome("c"), Double.valueOf(0.125))));
        assertTrue(outcomeProbabilities.contains(Pair.of(outcome("d"), Double.valueOf(0.125))));
     }
 
     private Collection<Pair<Long, Outcome>> outcomes() {
         final List<Pair<Long, Outcome>> outcomes = new ArrayList<Pair<Long, Outcome>>();
 
         outcomes.add(Pair.of(day(1), outcome("a")));
         outcomes.add(Pair.of(day(2), outcome("b")));
         outcomes.add(Pair.of(day(3), outcome("b")));
         outcomes.add(Pair.of(day(4), outcome("a")));
         outcomes.add(Pair.of(day(5), outcome("a")));
         outcomes.add(Pair.of(day(6), outcome("a")));
         outcomes.add(Pair.of(day(7), outcome("c")));
         outcomes.add(Pair.of(day(8), outcome("d")));
 
         return outcomes;
     }
 
     private Collection<Pair<Long, Attribute>> attributes() {
         final List<Pair<Long, Attribute>> attributes = new ArrayList<Pair<Long, Attribute>>();
         // Only one outcome per block is currently permitted.
         attributes.add(Pair.of(day(1), attribute("a")));
         attributes.add(Pair.of(day(1), attribute("b")));
         attributes.add(Pair.of(day(1), attribute("c")));
 
         attributes.add(Pair.of(day(2), attribute("b")));
 
         attributes.add(Pair.of(day(3), attribute("c")));
 
         attributes.add(Pair.of(day(4), attribute("d")));
 
        attributes.add(Pair.of(day(5), attribute("g")));
 
        attributes.add(Pair.of(day(6), attribute("h")));
 
        attributes.add(Pair.of(day(7), attribute("i")));
 
        attributes.add(Pair.of(day(8), attribute("j")));
 
         return attributes;
     }
 
     private long day(final int i) {
         return MILLIS_IN_DAY * i;
     }
 }
