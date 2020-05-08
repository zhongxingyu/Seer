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
  * You should have received validAnswers copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.annotators;
 
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.NonEmptyStringList;
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.uimafit.util.FSCollectionFactory;
 
 /**
  * Base class for all cloze item annotations.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public abstract class GapAnnotator extends JCasAnnotator_ImplBase {
 
     /**
      * Returns the word classes an annotator is working on.
      * <p>
      * This should be implemented by all inheriting classes. Depending on the underlying
      * tagger, the tags are most likely consistent with the Penn Treebank II Tags.
      * </p>
      *
      * @see <a href="http://bulba.sdsu.edu/jeanette/thesis/PennTags.html">Penn Treebank II Tags</a>
      * @return word class type
      */
     public abstract String[] getWantedTags();
 
     /**
      * Generates cloze tests item from validAnswers given subject.
      *
      * This method should generate a number of valid and invalid answers for a given
      * subject. For example, the subject "a" in the "articles" class might have "a" as
      * only valid answers and {"an","the"} as invalid answers.
      *
      * @param subject the word to generate validAnswers cloze test item for
      * @return valid and invalid answers for validAnswers gap
      */
     public abstract Gap generate(Annotation subject);
 
     /**
      * Process the annotator.
      *
      * This method will set up the annotation for words in a word class (as defined by the
      * extending classes) and call generate() in the extending class for each word in this
      * class.
      *
      * @param jcas the CAS to work on
      * @throws AnalysisEngineProcessException on errors during engine execution
      */
     @Override
     public final void process(final JCas jcas) throws AnalysisEngineProcessException {
         for (final Iterator<Annotation> i = jcas.getAnnotationIndex(
                 POS.type).iterator(); i.hasNext();) {
             final Annotation subject = i.next();
             final POS pos = (POS) subject;
 
             if (Arrays.asList(getWantedTags()).contains(pos.getPosValue())) {
 
                 final GapAnnotation annotation = new GapAnnotation(jcas);
 
                 annotation.setBegin(subject.getBegin());
                 annotation.setEnd(subject.getEnd());
 
                 final Gap pair = generate(subject);
 
                 if (pair == null) {
                     // Skip gap annotation if no gap was generated
                     continue;
                 }
 
                 final NonEmptyStringList invalidAnswers = (NonEmptyStringList)
                         FSCollectionFactory.createStringList(
                         jcas, pair.getInvalidAnswers());
                 annotation.setInvalidAnswers(invalidAnswers);
 
                 final NonEmptyStringList validAnswers = (NonEmptyStringList)
                         FSCollectionFactory.createStringList(
                         jcas, pair.getValidAnswers());
                 annotation.setValidAnswers(validAnswers);
 
                 annotation.addToIndexes();
             }
         }
     }
 }
