 /*
     jaiml - java AIML library
     Copyright (C) 2004-2005  Kim Sullivan
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package aiml.classifier;
 
 import aiml.classifier.node.EndOfStringNode;
 import aiml.classifier.node.PatternNodeFactory;
 import aiml.classifier.node.StringNode;
 import aiml.classifier.node.WildcardNode;
 
 /**
  * <p>This class encapsulates all AIML pattern matching functionality.</p>
  *
  *
  * @author Kim Sullivan
  * @version 1.0
  */
 
 public class Classifier {
   /** The root context tree */
   private static ContextNode tree;
 
   /** The number of paths in the tree */
   private static int count = 0;
 
   /**
    * Creates an instance of the aiml matcher. Since this class is meant to be
    * static, it's hidden. More robust techniques for making it a singleton might
    * be used in the future.
    */
   private Classifier() {
   }
 
   /**
    * Match the current context state to the paths in the tree.
    * @return a complete match state if succesfull; <code>null</code> otherwise
    */
   public static MatchState match() {
     MatchState m = new MatchState();
 
     if (tree != null && tree.match(m)) {
       return m;
     }
     else {
       return null;
     }
   }
 
   /**
    * Add a path to the matching tree.
    * @param path the path to be added
    * @param o the object to be stored
    * @throws DuplicatePathException
    */
   public static void add(Path path, Object o) throws DuplicatePathException {
     assert (PatternNodeFactory.getCount()>0) : "You have to register node types";
     if (tree == null) {
       if (path.getLength() != 0) {
        tree = new PatternContextNode(path.iterator(), o);
       }
       else {
         tree = new LeafContextNode(o);
       }
     }
     else {
      tree = tree.add(path.iterator(), o);
     }
     count++; //this is OK, because if the path isn't added, an exception gets thrown before we reach this
   }
 
   /**
    * Returns the number of loaded patterns.
    * @return the number of loaded patterns.
    */
   public static int getCount() {
     return count;
   }
 
   /**
    * <p>Resets the whole matching tree. This is usefull when the order of contexts
    * needs to be changed, because this invalidates the whole data structure.</p>
    * <p>This must follow after resetting the ContextInfo structure, but can be used
    * as a stand-alone method to remove all patterns from the matching tree.</p>
    * @see aiml.context.ContextInfo#reset()
    */
   public static void reset() {
     tree = null;
     count = 0;
   }
   
   /**
    * <p>This is a convenience method to register the default (or hopefully most
    * optimal) node handler classes. When using this method, you don't have to think
    * about all the different aiml.classifier.node.* implementations.</p>
    * @see aiml.classifier.node
    */
   public static void registerDefaultNodeHandlers() {
     StringNode.register();
     EndOfStringNode.register();
     WildcardNode.register();    
   }
 }
