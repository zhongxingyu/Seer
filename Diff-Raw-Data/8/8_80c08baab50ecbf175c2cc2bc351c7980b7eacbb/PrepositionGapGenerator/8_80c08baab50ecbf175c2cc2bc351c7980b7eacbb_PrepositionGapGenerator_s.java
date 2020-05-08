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
 
 import com.github.fhirschmann.clozegen.lib.annotators.AbstractGapGenerator;
 import com.github.fhirschmann.clozegen.lib.annotators.Gap;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
 import org.apache.uima.UimaContext;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class PrepositionGapGenerator extends AbstractGapGenerator {
 	public void initialize(UimaContext context) throws ResourceInitializationException {
        setFilterPosTag(PR.type);
         setLanguageCode("en");
     }
 
     @Override
     public Gap generate(final Annotation subject) {
         final Gap gap = new Gap();
         gap.getValidAnswers().add(subject.getCoveredText());
         gap.getInvalidAnswers().add(subject.getCoveredText());
 
         return gap;
     }
 }
