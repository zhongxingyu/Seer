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
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.LinkedHashMultiset;
 import com.google.common.collect.Multiset;
 import java.lang.reflect.Constructor;
 import junit.framework.TestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class MultisetUtilsTest extends TestCase {
 
     private Multiset<String> multiset1;
     private Multiset<String> multiset2;
 
     @Before
     public void setUp() {
         multiset1 = LinkedHashMultiset.create();
         multiset1.add("foo", 4);
         multiset1.add("bar", 9);
         multiset2 = LinkedHashMultiset.create();
         multiset2.add("foo", 42);
         multiset2.add("bar2", 11);
     }
 
     @Test
     public void testMergeMultiset() {
         Multiset<String> ms = MultisetUtils.mergeMultiSets(multiset1, multiset2);
         assertEquals(46, ms.count("foo"));
         assertEquals(9, ms.count("bar"));
         assertEquals(11, ms.count("bar2"));
     }
 
     @Test
     public void sortedElementList() {
         assertEquals(ImmutableList.of("bar", "foo"),
                 MultisetUtils.sortedElementList(multiset1));
     }
 
     @Test
     public void limitedSortedElementList() {
         assertEquals(ImmutableList.of("bar"),
                 MultisetUtils.sortedElementList(multiset1, 1));
     }
 
     /**
      * Hack to exclude the private constructor from code coverage metrics.
      */
     @Test
     public void testPrivateConstructor() throws Exception {
        Constructor<?>[] cons = MultisetUtils.class.getDeclaredConstructors();
         cons[0].setAccessible(true);
         cons[0].newInstance((Object[]) null);
     }
 }
