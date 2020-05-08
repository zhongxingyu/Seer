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
 import graphviz.GraphvizNode;
 import aiml.classifier.PaternSequence.PatternIterator;
 import aiml.classifier.node.PatternNode;
 import aiml.context.Context;
 
 /**
  * <p>
  * This class represents a node in the tree of contexts. Each context imposes
  * another constraint on the state of the matching system.
  * </p>
  * 
  * @author Kim Sullivan
  * @version 1.0
  */
 
 public abstract class ContextNode implements GraphvizNode {
 
   /** The context this tree applies to */
   Context<? extends Object> context;
 
   /** The classifier this context node is a part of */
   Classifier classifier;
 
   /**
    * the next lower context (subcontext), used when we fail matching to this
    * context's pattern tree or if there wasn't any pattern tree in the first
    * place (the latter shouldn't happen - if there are no patterns, there should
    * not be a context tree for them)
    */
   ContextNode next;
 
   protected ContextNode(Classifier classifier, Context<? extends Object> context) {
     this.classifier = classifier;
     this.context = context;
   }
 
   /**
    * Adds the remaining patterns in a sequence to this node. Creates all
    * necessary pattern subtrees and subcontexts, ensures that the proper order
    * of contexts is maintained.
    * 
    * @param patterns
    *          remaining patterns in the sequence
    * @param o
    *          the object to be added to the tree
    * @throws DuplicatePathException
    * @return the resulting context tree, with all modifications applied and the
    *         correct ordering.
    */
   public ContextNode add(PatternIterator patterns, Object o)
       throws DuplicatePathException {
     /*check the context order, if it's OK to add the current pattern to this context
      *or if we need to add a new context somewhere.
      *if we're adding it here, run the rootnode's add() method and
      * recursively construct the pattern tree for this context.
      */
     if (patterns.hasNext()) { //We're in the middle of the sequence
 
       PaternSequence.Pattern pattern = patterns.peek();
       if (context.compareTo(pattern.getContext()) < 0) {
         //add as next
         if (next == null) {
           next = pattern.getContext().createClassifierNode(classifier,
               patterns, null, o);
         } else {
           next = next.add(patterns, o);
         }
         return this;
       } else if (context.compareTo(pattern.getContext()) > 0) {
         //add instead of self
        return pattern.getContext().createClassifierNode(classifier, patterns,
            this, o);
       } else {
         //add the pattern into the current tree.
         PatternNode leaf = addPattern(pattern);
         patterns.next();
         leaf.addContext(patterns, o);
         return this;
       }
     } else { //we're at the end of the sequence, so we can add a Leaf node
       if (next == null) {
         next = new LeafContextNode(classifier, o);
       } else {
         next = next.add(patterns, o);
       }
       return this;
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
   public abstract PatternNode addPattern(PaternSequence.Pattern pattern);
 
   public Classifier getClassifier() {
     return classifier;
   }
 
   /**
    * Try to match the current match state.
    * 
    * @param match
    *          MatchState
    * @return <code>true</code> if a match was found; <code>false</code> if the
    *         match failed
    */
   public abstract boolean match(MatchState match);
 
   /** Returns a string representation of this context node */
   public String toString() {
     return "<" + context + ">" + "\n" + "[" + context + ".NEXT]: " + next;
   }
 
   public String gvNodeID() {
     return "ContextNode_" + context + "_" + hashCode();
   }
 
   public String gvNodeLabel() {
     return "<" + context + ">";
   }
 
   public void gvGraph(Graphviz graph) {
     gvNodes(graph);
     gvInternalGraph(graph);
     gvExternalGraph(graph);
   }
 
   public void gvNodes(Graphviz graph) {
     graph.node(gvNodeID(), "label", gvNodeLabel());
   }
 
   public void gvInternalGraph(Graphviz graph) {
   }
 
   public void gvExternalGraph(Graphviz graph) {
     if (next != null) {
       graph.edge(gvNodeID(), next.gvNodeID());
       next.gvGraph(graph);
     }
   }
 
 }
