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
 package com.github.fhirschmann.clozegen.lib.adapters.api;
 
 import com.github.fhirschmann.clozegen.lib.generators.api.GapGenerator;
 import java.util.List;
 import org.apache.uima.jcas.tcas.Annotation;
 
 /**
  * This interface represents a class which provides an adapter to the gap
  * generating algorithms.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public interface GeneratorAdapter {
     /**
      * This method gets called for each word in a sentence which matches
     * {@link ConstraintProvider#getConstraint()}.
      *
      * @param annotationList the list of annotations in the current sentence
      * @param offset the offset (index) of the word to generate a gap for
      * @return a gap generator
      */
     GapGenerator generator(List<Annotation> annotationList, int offset);
 }
