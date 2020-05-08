 package org.jetbrains.plugins.xml.searchandreplace.search;
 
 import com.intellij.openapi.util.Condition;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.xml.XmlElement;
 import com.intellij.psi.xml.XmlTag;
 import com.intellij.psi.xml.XmlText;
 import com.intellij.util.containers.FilteringIterator;
 import org.jetbrains.plugins.xml.searchandreplace.search.predicates.Not;
 import org.jetbrains.plugins.xml.searchandreplace.search.predicates.XmlElementPredicate;
 
 import java.util.*;
 
 public class Pattern implements Cloneable {
 
   public static class Node {
     private XmlElementPredicate predicate;
     private Set<Node> children = new HashSet<Node>();
     private boolean theOne;
 
     public XmlElementPredicate getPredicate() {
       return predicate;
     }
 
     public Set<Node> getChildren() {
       return children;
     }
 
     public boolean isTheOne() {
       return theOne;
     }
 
     public Node(XmlElementPredicate predicate, boolean isTheOne) {
       this.predicate = predicate;
       theOne = isTheOne;
     }
 
     public void setChildren(Set<Node> children) {
       this.children = children;
     }
 
     @Override
     public String toString() {
       return "\nNode{" +
                      "\npredicate=" + predicate +
                      ",\n children=" + children +
                      ",\n theOne=" + theOne +
                      "\n}";
     }
 
     public boolean apply(XmlElement tag) {
       return predicate.apply(tag);
     }
 
     public boolean isNot() {
       return predicate instanceof Not;
     }
 
     public Node mutableNotNodeInstanceByRemovingChildren(Set<Node> successfulChildren) {
       if (successfulChildren.isEmpty()) return this;
       class NotNode extends Node {
         public NotNode(XmlElementPredicate predicate, boolean isTheOne) {
           super(predicate, isTheOne);
         }
 
         @Override
         public Node mutableNotNodeInstanceByRemovingChildren(Set<Node> successfulChildren) {
           return this;
         }
       }
       NotNode n = new NotNode(predicate, isTheOne());
       Set<Node> newChildrenSet = new HashSet<Node>();
       newChildrenSet.addAll(children);
       newChildrenSet.removeAll(successfulChildren);
       n.setChildren(newChildrenSet);
       return n;
     }
 
     public void setPredicate(XmlElementPredicate predicate) {
       this.predicate = predicate;
     }
 
     static class NeverSuccessfullNode extends Node {
       public NeverSuccessfullNode(boolean isTheOne) {
         super(new XmlElementPredicate() {
 
           @Override
           public boolean apply(XmlElement element) {
             return false;
           }
 
         }, isTheOne);
       }
     }
 
     public Node neverSuccessfullNode() {
       NeverSuccessfullNode result = new NeverSuccessfullNode(isTheOne());
       result.setChildren(children);
       return result;
     }
   }
 
   private HashSet<Node> roots;
   private HashMap<Node, Integer> parentsNum;
 
   public Node getTheOne() {
     return theOne;
   }
 
   private Node theOne;
   private XmlElement candidate;
 
   public Set<Node> getAllNodes() {
     return allNodes;
   }
 
   private HashSet<Node> allNodes;
 
   public Pattern(HashSet<Node> nodes) {
     allNodes = nodes;
     validateNodes();
     for (Node n : allNodes) {
       if (n.isTheOne()) {
         theOne = n;
         break;
       }
     }
   }
 
   private Iterable<Node> childrenOfNode(final Node n) {
     return new Iterable<Node>() {
       public Iterator<Node> iterator() {
         return new FilteringIterator<Node, Node>(n.children.iterator(), new Condition<Node>() {
           public boolean value(Node node) {
             return allNodes.contains(node);
           }
         });
       }
     };
   }
 
   private int getParentsNum(Node n) {
     Integer num = parentsNum.get(n);
     return num == null ? 0 : num;
   }
 
   private void setParentsNum(Node n, int num) {
     parentsNum.put(n, num);
   }
 
   public void validateNodes() {
     roots = (HashSet<Node>) allNodes.clone();
     parentsNum = new HashMap<Node, Integer>();
     for (Node n : allNodes) {
       if (n.isTheOne()) {
         theOne = n;
       }
       for (Node c : childrenOfNode(n)) {
         roots.remove(c);
         setParentsNum(c, getParentsNum(c) + 1);
       }
     }
   }
 
   @Override
   public String toString() {
     return "\nPattern{" +
                    "\nroots=" + roots +
                    ",\n parentsNum=" + parentsNum +
                    ",\n theOne=" + theOne +
                    ",\n candidate=" + candidate +
                    ",\n allNodes=" + allNodes +
                    "}\n";
   }
 
   public final Pattern clone() {
     Pattern result = null;
     try {
       result = (Pattern) super.clone();
       result.candidate = candidate;
       result.theOne = theOne;
       result.roots = (HashSet<Node>) roots.clone();
       result.parentsNum = (HashMap<Node, Integer>) parentsNum.clone();
       result.allNodes = (HashSet<Node>) allNodes.clone();
     } catch (CloneNotSupportedException e) {
       e.printStackTrace();
     }
     return result;
   }
 
   private void rm(Pattern other) {
     assert candidate == null || other.candidate == null || other.candidate == candidate : "FUCKEN FUCK!!";
     boolean changed, onceChanged = false;
     do {
       changed = false;
       for (Node n : allNodes) {
         if (!other.allNodes.contains(n)) {
           allNodes.remove(n);
           changed = true;
           onceChanged = true;
           break;
         }
       }
     } while (changed);
     if (onceChanged) {
       validateNodes();
     }
   }
 
   private void removeRoot(Node root) {
     roots.remove(root);
     for (Node child : childrenOfNode(root)) {
       setParentsNum(child, getParentsNum(child) - 1);
       if (getParentsNum(child) == 0) {
         roots.add(child);
       }
     }
     allNodes.remove(root);
   }
 
   private boolean repair(Node n) {
     boolean changed = false;
    for (Node c : childrenOfNode(n)) {
       if (!allNodes.contains(c)) {
         allNodes.add(c);
         repair(c);
         changed = true;
       }
     }
     return changed;
   }
 
   private Pattern repair() {
     Pattern result = clone();
     if (result.repair(theOne)) {
       result.validateNodes();
     }
     return result;
   }
 
   private void replaceRoot(Node root, Node newRoot) {
     if (roots.contains(root)) {
       removeRoot(root);
       allNodes.add(newRoot);
       roots.add(newRoot);
       for (Node child : childrenOfNode(newRoot)) {
         if (roots.contains(child)) {
           roots.remove(child);
         }
         setParentsNum(child, getParentsNum(child) + 1);
       }
     }
   }
 
   private static boolean isEmpty(Iterable i) {
     for (Object o : i) {
       return false;
     }
     return true;
   }
 
   private boolean reduce(XmlElement tag, Node node) {
     if (node.apply(tag)) {
       if (node.isTheOne()) {
         candidate = tag;
       }
       if (node.isNot()) {
         Set<Node> successfullChildren = new HashSet<Node>();
         for (Node child : childrenOfNode(node)) {
           if (reduce(tag, child)) {
             successfullChildren.add(child);
           }
         }
         Node newRoot = node.mutableNotNodeInstanceByRemovingChildren(successfullChildren);
         if (isEmpty(childrenOfNode(newRoot))) {
           removeRoot(node);
           return true;
         } else {
           replaceRoot(node, newRoot);
         }
 
       } else {
         removeRoot(node);
         return true;
       }
     } else if (node.isNot()) {
       replaceRoot(node, node.neverSuccessfullNode());
     }
     return false;
   }
 
   private Pattern reduced(XmlElement tag) {
     Pattern reduced = this.clone();
     for (Node root : roots) {
       reduced.reduce(tag, root);
     }
     return reduced;
   }
 
   private static Map<XmlElement, Set<Pattern>> classifyByCandidate(Set<Pattern> patterns) {
     Map<XmlElement, Set<Pattern>> sort = new HashMap<XmlElement, Set<Pattern>>();
     for (Pattern p : patterns) {
       if (p.candidate != null) {
         Set<Pattern> s = sort.get(p.candidate);
         if (s == null) {
           s = new HashSet<Pattern>();
         }
         s.add(p);
         sort.put(p.candidate, s);
       }
     }
     for (Pattern p : patterns) {
       if (p.candidate == null) {
         for (XmlElement key : sort.keySet()) {
           sort.get(key).add(p);
         }
       }
     }
     return sort;
   }
 
   private static Set<Pattern> mergePatterns(Set<Pattern> patterns) {
     Map<XmlElement, Set<Pattern>> sort = classifyByCandidate(patterns);
     Set<Pattern> result = new HashSet<Pattern>();
     for (XmlElement aCandidate : sort.keySet()) {
       Set<Pattern> patternsForCandidate = sort.get(aCandidate);
       Pattern newPattern = null;
       for (Pattern p : patternsForCandidate) {
         if (newPattern == null) {
           newPattern = p;
         } else {
           newPattern.rm(p);
         }
       }
       if (newPattern != null) {
         result.add(newPattern);
       }
     }
     return result;
   }
 
   private boolean isEmptyOrContainsOnlyNot() {
     boolean result = true;
     for (Node n : allNodes) {
       if (!(n instanceof Node.NeverSuccessfullNode)) {
         result = false;
         break;
       }
     }
     return result;
   }
 
   private static Set<Pattern> matchChildren(XmlElement element, TagSearchObserver observer, Set<Pattern> patternSet) {
     Set<Pattern> afterChildrenMatching = new HashSet<Pattern>();
     for (PsiElement c : element.getChildren()) {
       if (c instanceof XmlTag || c instanceof XmlText) {
         XmlElement child = (XmlElement) c;
         for (Pattern p : patternSet) {
           Set<Pattern> matchChildResult = p.match(child, observer);
           afterChildrenMatching.addAll(matchChildResult);
         }
       }
     }
     if (afterChildrenMatching.isEmpty()) {
       return patternSet;
     }
     return mergePatterns(afterChildrenMatching);
   }
 
   public Set<Pattern> match(XmlElement element, TagSearchObserver observer) {
     Pattern reduced = this.reduced(element);
     Set<Pattern> forFurtherMatching = new HashSet<Pattern>();
     forFurtherMatching.add(reduced);
     if (reduced.candidate != element && getParentsNum(theOne) == 0 && theOne.predicate.apply(element)) {
       Pattern repaired = reduced.repair();
       repaired.candidate = element;
       forFurtherMatching.add(repaired);
     }
     Set<Pattern> result = matchChildren(element, observer, forFurtherMatching);
     for (Pattern p : result) {
       if (p.isEmptyOrContainsOnlyNot()) {
         if (p.candidate != null) {
           observer.elementFound(p.candidate);
         }
       }
     }
     return result;
   }
 }
