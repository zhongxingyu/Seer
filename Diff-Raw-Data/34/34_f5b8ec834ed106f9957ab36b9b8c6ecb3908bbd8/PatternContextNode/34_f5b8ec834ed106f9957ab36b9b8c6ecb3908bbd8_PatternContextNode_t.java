 /*
     jaiml - java AIML library
     Copyright (C) 2004, 2009  Kim Sullivan
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package aiml.classifier;
 
 import graphviz.Graphviz;

import java.util.NoSuchElementException;

 import aiml.classifier.node.PatternNode;
 
 /**
  * <p>
  * This class represents a node in the tree of contexts. Each context node
  * imposes another constraint on the state of the matching system.
  * </p>
  * 
  * @author Kim Sullivan
  * @version 1.0
  */
 
 public class PatternContextNode extends ContextNode {
   /** The subtree of pattern nodes */
   private PatternNode tree;
 
   /**
    * Create a new context tree from the current pattern in the sequence. Adds
    * all it's substructures (subcontexts and pattern trees), together with the
    * final object.
   * <p>
   * Note that it's impossible to create a new PatternContextNode using an empty
   * sequence of patterns (if <code>patterns.hasNext()</code> returns false). In
   * such cases, a <code>NoSuchElementException</code> will be thrown.
    * 
    * @param patterns
    *          the remaining patterns in the sequence
   * @param next
   *          the next context to try if this context node fails to match
    * @param o
    *          Object
   * @throws NoSuchElementException
   *           if the <code>patterns</code> parameter is an empty sequence
    */
   public PatternContextNode(Classifier classifier,
      PaternSequence.PatternIterator patterns, ContextNode next, Object o) {
     super(classifier, patterns.peek().getContext());
    this.next = next;
     try {
       add(patterns, o);
     } catch (DuplicatePathException e) {
       assert false : "Duplicate path exception after adding a single sequence to a newly created empty tree - should never happen";
     }
   }
 
   /**
    * Adds a pattern to this context.
    * 
    * <i>Note:</i> This method will be reworked once generalized context types
    * are implemented (i.e. it will be replaced by an addGeneralContextType()
    * method.
    * 
    * @param pattern
    *          Pattern
    */
   public PatternNode addPattern(PaternSequence.Pattern pattern) {
     //add the pattern into the current tree.
     String s = pattern.getPattern();
     if (tree == null) {
       tree = classifier.getPNF().getInstance(this, 0, s);
     }
     PatternNode.AddResult result = tree.add(0, s);
     assert (result.newDepth == s.length()) : "A pattern node tree has failed to add a pattern completely";
     tree = result.root; //update the pattern subtree
     return result.leaf;
 
   }
 
   /**
    * Try to match the current match state.
    * 
    * @param match
    *          MatchState
    * @return <code>true</code> if a match was found; <code>false</code> if the
    *         match failed
    */
   public boolean match(MatchState match) {
     match.addContext(context);
 
     if (!tree.match(match)) {
       match.dropContext();
       if (next != null) {
         return next.match(match);
       } else {
         return false;
       }
     } else {
       return true;
     }
 
   }
 
   /** Returns a string representation of this context node */
   public String toString() {
     return "<" + context + ">" + tree + "\n" + "[" + context + ".NEXT]: " +
         next;
   }
 
   public void gvGraph(Graphviz graph) {
     graph.start(("subgraph cluster_" + context + "_" + hashCode()));
     super.gvGraph(graph);
     graph.end();
   }
 
   public void gvNodes(Graphviz graph) {
     graph.node(gvNodeID(), "label", "<" + context + ">", "shape", "diamond");
   }
 
   public void gvInternalGraph(Graphviz graph) {
     if (tree != null) {
       graph.edge(gvNodeID(), tree.gvNodeID());
       tree.gvGraph(graph);
     }
   }
 
 }
