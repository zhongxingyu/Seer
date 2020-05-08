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
 package com.github.fhirschmann.clozegen.lib.util;
 
 import com.github.fhirschmann.clozegen.lib.type.GapAnnotation;
 import com.google.common.collect.Sets;
 import java.lang.reflect.Constructor;
 import java.util.Collection;
 import org.apache.uima.UIMAException;
 import org.apache.uima.jcas.JCas;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.uimafit.factory.JCasFactory;
 import org.uimafit.util.FSCollectionFactory;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class UIMAUtilsTest {
 
     @Test
     public void testCreateGapAnnotationAll() throws UIMAException {
         JCas aJCas = JCasFactory.createJCas();
         GapAnnotation gap = UIMAUtils.createGapAnnotation(aJCas,
                 Sets.newHashSet("true1"), Sets.newHashSet("false1", "false2"));
 
         Collection<String> list = FSCollectionFactory.create(gap.getAllAnswers());
         assertTrue(list.contains("true1"));
         assertTrue(list.contains("false1"));
         assertTrue(list.contains("false2"));
     }
 
     @Test
     public void testCreateGapAnnotationValid() throws UIMAException {
         JCas aJCas = JCasFactory.createJCas();
         GapAnnotation gap = UIMAUtils.createGapAnnotation(aJCas,
                 Sets.newHashSet("true1", "true2"), Sets.newHashSet("false1", "false2"));
 
         Collection<String> list = FSCollectionFactory.create(gap.getValidAnswers());
         assertTrue(list.contains("true1"));
         assertTrue(list.contains("true2"));
         assertFalse(list.contains("false2"));
     }
 
     /**
      * Hack to exclude the private constructor from code coverage metrics.
      */
     @Test
     public void testPrivateConstructor() throws Exception {
        Constructor<?>[] cons = UIMAUtils.class.getDeclaredConstructors();
         cons[0].setAccessible(true);
         cons[0].newInstance((Object[]) null);
     }
 }
