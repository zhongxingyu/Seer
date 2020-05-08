 /*
  *  Copyright 2011 Visural.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.visural.common.collection;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import junit.framework.TestCase;
 
 public class TopologicalSortTest extends TestCase {
     
     public void testSort() {
         Node a = new Node("a");
         Node b = new Node("b");
         Node c = new Node("c");
         Node d = new Node("d");
         Node e = new Node("e");
         Node o = new Node("o");
         d.setFollows(c,b);
         a.setPrecedes(b);
         d.setPrecedes(e);
         e.setFollows(b);
        o.setPrecedes(a);
        b.setPrecedes(c);
         TopologicalSort<Node> ts = new TopologicalSort(a, b, c, d, e, o);
         String order = ts.evaluationOrder().toString();
         System.out.println(order);
        assertTrue(order.equals("[o, a, b, c, d, e]"));
     }    
     
     public void testCycle() {
         Node a = new Node("a");
         Node b = new Node("b");
         Node c = new Node("c");
         Node d = new Node("d");
         Node e = new Node("e");
         Node o = new Node("o");
         d.setFollows(c,b);
         a.setPrecedes(b);
         d.setPrecedes(e, a);
         e.setFollows(b);
         TopologicalSort<Node> ts = new TopologicalSort(a, b, c, d, e, o);
         assertTrue(ts.isCyclic());
     }
     
     public static class Node implements Directional<Node> {
         private final String name;
 
         public Node(String name) {
             this.name = name;
         }
 
         public String getName() {
             return name;
         }
         
         private Set<Node> precedes = new HashSet();
         private Set<Node> follows = new HashSet();
 
         public void setFollows(Node... afters) {
             follows.clear();
             follows.addAll(Arrays.asList(afters));
         }
 
         public void setPrecedes(Node... befores) {
             precedes.clear();
             precedes.addAll(Arrays.asList(befores));
         }
                 
         public Collection<Node> follows() {
             return follows;
         }
 
         public Collection<Node> precedes() {
             return precedes;
         }
 
         @Override
         public String toString() {
             return name;
         }
         
     }
 }
