 package edu.kpi.pzks.core.model;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Class that incapsulates all links inside the graph.
  *
  * @author Aloren
  */
 public class Links extends HashSet<Link> {
 
     private static final long serialVersionUID = 2;
 
     public Links() {
         super(10);
     }
 
     public Links(Collection<Link> links) {
         super(links);
     }
 
     @Override
     public boolean add(Link link) {
         return super.add(link);
     }
 
     //TODO is it possible to have more than 1 link between 2 nodes?
     public void removeLinkBetweenNodes(Node fromNode, Node toNode) {
         List<Link> linksToRemove = new LinkedList<>();
         for (Link link : this) {
             if (link.getFromNode().equals(fromNode)
                     && link.getToNode().equals(toNode)) {
                 linksToRemove.add(link);
             }
         }
         removeAllLinks(linksToRemove);
     }
 
     public boolean remove(Link link) {
         link.removeFromOutputNodes();
         link.removeFromInputNodes();
         return super.remove(link);
     }
 
     public void removeAllLinksForNode(Node node) {
         List<Link> linksToRemove = new LinkedList<>();
         for (Link link : this) {
             if (link.getFromNode().equals(node)
                     || link.getToNode().equals(node)) {
                 linksToRemove.add(link);
             }
         }
         removeAllLinks(linksToRemove);
     }
    
     public boolean containsLinkBetween(Node fromNode, Node toNode) {
         for (Link link : this) {
             if (link.getFromNode().equals(fromNode)
                     && link.getToNode().equals(toNode)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean contains(Link link) {
         return super.contains(link);
     }
 
     private void removeAllLinks(Collection<Link> linksToRemove) {
         for (Link link : linksToRemove) {
             remove(link);
         }
     }
 }
