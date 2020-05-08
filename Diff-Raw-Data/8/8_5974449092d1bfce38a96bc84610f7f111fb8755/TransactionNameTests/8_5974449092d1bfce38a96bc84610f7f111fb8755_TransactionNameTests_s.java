 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved. Project Homepage:
  * http://blogs.cs.st-andrews.ac.uk/h2o H2O is free software: you can redistribute it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. H2O
  * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
  * Public License along with H2O. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.test;
 
 import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
 import static org.junit.Assert.assertThat;
 
 import org.h2o.util.TransactionNameGenerator;
 import org.junit.Test;
 
 public class TransactionNameTests {
 
     /**
      * Test that transaction names are correctly generated even when the number of transactions exceeds the maximum allowed long value.
      */
     @Test
     public void testGeneration() {
 
         // TODO this doesn't seem to check anything.
 
         final long lastNumber = Long.MAX_VALUE - 1000;
 
         final TransactionNameGenerator instance = new TransactionNameGenerator(null, lastNumber);
 
         for (long i = 0; i < 2000; i++) {
             instance.generateName();
         }
     }
 
     /**
      * Check that a null Database Instance parameter is handled without error.
      */
     @Test
     public void nullCheck() {
 
         final TransactionNameGenerator instance = new TransactionNameGenerator(null);
 
        assertThat(instance.generateName(), is(not(null)));
     }
 
     /**
      * Check that a null string parameter is handled without error.
      */
     @Test
     public void nullCheck2() {
 
        assertThat(TransactionNameGenerator.generateName((String) null), is(not(null)));
     }
 }
