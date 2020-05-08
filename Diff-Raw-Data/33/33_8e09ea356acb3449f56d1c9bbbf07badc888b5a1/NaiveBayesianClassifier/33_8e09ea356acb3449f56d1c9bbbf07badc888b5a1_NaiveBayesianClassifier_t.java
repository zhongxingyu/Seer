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
 
 import io.github.brinman2002.data.model.Attribute;
 import io.github.brinman2002.data.model.Outcome;
 import io.github.brinman2002.dofn.internal.AttributeOutcomeProbabilityCalculatingDoFn;
 import io.github.brinman2002.dofn.internal.ProbabilityCalculatingDoFn;
 import io.github.brinman2002.dofn.internal.RegroupOutcomeAttributeCountDoFn;
 
 import org.apache.crunch.PCollection;
 import org.apache.crunch.PTable;
 import org.apache.crunch.Pair;
 import org.apache.crunch.lib.Distinct;
 import org.apache.crunch.lib.Join;
 import org.apache.crunch.lib.PTables;
 import org.apache.crunch.types.avro.Avros;
 
 /**
  * Utility class providing methods to perform Naive Bayesian Classification.
  * 
  * @author brandon
  * 
  */
 public class NaiveBayesianClassifier {
 
     /**
      * Build training data from a PTable of Attribute instances and a PTable of
      * Outcome instances. The key of the table is used to join a given set of
      * attributes with the outcome that was observed. One convenient key is
      * milliseconds since Epoch, rounded to the nearest interval to group by.
      * However, no specific scheme for the key is enforced.
      * <p>
      * Callers may wish to limit one outcome per key, as multiple outcomes per
      * key will have identical attribute sets.
      * 
      * <p>
      * 
      * Calling this method will, at least partially, invoke the pipeline to
      * begin processing.
      * 
      * @param attributes
      *            PTable of attributes.
      * @param outcomes
      *            PTable of outcomes.
      * @return Training data.
      */
     public static Pair<PTable<Pair<Outcome, Attribute>, Double>, PTable<Outcome, Double>> train(final PTable<Long, Attribute> attributes,
             final PTable<Long, Outcome> outcomes) {
 
         final PTable<Outcome, Long> outcomeCounts = PTables.values(outcomes).count();
 
         // Join the outcomes and attributes on their key. Attributes are forced
         // to be distinct so that multiple occurrences of the same attribute
         // does not adversely weight the attribute.
         final PTable<Outcome, Attribute> attributesAssociatedToOutcome = PTables.asPTable(PTables.values(Join.innerJoin(outcomes,
                 Distinct.distinct(attributes))));
 
         // Count the number of Outcome/Attribute combinations and regroup by
         // the outcome. We are counting the number of times an attribute is
         // associated with an outcome so that we can calculate the probability
         // (by dividing this by the total count of the outcome).
         final PTable<Pair<Outcome, Attribute>, Long> countedEventAttributes = attributesAssociatedToOutcome.count();
         final PTable<Outcome, Pair<Attribute, Long>> attributeCountsByEvent = countedEventAttributes.parallelDo(
                 new RegroupOutcomeAttributeCountDoFn(),
                 Avros.tableOf(Avros.containers(Outcome.class), Avros.pairs(Avros.containers(Attribute.class), Avros.longs())));
 
         // This join is used to group together the count of outcomes with the
         // count of attributes.
         final PTable<Outcome, Pair<Pair<Attribute, Long>, Long>> joinedAttributeAndEventCounts = Join
                 .innerJoin(attributeCountsByEvent, outcomeCounts);
 
         final PTable<Pair<Outcome, Attribute>, Double> outcomeAttributeProbabilities = joinedAttributeAndEventCounts.parallelDo(
                 new AttributeOutcomeProbabilityCalculatingDoFn(),
                 Avros.tableOf(Avros.pairs(Avros.containers(Outcome.class), Avros.containers(Attribute.class)), Avros.doubles()));
 
         // final PTable<Outcome, Long> countedOutcomes =
         // PTables.keys(attributesAssociatedToOutcome).count();
 
         // This will trigger the pipeline so far, at least the parts related to
         // that PTable.
         final long totalOutcomeCount = outcomes.length().getValue();
         final PTable<Outcome, Long> countedOutcomes = PTables.values(outcomes).count();
 
        final PTable<Outcome, Double> outcomeProbabilities = countedOutcomes.parallelDo(new ProbabilityCalculatingDoFn<Outcome>(totalOutcomeCount),
                 Avros.tableOf(Avros.containers(Outcome.class), Avros.doubles()));
 
         final Pair<PTable<Pair<Outcome, Attribute>, Double>, PTable<Outcome, Double>> output = Pair.of(outcomeAttributeProbabilities,
                outcomeProbabilities);
         return output;
     }
 
     /**
      * Convenience method for {@link #predict(PTable, PTable)}.
      * 
      * @see {@link #predict(PTable, PTable)}
      * @param trainingResults
      * @return Predicted classification.
      */
     public Object predict(final Pair<PTable<Pair<Outcome, Attribute>, Double>, PTable<Attribute, Double>> trainingResults,
             final PCollection<Attribute> observedAttributes) {
         return predict(trainingResults.first(), trainingResults.second(), observedAttributes);
     }
 
     /**
      * Perform prediction based on the training results and a set of attributes.
      * 
      * @param outcomeAttributeProbabilities
      *            Probability of an attribute occurring given an outcome.
      * @param attributeProbabilities
      *            Probability of attribute occurring, independent of outcome.
      * @return Predicted classification.
      */
     public Object predict(final PTable<Pair<Outcome, Attribute>, Double> outcomeAttributeProbabilities,
             final PTable<Attribute, Double> attributeProbabilities, final PCollection<Attribute> observedAttributes) {
         // TODO not yet implemented
         return null;
     }
 }
