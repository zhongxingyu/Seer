 /*
  * Copyright 2013 Webster Smalley
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.webstersmalley.countdown.numbers;
 
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 
 public class NumbersSolverTest {
 
     @Test
     public void numbersSolverSinglePermutationTest() {
         List<Integer> input = new ArrayList<Integer>();
         input.add(25);
         input.add(4);
         input.add(1);
 
         NumbersSolver solver = new NumbersSolver();
         Set<Node> nodes = solver.getNodes(input);
 
        assertEquals(16, nodes.size());
 
     }
 
     @Test
     public void testPermuter() {
         NumbersSolver solver = new NumbersSolver();
         List<Integer> numbers = new ArrayList<>();
         numbers.add(9);
         numbers.add(8);
         numbers.add(50);
     }
 
     @Test
     public void numbersSolverNotUsingAllNumbersTest() {
 
         NumbersBean bean = new NumbersBean("75,50,8,3,9,8,979");
         NumbersSolver solver = new NumbersSolver();
         List<NumbersSolution> solutions = solver.solve(bean, 0);
         assertEquals(1, solutions.size());
     }
 
     @Test
     public void numbersSolverTest() {
         NumbersBean bean = new NumbersBean("1,25,4,101");
 
         NumbersSolver solver = new NumbersSolver();
         List<NumbersSolution> solutions = solver.solve(bean, 0);
 
         assertEquals(1, solutions.size());
 
     }
 
 }
