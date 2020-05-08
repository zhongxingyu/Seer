 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.grammar.state;
 
 import java.util.ArrayDeque;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Map;
 
     /**
  * DiGraph class implements the digraph algorithm described in FRANK DeREMER and 
  * THOMAS PENNELLO: Efficient Computation of LALR(1) Look-Ahead Sets
 * @see <a href="http://www.win.tue.nl/~wsinswan/softwaretools/material/DeRemer_Pennello.pdf">
 * FRANK DeREMER and 
 * THOMAS PENNELLO: Efficient Computation of LALR(1) Look-Ahead Sets
 * </a>
  * 
  * @author Timo Vesalainen
  */
 public class DiGraph<X extends Vertex>
 {
     private Deque<X> stack = new ArrayDeque<>();
     private Map<X,Integer> indexMap = new HashMap<>();
     private static final int INFINITY = 9999999;
 
     /**
      * This algorithm traverses all vertices and all edges once.
      * @param allX 
      */
     public void traverse(Collection<X> allX)
     {
         reset();
         for (X x : allX)
         {
             if (indexOf(x) == 0)
             {
                 traverse(x);
             }
         }
     }
     public void traverse(X x)
     {
         enter(x);
         stack.push(x);
 
         int d = stack.size();
         setIndexOf(x, d);
 
         int depth = traversedCount();
         Collection<X> c = x.edges();
         for (X s : c)
         {
             edge(x, s);
             if (indexOf(s) == 0)
             {
                 traverse(s);
             }
             setIndexOf(x, Math.min(indexOf(x), indexOf(s)));
         }
         if (indexOf(x) == d)
         {
             branch(x);
             X s = stack.peek();
             while (!s.equals(x))
             {
                 pop(s);
                 stack.pop();
                 setIndexOf(s, INFINITY);
                 s = stack.peek();
             }
             setIndexOf(x, INFINITY);
             stack.pop();
         }
         exit(x, traversedCount() - depth);
     }
     
     protected void enter(X x)
     {
     }
 
     protected void edge(X from, X to)
     {
     }
 
     protected void branch(X x)
     {
     }
 
     protected void pop(X s)
     {
     }
 
     protected void exit(X x, int depth)
     {
     }
 
     private void setIndexOf(X state, int index)
     {
         indexMap.put(state, index);
     }
     
     public boolean traversed(X x)
     {
         return indexMap.containsKey(x);
     }
     
     public int traversedCount()
     {
         return indexMap.size();
     }
 
     public int stackDepth()
     {
         return stack.size();
     }
     
     private int indexOf(X state)
     {
         Integer i = indexMap.get(state);
         if (i == null)
         {
             return 0;
         }
         else
         {
             return i;
         }
     }
     public void reset()
     {
         indexMap.clear();
         stack.clear();
     }
     /**
      * @param args the command line arguments
     public static void main(String[] args)    
     {
         try
         {
             DFA<Integer> dfa = Regex.createDFA("c(abd)*");
             DiGraph<DFAState<Integer>> d = new DiGraph<>();
             d.traverse(dfa);
             //dfa.dump(System.err);
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
         }
     }
      */
 
 }
