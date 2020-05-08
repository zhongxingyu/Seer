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
 package com.github.fhirschmann.clozegen.lib.annotators.en;
 
 import com.github.fhirschmann.clozegen.lib.annotators.AbstractPosTrigramAnnotator;
 import com.github.fhirschmann.clozegen.lib.multiset.MapMultiset;
 import com.github.fhirschmann.clozegen.lib.parser.FrequencyParser;
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.github.fhirschmann.clozegen.lib.util.MultisetUtils;
 import com.github.fhirschmann.clozegen.lib.util.PosUtils;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import com.google.common.base.Joiner;
 import com.google.common.collect.*;
 import com.google.common.collect.Multiset.Entry;
 import com.google.common.io.Resources;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.uima.UimaContext;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.NonEmptyStringList;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.util.FSCollectionFactory;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class PrepositionGapGenerator extends AbstractPosTrigramAnnotator {
 
     public static final String PARAM_MODEL_PATH = "ModelPath";
     @ConfigurationParameter(name = PARAM_MODEL_PATH, mandatory = true)
     private String modelPath;
 
     private Multiset<String> trigrams;
     private MapMultiset<String, String> after;
     private MapMultiset<String, String> before;
 
     private final static Joiner joiner = Joiner.on(" ");
 
     public final static int CHOICES_COUNT = 4;
 
     @Override
     public void initialize(UimaContext context) throws ResourceInitializationException {
         super.initialize(context);
         try {
             trigrams = FrequencyParser.parseMultiset(
                     Resources.getResource(modelPath + "/trigrams.txt"));
             after = FrequencyParser.parseMapMultiset(
                     Resources.getResource(modelPath + "/after.txt"), 0);
             before = FrequencyParser.parseMapMultiset(
                     Resources.getResource(modelPath + "/before.txt"), 1);
         } catch (IOException ex) {
             Logger.getLogger(PrepositionGapGenerator.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     @Override
     public void processTrigram(JCas aJCas, POS[] parts) {
         if (parts[1] instanceof PP) {
             final String[] strings = PosUtils.loweredWordsOrNULL(parts);
             final Multiset<String> candidates = ConcurrentHashMultiset.create(
                     MultisetUtils.mergeMultiSets(
                     before.get(strings[0]), after.get(strings[0])));
 
             for (Entry<String> entry : candidates.entrySet()) {
                 if (trigrams.contains(
                         joiner.join(strings[0], entry.getElement(), strings[1]))) {
                     candidates.remove(entry.getElement(), entry.getCount());
                 }
             }
             candidates.remove(strings[1], candidates.count(strings[1]));
 
             if (candidates.elementSet().size() > CHOICES_COUNT - 2) {
                 final Set<String> invalidAnswers = Sets.newHashSet(
                         MultisetUtils.sortedElementList(candidates, CHOICES_COUNT - 1));
 
                 GapAnnotation gap = UIMAUtils.createGapAnnotation(aJCas,
                         ImmutableSet.of(strings[1]), invalidAnswers);
                gap.setBegin(parts[1].getBegin());
                gap.setEnd(parts[1].getEnd());
                 gap.addToIndexes();
 
             }
 
         }
     }
 }
