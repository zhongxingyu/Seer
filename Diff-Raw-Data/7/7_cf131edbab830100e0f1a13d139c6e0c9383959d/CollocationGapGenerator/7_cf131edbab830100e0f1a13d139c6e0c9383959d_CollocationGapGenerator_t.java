 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.generators;
 
 import com.github.fhirschmann.clozegen.lib.generators.api.CollocationModelBasedGapGenerator;
 import com.github.fhirschmann.clozegen.lib.generators.api.Gap;
 import com.github.fhirschmann.clozegen.lib.generators.model.CollocationModel;
 import com.github.fhirschmann.clozegen.lib.generators.api.ListInputGapGenerator;
 import com.github.fhirschmann.clozegen.lib.util.CollectionUtils;
 import com.github.fhirschmann.clozegen.lib.util.MiscUtils;
 import com.github.fhirschmann.clozegen.lib.util.MultisetUtils;
 import com.google.common.base.Objects;
 import com.google.common.base.Objects.ToStringHelper;
 import com.google.common.base.Optional;
 import com.google.common.collect.ConcurrentHashMultiset;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Multiset.Entry;
 import com.google.common.collect.Sets;
 import java.util.List;
 import java.util.Set;
 import static com.google.common.base.Preconditions.checkNotNull;
 import org.javatuples.Triplet;
 
 /**
  * Generates gaps based on collocations.
  *
  * <p>
  * The argument to {@link CollocationGapGenerator#initialize(java.util.List)}
  * must have an odd number of elements and the element in the middle of the
  * list is the word a gap is generated for.
  * </p>
  *
  * <p>This implementation is based on the paper
  * <i>Automatic Generation of Cloze Items for Prepositions</i> [1]
  * by Lee et all.
  *
  * <p>[1] <b>J. Lee and S. Seneff</b>.<br/>
  * Automatic generation of cloze items for prepositions.<br/>
  * <i>In Eight Annual Conference of the International Speech Communication
  * Association, 2007</i>.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class CollocationGapGenerator
         implements ListInputGapGenerator, CollocationModelBasedGapGenerator {
     /**
      * The model for this generator.
      */
     private CollocationModel model;
 
     /**
      * The triplet (A, x, B) where x is the subject in question.
      */
     private Triplet<String, String, String> triplet;
 
     @Override
     public void initialize(final List<String> list) {
         triplet = CollectionUtils.triListJoin(list);
     }
 
     @Override
     public void initialize(final CollocationModel model) {
         this.model = checkNotNull(model);
     }
 
     @Override
     public Optional<Gap> generate(final int count) {
         checkNotNull(model);
         Gap gap = new Gap();
         gap.addValidAnswers(triplet.getValue1());
 
         // Collect a list of possible candidates for this gap
         final Multiset<String> candidates = ConcurrentHashMultiset.create(
                 MultisetUtils.mergeMultiSets(model.getTails().get(triplet.getValue2()),
                 model.getHeads().get(triplet.getValue0())));
 
        // Remove the correct answer from the candidate set
        candidates.remove(triplet.getValue1(), candidates.count(triplet.getValue1()));

         // Remove candidates p* which appear in the context (A, p*, B)
         for (Entry<String> entry : candidates.entrySet()) {
             if (model.getMultiset().contains(MiscUtils.WS_JOINER.join(
                     triplet.getValue0(), entry.getElement(), triplet.getValue2()))) {
                 candidates.remove(entry.getElement(), entry.getCount());
             }
         }
 
         if (candidates.elementSet().size() > count - 2) {
             final Set<String> invalidAnswers = Sets.newHashSet(
                     MultisetUtils.sortedElementList(candidates, count - 1));
             gap.addInvalidAnswers(invalidAnswers);
             return Optional.of(gap);
         } else {
             return Optional.absent();
         }
     }
 
     @Override
     public String toString() {
         final ToStringHelper str = Objects.toStringHelper(this);
         str.add("tuple", triplet.toString());
         str.add("model", model.toString());
         return str.toString();
     }
 }
